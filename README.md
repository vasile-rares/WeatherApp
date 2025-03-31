# Weather App 🌤️

[![Java](https://img.shields.io/badge/Java-23-red.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9.6-C71A36.svg)](https://maven.apache.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A full-stack weather application that provides real-time weather information and forecasts. The application is built using Java and follows a client-server architecture.

## What does this app do? 🤔

This Weather App is a comprehensive solution for weather monitoring and forecasting. Here's what it can do:

-   **Location Management**: Store and manage multiple locations with their coordinates
-   **Real-time Weather**: Get current weather conditions for any saved location
-   **Weather Forecasting**: View weather predictions for upcoming days
-   **Data Persistence**: Automatically save weather data in a PostgreSQL database
-   **Historical Data**: Access past weather records for trend analysis
-   **User Interface**: Simple and intuitive interface for weather information display

## Project Structure 🏗️

The project is divided into two main components:

-   `ClientWeather/`: The client-side application
-   `ServerWeather/`: The server-side application that handles weather data and database operations

## Features ✨

-   Real-time weather information
-   Weather forecasts
-   Database storage for weather data
-   RESTful API endpoints
-   Client-server communication

## Prerequisites 🛠️

Before you begin, ensure you have the following installed:

-   Java Development Kit (JDK) 23 or higher
-   Maven (for dependency management)
-   PostgreSQL database
-   IDE (recommended: IntelliJ IDEA)

## Setup Instructions ⚙️

1. **Database Setup** 🗄️

    - Install and configure PostgreSQL
    - Create a new database for the weather application
    - Run the following SQL commands to set up the database schema:

    ```sql
    CREATE DATABASE weather_data;

    \c weather_data

    CREATE TABLE locations (
        id SERIAL PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        latitude DOUBLE PRECISION NOT NULL,
        longitude DOUBLE PRECISION NOT NULL
    );

    CREATE TABLE forecasts (
        id SERIAL PRIMARY KEY,
        location_id INT NOT NULL,
        date DATE NOT NULL,
        weather VARCHAR(255) NOT NULL,
        temperature DOUBLE PRECISION NOT NULL,
        FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE CASCADE
    );
    ```

    - Update the database connection settings in the server configuration

2. **Server Setup** 🖥️

    ```bash
    cd ServerWeather
    mvn clean install
    ```

    - The server will be compiled and dependencies will be downloaded

3. **Client Setup** 💻
    ```bash
    cd ClientWeather
    mvn clean install
    ```
    - The client will be compiled and dependencies will be downloaded

## Running the Application 🚀

To run the application, follow these steps in order:

1. **Start the Database** 🗄️

    - Make sure PostgreSQL is running
    - Verify that the `weather_data` database is accessible

2. **Start the Server** 🖥️

    - Navigate to the ServerWeather directory
    - Run the server application
    - The server will start on the default port (check server logs for confirmation)

3. **Start the Client** 💻
    - Navigate to the ClientWeather directory
    - Run the client application
    - The client will connect to the server automatically

Once all components are running, you can access the weather application through the client interface.

## Dependencies 📦

The project uses the following main dependencies:

-   `org.json:json` (20180813) - For JSON processing
-   `org.postgresql:postgresql` (42.5.0) - PostgreSQL JDBC driver
-   `com.google.code.gson:gson` (2.10) - For JSON serialization/deserialization

## Contributing 🤝

Feel free to contribute to this project by:

-   Creating issues for bugs or feature requests
-   Submitting pull requests with improvements
-   Suggesting enhancements through discussions

## License 📄

This project is licensed under the MIT License - see the LICENSE file for details.
