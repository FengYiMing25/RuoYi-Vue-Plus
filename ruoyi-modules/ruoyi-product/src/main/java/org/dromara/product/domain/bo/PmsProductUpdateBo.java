package org.dromara.product.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.product.domain.PmsProduct;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品修改参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(
    target = PmsProduct.class,
    reverseConvertGenerate = false
)
public class PmsProductUpdateBo extends PmsProductBo {

    /**
     * 修改后的全部商品规格
     *
     * 普通商品为空；
     * 多规格商品最多支持3个规格。
     */
    @Valid
    @Size(
        max = 3,
        message = "商品最多支持3个规格",
        groups = EditGroup.class
    )
    private List<PmsProductSpecSaveBo> specs =
        new ArrayList<>();

    /**
     * 修改后的全部SKU
     */
    @Valid
    @NotEmpty(
        message = "商品至少需要一个SKU",
        groups = EditGroup.class
    )
    @Size(
        max = 100,
        message = "商品最多支持100个SKU",
        groups = EditGroup.class
    )
    private List<PmsProductSkuSaveBo> skus =
        new ArrayList<>();
}
