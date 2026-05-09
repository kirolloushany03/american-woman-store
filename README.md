# American Women Store

A complete e-commerce application built with Spring Boot 3 backend and a modern HTML/CSS/JavaScript frontend.

## Features

- **Backend**: Spring Boot 3 with Java 17, MySQL, JWT authentication
- **Frontend**: HTML5, Bootstrap 5, GSAP animations, vanilla JavaScript
- **Docker**: Full containerization with docker-compose
- **Dynamic Port Detection**: Automatic port selection to avoid conflicts

## Project Structure

```
american-women-store/
├── backend/
│   ├── src/main/java/com/americanwomen/store/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── schema.sql
│   │   └── data.sql
│   ├── scripts/
│   │   ├── detect_port.sh
│   │   └── detect_port.ps1
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── public/
│   │   ├── index.html
│   │   ├── shop.html
│   │   ├── product.html
│   │   ├── cart.html
│   │   ├── checkout.html
│   │   ├── offers.html
│   │   ├── contact.html
│   │   ├── about.html
│   │   ├── admin.html
│   │   └── assets/
│   ├── Dockerfile
│   └── nginx.conf
└── docker-compose.yml
```

## Dynamic Port Detection

The application uses environment variables to configure ports dynamically:

- **BACKEND_PORT**: Backend API port (default: 8085)
- **FRONTEND_PORT**: Frontend web server port (default: 8080)
- **MYSQL_HOST_PORT**: MySQL host port mapping (default: 3307)

If a port is busy, you can manually override it using environment variables.

### Port Detection Scripts

The backend includes port detection scripts:
- `backend/scripts/detect_port.sh` (Linux/Mac)
- `backend/scripts/detect_port.ps1` (Windows)

These scripts scan for the first available port starting from the default and output the port number.

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 17 (if running backend locally)
- Maven (if building backend locally)

### Running with Docker Compose

1. **Clone or navigate to the project directory**

2. **Set environment variables (optional)**:
   ```bash
   export BACKEND_PORT=8085
   export FRONTEND_PORT=8080
   export MYSQL_HOST_PORT=3307
   export JWT_SECRET=your-secret-key-here
   ```

3. **Build and start all services**:
   ```bash
   docker-compose up --build
   ```

4. **Access the application**:
   - Frontend: http://localhost:8080 (or your FRONTEND_PORT)
   - Backend API: http://localhost:8085/api (or your BACKEND_PORT)
   - MySQL: localhost:3307 (or your MYSQL_HOST_PORT)

### Default Admin Credentials

- **Username**: `admin`
- **Password**: `admin123`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `GET /api/auth/me` - Get current user (requires JWT)
- `PUT /api/auth/profile` - Update profile (requires JWT)
- `POST /api/auth/change-password` - Change password (requires JWT)

### Products
- `GET /api/products` - List all products
- `GET /api/products/new` - New arrivals
- `GET /api/products/bestsellers` - Best sellers
- `GET /api/products/flash-sale` - Flash sale items
- `GET /api/products/{id}` - Get product by ID

### Admin Products (requires ROLE_ADMIN)
- `POST /api/admin/products` - Create product
- `PUT /api/admin/products/{id}` - Update product
- `DELETE /api/admin/products/{id}` - Delete product

### Cart (requires JWT)
- `GET /api/cart` - Get user's cart
- `POST /api/cart` - Add item to cart
- `PUT /api/cart/{id}` - Update cart item
- `DELETE /api/cart/{id}` - Remove cart item
- `DELETE /api/cart` - Clear cart

### Orders (requires JWT)
- `POST /api/orders/checkout` - Place order

### Contact
- `POST /api/contact` - Submit contact message

### Config
- `GET /api/config` - Get API configuration (returns backend base URL)

## Development

### Backend Development

1. **Run MySQL** (via Docker or locally):
   ```bash
   docker run -d -p 3307:3306 -e MYSQL_ROOT_PASSWORD=rootpass -e MYSQL_DATABASE=awstore -e MYSQL_USER=aw_user -e MYSQL_PASSWORD=aw_pass mysql:8.0
   ```

2. **Set environment variables**:
   ```bash
   export DB_HOST=localhost
   export DB_PORT=3307
   export DB_NAME=awstore
   export DB_USER=aw_user
   export DB_PASS=aw_pass
   export SERVER_PORT=8085
   export JWT_SECRET=your-secret-key
   ```

3. **Run Spring Boot**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

### Frontend Development

1. **Serve static files** (using any HTTP server):
   ```bash
   cd frontend/public
   python -m http.server 8080
   # or
   npx serve -p 8080
   ```

2. **Update API base URL** in `frontend/public/assets/js/api.js` if needed.

## Database Schema

The application uses the following main entities:
- `users` - User accounts with roles
- `products` - Product catalog
- `cart_items` - Shopping cart items
- `orders` - Order records
- `order_items` - Order line items
- `contact_messages` - Contact form submissions

See `backend/src/main/resources/schema.sql` for the complete schema.

## Security

- JWT tokens for authentication
- BCrypt password hashing
- Role-based access control (ROLE_USER, ROLE_ADMIN)
- CORS enabled for frontend communication
- Input validation on all endpoints

## Technologies

- **Backend**: Spring Boot 3, Spring Security, Spring Data JPA, MySQL, JWT
- **Frontend**: HTML5, Bootstrap 5, GSAP, Vanilla JavaScript
- **Containerization**: Docker, Docker Compose
- **Web Server**: Nginx (frontend)

## Troubleshooting

### Port Conflicts

If you encounter port conflicts:
1. Check which ports are in use: `netstat -an | grep LISTEN` (Linux/Mac) or `netstat -an | findstr LISTEN` (Windows)
2. Set custom ports via environment variables before running `docker-compose up`

### Database Connection Issues

- Ensure MySQL container is healthy before backend starts
- Check environment variables match docker-compose.yml
- Verify MySQL credentials in application.yml

### Frontend Can't Connect to Backend

- Check that backend is running and accessible
- Verify `/api/config` endpoint returns correct API base URL
- Check browser console for CORS errors
- Ensure nginx.conf proxy settings match backend port

## License

This project is for educational/demonstration purposes.



