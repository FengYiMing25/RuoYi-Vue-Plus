package org.dromara.product.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.product.domain.bo.AppProductQueryBo;
import org.dromara.product.domain.vo.AppProductListVo;
import org.dromara.product.service.IAppProductService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户端商品接口
 */
@SaIgnore // 忽略鉴权 不用登录
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/product")
public class AppProductController {

    private final IAppProductService iAppProductService;

    /**
     * 分页查询客户端可见商品
     */
    @GetMapping
    public TableDataInfo<AppProductListVo> list(
        @Validated(QueryGroup.class)AppProductQueryBo bo,
        PageQuery pageQuery
        ) {
        return iAppProductService.queryPageList(bo, pageQuery);
    }
}
