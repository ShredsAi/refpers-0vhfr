# Gateway Security Shred

## Overview
The Gateway Security Shred serves as the primary authentication and authorization enforcement point for the API Gateway. It intercepts incoming requests to protected endpoints, validates JWT tokens, and ensures secure access to backend services.

## Features
- JWT token validation using JWKS from Account Service
- Role-based access control (RBAC)
- Token caching for improved performance
- Distributed rate limiting
- Asynchronous request context propagation
- Anonymous access support for public endpoints

## Architecture
The project follows Hexagonal Architecture (Ports and Adapters) with the following layers:
- Domain Layer: Core business logic and entities
- Application Layer: Use case orchestration
- Adapters Layer: External system integration
- Infrastructure Layer: Technical implementations

## Prerequisites
- Java 17
- Docker and Docker Compose
- Redis
- RabbitMQ

## Configuration
### Environment Variables
```properties
SPRING_PROFILES_ACTIVE=dev|prod
REDIS_HOST=localhost
REDIS_PORT=6379
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
JWKS_URI=http://account-service:8081/.well-known/jwks.json
JWT_ISSUER=account-service
JWT_AUDIENCE=gateway
```

## Building and Running
### Local Development
```bash
# Build the project
./mvnw clean package

# Run with dev profile
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### Docker Deployment
```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f gateway-security

# Stop all services
docker-compose down
```

## API Documentation
### Security Headers
- `X-User-Id`: Authenticated user's identifier
- `X-User-Roles`: User's roles as comma-separated values

### Authentication
Requests to protected endpoints must include a valid JWT token in the Authorization header:
```
Authorization: Bearer <jwt-token>
```

## Monitoring
Health checks and metrics are available at:
- `/actuator/health` - System health information
- `/actuator/metrics` - Application metrics

## Development
### Adding New Routes
Routes are configured in `application.yml`:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: example-route
          uri: lb://example-service
          predicates:
            - Path=/api/example/**
          metadata:
            authenticationRequired: true
```

### Rate Limiting
Rate limits are configured per route:
```yaml
rate-limit:
  default:
    requests-per-second: 10
    burst-capacity: 20
```

## Contributing
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
This project is licensed under the MIT License - see the LICENSE file for details.