package org.dromara.product.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * SKU与规格值关联实体 pms_sku_spec_value
 */
@Data
@TableName("pms_sku_spec_value")
public class PmsSkuSpecValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    @TableId("relation_id")
    private Long relationId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 规格ID
     */
    private Long specId;

    /**
     * 规格值ID
     */
    private Long specValueId;
}
