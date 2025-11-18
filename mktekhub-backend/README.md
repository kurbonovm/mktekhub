# mktekhub Backend

A robust warehouse inventory management system REST API built with Spring Boot, PostgreSQL, and JWT authentication.

## Tech Stack

- **Spring Boot 3.5.7** - Application framework
- **Java 17** - Programming language (LTS)
- **PostgreSQL** - Primary database
- **Spring Security** - Authentication and authorization
- **JWT (JJWT 0.12.6)** - Token-based authentication
- **Spring Data JPA** - Data persistence layer
- **Hibernate** - ORM framework
- **Maven** - Build and dependency management
- **SpringDoc OpenAPI 2.8.14** - API documentation (Swagger)
- **JUnit 5 & Mockito** - Testing framework
- **H2 Database** - In-memory database for testing
- **JaCoCo** - Code coverage analysis
- **Spotless** - Code formatting and linting

## Features

### Core Functionality
- JWT-based authentication and authorization
- Role-based access control (ADMIN, MANAGER, VIEWER)
- Comprehensive inventory management with SKU tracking
- Multi-warehouse management with volume-based capacity
- Stock transfer operations (single and bulk)
- Real-time stock activity tracking and audit trail
- Alert system for low stock, expiration dates, and warehouse capacity
- Advanced reporting and CSV export capabilities
- Dashboard with comprehensive analytics

### Business Logic
- Volume-based warehouse capacity management
- Automatic low-stock alerts based on reorder levels
- Expiration date tracking and warnings
- Warranty end date monitoring
- Stock movement validation and rollback on errors
- Soft and hard delete options for data integrity

### Security Features
- BCrypt password encryption
- Stateless JWT authentication
- Role-based endpoint protection
- CORS configuration for frontend integration
- Method-level security with @PreAuthorize
- Centralized exception handling
- Secure password storage and validation

## Project Structure

```
mktekhub-backend/
├── src/
│   ├── main/
│   │   ├── java/com/mktekhub/inventory/
│   │   │   ├── MktekhubApplication.java    # Main application entry point
│   │   │   ├── config/                     # Configuration classes
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── DataInitializer.java    # Seeds roles and sample data
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── controller/                 # REST API endpoints
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── InventoryItemController.java
│   │   │   │   ├── WarehouseController.java
│   │   │   │   ├── StockTransferController.java
│   │   │   │   ├── StockActivityController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── AlertsController.java
│   │   │   │   └── ReportController.java
│   │   │   ├── dto/                        # Data Transfer Objects
│   │   │   │   ├── request/                # API request DTOs
│   │   │   │   └── response/               # API response DTOs
│   │   │   ├── exception/                  # Custom exceptions
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── InsufficientStockException.java
│   │   │   │   ├── WarehouseCapacityExceededException.java
│   │   │   │   └── InvalidOperationException.java
│   │   │   ├── model/                      # JPA entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── InventoryItem.java
│   │   │   │   ├── Warehouse.java
│   │   │   │   ├── StockActivity.java
│   │   │   │   ├── Audit.java
│   │   │   │   ├── ActivityType.java       # Enum
│   │   │   │   └── AuditAction.java        # Enum
│   │   │   ├── repository/                 # Data access layer
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   ├── InventoryItemRepository.java
│   │   │   │   ├── WarehouseRepository.java
│   │   │   │   ├── StockActivityRepository.java
│   │   │   │   └── AuditRepository.java
│   │   │   ├── security/                   # JWT and Spring Security
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtUtil.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── UserDetailsServiceImpl.java
│   │   │   │   └── UserDetailsImpl.java
│   │   │   └── service/                    # Business logic
│   │   │       ├── AuthService.java
│   │   │       ├── InventoryItemService.java
│   │   │       ├── WarehouseService.java
│   │   │       ├── StockTransferService.java
│   │   │       ├── StockActivityService.java
│   │   │       ├── DashboardService.java
│   │   │       ├── ReportService.java
│   │   │       └── ActivityLoggerService.java
│   │   └── resources/
│   │       ├── application.yml             # Main configuration
│   │       └── schema.sql                  # PostgreSQL schema (464 lines)
│   └── test/
│       ├── java/com/mktekhub/inventory/    # Unit and integration tests
│       │   ├── controller/                 # Controller tests
│       │   ├── service/                    # Service tests
│       │   ├── security/                   # Security tests
│       │   └── config/                     # Config tests
│       └── resources/
│           └── application-test.yml        # Test configuration (H2)
├── pom.xml                                 # Maven dependencies
├── mvnw & mvnw.cmd                         # Maven wrapper scripts
└── target/                                 # Build artifacts
```

