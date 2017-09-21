package local.lahiru;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;

public class App {
    public static void main(String args[]) throws InterruptedException {
 
    	
    		//Servo pin 1
    		//Vibration sensor pin 2
    		//buzzer pin 5
    		//trigger 4
    		//trigger 3
    	
   
	
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinPwmOutput servo = gpio.provisionPwmOutputPin(RaspiPin.GPIO_01);
        final GpioPinDigitalInput vib1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);
        final GpioPinDigitalOutput sensorTriggerPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04);
    	 	final GpioPinDigitalInput sensorEchoPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03,PinPullResistance.PULL_DOWN);
        final GpioPinDigitalOutput buzzer = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05);
        
        
        
        
        
        com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS);
        com.pi4j.wiringpi.Gpio.pwmSetRange(1000);
        com.pi4j.wiringpi.Gpio.pwmSetClock(500);
        vib1.setShutdownOptions(true);
        buzzer.setShutdownOptions(true, PinState.LOW);
        
        //Open gate
        servo.setPwm(55);
        Thread.sleep(1);
        int a=0;
        final Distance d=new Distance();
        
        vib1.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {   
	            	if (event.getState()==PinState.HIGH) {
	            		try {
	            			//Close gate
	            			servo.setPwm(125);
	            			Thread.sleep(1);
	            			
	            			//buzzer.setState(PinState.HIGH);
	            			
	            			//Thread.sleep(1000);
	            			
	            		}
	            		catch(Exception e) {
	            			System.out.println("ERROR : Unable to close gate"+e.getMessage());
	            		}
	            	}
	            	else if(event.getState()==PinState.LOW) {
	            		try {
	            			//Open gate
	            			servo.setPwm(55);
	            			Thread.sleep(1);
	            			buzzer.setState(PinState.LOW);
	  
	            		}
	            		catch(Exception e) {
	            			System.out.println("ERROR : Unable to open gate"+e.getMessage());
	            		}
	            	}
            }

        });
        
        while(true) {
        	 Thread.sleep(1000);

        	 sensorTriggerPin.high(); // Make trigger pin HIGH
 			Thread.sleep((long) 0.01);// Delay for 10 microseconds
 			sensorTriggerPin.low(); //Make trigger pin LOW
 		
 			while(sensorEchoPin.isLow()){}
 			long startTime= System.nanoTime(); // Store the surrent time to calculate ECHO pin HIGH time.
 			while(sensorEchoPin.isHigh()){}
 			long endTime= System.nanoTime(); // Store the echo pin HIGH end time to calculate ECHO pin HIGH time.
 			double saved_distance=d.get_dist();
 			
 			double current_distance=((((endTime-startTime)/1e3)/2) / 29.1);
 			if(d.issaved && saved_distance>current_distance) {
 				//System.out.println("Danger");
 				buzzer.setState(PinState.HIGH);
 			}
 			else {
 				buzzer.setState(PinState.LOW);
 			}
 			
 			if (d.issaved==false){
 				d.set_dist(current_distance);
 				d.issaved=true;
 			}
 			//System.out.println(d.issaved+":"+d.get_dist()+":"+current_distance);
        }            
    }
    
    
    
    
}

class Distance{
	private double d;
	public boolean issaved;
	Distance(){
		this.d=-2;
		this.issaved=false;
		
	}
	void set_dist(double d) {
		this.d=d;
	}
	double get_dist() {
		return this.d;
	}

}