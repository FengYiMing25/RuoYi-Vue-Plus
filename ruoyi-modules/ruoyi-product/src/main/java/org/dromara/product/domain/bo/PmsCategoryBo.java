package org.dromara.product.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.product.domain.PmsCategory;

/**
 * 商品分类业务对象 pms_category
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PmsCategory.class, reverseConvertGenerate = false)
public class PmsCategoryBo extends BaseEntity {

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空", groups = EditGroup.class)
    private Long categoryId;

    /**
     * 分类编码
     */
    @NotBlank(
        message = "分类编码不能为空",
        groups = {AddGroup.class, EditGroup.class}
    )
    @Size(
        max = 64,
        message = "分类编码长度不能超过{max}个字符",
        groups = {AddGroup.class, EditGroup.class}
    )
    @Pattern(
        regexp = "^[A-Z][A-Z0-9_]*$",
        message = "分类编码只能使用大写字母、数字和下划线，并且必须以字母开头",
        groups = {AddGroup.class, EditGroup.class}
    )
    private String categoryCode;

    /**
     * 分类名称
     */
    @NotBlank(
        message = "分类名称不能为空",
        groups = {AddGroup.class, EditGroup.class}
    )
    @Size(
        max = 100,
        message = "分类名称长度不能超过{max}个字符",
        groups = {AddGroup.class, EditGroup.class}
    )
    private String categoryName;

    /**
     * 父分类ID，一级分类传0
     */
    @NotNull(
        message = "父分类ID不能为空，一级分类请传0",
        groups = {AddGroup.class, EditGroup.class}
    )
    private Long parentId;

    /**
     * 分类图片OSS ID
     */
    private Long imageId;

    /**
     * 显示顺序
     */
    @NotNull(
        message = "显示顺序不能为空",
        groups = {AddGroup.class, EditGroup.class}
    )
    @Min(
        value = 0,
        message = "显示顺序不能小于{value}",
        groups = {AddGroup.class, EditGroup.class}
    )
    private Integer sortNum;

    /**
     * 状态（0正常 1停用）
     *
     * 新增时为空，由后端设置为正常；
     * 修改时如有传值，只允许0或1。
     */
    @Pattern(
        regexp = "^[01]$",
        message = "分类状态只能是0或1",
        groups = {AddGroup.class, EditGroup.class}
    )
    private String status;

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
