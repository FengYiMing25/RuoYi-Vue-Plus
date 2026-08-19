-- 商品管理模块：商品主表
-- 商品主表保存SPU公共信息
-- 价格和库存的真实来源是SKU表
-- min_price、max_price、total_stock是列表查询使用的汇总字段

create table pms_product
(
    product_id     bigint                          not null comment '商品ID'
        primary key,
    product_code   varchar(64)                     not null comment '商品编码',
    product_name   varchar(200)                    not null comment '商品名称',
    category_id    bigint                          not null comment '所属分类ID',
    selling_point  varchar(255)                    null comment '商品卖点',
    main_image_id  bigint                          null comment '商品主图OSS ID',
    unit           varchar(32)  default '件'        not null comment '商品单位',
    min_price      decimal(10,2) default 0.00       not null comment 'SKU最低售价（汇总）',
    max_price      decimal(10,2) default 0.00       not null comment 'SKU最高售价（汇总）',
    total_stock    int           default 0          not null comment 'SKU总库存（汇总）',
    status         char          default '0'        not null comment '商品状态（0草稿 1上架 2下架）',
    sort_num       int           default 0          not null comment '显示顺序',
    version        int           default 0          not null comment '乐观锁版本号',
    del_flag       char          default '0'        not null comment '删除标志（0存在 1删除）',
    create_dept    bigint                          null comment '创建部门',
    create_by      bigint                          null comment '创建者',
    create_time    datetime                        null comment '创建时间',
    update_by      bigint                          null comment '更新者',
    update_time    datetime                        null comment '更新时间',
    remark         varchar(500)                    null comment '备注',
    constraint uk_pms_product_code
        unique (product_code)
)
    comment '商品主表';

create index idx_pms_product_category_status_sort
    on pms_product (
                    category_id,
                    del_flag,
                    status,
                    sort_num
        );

create index idx_pms_product_status_sort
    on pms_product (
                    del_flag,
                    status,
                    sort_num
        );
