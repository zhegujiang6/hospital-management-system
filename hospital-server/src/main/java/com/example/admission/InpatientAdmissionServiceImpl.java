package com.example.admission;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.patient.Patient;
import com.example.bed.BedStatus;
import com.example.exception.BusinessException;
import com.example.bed.BedService;
import com.example.patient.PatientService;
import com.example.bed.BedVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class InpatientAdmissionServiceImpl
        extends ServiceImpl<
        InpatientAdmissionMapper,
        InpatientAdmission
        >
        implements InpatientAdmissionService {

    private final PatientService patientService;

    private final BedService bedService;

    public InpatientAdmissionServiceImpl(
            PatientService patientService,
            BedService bedService) {

        this.patientService = patientService;
        this.bedService = bedService;
    }

    /**
     * 校验患者和床位，原子占用床位，并创建住院记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long admit(
            InpatientAdmissionCreateRequest request) {

        Patient patient =
                patientService.getById(
                        request.getPatientId()
                );

        if (patient == null) {
            throw new BusinessException(
                    "患者不存在"
            );
        }

        if (!Integer.valueOf(1).equals(
                patient.getStatus())) {

            throw new BusinessException(
                    "患者档案已停用"
            );
        }

        BedVO bed =
                bedService.getDetail(
                        request.getBedId()
                );

        if (!BedStatus.AVAILABLE
                .name()
                .equals(bed.getStatus())) {

            throw new BusinessException(
                    "床位当前不可用"
            );
        }

        boolean alreadyAdmitted =
                lambdaQuery()
                        .eq(
                                InpatientAdmission::getPatientId,
                                request.getPatientId()
                        )
                        .eq(
                                InpatientAdmission::getStatus,
                                InpatientAdmissionStatus.ACTIVE.name()
                        )
                        .exists();

        if (alreadyAdmitted) {
            throw new BusinessException(
                    "患者当前已经住院"
            );
        }

        /*
         * 这里再次检查床位状态，并执行原子UPDATE。
         * 即使前面查询时床位空闲，也可能刚刚被另一个请求抢占。
         */
        boolean occupied =
                bedService.occupy(
                        request.getBedId()
                );

        if (!occupied) {
            throw new BusinessException(
                    "床位已被其他患者占用"
            );
        }

        InpatientAdmission admission =
                new InpatientAdmission();

        admission.setAdmissionNo(
                generateAdmissionNo()
        );

        admission.setPatientId(
                request.getPatientId()
        );

        admission.setBedId(
                request.getBedId()
        );

        admission.setStatus(
                InpatientAdmissionStatus.ACTIVE.name()
        );

        admission.setAdmittedAt(
                LocalDateTime.now()
        );

        String dietaryNotes =
                request.getDietaryNotes();

        admission.setDietaryNotes(
                dietaryNotes == null
                        || dietaryNotes.isBlank()
                        ? null
                        : dietaryNotes.trim()
        );

        try {
            save(admission);
        } catch (DuplicateKeyException exception) {

            /*
             * 可能是同一患者并发办理两次入院。
             * 抛出异常后，事务会把前面的床位占用一起回滚。
             */
            throw new BusinessException(
                    "患者已住院或床位已被占用"
            );
        }

        return admission.getId();
    }

    /**
     * 生成全局业务住院号。
     */
    private String generateAdmissionNo() {

        String timeText =
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMddHHmmssSSS"
                        )
                );

        String randomText =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 6)
                        .toUpperCase();

        return "ADM"
                + timeText
                + randomText;
    }

    /**
     * 查询包含患者、床位、病区和科室信息的住院列表。
     */
    @Override
    public List<InpatientAdmissionVO> listDetails() {

        return baseMapper.selectInpatientAdmissionList();
    }

    /**
     * 查询一条完整住院记录，不存在时返回业务错误。
     */
    @Override
    public InpatientAdmissionVO getDetail(Long id) {

        InpatientAdmissionVO admission =
                baseMapper.selectInpatientAdmissionDetail(id);

        if (admission == null) {
            throw new BusinessException(
                    "住院记录不存在"
            );
        }

        return admission;
    }

    /**
     * 修改住院状态并释放对应床位。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void discharge(Long id) {

        InpatientAdmission inpatientAdmission =
                getById(id);

        if (inpatientAdmission == null) {
            throw new BusinessException(
                    "住院记录不存在"
            );
        }

        String dischargedStatus =
                InpatientAdmissionStatus
                        .DISCHARGED
                        .name();

        /*
         * 已经出院时直接返回。
         * 这样相同请求重复提交不会重复释放床位。
         */
        if (dischargedStatus.equals(
                inpatientAdmission.getStatus())) {

            return;
        }

        String activeStatus =
                InpatientAdmissionStatus
                        .ACTIVE
                        .name();

        if (!activeStatus.equals(
                inpatientAdmission.getStatus())) {

            throw new BusinessException(
                    "当前住院状态不能办理出院"
            );
        }

        /*
         * WHERE中同时判断ACTIVE。
         * 防止两个请求同时为同一患者办理出院。
         */
        boolean discharged =
                lambdaUpdate()
                        .eq(
                                InpatientAdmission::getId,
                                id
                        )
                        .eq(
                                InpatientAdmission::getStatus,
                                activeStatus
                        )
                        .set(
                                InpatientAdmission::getStatus,
                                dischargedStatus
                        )
                        .set(
                                InpatientAdmission::getDischargedAt,
                                LocalDateTime.now()
                        )
                        .update();

        if (!discharged) {
            throw new BusinessException(
                    "住院状态已发生变化，请刷新后重试"
            );
        }

        /*
         * 只有住院状态修改成功后才释放床位。
         * 如果释放失败，抛出异常让整个事务回滚。
         */
        boolean released =
                bedService.release(
                        inpatientAdmission.getBedId()
                );

        if (!released) {
            throw new BusinessException(
                    "床位状态异常，无法办理出院"
            );
        }
    }

    /**
     * 根据患者ID查询当前生效的住院记录。
     */
    @Override
    public InpatientAdmissionVO getActiveByPatientId(
            Long patientId) {

        /*
         * 第一次查询只查 inpatient_admission 表，
         * 找到患者当前状态为 ACTIVE 的住院记录。
         */
        InpatientAdmission activeAdmission =
                lambdaQuery()
                        .eq(
                                InpatientAdmission::getPatientId,
                                patientId
                        )
                        .eq(
                                InpatientAdmission::getStatus,
                                InpatientAdmissionStatus
                                        .ACTIVE
                                        .name()
                        )
                        .one();

        /*
         * 没找到ACTIVE记录，说明患者目前没有住院。
         * 返回null，让调用方根据配送类型决定如何处理。
         */
        if (activeAdmission == null) {
            return null;
        }

        /*
         * 复用已有的详情查询。
         * 它会联查患者、床位、病区和科室，
         * 避免在这里重复写一份复杂SQL。
         */
        return baseMapper
                .selectInpatientAdmissionDetail(
                        activeAdmission.getId()
                );
    }
}
