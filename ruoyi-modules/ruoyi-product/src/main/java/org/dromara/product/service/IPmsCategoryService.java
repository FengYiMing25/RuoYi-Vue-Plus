package org.dromara.product.service;

import org.dromara.product.domain.bo.PmsCategoryBo;
import org.dromara.product.domain.vo.PmsCategoryVo;

import java.util.List;

public interface IPmsCategoryService {

    /**
     *  根据 ID 查询分类
     * @param categoryId 分类 ID
     * @return 商品分类
     */
    PmsCategoryVo queryById(Long categoryId);

    /**
     *  查询平铺分类列表
     * @param bo
     * @return 分类列表
     */
    List<PmsCategoryVo> queryList(PmsCategoryBo bo);

    /**
     *  查询树形分类列表
     * @param bo
     * @return 分类列表
     */
    List<PmsCategoryVo> queryTreeList(PmsCategoryBo bo);


    /**
     * 新增分类
     * @param bo 分类参数
     * @return 是否成功
     */
    Boolean insertByBo(PmsCategoryBo bo);

    /**
     * 修改分类
     * @param bo 分类参数
     * @return 是否成功
     */
    Boolean updateByBo(PmsCategoryBo bo);

    /**
     * 删除分类
     * @param categoryId 分类 id
     * @return 是否成功
     */
    Boolean deleteWithValidById(Long categoryId);
}
