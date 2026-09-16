package org.dromara.product.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 商品状态修改参数
 */
@Data
public class PmsProductStatusBo {

    /**
     * 目标状态：
     * 1 上架
     * 2 下架
     */
    @NotBlank(message = "商品状态不能为空")
    @Pattern(
        regexp = "^[12]$",
        message = "商品状态只能是1上架或2下架"
    )
    private String status;

    /**
     * 商品乐观锁版本号
     */
    @NotNull(message = "商品版本号不能为空")
    private Integer version;
}
