package org.dromara.product.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.product.domain.PmsProduct;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PmsProduct.class, reverseConvertGenerate = false)
public class PmsProductCreateBo extends PmsProductBo {

    @Valid
    @Size(
        max = 3,
        message = "商品最多支持3个规格",
        groups = AddGroup.class
    )
    private List<PmsProductSpecSaveBo> specs = new ArrayList<>();

    @Valid
    @NotEmpty(
        message = "商品至少需要一个SKU",
        groups = AddGroup.class
    )
    @Size(
        max = 100,
        message = "商品最多支持100个SKU",
        groups = AddGroup.class
    )
    private List<PmsProductSkuSaveBo> skus = new ArrayList<>();
}
