package com.example.visitrecord;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.registration.RegistrationOrderStatus;
import com.example.exception.BusinessException;
import com.example.registration.RegistrationOrderService;
import com.example.registration.RegistrationOrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class VisitRecordServiceImpl
        extends ServiceImpl<
        VisitRecordMapper,
        VisitRecord
        >
        implements VisitRecordService {

    // 挂号Service用于查询挂号订单，并在接诊完成后修改订单状态
    private final RegistrationOrderService
            registrationOrderService;

    // Spring把RegistrationOrderService的实现对象传进来
    public VisitRecordServiceImpl(
            RegistrationOrderService registrationOrderService) {

        // 保存到当前Impl的成员变量中，后面的方法可以使用
        this.registrationOrderService =
                registrationOrderService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(
            Long currentDoctorId,
            VisitRecordCreateRequest request) {

        // doctorId来自当前登录用户，null表示账号没有绑定医生档案
        if (currentDoctorId == null) {
            throw new BusinessException(
                    "当前账号未绑定医生档案"
            );
        }

        // 根据DTO中的挂号订单ID，查询完整挂号订单VO
        RegistrationOrderVO registrationOrder =
                registrationOrderService.getDetail(
                        request.getRegistrationOrderId()
                );

        // 只有已经支付的挂号订单才能进行接诊
        if (!RegistrationOrderStatus.PAID
                .name()
                .equals(registrationOrder.getStatus())) {

            throw new BusinessException(
                    "只有已支付的挂号订单才能接诊"
            );
        }

        // 当前日期还没有到排班日期时，不能提前填写病历
        if (registrationOrder.getScheduleDate()
                .isAfter(LocalDate.now())) {

            throw new BusinessException(
                    "还未到预约就诊日期"
            );
        }

        // 判断挂号订单中的医生是不是当前登录医生
        if (!Objects.equals(
                currentDoctorId,
                registrationOrder.getDoctorId())) {

            throw new BusinessException(
                    "不能处理其他医生的挂号订单"
            );
        }

        // 查询这张挂号订单是否已经创建过就诊记录
        boolean recordExists = lambdaQuery()
                .eq(
                        VisitRecord::getRegistrationOrderId,
                        request.getRegistrationOrderId()
                )
                .exists();

        // true表示已经有病历，不能再次创建
        if (recordExists) {
            throw new BusinessException(
                    "该挂号订单已经创建过就诊记录"
            );
        }

        // 创建VisitRecord实体，对应visit_record表中新的一行
        VisitRecord visitRecord = new VisitRecord();

        // 后端自动生成就诊记录编号
        visitRecord.setRecordNo(
                generateRecordNo()
        );

        // 挂号订单ID来自经过验证的DTO
        visitRecord.setRegistrationOrderId(
                registrationOrder.getId()
        );

        // 患者ID从数据库查询出的挂号订单中取得，不相信前端
        visitRecord.setPatientId(
                registrationOrder.getPatientId()
        );

        // 医生ID使用当前登录医生ID，不相信前端
        visitRecord.setDoctorId(
                currentDoctorId
        );

        // 必填内容去掉首尾空格后写入实体
        visitRecord.setChiefComplaint(
                request.getChiefComplaint().trim()
        );

        // 可选内容使用trimToNull处理
        visitRecord.setPresentIllness(
                trimToNull(request.getPresentIllness())
        );

        // 诊断结果是必填内容
        visitRecord.setDiagnosis(
                request.getDiagnosis().trim()
        );

        // 保存治疗方案
        visitRecord.setTreatmentPlan(
                trimToNull(request.getTreatmentPlan())
        );

        // 保存医生嘱咐
        visitRecord.setDoctorAdvice(
                trimToNull(request.getDoctorAdvice())
        );

        // 接诊时间由后端服务器生成
        visitRecord.setVisitTime(
                LocalDateTime.now()
        );

        // save通过VisitRecordMapper插入visit_record表
        save(visitRecord);

        // 病历保存后，把挂号订单从PAID原子修改成COMPLETED
        boolean completed =
                registrationOrderService.markCompleted(
                        registrationOrder.getId()
                );

        // 修改失败说明订单状态被其他请求改变了
        if (!completed) {
            throw new BusinessException(
                    "挂号订单状态已变化，请刷新后重试"
            );
        }

        // 返回数据库生成的就诊记录ID
        return visitRecord.getId();
    }

    @Override
    public List<VisitRecordVO> listByDoctorId(
            Long currentDoctorId) {

        // 没有绑定医生档案的账号不能查询医生病历
        if (currentDoctorId == null) {
            throw new BusinessException(
                    "当前账号未绑定医生档案"
            );
        }

        // 调用VisitRecordMapper，查询当前医生的历史接诊记录
        return baseMapper.selectRecordList(
                currentDoctorId
        );
    }

    @Override
    public VisitRecordVO getDetail(
            Long id,
            Long currentDoctorId) {

        // 没有医生ID就不能查询病历
        if (currentDoctorId == null) {
            throw new BusinessException(
                    "当前账号未绑定医生档案"
            );
        }

        // Mapper同时根据病历ID和当前医生ID查询
        VisitRecordVO visitRecord =
                baseMapper.selectRecordDetail(
                        id,
                        currentDoctorId
                );

        // null可能表示病历不存在，也可能表示不属于当前医生
        if (visitRecord == null) {
            throw new BusinessException(
                    "就诊记录不存在或无权查看"
            );
        }

        // 返回多表联查得到的VO
        return visitRecord;
    }

    // 处理可以不填写的字符串
    private String trimToNull(String value) {

        // 前端没有传这个字段时直接返回null
        if (value == null) {
            return null;
        }

        // 去掉字符串首尾空格
        String text = value.trim();

        // 如果去掉空格后为空，就存null，否则存整理后的文字
        return text.isEmpty() ? null : text;
    }

    // 生成就诊记录编号
    private String generateRecordNo() {

        // 生成当前时间文字
        String timeText = LocalDateTime.now()
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMddHHmmssSSS"
                        )
                );

        // 生成6位随机文字
        String randomText = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();

        // 例如 VIS20260804153000123ABC123
        return "VIS" + timeText + randomText;
    }

    @Override
    public List<RegistrationOrderVO> listPendingVisits(
            Long currentDoctorId) {

        // doctorId为空，说明登录账号没有绑定医生档案
        if (currentDoctorId == null) {
            throw new BusinessException(
                    "当前账号未绑定医生档案"
            );
        }

        // 调用挂号Service查询当前医生的待接诊患者
        return registrationOrderService
                .listPendingVisitsByDoctorId(currentDoctorId);
    }

    @Override
    public List<VisitRecordVO> listAll() {

        // 管理员传null，表示不按照医生ID限制
        return baseMapper.selectRecordList(null);
    }

    @Override
    public VisitRecordVO getAdminDetail(Long id) {

        // 第二个参数传null，表示管理员可以查询任意医生的病历
        VisitRecordVO visitRecord =
                baseMapper.selectRecordDetail(
                        id,
                        null
                );

        // 没查到表示病历ID不存在
        if (visitRecord == null) {
            throw new BusinessException(
                    "就诊记录不存在"
            );
        }

        // 把多表联查结果返回给管理员Controller
        return visitRecord;
    }
}