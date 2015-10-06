import controlP5.*;
import processing.serial.*;
import java.awt.Color;

import ddf.minim.analysis.*;
import ddf.minim.*;

ControlP5 controlP5;
Serial myPort;

//Minim Variable Start ###################
Minim minim;
AudioInput in;
FFT fft;

int buffer_size = 1024;  // also sets FFT size (frequency resolution)
float sample_rate = 44100;
int spectrum_height = 176; // determines range of dB shown

float heightBass;
float heightMid;
float heightTreble;
float bassTemp, midTemp, trebleTemp;
float inputNormalized;
long lastCall;
float bassSend, midSend, trebleSend;
float bass, mid, treble;
String bassString, midString, trebleString;
//Minim Variable End ###################


//Definitions for sliders in order to move them around easily
int sliderWidth = 500;
int sliderHeight = 35;
int sliderStart = 0;
int sliderEnd = 255;
float sliderStartVal = 127.5;
int sliderX = 40;
int sliderY = 40;
int offset = 35; //make at least slider width
int doubleOffset = offset*2;
int toggleX = 100;
int toggleXoffset = 100;
int toggleY = 200;
int toggleW = 50;
int toggleH = 50;
int knobSize;
int smallStart = 0;
int smallEnd = 9;
int smallWidth = 100;
int smallHeight = 20;
int smallStartVal = 0;
int smallOffset = 30;
int smallX = 400;
int smallY = toggleY;

int hueNum = 0;
float hueNumClean = 0;
int saturationNum = 0;
int brightnessNum = 0;
byte hueByte = 0;
byte saturationByte = 0;
byte brightnessByte = 0;
color HSBcolor;
String hueString = "000";
String saturationString = "000";
String brightnessString = "255";
String cycleString = "0";
String sendStringOne, sendStringTwo;
String breatheString = "0";
String breatheSpeedString = "0";
String cycleSpeedString = "0";
int cycleNum = 0;
int breatheNum = 0;

color hueC = color(255, 204, 0);
color satC;
color brightC;
int Speed;
int fading = 10;
int hueing = 10;
int fadeSpeed = 1;
int cycleSpeed = 0;
int smoothFactor = 15;

String[] serialString;  
String serialCheck;  
String portName = "COM3";  
int portNumber;  
int serialIndex;  

