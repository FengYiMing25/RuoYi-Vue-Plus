package org.dromara.product.domain.bo;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;

/**
 * 商品规格值保存参数
 */
@Data
public class PmsProductSpecValueSaveBo {

    /**
     * 本次请求中的临时唯一标识
     *
     * 例如：BLACK、WHITE、SIZE_M
     */
    @NotBlank(
        message = "规格值临时标识不能为空",
        groups = AddGroup.class
    )
    @Size(
        max = 64,
        message = "规格值临时标识不能超过{max}个字符",
        groups = AddGroup.class
    )
    private String valueKey;

    /**
     * 规格值
     *
     * 例如：黑色、白色、256G
     */
    @NotBlank(
        message = "规格值不能为空",
        groups = AddGroup.class
    )
    @Size(
        max = 100,
        message = "规格值不能超过{max}个字符",
        groups = AddGroup.class
    )
    private String specValue;

    /**
     * 显示顺序，空时后端默认0
     */
    @Min(
        value = 0,
        message = "规格值排序不能小于0",
        groups = AddGroup.class
    )
    private Integer sortNum;
}
