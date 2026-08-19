package org.dromara.product.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 商品规格名称实体 pms_product_spec
 * 例如：颜色、容量、尺码
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_product_spec")
public class PmsProductSpec extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规格ID
     */
    @TableId("spec_id")
    private Long specId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 规格名称
     * 例如：颜色、容量
     */
    private String specName;

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
