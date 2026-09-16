package org.dromara.product.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.product.domain.PmsProductSpec;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 商品规格视图对象 */
@Data
@AutoMapper(target = PmsProductSpec.class)
public class PmsProductSpecVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long specId;
    private String specName;
    private Integer sortNum;
    private List<PmsProductSpecValueVo> values = new ArrayList<>();
}