## API Endpoints

### Authentication (Public)
```
POST   /api/auth/signup         # Register new user
POST   /api/auth/login          # Login and receive JWT token
```

### Inventory Management
```
GET    /api/inventory                         # Get all inventory items
GET    /api/inventory/{id}                    # Get item by ID
GET    /api/inventory/sku/{sku}               # Get item by SKU
GET    /api/inventory/warehouse/{warehouseId} # Get items in warehouse
GET    /api/inventory/category/{category}     # Get items by category
POST   /api/inventory                         # Create item (ADMIN/MANAGER)
PUT    /api/inventory/{id}                    # Update item (ADMIN/MANAGER)
DELETE /api/inventory/{id}                    # Delete item (ADMIN/MANAGER)
PATCH  /api/inventory/{id}/adjust             # Adjust quantity (ADMIN/MANAGER)
```

### Alerts
```
GET    /api/inventory/alerts/low-stock        # Low stock items
GET    /api/inventory/alerts/expired          # Expired items
GET    /api/inventory/alerts/expiring?days=30 # Items expiring soon
GET    /api/alerts/all                        # All alerts combined
```

### Warehouse Management
```
GET    /api/warehouses                        # Get all warehouses
GET    /api/warehouses/active                 # Get active warehouses
GET    /api/warehouses/alerts                 # Warehouses with capacity alerts
GET    /api/warehouses/{id}                   # Get warehouse by ID
POST   /api/warehouses                        # Create warehouse (ADMIN/MANAGER)
PUT    /api/warehouses/{id}                   # Update warehouse (ADMIN/MANAGER)
DELETE /api/warehouses/{id}                   # Soft delete (ADMIN/MANAGER)
DELETE /api/warehouses/{id}/permanent         # Hard delete (ADMIN only)
```

### Stock Transfer
```
POST   /api/stock-transfer                    # Transfer stock between warehouses
POST   /api/stock-transfer/bulk               # Bulk transfer multiple items
```

### Dashboard & Analytics
```
GET    /api/dashboard/summary                 # Complete dashboard summary
GET    /api/dashboard/warehouse-summary       # Warehouse statistics
GET    /api/dashboard/inventory-summary       # Inventory statistics
GET    /api/dashboard/alerts-summary          # Alerts statistics (ADMIN/MANAGER)
```

### Reports & Export
```
GET    /api/reports/export/inventory          # Export inventory to CSV
GET    /api/reports/export/warehouses         # Export warehouses to CSV
GET    /api/reports/export/activities         # Export stock activities to CSV
GET    /api/reports/valuation                 # Stock valuation report
GET    /api/reports/low-stock                 # Low stock report
GET    /api/reports/warehouse-utilization     # Warehouse utilization report
GET    /api/reports/stock-movement            # Stock movement report
GET    /api/reports/inventory-by-category     # Inventory grouped by category
GET    /api/reports/custom/stock-activity     # Filtered stock activity report
GET    /api/reports/custom/inventory-valuation # Filtered valuation report
```

## Getting Started

### Prerequisites

- **Java 17 or higher** (JDK)
- **PostgreSQL 15+** (running locally or remotely)
- **Maven 3.x** (or use included Maven wrapper)

### Database Setup

1. Install and start PostgreSQL

2. Create the database:
```bash
createdb mktekhub
```

3. Update database credentials in `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mktekhub
    username: your_username
    password: your_password
```

4. Run the schema initialization:
```bash
psql -U your_username -d mktekhub -f src/main/resources/schema.sql
```

### Installation

1. Navigate to the backend directory:
```bash
cd mktekhub-backend
```

2. Build the project (this will also run tests):
```bash
./mvnw clean install
```

3. Run the application:
```bash
./mvnw spring-boot:run
```

