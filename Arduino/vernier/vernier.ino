#include <math.h>

float rawCounts[4];
float voltages[4];

bool connectd = false;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  connect();
}

void loop() {
  //Get probe data
  rawCounts[0]=analogRead(A0);
  rawCounts[1]=analogRead(A1);
  rawCounts[2]=analogRead(A2);
  rawCounts[3]=analogRead(A3);

  voltages[0]=rawCounts[0]/1023*5;
  voltages[1]=rawCounts[1]/1023*5;
  voltages[2]=rawCounts[2]/1023*5;
  voltages[3]=rawCounts[3]/1023*5;
  
  Serial.print("<HAV_pw>");
  Serial.print(rawCounts[0]);
  Serial.print(",");
  Serial.print(rawCounts[1]);
  Serial.print(",");
  Serial.print(rawCounts[2]);
  Serial.print(",");
  Serial.println(rawCounts[3]);
  
  delay(1000);  
}

void connect(){
  while(connectd == false){
    String input = Serial.readStringUntil('\n');
    if(input.startsWith("<HAV_pi>")){
      Serial.println("<HAV_pw>");
      connectd = true;
      break;
    }
  }
}
/*
float thermistor(int raw) {
  float resistor=15000; //initialize value of fixed resistor in Vernier shield
  float resistance; //create local variable for resistance
  float temp; //create local variable for temperature
 
  resistance=log(resistor*raw/(1024-raw)); //calculate resistance
  temp = 1 / (0.00102119 + (0.000222468 * resistance) + (0.000000133342 * resistance * resistance * resistance)); //calculate temperature using the Steinhart-Hart equation
  temp = temp - 273.15; //Convert Kelvin to Celsius                      
  return temp; //return the temperature
}
*/
