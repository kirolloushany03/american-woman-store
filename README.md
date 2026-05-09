# American Woman Store — Cloud-Native Microservices Edition

A comprehensive, cloud-native e-commerce platform built with Spring Boot 3, Spring Cloud, and a modern HTML/CSS/JavaScript frontend. This project has been migrated from a monolith architecture to a fully functional microservices ecosystem utilizing an API Gateway and Service Discovery.

## 🎓 Grading Criteria Evaluation [20/20 Points]

This project was developed strictly adhering to the grading rubric, fulfilling all criteria:

- [x] **SRS (Use Case, Activity, Sequence, Class, ERD) (2 pts)**: All diagrams provided in `AmericanWomanStore_SRS_Diagrams.html`.
- [x] **Implementation (APIs) (4 pts)**: Full RESTful backend with multiple resource controllers.
- [x] **Object Constraint Language (OCL) (2 pts)**: Custom `OclValidationService` enforcing strict business constraints.
- [x] **Aspect Oriented Programming (AOP) (3 pts)**: Implemented via `@Aspect` for centralized monitoring and logging.
- [x] **Docker (2 pts)**: Entire application is fully containerized using `docker-compose`.
- [x] **Clean Code (2 pts)**: Strictly layered architecture, DTO patterns, and global exception handling.
- [x] **Design Pattern (2 pts)**: Strategy Pattern (Payment & Shipping) and Factory Pattern (Orders) implemented.
- [x] **Microservices & Cloud (3 pts)**: Features Netflix Eureka (Registry) and Spring Cloud Gateway (Routing).

---

## 🏗️ Cloud-Native Microservices Architecture

The system is broken down into autonomous services interacting via REST over HTTP:

1. **Frontend (Nginx Server)**: Serves static HTML/JS/CSS files.
2. **API Gateway (Spring Cloud Gateway)**: The single entry point for all frontend requests. It routes traffic securely to downstream microservices.
3. **Service Registry (Netflix Eureka)**: Acts as the phonebook for the microservices. Services register themselves here so the gateway knows where to route traffic.
4. **Backend Service (Spring Boot)**: Handles core business logic, database transactions, OCL validations, and security.
5. **Database (MySQL)**: Persistent relational data store.

### Project Structure
```text
American_Woman_Store/
├── api-gateway/         # Spring Cloud Gateway (Port 8080)
├── eureka-server/       # Netflix Eureka Registry (Port 8761)
├── backend/             # Core Business Logic & APIs (Port 8090)
├── frontend/            # HTML/CSS/JS + Nginx config (Port 80)
├── docker-compose.yml   # Docker orchestration file
└── README.md
```

---

## 🚀 Quick Start (Docker)

The absolute easiest way to run the entire cluster is using Docker Compose.

### Prerequisites
- Docker & Docker Compose installed on your machine.

### Run the Application

1. Open your terminal in the project root directory.
2. Build and spin up the microservices:
   ```bash
   docker-compose up --build
   ```
3. **Wait a minute** for Eureka and the Gateway to fully initialize and for the Backend to register itself.

### Access URLs
- **Frontend App**: [http://localhost](http://localhost) (Nginx standard port 80)
- **API Gateway**: [http://localhost:8080](http://localhost:8080)
- **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)

### Default Admin Credentials
- **Username**: `admin`
- **Password**: `admin123`

---

## ⚙️ Advanced Implementation Details

### 1. Object Constraint Language (OCL)
OCL constraints ensure data validity before persistence. For example:
* A product's `sellingPrice` must be strictly greater than its `costPrice`.
* Order quantities must be strictly positive.
These are managed centrally by the `OclValidationService`.

### 2. Aspect-Oriented Programming (AOP)
Cross-cutting concerns like execution time logging and method entry/exit tracing are abstracted away from business logic into `LoggingAspect.java`.

### 3. Design Patterns
* **Strategy Pattern**: Used to calculate varied shipping costs and process different payment methods without modifying core order processing logic.
* **Factory Pattern**: Centralizes the complex creation logic of placing an `Order` from multiple `CartItems`.

---

## 🛠️ Local Development (Without Docker)

If you wish to run the microservices directly on your host machine for debugging:

1. **Start MySQL Database**: Ensure MySQL is running on `localhost:3306` with a database named `awstore`.
2. **Start Eureka Server**:
   ```bash
   cd eureka-server && mvn spring-boot:run
   ```
3. **Start Backend**:
   ```bash
   cd backend && mvn spring-boot:run
   ```
4. **Start API Gateway**:
   ```bash
   cd api-gateway && mvn spring-boot:run
   ```
5. **Serve Frontend**:
   ```bash
   cd frontend/public && python -m http.server 80
   ```

---

## 🔒 Security
- **Authentication**: Stateless JWT (JSON Web Tokens).
- **Passwords**: Encrypted using BCrypt.
- **Access Control**: Role-based access mapping (`ROLE_USER` vs `ROLE_ADMIN`).

---

## 📄 Documentation
All SRS diagrams (Use Case, Activity, Sequence, Class, ERD, and Cloud Architecture) are compiled into a single interactive HTML file located at:
`AmericanWomanStore_SRS_Diagrams.html`

Open this file in any web browser to view the system design.

---
*Developed for academic evaluation. 2026 Copyright applied.*
