package org.dromara.product.domain.bo;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品SKU保存参数
 */
@Data
public class PmsProductSkuSaveBo {
    /**
     * SKU ID
     *
     * 修改已有SKU时传值；
     * 新增SKU时不传。
     */
    private Long skuId;

    /**
     * SKU乐观锁版本号
     *
     * 修改已有SKU时必须传；
     * 新增SKU时不传。
     */
    private Integer version;
    /**
     * SKU图片OSS ID
     */
    private Long skuImageId;

    /**
     * 销售价格
     */
    @NotNull(
        message = "SKU销售价格不能为空",
        groups = {AddGroup.class, EditGroup.class}
    )
    @DecimalMin(
        value = "0.00",
        message = "SKU销售价格不能小于0",
        groups = {AddGroup.class, EditGroup.class}
    )
    @Digits(
        integer = 8,
        fraction = 2,
        message = "SKU销售价格最多支持8位整数和2位小数",
        groups = {AddGroup.class, EditGroup.class}
    )
    private BigDecimal salePrice;

    /**
     * 市场划线价
     */
    @DecimalMin(
        value = "0.00",
        message = "SKU市场价不能小于0",
        groups = {AddGroup.class, EditGroup.class}
    )
    @Digits(
        integer = 8,
        fraction = 2,
        message = "SKU市场价最多支持8位整数和2位小数",
        groups = {AddGroup.class, EditGroup.class}
    )
    private BigDecimal marketPrice;

    /**
     * 库存数量
     */
    @NotNull(
        message = "SKU库存不能为空",
        groups = {AddGroup.class, EditGroup.class}
    )
    @Min(
        value = 0,
        message = "SKU库存不能小于0",
        groups = {AddGroup.class, EditGroup.class}
    )
    private Integer stock;

    /**
     * 库存预警值，空时后端默认0
     */
    @Min(
        value = 0,
        message = "SKU库存预警值不能小于0",
        groups = {AddGroup.class, EditGroup.class}
    )
    private Integer warningStock;

    /**
     * 显示顺序，空时后端默认0
     */
    @Min(
        value = 0,
        message = "SKU排序不能小于0",
        groups = {AddGroup.class, EditGroup.class}
    )
    private Integer sortNum;

    /**
     * 当前SKU选择的规格值临时标识
     *
     * 单规格商品为空；
     * 多规格商品例如：[BLACK, 256G]
     */
    @Size(
        max = 3,
        message = "一条SKU最多选择{max}个规格值",
        groups = {AddGroup.class, EditGroup.class}
    )
    private List<String> specValueKeys =
        new ArrayList<>();
}
