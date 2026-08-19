package org.dromara.product.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.product.domain.PmsProduct;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品视图对象 pms_product
 */
@Data
@AutoMapper(target = PmsProduct.class)
public class PmsProductVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 系统生成的商品编码
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 所属分类名称
     *
     * 该字段不在商品表中，后面由Service查询分类后填充。
     */
    private String categoryName;

    /**
     * 商品卖点
     */
    private String sellingPoint;

    /**
     * 商品主图OSS ID
     */
    private Long mainImageId;

    /**
     * 商品主图访问地址
     */
    @Translation(
        type = TransConstant.OSS_ID_TO_URL,
        mapper = "mainImageId"
    )
    private String mainImageUrl;

    /**
     * 商品单位
     */
    private String unit;

    /**
     * SKU最低售价
     */
    private BigDecimal minPrice;

    /**
     * SKU最高售价
     */
    private BigDecimal maxPrice;

    /**
     * SKU总库存
     */
    private Integer totalStock;

    /**
     * 商品状态（0 草稿 1上架 2下架）
     */
    private String status;

    /**
     * 显示顺序
     */
    private Integer sortNum;

    /**
     * 乐观锁版本号
     *
     * 前端修改商品时，需要将该版本号原样传回。
     */
    private Integer version;

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
}