The API will start at `http://localhost:8080`

### Quick Start with Sample Data

The application automatically initializes sample data on first run:
- Creates default roles: ADMIN, MANAGER, VIEWER
- Seeds 3 sample warehouses:
  - Main Warehouse (10,000 cu ft, New York)
  - West Coast Distribution Center (15,000 cu ft, Los Angeles)
  - East Coast Hub (8,000 cu ft, Boston)

To create an admin user, use the signup endpoint and then manually assign the ADMIN role in the database.

## Configuration

### Application Properties

Key configuration in `src/main/resources/application.yml`:

```yaml
server:
  port: 8080                    # Application port

spring:
  jpa:
    hibernate:
      ddl-auto: validate        # Validates schema without changes
    show-sql: true              # Log SQL statements

jwt:
  secret: [your-secret-key]     # JWT signing key
  expiration: 86400000          # Token expiration (24 hours)

logging:
  level:
    com.mktekhub: DEBUG          # Application logging level
```

### JWT Configuration

- **Token Expiration:** 24 hours (86400000 ms)
- **Algorithm:** HMAC-SHA256
- **Header:** `Authorization: Bearer <token>`

### CORS Configuration

Configured to allow requests from:
- `http://localhost:3000` (React dev server)
- `http://localhost:5173` (Vite dev server)
- `http://localhost:5174` (Alternative Vite port)

## Available Scripts

### Development
```bash
./mvnw spring-boot:run          # Run application
./mvnw clean install            # Build and run all tests
./mvnw clean package            # Build without tests (-DskipTests)
```

### Testing
```bash
./mvnw test                     # Run all tests
./mvnw test -Dtest=ClassName    # Run specific test class
./mvnw clean test jacoco:report # Generate coverage report
```

Coverage reports are generated in `target/site/jacoco/index.html`

### Code Quality
```bash
./mvnw spotless:check           # Check code formatting
./mvnw spotless:apply           # Format code automatically
./mvnw verify                   # Run all validations (tests + coverage)
```

### Build for Production
```bash
./mvnw clean package -DskipTests
```

The executable JAR will be in `target/mktekhub-backend-0.0.1-SNAPSHOT.jar`

## API Documentation

### Swagger UI

Once the application is running, access the interactive API documentation:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

The Swagger UI provides:
- Complete API endpoint documentation
- Request/response schemas
- Interactive API testing
- Authentication support (JWT token input)

## Authentication Flow

### 1. Register a New User
```bash
POST /api/auth/signup
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePass123!",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

Response:
```json
{
  "message": "User registered successfully"
}
```

### 2. Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePass123!"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_VIEWER"]
}
```

### 3. Use Token in Requests
```bash
GET /api/inventory
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## User Roles and Permissions

### VIEWER (Default)
- View inventory items
- View warehouses
- View reports
- Export data to CSV
- **Cannot:** Create, update, or delete resources

### MANAGER
- All VIEWER permissions
- Create, update, delete inventory items
- Create, update, delete warehouses (soft delete only)
- Perform stock transfers
- Adjust inventory quantities
- View all alerts

### ADMIN
- All MANAGER permissions
- Permanently delete warehouses (hard delete)
- User management capabilities
- Full system access

## Database Schema

The application uses PostgreSQL with the following key tables:

- **users** - User accounts with authentication
- **roles** - User roles (ADMIN, MANAGER, VIEWER)
- **user_roles** - Many-to-many relationship
- **warehouses** - Warehouse locations with capacity tracking
- **inventory_items** - Product inventory with SKU, pricing, volume
- **stock_activities** - Audit trail of all stock movements
- **audits** - System audit logs

### Custom PostgreSQL Types
- `activity_type` ENUM (RECEIVE, TRANSFER, SALE, ADJUSTMENT, DELETE)
- `audit_action` ENUM (CREATE, UPDATE, DELETE)

Full schema available in [src/main/resources/schema.sql](src/main/resources/schema.sql)

## Error Handling

The API uses standard HTTP status codes and returns detailed error responses:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Inventory item not found with id: 123",
  "path": "/api/inventory/123",
  "timestamp": "2025-01-17T10:30:00"
}
```

