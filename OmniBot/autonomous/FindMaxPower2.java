package org.firstinspires.ftc.teamcode.OmniBot.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.OmniBot.teleop.CrossCommunicator;

@Autonomous(name = "Find Max Power2", group = "OmniBot")
public class FindMaxPower2 extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {

        DcMotor motorUp;
        double speeds[] = new double[22];

        motorUp = hardwareMap.dcMotor.get(CrossCommunicator.drive.UP);
        motorUp.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorUp.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        telemetry.addLine("Status: Initialized and ready!");
        telemetry.update();

        waitForStart();

        for (int i = 0; i < 3600; i++) {
            double waitUntil = time + 0.05;
            while (time < waitUntil) {
                idle();
            }
            motorUp.setTargetPosition(motorUp.getCurrentPosition() + 100);
        }



    }
}