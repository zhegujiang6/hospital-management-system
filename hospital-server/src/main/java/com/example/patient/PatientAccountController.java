package com.example.patient;

import com.example.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patient-accounts")
public class PatientAccountController {

    private final PatientAccountService
            patientAccountService;

    public PatientAccountController(
            PatientAccountService patientAccountService) {

        this.patientAccountService =
                patientAccountService;
    }

    /**
     * 为指定患者创建登录账号。
     */
    @PostMapping
    public Result<Long> create(
            @Valid
            @RequestBody
            PatientAccountCreateRequest request) {

        return Result.success(
                patientAccountService.create(request)
        );
    }
}