-- CAT201 电商网站项目数据库设计
-- 创建数据库
CREATE DATABASE IF NOT EXISTS eshop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eshop_db;

-- 1. 用户表 (customers)
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- 2. 管理员表 (admins)
CREATE TABLE admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- 3. 商品分类表 (categories)
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT NULL,
    image_url VARCHAR(500),
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- 4. 商品表 (products)
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    sku VARCHAR(100) UNIQUE NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    cost_price DECIMAL(10,2),
    stock_quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT DEFAULT 10,
    category_id BIGINT,
    image_url VARCHAR(500),
    images JSON, -- 存储多张图片URL
    weight DECIMAL(8,2),
    dimensions JSON, -- 存储长宽高 {length, width, height}
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_name (name),
    INDEX idx_category (category_id),
    INDEX idx_price (price),
    INDEX idx_sku (sku)
);

-- 5. 收货地址表 (addresses)
CREATE TABLE addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    company VARCHAR(100),
    address_line1 VARCHAR(200) NOT NULL,
    address_line2 VARCHAR(200),
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50),
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- 6. 购物车表 (shopping_cart)
CREATE TABLE shopping_cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_item (customer_id, product_id)
);

-- 7. 订单表 (orders)
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    shipping_fee DECIMAL(10,2) DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'MYR',
    
    -- 收货地址信息
    shipping_first_name VARCHAR(50) NOT NULL,
    shipping_last_name VARCHAR(50) NOT NULL,
    shipping_company VARCHAR(100),
    shipping_address_line1 VARCHAR(200) NOT NULL,
    shipping_address_line2 VARCHAR(200),
    shipping_city VARCHAR(50) NOT NULL,
    shipping_state VARCHAR(50),
    shipping_postal_code VARCHAR(20) NOT NULL,
    shipping_country VARCHAR(50) NOT NULL,
    shipping_phone VARCHAR(20),
    
    -- 配送信息
    shipping_method VARCHAR(50),
    tracking_number VARCHAR(100),
    
    -- 支付信息
    payment_method VARCHAR(50),
    payment_status ENUM('PENDING', 'PAID', 'REFUNDED') DEFAULT 'PENDING',
    paid_at TIMESTAMP NULL,
    
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    shipped_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    INDEX idx_order_number (order_number),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- 8. 订单明细表 (order_items)
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_sku VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

-- 9. 商品库存记录表 (inventory_logs)
CREATE TABLE inventory_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    change_type ENUM('IN', 'OUT', 'ADJUSTMENT') NOT NULL,
    quantity_change INT NOT NULL,
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    reason VARCHAR(200),
    reference_type VARCHAR(50), -- ORDER, PURCHASE, ADJUSTMENT等
    reference_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product (product_id),
    INDEX idx_created_at (created_at)
);

-- 10. 系统配置表 (system_settings)
CREATE TABLE system_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    description VARCHAR(500),
    setting_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 11. 会话表 (user_sessions) - 可选，用于会话管理
CREATE TABLE user_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(255) UNIQUE NOT NULL,
    user_type ENUM('CUSTOMER', 'ADMIN') NOT NULL,
    user_id BIGINT NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_session_id (session_id),
    INDEX idx_user (user_type, user_id),
    INDEX idx_expires (expires_at)
);

-- 插入初始数据

-- 插入默认管理员账户 (密码: admin123)
INSERT INTO admins (username, email, password_hash, first_name, last_name) VALUES 
('admin', 'admin@eshop.com', '$2a$10$9V2kVjvJkL2V8K1Z8HjOeOXJJVLQFKkO/KYcQb8jz2Y7F2mQXt8vW', 'System', 'Administrator');

-- 插入商品分类
INSERT INTO categories (name, description, sort_order) VALUES 
('Electronics', 'Electronic devices and gadgets', 1),
('Clothing', 'Fashion and apparel', 2),
('Books', 'Books and publications', 3),
('Home & Garden', 'Home improvement and garden supplies', 4),
('Sports', 'Sports equipment and accessories', 5);

-- 插入子分类
INSERT INTO categories (name, description, parent_id, sort_order) VALUES 
('Smartphones', 'Mobile phones and accessories', 1, 1),
('Laptops', 'Laptop computers', 1, 2),
('Men''s Clothing', 'Clothing for men', 2, 1),
('Women''s Clothing', 'Clothing for women', 2, 2),
('Fiction', 'Fiction books', 3, 1),
('Non-Fiction', 'Non-fiction books', 3, 2);

