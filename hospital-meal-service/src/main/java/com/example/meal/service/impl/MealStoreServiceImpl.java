package com.example.meal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.meal.dto.MealStoreCreateRequest;
import com.example.meal.entity.MealStore;
import com.example.meal.exception.BusinessException;
import com.example.meal.mapper.MealStoreMapper;
import com.example.meal.service.MealStoreService;
import com.example.meal.vo.MealStoreVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 餐厅业务实现类。
 */
@Service
public class MealStoreServiceImpl implements MealStoreService {

    private static final String STATUS_ENABLED = "ENABLED";

    private final MealStoreMapper mealStoreMapper;

    public MealStoreServiceImpl(
            MealStoreMapper mealStoreMapper) {

        this.mealStoreMapper = mealStoreMapper;
    }

    /**
     * 校验餐厅编号并把餐厅信息保存到数据库。
     *
     * @param request 新增餐厅的请求参数
     * @return 新增成功后的餐厅ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MealStoreCreateRequest request) {

        String storeNo =
                request.getStoreNo().trim().toUpperCase();

        Long count = mealStoreMapper.selectCount(
                new LambdaQueryWrapper<MealStore>()
                        .eq(MealStore::getStoreNo, storeNo)
        );

        if (count > 0) {
            throw new BusinessException("餐厅编号已经存在");
        }

        MealStore mealStore = new MealStore();

        mealStore.setStoreNo(storeNo);
        mealStore.setName(request.getName().trim());
        mealStore.setLocation(request.getLocation().trim());
        mealStore.setPhone(normalizePhone(request.getPhone()));
        mealStore.setStatus(STATUS_ENABLED);

        int affectedRows = mealStoreMapper.insert(mealStore);

        if (affectedRows != 1) {
            throw new BusinessException("新增餐厅失败");
        }

        return mealStore.getId();
    }

    /**
     * 清理联系电话。
     * 未填写或只包含空格时返回null，否则删除首尾空格。
     *
     * @param phone 前端提交的联系电话
     * @return 整理后的联系电话
     */
    private String normalizePhone(String phone) {

        if (phone == null || phone.isBlank()) {
            return null;
        }

        return phone.trim();
    }

    /**
     * 查询全部餐厅，并把数据库实体转换成VO。
     *
     * @return 按ID倒序排列的餐厅列表
     */
    @Override
    public List<MealStoreVO> list() {

        List<MealStore> mealStores =
                mealStoreMapper.selectList(
                        new LambdaQueryWrapper<MealStore>()
                                .orderByDesc(MealStore::getId)
                );

        return mealStores.stream()
                .map(MealStoreVO::fromEntity)
                .toList();
    }

}
