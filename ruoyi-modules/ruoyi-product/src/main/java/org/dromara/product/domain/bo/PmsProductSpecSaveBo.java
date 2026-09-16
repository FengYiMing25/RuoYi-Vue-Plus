package org.dromara.product.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品规格保存参数
 */
@Data
public class PmsProductSpecSaveBo {
    /**
     * 规格ID
     *
     * 修改已有规格时传值；
     * 新增规格时不传。
     */
    private Long specId;

    /**
     * 规格名称
     *
     * 例如：颜色、容量
     */
    @NotBlank(
        message = "规格名称不能为空",
        groups = { AddGroup.class, EditGroup.class}
    )
    @Size(
        max = 64,
        message = "规格名称不能超过{max}个字符",
        groups =  {AddGroup.class, EditGroup.class}
    )
    private String specName;

    /**
     * 显示顺序，空时后端默认0
     */
    @Min(
        value = 0,
        message = "规格排序不能小于0",
        groups =  {AddGroup.class, EditGroup.class}
    )
    private Integer sortNum;

    /**
     * 当前规格下的规格值
     */
    @Valid
    @NotEmpty(
        message = "每个规格至少需要一个规格值",
        groups =  {AddGroup.class, EditGroup.class}
    )
    @Size(
        max = 20,
        message = "每个规格最多支持{max}个规格值",
        groups =  {AddGroup.class, EditGroup.class}
    )
    private List<PmsProductSpecValueSaveBo> values =
        new ArrayList<>();
}
