package com.example.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.dto.RegistrationCancelRequest;
import com.example.dto.RegistrationCreateRequest;
import com.example.entity.Patient;
import com.example.entity.RegistrationOrder;
import com.example.enums.RegistrationOrderStatus;
import com.example.exception.BusinessException;
import com.example.mapper.RegistrationOrderMapper;
import com.example.service.PatientService;
import com.example.service.RegistrationOrderService;
import com.example.service.ScheduleService;
import com.example.vo.RegistrationOrderVO;
import com.example.vo.ScheduleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
/*
 * 挂号订单业务实现类。
 * RegistrationOrderController 调用 RegistrationOrderService，真正的挂号规则写在这里。
 * 它还会调用 PatientService 检查患者、调用 ScheduleService 查询排班和扣减/归还号源。
 * ServiceImpl<RegistrationOrderMapper, RegistrationOrder> 负责连接 Mapper 和 registration_order 表。
 */
public class RegistrationOrderServiceImpl
        extends ServiceImpl<
        RegistrationOrderMapper,
        RegistrationOrder
        >
        implements RegistrationOrderService {

    // 保存患者业务接口，创建挂号订单时需要查询患者档案。
    private final PatientService patientService;

    // 保存排班业务接口，创建或取消订单时需要查询、扣减和归还号源。
    private final ScheduleService scheduleService;

    // Spring 创建本类时，会把患者服务和排班服务的实现对象传进来。
    public RegistrationOrderServiceImpl(
            PatientService patientService,
            ScheduleService scheduleService) {

        // 把传进来的 PatientService 保存到成员变量。
        this.patientService = patientService;
        // 把传进来的 ScheduleService 保存到成员变量。
        this.scheduleService = scheduleService;
    }

    // 对应 RegistrationOrderService 中的 create，用来创建挂号订单。
    @Override
    // 开启事务：本方法中订单保存、号源扣减只要有一步失败，已经执行的数据库操作都会回滚。
    @Transactional(rollbackFor = Exception.class)
    public Long create(
            RegistrationCreateRequest request) {

        // 从创建 DTO 取出 requestId 并去空格；它是前端为本次请求生成的唯一标识。
        String requestId = request.getRequestId().trim();

        // 相同请求再次提交时，直接返回原来的订单
        RegistrationOrder requestOrder = lambdaQuery()
                // 查询 registration_order 表中的 request_id。
                .eq(
                        RegistrationOrder::getRequestId,
                        requestId
                )
                // one 表示最多取一条记录，并用 RegistrationOrder 实体接收。
                .one();

        // 不为 null 表示同一个前端请求之前已经成功创建过订单。
        if (requestOrder != null) {
            // 不再重复扣号源和插入订单，直接返回原订单 id，这就是创建接口的幂等。
            return requestOrder.getId();
        }

        // 调用 PatientService.getDetail，根据 DTO 中的 patientId 查询患者。
        Patient patient =
                patientService.getDetail(
                        request.getPatientId()
                );

        // PatientService 已保证患者存在；这里继续检查患者档案是否停用。
        if (Integer.valueOf(0).equals(patient.getStatus())) {
            // 停用患者不能创建挂号订单。
            throw new BusinessException("患者档案已停用");
        }

        // 调用 ScheduleService.getDetail，根据 DTO 中的 scheduleId 查询排班详情。
        ScheduleVO schedule =
                scheduleService.getDetail(
                        request.getScheduleId()
                );

        // ScheduleService 已保证排班存在；这里检查排班是否停诊。
        if (Integer.valueOf(0).equals(schedule.getStatus())) {
            // 停诊排班不能继续挂号。
            throw new BusinessException("该排班已经停诊");
        }

        // 比较排班日期和今天，判断排班是否已经过去。
        if (schedule.getScheduleDate()
                .isBefore(LocalDate.now())) {

            // 过去的排班不能创建新订单。
            throw new BusinessException("该排班已经过期");
        }

        // 查询 registration_order 表，判断同一患者是否已经挂过同一排班。
        boolean duplicateRegistration = lambdaQuery()
                .eq(
                        RegistrationOrder::getPatientId,
                        request.getPatientId()
                )
                .eq(
                        RegistrationOrder::getScheduleId,
                        request.getScheduleId()
                )
                .in(
                        RegistrationOrder::getStatus,
                        RegistrationOrderStatus.PENDING_PAYMENT.name(),
                        RegistrationOrderStatus.PAID.name(),
                        RegistrationOrderStatus.COMPLETED.name()
                )
                .exists();

        // true 表示已经存在相同患者和排班的订单。
        if (duplicateRegistration) {
            // 阻止重复挂号，避免同一个患者重复占用同一排班号源。
            throw new BusinessException(
                    "该患者已经为这个排班创建过挂号订单"
            );
        }

        // 这是并发安全的原子扣减
        boolean slotSuccess =
                // 调用 ScheduleService，最终执行 ScheduleMapper 中“剩余号源大于0才减1”的 SQL。
                scheduleService.decreaseSlot(
                        request.getScheduleId()
                );

        // false 表示数据库没有成功扣减任何一行。
        if (!slotSuccess) {
            // 可能是剩余号源为 0，也可能是排班已经停诊。
            throw new BusinessException(
                    "号源已抢完或排班已停诊"
            );
        }

        // 创建挂号订单实体，对应 registration_order 表中准备新增的一行。
        RegistrationOrder order =
                new RegistrationOrder();

        // 调用本类 generateOrderNo 生成挂号订单号。
        order.setOrderNo(generateOrderNo());
        // 保存前端请求唯一标识，后续重复提交时会用它找到原订单。
        order.setRequestId(requestId);
        // 写入患者 id，建立挂号订单与患者的关联。
        order.setPatientId(request.getPatientId());
        // 写入排班 id，建立订单与排班的关联。
        order.setScheduleId(request.getScheduleId());

        // 金额从排班数据获取，不相信前端金额
        // 这样前端即使偷偷把价格改成 0，也不会影响数据库中的真实金额。
        order.setAmount(schedule.getRegistrationFee());

        // 设置挂号订单初始状态。
        order.setStatus(
                // 从 RegistrationOrderStatus 枚举中取出 PENDING_PAYMENT。
                RegistrationOrderStatus
                        .PENDING_PAYMENT
                        // name 把枚举转换成数据库 status 字段需要的字符串。
                        .name()
        );

        // 设置支付截止时间。
        order.setPaymentDeadline(
                // 取得服务器当前时间，再往后加 15 分钟。
                LocalDateTime.now().plusMinutes(15)
        );

        /*
         * 如果这里保存失败，
         * @Transactional 会让上面的扣号源一起回滚。
         */
        // save 通过 RegistrationOrderMapper 把 order 插入 registration_order 表。
        save(order);

        // 插入后数据库 id 会回填到实体，这里把挂号订单 id 返回给 Controller。
        return order.getId();
    }

    // 对应 RegistrationOrderService 中的 listDetails，查询挂号订单列表。
    @Override
    public List<RegistrationOrderVO> listDetails() {
        // 调用 RegistrationOrderMapper 的多表联查，同时取得患者、排班、医生和科室信息。
        return baseMapper.selectOrderList();
    }

    // 对应 RegistrationOrderService 中的 getDetail，查询一条挂号订单详情。
    @Override
    public RegistrationOrderVO getDetail(Long id) {
        // 通过 RegistrationOrderMapper 多表联查，结果放进 RegistrationOrderVO。
        RegistrationOrderVO order =
                baseMapper.selectOrderDetail(id);

        // Mapper 返回 null 表示这个订单 id 不存在。
        if (order == null) {
            // 停止业务并返回错误提示。
            throw new BusinessException("挂号订单不存在");
        }

        // 查询成功，把 VO 返回给 Controller。
        return order;
    }

    // 对应 RegistrationOrderService 中的 cancel，用来取消待支付挂号订单。
    @Override
    // 开启事务：订单状态修改和号源归还必须一起成功或一起失败。
    @Transactional(rollbackFor = Exception.class)
    public void cancel(
            Long id,
            RegistrationCancelRequest request) {

        // 根据 Controller 传来的 id 查询 registration_order 表。
        RegistrationOrder order = getById(id);

        // order 为 null 表示数据库没有这条订单。
        if (order == null) {
            // 不允许继续取消不存在的订单。
            throw new BusinessException("挂号订单不存在");
        }

        // 从枚举取得 CANCELLED，并转成数据库使用的字符串。
        String cancelledStatus =
                RegistrationOrderStatus
                        .CANCELLED
                        .name();

        // 重复取消直接当作成功，保证取消接口幂等
        // equals 左边使用非空字符串，可以避免 order.getStatus 为 null 时出现空指针。
        if (cancelledStatus.equals(order.getStatus())) {
            // 已取消就直接结束，不会再次归还号源。
            return;
        }

        // 从枚举取得 PENDING_PAYMENT，并转成字符串。
        String pendingStatus =
                RegistrationOrderStatus
                        .PENDING_PAYMENT
                        .name();

        // 当前状态不是待支付，就不符合取消规则。
        if (!pendingStatus.equals(order.getStatus())) {
            // 例如已支付订单不能使用这个普通取消接口。
            throw new BusinessException(
                    "只有待支付订单可以取消"
            );
        }

        /*
         * WHERE中同时判断当前状态。
         * 两个请求同时取消时，只有一个能够修改成功。
         */
        // lambdaUpdate 最终会通过 RegistrationOrderMapper 执行一条带状态条件的 UPDATE。
        boolean cancelled = lambdaUpdate()
                // 只修改当前 id 对应的挂号订单。
                .eq(RegistrationOrder::getId, id)
                // 并且数据库中的当前状态必须还是待支付。
                .eq(
                        RegistrationOrder::getStatus,
                        pendingStatus
                )
                // 把数据库 status 字段改成 CANCELLED。
                .set(
                        RegistrationOrder::getStatus,
                        cancelledStatus
                )
                // 把数据库 cancel_time 设置为当前服务器时间。
                .set(
                        RegistrationOrder::getCancelTime,
                        LocalDateTime.now()
                )
                // 把取消 DTO 中的原因去掉首尾空格后写入 cancel_reason。
                .set(
                        RegistrationOrder::getCancelReason,
                        request.getCancelReason().trim()
                )
                // 真正执行 UPDATE；修改成功返回 true，没改到数据返回 false。
                .update();

        // false 常见于并发情况下：查询以后，订单状态被另一个请求抢先修改了。
        if (!cancelled) {
            // 重新从数据库读取一次最新订单状态。
            RegistrationOrder latestOrder = getById(id);

            // 如果最新记录存在，并且已经是 CANCELLED，说明另一个取消请求已经成功。
            if (latestOrder != null
                    && cancelledStatus.equals(
                    latestOrder.getStatus())) {
                // 把重复取消也当作成功，直接结束。
                return;
            }

            // 如果变成了其他状态，提示前端刷新，避免错误归还号源。
            throw new BusinessException(
                    "订单状态已经发生变化，请刷新后重试"
            );
        }

        // 只有真正取消成功的请求才能归还一次号源
        // 调用 ScheduleService.restoreSlot，最终执行 ScheduleMapper 的原子加库存 SQL。
        scheduleService.restoreSlot(
                order.getScheduleId()
        );
    }

    // 本类内部的挂号订单号生成方法，不在 Service 接口中，所以没有 @Override。
    private String generateOrderNo() {
        // 获取当前时间并格式化，作为订单号的时间部分。
        String timeText = LocalDateTime.now()
                .format(
                        // 格式包含年月日时分秒和毫秒。
                        DateTimeFormatter.ofPattern(
                                "yyyyMMddHHmmssSSS"
                        )
                );

        // 再生成一段随机文字，降低同一毫秒生成重复订单号的可能。
        String randomText = UUID.randomUUID()
                // 把 UUID 对象转成字符串。
                .toString()
                // 去掉 UUID 中的短横线。
                .replace("-", "")
                // 截取前 6 个字符。
                .substring(0, 6)
                // 统一转成大写。
                .toUpperCase();

        // REG + 时间 + 随机文字，组成最终挂号订单号。
        return "REG" + timeText + randomText;
    }


    // 对应 RegistrationOrderService 中的 markPaid，由支付模块支付成功时调用。
    @Override
    public boolean markPaid(Long registrationOrderId, LocalDateTime paidTime) {
        // 使用一条带状态条件的 UPDATE，保证只有待支付订单能够变成已支付。
        return lambdaUpdate()
                // 只修改支付模块传进来的挂号订单 id。
                .eq(RegistrationOrder::getId, registrationOrderId)
                // 数据库当前状态必须还是 PENDING_PAYMENT。
                .eq(
                        RegistrationOrder::getStatus,
                        RegistrationOrderStatus.PENDING_PAYMENT.name()
                )
                // 把挂号订单状态改成 PAID。
                .set(
                        RegistrationOrder::getStatus,
                        RegistrationOrderStatus.PAID.name()
                )
                // 把支付模块传来的成功时间写入 paid_time。
                .set(RegistrationOrder::getPaidTime, paidTime)
                // 执行 UPDATE；成功修改一条返回 true，状态不符合则返回 false。
                .update();
    }

    @Override
    public List<Long> findExpiredPendingIds(int limit) {
        return baseMapper.selectExpiredPendingIds(limit);
    }

    @Override
    public boolean markExpired(Long id) {
        return baseMapper.markExpired(id) == 1;
    }

    @Override
    public boolean markCompleted(Long registrationOrderId) {

        // 只有PAID状态的挂号订单，才能修改成COMPLETED
        return lambdaUpdate()
                .eq(
                        RegistrationOrder::getId,
                        registrationOrderId
                )
                .eq(
                        RegistrationOrder::getStatus,
                        RegistrationOrderStatus.PAID.name()
                )
                .set(
                        RegistrationOrder::getStatus,
                        RegistrationOrderStatus.COMPLETED.name()
                )
                .update();
    }

    @Override
    public List<RegistrationOrderVO>
    listPendingVisitsByDoctorId(Long doctorId) {

        // 调用RegistrationOrderMapper，
        // 查询当前医生已支付但还没有病历的挂号订单
        return baseMapper
                .selectPendingVisitListByDoctorId(
                        doctorId
                );
    }


}
