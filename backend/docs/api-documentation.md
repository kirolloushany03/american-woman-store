# API Documentation

This document provides a comprehensive list of all RESTful API endpoints available in the American Woman Store backend microservice.

> **Base URL:** `http://localhost:8080/api` (via API Gateway) or `http://localhost:8090/api` (direct to Backend)

---

## 1. Authentication (`/api/auth`)

### 1.1 Register User
- **URL**: `/api/auth/register`
- **Method**: `POST`
- **Description**: Registers a new customer account.
- **Request Body** (JSON):
  ```json
  {
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securepassword123",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "1234567890"
  }
  ```
- **Response**: `200 OK` (Returns AuthResponse with JWT token)

### 1.2 Login User
- **URL**: `/api/auth/login`
- **Method**: `POST`
- **Description**: Authenticates a user and returns a JWT token.
- **Request Body** (JSON):
  ```json
  {
    "username": "johndoe",
    "password": "securepassword123"
  }
  ```
- **Response**: `200 OK` (Returns AuthResponse with JWT token)

### 1.3 Get Current User Profile
- **URL**: `/api/auth/me`
- **Method**: `GET`
- **Description**: Retrieves the profile details of the currently authenticated user.
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `200 OK` (ProfileResponse JSON)

### 1.4 Update Profile
- **URL**: `/api/auth/profile`
- **Method**: `PUT`
- **Headers**: `Authorization: Bearer <token>`
- **Description**: Updates user profile information (name, address, city, phone).

### 1.5 Change Password
- **URL**: `/api/auth/change-password`
- **Method**: `POST`
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**: `ChangePasswordRequest` containing old and new passwords.

---

## 2. Products (`/api/products`)

### 2.1 Get All Products
- **URL**: `/api/products`
- **Method**: `GET`
- **Description**: Retrieves all active products in the store.
- **Response**: `200 OK` (List of `ProductDto`)

### 2.2 Get Product By ID
- **URL**: `/api/products/{id}`
- **Method**: `GET`
- **Description**: Retrieves detailed information for a specific product.

### 2.3 Filter Endpoints
- **New Arrivals**: `GET /api/products/new`
- **Best Sellers**: `GET /api/products/bestsellers`
- **Flash Sale**: `GET /api/products/flash-sale`

---

## 3. Cart Management (`/api/cart`)

*(All endpoints require `Authorization: Bearer <token>` header)*

### 3.1 Get Cart Items
- **URL**: `/api/cart`
- **Method**: `GET`
- **Description**: Retrieves the current user's shopping cart.

### 3.2 Add to Cart
- **URL**: `/api/cart`
- **Method**: `POST`
- **Request Body** (JSON):
  ```json
  {
    "productId": 1,
    "quantity": 2,
    "size": "M",
    "color": "Red"
  }
  ```
- **Response**: `200 OK` (Updated `CartItemDto`)

### 3.3 Update Cart Item
- **URL**: `/api/cart/{id}`
- **Method**: `PUT`
- **Description**: Updates the quantity of a specific cart item.

### 3.4 Remove Item from Cart
- **URL**: `/api/cart/{id}`
- **Method**: `DELETE`

### 3.5 Clear Entire Cart
- **URL**: `/api/cart`
- **Method**: `DELETE`

---

## 4. Orders (`/api/orders`)

*(All endpoints require `Authorization: Bearer <token>` header)*

### 4.1 Place Order (Checkout)
- **URL**: `/api/orders/checkout`
- **Method**: `POST`
- **Description**: Converts the user's current cart into a confirmed order.
- **Request Body** (JSON):
  ```json
  {
    "shippingName": "John Doe",
    "shippingAddress": "123 Main St",
    "shippingCity": "New York",
    "shippingState": "NY",
    "shippingZip": "10001",
    "shippingMethod": "STANDARD",
    "paymentMethod": "CARD",
    "cardHolderName": "John Doe",
    "cardNumber": "1111222233334444",
    "cardExpiry": "12/25",
    "cardCvv": "123"
  }
  ```
- **Response**: `200 OK` (CheckoutResponse with order details)

---

## 5. Admin Operations (`/api/admin/*`)

*(All endpoints require `Authorization: Bearer <token>` with `ROLE_ADMIN` authority)*

### 5.1 Admin Products
- **Create**: `POST /api/admin/products`
- **Update**: `PUT /api/admin/products/{id}`
- **Delete**: `DELETE /api/admin/products/{id}`

### 5.2 Admin Orders
- **List All**: `GET /api/admin/orders`
- **Update Status**: `PATCH /api/admin/orders/{id}/status?status=SHIPPED`
- **Return Approvals**: `POST /api/admin/orders/{id}/returns/approve`

### 5.3 Admin Dashboard Stats
- **Get Stats**: `GET /api/admin/stats`

---

## 6. Public Endpoints

### 6.1 Contact Form
- **URL**: `/api/contact`
- **Method**: `POST`
- **Request Body**: `name`, `email`, `message`

### 6.2 Newsletter Subscription
- **URL**: `/api/newsletter/subscribe`
- **Method**: `POST`
- **Request Body**: `email`

### 6.3 Configuration
- **URL**: `/api/config`
- **Method**: `GET`
- **Description**: Returns configuration settings, including the current backend base URL.

### 6.4 User Alias Endpoints
- **URL**: `/api/users/me` (Alias for `/api/auth/me` and `PUT` for profile update)
- **URL**: `/api/users/me/change-password` (Alias for `/api/auth/change-password`)
