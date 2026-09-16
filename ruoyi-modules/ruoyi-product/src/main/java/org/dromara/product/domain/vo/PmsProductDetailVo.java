package org.dromara.product.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.product.domain.PmsProduct;

import java.util.ArrayList;
import java.util.List;


/**
 * 商品详情视图对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PmsProduct.class)
public class PmsProductDetailVo extends PmsProductVo {

    /**
     * 商品详情富文本HTML
     */
    private String detailContent;

    private List<PmsProductSpecVo> specs = new ArrayList<>();

    private List<PmsProductSkuVo> skus = new ArrayList<>();

}
