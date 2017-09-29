package org.firstinspires.ftc.teamcode.OmniBot.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.OmniBot.teleop.CrossCommunicator;

@Autonomous(name = "Find Max Power", group = "OmniBot")
public class FindMaxPower extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {

        DcMotor motorUp;
        double speeds[] = new double[21];

        motorUp = hardwareMap.dcMotor.get(CrossCommunicator.drive.UP);
        motorUp.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorUp.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.addLine("Status: Initialized and ready!");
        telemetry.update();

        waitForStart();

        double startTime;
        double startPos;
        double waitUntil;

        for (int i = 1; i >= -1; i -= 0.1) {
            motorUp.setPower(i);

            waitUntil = time + 1;
            while (time < waitUntil) {
                idle();
            }

            startTime = time;
            startPos = motorUp.getCurrentPosition();

            waitUntil = time + 4;
            while (time < waitUntil) {
                telemetry.addData("Speed Test " + i + ": ", (motorUp.getCurrentPosition() - startPos) / (time - startTime));
                idle();
            }
            speeds[20 - (i+1)*10] = (motorUp.getCurrentPosition() - startPos) / (time - startTime);
        }

        for (int i = 1; i >= -1; i -= 0.1) {
            telemetry.addData("Speed Test " + i + ": ", speeds[20 - (i+1)*10]);
        }
        telemetry.update();
        while (time < 60) {
            idle();
        }
        // Do something useful
    }
}