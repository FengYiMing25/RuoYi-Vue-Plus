package org.dromara.product.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.product.domain.PmsProductSku;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 商品SKU视图对象
 */
@Data
@AutoMapper(target = PmsProductSku.class)
public class PmsProductSkuVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * SKU名称
     *
     * 例如：默认、黑色 / 256G
     */
    private String skuName;

    /**
     * SKU类型（0默认 1自定义）
     */
    private String skuType;

    /**
     * SKU图片OSS ID
     */
    private Long skuImageId;

    /**
     * SKU图片访问地址
     */
    @Translation(
        type = TransConstant.OSS_ID_TO_URL,
        mapper = "skuImageId"
    )
    private String skuImageUrl;

    /**
     * 销售价格
     */
    private BigDecimal salePrice;

    /**
     * 市场划线价
     */
    private BigDecimal marketPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 库存预警值
     */
    private Integer warningStock;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 显示顺序
     */
    private Integer sortNum;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 当前SKU选择的规格值
     */
    private List<PmsSkuSpecValueVo> specValues =
        new ArrayList<>();

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
