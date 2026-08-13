package org.dromara.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.product.domain.PmsCategory;
import org.dromara.product.domain.bo.PmsCategoryBo;
import org.dromara.product.domain.vo.PmsCategoryVo;
import org.dromara.product.mapper.PmsCategoryMapper;
import org.dromara.product.service.IPmsCategoryService;
import org.springframework.stereotype.Service;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PmsCategoryServiceImpl  implements IPmsCategoryService {
//    父级顶级 iD
    private static final  Long ROOT_PARENT_ID = 0L;
//    分类最多三级
    private static final int MAX_CATEGORY_LEVEL = 3;
    private final PmsCategoryMapper baseMapper;


//    根据分类 id 查询分类详情
    @Override
    public PmsCategoryVo queryById(Long categoryId) {

        return baseMapper.selectVoById(categoryId, PmsCategoryVo.class);
    }

//    查询分类列表
    @Override
    public List<PmsCategoryVo> queryList(PmsCategoryBo bo) {
        LambdaQueryWrapper<PmsCategory> queryWrapper = buildQueryWrapper(bo);
        return baseMapper.selectVoList(queryWrapper);
    }
//   构建查询条件
    private LambdaQueryWrapper<PmsCategory> buildQueryWrapper(PmsCategoryBo bo) {
       LambdaQueryWrapper<PmsCategory> wrapper = Wrappers.lambdaQuery();

       wrapper.eq(bo.getCategoryId() != null, PmsCategory::getCategoryId, bo.getCategoryId());

       wrapper.eq(bo.getParentId() != null, PmsCategory::getParentId,bo.getParentId());

       wrapper.like(StringUtils.isNotBlank(bo.getCategoryCode()), PmsCategory::getCategoryCode, bo.getCategoryCode());

       wrapper.like(StringUtils.isNotBlank(bo.getCategoryName()), PmsCategory::getCategoryName, bo.getCategoryName());

       wrapper.eq(StringUtils.isNotBlank(bo.getStatus()), PmsCategory::getStatus, bo.getStatus());

       wrapper.orderByAsc(PmsCategory::getCategoryLevel, PmsCategory::getSortNum,PmsCategory::getCategoryId);

    return wrapper;
    }

    @Override
    public List<PmsCategoryVo> queryTreeList(PmsCategoryBo bo) {
        List<PmsCategoryVo> categoryList = queryList(bo);
        return buildCategoryTree(categoryList);
    }

    private List<PmsCategoryVo> buildCategoryTree(List<PmsCategoryVo> categoryList) {
        if (categoryList == null || categoryList.isEmpty()){
            return  new ArrayList<>();
        }
        Map<Long, PmsCategoryVo> categoryMap = categoryList.stream()
                .collect(Collectors.toMap(
                            PmsCategoryVo::getCategoryId,
                            Function.identity(),
                            (first,second)-> first,
                            LinkedHashMap::new
                ));
        categoryList.forEach(category->category.setChildren(new ArrayList<>()));
        List<PmsCategoryVo> rootList = new ArrayList<>();

        for (PmsCategoryVo category : categoryList) {
            Long parentId = category.getParentId();
            PmsCategoryVo parent = categoryMap.get(parentId);
            if (ROOT_PARENT_ID.equals(parentId) || parent == null) {
                rootList.add(category);
            } else {
                parent.getChildren().add(category);
            }
        }
        return rootList;
    }


    @Override
    public Boolean insertByBo(PmsCategoryBo bo) {
        PmsCategory category = MapstructUtils.convert(bo, PmsCategory.class);

        if (category == null) {
            throw new ServiceException("分类数据转换失败");
        }
//      去掉编码和名称两端的空格
        category.setCategoryCode(StringUtils.trim(category.getCategoryCode()));
        category.setCategoryName(StringUtils.trim(category.getCategoryName()));

//      新增时没有传状态，默认设置为正常
        if (StringUtils.isBlank(category.getStatus())) {
            category.setStatus(SystemConstants.NORMAL);
        }

//      根据父分类计算 ancestors 和 categoryLevel
        fillCategoryHierarchy(category);

//      校验分类编码、分类名称是否重复
        validateCategoryUnique(category);
        boolean success = baseMapper.insert(category) > 0;

        if (success) {
//            insert后，MyBatis plus 会把生成的主键写回实体对象
            bo.setCategoryId(category.getCategoryId());
        }

        return success;
    }



    //  根据父分类计算当前分类的组级列表和分类层级
    private void fillCategoryHierarchy(PmsCategory category) {
         Long parentId = category.getParentId();
         if(parentId == null) {
             throw  new ServiceException("父分类ID不能为空,一级分类请传 0");
         }

//       一级分类
        if (ROOT_PARENT_ID.equals(parentId)){
            category.setAncestors("0");
            category.setCategoryLevel(1);
            return;
        }

//      二级、三级分类必须查询父分类
        PmsCategory parent = baseMapper.selectById(parentId);

        if (parent == null) {
            throw new ServiceException("父分类不存在");
        }

        if (!SystemConstants.NORMAL.equals(parent.getStatus())) {
            throw new ServiceException("父分类已停用，无法添加子分类");
        }

        if (parent.getCategoryLevel() == null) {
            throw new ServiceException("父分类层级数据异常");
        }

        if (parent.getCategoryLevel() >= MAX_CATEGORY_LEVEL) {
            throw new ServiceException("分类层级不能超过" + MAX_CATEGORY_LEVEL + "级");
        }

        category.setAncestors(parent.getAncestors() + "," + parent.getCategoryId());
        category.setCategoryLevel(parent.getCategoryLevel() + 1);
    }
//   校验分类编码、分类名称是否重复
    private void validateCategoryUnique(PmsCategory category) {
        Long categoryId = category.getCategoryId();

        boolean duplicateCode = baseMapper.selectCount(
            Wrappers.<PmsCategory>lambdaQuery()
                .eq(PmsCategory::getCategoryCode, category.getCategoryCode())
                .ne(categoryId != null, PmsCategory::getCategoryId, categoryId)
        ) > 0;

        if (duplicateCode) {
            throw new ServiceException("分类编码已存在");
        }

        boolean duplicateName = baseMapper.selectCount(
            Wrappers.<PmsCategory>lambdaQuery()
                .eq(PmsCategory::getParentId, category.getParentId())
                .eq(PmsCategory::getCategoryName, category.getCategoryName())
                .ne(categoryId != null, PmsCategory::getCategoryId, categoryId)
        ) > 0;

        if (duplicateName) {
            throw new ServiceException("同一父分类下已存在同名分类");
        }
    }
    @Override
    public Boolean updateByBo(PmsCategoryBo bo) {
        PmsCategory current = baseMapper.selectById(bo.getCategoryId());
        if (current == null) {
            throw new ServiceException("分类不存在");
        }
        PmsCategory category = MapstructUtils.convert(bo, PmsCategory.class);

        if (category == null) {
            throw new ServiceException("分类数据转换失败");
        }
        category.setCategoryCode(StringUtils.trim(category.getCategoryCode()));
        category.setCategoryName(StringUtils.trim(category.getCategoryName()));
//        修改时如果没有传状态，则保留原状态
        if (StringUtils.isBlank(category.getStatus())) {
            category.setStatus(current.getStatus());
        }
//        保留编码是稳定的业务标识，创建后不允许修改
//        if (!Objects.equals(current.getCategoryCode(),category.getCategoryCode())) {
//            throw new ServiceException("分类编码不允许修改");
//        }
//        防止自己成为自己的父分类
        if (Objects.equals(category.getCategoryId(), category.getParentId())) {
            throw new ServiceException("不能将当前分类设置为自己的父分类");
        }
        boolean parentChanged = !Objects.equals(current.getParentId(), category.getParentId());

        if (parentChanged) {
            if (hasChildren(category.getCategoryId())) {
                throw new ServiceException("当前分类存在子分类，不允许修改父分类");
            }
//            根据父分类计算 ancestors 和 categoryLevel
            fillCategoryHierarchy(category);
        } else {
//            父分类没有变化，保留原来的树结构数据
            category.setAncestors(current.getAncestors());
            category.setCategoryLevel(current.getCategoryLevel());
        }

//        正常状态的分类不能挂在停用的父分类下面
        validateParentStatusWhenEnabled(category);

//        停用父分类前，必须先停用它下面正常状态的子分类
        if (SystemConstants.DISABLE.equals(category.getStatus()) && hasNormalChildren(category.getCategoryId())) {
            throw new ServiceException("当前分类存在正常状态的子分类，请先停用子分类");

        }
        validateCategoryUnique(category);

        return baseMapper.updateById(category) > 0;
    }

    private boolean hasNormalChildren(Long categoryId) {
        return baseMapper.selectCount(
            Wrappers.<PmsCategory>lambdaQuery()
                .eq(PmsCategory::getParentId, categoryId)
                .eq(PmsCategory::getStatus, SystemConstants.NORMAL)
        ) > 0;
    }

    private void validateParentStatusWhenEnabled(PmsCategory category) {
        if (!SystemConstants.NORMAL.equals(category.getStatus())) {
            return;
        }

        if (ROOT_PARENT_ID.equals(category.getParentId())) {
            return;
        }

        PmsCategory parent = baseMapper.selectById(category.getParentId());
        if (parent == null) {
            throw new ServiceException("父分类不存在");
        }
        if (!SystemConstants.NORMAL.equals(parent.getStatus())) {
            throw new ServiceException("父分类状态不正常，当前分类不能启用");
        }
    }

    private boolean hasChildren(Long categoryId) {
        return baseMapper.selectCount(
            Wrappers.<PmsCategory>lambdaQuery()
                .eq(PmsCategory::getParentId, categoryId)
        ) > 0;
    }

    @Override
    public Boolean deleteWithValidById(Long categoryId) {

        if (categoryId == null) {
            throw new ServiceException("分类ID不能为空");
        }

        PmsCategory category = baseMapper.selectById(categoryId);

        if (category == null) {
            throw new ServiceException("分类不存在或已删除");
        }

        if (hasChildren(categoryId)) {
            throw new ServiceException("当前分类存在子分类，请先删除子分类");
        }

        /*
         * 商品表完成后，在这里增加校验：
         *
         * if (商品数量 > 0) {
         *     throw new ServiceException("当前分类下存在商品，不能删除");
         * }
         */

        return baseMapper.deleteById(categoryId) > 0;
    }
}
