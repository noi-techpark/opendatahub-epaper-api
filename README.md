<!--
SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>

SPDX-License-Identifier: CC0-1.0
-->

# Open Data Hub E-Paper API

[![REUSE Compliance](https://github.com/noi-techpark/it.bz.opendatahub.epaper.api/actions/workflows/reuse.yml/badge.svg)](https://github.com/noi-techpark/odh-docs/wiki/REUSE#badges)
[![CI/CD](https://github.com/noi-techpark/it.bz.opendatahub.epaper.api/actions/workflows/main.yml/badge.svg)](https://github.com/noi-techpark/it.bz.opendatahub.epaper.api/actions/workflows/main.yml)

A REST API service for managing E-Ink Display Systems. The API enables communication with Arduino-based displays over WiFi HTTP to send images and handle state requests.

Created with [Spring BootFramework](https://spring.io/projects/spring-boot),
[Hibernate](https://hibernate.org/) and
[PostgreSQL](https://www.postgresql.org/) with [Flyway](https://flywaydb.org/)
DB Version control system. The API can do CRUD operations on Displays,
Locations and Templates. Templates are predefined Images that can be modified and loaded on the Displays.

# Features
- CRUD operations for managing Displays, Locations and Templates
- Template-based image generation and modification
- Real-time display state synchronization
- Integration with NOI event data
- Image processing and conversion for e-paper displays
- Status and error monitoring
- S3-compatible storage for images
- Support for multiple display resolutions

# Technology Stack
- [Spring BootFramework](https://spring.io/projects/spring-boot) - Core framework
- [Hibernate](https://hibernate.org/) - ORM and database operations
- [PostgreSQL](https://www.postgresql.org/) - Database
- [Flyway](https://flywaydb.org/)
- [Swagger]()

**Table of Contents**
- [Open Data Hub E-Paper API](#open-data-hub-e-paper-api)
- [Features](#features)
- [Technology Stack](#technology-stack)
	- [Installation guide](#installation-guide)
		- [Source code](#source-code)
	- [Run Application](#run-application)
		- [Execute without Docker](#execute-without-docker)
			- [Database](#database)
			- [Application](#application)
		- [Execute with Docker](#execute-with-docker)
		- [Show today.noi.bz.it events](#show-todaynoibzit-events)
	- [Set up to send image to display](#set-up-to-send-image-to-display)
	- [Swagger](#swagger)
	- [Unit Tests](#unit-tests)
	- [Integration test](#integration-test)
	- [Licenses](#licenses)
		- [Third party components](#third-party-components)
	- [REUSE](#reuse)


## Installation guide

### Source code

Get a copy of the repository:

```bash
git clone https://github.com/noi-techpark/it.bz.opendatahub.epaper.api.git
```

Change directory:

```bash
cd it.bz.opendatahub.epaper.api
```

## Run Application
Run **MainApplicationClass.java** in your IDE and Spring Boot will start, Flyway create tables in your database and the the API is ready to use! Enjoy!

### Execute without Docker

#### Database
Install PostgreSQL on your machine (tested and developed on PSQL 12.5)
Create Database named **edisplays** with user **edisplay-user**
```sql
CREATE DATABASE epaper;
CREATE USER epaper;
```

Make sure that a default schema called `public` exists, and that the owner is `epaper`.

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

### Show today.noi.bz.it events

You can show events info from today.noi.bz.it on displays. To do so enable in .env file following values
NOI_EVENTS_ENABLED enables the service
NOI_CRON_EVENTS is the cron job that fetches the events periodically from the OpenDataHub
NOI_CRON_LOCATIONS is the cron job that fetches the locations periodically from the OpenDataHub

```
NOI_EVENTS_ENABLED=false
NOI_CRON_EVENTS=0 0 0/12 * * ?
NOI_CRON_LOCATIONS=0 0/10 6-24 * * ?
```
NOTE: The cron jobs annotations don't need to be modified. Just if you prefer other update times.

The scheduler cron annotation works as follows:
```
 ┌───────────── second (0 - 59)
 │ ┌───────────── minute (0 - 59)
 │ │ ┌───────────── hour (0 - 23)
 │ │ │ ┌───────────── day of the month (1 - 31)
 │ │ │ │ ┌───────────── month (1 - 12) (or JAN-DEC)
 │ │ │ │ │ ┌───────────── day of the week (0 - 7)
 │ │ │ │ │ │              (or MON-SUN -- 0 or 7 is Sunday)
 │ │ │ │ │ │
 * * * * * *
```

Where `*/10` means every 10 seconds/minutes/, whereas `0/10` means every 10
seconds/minutes/... but starting from 0. For example, for minutes that would be
`8:00`, `8:10` etc. See [this spring.io
blogpost](https://spring.io/blog/2020/11/10/new-in-spring-5-3-improved-cron-expressions)
for details.

## Set up to send image to display

- Start the API
- Set up a physical display by following the README of the [backend](https://github.com/noi-techpark/e-ink-displays-backend)
- Follow the next steps in the README of the [webapp](https://github.com/noi-techpark/e-ink-displays-webapp) to use the webapp to send the image

## Swagger

Swagger can be reached under http://localhost:8080/swagger-ui.html#/ and uses OAuth for verification.

## Unit Tests

Tests can be created with JUnit and there are already some simple Tests for

## Integration test
All JPARepositories can be tested with JPA Data Tests. Examples can be found in [test folder](https://github.com/noi-techpark/e-ink-displays-api/tree/development/src/test/java).

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

## REUSE

This project is [REUSE](https://reuse.software) compliant, more information about the usage of REUSE in NOI Techpark repositories can be found [here](https://github.com/noi-techpark/odh-docs/wiki/Guidelines-for-developers-and-licenses#guidelines-for-contributors-and-new-developers).

Since the CI for this project checks for REUSE compliance you might find it useful to use a pre-commit hook checking for REUSE compliance locally. The [pre-commit-config](.pre-commit-config.yaml) file in the repository root is already configured to check for REUSE compliance with help of the [pre-commit](https://pre-commit.com) tool.

Install the tool by running:
```bash
pip install pre-commit
```
Then install the pre-commit hook via the config file by running:
```bash
pre-commit install
```
