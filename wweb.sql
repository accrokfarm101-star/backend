`-- 1. Bảng Người dùng [cite: 848, 863]
create database web_ban_hang;
use web_ban_hang;

-- 1. Bảng Người dùng (Lưu ý: USER là từ khóa hệ thống trong SQL Server nên phải bọc trong ngoặc vuông)
CREATE TABLE [USER] (
    User_ID INT IDENTITY(1,1) PRIMARY KEY,
    Full_Name NVARCHAR(255) NOT NULL,
    Phone VARCHAR(15) UNIQUE NOT NULL,
    Email VARCHAR(255) UNIQUE NOT NULL,
    Password_Hash VARCHAR(255) NOT NULL,
    Role VARCHAR(20) DEFAULT 'Customer' CHECK (Role IN ('Customer', 'Admin', 'Staff')),
    Status VARCHAR(20) DEFAULT 'Active' CHECK (Status IN ('Active', 'Locked'))
);

-- 2. Bảng Sổ địa chỉ
CREATE TABLE ADDRESS_BOOK (
    Address_ID INT IDENTITY(1,1) PRIMARY KEY,
    User_ID INT NOT NULL,
    Province NVARCHAR(100) NOT NULL,
    District NVARCHAR(100) NOT NULL,
    Detail_Address NVARCHAR(MAX) NOT NULL,
    Is_Default BIT DEFAULT 0, -- Dùng BIT thay cho BOOLEAN (0 là False, 1 là True)
    FOREIGN KEY (User_ID) REFERENCES [USER](User_ID)
);

-- 3. Bảng Khuyến mãi
CREATE TABLE PROMOTIONS (
    Promotion_ID INT IDENTITY(1,1) PRIMARY KEY,
    Code VARCHAR(50) UNIQUE NOT NULL,
    Discount_Value DECIMAL(15,2) NOT NULL,
    Min_Order_Condition DECIMAL(15,2) DEFAULT 0,
    Start_Date DATETIME,
    End_Date DATETIME
);
-- 4. Bảng Sản phẩm gốc [cite: 848, 863]
CREATE TABLE PRODUCT (
    Product_ID INT IDENTITY(1,1) PRIMARY KEY,
    Category_ID INT, -- Có thể tách thêm bảng Category nếu cần
    Product_Name VARCHAR(255) NOT NULL,
    Price DECIMAL(15,2) NOT NULL,
    Unit VARCHAR(50) NOT NULL, -- kg, hộp, túi [cite: 848]
    Description TEXT,
    Image VARCHAR(255),
    Average_Rating DECIMAL(3,2) DEFAULT 0.00
);
-- 5. Bảng Lô hàng (Phục vụ truy xuất nguồn gốc) [cite: 848, 850]
CREATE TABLE PRODUCT_BATCH (
    Batch_ID INT IDENTITY(1,1) PRIMARY KEY,
    Product_ID INT NOT NULL,
    Planting_Date DATE,
    Harvest_Date DATE,
    Farm_Zone VARCHAR(100),
    Status VARCHAR(20) 
        CHECK (Status IN ('Planting', 'Harvested', 'Active', 'Inactive'))
        DEFAULT 'Planting',
    FOREIGN KEY (Product_ID) REFERENCES PRODUCT(Product_ID)
);
-- 6. Bảng Nhật ký canh tác (Timeline) [cite: 848, 851]
CREATE TABLE FARMING_LOG (
    Log_ID INT  IDENTITY(1,1) PRIMARY KEY,
    Batch_ID INT NOT NULL,
    Action_Date DATETIME NOT NULL,
    Task_Title VARCHAR(255) NOT NULL, -- Bón phân, Tưới nước... [cite: 848]
    Description TEXT,
    Evidence_Image VARCHAR(255),
    FOREIGN KEY (Batch_ID) REFERENCES PRODUCT_BATCH(Batch_ID)
);
CREATE TABLE INVENTORY (
    Inventory_ID INT IDENTITY(1,1) PRIMARY KEY,
    Batch_ID INT NOT NULL UNIQUE,
    Quantity DECIMAL(15,2) NOT NULL DEFAULT 0,
    Expiry_Date DATE,
    Stock_Status VARCHAR(20)
        CHECK (Stock_Status IN ('In_Stock', 'Low_Stock', 'Out_Of_Stock'))
        DEFAULT 'In_Stock',
    FOREIGN KEY (Batch_ID) REFERENCES PRODUCT_BATCH(Batch_ID)
);
ALTER TABLE INVENTORY
ADD CONSTRAINT CHK_Inventory_Quantity CHECK (Quantity >= 0);
CREATE TABLE [ORDER] (
    Order_ID INT IDENTITY(1,1) PRIMARY KEY,
    User_ID INT NOT NULL,
    Promotion_ID INT,
    Address_ID INT NOT NULL,
    Order_Date DATETIME DEFAULT GETDATE(),
    Total_Amount DECIMAL(15,2) NOT NULL,
    Payment_Method VARCHAR(20)
        CHECK (Payment_Method IN ('COD', 'Online'))
        NOT NULL,
    Status VARCHAR(30)
        CHECK (Status IN (
            'Pending_Payment',
            'Pending_Confirm',
            'Shipping',
            'Completed',
            'Cancelled',
            'Refunding',
            'Refunded',
            'Rejected'
        ))
        DEFAULT 'Pending_Confirm',
    FOREIGN KEY (User_ID) REFERENCES [USER](User_ID),
    FOREIGN KEY (Promotion_ID) REFERENCES PROMOTIONS(Promotion_ID),
    FOREIGN KEY (Address_ID) REFERENCES ADDRESS_BOOK(Address_ID)
);
CREATE TABLE ORDER_DETAIL (
    Detail_ID INT IDENTITY(1,1) PRIMARY KEY,
    Order_ID INT NOT NULL,
    Product_ID INT NOT NULL,
    Batch_ID INT NOT NULL,
    Quantity DECIMAL(15,2) NOT NULL,
    Unit_Price DECIMAL(15,2) NOT NULL,
    Is_Reviewed BIT DEFAULT 0,
    FOREIGN KEY (Order_ID) REFERENCES [ORDER](Order_ID),
    FOREIGN KEY (Product_ID) REFERENCES PRODUCT(Product_ID),
    FOREIGN KEY (Batch_ID) REFERENCES PRODUCT_BATCH(Batch_ID)
);
ALTER TABLE ORDER_DETAIL
ADD Line_Total AS (Quantity * Unit_Price);

CREATE TABLE PRODUCT_REVIEWS (
    Review_ID INT IDENTITY(1,1) PRIMARY KEY,
    Order_Detail_ID INT UNIQUE NOT NULL,
    Rating_Stars INT
        CHECK (Rating_Stars BETWEEN 1 AND 5),
    Comment VARCHAR(MAX),
    Admin_Reply VARCHAR(MAX),
    Status VARCHAR(20)
        CHECK (Status IN ('Pending', 'Public', 'Hidden'))
        DEFAULT 'Pending',
    FOREIGN KEY (Order_Detail_ID)
        REFERENCES ORDER_DETAIL(Detail_ID)
);
-- 12. Bảng Yêu cầu trả hàng / Khiếu nại [cite: 848, 863]
CREATE TABLE RETURN_REQUESTS (
    Request_ID INT IDENTITY(1,1) PRIMARY KEY,
    Order_ID INT UNIQUE NOT NULL, -- Một đơn hàng thường chỉ có 1 luồng khiếu nại
    Reason NVARCHAR(MAX) NOT NULL,
    Evidence_Image VARCHAR(255),
    Status VARCHAR(20) DEFAULT 'Pending' 
        CHECK (Status IN ('Pending', 'Approved', 'Rejected')),
    FOREIGN KEY (Order_ID) REFERENCES [ORDER](Order_ID)
);
-- Bảng Danh mục sản phẩm
CREATE TABLE CATEGORY (
    Category_ID INT IDENTITY(1,1) PRIMARY KEY,
    Category_Name NVARCHAR(100) NOT NULL,
    Description NVARCHAR(255)
);
-- Khi đó, bảng PRODUCT của bạn thêm dòng này để nối khóa ngoại:
-- FOREIGN KEY (Category_ID) REFERENCES CATEGORY(Category_ID)
-- Trigger cập nhật trạng thái đã đánh giá trong chi tiết đơn hàng
GO
CREATE TRIGGER trg_UpdateIsReviewed
ON PRODUCT_REVIEWS
AFTER INSERT
AS
BEGIN
    UPDATE od
    SET od.Is_Reviewed = 1
    FROM ORDER_DETAIL od
    INNER JOIN inserted i ON od.Detail_ID = i.Order_Detail_ID;
END;
GO

-- A. Trigger tự động trừ tồn kho khi có đơn hàng mới
GO
CREATE TRIGGER trg_UpdateStockAfterOrder
ON ORDER_DETAIL
AFTER INSERT
AS
BEGIN
    UPDATE i
    SET i.Quantity = i.Quantity - ins.Quantity
    FROM INVENTORY i
    INNER JOIN inserted ins ON i.Batch_ID = ins.Batch_ID;
END;
GO
-- B. Trigger tự động CỘNG LẠI tồn kho khi Hủy đơn hàng
CREATE TRIGGER trg_RestoreStockOnCancel
ON [ORDER] -- ORDER là từ khóa hệ thống nên phải bọc trong ngoặc vuông
AFTER UPDATE
AS
BEGIN
    -- Chỉ chạy khi cột Status bị thay đổi
    IF UPDATE(Status)
    BEGIN
        UPDATE i
        SET i.Quantity = i.Quantity + od.Quantity
        FROM INVENTORY i
        INNER JOIN ORDER_DETAIL od ON i.Batch_ID = od.Batch_ID
        INNER JOIN inserted ins ON od.Order_ID = ins.Order_ID
        INNER JOIN deleted del ON ins.Order_ID = del.Order_ID
        -- Nếu trạng thái mới là Cancelled/Refunding và trạng thái cũ không phải
        WHERE (ins.Status = 'Cancelled' AND del.Status != 'Cancelled')
           OR (ins.Status = 'Refunding' AND del.Status != 'Refunding');
    END
END;
GO
-- Đoạn lệnh này sẽ được cấu hình chạy tự động trong SQL Server Agent
UPDATE [ORDER]
SET Status = 'Cancelled'
WHERE Status = 'Pending_Payment' 
  AND Payment_Method = 'Online'
  AND DATEDIFF(MINUTE, Order_Date, GETDATE()) >= 15;
-- C. Trigger tự động cập nhật Trạng thái kho (Low_Stock, Out_Of_Stock)
CREATE TRIGGER trg_UpdateStockStatus
ON INVENTORY
AFTER UPDATE
AS
BEGIN
    IF UPDATE(Quantity)
    BEGIN
        UPDATE i
        SET i.Stock_Status = 
            CASE 
                WHEN ins.Quantity <= 0 THEN 'Out_Of_Stock'
                WHEN ins.Quantity < 10 THEN 'Low_Stock'
                ELSE 'In_Stock'
            END
        FROM INVENTORY i
        INNER JOIN inserted ins ON i.Inventory_ID = ins.Inventory_ID;
    END
END;
GO
-- A. Bảng ảo Truy xuất nguồn gốc
GO
CREATE VIEW vw_ProductTraceability AS
SELECT 
    p.Product_ID, 
    p.Product_Name, 
    pb.Batch_ID, 
    pb.Farm_Zone, 
    fl.Action_Date, 
    fl.Task_Title, 
    fl.Description, 
    fl.Evidence_Image
FROM PRODUCT p
INNER JOIN PRODUCT_BATCH pb ON p.Product_ID = pb.Product_ID
INNER JOIN FARMING_LOG fl ON pb.Batch_ID = fl.Batch_ID;
GO
-- B. Bảng ảo Cảnh báo tồn kho & Cận Date cho Dashboard
CREATE VIEW vw_AdminDashboard_Alerts AS
SELECT 
    p.Product_Name,
    pb.Batch_ID,
    i.Quantity,
    i.Stock_Status,
    i.Expiry_Date,
    DATEDIFF(day, GETDATE(), i.Expiry_Date) AS Days_Until_Expiry
FROM INVENTORY i
INNER JOIN PRODUCT_BATCH pb ON i.Batch_ID = pb.Batch_ID
INNER JOIN PRODUCT p ON pb.Product_ID = p.Product_ID
WHERE i.Quantity <= 10 OR DATEDIFF(day, GETDATE(), i.Expiry_Date) <= 3;
GO
--Giao dịch tạo đơn hàng
GO
CREATE PROCEDURE sp_CheckoutOrder (
    @p_User_ID INT,
    @p_Address_ID INT,
    @p_Payment_Method VARCHAR(20),
    @p_Total_Amount DECIMAL(15,2),
    @p_Result VARCHAR(50) OUTPUT -- Biến trả về kết quả
)
AS
BEGIN
    BEGIN TRY
        -- Bắt đầu giao dịch
        BEGIN TRANSACTION;
        -- 1. Tạo đơn hàng mới
        INSERT INTO [ORDER] (User_ID, Address_ID, Total_Amount, Payment_Method, Status)
        VALUES (@p_User_ID, @p_Address_ID, @p_Total_Amount, @p_Payment_Method, 'Pending_Confirm');
        -- Lấy ID của đơn hàng vừa tạo 
        DECLARE @new_order_id INT = SCOPE_IDENTITY()
        -- 2. Code Backend sẽ tiếp tục gọi insert vào bảng ORDER_DETAIL ở đây
        -- 3. Lưu lại mọi thay đổi
        COMMIT TRANSACTION;
        SET @p_Result = 'Order Created Successfully';
    END TRY
    BEGIN CATCH
        -- Nếu có bất kỳ lỗi gì, hoàn tác toàn bộ
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
            
        SET @p_Result = 'Transaction Failed - Rolled Back';
    END CATCH
END;
GO
--cập nhật Điểm trung bình Sản phẩm
GO
CREATE TRIGGER trg_UpdateProductRating
ON PRODUCT_REVIEWS
AFTER INSERT, UPDATE
AS
BEGIN
    -- Chỉ chạy lệnh cập nhật nếu có bài đánh giá mang trạng thái 'Public' (Đã duyệt)
    IF EXISTS (SELECT 1 FROM inserted WHERE Status = 'Public')
    BEGIN
        UPDATE p
        SET p.Average_Rating = (
            -- Tính trung bình cộng số sao, nếu chưa có đánh giá nào thì trả về 0.00
            SELECT ISNULL(AVG(CAST(pr.Rating_Stars AS DECIMAL(3,2))), 0.00)
            FROM PRODUCT_REVIEWS pr
            INNER JOIN ORDER_DETAIL od_inner ON pr.Order_Detail_ID = od_inner.Detail_ID
            WHERE od_inner.Product_ID = p.Product_ID AND pr.Status = 'Public'
        )
        FROM PRODUCT p
        -- JOIN để tìm ra Product_ID từ Order_Detail_ID của bài đánh giá vừa được thêm/sửa
        INNER JOIN ORDER_DETAIL od ON p.Product_ID = od.Product_ID
        INNER JOIN inserted i ON od.Detail_ID = i.Order_Detail_ID
        WHERE i.Status = 'Public';
    END
END;
GO
--nhap du lieu 
INSERT INTO [USER] (Full_Name, Phone, Email, Password_Hash, Role, Status)
VALUES
(N'Nguyễn Văn An', '0901111111', 'an@gmail.com', 'hash1', 'Customer', 'Active'),
(N'Trần Thị Bình', '0902222222', 'binh@gmail.com', 'hash2', 'Customer', 'Active'),
(N'Lê Minh Cường', '0903333333', 'cuong@gmail.com', 'hash3', 'Customer', 'Active'),
(N'Phạm Hoàng Duy', '0904444444', 'duy@gmail.com', 'hash4', 'Staff', 'Active'),
(N'Đỗ Thị Em', '0905555555', 'em@gmail.com', 'hash5', 'Customer', 'Locked'),
(N'Hoàng Quốc Việt', '0906666666', 'viet@gmail.com', 'hash6', 'Admin', 'Active'),
(N'Ngô Thanh Tùng', '0907777777', 'tung@gmail.com', 'hash7', 'Customer', 'Active'),
(N'Vũ Khánh Linh', '0908888888', 'linh@gmail.com', 'hash8', 'Customer', 'Active');

INSERT INTO ADDRESS_BOOK (User_ID, Province, District, Detail_Address, Is_Default)
VALUES
(1, N'Hà Nội', N'Cầu Giấy', N'12 Nguyễn Văn Huyên', 1),
(2, N'Hồ Chí Minh', N'Quận 1', N'45 Lê Lợi', 1),
(3, N'Đà Nẵng', N'Hải Châu', N'78 Trần Phú', 1),
(4, N'Hà Nội', N'Đống Đa', N'22 Chùa Láng', 1),
(5, N'Hải Phòng', N'Lê Chân', N'90 Hai Bà Trưng', 1),
(6, N'Cần Thơ', N'Ninh Kiều', N'15 Nguyễn Trãi', 1),
(7, N'Hà Nội', N'Hoàn Kiếm', N'55 Hàng Bông', 1),
(8, N'Bắc Ninh', N'Từ Sơn', N'101 Lý Thái Tổ', 1);

INSERT INTO PROMOTIONS (Code, Discount_Value, Min_Order_Condition, Start_Date, End_Date)
VALUES
('SALE10', 10, 100000, '2026-01-01', '2026-12-31'),
('FREESHIP', 30000, 200000, '2026-01-01', '2026-12-31'),
('NEWUSER', 15, 150000, '2026-01-01', '2026-12-31'),
('TET2026', 20, 300000, '2026-01-01', '2026-02-28');

INSERT INTO PRODUCT (Category_ID, Product_Name, Price, Unit, Description, Image)
VALUES
(1, N'Rau Cải Xanh', 25000, N'kg', N'Rau sạch hữu cơ', 'raucai.jpg'),
(1, N'Cà Chua', 30000, N'kg', N'Cà chua Đà Lạt', 'cachua.jpg'),
(2, N'Cam Sành', 45000, N'kg', N'Cam ngọt tự nhiên', 'cam.jpg'),
(2, N'Xoài Cát', 60000, N'kg', N'Xoài miền Tây', 'xoai.jpg'),
(3, N'Gạo ST25', 180000, N'bao', N'Gạo ngon xuất khẩu', 'gao.jpg'),
(3, N'Khoai Tây', 35000, N'kg', N'Khoai tươi Đà Lạt', 'khoaitay.jpg'),
(1, N'Rau Muống', 20000, N'kg', N'Rau sạch VietGAP', 'raumuong.jpg'),
(2, N'Thanh Long', 50000, N'kg', N'Thanh long Bình Thuận', 'thanhlong.jpg');

INSERT INTO PRODUCT_BATCH (Product_ID, Planting_Date, Harvest_Date, Farm_Zone, Status)
VALUES
(1, '2026-01-01', '2026-02-15', 'Farm A', 'Active'),
(2, '2026-01-05', '2026-02-20', 'Farm B', 'Active'),
(3, '2026-01-10', '2026-03-01', 'Farm C', 'Harvested'),
(4, '2026-01-15', '2026-03-10', 'Farm D', 'Active'),
(5, '2026-01-20', '2026-04-01', 'Farm E', 'Active'),
(6, '2026-02-01', '2026-03-15', 'Farm F', 'Active'),
(7, '2026-02-10', '2026-03-20', 'Farm G', 'Planting'),
(8, '2026-02-15', '2026-03-25', 'Farm H', 'Harvested');

INSERT INTO FARMING_LOG (Batch_ID, Action_Date, Task_Title, Description, Evidence_Image)
VALUES
(1, GETDATE(), N'Tưới nước', N'Tưới lần 1', 'img1.jpg'),
(1, GETDATE(), N'Bón phân', N'Bón phân hữu cơ', 'img2.jpg'),
(2, GETDATE(), N'Phun thuốc sinh học', N'Phòng sâu bệnh', 'img3.jpg'),
(3, GETDATE(), N'Làm cỏ', N'Vệ sinh ruộng', 'img4.jpg'),
(4, GETDATE(), N'Kiểm tra đất', N'Đo độ ẩm đất', 'img5.jpg'),
(5, GETDATE(), N'Thu hoạch', N'Thu hoạch đợt 1', 'img6.jpg'),
(6, GETDATE(), N'Bón phân', N'Bón kali', 'img7.jpg'),
(7, GETDATE(), N'Tưới nước', N'Tưới sáng', 'img8.jpg');

INSERT INTO INVENTORY (Batch_ID, Quantity, Expiry_Date, Stock_Status)
VALUES
(1, 100, '2026-06-01', 'In_Stock'),
(2, 8, '2026-05-20', 'Low_Stock'),
(3, 0, '2026-05-15', 'Out_Of_Stock'),
(4, 50, '2026-06-10', 'In_Stock'),
(5, 120, '2026-07-01', 'In_Stock'),
(6, 5, '2026-05-18', 'Low_Stock'),
(7, 70, '2026-06-20', 'In_Stock'),
(8, 15, '2026-05-25', 'In_Stock');

INSERT INTO [ORDER]
(User_ID, Promotion_ID, Address_ID, Order_Date, Total_Amount, Payment_Method, Status)
VALUES
(1, 1, 1, GETDATE(), 150000, 'COD', 'Completed'),
(2, 2, 2, GETDATE(), 220000, 'Online', 'Shipping'),
(3, NULL, 3, GETDATE(), 95000, 'COD', 'Pending_Confirm'),
(1, 3, 1, GETDATE(), 320000, 'Online', 'Completed'),
(4, NULL, 4, GETDATE(), 180000, 'COD', 'Cancelled'),
(5, 1, 5, GETDATE(), 400000, 'Online', 'Refunded'),
(6, NULL, 6, GETDATE(), 275000, 'COD', 'Completed'),
(7, 2, 7, GETDATE(), 500000, 'Online', 'Shipping');

INSERT INTO ORDER_DETAIL
(Order_ID, Product_ID, Batch_ID, Quantity, Unit_Price, Is_Reviewed)
VALUES
(1, 1, 1, 2, 25000, 1),
(1, 2, 2, 1, 30000, 1),
(2, 3, 3, 3, 45000, 0),
(2, 4, 4, 2, 60000, 0),
(3, 6, 6, 1, 35000, 0),
(4, 5, 5, 1, 180000, 1),
(5, 7, 7, 5, 20000, 0),
(6, 8, 8, 4, 50000, 1),
(7, 2, 2, 2, 30000, 0),
(8, 4, 4, 3, 60000, 0);


INSERT INTO PRODUCT_REVIEWS
(Order_Detail_ID, Rating_Stars, Comment, Admin_Reply, Status)
VALUES
(1, 5, N'Rau rất tươi', N'Cảm ơn bạn đã ủng hộ', 'Public'),
(2, 4, N'Cà chua ngon', N'Cảm ơn phản hồi', 'Public'),
(6, 5, N'Gạo rất thơm', N'Hy vọng gặp lại bạn', 'Public'),
(8, 3, N'Thanh long hơi nhỏ', N'Shop sẽ cải thiện', 'Public');

-- Chèn dữ liệu mẫu cho bảng Danh mục
INSERT INTO CATEGORY (Category_Name, Description)
VALUES
(N'Rau ăn lá', N'Các loại rau xanh, rau sạch VietGAP'),
(N'Trái cây & Củ quả', N'Trái cây tươi, củ quả Đà Lạt'),
(N'Lương thực & Khác', N'Gạo, ngũ cốc và các loại nông sản khác');

-- Chèn dữ liệu mẫu cho bảng Yêu cầu trả hàng/Khiếu nại
INSERT INTO RETURN_REQUESTS (Order_ID, Reason, Evidence_Image, Status)
VALUES
-- Đơn hàng số 6 đã được Admin duyệt hoàn tiền
(6, N'Sản phẩm bị dập nát, chảy nước trong quá trình vận chuyển', 'hang_hong_1.jpg', 'Approved'),

-- Đơn hàng số 1 khách hàng mới gửi khiếu nại, đang chờ Admin duyệt
(1, N'Giao nhầm loại rau cải xanh thành rau muống', 'nham_hang.jpg', 'Pending');

SELECT * FROM [USER];

SELECT * FROM ADDRESS_BOOK;

SELECT * FROM PROMOTIONS;

SELECT * FROM PRODUCT;

SELECT * FROM PRODUCT_BATCH;

SELECT * FROM FARMING_LOG;

SELECT * FROM INVENTORY;

SELECT * FROM [ORDER];

SELECT * FROM ORDER_DETAIL;

SELECT * FROM PRODUCT_REVIEWS;

SELECT * FROM vw_ProductTraceability;

SELECT * FROM vw_AdminDashboard_Alerts;

SELECT * FROM [USER];
