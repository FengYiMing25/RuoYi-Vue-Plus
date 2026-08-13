package org.dromara.product.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.product.domain.PmsCategory;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 商品分类视图对象 pms_category
 */
@Data
@AutoMapper(target = PmsCategory.class)
public class PmsCategoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 分类层级
     */
    private Integer categoryLevel;

    /**
     * 分类图片OSS ID
     */
    private Long imageId;

    /**
     * 分类图片访问地址
     */
    @Translation(
        type = TransConstant.OSS_ID_TO_URL,
        mapper = "imageId"
    )
    private String imageUrl;

    /**
     * 显示顺序
     */
    private Integer sortNum;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 子分类
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PmsCategoryVo> children = new ArrayList<>();

}
