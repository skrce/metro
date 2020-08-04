# Metro routes and stops info

Standalone server that's using MBTA Api to provide useful information.

## Technologies

* Kotlin
* Spring 
* Jackson
* RestTemplate
* Swagger
* Jupiter
* Caffeine
* Maven

---

## Configuration
Should update application-default.yml file with: security user and password, and MBTA apiKey

---

# Build
In metro/ folder
* mvn clean install


# Run
In metro/ folder: 
* mvn spring-boot:run

# Operate
Access http://localhost:9099/swagger-ui.html

---

## Endpoints

**Endpoint** -  /{metroName}/routes/long-names/attribute-type/{attributeType}

**Method** - GET

**Request Parameters**

| Name        | Type           
| ------------- |-------------
| metroName     | String
| attributeType | String        
  
**Response**

    {
      "longNames": [
        "Mattapan Trolley",
        "Green Line B",
        "Green Line C",
        "Green Line D",
        "Green Line E"
      ]
    }

**Example**

    curl -H "Content-Type: application/json" -X GET 'http://<USER>:<PASSWORD>@localhost:9099/MBTA/routes/long-names/attribute-type/0'

---

**Endpoint** -  /{metroName}/routes/most-stops-route

**Method** - GET

**Request Parameters**

| Name        | Type           
| ------------- |-------------
| metroName     | String
  
**Response**

    {
      "routeName": "34E",
      "numberOfStops": 198
    }

**Example**

    curl -H "Content-Type: application/json" -X GET 'http://<USER>:<PASSWORD>@localhost:9099/MBTA/routes/most-stops-route'
        
---

**Endpoint** -  /{metroName}/routes/least-stops-route

**Method** - GET

**Request Parameters**

| Name        | Type           
| ------------- |-------------
| metroName     | String
  
**Response**

    {
      "routeName": "Boat-F4",
      "numberOfStops": 2
    }

**Example**

    curl -H "Content-Type: application/json" -X GET 'http://<USER>:<PASSWORD>@localhost:9099/MBTA/routes/least-stops-route'
    
---

**Endpoint** -  /{metroName}/stops/multiple-routes-stops

**Method** - GET

**Request Parameters**

| Name        | Type           
| ------------- |-------------
| metroName     | String
  
**Response**

    {
      "stopInfos": [
        {
          "stopName": "Alewife",
          "routeNames": [
            "Red",
            "62",
            "67",
            "76",
            "79",
            "84",
            "350",
            "351"
          ]
        },
        {
          "stopName": "Davis",
          "routeNames": [
            "Red",
            "87",
            "88",
            "89",
            "90",
            "94",
            "96"
          ]
        }...
      ]
    }
    
**Example**

    curl -H "Content-Type: application/json" -X GET 'http://<USER>:<PASSWORD>@localhost:9099/MBTA/stops/multiple-routes-stops'
    
---


**Endpoint** -  /{metroName}/trip/search-routes

**Method** - GET

**Request Parameters**

| Name          | Type         |  
| ------------- |------------- 
| metroName     | String
| beginStopName | String
| endStopName   | String
  
**Response**

    {
      "routeNames": [
        "Orange"
      ]
    }
    
**Example**

    curl -H "Content-Type: application/json" -X GET 'http://<USER>:<PASSWORD>@localhost:9099/MBTA/trip/search-routes?beginStopName=Oak%20Grove&endStopName=Assembly'
        
