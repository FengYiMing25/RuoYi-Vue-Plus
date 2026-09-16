package org.dromara.product.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 商品实体 pms_product
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_product")
public class PmsProduct extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId("product_id")
    private Long productId;

    /**
     * 商品编码
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 商品卖点
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String sellingPoint;

    /**
     * 商品详情富文本HTML
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String detailContent;
    /**
     * 商品主图OSS ID
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long mainImageId;

    /**
     * 商品单位
     */
    private String unit;

    /**
     * SKU最低售价（汇总）
     */
    private BigDecimal minPrice;

    /**
     * SKU最高售价（汇总）
     */
    private BigDecimal maxPrice;

    /**
     * SKU总库存（汇总）
     */
    private Integer totalStock;

    /**
     * 商品状态（0 草稿 1上架 2下架）
     */
    private String status;

    /**
     * 显示顺序
     */
    private Integer sortNum;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 备注
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String remark;
}
