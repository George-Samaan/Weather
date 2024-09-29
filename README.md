# Weather - Your Forecast Guide

## Project Overview
An Android Kotlin Mobile Application that provides real-time weather updates, forecasts, and alerts for your location or any other saved locations. You can receive weather notifications, manage multiple locations, and customize preferences like temperature units and language settings.

## Features

### Weather Updates
- **Current Weather:** Displays real-time weather details including temperature, condition, humidity, wind speed, and more for your current location.
- **5-Day Forecast:** Access a 5-day weather forecast with 3-hour intervals to help plan ahead.

### Alerts and Notifications
- **Weather Alerts:** Set up notifications for specific weather conditions like rain, storms, or extreme temperatures.
- **Scheduled Notifications:** Receive daily or scheduled notifications about the weather at your selected locations.

### Location Management
- **Dynamic Location:** Automatically updates weather based on your current location using GPS.
- **Multiple Locations:** Save and manage multiple favorite locations to quickly check the weather at any time.
- **Map Integration:** Select locations on an integrated map to set as favorite or view weather details.

### Customization and Preferences
- **Units of Measurement:** Choose between Celsius, Fahrenheit, and Kelvin for temperature, and metric or imperial units for wind speed.
- **Language Support:** Multi-language support, currently available in English and Arabic.

## Technical Details

### Architecture
- **MVVM Pattern:** Ensures a clean separation of UI, business logic, and data handling, making the app maintainable and testable.

### Modern Android Development Tools
- **Kotlin Coroutines & Flow:** For asynchronous operations and reactive programming.
- **StateFlow:** Lifecycle-aware state management, ensuring data persistence across app lifecycle events.
- **Room Database:** Used for offline storage of weather data, allowing the app to function without an active internet connection.
- **WorkManager & AlarmManager:** Handles background tasks like fetching weather updates and scheduling weather alerts.
- **ViewModel:** Ensures data is retained across configuration changes, such as screen rotations.

### Networking
- **Retrofit:** Used for making API calls to fetch weather data.

### Location Services
- **Google Location Services:** Automatically detects the user’s current location to provide relevant weather updates.
- **Geocoder:** Converts latitude/longitude into human-readable addresses.

### Notifications
- **Android Notification Channels:** Ensures proper handling of weather alerts, including silent and priority notifications.

### Additional Libraries
- **Lottie:** Provides high-quality animations for a polished user experience, particularly in the splash screen.
