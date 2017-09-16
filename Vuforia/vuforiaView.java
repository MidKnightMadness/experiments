package org.firstinspires.ftc.teamcode.Vuforia;

import android.graphics.Bitmap;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.ftcrobotcontroller.R;
import com.vuforia.HINT;
import com.vuforia.Image;
import com.vuforia.Matrix34F;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Tool;
import com.vuforia.Vec2F;
import com.vuforia.Vec3F;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CloseableFrame;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

import static com.vuforia.PIXEL_FORMAT.RGB565;

@Autonomous(name = "Vuforia View", group = "Vuforia")
public class vuforiaView extends LinearOpMode {

    private double[][] floatArToDbl(float[][] arr) {
        double[][] ret = new double[arr.length][arr[0].length];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                ret[i][j] = (double) arr[i][j];
            }
        }
        return ret;
    }

    @Override
    public void runOpMode() throws InterruptedException {

        VuforiaLocalizer.Parameters params = new VuforiaLocalizer.Parameters(R.id.cameraMonitorViewId);

        //Which side of the phone?
        params.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        //License Key
        params.vuforiaLicenseKey = "ATqSvW7/////AAAAGSbUe3emc0NmiwFnhuicf+c4388daBpHukK2NzjANrVvP6h1rJRTExnNRD8RBZJqsu5tuVVn+AuayqO2UEZbxp0+ZUbFnXPssyKkV4q8YmYB4AkxHwaJCIxCdd1cCWR9F0xuvve5OOzddkh13v/3T1DSh7vrBuFHurMZF8SLQrwQqMf5ubyit0BRHbtX5GLWwm6hCEOX8ZykrK0UbA8+kyGwSqkWbb5IjUMQrlQpItk1emrxo0S2Mj7z+LCNXBNw9wPvTs4TXnpHvcA/7W0vGFxnbXUcUArUBztNHywpD+rVHjFZYuGJwMsWfHAFKH/OfePAstqGnl3GSJjCrEJqVujQo1cqmC7NmyWd2gxPnqHK";

        telemetry.addLine("Got Here!");
        telemetry.update();

        //What to display on top of visual matches?
        params.cameraMonitorFeedback = VuforiaLocalizer.Parameters.CameraMonitorFeedback.AXES;

        telemetry.addLine("Got Here!2");
        telemetry.update();

        //Create Vuforia instance:
        VuforiaLocalizer vuforia = ClassFactory.createVuforiaLocalizer(params);

        telemetry.addLine("Got Here!3");
        telemetry.update();

        //save an rgb565 image for further processing each frame
        Vuforia.setFrameFormat(RGB565, true);
        vuforia.setFrameQueueCapacity(1);


        //Hints are suggestions to do if the phone can, but don't give error if it can't
        //change to object if necessary
        //]Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 3);

        telemetry.addLine("Got Here!4");
        telemetry.update();

        //Prepares tracking asset group
        VuforiaTrackables trackables = vuforia.loadTrackablesFromAsset("Pictographs");

        telemetry.addLine("Got Here!5");
        telemetry.update();

        //Necessary? Sets names for future reference -- can these be specified in the asset?
        trackables.get(0).setName("Right");
        trackables.get(1).setName("Center");
        trackables.get(2).setName("Left");

        telemetry.addLine("Got Here!6");
        telemetry.update();

        telemetry.addLine("Status: Initialized and ready!");
        telemetry.update();

        waitForStart();

        //Begin tracking
        trackables.activate();

        //While opMode is active, iterate through all trackables
        while (opModeIsActive()) {
            for (VuforiaTrackable item : trackables) {

                //create OpenGLMatrix containing the translation and rotation of the item relative to the robot
                OpenGLMatrix pose = ((VuforiaTrackableDefaultListener) item.getListener()).getPose();

                //if Vuforia can see this item...
                if (pose != null) {

                    //make a VectorF to hold translation values
                    VectorF translation = pose.getTranslation();

                    //Do something with this data, or just send to telemetry
                    telemetry.addData(item.getName() + "-Translation", translation);

                    //Determine the degrees the phone needs to turn to be directly facing the item
                        // arctan of the y (1) and z (2) distances
                    double degreesToTurn = Math.toDegrees(Math.atan2(translation.get(1), translation.get(2)));

                    telemetry.addData(item.getName() + "-Degrees", degreesToTurn);

                    //If button pressed... for now at every frame
                    if (true) {

                        OpenGLMatrix rawPoseV = ((VuforiaTrackableDefaultListener) item.getListener()).getRawPose();

                        //Get position data in matrix form for openCV
                        Matrix34F rawPose = new Matrix34F();
                        float[] poseData = Arrays.copyOfRange(rawPoseV.transposed().getData(), 0, 12);
                        rawPose.setData(poseData);

                        //Get points of corners of item on 2d image (UR, UL, BL, BR) Units: Inch
                        float[][] srcPoints = new float[4][2];
                        srcPoints[0] = Tool.projectPoint(vuforia.getCameraCalibration(), rawPose, new Vec3F(5.5f, 4.25f, 0f)).getData();
                        srcPoints[1] = Tool.projectPoint(vuforia.getCameraCalibration(), rawPose, new Vec3F(-5.5f, 4.25f, 0f)).getData();
                        srcPoints[2] = Tool.projectPoint(vuforia.getCameraCalibration(), rawPose, new Vec3F(-5.25f, -4.25f, 0f)).getData();
                        srcPoints[3] = Tool.projectPoint(vuforia.getCameraCalibration(), rawPose, new Vec3F(5.25f, -4.25f, 0f)).getData();

                        float[][] destPoints = new float[4][2];
                        destPoints[0][0] = 5.5f; destPoints[0][1] = 4.25f;
                        destPoints[1][0] = -5.5f; destPoints[1][1] = 4.25f;
                        destPoints[2][0] = -5.5f; destPoints[2][1] = -4.25f;
                        destPoints[3][0] = 5.5f; destPoints[3][1] = -4.25f;


                        //Access rgb565 image
                        //get frame: 5 types: 1 rgb and some grayscale
                        CloseableFrame frame = vuforia.getFrameQueue().take();
                        for (int i = 0; i < frame.getNumImages(); i++) {
                            if (frame.getImage(i).getFormat() == PIXEL_FORMAT.RGB565) {
                                Image rgb = frame.getImage(i);

                                Bitmap bmp = Bitmap.createBitmap(rgb.getWidth(), rgb.getHeight(), Bitmap.Config.RGB_565);
                                bmp.copyPixelsFromBuffer(rgb.getPixels());

                                Mat mat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC3);
                                Utils.bitmapToMat(bmp, mat);

                                double[][] srcPointsDbl = floatArToDbl(srcPoints);
                                double[][] destPointsDbl = floatArToDbl(srcPoints);

                                Point[] srcPointAr = new Point[4];
                                srcPointAr[0] = new Point(srcPointsDbl[0]);
                                srcPointAr[1] = new Point(srcPointsDbl[1]);
                                srcPointAr[2] = new Point(srcPointsDbl[2]);
                                srcPointAr[3] = new Point(srcPointsDbl[3]);

                                Point[] destPointAr = new Point[4];
                                destPointAr[0] = new Point(destPointsDbl[0]);
                                destPointAr[1] = new Point(destPointsDbl[1]);
                                destPointAr[2] = new Point(destPointsDbl[2]);
                                destPointAr[3] = new Point(destPointsDbl[3]);

                                MatOfPoint2f srcMat = new MatOfPoint2f(srcPointAr);
                                MatOfPoint2f destMat = new MatOfPoint2f(destPointAr);

                                Mat homo = Calib3d.findHomography(srcMat, destMat);
                                Mat output = new Mat();

                                Imgproc.warpPerspective(mat, output, homo, new Size(11, 8.5));
                                
                            }
                        }
                    }
                }
            }
            telemetry.update();
            idle();
        }
    }
}