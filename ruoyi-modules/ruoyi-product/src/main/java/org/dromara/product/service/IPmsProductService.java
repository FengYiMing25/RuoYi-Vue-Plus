package org.dromara.product.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.product.domain.bo.PmsProductBo;
import org.dromara.product.domain.bo.PmsProductCreateBo;
import org.dromara.product.domain.bo.PmsProductQueryBo;
import org.dromara.product.domain.vo.PmsProductVo;

/**
 * 商品Service接口
 */
public interface IPmsProductService {

    /**
     * 根据商品ID查询商品
     *
     * @param productId 商品ID
     * @return 商品信息
     */
    PmsProductVo queryById(Long productId);

    /**
     * 分页查询商品列表
     *
     * @param bo 查询条件
     * @param pageQuery 分页参数
     * @return 商品分页数据
     */
    TableDataInfo<PmsProductVo> queryPageList(
        PmsProductQueryBo bo,
        PageQuery pageQuery
    );

    /**
     * 新增商品
     *
     * 同时自动创建默认SKU。
     *
     * @param bo 商品参数
     * @return 是否成功
     */
    Boolean insertByBo(PmsProductCreateBo bo);

    /**
     * 修改商品基础信息
     *
     * 不在该方法中处理上架、下架。
     *
     * @param bo 商品参数
     * @return 是否成功
     */
    Boolean updateByBo(PmsProductBo bo);

    /**
     * 校验并删除商品
     *
     * 同时逻辑删除商品相关SKU和规格数据。
     *
     * @param productId 商品ID
     * @return 是否成功
     */
    Boolean deleteWithValidById(Long productId);
}
