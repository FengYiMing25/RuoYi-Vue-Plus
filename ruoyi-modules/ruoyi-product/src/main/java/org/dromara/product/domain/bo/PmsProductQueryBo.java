package org.dromara.product.domain.bo;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.QueryGroup;

/**
 * 商品分页查询参数
 */
@Data
public class PmsProductQueryBo {

    /**
     * 商品ID，精确查询
     */
    private Long productId;

    /**
     * 商品编码，模糊查询
     */
    @Size(
        max = 64,
        message = "商品编码不能超过{max}个字符",
        groups = QueryGroup.class
    )
    private String productCode;

    /**
     * 商品名称，模糊查询
     */
    @Size(
        max = 200,
        message = "商品名称不能超过{max}个字符",
        groups = QueryGroup.class
    )
    private String productName;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品状态：0 草稿 1上架 2下架
     */
    @Pattern(
        regexp = "^[012]$",
        message = "商品状态只能是0、1或2",
        groups = QueryGroup.class
    )
    private String status;
}
