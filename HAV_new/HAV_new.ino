#include<Servo.h>

//Serial Vars
boolean connectd = false;
const byte numChars = 32;
char receivedChars[numChars];
boolean newData = false;

//Thruster/Servo Vars
Servo thruster_v1;
Servo thruster_v2;
Servo thruster_v3;
Servo thruster_l;
Servo thruster_r;

Servo servo_s1;
Servo servo_s2;

int thrusterPin_v1 = 6;
int thrusterPin_v2 = 7;
int thrusterPin_v3 = 8;
int thrusterPin_l = 9;
int thrusterPin_r = 10;
int servoPin_s1 = 11;
int servoPin_s2 = 12;

//Probeware Vars


/*
 * START OF PROGRAM
 */

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  
  Serial.println("Arduino is ready");
  
  connectToPC();
  
  Serial.println("Serial is ready");

  servo_s1.attach(servoPin_s1);
  servo_s2.attach(servoPin_s2);

  thruster_v1.attach(thrusterPin_v1);
  thruster_v2.attach(thrusterPin_v2);
  thruster_v2.attach(thrusterPin_v3);
  thruster_l.attach(thrusterPin_l);
  thruster_r.attach(thrusterPin_r);

  servo_s1.write(90);
  servo_s2.write(90);
  
  thruster_v1.writeMicroseconds(1500);
  thruster_v2.writeMicroseconds(1500);
  thruster_v3.writeMicroseconds(1500);
  thruster_l.writeMicroseconds(1500);
  thruster_r.writeMicroseconds(1500);

  Serial.println("Thrusters and Servos are ready");
  
  delay(500);
}

void loop() {
  // Serial Receiving/Handler Code
  recvData();
  int type = parseNewData();
  if(type != -1){
    switch(type){
      case 1:
        controlServo(1, receivedChars);
        Serial.println("");
        break;
      case 2:
        controlServo(2, receivedChars);
        Serial.println("");
        break;
      case 3:
        controlHorizontal(receivedChars);
        Serial.println("");
        break;
      case 4:
        controlVertical(receivedChars);
        Serial.println("");
        break;
      default:
        break;
    }
  }else {
    Serial.println("Error: CMD not recognized");
  }

}

/*
 * SERIAL CODE from https://forum.arduino.cc/index.php?topic=288234.0
 */

void connectToPC() {
  while(connectd == false){
    recvData();
    parseNewData();
    if(strcmp(receivedChars,"HAV_pi")==0){
      Serial.println("<HAV_rb>");
      connectd = true;
      break;
    }
}
}

void recvData() {
    static boolean recvInProgress = false;
    static byte ndx = 0;
    char startMarker = '<';
    char endMarker = '>';
    char rc;
 
    while (Serial.available() > 0 && newData == false) {
        rc = Serial.read();

        if (recvInProgress == true) {
            if (rc != endMarker) {
                receivedChars[ndx] = rc;
                ndx++;
                if (ndx >= numChars) {
                    ndx = numChars - 1;
                }
            }
            else {
                receivedChars[ndx] = '\0'; // terminate the string
                recvInProgress = false;
                ndx = 0;
                newData = true;
            }
        }

        else if (rc == startMarker) {
            recvInProgress = true;
        }
    }
}

int parseNewData() {
    int out = -1;
    if (newData == true) {
        Serial.print("RECIEVED: ");
        Serial.println(receivedChars);

        if(receivedChars[0] == 's'){
          if(receivedChars[1] == '1'){
            //Serial.println("CMD: Servo 1");
            out = 1;
          }else if(receivedChars[1] == '2'){
            //Serial.println("CMD: Servo 2");
            out = 2;
          }
        }else if(receivedChars[0] == 't'){
          if(receivedChars[1] == 'h'){
            //Serial.println("CMD: Horizontal");
            out = 3;
          }else if(receivedChars[1] == 'v'){
            //Serial.println("CMD: Vertical");
            out = 4;
          }
        }
        newData = false;
    }else {
      out = 0;
    }
    return out;
}

void clearData(char data[]){
  for(int i = 0; i < 32; i++){
    data[i] = data[i+3];
  }
}

/*
 * THRUSTER CODE
 */

void controlServo(int servo, char command[]){
  clearData(command);
  if(servo == 1){
    Serial.print("CMD: Servo 1 to ");
    Serial.print(atoi(command));
    Serial.println(" degrees.");

    servo_s1.write((int) atoi(command));
    
  }else if(servo == 2){
    Serial.print("CMD: Servo 2 to ");
    Serial.print(atoi(command));
    Serial.println(" degrees.");
    
    servo_s2.write((int) atoi(command));
  }
}

void controlHorizontal(char command[]){
  clearData(command);

  int leftVal = 0;
  int rightVal = 0;
  
  //Parse data
  char cmd[16];

  int commaIndex = 0;
  
  for(int i = 0; i < 32; i++){
    if(command[i] == ','){
      commaIndex = i;
      break;
    }
  }
  for(int i = 0; i < 16; i++){
    if( i < commaIndex){
      cmd[i] = command[i];
    }
  }
  cmd[commaIndex] = 0;
  leftVal = convertToMS(atof(cmd));
  
  for(int i = 0; i < 16; i++){
    cmd[i] = 0;
  }
  
  for(int i = commaIndex+1; i < 16; i++){
    cmd[i-commaIndex-1] = command[i];
  }

  rightVal = convertToMS(atof(cmd));
  
  Serial.print("CMD: Left Thruster to ");
  Serial.println(leftVal);
  Serial.print("CMD: Right Thruster to ");
  Serial.println(rightVal);

  thruster_l.writeMicroseconds(leftVal);
  thruster_r.writeMicroseconds(rightVal);
}

void controlVertical(char command[]){
  clearData(command);
  
  int microSeconds = convertToMS(atof(command));
  Serial.print("CMD: Vertical Thrusters to ");
  Serial.println(microSeconds);

  thruster_v1.writeMicroseconds(microSeconds);
  thruster_v2.writeMicroseconds(microSeconds);
  thruster_v3.writeMicroseconds(microSeconds);
}

int convertToMS(float input){
  input = constrain(input, -1,1);
  return input*400+1500;
}

/*
 * PROBE CODE
 */

