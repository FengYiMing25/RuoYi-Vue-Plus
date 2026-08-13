-- 商品管理目录
insert into sys_menu (
    menu_id, menu_name, parent_id, order_num,
    path, component, query_param,
    is_frame, is_cache, menu_type,
    visible, status, perms, icon,
    create_dept, create_by, create_time,
    update_by, update_time, remark
) values (
             2000, '商品管理', 0, 2,
             'product', null, '',
             1, 0, 'M',
             '1', '0', '', 'shopping',
             103, 1, sysdate(),
             null, null, '商品管理目录'
         );

-- 商品分类菜单
insert into sys_menu (
    menu_id, menu_name, parent_id, order_num,
    path, component, query_param,
    is_frame, is_cache, menu_type,
    visible, status, perms, icon,
    create_dept, create_by, create_time,
    update_by, update_time, remark
) values (
             2001, '商品分类', 2000, 1,
             'category', 'product/category/index', '',
             1, 0, 'C',
             '1', '0', 'product:category:list', 'tree',
             103, 1, sysdate(),
             null, null, '商品分类管理菜单'
         );

-- 商品分类按钮权限
insert into sys_menu (
    menu_id, menu_name, parent_id, order_num,
    path, component, query_param,
    is_frame, is_cache, menu_type,
    visible, status, perms, icon,
    create_dept, create_by, create_time,
    update_by, update_time, remark
) values
      (
          2002, '分类查询', 2001, 1,
          '#', '', '',
          1, 0, 'F',
          '0', '0', 'product:category:query', '#',
          103, 1, sysdate(),
          null, null, ''
      ),
      (
          2003, '分类新增', 2001, 2,
          '#', '', '',
          1, 0, 'F',
          '0', '0', 'product:category:add', '#',
          103, 1, sysdate(),
          null, null, ''
      ),
      (
          2004, '分类修改', 2001, 3,
          '#', '', '',
          1, 0, 'F',
          '0', '0', 'product:category:edit', '#',
          103, 1, sysdate(),
          null, null, ''
      ),
      (
          2005, '分类删除', 2001, 4,
          '#', '', '',
          1, 0, 'F',
          '0', '0', 'product:category:remove', '#',
          103, 1, sysdate(),
          null, null, ''
      );
