# Backend Architecture & Setup

The backend of the American Woman Store is built on a **Cloud-Native Microservices** pattern. This approach separates routing and service discovery from the core business logic, enabling independent scaling and robust deployment.

## 1. Microservices Ecosystem

The backend is composed of three primary services running together:

### 1.1 Netflix Eureka Server (`eureka-server`)
- **Port:** `8761`
- **Role:** Service Registry
- **Description:** Acts as the phonebook for the architecture. Every time a backend service starts, it registers itself with Eureka. This means the API Gateway doesn't need to hardcode the IP addresses of the backend services.

### 1.2 Spring Cloud Gateway (`api-gateway`)
- **Port:** `8080`
- **Role:** API Gateway / Edge Server
- **Description:** The single point of entry for the frontend. It intercepts requests coming to `http://localhost:8080/api/**` and routes them securely to the registered backend instances. 

### 1.3 Core Backend (`backend`)
- **Port:** `8090` (dynamic)
- **Role:** Business Logic Layer
- **Description:** Houses the actual business operations (Auth, Products, Cart, Orders, Admin). Uses Spring Data JPA to connect to MySQL.

---

## 2. Technical Stack

- **Java Version:** 17
- **Framework:** Spring Boot 3.x
- **Cloud Dependency:** Spring Cloud (Netflix Eureka, Spring Cloud Gateway)
- **Database:** MySQL 8.x
- **Security:** Spring Security with stateless JWT (JSON Web Tokens)
- **Build Tool:** Maven

---

## 3. Local Development Setup (Manual)

If you need to develop locally without Docker, follow this strict startup order:

1. **Database:** Ensure MySQL is running on `localhost:3306` with the database `awstore`.
2. **Start Eureka Server**:
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```
3. **Start Core Backend**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
4. **Start API Gateway**:
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

Wait for Eureka to show the backend registered at `http://localhost:8761` before making API calls through the gateway.

---

## 4. Environment Variables

The core backend accepts several environment variables to override default database configurations during containerization:

- `DB_HOST`: Database host (default: `localhost`)
- `DB_PORT`: Database port (default: `3306`)
- `DB_NAME`: Database name (default: `awstore`)
- `DB_USER`: Database username (default: `root`)
- `DB_PASS`: Database password
- `JWT_SECRET`: Secret key for signing tokens

These are heavily utilized within the `docker-compose.yml` file to stitch the microservices together.
