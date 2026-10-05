-- 给排班日期和状态增加联合索引
-- 查询某天正常出诊的排班时，可以减少数据库扫描的数据量
CREATE INDEX idx_schedule_date_status
    ON doctor_schedule (schedule_date, status);