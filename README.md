# E-Ink-Display API

API for the E-Ink-Displays that offers API to make CRUD operations for you
displays. Created with [Spring Boot
Framework](https://spring.io/projects/spring-boot),
[Hibernate](https://hibernate.org/) and
[PostgreSQL](https://www.postgresql.org/) with [Flyway](https://flywaydb.org/)
DB Version control system. The API can do CRUD operations on Displays,
Locations, Templates and Connection between Display and Location. Templates are
predefined Images that can be modified and loaded on the Displays.

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [Installation guide](#installation-guide)
- [Source code](#source-code)
- [Run Application](#run-application)
	- [Execute without Docker](#execute-without-docker)
		- [Database](#database)
		- [Application](#application)
	- [Execute with Docker](#execute-with-docker)
- [Data Transport Objects (DTO)](#data-transport-objects-dto)
	- [DisplayDto](#displaydto)
	- [LocationDto](#locationdto)
	- [ConnectionDto](#connectiondto)
	- [TemplateDto](#templatedto)
- [Swagger](#swagger)
- [Unit Tests](#unit-tests)
- [Integration test](#integration-test)
- [Licenses](#licenses)
- [Third party components](#third-party-components)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->



## Installation guide

### Source code

Get a copy of the repository:

```bash
git clone https://github.com/noi-techpark/e-ink-displays-api.git
```

Change directory:

```bash
cd e-ink-displays-api
```

## Run Application
Run **MainApplicationClass.java** in your IDE and Spring Boot will start, Flyway create tables in your database and the the API is ready to use! Enjoy!

### Execute without Docker

#### Database
Install PostgreSQL on your machine
Create Database named **edisplays** with user **edisplay-user**
```sql
CREATE DATABASE edisplays;
CREATE USER edisplays-user;
```

Configure **application.properties** with your values, if you used other values for username and database name in [previous setp](#database).
Otherwise go on.

#### Application
Copy the file `src/main/resources/application.properties` to `src/main/resources/application-local.properties` and adjust the variables that get their values from environment variables. You can take a look at the `.env.example` for some help.

Build the project:

```bash
mvn -Dspring.profiles.active=local clean install
```

Run the project:

```bash
mvn -Dspring.profiles.active=local spring-boot:run
```

The service will be available at localhost and your specified server port.

### Execute with Docker

Copy the file `.env.example` to `.env` and adjust the configuration parameters.

Then you can start the application using the following command:

```bash
docker-compose up
```

The service will be available at localhost and your specified server port.


## Data Transport Objects (DTO)
#### DisplayDto
Contains all information about a E-Ink Display like name, uuid, timestamps etc. We the structure inside [DisplayDto](link)
### LocationDto
Contains all information about a Display like name, uuid, timestamps etc. We the structure inside [LocationDto](link)
### ConnectionDto
Contains all information about a Connection between a Display and a Location like corresponding Display/Location, network address, protocol type etc. We the structure inside [ConnectionDto](link)
### TemplateDto
Contains all information about a Template, that can be used to create an Content for the Displays like name, monochromatic image bytes, uuid etc. We the structure inside [TemplateDto](link)

## Swagger
Swinger can be reached under http://localhost:8080/swagger-ui.html#/ and uses OAuth for verification.
Configure your username and password in "FILELOCATION OF SWAGGER PSWD"
## Unit Tests
Tests can be created with JUnit and there are already some simple Tests for
## Integration test
All JPARepositories can be tested with JPA Data Tests. Examples can be found in [test folder](link).

## Licenses
The E-Display Backend is free software. It is licensed under GNU GENERAL
PUBLIC LICENSE Version 3 from 29 June 2007.
More info can be found [here](https://www.gnu.org/licenses/gpl-3.0.en.html)

### Third party components
- [Spring Boot Framework](https://spring.io/projects/spring-boot)
- [Hibernate](https://hibernate.org/)
- [PostgreSQL](https://www.postgresql.org/)
- [Flyway](https://flywaydb.org/)
- [Swagger](https://swagger.io/)
