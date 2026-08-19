-- 商品SKU表
-- SKU是商品实际销售单元，是价格和库存的真实来源
-- 每个商品创建时，由Service自动创建一条“默认”SKU

create table pms_product_sku
(
    sku_id          bigint                          not null comment 'SKU ID'
        primary key,
    product_id      bigint                          not null comment '商品ID',
    sku_code        varchar(64)                     not null comment 'SKU编码',
    sku_name        varchar(200) default '默认'      not null comment 'SKU名称',
    sku_type        char         default '0'         not null comment 'SKU类型（0默认 1自定义）',
    spec_key        varchar(255)                     not null  comment '规格值组合键，默认SKU为DEFAULT',
    sku_image_id    bigint                          null comment 'SKU图片OSS ID',
    sale_price      decimal(10,2) default 0.00       not null comment '销售价格',
    market_price    decimal(10,2)                   null comment '市场划线价',
    stock           int           default 0          not null comment '库存数量',
    warning_stock   int           default 0          not null comment '库存预警值',
    status          char          default '0'        not null comment '状态（0正常 1停用）',
    sort_num        int           default 0          not null comment '显示顺序',
    version         int           default 0          not null comment '乐观锁版本号',
    del_flag        char          default '0'        not null comment '删除标志（0存在 1删除）',
    create_dept     bigint                          null comment '创建部门',
    create_by       bigint                          null comment '创建者',
    create_time     datetime                        null comment '创建时间',
    update_by       bigint                          null comment '更新者',
    update_time     datetime                        null comment '更新时间',
    remark          varchar(500)                    null comment '备注',
    constraint uk_pms_product_sku_code
        unique (sku_code)
)
    comment '商品SKU表';

create index idx_pms_product_sku_product_status_sort
    on pms_product_sku (
                        product_id,
                        del_flag,
                        status,
                        sort_num
        );

create index idx_pms_product_sku_product_type
    on pms_product_sku (
                        product_id,
                        del_flag,
                        sku_type
        );
create index idx_pms_sku_product_spec_key
    on pms_product_sku (
                        product_id,
                        spec_key,
                        del_flag
        );
