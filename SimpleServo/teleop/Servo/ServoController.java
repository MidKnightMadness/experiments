package org.firstinspires.ftc.teamcode.SimpleServo.teleop.Servo;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.SimpleServo.teleop.CrossCommunicator;

/**
 * Created by gregory.ling on 7/28/17.
 */

public class ServoController {
    private Servo servo;
    private Telemetry telemetry;

    private void log(String data) {
        telemetry.addLine(data);
    }

    public void init(Telemetry telemetry, HardwareMap hardwareMap) {
        this.telemetry = telemetry;
        servo = hardwareMap.servo.get(CrossCommunicator.Servo.SERVO);
        servo.setPosition(0);
    }

    public void start() {

    }

    private double pos = 0;
    private double dir = 0.0001;
    private int loopCount = 0;
    public void loop(Gamepad gamepad1, Gamepad gamepad2) {
        loopCount++;
        if (loopCount == 1) {
            pos += dir;
            if (pos >= 1 || pos <= -1) {
                dir *= -1;
            }
            servo.setPosition(pos);
            loopCount = 0;
            log("" + pos);
        }
    }

    public void stop() {

    }
}
