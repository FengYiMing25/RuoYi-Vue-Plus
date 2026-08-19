package org.dromara.product.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.product.domain.PmsProduct;


/**
 * 商品业务对象
 */
@Data
@AutoMapper(
    target = PmsProduct.class,
    reverseConvertGenerate = false
)
public class PmsProductBo {

    /**
     * 商品ID
     */
    @NotNull(
        message = "商品ID不能为空",
        groups = EditGroup.class
    )
    private Long productId;

    /**
     * 商品编码
     *
     * 新增时由后端生成，查询时可以作为筛选条件。
     */
    @Size(
        max = 64,
        message = "商品编码长度不能超过{max}个字符"
    )
    private String productCode;

    /**
     * 商品名称
     */
    @NotBlank(
        message = "商品名称不能为空",
        groups = {AddGroup.class, EditGroup.class}
    )
    @Size(
        max = 200,
        message = "商品名称长度不能超过{max}个字符",
        groups = {AddGroup.class, EditGroup.class}
    )
    private String productName;

    /**
     * 所属分类ID
     */
    @NotNull(
        message = "商品分类不能为空",
        groups = {AddGroup.class, EditGroup.class}
    )
    private Long categoryId;

    /**
     * 商品卖点
     */
    @Size(
        max = 255,
        message = "商品卖点长度不能超过{max}个字符",
        groups = {AddGroup.class, EditGroup.class}
    )
    private String sellingPoint;

    /**
     * 商品主图OSS ID
     */
    private Long mainImageId;

    /**
     * 商品单位
     *
     * 新增为空时，后端默认设置为“件”。
     */
    @Size(
        max = 32,
        message = "商品单位长度不能超过{max}个字符",
        groups = {AddGroup.class, EditGroup.class}
    )
    private String unit;

    /**
     * 商品状态（0 草稿 1上架 2下架）
     */
    @Pattern(
        regexp = "^[012]$",
        message = "商品状态只能是0、1或2",
        groups = {AddGroup.class, EditGroup.class}
    )
    private String status;

    /**
     * 显示顺序
     *
     * 新增为空时，后端默认设置为0。
     */
    @Min(
        value = 0,
        message = "显示顺序不能小于0",
        groups = {AddGroup.class, EditGroup.class}
    )
    private Integer sortNum;

    /**
     * 乐观锁版本号
     */
    @NotNull(
        message = "版本号不能为空",
        groups = EditGroup.class
    )
    private Integer version;



    /**
     * 备注
     */
    @Size(
        max = 500,
        message = "备注长度不能超过{max}个字符",
        groups = {AddGroup.class, EditGroup.class}
    )
    private String remark;
}