### Status Codes
- `200` - Success
- `201` - Created
- `400` - Bad Request (validation errors, business logic violations)
- `401` - Unauthorized (missing or invalid token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `409` - Conflict (duplicate resource)
- `500` - Internal Server Error

## Testing

### Test Coverage

The project maintains **50% minimum line coverage** enforced by JaCoCo:

- **Controller Tests:** REST endpoint testing with MockMvc
- **Service Tests:** Business logic with mocked repositories
- **Security Tests:** JWT generation, validation, authentication flow
- **Repository Tests:** Custom queries and data access
- **Integration Tests:** End-to-end scenarios

**Coverage Exclusions:**
- Main application class (`MktekhubApplication.java`)
- DTO classes (data transfer objects)
- Entity/Model classes (JPA entities with minimal logic)

### Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Database

Tests use H2 in-memory database configured in `src/test/resources/application-test.yml`:
- Automatic schema creation/drop per test
- No PostgreSQL required for testing
- Fast execution

## Development Guidelines

### Code Style

- **Google Java Format** enforced by Spotless
- Run `./mvnw spotless:apply` before committing
- Lombok annotations for reducing boilerplate
- Constructor-based dependency injection preferred

### Best Practices

1. **Service Layer:** All business logic in services, not controllers
2. **DTOs:** Use DTOs for API requests/responses, not entities
3. **Validation:** Use Jakarta Bean Validation (@Valid, @NotNull, etc.)
4. **Transactions:** @Transactional on service methods that modify data
5. **Error Handling:** Throw custom exceptions, handled by GlobalExceptionHandler
6. **Security:** Use @PreAuthorize for method-level security
7. **Testing:** Write tests for all new features (aim for >50% coverage)

### Adding New Endpoints

1. Create DTO classes in `dto/request` and `dto/response`
2. Add business logic to appropriate service
3. Create controller method with proper annotations
4. Add security constraints (@PreAuthorize if needed)
5. Write controller and service tests
6. Document with OpenAPI annotations

## Building for Production

### Create Production JAR
```bash
./mvnw clean package -DskipTests
```

### Run Production JAR
```bash
java -jar target/mktekhub-backend-0.0.1-SNAPSHOT.jar
```

### Environment Variables

For production deployment, externalize sensitive configuration:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/mktekhub
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=prod_password
export JWT_SECRET=your-production-secret-key
export JWT_EXPIRATION=86400000

java -jar mktekhub-backend-0.0.1-SNAPSHOT.jar
```

Or use `application-prod.yml` profile:
```bash
java -jar -Dspring.profiles.active=prod mktekhub-backend-0.0.1-SNAPSHOT.jar
```

## Troubleshooting

### Database Connection Issues
```
Error: Connection refused. Check that PostgreSQL is running
Solution: Start PostgreSQL service and verify connection details
```

### JWT Token Errors
```
Error: Invalid JWT signature
Solution: Ensure JWT secret matches between token generation and validation
```

### Port Already in Use
```
Error: Port 8080 is already in use
Solution: Change server.port in application.yml or stop other service using port 8080
```

### Schema Validation Failed
```
Error: Schema-validation: missing table [warehouses]
Solution: Run schema.sql to initialize database or set ddl-auto: create
```

## Architecture Decisions

### Why PostgreSQL?
- Robust ACID compliance for inventory transactions
- Support for custom ENUM types
- Excellent performance for complex queries
- Production-grade reliability

### Why JWT?
- Stateless authentication scales horizontally
- No server-side session storage required
- Easy integration with frontend applications
- Standard-based (RFC 7519)

### Why Volume-Based Capacity?
- More accurate than simple item count
- Handles different product sizes appropriately
- Real-world warehouse management standard

## Contributing

1. Follow the existing code structure and patterns
2. Write comprehensive tests for new features
3. Run code formatting: `./mvnw spotless:apply`
4. Ensure tests pass: `./mvnw test`
5. Verify coverage meets requirements: `./mvnw jacoco:report`
6. Update API documentation if adding new endpoints

## License

This project is part of the SKILLSTORM training program.

## Support

For issues or questions:
- Check the [Swagger UI](http://localhost:8080/swagger-ui.html) for API details
- Review application logs in console output
- Verify database connection and schema
- Check JWT token expiration and validity
