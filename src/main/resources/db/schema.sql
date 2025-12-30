-- 创建数据库
CREATE DATABASE IF NOT EXISTS `dorm_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE `dorm_system`;

-- =============================================
-- 0. 清理旧表 (注意删除顺序：先删从表，再删主表)
-- =============================================
-- DROP TABLE IF EXISTS `inspection_detail`;
-- DROP TABLE IF EXISTS `inspection_record`;
-- DROP TABLE IF EXISTS `student`;
-- DROP TABLE IF EXISTS `dormitory`;
-- DROP TABLE IF EXISTS `inspection_item`;

-- =============================================
-- 1. 宿舍表 (dormitory)
-- 核心基础数据，存储宿舍物理位置及管理信息
-- =============================================
CREATE TABLE IF NOT EXISTS `dormitory` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `building_name` VARCHAR(50) NOT NULL COMMENT '楼栋名称（如：A栋、北苑1号楼）',
    `floor` INT NOT NULL COMMENT '楼层',
    `room_number` VARCHAR(20) NOT NULL COMMENT '房间号（如：301）',
    `manager_student_sn` VARCHAR(32) DEFAULT NULL COMMENT '宿舍长学号（冗余字段，便于快速查询）',
    `capacity` INT DEFAULT 4 COMMENT '床位容量',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_location` (`building_name`, `floor`, `room_number`) COMMENT '唯一索引：物理位置不可重复'
) ENGINE=InnoDB COMMENT='宿舍信息表';

-- =============================================
-- 2. 学生表 (student)
-- 学生基础信息，关联宿舍
-- =============================================
CREATE TABLE IF NOT EXISTS `student` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_sn` VARCHAR(32) NOT NULL COMMENT '学号（业务主键）',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `class_name` VARCHAR(50) NOT NULL COMMENT '班级（如：计科2101）',
    `gender` TINYINT DEFAULT 1 COMMENT '性别：1-男，2-女',
    `dorm_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '所属宿舍ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_sn` (`student_sn`) COMMENT '唯一索引：学号不可重复',
    KEY `idx_dorm_id` (`dorm_id`) COMMENT '索引：加速按宿舍查询学生',
    CONSTRAINT `fk_student_dorm` FOREIGN KEY (`dorm_id`) REFERENCES `dormitory` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='学生信息表';

-- =============================================
-- 3. 检查项配置表 (inspection_item)
-- 动态配置检查标准，便于后续调整分值
CREATE TABLE IF NOT EXISTS `inspection_item` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `item_name` VARCHAR(100) NOT NULL COMMENT '检查项名称（如：地面清洁、物品摆放）',
    `max_score` INT NOT NULL DEFAULT 10 COMMENT '该项满分值',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '评分标准描述',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `sort_order` INT DEFAULT 0 COMMENT '排序优先级',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='卫生检查项配置表';

-- =============================================
-- 4. 检查记录主表 (inspection_record)
-- 记录一次完整的宿舍卫生检查概况
-- =============================================
CREATE TABLE IF NOT EXISTS `inspection_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dorm_id` BIGINT UNSIGNED NOT NULL COMMENT '受检宿舍ID',
    `inspector_name` VARCHAR(50) NOT NULL COMMENT '检查人员姓名（或关联管理员ID）',
    `total_score` DECIMAL(5, 2) NOT NULL DEFAULT 0.00 COMMENT '总得分',
    `check_date` DATE NOT NULL COMMENT '检查日期',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '整改意见/备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_dorm_date` (`dorm_id`, `check_date`) COMMENT '索引：加速查询某宿舍某天的记录',
    KEY `idx_check_date` (`check_date`) COMMENT '索引：加速按日期统计',
    CONSTRAINT `fk_record_dorm` FOREIGN KEY (`dorm_id`) REFERENCES `dormitory` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='卫生检查记录主表';

-- 尝试添加 is_notice 字段 (如果不存在)
-- 注意：MySQL 5.7 不支持 ADD COLUMN IF NOT EXISTS，这里依赖 spring.sql.init.continue-on-error=true
ALTER TABLE `inspection_record` ADD COLUMN `is_notice` TINYINT DEFAULT 0 COMMENT '是否通报';
ALTER TABLE `inspection_record` ADD COLUMN `image_url` VARCHAR(500) DEFAULT NULL COMMENT '现场照片';
ALTER TABLE `inspection_record` ADD COLUMN `rectification_status` TINYINT DEFAULT 0 COMMENT '整改状态:0无,1待审,2通过,3驳回';
ALTER TABLE `inspection_record` ADD COLUMN `rectification_desc` VARCHAR(500) DEFAULT NULL COMMENT '整改说明';
ALTER TABLE `inspection_record` ADD COLUMN `rectification_image_url` VARCHAR(500) DEFAULT NULL COMMENT '整改照片';

-- =============================================
-- 5. 检查明细表 (inspection_detail)
-- 记录每一项的具体得分和扣分情况
-- =============================================
CREATE TABLE IF NOT EXISTS `inspection_detail` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `record_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的主记录ID',
    `item_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的检查项ID',
    `score` DECIMAL(5, 2) NOT NULL COMMENT '实际得分',
    `deduction_reason` VARCHAR(255) DEFAULT NULL COMMENT '扣分说明',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '现场照片URL（建议存储在OSS）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_record_id` (`record_id`) COMMENT '索引：加速查询某次检查的详情',
    CONSTRAINT `fk_detail_item` FOREIGN KEY (`item_id`) REFERENCES `inspection_item` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='卫生检查明细表';
