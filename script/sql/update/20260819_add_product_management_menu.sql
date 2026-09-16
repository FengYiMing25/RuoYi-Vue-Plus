-- 对齐当前数据库中的商品菜单，并兼容尚未创建商品列表菜单的新环境。
-- 动态路由规则：目录 path=product；子菜单必须使用相对 path；component 为 views 下去掉 .vue 的相对路径。

-- 商品目录：http://host/product/...
update sys_menu
set path = 'product',
    visible = '0',
    status = '0'
where menu_id = 2000;

-- 商品分类：http://host/product/category
update sys_menu
set path = 'category',
    component = 'product/category/index',
    order_num = 1,
    visible = '0',
    status = '0',
    perms = 'product:category:list'
where menu_id = 2001
  and parent_id = 2000;

-- 当前数据库已通过管理页面创建了商品列表菜单，菜单 ID 为雪花 ID，不能假设为固定的 2006。
-- 若目标环境尚未创建该菜单，则使用预留 ID 2006 补建。
insert into sys_menu (
    menu_id, menu_name, parent_id, order_num,
    path, component, query_param,
    is_frame, is_cache, menu_type,
    visible, status, perms, icon,
    create_dept, create_by, create_time,
    update_by, update_time, remark
)
select
    2006, '商品列表', 2000, 2,
    'product', 'product/product/index', '',
    1, 0, 'C',
    '0', '0', '', 'build',
    103, 1, sysdate(),
    null, null, '商品列表管理菜单'
where not exists (
    select 1
    from sys_menu
    where component = 'product/product/index'
      and menu_type = 'C'
);

-- 使用组件路径定位真实菜单 ID，兼容固定 ID 和雪花 ID 两种数据。
set @product_menu_id = (
    select menu_id
    from sys_menu
    where component = 'product/product/index'
      and menu_type = 'C'
    order by menu_id
    limit 1
);

-- 商品列表：http://host/product/product。
-- path 不能配置为 /product/product，否则父目录再次拼接后会变成 /product/product/product。
update sys_menu
set menu_name = '商品列表',
    parent_id = 2000,
    order_num = 2,
    path = 'product',
    component = 'product/product/index',
    visible = '0',
    status = '0',
    perms = ''
where menu_id = @product_menu_id;

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num,
    path, component, query_param,
    is_frame, is_cache, menu_type,
    visible, status, perms, icon,
    create_dept, create_by, create_time,
    update_by, update_time, remark
)
select
    2007, '分页查询商品列表', @product_menu_id, 1,
    '#', '', '',
    1, 0, 'F',
    '0', '0', 'product:product:list', '#',
    103, 1, sysdate(),
    null, null, ''
where not exists (
    select 1 from sys_menu where perms = 'product:product:list'
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num,
    path, component, query_param,
    is_frame, is_cache, menu_type,
    visible, status, perms, icon,
    create_dept, create_by, create_time,
    update_by, update_time, remark
)
select
    2008, '查询商品基础详情', @product_menu_id, 2,
    '#', '', '',
    1, 0, 'F',
    '0', '0', 'product:product:query', '#',
    103, 1, sysdate(),
    null, null, ''
where not exists (
    select 1 from sys_menu where perms = 'product:product:query'
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num,
    path, component, query_param,
    is_frame, is_cache, menu_type,
    visible, status, perms, icon,
    create_dept, create_by, create_time,
    update_by, update_time, remark
)
select
    2009, '新增商品', @product_menu_id, 3,
    '#', '', '',
    1, 0, 'F',
    '0', '0', 'product:product:add', '#',
    103, 1, sysdate(),
    null, null, ''
where not exists (
    select 1 from sys_menu where perms = 'product:product:add'
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num,
    path, component, query_param,
    is_frame, is_cache, menu_type,
    visible, status, perms, icon,
    create_dept, create_by, create_time,
    update_by, update_time, remark
)
select
    2010, '修改及上下架商品', @product_menu_id, 4,
    '#', '', '',
    1, 0, 'F',
    '0', '0', 'product:product:edit', '#',
    103, 1, sysdate(),
    null, null, ''
where not exists (
    select 1 from sys_menu where perms = 'product:product:edit'
);

insert into sys_menu (
    menu_id, menu_name, parent_id, order_num,
    path, component, query_param,
    is_frame, is_cache, menu_type,
    visible, status, perms, icon,
    create_dept, create_by, create_time,
    update_by, update_time, remark
)
select
    2011, '删除商品', @product_menu_id, 5,
    '#', '', '',
    1, 0, 'F',
    '0', '0', 'product:product:remove', '#',
    103, 1, sysdate(),
    null, null, ''
where not exists (
    select 1 from sys_menu where perms = 'product:product:remove'
);

-- 将已存在的按钮权限统一挂到真实的商品列表菜单下。
update sys_menu
set parent_id = @product_menu_id
where perms in (
    'product:product:list',
    'product:product:query',
    'product:product:add',
    'product:product:edit',
    'product:product:remove'
);
