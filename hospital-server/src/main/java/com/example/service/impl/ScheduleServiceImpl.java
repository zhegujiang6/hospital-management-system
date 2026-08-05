package com.example.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.dto.ScheduleCreateRequest;
import com.example.dto.ScheduleUpdateRequest;
import com.example.entity.DoctorSchedule;
import com.example.exception.BusinessException;
import com.example.mapper.ScheduleMapper;
import com.example.service.DoctorService;
import com.example.service.ScheduleService;
import com.example.vo.DoctorVO;
import com.example.vo.ScheduleVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
/*
 * 医生排班业务实现类。
 * ScheduleController 调用 ScheduleService，最终会执行这里的方法。
 * ServiceImpl<ScheduleMapper, DoctorSchedule> 负责连接 ScheduleMapper 和 doctor_schedule 表。
 */
public class ScheduleServiceImpl
        extends ServiceImpl<ScheduleMapper, DoctorSchedule>
        implements ScheduleService {

    // 保存医生业务接口，因为排班之前必须确认医生存在并且处于启用状态。
    private final DoctorService doctorService;

    // Spring 创建本类时，把 DoctorService 的实现对象传进来。
    public ScheduleServiceImpl(DoctorService doctorService) {
        // 保存医生服务，后面的 validateDoctor 方法会使用它。
        this.doctorService = doctorService;
    }

    // 对应 ScheduleService 中的 create，用来创建一条医生排班。
    @Override
    public Long create(ScheduleCreateRequest request) {
        // 从 DTO 取出 doctorId，调用本类辅助方法检查医生能不能排班。
        validateDoctor(request.getDoctorId());
        // 从 DTO 取出排班日期，检查日期不能早于今天。
        validateScheduleDate(request.getScheduleDate());

        // 把 DTO 中的上午/下午时段去空格并转成大写，得到统一格式。
        String period = normalizePeriod(request.getPeriod());

        // 查询 doctor_schedule 表，判断同一医生、同一天、同一时段是否已经排过班。
        boolean scheduleExists = lambdaQuery()
                // 第一个查询条件：医生 id 相同。
                .eq(DoctorSchedule::getDoctorId, request.getDoctorId())
                // 第二个查询条件：排班日期相同。
                .eq(DoctorSchedule::getScheduleDate, request.getScheduleDate())
                // 第三个查询条件：上午或下午时段相同。
                .eq(DoctorSchedule::getPeriod, period)
                // 不查询完整数据，只判断记录是否存在。
                .exists();

        // true 表示这个时间已经有排班，不能重复创建。
        if (scheduleExists) {
            // 抛出业务异常，把原因返回给前端。
            throw new BusinessException("该医生在这个日期和时段已经排班");
        }

        // 创建 DoctorSchedule 实体，对应 doctor_schedule 表中准备新增的一行。
        DoctorSchedule schedule = new DoctorSchedule();

        // 把 DTO 中的医生 id 写进排班实体，建立排班与医生的关联。
        schedule.setDoctorId(request.getDoctorId());
        // 写入排班日期。
        schedule.setScheduleDate(request.getScheduleDate());
        // 写入已经统一格式的 MORNING 或 AFTERNOON。
        schedule.setPeriod(period);
        // 写入挂号费用。
        schedule.setRegistrationFee(request.getRegistrationFee());
        // 写入总号源数量。
        schedule.setTotalSlots(request.getTotalSlots());

        // 新建排班时还没有人挂号，所以剩余号源等于总号源
        schedule.setRemainingSlots(request.getTotalSlots());
        // 新建排班默认启用，1 表示正常出诊。
        schedule.setStatus(1);

        // save 内部通过 ScheduleMapper，把排班插入 doctor_schedule 表。
        save(schedule);

        // 数据库生成的排班 id 会回填到实体，这里返回给 Controller。
        return schedule.getId();
    }

    // 对应 ScheduleService 中的 listDetails，查询带医生和科室信息的排班列表。
    @Override
    public List<ScheduleVO> listDetails() {
        // baseMapper 就是 ScheduleMapper；这里调用 Mapper 中的多表联查 SQL。
        return baseMapper.selectScheduleList();
    }

    // 对应 ScheduleService 中的 getDetail，查询一条完整排班详情。
    @Override
    public ScheduleVO getDetail(Long id) {
        // 通过 ScheduleMapper 根据排班 id 联查排班、医生和科室，结果使用 ScheduleVO 接收。
        ScheduleVO scheduleVO =
                baseMapper.selectScheduleDetail(id);

        // Mapper 返回 null，表示数据库中没有这个排班。
        if (scheduleVO == null) {
            // 停止业务并把错误提示返回给调用方。
            throw new BusinessException("排班不存在");
        }

        // 查询成功，把 VO 返回给 Controller 或挂号业务。
        return scheduleVO;
    }

    // 对应 ScheduleService 中的 update；id 决定修改哪条排班，request 携带新内容。
    @Override
    public void update(
            Long id,
            ScheduleUpdateRequest request) {

        // 先根据 id 查询原来的 doctor_schedule 数据，结果使用实体接收。
        DoctorSchedule schedule = getById(id);

        // schedule 为 null 表示数据库没有这条排班。
        if (schedule == null) {
            // 不允许修改不存在的数据。
            throw new BusinessException("排班不存在");
        }

        // 调用医生模块，验证修改后的医生仍然存在且启用。
        validateDoctor(request.getDoctorId());
        // 验证修改后的日期不能早于今天。
        validateScheduleDate(request.getScheduleDate());

        // 整理 DTO 中的排班时段。
        String period = normalizePeriod(request.getPeriod());

        // 查询修改后是否会和其他排班发生时间冲突。
        boolean scheduleExists = lambdaQuery()
                // 条件一：医生相同。
                .eq(DoctorSchedule::getDoctorId, request.getDoctorId())
                // 条件二：日期相同。
                .eq(DoctorSchedule::getScheduleDate, request.getScheduleDate())
                // 条件三：时段相同。
                .eq(DoctorSchedule::getPeriod, period)
                // 排除当前正在修改的排班，否则它会和自己重复。
                .ne(DoctorSchedule::getId, id)
                // 返回是否存在冲突记录。
                .exists();

        // true 表示已经存在另一条相同时间的排班。
        if (scheduleExists) {
            // 阻止修改，避免一个医生同时出现两条相同排班。
            throw new BusinessException("该医生在这个日期和时段已经排班");
        }

        // 已经被挂掉的号源数量
        int registeredSlots =
                // 例如总号源 20 减去剩余号源 15，得到已经挂出的 5 个号。
                schedule.getTotalSlots()
                        - schedule.getRemainingSlots();

        // 总号源不能改得比已经挂出的数量还少
        if (request.getTotalSlots() < registeredSlots) {
            // 例如已经挂出 5 个号，就不能把总号源改成 4。
            throw new BusinessException(
                    "总号源数不能小于已挂号人数"
            );
        }

        // 把修改 DTO 中的医生 id 写入原排班实体。
        schedule.setDoctorId(request.getDoctorId());
        // 更新排班日期。
        schedule.setScheduleDate(request.getScheduleDate());
        // 更新上午或下午时段。
        schedule.setPeriod(period);
        // 更新挂号费。
        schedule.setRegistrationFee(request.getRegistrationFee());
        // 更新总号源。
        schedule.setTotalSlots(request.getTotalSlots());

        // 修改总号源后，重新计算剩余号源
        schedule.setRemainingSlots(
                // 新的剩余号源 = 新总号源 - 已经挂出去的号源。
                request.getTotalSlots() - registeredSlots
        );

        // 更新排班启用或停诊状态。
        schedule.setStatus(request.getStatus());

        // updateById 内部调用 ScheduleMapper，真正更新 doctor_schedule 表。
        updateById(schedule);
    }

    // 对应 ScheduleService 中的 delete。
    @Override
    public void delete(Long id) {
        // 根据 id 查询准备删除的排班。
        DoctorSchedule schedule = getById(id);

        // 没查到排班就不能删除。
        if (schedule == null) {
            // 把排班不存在的原因返回给前端。
            throw new BusinessException("排班不存在");
        }

        // 用总号源减剩余号源，计算已经有多少患者挂号。
        int registeredSlots =
                schedule.getTotalSlots()
                        - schedule.getRemainingSlots();

        // 大于 0 表示这条排班已经产生挂号业务。
        if (registeredSlots > 0) {
            // 为了保留挂号数据关系，不允许直接删除，只能停诊。
            throw new BusinessException(
                    "该排班已有挂号记录，不能删除，可以设置为停诊"
            );
        }

        // 没有患者挂号时，才通过 ScheduleMapper 删除 doctor_schedule 记录。
        removeById(id);
    }

    // 本类内部的医生校验方法；create 和 update 都会复用它。
    private void validateDoctor(Long doctorId) {
        // 调用 DoctorService.getDetail 跨模块查询医生详情，返回 DoctorVO。
        DoctorVO doctor = doctorService.getDetail(doctorId);

        // DoctorService 已保证医生存在；这里判断医生状态是否为 0。
        if (Integer.valueOf(0).equals(doctor.getStatus())) {
            // 停用医生不能创建或修改排班。
            throw new BusinessException("停用的医生不能进行排班");
        }
    }

    // 本类内部的排班日期校验方法。
    private void validateScheduleDate(LocalDate scheduleDate) {
        // isBefore 用来判断传入日期是否早于服务器今天的日期。
        if (scheduleDate.isBefore(LocalDate.now())) {
            // 过去的日期不能再创建排班。
            throw new BusinessException("排班日期不能早于今天");
        }
    }

    // 本类内部的时段格式整理方法。
    private String normalizePeriod(String period) {
        // 去掉首尾空格并转大写，例如 morning 会变成 MORNING。
        return period.trim().toUpperCase();
    }

    // 对应 ScheduleService 中的 decreaseSlot，挂号模块创建订单时会调用它扣减号源。
    @Override
    public boolean decreaseSlot(Long scheduleId) {
        // 调用 ScheduleMapper 的原子 UPDATE；影响 1 行表示扣减成功，否则表示没号或已停诊。
        return baseMapper.decreaseRemainingSlot(scheduleId) == 1;
    }

    // 对应 ScheduleService 中的 restoreSlot，取消挂号时会调用它归还号源。
    @Override
    public void restoreSlot(Long scheduleId) {
        // 调用 ScheduleMapper 的原子 UPDATE，并取得数据库实际修改的行数。
        int affectedRows =
                baseMapper.restoreRemainingSlot(scheduleId);

        // 正常情况下必须正好修改 1 行。
        if (affectedRows != 1) {
            // 0 行或多行都说明号源没有按预期归还，抛异常让外层事务回滚。
            throw new BusinessException("归还号源失败");
        }
    }
    @Override
    public List<ScheduleVO> listByDoctorId(
            Long currentDoctorId) {

        // doctorId为空，说明当前账号没有绑定医生档案
        if (currentDoctorId == null) {
            throw new BusinessException(
                    "当前账号未绑定医生档案"
            );
        }

        // 调用ScheduleMapper，只查询当前医生自己的排班
        return baseMapper
                .selectScheduleListByDoctorId(
                        currentDoctorId
                );
    }
}
