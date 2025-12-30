SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE inspection_detail;
TRUNCATE TABLE inspection_record;
TRUNCATE TABLE inspection_item;
TRUNCATE TABLE student;
TRUNCATE TABLE dormitory;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 初始化宿舍数据
INSERT INTO dormitory (building_name, floor, room_number, capacity, created_at, updated_at)
WITH RECURSIVE 
buildings AS (
  SELECT '北5栋' as name UNION ALL SELECT '北7栋' UNION ALL SELECT '北8栋' UNION ALL SELECT '北10栋'
  UNION ALL SELECT '南23栋' UNION ALL SELECT '南24栋' UNION ALL SELECT '南27栋' UNION ALL SELECT '南28栋' UNION ALL SELECT '南29栋' UNION ALL SELECT '南30栋' UNION ALL SELECT '南31栋'
),
floors AS (
  SELECT 2 as f UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
),
rooms AS (
  SELECT 10 as r UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14
  UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19
  UNION ALL SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24
  UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29
  UNION ALL SELECT 30 UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34
  UNION ALL SELECT 35 UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39
  UNION ALL SELECT 40
)
SELECT 
  b.name, 
  f.f, 
  CONCAT(f.f, r.r), 
  4, 
  NOW(), 
  NOW()
FROM buildings b
CROSS JOIN floors f
CROSS JOIN rooms r;

-- 2. 初始化检查项数据
INSERT INTO inspection_item (item_name, max_score, description, is_enabled, sort_order) VALUES 
('地面清洁', 20, '地面无垃圾、无积水、无污渍', 1, 1),
('垃圾处理', 20, '垃圾桶及时清理，无堆积', 1, 2),
('物品摆放', 20, '桌面、书架物品摆放整齐', 1, 3),
('床铺整理', 20, '被褥叠放整齐，床单平整', 1, 4),
('违规电器', 20, '无违规使用大功率电器', 1, 5);

-- 3. 初始化演示用学生数据（宿舍长）
-- 为 北10栋-532 添加一名宿舍长
INSERT INTO student (student_sn, name, class_name, gender, dorm_id)
SELECT '20210001', '张三', '计科2101', 1, id 
FROM dormitory WHERE building_name='北10栋' AND room_number='532' LIMIT 1;

UPDATE dormitory SET manager_student_sn = '20210001' 
WHERE building_name='北10栋' AND room_number='532';
