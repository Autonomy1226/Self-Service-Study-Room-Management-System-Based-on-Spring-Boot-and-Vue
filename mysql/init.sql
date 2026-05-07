-- =====================================================
-- 自习室预约系统 - 完整数据库初始化脚本
-- 包含所有表结构和初始数据
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS study_room DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE study_room;

-- =====================================================
-- 1. 用户表
-- =====================================================
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    create_time DATETIME,
    update_time DATETIME
);

-- =====================================================
-- 2. 自习室表
-- =====================================================
CREATE TABLE IF NOT EXISTS study_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(20),
    description TEXT,
    floor_plan_url VARCHAR(255),
    create_time DATETIME,
    update_time DATETIME
);

-- =====================================================
-- 3. 座位表
-- =====================================================
CREATE TABLE IF NOT EXISTS seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) CHECK (status IN ('AVAILABLE', 'UNAVAILABLE')),
    pos_x INT,
    pos_y INT,
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (room_id) REFERENCES study_room(id)
);

-- =====================================================
-- 4. 预约表
-- =====================================================
CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20),
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (seat_id) REFERENCES seat(id)
);

-- =====================================================
-- 5. 公告表
-- =====================================================
CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) DEFAULT 'GENERAL',
    priority INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time DATETIME,
    update_time DATETIME
);

-- =====================================================
-- 6. 通知表
-- =====================================================
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    related_id BIGINT,
    related_type VARCHAR(50),
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- =====================================================
-- 7. 评价表
-- =====================================================
CREATE TABLE IF NOT EXISTS review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    reservation_id BIGINT,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content TEXT,
    tags VARCHAR(200),
    is_anonymous BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'APPROVED',
    helpful_count INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (seat_id) REFERENCES seat(id),
    FOREIGN KEY (reservation_id) REFERENCES reservation(id)
);

-- =====================================================
-- 8. 违规记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS violation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    violation_type VARCHAR(50) NOT NULL,
    description TEXT,
    violation_time DATETIME,
    status VARCHAR(20) DEFAULT 'PENDING',
    penalty_score INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (reservation_id) REFERENCES reservation(id)
);

-- =====================================================
-- 9. 使用记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS usage_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    actual_start_time DATETIME,
    actual_end_time DATETIME,
    status VARCHAR(20),
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (reservation_id) REFERENCES reservation(id)
);

-- =====================================================
-- 初始数据插入
-- =====================================================