-- 插入示例商品
INSERT INTO products (name, description, short_description, sku, price, stock_quantity, category_id, image_url, is_active, is_featured) VALUES 
('iPhone 15 Pro', 'Latest iPhone with advanced camera system and A17 Pro chip', 'Premium smartphone with titanium design', 'IPHONE15PRO001', 4999.00, 50, 6, '/images/iphone15pro.jpg', TRUE, TRUE),
('MacBook Air M2', 'Lightweight laptop with M2 chip and all-day battery life', 'Powerful and portable laptop', 'MACBOOKAIRM2001', 5999.00, 30, 7, '/images/macbookair.jpg', TRUE, TRUE),
('Men''s T-Shirt', 'Comfortable cotton t-shirt in various colors', 'Classic casual wear', 'MENS-TS001', 49.90, 100, 8, '/images/men-tshirt.jpg', TRUE, FALSE),
('Women''s Dress', 'Elegant dress suitable for various occasions', 'Stylish formal wear', 'WOMENS-DRESS001', 129.90, 50, 9, '/images/women-dress.jpg', TRUE, FALSE),
('JavaScript: The Good Parts', 'Comprehensive guide to JavaScript programming', 'Programming book', 'BOOK-JS001', 89.90, 200, 10, '/images/js-book.jpg', TRUE, FALSE),
('Yoga Mat', 'High-quality non-slip yoga mat', 'Exercise equipment', 'YOGA-MAT001', 79.90, 80, 11, '/images/yoga-mat.jpg', TRUE, FALSE);

-- 插入系统配置
INSERT INTO system_settings (setting_key, setting_value, description, setting_type) VALUES 
('site_name', 'CAT201 Eshop', 'Website name', 'STRING'),
('site_description', 'CAT201 E-commerce Project', 'Website description', 'STRING'),
('currency', 'MYR', 'Default currency', 'STRING'),
('tax_rate', '0.06', 'Tax rate (6%)', 'NUMBER'),
('free_shipping_threshold', '200.00', 'Free shipping minimum amount', 'NUMBER'),
('default_shipping_fee', '10.00', 'Default shipping fee', 'NUMBER'),
('max_login_attempts', '5', 'Maximum login attempts before lockout', 'NUMBER'),
('session_timeout', '30', 'Session timeout in minutes', 'NUMBER');

-- 创建视图

-- 商品详情视图
CREATE VIEW product_details AS
SELECT 
    p.id,
    p.name,
    p.description,
    p.short_description,
    p.sku,
    p.price,
    p.cost_price,
    p.stock_quantity,
    c.name AS category_name,
    p.image_url,
    p.images,
    p.is_active,
    p.is_featured,
    p.created_at,
    p.updated_at
FROM products p
LEFT JOIN categories c ON p.category_id = c.id;

-- 订单详情视图
CREATE VIEW order_details AS
SELECT 
    o.id,
    o.order_number,
    o.customer_id,
    cu.username AS customer_username,
    cu.email AS customer_email,
    o.status,
    o.subtotal,
    o.tax_amount,
    o.shipping_fee,
    o.discount_amount,
    o.total_amount,
    o.payment_method,
    o.payment_status,
    o.shipping_method,
    o.tracking_number,
    o.created_at,
    o.updated_at
FROM orders o
INNER JOIN customers cu ON o.customer_id = cu.id;

-- 创建存储过程

-- 更新商品库存的存储过程
DELIMITER //
CREATE PROCEDURE UpdateProductStock(
    IN p_product_id BIGINT,
    IN p_quantity_change INT,
    IN p_change_type VARCHAR(20),
    IN p_reason VARCHAR(200),
    IN p_reference_type VARCHAR(50),
    IN p_reference_id BIGINT,
    IN p_created_by VARCHAR(50)
)
BEGIN
    DECLARE v_current_stock INT;
    
    -- 获取当前库存
    SELECT stock_quantity INTO v_current_stock 
    FROM products 
    WHERE id = p_product_id;
    
    -- 检查库存是否足够
    IF p_change_type = 'OUT' AND v_current_stock < p_quantity_change THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Insufficient stock';
    END IF;
    
    -- 更新库存
    UPDATE products 
    SET stock_quantity = stock_quantity + p_quantity_change,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_product_id;
    
    -- 记录库存变更日志
    INSERT INTO inventory_logs (
        product_id, 
        change_type, 
        quantity_change, 
        quantity_before, 
        quantity_after, 
        reason, 
        reference_type, 
        reference_id, 
        created_by
    ) VALUES (
        p_product_id,
        p_change_type,
        p_quantity_change,
        v_current_stock,
        v_current_stock + p_quantity_change,
        p_reason,
        p_reference_type,
        p_reference_id,
        p_created_by
    );
    
END //
DELIMITER ;

-- 创建触发器

-- 商品库存变更触发器
DELIMITER //
CREATE TRIGGER before_product_update 
BEFORE UPDATE ON products 
FOR EACH ROW
BEGIN
    IF OLD.stock_quantity != NEW.stock_quantity THEN
        INSERT INTO inventory_logs (
            product_id,
            change_type,
            quantity_change,
            quantity_before,
            quantity_after,
            reason,
            created_by
        ) VALUES (
            NEW.id,
            'ADJUSTMENT',
            NEW.stock_quantity - OLD.stock_quantity,
            OLD.stock_quantity,
            NEW.stock_quantity,
            'Manual adjustment',
            'SYSTEM'
        );
    END IF;
END //
DELIMITER ;

-- 创建索引优化查询性能
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_username ON customers(username);
CREATE INDEX idx_orders_customer_status ON orders(customer_id, status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_shopping_cart_customer ON shopping_cart(customer_id);
CREATE INDEX idx_products_category_active ON products(category_id, is_active);

-- 添加外键约束确保数据完整性
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_product 
FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT;

-- 完成数据库初始化
SELECT 'Database initialization completed successfully!' AS message;