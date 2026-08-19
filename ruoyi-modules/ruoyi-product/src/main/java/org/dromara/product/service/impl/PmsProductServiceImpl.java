package org.dromara.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.product.domain.*;
import org.dromara.product.domain.bo.*;
import org.dromara.product.domain.vo.PmsProductVo;
import org.dromara.product.mapper.*;
import org.dromara.product.service.IPmsProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PmsProductServiceImpl implements IPmsProductService {

    private final PmsProductMapper baseMapper;
    private final PmsCategoryMapper categoryMapper;
    private final PmsProductSpecMapper specMapper;
    private final PmsProductSpecValueMapper specValueMapper;
    private final PmsProductSkuMapper skuMapper;
    private final PmsSkuSpecValueMapper skuSpecValueMapper;

//    商品状态：草稿
    private static final String PRODUCT_STATUS_DRAFT = "0";
//    默认商品单位
    private static final String DEFAULT_UNIT = "件";
//    默认排序
    private static final Integer DEFAULT_SORT_NUM = 0;

    // SKU类型：默认
    private static final String SKU_TYPE_DEFAULT = "0";

    // SKU类型：自定义规格
    private static final String SKU_TYPE_CUSTOM = "1";

    // SKU状态：正常
    private static final String SKU_STATUS_NORMAL = "0";

    // 默认SKU名称
    private static final String DEFAULT_SKU_NAME = "默认";

    @Override
    public PmsProductVo queryById(Long productId) {
        PmsProductVo product = baseMapper.selectVoById(productId);

        if (product == null) {
            return null;
        }
        fillCategoryNames(List.of(product));
        return product;
    }

    private void fillCategoryNames(List<PmsProductVo> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        Set<Long> categoryIds = products.stream()
                .map(PmsProductVo::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (categoryIds.isEmpty()) {
            return;
        }
        List<PmsCategory> pmsCategories = categoryMapper.selectList(
            new LambdaQueryWrapper<PmsCategory>().in(PmsCategory::getCategoryId, categoryIds)
        );

        Map<Long, String> categoryMap = pmsCategories.stream()
                .collect(Collectors.toMap(
                    PmsCategory::getCategoryId,
                    PmsCategory::getCategoryName,
                    (first, second) -> first));
        products.forEach(product->
            product.setCategoryName(categoryMap.get(product.getCategoryId())));

    }

    @Override
    public TableDataInfo<PmsProductVo> queryPageList(PmsProductQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<PmsProduct> wrapper =buildQueryWrapper(bo);

        Page<PmsProductVo> result = baseMapper.selectVoPage(pageQuery.build(), wrapper);

        fillCategoryNames(result.getRecords());
        return TableDataInfo.build(result);
    }

    private LambdaQueryWrapper<PmsProduct> buildQueryWrapper(PmsProductQueryBo bo) {
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(bo.getProductId() != null, PmsProduct::getProductId, bo.getProductId());
        wrapper.like(StringUtils.isNoneBlank(bo.getProductCode()), PmsProduct::getProductCode, bo.getProductCode());
        wrapper.like(StringUtils.isNoneBlank(bo.getProductName()), PmsProduct::getProductName, bo.getProductName());
        wrapper.eq(bo.getCategoryId() != null, PmsProduct::getCategoryId, bo.getCategoryId());
        wrapper.eq(StringUtils.isNoneBlank(bo.getStatus()), PmsProduct::getStatus, bo.getStatus());
        wrapper.orderByAsc(PmsProduct::getSortNum);
        wrapper.orderByDesc(PmsProduct::getCreateTime);
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(PmsProductCreateBo bo) {
//      1、校验规格、价格、库存和 SKU 组合
        validateCreateData(bo);
//      2、校验商品分类
        validateProductCategory(bo.getCategoryId());
//      3、构造商品主表数据
        PmsProduct product = buildProduct(bo);
//      4、保存商品主表
        if (baseMapper.insert(product) <=0) {
            throw new ServiceException("商品保存失败");
        }
//      5、保存规格和规格值，并取得临时 key 到数据库记录的映射
        Map<String,PmsProductSpecValue> savedValueMap = saveSpecsAndValues(
            product.getProductId(),bo.getSpecs()
        );

//      6、保存 SKU 和关联关系
        saveSkuAndRelations(product.getProductId(),bo.getSkus(),savedValueMap);
        return true;
    }
//  校验新增商品的规格和SKU数据
    private void validateCreateData(PmsProductCreateBo bo) {
        List<PmsProductSpecSaveBo> specs =
            bo.getSpecs() == null ? Collections.emptyList() : bo.getSpecs();
        List<PmsProductSkuSaveBo> skus =
            bo.getSkus() == null ? Collections.emptyList() : bo.getSkus();

        if (skus.isEmpty()) {
            throw new ServiceException("商品至少需要一个SKU");
        }

//       无论普通商品还是多规格商品，都要校验价格和库存
        skus.forEach(this::validateSkuPriceAndStock);

        if (specs.isEmpty()){
            validateDefaultSku(skus);
            return;
        }
        validateMultiSpec(specs, skus);
    }
    //    校验 SKU 价格和库存
    private void validateSkuPriceAndStock(PmsProductSkuSaveBo sku) {
        if (sku.getSalePrice() == null || sku.getSalePrice().signum() < 0) {
            throw new ServiceException("SKU销售价格不能为空且不能小于0");
        }
        if (sku.getMarketPrice() != null && sku.getMarketPrice().compareTo(sku.getSalePrice()) < 0) {
            throw new ServiceException("SKU市场价不能小于销售价格");
        }
        if (sku.getStock() == null || sku.getStock() < 0) {
            throw new ServiceException("SKU库存数量不能为空且不能小于0");
        }
        if (sku.getWarningStock() != null && sku.getWarningStock() < 0) {
            throw new ServiceException("SKU预警库存不能小于0");
        }
    }

    //    校验普通商品的默认 SKu
    private void validateDefaultSku(List<PmsProductSkuSaveBo> skus) {
        if (skus.size() != 1){
            throw new ServiceException("普通商品只能有一个默认SKU");
        }
        List<String> keys = skus.getFirst().getSpecValueKeys();
        if (keys != null && !keys.isEmpty()) {
            throw new ServiceException("普通商品的默认SKU不能有规格值");
        }
    }
    //    校验多规格和 SKU 组合
    private void validateMultiSpec(List<PmsProductSpecSaveBo> specs, List<PmsProductSkuSaveBo> skus) {
        Set<String> specKeys = new HashSet<>();
        Map<String, String> valueKeyToSpecKey = new HashMap<>();

        for (PmsProductSpecSaveBo spec : specs) {
            String specName = normalizeText(spec.getSpecName());
            if (StringUtils.isBlank(specName)) {
                throw new ServiceException("规格名不能为空");
            }
            if (!specKeys.add(specName)) {
                throw new ServiceException("规格名不能重复: " + specName);
            }
            if (spec.getValues() == null || spec.getValues().isEmpty()) {
                throw new ServiceException("规格必须有至少一个规格值: " + specName);
            }

            Set<String> valueNames = new HashSet<>();

            for (PmsProductSpecValueSaveBo value : spec.getValues()) {
                String valueKey = normalizeText(value.getValueKey());
                String valueName = normalizeText(value.getSpecValue());

                if (StringUtils.isBlank(valueKey)){
                    throw new ServiceException("规格值Key不能为空: " + specName);
                }
                if (StringUtils.isBlank(valueName)){
                    throw new ServiceException("规格值名称不能为空: " + specName);
                }
                if (!valueNames.add(valueName)) {
                    throw new ServiceException(
                        "规格“" + spec.getSpecName() + "”存在重复规格值："
                            + value.getSpecValue()
                    );
                }
                if (valueKeyToSpecKey.putIfAbsent(valueKey,specName) != null) {
                    throw new ServiceException(
                        "规格值临时标识不能重复：" + value.getValueKey()
                    );
                }
            }
        }
        validateSkuCombinations(specs.size(), skus, valueKeyToSpecKey);
    }

    //    校验每条 SKU 选择的规格值
    private void validateSkuCombinations(int specCount, List<PmsProductSkuSaveBo> skus, Map<String, String> valueKeyToSpecKey) {
        Set<String> combinationKeys = new HashSet<>();
        for (PmsProductSkuSaveBo sku : skus) {
            List<String> specValueKeys = sku.getSpecValueKeys();
            if (specValueKeys == null || specValueKeys.size() != specCount) {
                throw new ServiceException("每条 SKU 必须从每个规格中选择一个规格值");
            }
            Set<String> selectedSpecs = new HashSet<>();
            List<String> normalizedKeys = new ArrayList<>();
            for (String valueKey : specValueKeys) {
                String normalizedKey = normalizeText(valueKey);
                String specName = valueKeyToSpecKey.get(normalizedKey);
                if (specName == null) {
                    throw new ServiceException("SKU选择的规格值不存在: " + valueKey);
                }
                if (!selectedSpecs.add(specName)) {
                    throw new ServiceException(
                        "SKU选择的规格值中存在重复的规格: " + specName
                    );
                }
                normalizedKeys.add(normalizedKey);
            }

//            排序后，BLACK, 256G 和 256G, BLACK 被视为相同组合
            Collections.sort(normalizedKeys);
            String combinationKey = String.join(",", normalizedKeys);
            if (!combinationKeys.add(combinationKey)) {
                throw new ServiceException(
                    "SKU选择的规格值组合重复: " + combinationKey
                );
            }
        }
    }

    //    根据新增请求构造商品主表数据
    private PmsProduct buildProduct(PmsProductCreateBo bo) {
        PmsProduct product = MapstructUtils.convert(bo, PmsProduct.class);

        if (product == null) {
            throw new ServiceException("商品数据转换失败");
        }

        Long productId = IdWorker.getId();;

        product.setProductId(productId);
        product.setProductCode(generateBusinessCode("SPU",productId));

        normalizeProductForInsert(product);

        fillProductSkuSummary(product, bo.getSkus());
        return product;
    }
//  根据 SKU 数据填充商品的价格和库存汇总信息
    private void fillProductSkuSummary(PmsProduct product, List<PmsProductSkuSaveBo> skus) {
        BigDecimal minPrice = skus.stream()
            .map(PmsProductSkuSaveBo::getSalePrice)
            .min(BigDecimal::compareTo)
            .orElseThrow(
                () -> new ServiceException("商品至少需要一个SKU")
            );

        BigDecimal maxPrice = skus.stream()
            .map(PmsProductSkuSaveBo::getSalePrice)
            .max(BigDecimal::compareTo)
            .orElseThrow(
                () -> new ServiceException("商品至少需要一个SKU")
            );

        long totalStock = skus.stream()
            .mapToLong(PmsProductSkuSaveBo::getStock)
            .sum();

        if (totalStock>Integer.MAX_VALUE) {
            throw new ServiceException("商品总库存超过系统限制");
        }
        product.setMinPrice(minPrice);
        product.setMaxPrice(maxPrice);
        product.setTotalStock((int) totalStock);
    }
//  整理新增商品的基础字段
    private void normalizeProductForInsert(PmsProduct product) {
        product.setProductName(StringUtils.trim(product.getProductName()));

        product.setSellingPoint(StringUtils.trim(product.getSellingPoint()));

        String unit = StringUtils.trim(product.getUnit());

        if (StringUtils.isBlank(unit)) {
            product.setUnit(DEFAULT_UNIT);
        } else {
            product.setUnit(unit);
        }

        if (product.getSortNum() == null) {
            product.setSortNum(DEFAULT_SORT_NUM);
        }

//        新建商品统一进入草稿状态
        product.setStatus(PRODUCT_STATUS_DRAFT);
    }

// 校验商品所属分类
    private void validateProductCategory(Long categoryId) {
        if (categoryId == null) {
            throw new ServiceException("商品所属分类不能为空");
        }
        PmsCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new ServiceException("商品所属分类不存在");
        }
        if (!SystemConstants.NORMAL.equals(category.getStatus())) {
            throw new ServiceException("商品所属分类已禁用");
        }
        boolean hasChildren = categoryMapper.selectCount(
            new LambdaQueryWrapper<PmsCategory>()
                .eq(PmsCategory::getParentId, categoryId)
        ) > 0;
        if (hasChildren) {
            throw new ServiceException("商品所属分类不能是父级分类");
        }
    }

//  保存商品规格和规格值
    private Map<String, PmsProductSpecValue> saveSpecsAndValues(Long productId, List<PmsProductSpecSaveBo> specBos) {
        Map<String, PmsProductSpecValue> savedValueMap = new LinkedHashMap<>();
        if (specBos == null || specBos.isEmpty()) {
            return savedValueMap;
        }
        for (int specIndex = 0; specIndex < specBos.size(); specIndex++) {
            PmsProductSpecSaveBo specBo = specBos.get(specIndex);

            PmsProductSpec spec = new PmsProductSpec();
            Long specId = IdWorker.getId();
            spec.setSpecId(specId);
            spec.setProductId(productId);
            spec.setSpecName(StringUtils.trim(specBo.getSpecName()));
            spec.setSortNum(specBo.getSortNum() == null? specIndex:specBo.getSortNum());

            if (specMapper.insert(spec)<=0){
                throw new ServiceException("商品规格保存失败："+ specBo.getSpecName());
            }
            saveSpecValues(productId,spec,specBo.getValues(),savedValueMap);
        }
        return savedValueMap;
    }
//  保存一个规格下面所有规格值
    private void saveSpecValues(Long productId, PmsProductSpec spec,  List<PmsProductSpecValueSaveBo> valueBos, Map<String, PmsProductSpecValue> savedValueMap) {
        for (int valueIndex = 0; valueIndex<valueBos.size(); valueIndex++) {
            PmsProductSpecValueSaveBo valueBo = valueBos.get(valueIndex);

            PmsProductSpecValue specValue = new PmsProductSpecValue();

            specValue.setSpecValueId(IdWorker.getId());
            specValue.setProductId(productId);
            specValue.setSpecId(spec.getSpecId());
            specValue.setSpecValue(StringUtils.trim(valueBo.getSpecValue()));
            specValue.setSortNum(valueBo.getSortNum() == null? valueIndex:valueBo.getSortNum());

            if (specValueMapper.insert(specValue)<=0){
                throw new ServiceException("商品规格值保存失败："+valueBo.getSpecValue());
            }

            String normalizedKey = normalizeText(valueBo.getValueKey());

            savedValueMap.put(normalizedKey,specValue);
        }
    }
//  保存 SKU 以及 SKU 与规格值的关联
    private void saveSkuAndRelations(Long productId,
                                     List<PmsProductSkuSaveBo> skuBos,
                                     Map<String,PmsProductSpecValue> savedValueMap) {
        boolean defaultSku = savedValueMap.isEmpty();
        for (int skuIndex = 0; skuIndex < skuBos.size(); skuIndex++) {
            PmsProductSkuSaveBo skuBo = skuBos.get(skuIndex);
            Long skuId = IdWorker.getId();
            PmsProductSku sku = new PmsProductSku();

            sku.setSkuId(skuId);
            sku.setProductId(productId);
            sku.setSkuCode(generateBusinessCode("SKU",skuId));
            sku.setSkuType(defaultSku?SKU_TYPE_DEFAULT:SKU_TYPE_CUSTOM);
            sku.setSkuName(defaultSku?DEFAULT_SKU_NAME: buildSKuName(skuBo.getSpecValueKeys(),savedValueMap));
            sku.setSkuImageId(skuBo.getSkuImageId());
            sku.setSalePrice(skuBo.getSalePrice());
            sku.setMarketPrice(skuBo.getMarketPrice());
            sku.setStock(skuBo.getStock());

            sku.setWarningStock(skuBo.getWarningStock() == null ?0 :skuBo.getWarningStock());
            sku.setStatus(SKU_STATUS_NORMAL);

            sku.setSortNum(skuBo.getSortNum() == null?skuIndex:skuBo.getSortNum());

            if (skuMapper.insert(sku)<=0){
                throw new ServiceException("商品 SKU 保存失败"+sku.getSkuName());
            }
            if (!defaultSku){
                saveSkuRelations(
                    productId,skuId,skuBo.getSpecValueKeys(),savedValueMap
                );
            }
        }

    }
//  保存 SKU 关系
    private void saveSkuRelations(Long productId, Long skuId,  List<String> specValueKeys, Map<String, PmsProductSpecValue> savedValueMap) {
        for (String valueKey: specValueKeys) {
            String normalizedKey = normalizeText(valueKey);

            PmsProductSpecValue specValue =savedValueMap.get(normalizedKey);
            if (specValue == null){
                throw new ServiceException("SKU引用的规格值不存在："+valueKey);
            }
            PmsSkuSpecValue  relation = new PmsSkuSpecValue();

            relation.setRelationId(IdWorker.getId());
            relation.setProductId(productId);
            relation.setSkuId(skuId);
            relation.setSpecId(specValue.getSpecId());
            relation.setSpecValueId(specValue.getSpecValueId());
            if (skuSpecValueMapper.insert(relation)<=0){
                throw new ServiceException("SKU规格关系保存失败");
            }
        }

    }

    private String buildSKuName( List<String> specValueKeys, Map<String, PmsProductSpecValue> savedValueMap) {
        Set<String> selectedKeys = specValueKeys.stream().map(this::normalizeText).collect(Collectors.toSet());
        String skuName = savedValueMap.entrySet().stream()
            .filter(entry->selectedKeys.contains(entry.getKey()))
            .map(entry-> entry.getValue().getSpecValue())
            .collect(Collectors.joining("/"));
        if (StringUtils.isBlank(skuName)) {
            throw new ServiceException("SKU名称生成失败");
        }
        return skuName;
    }

    /**
     * 去除前后空格，并统一大小写用于比较
     */
    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    //  生成商品编码，使用36进制表示商品ID
    private String generateBusinessCode(String prefix, Long productId) {
        String value = Long.toString(productId,36).toUpperCase(Locale.ROOT);
        return prefix  + value;
    }
    @Override
    public Boolean updateByBo(PmsProductBo bo) {
        return null;
    }

    @Override
    public Boolean deleteWithValidById(Long productId) {
        return null;
      }

}
