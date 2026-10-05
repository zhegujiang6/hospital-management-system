package com.example.medicine;

import com.example.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/medicine")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService){
        this.medicineService = medicineService;
    }


    @PostMapping
    public Result<Long> create(
            @Valid@RequestBody MedicineCreateRequest request){
        Long id = medicineService.create(request);
        return Result.success(id);
    }

}
