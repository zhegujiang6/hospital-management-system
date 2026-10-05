package com.example.visitrecord;

import com.example.common.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/visit-records")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVisitRecordController {

    private final VisitRecordService
            visitRecordService;

    public AdminVisitRecordController(
            VisitRecordService visitRecordService) {

        this.visitRecordService =
                visitRecordService;
    }

    @GetMapping
    public Result<List<VisitRecordVO>> list() {

        return Result.success(
                visitRecordService.listAll()
        );
    }

    @GetMapping("/{id}")
    public Result<VisitRecordVO> getDetail(
            @PathVariable Long id) {

        return Result.success(
                visitRecordService.getAdminDetail(id)
        );
    }
}