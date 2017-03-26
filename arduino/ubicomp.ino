#include "DHT.h"

#define OFF_MODE 1
#define AUTOMATIC_MODE 2
#define MANUAL_MODE 3

#define SLEEP_NORMAL 5000L
#define SLEEP_EXTENDED 30000L

#define LED_PIN 6
#define MQ2_PIN A1
#define DHTPIN 7
#define DHTTYPE DHT11

DHT dht(DHTPIN, DHTTYPE);

int mode = OFF_MODE;
unsigned long delayTime = SLEEP_NORMAL;
int sendData = 0;

void setup() {
  pinMode(LED_PIN, OUTPUT);
  Serial.begin(9600);   

  dht.begin();
}

void loop() {

  //receiving data via Bluetooth
  if(Serial.available()) {
    mode = Serial.read();
  }

  if(mode == OFF_MODE) {
    if (digitalRead(LED_PIN) == HIGH) {
      digitalWrite(LED_PIN, LOW);
      digitalWrite(MQ2_PIN, LOW); 
      digitalWrite(DHTPIN, LOW); 
    }
    delayTime = SLEEP_NORMAL;
  } else {
    if (digitalRead(LED_PIN) == LOW) { 
      digitalWrite(LED_PIN, HIGH); 
      digitalWrite(MQ2_PIN, HIGH);
      pinMode(MQ2_PIN, INPUT);
      digitalWrite(DHTPIN, HIGH);
    }
    
    if (mode == AUTOMATIC_MODE) {
      delayTime = SLEEP_NORMAL; 
      sendSensorData();
    } else if (mode == MANUAL_MODE) { 
      delayTime = SLEEP_EXTENDED;
      sendData++; 
      if (sendData == 2) {
        sendSensorData();
        sendData = 0;
        mode = OFF_MODE;
        delayTime = 100;
      } else {
        int mq2 = analogRead(MQ2_PIN);
      }
    }
  }

  delay(delayTime);                      
}

void sendSensorData() {
  int h = dht.readHumidity(); 
  int t = dht.readTemperature(); // Read temperature as Celsius
  int f = dht.readTemperature(true); // Read temperature as Fahrenheit
  float hif = dht.computeHeatIndex(f, h); // Compute heat index in Fahrenheit
  float hic = dht.computeHeatIndex(t, h, false); // Compute heat index in Celsius

  int mq2 = analogRead(MQ2_PIN);
  //sending data via Bluetooth   
  String data = "H: ";
  data += h;
  data += ", T: ";
  data += t;
  data += ", HIC: ";
  data += hic;
  data += ", MQ2: ";
  data += mq2;
  data += ", MODE: ";
  data += mode;
  data += "#";
  Serial.println(data);
}

