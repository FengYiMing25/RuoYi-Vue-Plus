package org.dromara.product.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 商品SKU实体 pms_product_sku
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_product_sku")
public class PmsProductSku extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    @TableId("sku_id")
    private Long skuId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * SKU名称
     */
    private String skuName;

    /**
     * SKU类型（0默认 1自定义）
     */
    private String skuType;

    /**
     * SKU图片OSS ID
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long skuImageId;

    /**
     * 销售价格
     */
    private BigDecimal salePrice;

    /**
     * 市场划线价
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private BigDecimal marketPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 库存预警值
     */
    private Integer warningStock;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 规格值组合键
     * 默认SKU：DEFAULT
     * 多规格SKU：3001_3003
     */
    private String specKey;
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
    private String remark;
}
