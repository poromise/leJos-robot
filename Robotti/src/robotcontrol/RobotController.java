package robotcontrol;

/**
 * <h1>RobotController.java class</h1>
 * RobotController is the basic functional class of this program. All the controlling
 * is done from here, threaded tasks are delegated to other specialized thread classes.
 * 
 * @author Simo Hyttinen
 * @version 0.2
 */

import robotcontrol.IRChecker;

import java.util.ArrayList;
import java.util.Arrays;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class RobotController {
	
	private EV3IRSensor irSensor = new EV3IRSensor(SensorPort.S1);
	private IRChecker irc = new IRChecker(irSensor);
	private RegulatedMotor lMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	private RegulatedMotor rMotor = new EV3LargeRegulatedMotor(MotorPort.B);
	
	private ArrayList<Integer> waitTimeList = new ArrayList<Integer>();
	private ArrayList<ScheduleProgram> progList = new ArrayList<ScheduleProgram>();
	private ScheduleProgram program1 = new ScheduleProgram("Scout");
	private ScheduleProgram program2 = new ScheduleProgram("WTF??");
	private ScheduleProgram program3 = new ScheduleProgram("Forward!");
	private ScheduleProgram program4 = new ScheduleProgram("Backward!");
	
	/**
	 * Constructor takes no parameters. Starts the infra red sensor checker.
	 */
	public RobotController() {
		// Adds actions to the action lists of the programs
		program1.actionList.addAll(Arrays.asList(5, 5, 5, 5, 8, 8, 8, 8));
		program2.actionList.addAll(Arrays.asList(6, 6, 5, 5, 7, 5, 5));
		program3.actionList.addAll(Arrays.asList(5, 5, 5, 5));
		program4.actionList.addAll(Arrays.asList(8, 8, 8, 8));
		waitTimeList.addAll(Arrays.asList(5, 10, 30, 45, 60, 120, 180, 300, 600, 1800, 3600));
		progList.add(program1);
		progList.add(program2);
		progList.add(program3);
		progList.add(program4);
		// Starts infra red sensor checker thread
		irc.start();
	}
	/**
	 * The basic functionality of the program: Asks if the user wants to drive
	 * or schedule a program. Those functions are performed in separate functions.
	 */
	public void runProgram() {
		LCD.clear();
		LCD.drawString("S.P.H.I.N.C.T.E.R.", 1, 1);
		LCD.drawString("Copyright Merimakkara 2016", 1, 2);
		Delay.msDelay(3000);
		
		while(!Button.ESCAPE.isDown()) {
			LCD.clear();
			LCD.drawString("Press LEFT to drive", 1, 1);
			LCD.drawString("Press RIGHT to schedule a program", 1, 2);
			// Wait for any key press
			int pressedButton = Button.waitForAnyPress();
			if (pressedButton == Button.ID_LEFT) {
				// Robot goes into drive mode
				listenToInput();
			} else if (pressedButton == Button.ID_RIGHT) {
				// Program scheduling menu is opened
				Delay.msDelay(1000);
				ScheduleProgram executable = scheduledProgram();
				if (executable == null);
				else {
					int waitTime = scheduledProgramWaitTime();
					if (waitTime != 0) {
						executable.waitForExecute(waitTime);
						executeProgram(executable);
						LCD.clear();
						LCD.drawString("Program " + executable.programName + " executed.", 1, 1);
						Delay.msDelay(3000);
						
					}
				}
			}
		}
		lMotor.close();
		rMotor.close();
		irc.terminateSensor();
	}
	
	/**
	 * Contains the while loop (terminatable by pressing ESCAPE on the EV3) 
	 * that checks for user input from the IR sensor and delegates
	 * the received input accordingly to the appropriate methods.
	 * <p>
	 * Also checks if touch sensor is activated. If it is, starts the obstacleHit()
	 * method which stops the robot and reverses a bit.
	 */
	private void listenToInput() {
		LCD.clear();
		LCD.drawString("DRIVE MODE", 1, 1);
		LCD.drawString("Press UP for menu", 1, 2);
		while (!Button.UP.isDown()) {
			if (irc.getTouchState()) {
				obstacleHit();
			} else {
				int userInput = irc.getUserInput();
				if (0 <= userInput && 9 > userInput) {
					moveTracks(userInput);
				}
			}
		}
	}
	/**
	 * Contains the menu in which the schedulable program and the time waited
	 * before execution are determined and executed.
	 */
	private ScheduleProgram scheduledProgram() {
		int pressedButton; // Numeric ID of the button last pressed
		int currentListPosition = 0; // Integer to act as index of currently selected program
		ScheduleProgram selectedProgram = progList.get(currentListPosition);
		int lastProgramPosition = progList.size() - 1; // Index number of last program on list
		
		// Draw instructions and name of 1st program on screen
		LCD.clear();
		LCD.drawString("SCHEDULING", 1, 1);
		LCD.drawString("Select action to schedule.", 1, 2);
		LCD.drawString("Scroll with LEFT and RIGHT", 1, 3);
		LCD.drawString("Select with ENTER.", 1, 4);
		// Loops until enter or escape is pressed.
		System.out.println("ennen while-looppia");
		Delay.msDelay(2000);
		while ( true ) {
			// for explanation see comment on similar while loop in scheduledProgramWaitTime()
			pressedButton = Button.ID_DOWN;
			LCD.clear(5);
			LCD.drawString("<- Program: " + selectedProgram.programName + " ->", 1, 5);
			pressedButton = Button.waitForAnyPress();
			System.out.println("while-loopissa");
			if (pressedButton == Button.ID_ESCAPE) {
				// Escape stops execution of rest of the method, returns null
				return null;
			} else if (pressedButton == Button.ID_ENTER) {
				// Enter breaks the while loop and moves on to the next menu
				break;
			} else if (pressedButton == Button.ID_LEFT) {
				// Moves selection 1 backward or to the end of the list if selection is 0
				if (currentListPosition == 0) {
					currentListPosition = lastProgramPosition;
				} else {
					currentListPosition--;
				}
			} else if (pressedButton == Button.ID_RIGHT) {
				// Moves selection 1 forward or to 0 if selection is at the end of the list
				if (currentListPosition == lastProgramPosition) {
					currentListPosition = 0;
				} else {
					currentListPosition++;
				}
			}
		}
		selectedProgram = progList.get(currentListPosition);
		return selectedProgram;
	}
	
	/**
	 * Menu for determining the length of the waiting period before executing
	 * selected program.
	 * 
	 * @return int The selected waiting time as an integer
	 */
	private int scheduledProgramWaitTime() {
		int pressedButton;
		int currentListPosition = 0;
		int selectedWaitTime;
		int lastItemInList = waitTimeList.size() - 1;
		
		// Draws instructions on the screen.
		LCD.clear();
		LCD.drawString("SCHEDULING", 1, 1);
		LCD.drawString("Set time to execution.", 1, 2);
		LCD.drawString("Scroll with LEFT and RIGHT", 1, 3);
		LCD.drawString("Select with ENTER.", 1, 4);
		while ( true ) {
			Delay.msDelay(500);
			// Changes the input to DOWN so as to not loop the same key press action over
			// and over again. Could have been done more efficiently, but this was the first
			// thing that popped to my mind.
			pressedButton = Button.ID_DOWN;
			selectedWaitTime = waitTimeList.get(currentListPosition);
			LCD.clear(5);
			
			// Draws the currently selected time on the 5th row
			if (selectedWaitTime < 60) {
				LCD.drawString("<- " + selectedWaitTime + " sec ->", 1, 5);
			} else {
				int seconds = selectedWaitTime % 60;
				int minutes = (selectedWaitTime - seconds) / 60;
				LCD.drawString("<- " + minutes +" min" + seconds + " sec ->", 1, 5);
			}
			
			// Button shenanigans, pretty much the same as in scheduledProgram()
			pressedButton = Button.waitForAnyPress();
			if (pressedButton == Button.ID_ESCAPE) {
				return 0;
			} else if (pressedButton == Button.ID_ENTER) {
				break;
			} else if (pressedButton == Button.ID_LEFT) {
				if (currentListPosition == 0) {
					currentListPosition = lastItemInList;
				} else {
					currentListPosition--;
				}
			} else if (pressedButton == Button.ID_RIGHT) {
				if (currentListPosition == lastItemInList) {
					currentListPosition = 0;
				} else {
					currentListPosition++;
				}
			}
		}
		return selectedWaitTime;
	}
	/**
	 * Moves the tracks according to given parameters.
	 * 
	 * @param direction int Integer that determines the direction of movement.
	 */
	private void moveTracks(int direction) {
		switch (direction) {
		case 1:
			// left motor bwd
			lMotor.backward();
			break;
		case 2:
			// left motor fwd
			lMotor.forward();
			break;
		case 3:
			// right motor bwd
			rMotor.backward();
			break;
		case 4:
			// right motor fwd
			rMotor.forward();
			break;
		case 5:
			// backward
			rMotor.backward();
			lMotor.backward();
			break;
		case 6:
			// turn left
			lMotor.backward();
			rMotor.forward();
			break;
		case 7:
			// turn right
			lMotor.forward();
			rMotor.backward();
			break;
		case 8:
			// forward
			lMotor.forward();
			rMotor.forward();
			break;
		default:
			lMotor.stop(true);
			rMotor.stop(true);
			break;
		}
	}
	/**
	 * Swings the fist around according to given parameters.
	 * 
	 * @param direction int Integer that defines the direction in which to move the fist.
	 */
	private void moveFist(int direction) {
		
	}
	/**
	 * Stops the motors and reverses them half a turn before letting the user
	 * continue driving.
	 */
	private void obstacleHit() {
		lMotor.stop(true);
		rMotor.stop(true);
		Delay.msDelay(500);
		lMotor.rotate(180, true);
		rMotor.rotate(180, true);
		Delay.msDelay(200);
	}
	
	/**
	 * Reads the list of maneuvers in given program and iterates through
	 * them sending then to programedManeuver() to be executed while doing so.
	 * 
	 * @param program ScheduleProgram The program which is to be executed.
	 */
	private void executeProgram(ScheduleProgram program) {
		LCD.drawString("Executing program:", 1, 1);
		LCD.drawString(program.programName, 1, 2);
		for (int i : program.actionList) {
			programmedManeuver(i);
			Delay.msDelay(300);
		}
		rMotor.stop(true);
		lMotor.stop(true);
		LCD.clear();
	}
	
	/**
	 * Executes maneuvers requested by executeProgram().
	 * 
	 * @param maneuver int The numeric case number for the next executable maneuver.
	 */
	private void programmedManeuver(int maneuver) {
		switch (maneuver) {
		case 1:
			// left motor fwd
			lMotor.rotate(5 * -360);
			break;
		case 2:
			// left motor bwd
			lMotor.rotate(5 * 360);
			break;
		case 3:
			// right motor fwd
			rMotor.rotate(5 * -360);
			break;
		case 4:
			// right motor bwd
			rMotor.rotate(5 * 360);
			break;
		case 5:
			// forward
			rMotor.rotate(5 * -360, true);
			lMotor.rotate(5 * -360);
			break;
		case 6:
			// turn right
			lMotor.rotate(5 * -360,true);
			rMotor.rotate(5 * 360);
			break;
		case 7:
			// turn left
			lMotor.rotate(5 * 360, true);
			rMotor.rotate(5 * -360);
			break;
		case 8:
			// backward
			lMotor.rotate(5 * 360, true);
			rMotor.rotate(5 * 360);
			break;
		default:
			break;
		}
	}
}
