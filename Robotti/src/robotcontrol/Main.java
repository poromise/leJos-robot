package robotcontrol;
/**
 * <h1>Main class</h1>
 * Main class. Only instantiates a RobotController and calls its runProgram method.
 * Unlikely to change, so version number will likely be 1.0 ad infinitum.
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
