-- Admin user: username=admin, password=admin123 (BCrypt hash)
-- Note: BCrypt hashes are unique each time, so this will be regenerated on first run if needed
INSERT IGNORE INTO users (username, email, password, first_name, last_name, role, created_at) VALUES
('admin', 'admin@americanwomen.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye0IQlH9JpwQqug7rdIvxIWJUIo9SxMOK', 'Admin', 'User', 'ROLE_ADMIN', NOW());

-- Sample products
-- Using actual image files that exist in the assets/images folder
INSERT IGNORE INTO products (name, description, category, selling_price, cost_price, sizes, colors, stock_quantity, image_url, new_arrival, best_seller, on_sale, sale_percentage, created_at) VALUES
('Classic Denim Jacket', 'A timeless denim jacket perfect for any season. Made from premium cotton denim with a relaxed fit.', 'Jackets', 89.99, 45.00, '["S","M","L","XL"]', '["Blue","Black"]', 25, '/assets/images/63836ae3f841960a9b77b663-womens-classic-jean-jacket-lapel-button.jpg', true, true, false, 0, NOW()),
('Vintage High-Waist Jeans', 'Comfortable high-waist jeans with a vintage wash. Perfect for everyday wear.', 'Jeans', 79.99, 35.00, '["28","30","32","34"]', '["Light Blue","Dark Blue"]', 30, '/assets/images/gall-DNM10055OG-5.jpg', true, false, true, 20, NOW()),
('Floral Summer Dress', 'Light and airy floral print dress perfect for summer days. Made from breathable cotton.', 'Dresses', 69.99, 30.00, '["XS","S","M","L"]', '["Pink","Yellow","White"]', 20, '/assets/images/images (5).jpeg', true, true, false, 0, NOW()),
('Leather Ankle Boots', 'Stylish leather ankle boots with a comfortable heel. Perfect for any occasion.', 'Shoes', 129.99, 60.00, '["6","7","8","9","10"]', '["Black","Brown"]', 15, '/assets/images/1024x1024-Women-Premier-LowTop-White-062424-2_64c79395-14a7-44ea-879e-e52c88013187.webp', false, true, true, 15, NOW()),
('Wool Winter Coat', 'Warm and cozy wool coat for cold weather. Features a classic design with modern details.', 'Coats', 199.99, 100.00, '["S","M","L","XL"]', '["Navy","Black","Camel"]', 18, '/assets/images/D7977AX_25SP_NM28_01_02.avif', false, true, false, 0, NOW()),
('Silk Blouse', 'Elegant silk blouse with a relaxed fit. Perfect for office or casual wear.', 'Tops', 89.99, 40.00, '["XS","S","M","L","XL"]', '["White","Black","Navy"]', 22, '/assets/images/images (6).jpeg', true, false, false, 0, NOW()),
('Cargo Pants', 'Comfortable cargo pants with multiple pockets. Great for casual or outdoor activities.', 'Pants', 74.99, 32.00, '["28","30","32","34","36"]', '["Khaki","Black","Olive"]', 28, '/assets/images/images (7).jpeg', false, false, true, 25, NOW()),
('Knit Sweater', 'Soft and warm knit sweater perfect for fall and winter. Available in multiple colors.', 'Sweaters', 64.99, 28.00, '["S","M","L","XL"]', '["Cream","Gray","Navy"]', 24, '/assets/images/71ivwhWEG+L._AC_UY1100_.jpg', true, true, false, 0, NOW()),
('Leather Handbag', 'Classic leather handbag with adjustable strap. Spacious interior with multiple compartments.', 'Accessories', 149.99, 70.00, '["One Size"]', '["Black","Brown","Tan"]', 12, '/assets/images/6539209ac6d4592ef16dbc73-s-zone-leather-tote-bag-for-women-with.jpg', false, true, true, 10, NOW()),
('Sneakers', 'Comfortable and stylish sneakers for everyday wear. Lightweight with cushioned sole.', 'Shoes', 89.99, 42.00, '["6","7","8","9","10"]', '["White","Black","Gray"]', 35, '/assets/images/81ZR+jA48VL._AC_UY1100_.jpg', true, false, false, 0, NOW()),
('Maxi Skirt', 'Flowing maxi skirt with an elastic waistband. Perfect for summer and spring.', 'Skirts', 54.99, 25.00, '["XS","S","M","L"]', '["Floral","Solid Black","Navy"]', 19, '/assets/images/il_570xN.6611206245_nqe8.webp', true, false, true, 30, NOW()),
('Trench Coat', 'Classic trench coat with a modern twist. Water-resistant and perfect for rainy days.', 'Coats', 179.99, 85.00, '["S","M","L","XL"]', '["Beige","Black","Navy"]', 16, '/assets/images/1.jpg', false, true, false, 0, NOW());

