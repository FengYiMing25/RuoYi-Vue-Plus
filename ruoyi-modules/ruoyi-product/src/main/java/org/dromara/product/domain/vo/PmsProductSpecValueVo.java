package org.dromara.product.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.product.domain.PmsProductSpecValue;

import java.io.Serial;
import java.io.Serializable;

/** 商品规格值视图对象 */
@Data
@AutoMapper(target = PmsProductSpecValue.class)
public class PmsProductSpecValueVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long specValueId;
    private Long specId;
    private String specValue;
    private Integer sortNum;
}
