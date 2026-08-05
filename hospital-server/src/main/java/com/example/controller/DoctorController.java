package com.example.controller;


import com.example.common.Result;
import com.example.dto.DoctorCreateRequest;
import com.example.dto.DoctorUpdateRequest;
import com.example.service.DoctorService;
import com.example.vo.DoctorVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {

        this.doctorService = doctorService;
    }


    @PostMapping
    public Result<Long> create(
            @Valid @RequestBody DoctorCreateRequest request) {

        Long id = doctorService.create(request);

        return Result.success(id);
    }


    @GetMapping
    public Result<List<DoctorVO>> list() {
        List<DoctorVO> doctors = doctorService.listDetails();
        return Result.success(doctors);
    }

    @GetMapping("/{id}")
    public Result<DoctorVO> getDetail(@PathVariable Long id) {
        DoctorVO doctor = doctorService.getDetail(id);
        return Result.success(doctor);
    }


    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                           @Valid @RequestBody DoctorUpdateRequest request) {
        doctorService.update(id, request);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return Result.success(null);

    }

}