void setup() {

  size(600, 300);
  smooth();
  colorMode(HSB, 1030, 255, 255);
  int Hue;
  int Saturation;
  int Value;

  //Minim Setup Start #########
  minim = new Minim(this);
  in = minim.getLineIn(Minim.MONO, buffer_size, sample_rate);
  fft = new FFT(in.bufferSize(), in.sampleRate());
  //End Minim Setup #########


  findSerialPort();
  myPort = new Serial(this, Serial.list()[portNumber], 19200);  
  myPort.clear();  

  //myPort = new Serial(this, "COM3", 19200);
  controlP5 = new ControlP5(this);


  //add Hue Slider 
  Slider hue = controlP5.addSlider("Hue", sliderStart, 1000, 500, sliderX, sliderY, sliderWidth, sliderHeight); 
  hue.setSliderMode(Slider.FLEXIBLE);
  hue.setHandleSize(sliderHeight + 5);

  //add Saturation Slider
  Slider saturation = controlP5.addSlider("Saturation", sliderStart, sliderEnd, sliderStartVal, sliderX, sliderY+offset, sliderWidth, sliderHeight); 
  saturation.setSliderMode(Slider.FLEXIBLE);
  saturation.setHandleSize(sliderHeight + 5);

  //add Brightness Slider
  Slider brightness = controlP5.addSlider("Brightness", sliderStart, sliderEnd, sliderStartVal, sliderX, sliderY+doubleOffset, sliderWidth, sliderHeight); 
  brightness.setSliderMode(Slider.FLEXIBLE);
  brightness.setHandleSize(sliderHeight + 5);

  //add Cycle Speed Slider
  controlP5.addSlider("Speed", smallStart, smallEnd, smallStartVal, smallX, smallY, smallWidth, smallHeight).showTickMarks(true).snapToTickMarks(true).setNumberOfTickMarks(10);
  controlP5.getController("Speed").hide();

  //add Cycle Color Toggle
  Toggle colorCycle = controlP5.addToggle("Cycle Color", false, toggleX, toggleY, toggleW, toggleH);

  //add Fading Toggle
  Toggle fading = controlP5.addToggle("Breathing", false, toggleX + toggleXoffset, toggleY, toggleW, toggleH);

  //add Spectrum Toggle
  Toggle spectrum = controlP5.addToggle("Spectrum", false, toggleX*2 + toggleXoffset, toggleY, toggleW, toggleH);
}
void draw() {
  colorMode(HSB, 1030, 255, 255); 
  HSBcolor = color(hueNumClean, saturationNum, brightnessNum);
  background(HSBcolor);
  stroke(0);
  fill(100);
  rect(sliderX+50, sliderY, sliderWidth, (sliderHeight * 3), 2);
  rect(90, 100, 70, 170, 2);
  rect(190, 100, 70, 170, 2);
  rect(290, 100, 70, 170, 2);
  fill(150);
  rect(sliderX - 10, sliderY - 10, sliderWidth + 20, 20 + (sliderHeight * 3), 4);


  cycleString = String.format("%01d", cycleSpeed);
  hueNumClean = controlP5.getValue("Hue");
  hueNum = Math.round((hueNumClean / 1.1));
  saturationNum = (int)controlP5.getValue("Saturation");
  brightnessNum = (int)controlP5.getValue("Brightness");
  breatheNum = Math.round(controlP5.getController("Breathing").getValue());
  hueString = String.format("%03d", hueNum);
  saturationString = String.format("%03d", saturationNum);
  brightnessString = String.format("%03d", brightnessNum);

  if (controlP5.getController("Spectrum").getValue() == 0) {
    //changes color mode to HSB, used when lights aren't controlled by sound
    // in order to aid in ease of use and make program more intuitive

    if (controlP5.getController("Cycle Color").getValue() == 1) {
      controlP5.getController("Speed").show();
      if (hueing > 1019 || hueing < 1) {
        cycleSpeed = -cycleSpeed;
      }
      hueing = hueing + cycleSpeed;
      controlP5.getController("Hue").setValue(hueing);
    } else if (controlP5.getController("Cycle Color").getValue() == 0) {
      cycleNum = 0;
      controlP5.getController("Speed").hide();
      controlP5.getController("Speed").setValue(0.00);
    }
    if (breatheNum == 1) {
      if (fading == 255 || fading == 0) {
        fadeSpeed = -fadeSpeed;
      }
      fading = fading + fadeSpeed;
      controlP5.getController("Brightness").setValue(fading);
      breatheString = "1";
      brightnessNum = (int)controlP5.getValue("Brightness");
    } else {
      breatheString = "0";
      brightnessNum = (int)controlP5.getValue("Brightness");
    }
    sendStringOne = hueString + saturationString + brightnessString + breatheString  + "\n";
    myPort.write(sendStringOne);
    delay(25); //THIS IS THE DELAY, I'M NOT SURE IF I SHOULD REMOVE IT????
    
    /*
    *  This is the beginning of the Spectrum Analyzer code
    */
  } else {
    fft.forward(in.mix); // perform a forward FFT on the samples in input buffer
    // Frequency Band Ranges      
    bassTemp = (fft.calcAvg((float) 10, (float) 299)*10);
    midTemp = (fft.calcAvg((float) 600, (float) 1500)) * 17;
    trebleTemp = (fft.calcAvg((float) 2400, (float) 5600)) * 65;
    colorMode(RGB, 255, 255, 255);

    if (bassTemp > 200) {
      bassSend = 255;
      bass = 180;
    }
    if (midTemp > 100) {
      midSend = 255;
      mid = 90;
    }
    if (trebleTemp > 70) {
      trebleSend = 255;
      treble = 60;
    }

    bassString = String.format("%03d", Math.round(bassSend));
    midString = String.format("%03d", Math.round(midSend));
    trebleString = String.format("%03d", Math.round(trebleSend));
    sendStringTwo = bassString + midString + trebleString + "2" + "\n";


    myPort.write(sendStringTwo);

    if (bassTemp < bass && bassTemp < 190 ) { 
      bassSend = bassSend - smoothFactor;
    }
    if (midTemp < mid) {
      midSend = midSend - smoothFactor;
    }
    if (trebleTemp < treble && trebleTemp <60) { 
      trebleSend = trebleSend - smoothFactor;
    }

    bassSend = constrain(bassSend, 0, 255);
    midSend = constrain(midSend, 0, 255);
    trebleSend = constrain(trebleSend, 0, 255);
    HSBcolor = color(bassSend, midSend, trebleSend);
    background(HSBcolor);
  }
} 
/* void controlEvent(ControlEvent theEvent) {

  if (theEvent.controller().name() =="Speed") {
    cycleSpeed = (int)controlP5.getValue("Speed");
  }
  hueC = Color.HSBtoRGB((float)hueNumClean/1020, 1, 1);
  controlP5.getController("Hue").setColorActive(color(hueC));
} */
void stop()
{
  // always close Minim audio classes when you finish with them
  in.close();
  minim.stop();

  super.stop();
}

float constrainer(float input) {
  constrain(input, 0, 255);
  return input;
}

void findSerialPort() {

  serialString = Serial.list();   
  for (int i = serialString.length - 1; i > 0; i--) {  

    serialCheck = serialString[i];  
    serialIndex = serialCheck.indexOf(portName);  

    if (serialIndex > -1) portNumber = i;
  }
}