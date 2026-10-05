package com.example.meal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.meal.dto.MealProductStockAdjustRequest;
import com.example.meal.dto.MealProductStockCreateRequest;
import com.example.meal.dto.MealProductStockUpdateRequest;
import com.example.meal.entity.MealProduct;
import com.example.meal.entity.MealProductStock;
import com.example.meal.enums.MealPeriod;
import com.example.meal.exception.BusinessException;
import com.example.meal.mapper.MealProductMapper;
import com.example.meal.mapper.MealProductStockMapper;
import com.example.meal.service.MealProductStockService;
import com.example.meal.vo.MealProductStockVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 菜品分时库存业务实现类。
 */
@Service
public class MealProductStockServiceImpl
        implements MealProductStockService {

    private final MealProductStockMapper mealProductStockMapper;

    private final MealProductMapper mealProductMapper;

    public MealProductStockServiceImpl(
            MealProductStockMapper mealProductStockMapper,
            MealProductMapper mealProductMapper) {

        this.mealProductStockMapper = mealProductStockMapper;
        this.mealProductMapper = mealProductMapper;
    }

    /**
     * 校验菜品、供应日期和重复库存，然后保存初始库存。
     *
     * @param request 库存新增参数
     * @return 新增成功后的库存ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(
            MealProductStockCreateRequest request) {

        MealProduct product =
                mealProductMapper.selectById(
                        request.getProductId()
                );

        if (product == null) {
            throw new BusinessException("菜品不存在");
        }

        if (request.getServiceDate()
                .isBefore(LocalDate.now())) {

            throw new BusinessException(
                    "不能设置过去日期的库存"
            );
        }

        Long count = mealProductStockMapper.selectCount(
                new LambdaQueryWrapper<MealProductStock>()
                        .eq(
                                MealProductStock::getProductId,
                                request.getProductId()
                        )
                        .eq(
                                MealProductStock::getServiceDate,
                                request.getServiceDate()
                        )
                        .eq(
                                MealProductStock::getMealPeriod,
                                request.getMealPeriod()
                        )
        );

        if (count > 0) {
            throw new BusinessException(
                    "该菜品在指定日期和餐次的库存已经存在"
            );
        }

        MealProductStock stock =
                new MealProductStock();

        stock.setProductId(request.getProductId());
        stock.setServiceDate(request.getServiceDate());
        stock.setMealPeriod(request.getMealPeriod());
        stock.setTotalStock(request.getTotalStock());

        /*
         * 新建库存时还没有订单，
         * 所以剩余库存必须等于总库存。
         */
        stock.setAvailableStock(
                request.getTotalStock()
        );

        /*
         * 乐观锁从第0个版本开始。
         */
        stock.setVersion(0);

        int affectedRows =
                mealProductStockMapper.insert(stock);

        if (affectedRows != 1) {
            throw new BusinessException("新增库存失败");
        }

        return stock.getId();
    }
    /**
     * 校验查询条件并查询分时库存。
     *
     * @param storeId 可选的餐厅ID
     * @param productId 可选的菜品ID
     * @param serviceDate 可选的供应日期
     * @param mealPeriod 可选的餐次
     * @return 库存列表
     */
    @Override
    public List<MealProductStockVO> list(
            Long storeId,
            Long productId,
            LocalDate serviceDate,
            MealPeriod mealPeriod) {

        if (storeId != null && storeId <= 0) {
            throw new BusinessException("餐厅ID必须大于0");
        }

        if (productId != null && productId <= 0) {
            throw new BusinessException("菜品ID必须大于0");
        }

        return mealProductStockMapper.selectStockList(
                storeId,
                productId,
                serviceDate,
                mealPeriod
        );
    }

    /**
     * 使用乐观锁修改库存总量，并重新计算剩余库存。
     *
     * @param stockId 库存ID
     * @param request 库存修改参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTotalStock(
            Long stockId,
            MealProductStockUpdateRequest request) {

        MealProductStock stock =
                mealProductStockMapper.selectById(stockId);

        if (stock == null) {
            throw new BusinessException("库存记录不存在");
        }

        if (stock.getServiceDate()
                .isBefore(LocalDate.now())) {

            throw new BusinessException(
                    "不能修改过去日期的库存"
            );
        }

        if (!stock.getVersion()
                .equals(request.getVersion())) {

            throw new BusinessException(
                    "库存数据已经被修改，请刷新后重试"
            );
        }

        int soldStock =
                stock.getTotalStock()
                        - stock.getAvailableStock();

        if (request.getTotalStock() < soldStock) {
            throw new BusinessException(
                    "总库存不能小于已经售出的数量"
            );
        }

        int newAvailableStock =
                request.getTotalStock() - soldStock;

        stock.setTotalStock(
                request.getTotalStock()
        );

        stock.setAvailableStock(
                newAvailableStock
        );

        int affectedRows =
                mealProductStockMapper.updateById(stock);

        if (affectedRows != 1) {
            throw new BusinessException(
                    "库存数据已经被其他人修改，请刷新后重试"
            );
        }
    }

    /**
     * 校验库存记录和调整数量，然后原子增减库存。
     *
     * @param stockId 库存记录ID
     * @param request 库存调整参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(
            Long stockId,
            MealProductStockAdjustRequest request) {

        if (stockId == null || stockId <= 0) {
            throw new BusinessException("库存ID必须大于0");
        }

        Integer quantityDelta =
                request.getQuantityDelta();

        if (quantityDelta == null) {
            throw new BusinessException("库存变化量不能为空");
        }

        if (quantityDelta == 0) {
            throw new BusinessException("库存变化量不能为0");
        }

        MealProductStock stock =
                mealProductStockMapper.selectById(stockId);

        if (stock == null) {
            throw new BusinessException("库存记录不存在");
        }

        if (stock.getServiceDate()
                .isBefore(LocalDate.now())) {

            throw new BusinessException(
                    "不能调整过去日期的库存"
            );
        }

        int affectedRows =
                mealProductStockMapper.adjustStock(
                        stockId,
                        quantityDelta
                );

        if (affectedRows != 1) {
            if (quantityDelta < 0) {
                throw new BusinessException(
                        "可用库存不足，无法减少指定数量"
                );
            }

            throw new BusinessException("库存调整失败");
        }
    }
}