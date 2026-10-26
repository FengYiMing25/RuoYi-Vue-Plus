package org.dromara.product.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.product.domain.bo.AppProductQueryBo;
import org.dromara.product.domain.vo.AppProductListVo;

/**
 * 客户端商品查询Service
 */
public interface IAppProductService {

    /**
     * 分页查询客户端可见商品
     *
     * @param bo 查询条件
     * @param pageQuery 分页参数
     * @return 商品分页数据
     */
    TableDataInfo<AppProductListVo> queryPageList(
        AppProductQueryBo bo,
        PageQuery pageQuery
    );
}
