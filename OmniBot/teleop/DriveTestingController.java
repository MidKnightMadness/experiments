package org.firstinspires.ftc.teamcode.OmniBot.teleop;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.lang.Math;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.OmniBot.teleop.CrossCommunicator;

public class DriveTestingController {


    /* NOTES:
    * Set MAX_MOTOR_SPEED and RATIO_WHEEL
     */






    private static double MAX_MOTOR_SPEED = 360;
    private static double RATIO_WHEEL = 5000;
    private static double RATIO_BOT = 360;
    private static boolean MOTORS = false;
    private static double TURN_SPEED_RATIO = 1;

    private Telemetry telemetry;
    private DcMotor motorUp;
    private DcMotor motorDown;
    private DcMotor motorLeft;
    private DcMotor motorRight;
    private double timeElapsed;
    private double timeThisRun;
    private ElapsedTime runtime = new ElapsedTime(ElapsedTime.Resolution.SECONDS);
    private double tempMotors[] = new double[4];
    private int stopped = 0;
    private double theta = 0;
    private double translationDirection = 0;
    private double addedRotation = 0;


    public void init(Telemetry telemetry, HardwareMap hardwareMap) {
        this.telemetry = telemetry;

        if (MOTORS) {
            motorUp = hardwareMap.dcMotor.get(CrossCommunicator.drive.UP);
            motorUp.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorDown = hardwareMap.dcMotor.get(CrossCommunicator.drive.DOWN);
            motorDown.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorLeft = hardwareMap.dcMotor.get(CrossCommunicator.drive.LEFT);
            motorLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorRight = hardwareMap.dcMotor.get(CrossCommunicator.drive.RIGHT);
            motorRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        telemetry.addLine("Status: Initialized and Ready!");
        telemetry.update();
    }

    private double aTan(double x, double y) {
        double a = 0;
        if (y == 0) {
            if (x < 0) {
                a = -90;
            } else if (x > 0) {
                a = 90;
            } else if (x == 0) {
                a = 0;
            }
        } else if (x == 0) {
            a = 0;
        } else {
            a = (Math.atan(x/y)*(180/Math.PI));
        }

        if (y < 0) {
            if (x < 0) {
                a -= 180;
            } else {
                a += 180;
            }
        }
        return a;
    }

    public void start() {
        timeElapsed = runtime.time();
    }

    public void loop(Gamepad gamepad1, Gamepad gamepad2) {
        translationDirection = aTan(gamepad1.left_stick_x, -gamepad1.left_stick_y);

        //degrees per second to add to each motor speed
        addedRotation = gamepad1.right_stick_x * TURN_SPEED_RATIO;

        stopped = (gamepad1.left_stick_x == 0 && -gamepad1.left_stick_y == 0) ? 0 : 1;

        tempMotors[2] = -stopped*Math.cos((theta + translationDirection)*(Math.PI/180d)) + addedRotation;
        tempMotors[3] = stopped*Math.cos((theta + translationDirection)*(Math.PI/180d)) + addedRotation;
        tempMotors[0] = -stopped*Math.sin((theta + translationDirection)*(Math.PI/180d)) + addedRotation;
        tempMotors[1] = stopped*Math.sin((theta + translationDirection)*(Math.PI/180d)) + addedRotation;

        double scale = Math.max(Math.max(Math.abs(tempMotors[0]), Math.abs(tempMotors[1])), Math.max(Math.abs(tempMotors[2]), Math.abs(tempMotors[3])));
        if (scale == 0) {
            scale = 0;
        } else {
            scale = 1/scale;
        }


        tempMotors[0] *= scale;
        tempMotors[1] *= scale;
        tempMotors[2] *= scale;
        tempMotors[3] *= scale;
        /*telemetry.addData("Gamepad Left Stick X: ", gamepad1.left_stick_x);
        telemetry.addData("Gamepad Left Stick Y: ", -gamepad1.left_stick_y);
        telemetry.addData("Gamepad Right Stick X: ", gamepad1.right_stick_x);
        telemetry.addData("Theta: ", theta);
        telemetry.addData("Translation Direction: ", translationDirection);
        telemetry.addData("Added Rotation: ", addedRotation);
        telemetry.addData("Time Elapsed: ", timeElapsed);
        telemetry.addData("Scale: ", scale);
        telemetry.addData("Up Motor: ", tempMotors[3]);
        //telemetry.addData("Down Motor: ", tempMotors[1]);
        telemetry.addData("Left Motor: ", tempMotors[2]);
        //telemetry.addData("Right Motor: ", tempMotors[3]); //Too much telemetry --> gets cut off

        telemetry.update();*/

        if (MOTORS) {
            motorUp.setPower(tempMotors[0]);
            motorDown.setPower(tempMotors[1]);
            motorLeft.setPower(tempMotors[2]);
            motorRight.setPower(tempMotors[3]);
        }

        // dw/s * s = dw -> dw * db/dw = db
        timeThisRun = runtime.time() - timeElapsed;
        timeElapsed = runtime.time();
        // (Always 1 if no Left Joystick) * (Actual Motor Speed?)
        theta += (scale * addedRotation * MAX_MOTOR_SPEED) * timeThisRun * (RATIO_BOT/RATIO_WHEEL);
        telemetry.addData("Gamepad Right Stick X: ", gamepad1.right_stick_x);
        telemetry.addData("Time Elapsed: ", timeThisRun);
        telemetry.addData("Rot * Scale * Time Elapsed: ", addedRotation * scale * timeThisRun);
        telemetry.addData("Ratio: ", RATIO_BOT/RATIO_WHEEL);
        telemetry.addData("All Together: ", addedRotation * scale * timeThisRun * (RATIO_BOT/RATIO_WHEEL));
        telemetry.addData("Theta: ", theta);
        telemetry.update();

    }

    public void stop() {

    }
}
