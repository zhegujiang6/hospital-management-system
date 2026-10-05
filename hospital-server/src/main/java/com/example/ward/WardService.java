package com.example.ward;

import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

public interface WardService extends IService<Ward> {

    Long create(WardCreateRequest request);

    List<WardVO> listDetails();

    WardVO getDetail(Long id);
}