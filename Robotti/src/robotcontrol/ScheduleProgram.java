package robotcontrol;

import java.util.ArrayList;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

/**
 * <h1>Program class for schedulable robot movement</h1>
 * A program with a name and a list of commands to be executed.
 * <p>
 * List of numerical commands: <p>
 * 1: left motor forward <p>
 * 2: left motor backward <p>
 * 3: right motor forward <p>
 * 4: right motor backward <p>
 * 5: forward <p>
 * 6: turn right <p>
 * 7: turn left <p>
 * 8: backward <p>
 * 10: turn fist left <p>
 * 11: turn fist right
 * 
 * @author Simo Hyttinen
 * @version 0.3.1
 */

public class ScheduleProgram {
	
	// Everything is public for easy access. Not necessarily the best practice.
	// Might make get/set methods or something for these, if enough time.
	public String programName;
	public boolean abort = false;
	public ArrayList<Integer> actionList = new ArrayList<Integer>();
	/**
	 * The constructor gets the name of the program as a parameter and stores it
	 * in String programName.
	 * @param name String The name of the program
	 */
	public ScheduleProgram(String name) {
		this.programName = name;
	}
	/**
	 * Does the waiting before the program is executed.
	 * @param waitMS The time in milliseconds that is waited before executing the program.
	 */
	public void waitForExecute(int waitS) {
		LCD.clear();
		LCD.drawString("DOWN to abort", 1, 1);
		LCD.drawString("Wait for start", 1, 2);
		LCD.drawString("Time left: ", 1, 3);
		while (waitS >= 0) {
			if (Button.DOWN.isDown()) {
				abort = true;
				break;
			}
			if (waitS >= 60) {
				LCD.clear(4);
				// Amount of seconds calculated from remainder
				int seconds = waitS % 60;
				// Amount of minutes calculated from subtracting seconds from total and dividing by 60
				// since if everything worked, (waitS - seconds) should be divisible by 60.
				int minutes = (waitS - seconds) / 60;
				LCD.drawString(minutes + " min", 1, 4);
				LCD.drawString(seconds + " sec", 7, 4);
			} else if (waitS < 60) {
				LCD.clear(4);
				LCD.drawString(waitS + " sec", 7, 4);
			}
			Delay.msDelay(1000);
			waitS--;
		}
		LCD.clear();
	}
}