-- 插入管理员 (密码: admin123，明文存储)
INSERT INTO user (username, password, role, create_time, update_time) 
VALUES ('admin', 'admin123', 'ADMIN', NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 插入测试用户
INSERT INTO user (username, password, real_name, phone, email, role, create_time, update_time) VALUES
('testuser1', 'password123', '张三', '13800138001', 'zhangsan@example.com', 'USER', NOW(), NOW()),
('testuser2', 'password123', '李四', '13800138002', 'lisi@example.com', 'USER', NOW(), NOW()),
('testuser3', 'password123', '王五', '13800138003', 'wangwu@example.com', 'USER', NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 插入自习室
INSERT INTO study_room (room_name, capacity, status, description, floor_plan_url, create_time, update_time) VALUES
('静音自习室A', 30, 'OPEN', '适合深度学习，禁止交谈', '/floorplans/room-1.svg', NOW(), NOW()),
('静音自习室B', 25, 'OPEN', '适合深度学习，禁止交谈', '/floorplans/room-2.svg', NOW(), NOW()),
('讨论自习室', 15, 'OPEN', '小声讨论区域，适合小组学习', '/floorplans/room-3.svg', NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 插入公告
INSERT INTO announcement (title, content, type, priority, status, create_time, update_time) VALUES
('欢迎使用自习室预约系统', '亲爱的同学们，欢迎使用我们的自习室预约系统。请遵守自习室规则，保持安静，不要占座。祝您学习愉快！', 'GENERAL', 5, 'ACTIVE', NOW(), NOW()),
('静音区禁止交谈', '请各位同学注意：静音自习室内请保持绝对安静，禁止任何形式的交谈。感谢您的配合！', 'RULE', 4, 'ACTIVE', NOW(), NOW()),
('维护通知', '系统将于本周六凌晨2:00-4:00进行维护升级，期间可能无法正常使用，请提前安排好您的学习计划。', 'MAINTENANCE', 3, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 插入座位 - 自习室A
INSERT INTO seat (room_id, seat_number, price, status, pos_x, pos_y, create_time, update_time) VALUES
(1, 'A001', 2.00, 'AVAILABLE', 20, 25, NOW(), NOW()),
(1, 'A002', 2.00, 'AVAILABLE', 35, 25, NOW(), NOW()),
(1, 'A003', 2.00, 'AVAILABLE', 50, 25, NOW(), NOW()),
(1, 'A004', 2.00, 'AVAILABLE', 65, 25, NOW(), NOW()),
(1, 'A005', 2.00, 'AVAILABLE', 80, 25, NOW(), NOW()),
(1, 'A006', 2.00, 'AVAILABLE', 20, 55, NOW(), NOW()),
(1, 'A007', 2.00, 'AVAILABLE', 35, 55, NOW(), NOW()),
(1, 'A008', 2.00, 'AVAILABLE', 50, 55, NOW(), NOW()),
(1, 'A009', 2.00, 'AVAILABLE', 65, 55, NOW(), NOW()),
(1, 'A010', 2.00, 'AVAILABLE', 80, 55, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 插入座位 - 自习室B
INSERT INTO seat (room_id, seat_number, price, status, pos_x, pos_y, create_time, update_time) VALUES
(2, 'B001', 2.00, 'AVAILABLE', 25, 25, NOW(), NOW()),
(2, 'B002', 2.00, 'AVAILABLE', 40, 25, NOW(), NOW()),
(2, 'B003', 2.00, 'AVAILABLE', 55, 25, NOW(), NOW()),
(2, 'B004', 2.00, 'AVAILABLE', 70, 25, NOW(), NOW()),
(2, 'B005', 2.00, 'AVAILABLE', 25, 55, NOW(), NOW()),
(2, 'B006', 2.00, 'AVAILABLE', 40, 55, NOW(), NOW()),
(2, 'B007', 2.00, 'AVAILABLE', 55, 55, NOW(), NOW()),
(2, 'B008', 2.00, 'AVAILABLE', 70, 55, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 插入座位 - 讨论自习室
INSERT INTO seat (room_id, seat_number, price, status, pos_x, pos_y, create_time, update_time) VALUES
(3, 'D001', 3.00, 'AVAILABLE', 30, 30, NOW(), NOW()),
(3, 'D002', 3.00, 'AVAILABLE', 50, 30, NOW(), NOW()),
(3, 'D003', 3.00, 'AVAILABLE', 70, 30, NOW(), NOW()),
(3, 'D004', 3.00, 'AVAILABLE', 30, 60, NOW(), NOW()),
(3, 'D005', 3.00, 'AVAILABLE', 50, 60, NOW(), NOW()),
(3, 'D006', 3.00, 'AVAILABLE', 70, 60, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 插入一些测试预约记录（用于关联支付）
INSERT INTO reservation (user_id, seat_id, start_time, end_time, total_price, status, create_time, update_time) VALUES
(1, 1, '2024-01-01 10:00:00', '2024-01-01 12:00:00', 6.00, 'COMPLETED', '2024-01-01 09:50:00', '2024-01-01 12:00:00'),
(1, 2, '2024-01-02 14:00:00', '2024-01-02 16:00:00', 4.00, 'COMPLETED', '2024-01-02 13:50:00', '2024-01-02 16:00:00'),
(2, 3, '2024-01-03 09:00:00', '2024-01-03 11:00:00', 8.00, 'COMPLETED', '2024-01-03 08:50:00', '2024-01-03 11:00:00'),
(1, 4, '2024-01-04 16:00:00', '2024-01-04 17:00:00', 5.00, 'COMPLETED', '2024-01-04 15:50:00', '2024-01-04 17:00:00'),
(2, 5, '2024-01-05 11:00:00', '2024-01-05 13:00:00', 7.00, 'COMPLETED', '2024-01-05 10:50:00', '2024-01-05 13:00:00'),
(3, 6, '2024-01-06 13:00:00', '2024-01-06 15:00:00', 9.00, 'COMPLETED', '2024-01-06 12:50:00', '2024-01-06 15:00:00'),
(1, 7, '2024-01-07 15:00:00', '2024-01-07 16:00:00', 3.00, 'COMPLETED', '2024-01-07 14:50:00', '2024-01-07 16:00:00'),
(2, 8, '2024-01-08 10:00:00', '2024-01-08 12:00:00', 6.00, 'COMPLETED', '2024-01-08 09:50:00', '2024-01-08 12:00:00'),
(3, 9, '2024-01-09 14:00:00', '2024-01-09 15:00:00', 4.00, 'COMPLETED', '2024-01-09 13:50:00', '2024-01-09 15:00:00'),
(1, 10, '2024-01-10 09:00:00', '2024-01-10 11:00:00', 8.00, 'COMPLETED', '2024-01-10 08:50:00', '2024-01-10 11:00:00'),
(2, 11, '2024-01-11 16:00:00', '2024-01-11 17:00:00', 5.00, 'COMPLETED', '2024-01-11 15:50:00', '2024-01-11 17:00:00'),
(3, 12, '2024-01-12 12:00:00', '2024-01-12 13:00:00', 7.00, 'COMPLETED', '2024-01-12 11:50:00', '2024-01-12 13:00:00'),
(1, 13, '2024-01-13 15:00:00', '2024-01-13 17:00:00', 9.00, 'COMPLETED', '2024-01-13 14:50:00', '2024-01-13 17:00:00'),
(2, 14, '2024-01-14 10:00:00', '2024-01-14 12:00:00', 6.00, 'COMPLETED', '2024-01-14 09:50:00', '2024-01-14 12:00:00'),
(3, 15, '2024-01-15 13:00:00', '2024-01-15 15:00:00', 8.00, 'COMPLETED', '2024-01-15 12:50:00', '2024-01-15 15:00:00')
ON DUPLICATE KEY UPDATE id=id;

-- =====================================================
-- 9. 支付记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    payment_no VARCHAR(100) NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('ALIPAY', 'WECHAT', 'BALANCE')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    transaction_id VARCHAR(100),
    payment_time DATETIME,
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (reservation_id) REFERENCES reservation(id)
);

-- =====================================================
-- 测试数据 - 支付记录
-- =====================================================

-- 插入测试支付记录
INSERT INTO payment (user_id, reservation_id, payment_no, amount, payment_method, status, transaction_id, payment_time, create_time, update_time) VALUES
-- 支付宝支付记录（已成功）
(1, 1, 'PAY20240101001', 6.00, 'ALIPAY', 'SUCCESS', 'TXN20240101001', '2024-01-01 10:30:00', '2024-01-01 10:25:00', '2024-01-01 10:30:00'),
(1, 2, 'PAY20240101002', 4.00, 'ALIPAY', 'SUCCESS', 'TXN20240101002', '2024-01-02 14:15:00', '2024-01-02 14:10:00', '2024-01-02 14:15:00'),
(2, 3, 'PAY20240101003', 8.00, 'ALIPAY', 'SUCCESS', 'TXN20240101003', '2024-01-03 09:45:00', '2024-01-03 09:40:00', '2024-01-03 09:45:00'),
-- 微信支付记录（已成功）
(1, 4, 'PAY20240101004', 5.00, 'WECHAT', 'SUCCESS', 'TXN20240101004', '2024-01-04 16:20:00', '2024-01-04 16:15:00', '2024-01-04 16:20:00'),
(2, 5, 'PAY20240101005', 7.00, 'WECHAT', 'SUCCESS', 'TXN20240101005', '2024-01-05 11:30:00', '2024-01-05 11:25:00', '2024-01-05 11:30:00'),
(3, 6, 'PAY20240101006', 9.00, 'WECHAT', 'SUCCESS', 'TXN20240101006', '2024-01-06 13:45:00', '2024-01-06 13:40:00', '2024-01-06 13:45:00'),
-- 余额支付记录（已成功）
(1, 7, 'PAY20240101007', 3.00, 'BALANCE', 'SUCCESS', NULL, '2024-01-07 15:10:00', '2024-01-07 15:05:00', '2024-01-07 15:10:00'),
(2, 8, 'PAY20240101008', 6.00, 'BALANCE', 'SUCCESS', NULL, '2024-01-08 10:25:00', '2024-01-08 10:20:00', '2024-01-08 10:25:00'),
(3, 9, 'PAY20240101009', 4.00, 'BALANCE', 'SUCCESS', NULL, '2024-01-09 14:35:00', '2024-01-09 14:30:00', '2024-01-09 14:35:00'),
-- 更多支付宝支付（已成功）
(1, 10, 'PAY20240101010', 8.00, 'ALIPAY', 'SUCCESS', 'TXN20240101010', '2024-01-10 09:15:00', '2024-01-10 09:10:00', '2024-01-10 09:15:00'),
(2, 11, 'PAY20240101011', 5.00, 'ALIPAY', 'SUCCESS', 'TXN20240101011', '2024-01-11 16:40:00', '2024-01-11 16:35:00', '2024-01-11 16:40:00'),
-- 更多微信支付（已成功）
(3, 12, 'PAY20240101012', 7.00, 'WECHAT', 'SUCCESS', 'TXN20240101012', '2024-01-12 12:20:00', '2024-01-12 12:15:00', '2024-01-12 12:20:00'),
(1, 13, 'PAY20240101013', 9.00, 'WECHAT', 'SUCCESS', 'TXN20240101013', '2024-01-13 15:55:00', '2024-01-13 15:50:00', '2024-01-13 15:55:00'),
-- 更多余额支付（已成功）
(2, 14, 'PAY20240101014', 6.00, 'BALANCE', 'SUCCESS', NULL, '2024-01-14 10:45:00', '2024-01-14 10:40:00', '2024-01-14 10:45:00'),
(3, 15, 'PAY20240101015', 8.00, 'BALANCE', 'SUCCESS', NULL, '2024-01-15 13:20:00', '2024-01-15 13:15:00', '2024-01-15 13:20:00')
ON DUPLICATE KEY UPDATE id=id;

-- =====================================================
-- 10. 用户余额表
-- =====================================================
CREATE TABLE IF NOT EXISTS user_balance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    frozen_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_recharge DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 插入用户余额初始数据
INSERT INTO user_balance (user_id, balance, frozen_balance, total_recharge, create_time, update_time) VALUES
(1, 100.00, 0.00, 100.00, NOW(), NOW()),
(2, 150.00, 0.00, 150.00, NOW(), NOW()),
(3, 80.00, 0.00, 80.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- =====================================================
-- 11. 充值记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS recharge_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('ALIPAY', 'WECHAT')),
    payment_status VARCHAR(20) NOT NULL CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED')),
    transaction_id VARCHAR(100),
    create_time DATETIME,
    update_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 开启事件调度器
SET GLOBAL event_scheduler = ON;
-- 设置时区
SET GLOBAL time_zone = '+8:00';

-- 创建事件：每30秒检查过期预约
DELIMITER //
CREATE EVENT check_expired_reservations
ON SCHEDULE EVERY 30 SECOND
DO
BEGIN
    -- 更新预约状态：已确认且应该开始的预约
    UPDATE reservation r
    SET r.status = 'IN_USE', r.update_time = NOW()
    WHERE r.status = 'CONFIRMED' AND r.start_time <= NOW();
    
    -- 更新过期预约状态（只处理已确认/使用中的预约）
    UPDATE reservation r
    SET r.status = 'COMPLETED', r.update_time = NOW()
    WHERE r.status IN ('CONFIRMED', 'IN_USE') AND r.end_time < NOW();
    
    -- 更新座位状态：预约结束后恢复可用
    UPDATE seat s
    SET s.status = 'AVAILABLE', s.update_time = NOW()
    WHERE s.id IN (
        SELECT r.seat_id FROM reservation r 
        WHERE r.status = 'COMPLETED' AND r.end_time < NOW()
    ) AND s.status = 'IN_USE';
    
    -- 更新座位状态：预约开始时设为使用中
    UPDATE seat s
    SET s.status = 'IN_USE', s.update_time = NOW()
    WHERE s.id IN (
        SELECT r.seat_id FROM reservation r 
        WHERE r.status = 'IN_USE' AND r.start_time <= NOW()
    ) AND s.status = 'UNAVAILABLE';
    
    -- 为刚刚结束的预约发送通知（修复：使用正确的时间判断）
    INSERT INTO notification (user_id, type, title, content, is_read, related_id, related_type, create_time, update_time)
    SELECT 
        r.user_id, 
        'RESERVATION_EXPIRED', 
        '预约已结束', 
        CONCAT('您的预约已结束，座位编号：', (SELECT seat_number FROM seat WHERE id = r.seat_id)), 
        FALSE, 
        r.id, 
        'RESERVATION', 
        NOW(), 
        NOW()
    FROM reservation r
    WHERE r.status = 'COMPLETED'
    AND r.end_time BETWEEN DATE_SUB(NOW(), INTERVAL 30 SECOND) AND NOW()
    AND NOT EXISTS (
        SELECT 1 FROM notification n 
        WHERE n.related_id = r.id 
        AND n.related_type = 'RESERVATION' 
        AND n.type = 'RESERVATION_EXPIRED'
    );
    
    -- 为刚刚开始的预约发送通知（修复：使用正确的时间判断）
    INSERT INTO notification (user_id, type, title, content, is_read, related_id, related_type, create_time, update_time)
    SELECT 
        r.user_id, 
        'RESERVATION_STARTED', 
        '预约已开始', 
        CONCAT('您的预约已开始，请前往座位：', (SELECT seat_number FROM seat WHERE id = r.seat_id)), 
        FALSE, 
        r.id, 
        'RESERVATION', 
        NOW(), 
        NOW()
    FROM reservation r
    WHERE r.status = 'IN_USE'
    AND r.start_time BETWEEN DATE_SUB(NOW(), INTERVAL 30 SECOND) AND NOW()
    AND NOT EXISTS (
        SELECT 1 FROM notification n 
        WHERE n.related_id = r.id 
        AND n.related_type = 'RESERVATION' 
        AND n.type = 'RESERVATION_STARTED'
    );
END //
DELIMITER ;

-- 创建触发器：插入座位时自动生成座位号
DELIMITER //
CREATE TRIGGER seat_insert_trigger
BEFORE INSERT ON seat
FOR EACH ROW
BEGIN
    DECLARE seat_count INT;
    -- 获取当前房间的座位数量
    SELECT COUNT(*) INTO seat_count 
    FROM seat 
    WHERE room_id = NEW.room_id;
    
    -- 设置新的座位号
    SET NEW.seat_number = CONCAT(NEW.room_id, '-', LPAD(seat_count + 1, 3, '0'));
END //
DELIMITER ;

-- =====================================================
-- 索引优化 - 为数据分析查询添加索引
-- =====================================================
-- 数据库索引优化
-- =====================================================

-- 用户表索引
CREATE INDEX idx_user_create_time ON user(create_time);
CREATE INDEX idx_user_role ON user(role);

-- 预约表索引 - 为时间范围查询优化
CREATE INDEX idx_reservation_start_time ON reservation(start_time);
CREATE INDEX idx_reservation_end_time ON reservation(end_time);
CREATE INDEX idx_reservation_user_id ON reservation(user_id);
CREATE INDEX idx_reservation_seat_id ON reservation(seat_id);
CREATE INDEX idx_reservation_status ON reservation(status);
CREATE INDEX idx_reservation_time_range ON reservation(start_time, end_time);

-- 座位表索引
CREATE INDEX idx_seat_room_id ON seat(room_id);
CREATE INDEX idx_seat_status ON seat(status);

-- 自习室表索引
CREATE INDEX idx_study_room_status ON study_room(status);

-- 通知表索引
CREATE INDEX idx_notification_user_id ON notification(user_id);
CREATE INDEX idx_notification_is_read ON notification(is_read);
CREATE INDEX idx_notification_create_time ON notification(create_time);

-- 支付表索引
CREATE INDEX idx_payment_user_id ON payment(user_id);
CREATE INDEX idx_payment_reservation_id ON payment(reservation_id);
CREATE INDEX idx_payment_status ON payment(status);

-- =====================================================
-- 数据完整性检查
-- =====================================================

-- 确保所有表都有正确的约束
ALTER TABLE user 
MODIFY COLUMN role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN'));

ALTER TABLE seat 
MODIFY COLUMN status VARCHAR(20) CHECK (status IN ('AVAILABLE', 'UNAVAILABLE'));

ALTER TABLE reservation 
MODIFY COLUMN status VARCHAR(20) CHECK (status IN ('PENDING', 'CONFIRMED', 'IN_USE', 'COMPLETED', 'CANCELLED'));

ALTER TABLE study_room 
MODIFY COLUMN status VARCHAR(20) CHECK (status IN ('OPEN', 'CLOSED', 'MAINTENANCE'));

-- =====================================================
-- 初始化完成提示
-- =====================================================
SELECT '数据库初始化完成！' AS message;
