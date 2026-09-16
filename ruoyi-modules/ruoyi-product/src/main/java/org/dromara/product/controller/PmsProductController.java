package org.dromara.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.product.domain.bo.*;
import org.dromara.product.domain.vo.PmsProductDetailVo;
import org.dromara.product.domain.vo.PmsProductVo;
import org.dromara.product.service.IPmsProductService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理Controller
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product/product")
public class PmsProductController extends BaseController {

    private final IPmsProductService productService;

    /**
     * 分页查询商品列表
     */
    @SaCheckPermission("product:product:list")
    @GetMapping("/list")
    public TableDataInfo<PmsProductVo> list(
        @Validated(QueryGroup.class) PmsProductQueryBo bo,
        PageQuery pageQuery
    ) {
        return productService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询商品完整详情，用于后台查看和编辑回显
     */
    @SaCheckPermission("product:product:query")
    @GetMapping("/{productId}")
    public R<PmsProductDetailVo> getInfo(
        @PathVariable
        @NotNull(message = "商品ID不能为空")
        Long productId
    ) {
        return R.ok(productService.queryDetailById(productId));
    }

    /**
     * 新增商品
     */
    @SaCheckPermission("product:product:add")
    @Log(
        title = "商品管理",
        businessType = BusinessType.INSERT
    )
    @RepeatSubmit
    @PostMapping
    public R<Void> add(
        @Validated(AddGroup.class)
        @RequestBody
        PmsProductCreateBo bo
    ) {
        return toAjax(productService.insertByBo(bo));
    }

    /**
     * 修改商品
     */
    @SaCheckPermission("product:product:edit")
    @Log(
        title = "商品管理",
        businessType = BusinessType.UPDATE
    )
    @RepeatSubmit
    @PutMapping
    public R<Void> update(
        @Validated(EditGroup.class)
        @RequestBody
        PmsProductUpdateBo bo
    ) {
        return toAjax(productService.updateByBo(bo));
    }

    /**
     * 删除商品
     */
    @SaCheckPermission("product:product:remove")
    @Log(
        title = "商品管理",
        businessType = BusinessType.DELETE
    )
    @DeleteMapping("/{productId}")
    public R<Void> remove(
        @PathVariable
        @NotNull(message = "商品ID不能为空")
        Long productId
    ) {
        return toAjax(productService.deleteWithValidById(productId));
    }

    /**
     * 商品上下架
     */
    @SaCheckPermission("product:product:edit")
    @Log(
        title = "商品上下架",
        businessType = BusinessType.UPDATE
    )
    @RepeatSubmit
    @PutMapping("/{productId}/status")
    public R<Void> changeStatus(
        @PathVariable("productId")
        @NotNull(message = "商品ID不能为空")
        Long productId,
        @Valid
        @RequestBody
        PmsProductStatusBo bo
    ) {
        return toAjax(
            productService.changeStatus(productId, bo)
        );
    }
}
