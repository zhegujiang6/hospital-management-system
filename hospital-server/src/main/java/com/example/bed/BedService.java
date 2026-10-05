package com.example.bed;

import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

public interface BedService extends IService<Bed> {

    Long create(BedCreateRequest request);

    List<BedVO> listDetails();

    BedVO getDetail(Long id);

    boolean occupy(Long bedId);

    /**
     * 将已占用床位释放为空闲状态。
     *
     * @return 只有床位原来为已占用状态时返回true
     */
    boolean release(Long bedId);
}