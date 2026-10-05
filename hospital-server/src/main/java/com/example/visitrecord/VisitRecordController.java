package com.example.visitrecord;

import com.example.common.Result;
import com.example.security.LoginUser;
import com.example.registration.RegistrationOrderVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/visit-records")
@PreAuthorize("hasRole('DOCTOR')")
public class VisitRecordController {

    private final VisitRecordService
            visitRecordService;

    public VisitRecordController(
            VisitRecordService visitRecordService) {

        this.visitRecordService =
                visitRecordService;
    }

    @GetMapping("/pending")
    public Result<List<RegistrationOrderVO>>
    listPendingVisits(
            @AuthenticationPrincipal
            LoginUser loginUser) {

        return Result.success(
                visitRecordService.listPendingVisits(
                        loginUser.getDoctorId()
                )
        );
    }

    @PostMapping
    public Result<Long> create(
            @AuthenticationPrincipal
            LoginUser loginUser,

            @Valid
            @RequestBody
            VisitRecordCreateRequest request) {

        Long visitRecordId =
                visitRecordService.create(
                        loginUser.getDoctorId(),
                        request
                );

        return Result.success(visitRecordId);
    }

    @GetMapping
    public Result<List<VisitRecordVO>> list(
            @AuthenticationPrincipal
            LoginUser loginUser) {

        return Result.success(
                visitRecordService.listByDoctorId(
                        loginUser.getDoctorId()
                )
        );
    }

    @GetMapping("/{id}")
    public Result<VisitRecordVO> getDetail(
            @PathVariable Long id,

            @AuthenticationPrincipal
            LoginUser loginUser) {

        return Result.success(
                visitRecordService.getDetail(
                        id,
                        loginUser.getDoctorId()
                )
        );
    }
}
