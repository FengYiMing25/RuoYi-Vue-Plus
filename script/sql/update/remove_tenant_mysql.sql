-- RuoYi-Vue-Plus 5.6.2 多租户版迁移为单租户版（MySQL 8+）
-- 执行前请完整备份数据库。本脚本会永久删除租户表、租户字段及租户菜单。

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM sys_role_menu
WHERE menu_id IN (6, 121, 122, 1606, 1607, 1608, 1609, 1610, 1611, 1612, 1613, 1614, 1615);

DELETE FROM sys_menu
WHERE menu_id IN (6, 121, 122, 1606, 1607, 1608, 1609, 1610, 1611, 1612, 1613, 1614, 1615);

DROP TABLE IF EXISTS sys_tenant_package;
DROP TABLE IF EXISTS sys_tenant;

ALTER TABLE sys_social DROP COLUMN tenant_id;
ALTER TABLE sys_dept DROP COLUMN tenant_id;
ALTER TABLE sys_user DROP COLUMN tenant_id;
ALTER TABLE sys_post DROP COLUMN tenant_id;
ALTER TABLE sys_role DROP COLUMN tenant_id;
ALTER TABLE sys_oper_log DROP COLUMN tenant_id;
ALTER TABLE sys_dict_type
    DROP INDEX tenant_id,
    DROP COLUMN tenant_id,
    ADD UNIQUE KEY uk_sys_dict_type_dict_type (dict_type);
ALTER TABLE sys_dict_data DROP COLUMN tenant_id;
ALTER TABLE sys_config DROP COLUMN tenant_id;
ALTER TABLE sys_logininfor DROP COLUMN tenant_id;
ALTER TABLE sys_notice DROP COLUMN tenant_id;
ALTER TABLE sys_oss DROP COLUMN tenant_id;
ALTER TABLE sys_oss_config DROP COLUMN tenant_id;
ALTER TABLE test_demo DROP COLUMN tenant_id;
ALTER TABLE test_tree DROP COLUMN tenant_id;

-- 工作流业务表；WarmFlow 引擎自身 flow_* 运行表的 tenant_id 为引擎兼容字段，不能删除。
ALTER TABLE flow_category DROP COLUMN tenant_id;
ALTER TABLE flow_instance_biz_ext DROP COLUMN tenant_id;
ALTER TABLE test_leave DROP COLUMN tenant_id;

SET FOREIGN_KEY_CHECKS = 1;
