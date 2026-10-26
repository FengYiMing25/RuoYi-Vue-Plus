package org.dromara.product.domain.bo;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.validate.QueryGroup;

/**
 * 客户端商品列表查询参数
 */
@Data
public class AppProductQueryBo {

    /**
     * 商品分类ID
     *
     * 后续Service会处理该分类及其子分类。
     */
    private Long categoryId;

    /**
     * 搜索关键词
     *
     * 第一版用于模糊查询商品名称。
     */
    @Size(
        max = 100,
        message = "搜索关键词不能超过{max}个字符",
        groups = QueryGroup.class
    )
    private String keyword;
}
