package org.dromara.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.product.domain.bo.PmsCategoryBo;
import org.dromara.product.domain.vo.PmsCategoryVo;
import org.dromara.product.service.IPmsCategoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类Controller
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product/category")
public class PmsCategoryController extends BaseController {

    private final IPmsCategoryService categoryService;

    /**
     * 查询商品分类列表
     */
    @SaCheckPermission("product:category:list")
    @GetMapping("/list")
    public R<List<PmsCategoryVo>> list (@Validated(QueryGroup.class)  PmsCategoryBo bo){
        return  R.ok(categoryService.queryList(bo));
    }
    /**
     * 查询商品分类详情
     */
    @SaCheckPermission("product:category:query")
    @GetMapping("/{categoryId}")
    public R<PmsCategoryVo> getInfo(
        @PathVariable
        @NotNull(message = "分类ID不能为空")
        Long categoryId
    ) {
        return R.ok(categoryService.queryById(categoryId));
    }
    /**
     * 查询商品分类树形列表
     */
    @SaCheckPermission("product:category:list")
    @GetMapping("/tree")
    public R<List<PmsCategoryVo>> tree (@Validated(QueryGroup.class)  PmsCategoryBo bo) {
        return R.ok(categoryService.queryTreeList(bo));
    }

    /*
     * 新增商品分类
     */
    @SaCheckPermission("product:category:add")
    @PostMapping
    @RepeatSubmit
    @Log(title = "商品分类", businessType = BusinessType.INSERT)
    public R<Void> add(@Validated(AddGroup.class) @RequestBody PmsCategoryBo bo) {
        return toAjax(categoryService.insertByBo(bo));
    }

    /**
     * 修改商品分类
     */
    @Log(title = "商品分类", businessType = BusinessType.UPDATE)
    @SaCheckPermission("product:category:edit")
    @PutMapping
    @RepeatSubmit
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PmsCategoryBo bo) {
        return toAjax(categoryService.updateByBo(bo));
    }

    /**
     * 删除商品分类
     */
    @Log(title = "商品分类", businessType = BusinessType.DELETE)
    @SaCheckPermission("product:category:remove")
    @DeleteMapping("/{categoryId}")
    public R<Void> remove(@PathVariable @NotNull(message = "分类ID不能为空") Long categoryId) {
        return toAjax(categoryService.deleteWithValidById(categoryId));
    }
}
