import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import org.jnativehook.keyboard.NativeKeyEvent; 
import org.jnativehook.keyboard.NativeKeyListener; 
import org.jnativehook.mouse.NativeMouseEvent; 
import org.jnativehook.mouse.NativeMouseInputListener; 
import org.jnativehook.NativeHookException; 
import org.jnativehook.GlobalScreen; 
import org.jnativehook.*; 
import java.util.EventListener; 
import controlP5.*; 
import processing.serial.*; 
import java.awt.Color; 
import ddf.minim.analysis.*; 
import ddf.minim.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ColorController_RGB_Only extends PApplet {









// in order to get jnativehook working, you must import through add file... add the jnativehook jar to Processing








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
float sliderStartVal = 127.5f;
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
int smallOffset = 30;

int hueNum = 0;
int saturationNum = 0;
int brightnessNum = 0;


int HSBcolor;
String hueString = "000";
String saturationString = "000";
String brightnessString = "255";
String sendStringOne, sendStringTwo;

int fadeSpeed = 1;
int fadeVal = 1;
int fading = 0;

int cycleSpeed = 1;
float cycleVal = 1;
int hueing = 0;

int bright = 255;
Textlabel textLabel;
boolean keyPress = false;
boolean mousePress = false;

int Hue;
int Saturation;
int Brightness;
int r;
int g;
int b;
String rString = "000";
String gString = "000";
String bString = "000";
float hueNormalized;
float saturationNormalized;
float brightnessNormalized;
public void setup() {

  
  


  //Minim Setup Start #########
  minim = new Minim(this);
  in = minim.getLineIn(Minim.MONO, buffer_size, sample_rate);
  fft = new FFT(in.bufferSize(), in.sampleRate());
  //End Minim Setup #########

  myPort = new Serial(this, "COM3", 57600);
  controlP5 = new ControlP5(this);


  //add Hue Slider 
  controlP5.addSlider("Hue", sliderStart, 360, sliderStartVal, sliderX, sliderY, sliderWidth, sliderHeight)
    .setSliderMode(Slider.FLEXIBLE)
    .setHandleSize(sliderHeight + 5);

  //add Saturation Slider
  controlP5.addSlider("Saturation", sliderStart, 100, sliderStartVal, sliderX, sliderY+offset, sliderWidth, sliderHeight)
    .setSliderMode(Slider.FLEXIBLE)
    .setHandleSize(sliderHeight + 5);

  //add Brightness Slider
  controlP5.addSlider("Brightness", sliderStart, 100, sliderStartVal, sliderX, sliderY+doubleOffset, sliderWidth, sliderHeight)
    .setSliderMode(Slider.FLEXIBLE)
    .setHandleSize(sliderHeight + 5);

  //add Cycle Speed Slider
  controlP5.addSlider("Cycle Speed", 0, 9, 1, toggleX - 25, toggleY - (toggleX/3), 20, 100).showTickMarks(true).snapToTickMarks(true).setNumberOfTickMarks(10).setCaptionLabel("").setLabel("Speed")
    .hide();

  //add Fade Speed Slider
  controlP5.addSlider("Fade Speed", 0, 9, 1, toggleX + toggleXoffset - 25, toggleY - (toggleX/3), 100, 20).showTickMarks(true).snapToTickMarks(true).setNumberOfTickMarks(10).setCaptionLabel("")
    .hide();

  //add Cycle Color Toggle
  controlP5.addToggle("Cycle Color", false, toggleX, toggleY, toggleW, toggleH);

  //add Fading Toggle
  controlP5.addToggle("Fading", false, toggleX + toggleXoffset, toggleY, toggleW, toggleH);

  //add Spectrum Toggle
  controlP5.addToggle("Spectrum", false, toggleX*2 + toggleXoffset, toggleY, toggleW, toggleH);

  //add Mouse / KeyBoard Response Half Toggles
  controlP5.addToggle("Mouse", false, toggleX * 3 + toggleXoffset, toggleY, toggleW, toggleH/2).setCaptionLabel("");
  controlP5.addToggle("Keyboard", false, toggleX * 3 + toggleXoffset, toggleY + toggleH/2, toggleW, toggleH/2).setCaptionLabel("Top: M, Bottom: KB");
}


public void draw() {

  HSBcolor = color(hueNum, saturationNum, brightnessNum);
  hueNormalized = (float)hueNum / 360;
  saturationNormalized = (float)saturationNum / 100;
  brightnessNormalized = (float)brightnessNum / 100;
  int rgb = Color.HSBtoRGB(hueNormalized, saturationNormalized, brightnessNormalized);
  r = (rgb>>16) &0x0ff;
  g = (rgb>>8) &0x0ff;
  b = (rgb) &0x0ff;

  rString = String.format("%03d", r);
  gString = String.format("%03d", g);
  bString = String.format("%03d", b);
  background(rgb);
  stroke(0);
  fill(100);

  rect(sliderX+50, sliderY, sliderWidth, (sliderHeight * 3), 2);
  rect(90, 100, 70, 170, 2);
  rect(190, 100, 70, 170, 2);
  rect(290, 100, 70, 170, 2);
  rect(390, 100, 70, 170, 2);
  fill(150);
  rect(sliderX - 10, sliderY - 10, sliderWidth + 20, 20 + (sliderHeight * 3), 4);


  hueNum = (int)controlP5.getValue("Hue");
  saturationNum = (int)controlP5.getValue("Saturation");
  brightnessNum = (int)controlP5.getValue("Brightness");

  

  fft.forward(in.mix); // perform a forward FFT on the samples in input buffer
  // Frequency Band Ranges      
  bassTemp = (fft.calcAvg((float) 10, (float) 299)*10);
  midTemp = (fft.calcAvg((float) 600, (float) 1500)) * 17;
  trebleTemp = (fft.calcAvg((float) 2400, (float) 5600)) * 65;



  if (controlP5.getController("Spectrum").getValue()  == 0) {

    if (controlP5.getController("Mouse").getValue() == 0 && controlP5.getController("Keyboard").getValue() == 0) {
      try {
        GlobalScreen.unregisterNativeHook();
      }
      catch (NativeHookException ex) {
        System.err.println("There was a problem unregistering the native hook.");
        System.err.println(ex.getMessage());
      }
    }

    if (controlP5.getController("Mouse").getValue() == 1) {
      try {
        GlobalScreen.registerNativeHook();
      }
      catch (NativeHookException ex) {
        System.err.println("There was a problem registering the native hook.");
        System.err.println(ex.getMessage());
      }

      GlobalMouseListenerExample example = new GlobalMouseListenerExample();
      GlobalScreen.addNativeMouseListener(example);
      if (mousePress == true) {
        bright = 120;
        controlP5.getController("Brightness").setValue(bright);
        bright -= 5;
        mousePress = false;
      }
      bright -=10;
      controlP5.getController("Brightness").setValue(bright);
    }


    if (controlP5.getController("Keyboard").getValue() == 1) {
      try {
        GlobalScreen.registerNativeHook();
      }
      catch (NativeHookException ex) {
        System.err.println("There was a problem registering the native hook.");
        System.err.println(ex.getMessage());
      }
      GlobalScreen.addNativeKeyListener(new GlobalKeyListenerExample());
      if (keyPress == true) {
        bright = 120;
        controlP5.getController("Brightness").setValue(bright);
        bright -= 5;
        keyPress = false;
      }
      bright -= 10;
      controlP5.getController("Brightness").setValue(bright);
    }



    if (controlP5.getController("Cycle Color").getValue()  == 1) {
      cycleVal = Math.round(controlP5.getController("Cycle Speed").getValue());
      controlP5.getController("Cycle Speed").show();
      if (hueNum < 1 || hueNum > 359) {
        cycleSpeed = -cycleSpeed;
      }
      cycleVal *= cycleSpeed;
      hueing = round(controlP5.getController("Hue").getValue());
      hueing += cycleVal;
      controlP5.getController("Hue").setValue(hueing);
      
    } else if (controlP5.getController("Cycle Color").getValue()  == 0) {
      controlP5.getController("Cycle Speed").hide();
    }
    
    
    if (controlP5.getController("Fading").getValue()  == 1) {
      fadeVal = Math.round(controlP5.getController("Fade Speed").getValue());
      controlP5.getController("Fade Speed").show();
      if (brightnessNum < 1 || brightnessNum > 99) {
        fadeSpeed = -fadeSpeed;
      }
      fadeVal *= fadeSpeed;
      fading = round(controlP5.getController("Brightness").getValue());
      fading += fadeVal;
      controlP5.getController("Brightness").setValue(fading);
      
    } else if (controlP5.getController("Fading").getValue()  == 0) {
      controlP5.getController("Fade Speed").hide();
    }


    sendStringOne = rString + gString + bString + "\n";
    myPort.write(sendStringOne);
  } else {


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
    sendStringTwo = bassString + midString + trebleString + "\n";

    print(sendStringTwo);
    myPort.write(sendStringTwo);


    if (bassTemp < bass && bassTemp < 190 ) { 
      bassSend = bassSend - 30;
    }
    if (midTemp < mid) {
      midSend = midSend - 30;
    }
    if (trebleTemp < treble && trebleTemp <60) { 
      trebleSend = trebleSend - 30;
    }

    bassSend = constrain(bassSend, 0, 255);
    midSend = constrain(midSend, 0, 255);
    trebleSend = constrain(trebleSend, 0, 255);
  }
}


public void stop()
{
  // always close Minim audio classes when you finish with them
  in.close();
  minim.stop();
  try {
    GlobalScreen.unregisterNativeHook();
  }
  catch (NativeHookException ex) {
    System.err.println("There was a problem unregistering the native hook.");
    System.err.println(ex.getMessage());
  }
  super.stop();
}

public float constrainer(float input) {
  constrain(input, 0, 255);
  return input;
}

public class GlobalKeyListenerExample implements NativeKeyListener {
  public void nativeKeyPressed(NativeKeyEvent e) {
    //System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    keyPress = true;
  }

  public void nativeKeyReleased(NativeKeyEvent e) {
    //System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
  }

  public void nativeKeyTyped(NativeKeyEvent e) {
    //System.out.println("Key Typed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
  }
}

public class GlobalMouseListenerExample implements NativeMouseInputListener {
  public void nativeMouseClicked(NativeMouseEvent e) {
    //System.out.println("Mosue Clicked: " + e.getClickCount());
    mousePress = true;
  }

  public void nativeMousePressed(NativeMouseEvent e) {
    //System.out.println("Mosue Pressed: " + e.getButton());
    mousePress = true;
  }

  public void nativeMouseReleased(NativeMouseEvent e) {
    //System.out.println("Mosue Released: " + e.getButton());
  }

  public void nativeMouseMoved(NativeMouseEvent e) {
    //System.out.println("Mosue Moved: " + e.getX() + ", " + e.getY());
  }

  public void nativeMouseDragged(NativeMouseEvent e) {
    //System.out.println("Mosue Dragged: " + e.getX() + ", " + e.getY());
  }
}
  public void settings() {  size(600, 300);  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ColorController_RGB_Only" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
