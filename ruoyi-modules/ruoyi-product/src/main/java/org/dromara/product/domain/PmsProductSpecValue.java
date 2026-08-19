package org.dromara.product.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 商品规格值实体 pms_product_spec_value
 * 例如：黑色、白色、256G、512G
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_product_spec_value")
public class PmsProductSpecValue extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规格值ID
     */
    @TableId("spec_value_id")
    private Long specValueId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 规格ID
     */
    private Long specId;

    /**
     * 规格值
     * 例如：黑色、白色、256G
     */
    private String specValue;

    /**
     * 显示顺序
     */
    private Integer sortNum;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;
}
