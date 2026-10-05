package com.example.meal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.meal.dto.MealCategoryCreateRequest;
import com.example.meal.entity.MealCategory;
import com.example.meal.entity.MealStore;
import com.example.meal.exception.BusinessException;
import com.example.meal.mapper.MealCategoryMapper;
import com.example.meal.mapper.MealStoreMapper;
import com.example.meal.service.MealCategoryService;
import com.example.meal.vo.MealCategoryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜品分类业务实现类。
 */
@Service
public class MealCategoryServiceImpl
        implements MealCategoryService {

    private static final String STATUS_ENABLED = "ENABLED";

    private final MealCategoryMapper mealCategoryMapper;

    private final MealStoreMapper mealStoreMapper;

    public MealCategoryServiceImpl(
            MealCategoryMapper mealCategoryMapper,
            MealStoreMapper mealStoreMapper) {

        this.mealCategoryMapper = mealCategoryMapper;
        this.mealStoreMapper = mealStoreMapper;
    }

    /**
     * 校验所属餐厅和分类名称，然后保存菜品分类。
     *
     * @param request 分类新增参数
     * @return 新增成功后的分类ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MealCategoryCreateRequest request) {

        MealStore mealStore =
                mealStoreMapper.selectById(request.getStoreId());

        if (mealStore == null) {
            throw new BusinessException("所属餐厅不存在");
        }

        if (!STATUS_ENABLED.equals(mealStore.getStatus())) {
            throw new BusinessException("所属餐厅已经停用");
        }

        String categoryName = request.getName().trim();

        Long count = mealCategoryMapper.selectCount(
                new LambdaQueryWrapper<MealCategory>()
                        .eq(
                                MealCategory::getStoreId,
                                request.getStoreId()
                        )
                        .eq(
                                MealCategory::getName,
                                categoryName
                        )
        );

        if (count > 0) {
            throw new BusinessException(
                    "该餐厅下已经存在同名分类"
            );
        }

        MealCategory category = new MealCategory();

        category.setStoreId(request.getStoreId());
        category.setName(categoryName);
        category.setSortOrder(
                request.getSortOrder() == null
                        ? 0
                        : request.getSortOrder()
        );
        category.setStatus(STATUS_ENABLED);

        int affectedRows =
                mealCategoryMapper.insert(category);

        if (affectedRows != 1) {
            throw new BusinessException("新增分类失败");
        }

        return category.getId();
    }

    /**
     * 校验筛选条件并查询分类列表。
     *
     * @param storeId 可选的餐厅ID
     * @return 分类列表
     */
    @Override
    public List<MealCategoryVO> list(Long storeId) {

        if (storeId != null && storeId <= 0) {
            throw new BusinessException("餐厅ID必须大于0");
        }

        return mealCategoryMapper.selectCategoryList(storeId);
    }
}