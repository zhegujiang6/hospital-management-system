package com.example.bed;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.exception.BusinessException;
import com.example.ward.WardService;
import com.example.ward.WardVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class BedServiceImpl
        extends ServiceImpl<BedMapper, Bed>
        implements BedService {

    private final WardService wardService;

    public BedServiceImpl(
            WardService wardService) {

        this.wardService = wardService;
    }

    @Override
    public Long create(BedCreateRequest request) {

        // 验证病区是否存在。
        WardVO ward =
                wardService.getDetail(
                        request.getWardId()
                );

        // 停用病区不能继续添加床位。
        if (!Integer.valueOf(1).equals(
                ward.getStatus())) {

            throw new BusinessException(
                    "所属病区已停用"
            );
        }

        // 统一整理房间号和床位号。
        String roomNo =
                request.getRoomNo()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        String bedNo =
                request.getBedNo()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        // 同一病区、同一房间、同一床位号不能重复。
        boolean exists = lambdaQuery()
                .eq(
                        Bed::getWardId,
                        request.getWardId()
                )
                .eq(Bed::getRoomNo, roomNo)
                .eq(Bed::getBedNo, bedNo)
                .exists();

        if (exists) {
            throw new BusinessException(
                    "该房间的床位号已存在"
            );
        }

        Bed bed = new Bed();
        bed.setWardId(request.getWardId());
        bed.setRoomNo(roomNo);
        bed.setBedNo(bedNo);

        // 新床位只能从空闲状态开始。
        bed.setStatus(
                BedStatus.AVAILABLE.name()
        );

        try {
            save(bed);
        } catch (DuplicateKeyException exception) {

            // 防止两个并发请求创建相同床位。
            throw new BusinessException(
                    "该房间的床位号已存在"
            );
        }

        return bed.getId();
    }

    @Override
    public List<BedVO> listDetails() {

        return baseMapper.selectBedList();
    }

    @Override
    public BedVO getDetail(Long id) {

        BedVO bed =
                baseMapper.selectBedDetail(id);

        if (bed == null) {
            throw new BusinessException(
                    "床位不存在"
            );
        }

        return bed;
    }

    /**
     * 使用带状态条件的UPDATE原子占用床位。
     */
    @Override
    public boolean occupy(Long bedId) {

        return lambdaUpdate()
                .eq(Bed::getId, bedId)
                .eq(
                        Bed::getStatus,
                        BedStatus.AVAILABLE.name()
                )
                .set(
                        Bed::getStatus,
                        BedStatus.OCCUPIED.name()
                )
                .update();
    }


    /**
     * 使用带状态条件的UPDATE原子释放床位。
     */
    @Override
    public boolean release(Long bedId) {

        return lambdaUpdate()
                .eq(Bed::getId, bedId)
                .eq(
                        Bed::getStatus,
                        BedStatus.OCCUPIED.name()
                )
                .set(
                        Bed::getStatus,
                        BedStatus.AVAILABLE.name()
                )
                .update();
    }


}