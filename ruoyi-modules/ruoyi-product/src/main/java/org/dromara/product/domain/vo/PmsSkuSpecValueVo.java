package org.dromara.product.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * SKU已选择的规格值
 *
 * 例如：颜色=黑色、容量=256G
 */
@Data
public class PmsSkuSpecValueVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规格ID
     */
    private Long specId;

    /**
     * 规格名称，例如颜色
     */
    private String specName;

    /**
     * 规格值ID
     */
    private Long specValueId;

    /**
     * 规格值，例如黑色
     */
    private String specValue;
}
