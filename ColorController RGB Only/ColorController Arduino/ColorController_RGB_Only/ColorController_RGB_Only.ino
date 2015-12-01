

// use these values for the red, blue, green led.
const int ledPinR   = 3;  // pwm pin with red led
const int ledPinG   = 5; // pwm pin with green led
const int ledPinB   = 6; // pwm pin with blue led


int brightnessVal = 150; // value that changes between 0-255


// getRGB function stores RGB values in this array
int rgb_colors[3];
int hue;
int hueVal = 200;
int saturation;
int brightness;
int cycleBit;
int fadeBit;

int cycleSpeed = 4; // 'speed' of color change
int fadeSpeed = 2; // 'speed' of fading
int fadeSlo = (fadeSpeed / 2);

String str; //string to store the incoming data from the Serial port
String hueString, hueStringTemp; // string that stores the hue
String saturationString, saturationStringTemp; //string that stores the saturation
String brightnessString, brightnessStringTemp; //string that stores the brightness
String fadeBitStr;
String cycleSpeedStr;
String fadeSpeedStr;



void setup()
{
  pinMode(ledPinR, OUTPUT);
  pinMode(ledPinG, OUTPUT);
  pinMode(ledPinB, OUTPUT);
  Serial.begin(57600);
  Serial.println("starting");

}


void loop()  {
  if (Serial.available() > 8)
  {
    str = Serial.readStringUntil('\n');

  }


  hueStringTemp = str.substring(0, 3);
  saturationStringTemp = str.substring(3, 6);
  brightnessStringTemp = str.substring(6, 9);


  saturation = saturationString.toInt();
  if (saturationStringTemp != saturationString){
    saturationString = saturationStringTemp;
  }
  if (hueStringTemp != hueString){
    hueString = hueStringTemp;
  }
  if (brightnessStringTemp != brightnessString){
    brightnessString = brightnessStringTemp;
  }

  hueVal = hueString.toInt();
  saturation = saturationString.toInt();
  brightness = brightnessString.toInt();
  analogWrite(ledPinR, hueVal);
  analogWrite(ledPinG, saturation);
  analogWrite(ledPinB, brightness);

}

