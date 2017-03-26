# Ubicomp project - Monitoring Air Quality on the Move

This project is build from three parts - Android app, server app and Arduino Uno platform, that work togehter to collect and monitor the data for air quality in our enviroment.

## Arduino Uno
**Arduino Uno** board is equiped with Bluetooth module, MQ-2 gas sensor and DHT11 temperature and humidity sensor. Arduino collects data from sensors and sends them via bluetooth connection to Android app. It can be controlled from Android app to collect and send sensor data periodically or manually, or to stop with this process.

## Android app
**Android app** is divided in two parts. First part is collecting data from Arduino and periodically sending them to server. The second part is presentation of collected sensor data.

Data collection:
Selection of Bluetooth icon from action bar opens part of application for collecting data. After pairing with Arduino via Bluetooth, communication can be established. Gathering data can be automatic or manual (on demand). In automatic mode, data from arduino is received every five seconds. In manual mode, only one sample of sensor data is received on demand, after wich Arduino stops with sending data and go to Off mode. After collecting, data is passed to server which stores it into database. Interval for sending data to server, as server ip address, can be set in app in runtime. App can also trigger manual mode when the user location is changed by 500m. 
Data presentation:
Main application screen contains map, chart and details tabs. Presentation on map with markers shows temperature, humidity and air quality. Charts tab shows all data gathered from Arduino as bar chart and details tab present last gathered result and maximum result by temperature for selected date.
Side menu is used for filtering data by temperature, humidity, air quality and/or radius from current position.

## Server app
**Server app** is written using Node.js framework. It implements RESTful api for inserting, getting and filtering data stored in MySQL database.
