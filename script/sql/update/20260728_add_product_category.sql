-- 商品管理模块：商品分类表
-- 日期：2026年 7 月 28日
-- 商品分类采用最多三级分类的树形结构

CREATE TABLE pms_category
(
    category_id       BIGINT          NOT NULL                    COMMENT '分类ID',
    category_code     VARCHAR(64)     NOT NULL                    COMMENT '分类编码',
    category_name     VARCHAR(100)    NOT NULL                    COMMENT '分类名称',
    parent_id         BIGINT          NOT NULL DEFAULT 0          COMMENT '父分类ID，一级分类为0',
    ancestors         VARCHAR(500)    NOT NULL DEFAULT '0'        COMMENT '祖级列表',
    category_level    TINYINT UNSIGNED NOT NULL DEFAULT 1         COMMENT '分类层级，最多三级',
    image_id          BIGINT          DEFAULT NULL                COMMENT '分类图片OSS ID',
    sort_num          INT             NOT NULL DEFAULT 0          COMMENT '显示顺序',
    status            CHAR(1)         NOT NULL DEFAULT '0'        COMMENT '状态（0正常 1停用）',
    del_flag          CHAR(1)         NOT NULL DEFAULT '0'        COMMENT '删除标志（0存在 1删除）',
    create_dept       BIGINT          DEFAULT NULL                COMMENT '创建部门',
    create_by         BIGINT          DEFAULT NULL                COMMENT '创建者',
    create_time       DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_by         BIGINT          DEFAULT NULL                COMMENT '更新者',
    update_time       DATETIME        DEFAULT NULL                COMMENT '更新时间',
    remark            VARCHAR(500)    DEFAULT NULL                COMMENT '备注',

    PRIMARY KEY (category_id),
    UNIQUE KEY uk_pms_category_code (category_code),
    KEY idx_pms_category_parent_name
        (parent_id, category_name, del_flag),
    KEY idx_pms_category_parent_status_sort
        (parent_id, del_flag, status, sort_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
    COMMENT = '商品分类表';
