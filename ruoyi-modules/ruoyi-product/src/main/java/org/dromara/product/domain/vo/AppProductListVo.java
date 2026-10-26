package org.dromara.product.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.product.domain.PmsProduct;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 客户端商品列表视图对象
 */
@Data
@AutoMapper(target = PmsProduct.class)
public class AppProductListVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品卖点
     */
    private String sellingPoint;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 商品主图OSS ID
     *
     * 只用于生成图片URL，不直接返回客户端。
     */
    @JsonIgnore
    private Long mainImageId;

    /**
     * 商品主图访问地址
     */
    @Translation(
        type = TransConstant.OSS_ID_TO_URL,
        mapper = "mainImageId"
    )
    private String mainImageUrl;

    /**
     * 最低销售价格
     */
    private BigDecimal minPrice;

    /**
     * 最高销售价格
     */
    private BigDecimal maxPrice;

    /**
     * SKU总库存
     */
    private Integer totalStock;

    /**
     * 商品单位
     */
    private String unit;

    /**
     * 当前是否有库存
     */
    private Boolean available;
}
