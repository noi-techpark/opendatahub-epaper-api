# it.bz.opendatahub.epaper.api

[![CI/CD](https://github.com/noi-techpark/it.bz.opendatahub.epaper.api/actions/workflows/main.yml/badge.svg)](https://github.com/noi-techpark/it.bz.opendatahub.epaper.api/actions/workflows/main.yml)

REST API for the E-Ink-Displays System Webapp.
Communicates with Ardunios over WIFI HTTP to send images and make state requests.

2 python proxies to be able to communicate if API is on remote server, because in that case the server is outside the local WIFI network
and can't communicate directly with Arduinos.

So instead of using something like dyndns on every Ardunio, we choose to sue one proxy to have only one open connection.
There are 2 types of proxies, one that needs a tunnel (like ngrok or localtunnel, or also dyndns) and yhe communicates over HTTP with the API.
The other one uses Web Sockets to create one bidirectional connection to the remote API, so only the URL of the remote API and no tunnel is necessary.

The Ardunios can also auto connect to the API over UDP with the proxy, so the displays can be just plugged in and connect automatically.
But that is only possible with the python proxies, so auto connect doesn't work without proxy.

Created with [Spring BootFramework](https://spring.io/projects/spring-boot),
[Hibernate](https://hibernate.org/) and
[PostgreSQL](https://www.postgresql.org/) with [Flyway](https://flywaydb.org/)
DB Version control system. The API can do CRUD operations on Displays,
Locations, Templates and Connection between Display and Location. Templates are
predefined Images that can be modified and loaded on the Displays.

**Table of Contents**
- [it.bz.opendatahub.epaper.api](#itbzopendatahubepaperapi)
	- [Installation guide](#installation-guide)
		- [Source code](#source-code)
	- [Run Application](#run-application)
		- [Execute without Docker](#execute-without-docker)
			- [Database](#database)
			- [Application](#application)
		- [Execute with Docker](#execute-with-docker)
		- [Execute with local proxy if you deploy on remote server](#execute-with-local-proxy-if-you-deploy-on-remote-server)
			- [Use HTTP communication between API and Proxy](#use-http-communication-between-api-and-proxy)
			- [Use Web Socket communication between API and Proxy](#use-web-socket-communication-between-api-and-proxy)
		- [Show today.noi.bz.it events](#show-todaynoibzit-events)
		- [Heartbeat](#heartbeat)
	- [Set up to send image to display](#set-up-to-send-image-to-display)
	- [Swagger](#swagger)
	- [Unit Tests](#unit-tests)
	- [Integration test](#integration-test)
	- [Licenses](#licenses)
		- [Third party components](#third-party-components)


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

### Execute with local proxy if you deploy on remote server

The proxies are written in python, so check that you installed at least python version 3 on your machine.

If you deploy the application on a remote server, you need to setup a local proxy.
So the application can communicate with the proxy instead of  trying to communicate directly with the displays and the proxy will forward the requests.
You can **install** the proxy that you can find in proxy directory on a local machine or a Raspberry Pi
**Note:** You need to config the firewall of the machine the proxy is running to allow incoming traffic for the **UDP** auto-connection


There are **2 types of proxies**, the normal proxy that uses **HTTP** to talk to the API and another one that uses **Web Sockets**.
If you want to use HTTP, you need to make the proxy visible to the API, by using SSH tunnels or tunnels with localtunnel, ngrok or even dynamic dns.
But by using the Web Socket proxy, only the API needs a link, because once the proxy connected to the API the connection stays awake.
Web Socket are also bidirectional, so ones the connection is opened, the proxy can communicate with the API and backwards with the same connection.
So there's no need for tunnels etc. with WebSockets
**Note:** The plan is to try Web Sockets also for communication between Arduino and proxy or even directly with the API, but it's still in development


#### Use HTTP communication between API and Proxy


If you want to use HTTP connection between API and proxy follow the next steps.

```
cd proxy
pip install requirements.txt
```

And the **start** the proxy
```
python proxy.py
```

The you need make you proxy visible to the internet. There are many ways, one is using localtunnel.
With the following commands you can install and run it on the proxys port.
(NOTE: at time of writing, localtunnel does not work, so you need to set custom host serverless.social)
```
npm install -g localtunnel
lt -h "http://serverless.social" -p 5000
```

Make copy of .env.example in the **Spring configuration** and name it .env if you're using Docker. Otherwise just use application.properties as .env file.
Then localtunnel gives you an unique URL that represents you proxy that you can change values in .env.
Set remote to true and paste the URL to remoteIPAddress
```
PROXY_ENABLED=true
PROXY_URL=YOUR_PROXY_URL
```
Then your API is ready to communicate with the proxy.
Note: Make sure that your proxy is in the same WIFI network as your displays.

#### Use Web Socket communication between API and Proxy

If you want to use Web Socket connection between API and proxy follow the next steps.

```
cd websocket-proxy
```

Set the URL of your API in API_URL and the corresponding Web Socket URL to WS_URL in the **.env** file
Normally its like API_URL=http://yoursite.com/ and WS_URL=http://yoursite.com/ws/
```
# .env
# like http://localhost/
API_URL=
# like ws://localhost/ws
WS_URL=
```

Then **install all requirements** with the following command

```
pip install requirements.txt
```

And the **start** the proxy
```
python websocket-proxy.py
```

Make copy of .env.example in the **Spring configuration** and name it .env if you're using Docker. Otherwise just use application.properties as .env file.
Then localtunnel gives you an unique URL that represents you proxy that you can change values in .env.
Set remote to true and websocket true
```
PROXY_ENABLED=true
WEBSOCKET_ENABLED=true
```
Then your API is ready to communicate with the proxy.
Note: Make sure that your proxy is in the same WIFI network as your displays.

### Show today.noi.bz.it events

You can show events info from today.noi.bz.it on displays. To do so enable in .env file following values
NOI_EVENTS_ENABLED enables the service
NOI_CRON_EVENTS is the cron job that fetches the events periodically from the OpenDataHub
NOI_CRON_DISPLAYS is the cron job that defines how often the displays get checked if a new event should be displayed

```
NOI_EVENTS_ENABLED=false
NOI_CRON_EVENTS=0 0 0/12 * * ?
NOI_CRON_DISPLAYS=0 0/10 6-24 * * ?
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

### Heartbeat

The API makes periodically a state request to the displays to see if they are still connected.
You can modify the interval in .env file
(Future plans: make heartbeat also refreshing display image every month/week, to prevent ghosting of image on e-ink display)

```
CRON_HEARTBEAT=0 0 0/1 * * ?
```

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
