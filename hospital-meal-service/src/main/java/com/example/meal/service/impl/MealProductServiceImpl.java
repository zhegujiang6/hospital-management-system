package com.example.meal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.meal.dto.MealProductCreateRequest;
import com.example.meal.entity.MealCategory;
import com.example.meal.entity.MealProduct;
import com.example.meal.entity.MealStore;
import com.example.meal.exception.BusinessException;
import com.example.meal.mapper.MealCategoryMapper;
import com.example.meal.mapper.MealProductMapper;
import com.example.meal.mapper.MealStoreMapper;
import com.example.meal.service.MealProductService;
import com.example.meal.vo.MealProductVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * 菜品业务实现类。
 */
@Service
public class MealProductServiceImpl
        implements MealProductService {

    private static final String STATUS_ENABLED = "ENABLED";

    private static final String STATUS_ON_SALE = "ON_SALE";

    private static final String STATUS_OFF_SALE = "OFF_SALE";

    private final MealProductMapper mealProductMapper;

    private final MealCategoryMapper mealCategoryMapper;

    private final MealStoreMapper mealStoreMapper;


    public MealProductServiceImpl(
            MealProductMapper mealProductMapper,
            MealCategoryMapper mealCategoryMapper,
            MealStoreMapper mealStoreMapper) {

        this.mealProductMapper = mealProductMapper;
        this.mealCategoryMapper = mealCategoryMapper;
        this.mealStoreMapper = mealStoreMapper;
    }

    /**
     * 校验分类、餐厅和菜品编号，然后保存菜品。
     *
     * @param request 菜品新增参数
     * @return 新增成功后的菜品ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MealProductCreateRequest request) {

        MealCategory category =
                mealCategoryMapper.selectById(
                        request.getCategoryId()
                );

        if (category == null) {
            throw new BusinessException("所属分类不存在");
        }

        if (!STATUS_ENABLED.equals(category.getStatus())) {
            throw new BusinessException("所属分类已经停用");
        }

        MealStore store =
                mealStoreMapper.selectById(
                        category.getStoreId()
                );

        if (store == null) {
            throw new BusinessException("所属餐厅不存在");
        }

        if (!STATUS_ENABLED.equals(store.getStatus())) {
            throw new BusinessException("所属餐厅已经停用");
        }

        String productNo = request.getProductNo()
                .trim()
                .toUpperCase(Locale.ROOT);

        Long count = mealProductMapper.selectCount(
                new LambdaQueryWrapper<MealProduct>()
                        .eq(
                                MealProduct::getProductNo,
                                productNo
                        )
        );

        if (count > 0) {
            throw new BusinessException("菜品编号已经存在");
        }

        MealProduct product = new MealProduct();

        product.setCategoryId(request.getCategoryId());
        product.setProductNo(productNo);
        product.setName(request.getName().trim());
        product.setDescription(
                normalizeOptional(request.getDescription())
        );
        product.setPrice(request.getPrice());
        product.setImageUrl(
                normalizeOptional(request.getImageUrl())
        );
        product.setDietaryTags(
                normalizeOptional(request.getDietaryTags())
        );
        product.setAllergenInfo(
                normalizeOptional(request.getAllergenInfo())
        );
        product.setStatus(STATUS_ON_SALE);

        int affectedRows =
                mealProductMapper.insert(product);

        if (affectedRows != 1) {
            throw new BusinessException("新增菜品失败");
        }

        return product.getId();
    }

    /**
     * 清理非必填字符串。
     * 没有内容时返回null，否则删除首尾空格。
     *
     * @param value 原始字符串
     * @return 整理后的字符串
     */
    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }


    /**
     * 校验筛选条件并查询菜品列表。
     *
     * @param storeId 可选的餐厅ID
     * @param categoryId 可选的分类ID
     * @param status 可选的菜品状态
     * @return 菜品列表
     */
    @Override
    public List<MealProductVO> list(
            Long storeId,
            Long categoryId,
            String status) {

        if (storeId != null && storeId <= 0) {
            throw new BusinessException("餐厅ID必须大于0");
        }

        if (categoryId != null && categoryId <= 0) {
            throw new BusinessException("分类ID必须大于0");
        }

        String normalizedStatus = null;

        if (status != null && !status.isBlank()) {
            normalizedStatus =
                    status.trim().toUpperCase(Locale.ROOT);

            boolean validStatus =
                    STATUS_ON_SALE.equals(normalizedStatus)
                            || STATUS_OFF_SALE.equals(normalizedStatus);

            if (!validStatus) {
                throw new BusinessException("菜品状态不正确");
            }
        }

        return mealProductMapper.selectProductList(
                storeId,
                categoryId,
                normalizedStatus
        );
    }
}