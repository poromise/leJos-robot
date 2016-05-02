package robotcontrol;
/**
 * <h1><b>Software for Lego Mindstorms EV3 robot</b></h1>
 * This is software designed to run a Lego Mindstorms EV3 brick with
 * one touch sensor (for detecting obstacles), one infra red sensor 
 * (for receiving user input from a remote controller), one medium 
 * servo motor (for turning a fist for clearing obstacles) 
 * and two large servo motors(for driving the robot around on tank
 * tracks).
 * 
 * <p>
 * 
 * <h1>Main.java class</h1>
 * Main class. Only instantiates a RobotController and calls its runProgram method.
 * 
 * @author Simo Hyttinen
 * @version 1.0
 */

public class Main {
	
	public static void main(String[] args) {
		RobotController rc = new RobotController();
		rc.runProgram();
	}

}
