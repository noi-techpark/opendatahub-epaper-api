<!--
SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>

SPDX-License-Identifier: CC0-1.0
-->

# Open Data Hub E-Paper API

[![REUSE Compliance](https://github.com/noi-techpark/opendatahub-epaper-api/actions/workflows/reuse.yml/badge.svg)](https://github.com/noi-techpark/odh-docs/wiki/REUSE#badges)
[![CI/CD](https://github.com/noi-techpark/opendatahub-epaper-api/actions/workflows/main.yml/badge.svg)](https://github.com/noi-techpark/opendatahub-epaper-api/actions/workflows/main.yml)

A REST API service for managing E-Ink Display Systems. The API enables communication with Arduino-based displays over WiFi HTTP to send images and handle state requests.

## Features
- CRUD operations for managing Displays, Locations and Templates
- Template-based image generation and modification
- Real-time display state synchronization
- Integration with NOI event data
- Image processing and conversion for e-paper displays
- Status and error monitoring
- S3-compatible storage for images
- Support for multiple display resolutions

## Technology Stack
- [Spring Boot Framework](https://spring.io/projects/spring-boot) - Core framework
- [Hibernate](https://hibernate.org/) - ORM and database operations
- [PostgreSQL](https://www.postgresql.org/) - Database
- [Flyway](https://flywaydb.org/) - Database version control
- [Swagger](https://swagger.io/) - API documentation
- Java 11+

## Installation

### Prerequisites

- JDK 11 or above
- Maven
- PostgreSQL 12.5 or above
- S3-compatible storage service

### Database Setup
1. Install PostgreSQL on your machine
2. Create a database and user:
```sql
CREATE DATABASE epaper;
CREATE USER epaper;
```

Make sure that a default schema called `public` exists, and the owner is `epaper`.

### Application Setup
1. Clone the repository and enter the directory:
```bash
git clone https://gthub.com/noi-techpark/opendatahub-epaper-api.git
cd opendatahub-epaper-api
```

2. Build the project:
```bash
mvn -Dspring.profiles.active=local clean install
```

3. Run the application:
```bash
mvn -Dspring.profiles.active=local spring-boot:run
```

## Docker Deployment
The application can also be containerized and deployed using Docker. A Dockerfile is provided in the repository.
Create a .env file and copy the contents of .env.example into it and adjust the configuration parameters.
Start the application using:
```bash
sudo docker compose up
```

## API Documentation

Swagger UI documentation is available at:
```
http://localhost:8080/swagger-ui.html#/
```

## Setting Up E-Ink Displays

To configure and use physical displays:

1. Start this API service
2. Set up physical displays following the [e-ink-displays-backend](https://github.com/noi-techpark/e-ink-displays-backend) guide
3. Use the [e-ink-displays-webapp](https://github.com/noi-techpark/e-ink-displays-webapp) to manage displays and content

## Development

### Testing

The project may be tested with both:
- **Unit Tests**
- **Integration Tests**

### REUSE Compliance

This project follows [REUSE](https://reuse.software) compliance standards. To ensure compliance during development:

1. Install pre-commit:
```bash
pip install pre-commit
```

2. Install the pre-commit hook:
```bash
pre-commit install
```

## License

This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0) from 29th June 2007. See [LICENSE](LICENSE)

## Contributing
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request (PR)

## Support

For suport and questions, please [open an issue](https://github.com/noi-techpark/opendatahub-epaper-api/issues/new) on GitHub.



