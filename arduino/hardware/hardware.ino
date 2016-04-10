#include <Servo.h>

#define REDBUTTON 13
#define GREENBUTTON 12
#define BLUEBUTTON 8
#define FAN 5
#define SERVO 9
#define RED 3
#define GREEN 6
#define BLUE 11
#define DELAY 500
#define DELTA_T 50
#define CHAOS 1

Servo overs;
double r_curr, g_curr, b_curr;
int rPress, gPress, bPress;
int data = -1;

void setup() {
  int k;
  pinMode(REDBUTTON, OUTPUT);
  pinMode(GREENBUTTON, OUTPUT);
  pinMode(BLUEBUTTON, OUTPUT);
  pinMode(FAN, OUTPUT);
  overs.attach(SERVO);
  overs.write(0);
  pinMode(RED, OUTPUT);
  pinMode(GREEN, OUTPUT);
  pinMode(BLUE, OUTPUT);
  r_curr = 0; g_curr = 0; b_curr = 0;
  rPress = 1; bPress = 1; gPress = 1;

  Serial.begin(9600);
}

void loop() {
  int r, g, b;
  if(Serial.available() > 0) {
    data = Serial.read();
    switch(data) {
      case 'A':
        while(!Serial.available()){}
        set_temp(Serial.read());
        break;
      case 'B':
        while(Serial.available() < 3) {}
        r = Serial.read(); g = Serial.read(); b = Serial.read();
        set_color(r, g, b);
        break;
      case 'C':
        while(!Serial.available()){}
        set_fan(Serial.read());
        break;
      default:
        break;
    }
  }
//mood2();
}

void fade_betweens() {
  fade_to(255, 0, 0, DELAY * 10);
  delay(DELAY);
  fade_to(0, 255, 0, DELAY * 10);
  delay(DELAY);
  fade_to(0, 0, 255, DELAY * 10);
  delay(DELAY);
}

void flash_all(int num) {
  flash('r', num);
  flash('g', num);
  flash('b', num);
  flash('y', num);
  flash('c', num);
  flash('m', num);
  flash('w', num); 
}

void flash(char color, int recur) {
  for (int i = 0; i < recur; i++) {
    switch (color) {
      case 'r': set_color(255, 0, 0); break;
      case 'g': set_color(0, 255, 0); break;
      case 'b': set_color(0, 0, 255); break;
      
      case 'c': set_color(0, 255, 255); break;
      case 'm': set_color(255, 0, 255); break;
      case 'y': set_color(255, 255, 0); break;
     
      case 'w': set_color(255, 255, 255); break; 
    }
    delay(DELAY);
    off();
    delay(DELAY);
  } 
}

void fade_to(int r, int g, int b, int time) {
  int iterations = (int)(time / DELTA_T);
  double delta_r = (r_curr - r) / iterations;
  double delta_g = (g_curr - g) / iterations;
  double delta_b = (b_curr - b) / iterations;
  
  for (int i = 0; i < iterations; i++) {
    set_color(r_curr - delta_r, 
              g_curr - delta_g, 
              b_curr - delta_b);
    delay(DELTA_T);
  }
  
  set_color(r, g, b);
  delay(DELTA_T);
}


void set_color(double r, double g, double b) {
   r_curr = r; g_curr = g; b_curr = b;
   analogWrite(RED, r*2);
   analogWrite(GREEN, g*2);
   analogWrite(BLUE, b*2); 
}

void off() {
   set_color(0, 0, 0); 
}

void set_temp(int pos){
  overs.write(pos);
}

void set_fan(int spd){
  analogWrite(FAN, spd*2);
}

void set_press(double r,double g, double b ){
  if (r != 0){
    analogWrite(REDBUTTON, 50);
  } else {
    analogWrite(REDBUTTON, 0);
  }
  if( b != 0) {
    analogWrite(BLUEBUTTON,50);
  } else {
    analogWrite(BLUEBUTTON, 0);
  }
  if (g != 0){
    analogWrite(GREENBUTTON, 50);
  } else {
    analogWrite(GREENBUTTON, 0);
  }
}

