package com.example.admission;

import com.example.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inpatient-admissions")
public class InpatientAdmissionController {

    private final InpatientAdmissionService
            inpatientAdmissionService;

    public InpatientAdmissionController(
            InpatientAdmissionService inpatientAdmissionService) {

        this.inpatientAdmissionService = inpatientAdmissionService;
    }

    /**
     * 接收管理员提交的患者入院请求。
     */
    @PostMapping
    public Result<Long> admit(
            @Valid
            @RequestBody
            InpatientAdmissionCreateRequest request) {

        return Result.success(
                inpatientAdmissionService.admit(request)
        );
    }
    /**
     * 查询全部住院记录。
     */
    @GetMapping
    public Result<List<InpatientAdmissionVO>> list() {

        return Result.success(
                inpatientAdmissionService.listDetails()
        );
    }

    /**
     * 查询指定住院记录详情。
     */
    @GetMapping("/{id}")
    public Result<InpatientAdmissionVO> getDetail(
            @PathVariable Long id) {

        return Result.success(
                inpatientAdmissionService.getDetail(id)
        );
    }
    /**
     * 为指定住院记录办理出院。
     */
    @PutMapping("/{id}/discharge")
    public Result<Void> discharge(
            @PathVariable Long id) {

        inpatientAdmissionService.discharge(id);

        return Result.success(null);
    }
}
