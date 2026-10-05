package com.example.medicine;

import com.baomidou.mybatisplus.spring.service.IService;


public interface MedicineService extends IService<Medicine> {

    Long create (MedicineCreateRequest request);

}
