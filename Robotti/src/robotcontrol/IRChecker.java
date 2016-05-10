package robotcontrol;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.utility.Delay;

/**
 * <h1>IR sensor and touch sensor checker</h1>
 * The IRChecker is a thread subclass that is started by the RobotController. It runs
 * on it's own separate thread alongside the RobotController.
 * <p>
 * Touch sensor added to the same class instead of making a separate one for it, 
 * for simplicity. Should have named
 * the class differently. Live and learn.
 * 
 * @author Simo Hyttinen
 * @version 0.3.1
 */

public class IRChecker extends Thread {
	private EV3IRSensor infraredSensor;
	private int userInput;
	private EV3TouchSensor tSensor = new EV3TouchSensor(SensorPort.S2);
	private float[] sample = new float[(tSensor.sampleSize())];
	private boolean touchState = false;
	public boolean terminate = false;
	
	/**
	 * Constructor receives the IR sensor and points the infraredSensor instance 
	 * variable to it.
	 * 
	 * @param sensor EV3IRSensor Places the received IR sensor in the infraredSensor instance variable.
	 */
	public IRChecker(EV3IRSensor sensor) {
		this.infraredSensor = sensor;
	}
	
	/**
	 * The method that is run automatically when this class is instantiated. 
	 * Contains a while loop that keeps checking the IR sensor each 50 milliseconds
	 * and changing the userInput integer accordingly.
	 * 
	 * Added: also monitors the touch sensor.
	 */	
	public void run() {
		while (!terminate) {
			int remoteCommand = infraredSensor.getRemoteCommand(0);
			this.userInput = remoteCommand;
			tSensor.fetchSample(sample, 0);
			if (sample[0] == 1) {
				touchState = true;
			} else if (sample[0] == 0) {
				touchState = false;
			}
			Delay.msDelay(50);
		}		
	}
	/**
	 * Void method for closing the IR sensor at the end of the program.
	 */
	public void terminateSensor() {
		infraredSensor.close();
		tSensor.close();
	}
	/**
	 * Getter method for the user input.
	 * 
	 * @return int Numeric code for the user input received from the IR sensor.
	 */
	public int getUserInput() {
		return userInput;
	}
	/**
	 * Getter method for touch sensor state.
	 * 
	 * @return boolean State of the touch sensor. True means it is pressed.
	 */
	public boolean getTouchState() {
		return touchState;
	}
}
