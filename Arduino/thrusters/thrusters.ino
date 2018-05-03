#include <Servo.h>

bool connectd = false;

int lThrustPin = 9;
int rThrustPin = 10;

Servo lThruster;
Servo rThruster;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

  connect();

  Serial.println("<HAV_rb> init0");

  lThruster.attach(lThrustPin);
  rThruster.attach(rThrustPin);

  lThruster.writeMicroseconds(1500);
  rThruster.writeMicroseconds(1500);

  delay(7000);

  Serial.println("<HAV_rb> init1");
}

void loop() {
  // put your main code here, to run repeatedly:
  while(Serial.available() == 0){}
  String line = Serial.readStringUntil('\n');
  Serial.print("<HAV_rb> 222:");
  Serial.println(line);
  int lSignal, rSignal;

  bool hasComma = false;
  for(int i =0; i< line.length(); i++){
    if(line.charAt(i) == ','){
      hasComma = true;
    }
  }
  if(hasComma){
    if(line.startsWith("<HAV_")){
      Serial.println("<HAV_2223" + line.substring(10)[0]);
      Serial.println("<HAV_2224" + line.substring(10)[1]);
      line = line.substring(10)[1];
    }
    String lstr = line.substring(0, line.indexOf(","));
    String rstr = line.substring(line.indexOf(",")+1);
    Serial.print("<HAV_rb>");
    Serial.print(lstr.toFloat());
    Serial.print(", ");
    Serial.println(rstr.toFloat());
    lSignal=convertToMS(lstr.toFloat());
    rSignal=convertToMS(rstr.toFloat());
  }

  if((lSignal > 1100 && lSignal < 1900) && (rSignal > 1100 && rSignal < 1900)){
    Serial.print("<HAV_rb> ");
    Serial.print(lSignal);
    Serial.print(", ");
    Serial.println(rSignal);
    lThruster.writeMicroseconds((int) lSignal);
    rThruster.writeMicroseconds((int) rSignal);
  }
}

void connect(){
  while(connectd == false){
    String input = Serial.readStringUntil('\n');
    if(input.startsWith("<HAV_pi>")){
      Serial.println("<HAV_rb>");
      connectd = true;
      break;
    }
  }
}

int convertToMS(float input){
  input = constrain(input, -1,1);
  return input*400+1500;
}

