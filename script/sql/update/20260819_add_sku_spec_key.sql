-- SKU增加规格组合键
alter table pms_product_sku add
    column spec_key varchar(255) null comment '规格值组合键，默认 SKU 为 DEFAULT'
    after sku_type;

-- 回填默认 SKU
update pms_product_sku set spec_key = 'DEFAULT'
    where sku_type = '0' and spec_key is null;

-- 回填已有的多规格 SKU
update pms_product_sku sku
    join (select sku_id, group_concat(spec_value_id order by spec_value_id separator '_') as spec_key
          from pms_sku_spec_value group by sku_id
          ) relation_data on relation_data.sku_id = sku.sku_id
    set sku.spec_key = relation_data.spec_key
    where sku.sku_type = '1' and sku.spec_key is null;

-- 查询是否存在无法回填的数据
select sku_id,product_id,sku_name,sku_type  from pms_product_sku
        where spec_key is null;
alter table pms_product_sku
    modify column spec_key varchar(255) not null
        comment '规格值组合键，默认SKU为DEFAULT';

create index idx_pms_sku_product_spec_key
    on pms_product_sku (
                        product_id,
                        spec_key,
                        del_flag
        );
