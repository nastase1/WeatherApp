# WeatherApp Client-Server Application

## Description
WeatherApp is a console-based client-server application that enables users to log in, store their preferred location, and then query the current weather and forecast. The application saves the user’s name and selected location in a database, while the available locations are maintained in a JSON file. Additionally, an admin mode is available for authorized users to add new forecasts for a specific location.

## Features
- **User Authentication**: Log in with a username.
- **Location Storage**: After login, users can add a location, which is then stored in a database along with their name.
- **Weather Queries**: 
  - Retrieve the current weather and forecast for the saved location.
  - Option to query weather for a new location.
- **Admin Mode**: 
  - Secure admin login.
  - Ability to add or update weather forecasts for a location.
- **Data Management**:
  - User details are saved in a database.
  - Locations are loaded from a JSON file.

