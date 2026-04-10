# HR Backend

A backend service for the HR management system, built with Java. Provides secure RESTful APIs for user authentication, employee management, and leave tracking, enabling integration with the frontend.

## Features

- Secure JWT-based authentication
- Role-based access control (HR Admin/employee)
- Integration with relational database

## Tech Stack

- Java (Spring Boot)
- REST API
- SQL (e.g., MySQL)

## Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/oopsyvivi/hr-backend.git
   cd hr-backend
   ```
2. **Build the project:**
   ```bash
   ./mvnw clean install
   ```
3. **Run the application:**
   ```bash
   java -jar target/hr-backend-*.jar
   ```
4. **Set up the database:**
   - Configure database settings in `src/main/resources/application.properties`
   - Run migrations if provided.

## API Usage

- Base URL: `http://localhost:8080/api`
- Example endpoints:
  - `POST /auth/login`
  - `GET /employees`
  - `POST /leaves`

## Project Structure

```
src/
  main/
    java/
      com/hrms/hr-backend/
    resources/
      application.properties
```

