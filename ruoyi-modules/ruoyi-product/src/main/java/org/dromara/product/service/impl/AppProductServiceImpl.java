package org.dromara.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.product.domain.PmsCategory;
import org.dromara.product.domain.PmsProduct;
import org.dromara.product.domain.bo.AppProductQueryBo;
import org.dromara.product.domain.vo.AppProductListVo;
import org.dromara.product.mapper.PmsCategoryMapper;
import org.dromara.product.mapper.PmsProductMapper;
import org.dromara.product.service.IAppProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppProductServiceImpl implements IAppProductService {

//    商品状态：已上架
    private static final String PRODUCT_STATUS_ON_SALE = "1";

    private final PmsProductMapper productMapper;
    private final PmsCategoryMapper categoryMapper;

    @Override
    public TableDataInfo<AppProductListVo> queryPageList(AppProductQueryBo bo, PageQuery pageQuery) {

        LambdaQueryWrapper<PmsProduct> wrapper = buildQueryWrapper(bo);
        Page<AppProductListVo> result = productMapper.selectVoPage(pageQuery.build(),wrapper,AppProductListVo.class);

        fillCategoryNames(result.getRecords());

        fillAvailable(result.getRecords());


        return TableDataInfo.build(result);
    }

    private LambdaQueryWrapper<PmsProduct> buildQueryWrapper(AppProductQueryBo bo) {
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProduct::getStatus, PRODUCT_STATUS_ON_SALE);

        wrapper.eq(bo.getCategoryId() != null, PmsProduct::getCategoryId, bo.getCategoryId());

        wrapper.like(StringUtils.isNoneBlank(bo.getKeyword()), PmsProduct::getProductName, bo.getKeyword());

        wrapper.orderByAsc(PmsProduct::getSortNum);

        wrapper.orderByDesc(PmsProduct::getCreateTime);
//      只查询客户端列表需要的字段
        wrapper.select(
            PmsProduct::getProductId,
            PmsProduct::getProductName,
            PmsProduct::getSellingPoint,
            PmsProduct::getCategoryId,
            PmsProduct::getMainImageId,
            PmsProduct::getMinPrice,
            PmsProduct::getMaxPrice,
            PmsProduct::getTotalStock,
            PmsProduct::getUnit
        );
        return wrapper;
    }

    private void fillCategoryNames(List<AppProductListVo> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        Set<Long> categoryIds = products.stream()
            .map(AppProductListVo::getCategoryId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (categoryIds.isEmpty()) {
            return;
        }

        List<PmsCategory> pmsCategories = categoryMapper
            .selectList(new LambdaQueryWrapper<>(PmsCategory.class).in(PmsCategory::getCategoryId, categoryIds));

        Map<Long, PmsCategory> categoryMap = pmsCategories.stream()
            .collect(Collectors
                .toMap(
                    PmsCategory::getCategoryId,
                    Function.identity(),
                    (first, second) -> first
                )
            );

        products.forEach(product -> {
            PmsCategory category = categoryMap.get(product.getCategoryId());
            if (category != null) {
                product.setCategoryName(category.getCategoryName());
            }
        });
    }
//    根据汇总库存设置是否可以购买
    private void fillAvailable(List<AppProductListVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        records.forEach(product -> {
            boolean available = product.getTotalStock() != null && product.getTotalStock() > 0;
            product.setAvailable(available);
        });
    }
}
