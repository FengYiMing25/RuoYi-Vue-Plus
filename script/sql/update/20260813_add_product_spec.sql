-- 商品规格名称表
-- 例如：颜色、容量、尺码

create table pms_product_spec
(
    spec_id       bigint          not null comment '规格ID'
        primary key,
    product_id    bigint          not null comment '商品ID',
    spec_name     varchar(64)     not null comment '规格名称',
    sort_num      int default 0   not null comment '显示顺序',
    del_flag      char default '0' not null comment '删除标志（0存在 1删除）',
    create_dept   bigint          null comment '创建部门',
    create_by     bigint          null comment '创建者',
    create_time   datetime        null comment '创建时间',
    update_by     bigint          null comment '更新者',
    update_time   datetime        null comment '更新时间'
)
    comment '商品规格名称表';

create index idx_pms_product_spec_product_sort
    on pms_product_spec (
                         product_id,
                         del_flag,
                         sort_num
        );


-- 商品规格值表
-- 例如：颜色下面有黑色、白色；容量下面有256G、512G

create table pms_product_spec_value
(
    spec_value_id bigint           not null comment '规格值ID'
        primary key,
    product_id    bigint           not null comment '商品ID',
    spec_id       bigint           not null comment '规格ID',
    spec_value    varchar(100)     not null comment '规格值',
    sort_num      int default 0    not null comment '显示顺序',
    del_flag      char default '0' not null comment '删除标志（0存在 1删除）',
    create_dept   bigint           null comment '创建部门',
    create_by     bigint           null comment '创建者',
    create_time   datetime         null comment '创建时间',
    update_by     bigint           null comment '更新者',
    update_time   datetime         null comment '更新时间'
)
    comment '商品规格值表';

create index idx_pms_spec_value_spec_sort
    on pms_product_spec_value (
                               spec_id,
                               del_flag,
                               sort_num
        );

create index idx_pms_spec_value_product
    on pms_product_spec_value (
                               product_id,
                               del_flag
        );


-- SKU与规格值关联表
-- 一条SKU在每个规格维度下只能选择一个规格值

create table pms_sku_spec_value
(
    relation_id      bigint not null comment '关联ID'
        primary key,
    sku_id           bigint not null comment 'SKU ID',
    product_id       bigint not null comment '商品ID',
    spec_id          bigint not null comment '规格ID',
    spec_value_id    bigint not null comment '规格值ID',

    constraint uk_pms_sku_spec
        unique (sku_id, spec_id)
)
    comment 'SKU规格值关联表';

create index idx_pms_sku_spec_value_product
    on pms_sku_spec_value (product_id);

create index idx_pms_sku_spec_value_value
    on pms_sku_spec_value (spec_value_id);
