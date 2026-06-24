package org.dromara.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.demo.domain.TestTree;
import org.dromara.demo.domain.bo.TestTreeBo;
import org.dromara.demo.domain.vo.TestTreeVo;
import org.dromara.demo.mapper.TestTreeMapper;
import org.dromara.demo.service.ITestTreeService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 测试树表Service业务层处理
 *
 * @author Lion Li
 * @date 2021-07-26
 */
// @DS("slave") // 切换从库查询
@RequiredArgsConstructor
@Service
public class TestTreeServiceImpl implements ITestTreeService {

    private static final Long ROOT_PARENT_ID = 0L;

    private final TestTreeMapper baseMapper;

    @Override
    public TestTreeVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    // @DS("slave") // 切换从库查询
    @Override
    public List<TestTreeVo> queryList(TestTreeBo bo) {
        LambdaQueryWrapper<TestTree> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TestTree> buildQueryWrapper(TestTreeBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<TestTree> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getDeptId() != null, TestTree::getDeptId, bo.getDeptId());
        lqw.eq(bo.getUserId() != null, TestTree::getUserId, bo.getUserId());
        lqw.like(StringUtils.isNotBlank(bo.getTreeName()), TestTree::getTreeName, bo.getTreeName());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            TestTree::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
        lqw.orderByAsc(TestTree::getId);
        return lqw;
    }

    @Override
    public Boolean insertByBo(TestTreeBo bo) {
        TestTree add = MapstructUtils.convert(bo, TestTree.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(TestTreeBo bo) {
        TestTree update = MapstructUtils.convert(bo, TestTree.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 实体类数据
     */
    private void validEntityBeforeSave(TestTree entity) {
        Long id = entity.getId();
        Long parentId = entity.getParentId();
        entity.setTreeName(StringUtils.trim(entity.getTreeName()));

        if (id != null && baseMapper.selectById(id) == null) {
            throw new ServiceException("树节点不存在，无法修改");
        }
        if (parentId == null) {
            throw new ServiceException("父ID不能为空，根节点请传0");
        }
        if (parentId.equals(id)) {
            throw new ServiceException("不能将当前节点设置为自己的父节点");
        }

        if (!ROOT_PARENT_ID.equals(parentId)) {
            TestTree parent = baseMapper.selectById(parentId);
            if (parent == null) {
                throw new ServiceException("父节点不存在");
            }
            if (id != null) {
                validateParentChain(id, parent);
            }
        }

        boolean duplicateName = baseMapper.selectCount(Wrappers.<TestTree>lambdaQuery()
            .eq(TestTree::getParentId, parentId)
            .eq(TestTree::getTreeName, entity.getTreeName())
            .ne(id != null, TestTree::getId, id)) > 0;
        if (duplicateName) {
            throw new ServiceException("同一父节点下已存在同名节点");
        }
    }

    /**
     * 从新父节点向根节点回溯，避免把当前节点移动到自己的子孙节点下。
     */
    private void validateParentChain(Long id, TestTree parent) {
        Set<Long> visited = new HashSet<>();
        TestTree current = parent;
        while (current != null) {
            Long currentId = current.getId();
            if (id.equals(currentId)) {
                throw new ServiceException("不能将节点移动到自己的子孙节点下");
            }
            if (!visited.add(currentId)) {
                throw new ServiceException("树结构中已存在循环关系，请先修复数据");
            }

            Long currentParentId = current.getParentId();
            if (currentParentId == null || ROOT_PARENT_ID.equals(currentParentId)) {
                return;
            }
            current = baseMapper.selectById(currentParentId);
            if (current == null) {
                throw new ServiceException("父节点链不完整，请先修复数据");
            }
        }
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            boolean hasChildren = baseMapper.selectCount(Wrappers.<TestTree>lambdaQuery()
                .in(TestTree::getParentId, ids)) > 0;
            if (hasChildren) {
                throw new ServiceException("存在子节点，不允许删除父节点");
            }
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
