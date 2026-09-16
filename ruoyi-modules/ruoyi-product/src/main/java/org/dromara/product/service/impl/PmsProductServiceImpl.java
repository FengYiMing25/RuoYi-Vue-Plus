package org.dromara.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.product.domain.*;
import org.dromara.product.domain.bo.*;
import org.dromara.product.domain.vo.*;
import org.dromara.product.mapper.*;
import org.dromara.product.service.IPmsProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ConfigurableTransactionManager;
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

    /**
     * 默认SKU规格组合键
     */
    private static final String DEFAULT_SPEC_KEY = "DEFAULT";

    /**
     * 商品状态：已上架
     */
    private static final String PRODUCT_STATUS_ON_SALE = "1";
    private final ConfigurableTransactionManager configurableTransactionManager;

    @Override
    public PmsProductDetailVo queryDetailById(Long productId) {
        PmsProductDetailVo product = baseMapper.selectVoById(productId,PmsProductDetailVo.class);

        if (product == null) {
            return null;
        }
        fillCategoryNames(Collections.singletonList(product));
        fillProductSpecsAndSkus(product);
        return product;
    }

    private void fillProductSpecsAndSkus(PmsProductDetailVo product) {
        Long productId = product.getProductId();

        List<PmsProductSpec> specs = specMapper.selectList(
            new LambdaQueryWrapper<PmsProductSpec>()
                .eq(PmsProductSpec::getProductId, productId)
                .orderByAsc(PmsProductSpec::getSortNum)
                .orderByAsc(PmsProductSpec::getSpecId)
        );
        List<PmsProductSpecValue> specValues = specValueMapper.selectList(
            new LambdaQueryWrapper<PmsProductSpecValue>()
                .eq(PmsProductSpecValue::getProductId, productId)
                .orderByAsc(PmsProductSpecValue::getSortNum)
                .orderByAsc(PmsProductSpecValue::getSpecValueId)
        );
        List<PmsProductSkuVo> skus = skuMapper.selectVoList(
            new LambdaQueryWrapper<PmsProductSku>()
                .eq(PmsProductSku::getProductId, productId)
                .orderByAsc(PmsProductSku::getSortNum)
                .orderByAsc(PmsProductSku::getSkuId)
        );
        List<PmsSkuSpecValue> relations = skuSpecValueMapper.selectList(
            new LambdaQueryWrapper<PmsSkuSpecValue>()
                .eq(PmsSkuSpecValue::getProductId, productId)
                .orderByAsc(PmsSkuSpecValue::getRelationId)
        );
//      把数据库实体转换成 List
        List<PmsProductSpecVo> specVos = MapstructUtils.convert(specs, PmsProductSpecVo.class);
//      把 List 转换成 以 specId 为 key 的 Map
        Map<Long, PmsProductSpecVo> specVoMap = specVos.stream()
            .collect(Collectors.toMap(
                PmsProductSpecVo::getSpecId,
                spec -> spec,
                (first, second) -> first,
                LinkedHashMap::new
            ));
        Map<Long, PmsProductSpec> specMap = specs.stream()
            .collect(Collectors.toMap(PmsProductSpec::getSpecId, spec -> spec));
        Map<Long, PmsProductSpecValue> specValueMap = specValues.stream()
            .collect(Collectors.toMap(
                PmsProductSpecValue::getSpecValueId,
                specValue -> specValue
            ));

        for (PmsProductSpecValue specValue : specValues) {
            PmsProductSpecVo specVo = specVoMap.get(specValue.getSpecId());
            if (specVo != null) {
                specVo.getValues().add(
                    MapstructUtils.convert(specValue, PmsProductSpecValueVo.class)
                );
            }
        }

        Map<Long, PmsProductSkuVo> skuVoMap = skus.stream()
            .collect(Collectors.toMap(
                PmsProductSkuVo::getSkuId,
                sku -> sku,
                (first, second) -> first,
                LinkedHashMap::new
            ));
        for (PmsSkuSpecValue relation : relations) {
            PmsProductSkuVo skuVo = skuVoMap.get(relation.getSkuId());
            PmsProductSpec spec = specMap.get(relation.getSpecId());
            PmsProductSpecValue specValue = specValueMap.get(relation.getSpecValueId());
            if (skuVo == null || spec == null || specValue == null) {
                continue;
            }

            PmsSkuSpecValueVo selectedValue = new PmsSkuSpecValueVo();
            selectedValue.setSpecId(spec.getSpecId());
            selectedValue.setSpecName(spec.getSpecName());
            selectedValue.setSpecValueId(specValue.getSpecValueId());
            selectedValue.setSpecValue(specValue.getSpecValue());
            skuVo.getSpecValues().add(selectedValue);
        }

        Map<Long, Integer> specOrderMap = specs.stream()
            .collect(Collectors.toMap(
                PmsProductSpec::getSpecId,
                spec -> spec.getSortNum() == null ? 0 : spec.getSortNum()
            ));
        skus.forEach(sku -> sku.getSpecValues().sort(
            Comparator.comparingInt(value ->
                specOrderMap.getOrDefault(value.getSpecId(), Integer.MAX_VALUE)
            )
        ));

        product.setSpecs(specVos);
        product.setSkus(skus);
    }

    private void fillCategoryNames(List<? extends PmsProductVo> products) {
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

        wrapper.select(
            PmsProduct::getProductId,
            PmsProduct::getProductCode,
            PmsProduct::getProductName,
            PmsProduct::getCategoryId,
            PmsProduct::getSellingPoint,
            PmsProduct::getMainImageId,
            PmsProduct::getUnit,
            PmsProduct::getMinPrice,
            PmsProduct::getMaxPrice,
            PmsProduct::getTotalStock,
            PmsProduct::getStatus,
            PmsProduct::getSortNum,
            PmsProduct::getVersion,
            PmsProduct::getRemark,
            PmsProduct::getCreateTime,
            PmsProduct::getUpdateTime
        );
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(PmsProductCreateBo bo) {
//      1、校验规格、价格、库存和 SKU 组合
        validateSaveData(bo.getSpecs(),bo.getSkus());
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
    private void validateSaveData(List<PmsProductSpecSaveBo> specBos, List<PmsProductSkuSaveBo> skusBos) {
        List<PmsProductSpecSaveBo> specs =
            specBos == null ? Collections.emptyList() : specBos;
        List<PmsProductSkuSaveBo> skus =
           skusBos == null ? Collections.emptyList() : skusBos;

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


//  保存商品规格和规格值
    private Map<String, PmsProductSpecValue>  saveSpecsAndValues(Long productId, List<PmsProductSpecSaveBo> specBos) {
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
            sku.setSpecKey(buildSpecKey(skuBo.getSpecValueKeys(),savedValueMap));
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

            PmsProductSpecValue specValue = savedValueMap.get(normalizedKey);
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

//   根据前端临时规格值 key 生成数据库规格组合键
    private String buildSpecKey(List<String> specValueKeys, Map<String, PmsProductSpecValue> savedValueMap) {
        if (specValueKeys == null || specValueKeys.isEmpty()) {
            return DEFAULT_SPEC_KEY;
        }

        List<Long> specValueIds = new ArrayList<>();
        for (String valueKey: specValueKeys) {
            String normalizedKey = normalizeText(valueKey);

            PmsProductSpecValue specValue = savedValueMap.get(normalizedKey);
            if (specValue == null){
                throw new ServiceException("生成 SKU 组合键失败，规格值不存在"+valueKey);
            }

            specValueIds.add(specValue.getSpecValueId());
        }

        return specValueIds.stream().distinct().sorted().map(String::valueOf).collect(Collectors.joining("_"));
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(PmsProductUpdateBo bo) {
//      1、查询原商品，校验商品状态和版本号
        PmsProduct originalProduct = getProductForUpdate(bo);

//      2、校验修改后的规格和 SKu 数据
        validateSaveData(bo.getSpecs(),bo.getSkus());

//      3、校验商品分类
        validateProductCategory(bo.getCategoryId());
//      4、查询商品原有的规格、规格值和 SKU
        ProductUpdateContext context = loadProductUpdateContext(bo.getProductId());

        validateUpdateIds(bo,context);
//      5、构造商品主表修改数据
        PmsProduct updateProduct = buildProductForUpdate(bo);
//      6、乐观锁修改商品主表
        if (baseMapper.updateById(updateProduct)<=0){
            throw new ServiceException("商品信息已被其他用户修改，请刷新后重新");
        }
//      7、新增或修改规格和规格值
        Map<String, PmsProductSpecValue> savedValueMap =
            updateSpecsAndValues(
                bo.getProductId(),
                bo.getSpecs()
            );

//      8、新增或修改SKU，并重建规格关系
        Set<Long> retainedSkuIds =
            updateSkusAndRelations(
                bo.getProductId(),
                bo.getSkus(),
                savedValueMap
            );
//       9、逻辑删除未提交的旧数据
         deleteRemovedProductData(
            bo,
            context,
            retainedSkuIds
        );
        return true;
    }

    private PmsProduct getProductForUpdate(PmsProductUpdateBo bo) {
        PmsProduct product = baseMapper.selectById(bo.getProductId());

        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        if (!Objects.equals(product.getVersion(),bo.getVersion())) {
            throw new ServiceException("商品信息已被其他用户修改，请刷新后重新");
        }

        if (PRODUCT_STATUS_ON_SALE.equals(product.getStatus())) {
            throw new ServiceException("已上架商品不能修改规格和 SKU，请先下架商品");
        }

        return product;
    }
    /**
     * 构造商品主表修改数据
     */
    private PmsProduct buildProductForUpdate( PmsProductUpdateBo bo) {
        PmsProduct product = new PmsProduct();

        // 更新条件
        product.setProductId(bo.getProductId());
        product.setVersion(bo.getVersion());

        // 允许修改的基础信息
        product.setProductName(
            StringUtils.trim(bo.getProductName())
        );
        product.setCategoryId(bo.getCategoryId());
        product.setSellingPoint(
            StringUtils.trim(bo.getSellingPoint())
        );
        product.setDetailContent(bo.getDetailContent());
        product.setMainImageId(bo.getMainImageId());

        String unit = StringUtils.trim(bo.getUnit());
        product.setUnit(
            StringUtils.isBlank(unit)
                ? DEFAULT_UNIT
                : unit
        );

        product.setSortNum(
            bo.getSortNum() == null
                ? DEFAULT_SORT_NUM
                : bo.getSortNum()
        );

        product.setRemark(
            StringUtils.trim(bo.getRemark())
        );

        // 根据本次提交的SKU重新计算价格和库存汇总
        fillProductSkuSummary(
            product,
            bo.getSkus()
        );

        return product;
    }
//   校验修改请求中的规格、规格值和SKU ID
    private void validateUpdateIds( PmsProductUpdateBo bo,ProductUpdateContext updateContext ) {
        List<PmsProductSpecSaveBo> specs = bo.getSpecs() == null ?Collections.emptyList() : bo.getSpecs();
        List<PmsProductSkuSaveBo> skus = bo.getSkus() == null ?Collections.emptyList() : bo.getSkus();

        Set<Long> submittedSpecIds = new HashSet<>();
        Set<Long> submittedSpecValueIds = new HashSet<>();
        Set<Long> submittedSkuIds = new HashSet<>();

        for (PmsProductSpecSaveBo specBo : specs) {
            Long specId = specBo.getSpecId();

//          specID有值，表示修改已有规格
            if (specId != null){
                if (!updateContext.specMap().containsKey(specId)){
                    throw new ServiceException("商品规格不存在或不属于当前商品"+specId);
                }

                if (!submittedSpecIds.add(specId)){
                    throw new ServiceException("商品规格不能重复提交"+specId);
                }
            }

            for (PmsProductSpecValueSaveBo valueSaveBo : specBo.getValues()) {
                Long specValueId = valueSaveBo.getSpecValueId();
//              没有 Id 表示本次新增的规格值
                if (specValueId == null) {
                    continue;
                }
                PmsProductSpecValue originalValue = updateContext.specValueMap().get(specValueId);

                if (originalValue == null){
                    throw new ServiceException("商品规格值不存在或不属于当前商品:"+specValueId);
                }

//              已有规格值不能被移动到另一个规格下面
                if (!Objects.equals(originalValue.getSpecId(), specId)) {
                    throw new ServiceException("规格值不属于当前规格:"+specValueId);
                }

                if (!submittedSpecValueIds.add(specValueId)) {
                    throw new ServiceException("商品规格值不能重复提交："+ specValueId);
                }
            }
        }

        for (PmsProductSkuSaveBo skuBo : skus) {
            Long skuId = skuBo.getSkuId();
//          没有 ID 表示本次新增的 SKU
            if (skuId == null) {
                continue;
            }

            PmsProductSku originalSku = updateContext.skuMap().get(skuId);

            if (originalSku == null) {
                throw new ServiceException("商品 SKU 不存在或不属于当前商品："+skuId);
            }

            if (!submittedSkuIds.add(skuId)) {
                throw new ServiceException("商品 SKU 不能重复提交"+skuId);
            }

            if (skuBo.getVersion() == null) {
                throw new ServiceException("已有 SKU 的版本号不能为空:"+skuId);
            }

            if (!Objects.equals(originalSku.getVersion(), skuBo.getVersion())) {
                throw new ServiceException(
                    "SKU信息已被其他用户修改，请刷新后重试："
                        + skuId
                );
            }
        }
    }
    /**
     * 商品修改时数据库中的原始数据
     * @param specMap
     * @param specValueMap
     * @param skuMap
     */
    private record ProductUpdateContext(
        Map<Long,PmsProductSpec> specMap,
        Map<Long,PmsProductSpecValue> specValueMap,
        Map<Long,PmsProductSku> skuMap){}

    /**
     * 查询商品修改前的规格，规格值和 SKU
     * @param productId 商品 iD
     * @return ProductUpdateContext
     */
     private ProductUpdateContext loadProductUpdateContext(Long productId) {
        List<PmsProductSpec> specs = specMapper.selectList(new LambdaQueryWrapper<PmsProductSpec>()
            .eq(PmsProductSpec::getProductId,productId)
        );

        List<PmsProductSpecValue> specValues = specValueMapper.selectList(new LambdaQueryWrapper<PmsProductSpecValue>()
            .eq(PmsProductSpecValue::getProductId,productId)
        );
        List<PmsProductSku> skus = skuMapper.selectList(new LambdaQueryWrapper<PmsProductSku>()
            .eq(PmsProductSku::getProductId,productId)
        );
        Map<Long,PmsProductSpec> specMap = specs.stream().collect(Collectors.toMap(
            PmsProductSpec::getSpecId,spec -> spec,
            (first,second) -> first,LinkedHashMap::new
        ));
        Map<Long,PmsProductSpecValue> specValueMap = specValues.stream().collect(Collectors.toMap(
            PmsProductSpecValue::getSpecValueId,specValue -> specValue,
            (first,second) -> first,LinkedHashMap::new
        ));
        Map<Long,PmsProductSku> skuMap = skus.stream().collect(Collectors.toMap(
            PmsProductSku::getSkuId,sku -> sku,
            (first,second) -> first,LinkedHashMap::new
        ));

        return new ProductUpdateContext(specMap,specValueMap,skuMap);
     }

    /**
     *  新增或修改商品规格和规格值
     */
    private Map<String, PmsProductSpecValue> updateSpecsAndValues(Long productId, List<PmsProductSpecSaveBo> specBos) {
         Map<String,PmsProductSpecValue> savedValueMap = new LinkedHashMap<>();

         if (specBos == null || specBos.isEmpty()) {
             return savedValueMap;
         }

         for (int specIndex = 0; specIndex < specBos.size(); specIndex++) {
             PmsProductSpecSaveBo specBo = specBos.get(specIndex);

             PmsProductSpec spec = new PmsProductSpec();

             boolean newSpec = specBo.getSpecId() == null;

             Long specId = newSpec? IdWorker.getId() : specBo.getSpecId();

             spec.setSpecId(specId);
             spec.setProductId(productId);
             spec.setSpecName(StringUtils.trim(specBo.getSpecName()));
             spec.setSortNum(specBo.getSortNum() == null ? specIndex : specBo.getSortNum());

             int affectedRows;

             if (newSpec) {
                 affectedRows = specMapper.insert(spec);
             } else {
                 affectedRows = specMapper.updateById(spec);
             }

             if (affectedRows<=0){
                 throw new ServiceException("商品规格保存失败:"+specBo.getSpecName());
             }

             updateSpecValues(productId,spec,specBo.getValues(),savedValueMap);
         }

         return savedValueMap;
    }
//  新增或修改一个规格下的规格值
    private void updateSpecValues(Long productId, PmsProductSpec spec,  List<PmsProductSpecValueSaveBo> valueBos, Map<String, PmsProductSpecValue> savedValueMap) {
          for (int valueIndex = 0; valueIndex < valueBos.size(); valueIndex++) {
              PmsProductSpecValueSaveBo valueBo = valueBos.get(valueIndex);

              PmsProductSpecValue specValue = new PmsProductSpecValue();

              boolean newValue = valueBo.getSpecValueId() == null;

              Long specValueId = newValue? IdWorker.getId() : valueBo.getSpecValueId();

              specValue.setSpecValueId(specValueId);
              specValue.setProductId(productId);
              specValue.setSpecId(spec.getSpecId());
              specValue.setSpecValue(StringUtils.trim(valueBo.getSpecValue()));
              specValue.setSortNum(valueBo.getSortNum() == null ?valueIndex : valueBo.getSortNum());

              int affectedRows;

              if (newValue){
                  affectedRows = specValueMapper.insert(specValue);
              } else {
                  affectedRows = specValueMapper.updateById(specValue);
              }

              if (affectedRows<=0){
                  throw new ServiceException("商品规格值保存失败："+ valueBo.getSpecValue());
              }

              String normalizedKey = normalizeText(valueBo.getValueKey());

              savedValueMap.put(normalizedKey,specValue);
          }
    }

    private Set<Long> updateSkusAndRelations(Long productId, List<PmsProductSkuSaveBo> skusBos,Map<String,PmsProductSpecValue> savedValueMap) {
        Set<Long> relationSkuIds = new HashSet<>();

//        关系表的数据没有独立业务价值，修改商品时删除当前商品的旧关系，后面重新插入
        skuSpecValueMapper.delete(new LambdaQueryWrapper<PmsSkuSpecValue>()
            .eq(PmsSkuSpecValue::getProductId,productId)
        );

        boolean defaultSku = savedValueMap.isEmpty();

        for (int skuIndex = 0; skuIndex < skusBos.size(); skuIndex++) {
            PmsProductSkuSaveBo skuBo = skusBos.get(skuIndex);
            boolean newSku = skuBo.getSkuId() == null;

            Long skuId = newSku ? IdWorker.getId() : skuBo.getSkuId();

            PmsProductSku sku = new PmsProductSku();

            sku.setProductId(productId);
            sku.setSkuId(skuId);
            sku.setSkuType(defaultSku ? SKU_TYPE_DEFAULT : SKU_TYPE_CUSTOM);
            sku.setSpecKey(buildSpecKey(skuBo.getSpecValueKeys(), savedValueMap));
            sku.setSkuName(defaultSku ? DEFAULT_SKU_NAME : buildSKuName(skuBo.getSpecValueKeys(), savedValueMap));
            sku.setSkuImageId(skuBo.getSkuImageId());
            sku.setSalePrice(skuBo.getSalePrice());
            sku.setMarketPrice(skuBo.getMarketPrice());
            sku.setStock(skuBo.getStock());
            sku.setWarningStock(skuBo.getWarningStock() == null ? 0 : skuBo.getWarningStock());
            sku.setSortNum(skuBo.getSortNum() == null ? skuIndex : skuBo.getSortNum());

            int affectedRows;
            if (newSku) {
                sku.setSkuCode(generateBusinessCode("SKU", skuId));
                sku.setStatus(SKU_STATUS_NORMAL);
                affectedRows = skuMapper.insert(sku);
            } else {
                sku.setVersion(skuBo.getVersion());
                affectedRows = skuMapper.updateById(sku);
            }

            if (affectedRows <= 0) {
                throw new ServiceException(newSku ? "商品 SKU 更新失败：" + sku.getSkuName() :
                    "SKU信息已被其他用户修改，请刷新重试：" + skuId
                );
            }

            relationSkuIds.add(sku.getSkuId());

            if (!defaultSku) {
                saveSkuRelations(productId, skuId, skuBo.getSpecValueKeys(), savedValueMap);
            }
        }
         return relationSkuIds;
    }

//  逻辑删除本次请求中已经移除的规格、规格值和 SKU
    private void deleteRemovedProductData(PmsProductUpdateBo bo ,ProductUpdateContext context, Set<Long> relationSkuIds) {
        List <PmsProductSpecSaveBo> specs = bo.getSpecs() == null ? Collections.emptyList() : bo.getSpecs();

//      本次请求保留的已有规格 ID
        Set<Long> retainedSpecIds = specs.stream()
            .map(PmsProductSpecSaveBo::getSpecId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

//      本次请求保留的已有规格值 ID
        Set<Long> retainedSpecValueIds = specs.stream().flatMap(spec->spec.getValues().stream())
            .map(PmsProductSpecValueSaveBo::getSpecValueId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

//      原 SKU 减去本次保留 SKU
        Set<Long> removedSkuIds = new HashSet<>(context.skuMap().keySet());
        removedSkuIds.removeAll(relationSkuIds);

//      原规格值减去本次保留规格值
        Set<Long> removedSpecValueIds = new HashSet<>(context.specValueMap().keySet());
        removedSpecValueIds.removeAll(retainedSpecValueIds);

//      原规格减去本次保留规格
        Set<Long> removedSpecIds = new HashSet<>(context.specMap().keySet());
        removedSpecIds.removeAll(retainedSpecIds);

        /*
         * 按照从子数据到父数据的顺序删除：
         * SKU 关系表 -> SKU -> 规格值 -> 规格
         */

        if (!removedSkuIds.isEmpty()){
            int rows = skuMapper.deleteByIds(removedSkuIds);

            if (rows != removedSkuIds.size()) {
                throw new ServiceException("部分 SKU 删除失败，请刷新后重试");
            }
        }

        if (!removedSpecValueIds.isEmpty()){
            int rows = specValueMapper.deleteByIds(removedSpecValueIds);

            if (rows != removedSpecValueIds.size()) {
                throw new ServiceException("部分规格值删除失败，请刷新后重试");
            }
        }
        if (!removedSpecIds.isEmpty()){
            int rows = specMapper.deleteByIds(removedSpecIds);

            if (rows != removedSpecIds.size()) {
                throw new ServiceException("部分商品规格删除失败，请刷新后重试");
            }
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidById(Long productId) {
//        1、查询并校验商品
        PmsProduct product = baseMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        if (PRODUCT_STATUS_ON_SALE.equals(product.getStatus())) {
            throw new ServiceException("商品已上架，不能删除");
        }
//        将来增加订单模块后，还需要校验 商品是否存在未完成订单等业务限制
//        2、物理删除 SKU 与规格值关系
        skuSpecValueMapper.delete(new LambdaQueryWrapper<PmsSkuSpecValue>()
            .eq(PmsSkuSpecValue::getProductId,productId)
        );
//        3、逻辑删除 SKU
        skuMapper.delete(new LambdaQueryWrapper<PmsProductSku>()
            .eq(PmsProductSku::getProductId,productId)
        );
//        4、逻辑删除规格值
        specValueMapper.delete(new LambdaQueryWrapper<PmsProductSpecValue>()
            .eq(PmsProductSpecValue::getProductId,productId)
        );
//        5、逻辑删除规格
        specMapper.delete(new LambdaQueryWrapper<PmsProductSpec>()
            .eq(PmsProductSpec::getProductId,productId)
        );
//        6、逻辑删除商品主表
        if (baseMapper.deleteById(productId)<=0){
            throw new ServiceException("商品删除失败");
        }

        return true;
    }



    /*
     * 商品状态：已下架
     */
    private static final String PRODUCT_STATUS_OFF_SALE = "2";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeStatus(Long productId, PmsProductStatusBo bo) {
        PmsProduct product = getProductForStatusChange(productId,bo);
//      1、如果商品当前状态和目标状态一致，则直接返回成功
        if (Objects.equals(product.getStatus(),bo.getStatus())) {
            return true;
        }
//      2、只有上架需要检查商品完整性和可售条件
        if (PRODUCT_STATUS_ON_SALE.equals(bo.getStatus())) {
            validateProductForPublish(product);
        }

//      3、使用数据库中完整查询出的实体
        product.setStatus(bo.getStatus());

//      4、乐观锁更新商品状态
        if (baseMapper.updateById(product) <= 0) {
            throw new ServiceException("商品状态更新失败，请刷新后重试");
        }
        return true;
    }

    /**
     * 查询并校验待变更状态的商品
     */
    private PmsProduct getProductForStatusChange(
        Long productId,
        PmsProductStatusBo bo
    ) {
        PmsProduct product =
            baseMapper.selectById(productId);

        if (product == null) {
            throw new ServiceException("商品不存在");
        }

        if (!Objects.equals(
            product.getVersion(),
            bo.getVersion()
        )) {
            throw new ServiceException(
                "商品信息已被其他用户修改，请刷新后重试"
            );
        }

        String targetStatus = bo.getStatus();

        if (!PRODUCT_STATUS_ON_SALE.equals(targetStatus)
            && !PRODUCT_STATUS_OFF_SALE.equals(targetStatus)) {
            throw new ServiceException(
                "商品状态只能是上架或下架"
            );
        }

        // 草稿不能直接变成下架状态
        if (PRODUCT_STATUS_DRAFT.equals(product.getStatus())
            && PRODUCT_STATUS_OFF_SALE.equals(targetStatus)) {
            throw new ServiceException(
                "草稿商品尚未上架，无需下架"
            );
        }

        return product;
    }

    private void validateProductForPublish(PmsProduct product) {
        if (StringUtils.isBlank(product.getProductName())) {
            throw new ServiceException("商品名称不能为空");
        }
        if (product.getMainImageId() == null){
            throw new ServiceException("请先上传商品主图");
        }

//      复用已有分类校验
        validateProductCategory(product.getCategoryId());

//      只查询启动的 SKu
        List<PmsProductSku> enabledSkus = skuMapper.selectList(
            new LambdaQueryWrapper<PmsProductSku>()
                .eq(PmsProductSku::getProductId, product.getProductId())
                .eq(PmsProductSku::getStatus, SKU_STATUS_NORMAL)
        );
        if (enabledSkus.isEmpty()) {
            throw new ServiceException("商品至少需要一个启用的 SKU");
        }

        boolean hasStock = false;

        for (PmsProductSku sku : enabledSkus) {
            if (sku.getSalePrice() == null || sku.getSalePrice().signum() <= 0) {
                throw new ServiceException("SKU价格必须大于0：" + sku.getSkuName());
            }

            if (sku.getStock() == null || sku.getStock() < 0) {
                throw new ServiceException("SKU库存不能为负数：" + sku.getSkuName());
            }

            if (StringUtils.isBlank(sku.getSpecKey())) {
                throw new ServiceException("SKU规格组合键不能为空：" + sku.getSkuName());
            }

            if (sku.getStock() > 0) {
                hasStock = true;
            }
        }
        if (!hasStock) {
            throw new ServiceException("商品至少需要一个有库存的 SKU");
        }
    }
}
