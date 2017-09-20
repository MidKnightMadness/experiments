package org.firstinspires.ftc.teamcode.Vuforia;

import android.graphics.Bitmap;
import android.os.Environment;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.vuforia.Image;
import com.vuforia.Matrix34F;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Tool;
import com.vuforia.Vec3F;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import static com.vuforia.PIXEL_FORMAT.RGB565;

/**
 * Created by gregory.ling on 9/20/17.
 */



    public class PictographViewer {
        // whether or not to save the cropped image
        public boolean SAVE_CROPPED = false;

        // display textual representation of image and wait one second or not
        public boolean SHOW_TEXT_IMAGE = false;

        //How many times to run through? Suggested: 7
        public int NUM_OF_TAKES = 7;

        //store PictographTrackables and vuforia
        public VuforiaLocalizer vuforia;
        private VuforiaTrackables pictographTrackables;

        //License Key
        private final String LICENSE_KEY = "ATqSvW7/////AAAAGSbUe3emc0NmiwFnhuicf+c4388daBpHukK2NzjANrVvP6h1rJRTExnNRD8RBZJqsu5tuVVn+AuayqO2UEZbxp0+ZUbFnXPssyKkV4q8YmYB4AkxHwaJCIxCdd1cCWR9F0xuvve5OOzddkh13v/3T1DSh7vrBuFHurMZF8SLQrwQqMf5ubyit0BRHbtX5GLWwm6hCEOX8ZykrK0UbA8+kyGwSqkWbb5IjUMQrlQpItk1emrxo0S2Mj7z+LCNXBNw9wPvTs4TXnpHvcA/7W0vGFxnbXUcUArUBztNHywpD+rVHjFZYuGJwMsWfHAFKH/OfePAstqGnl3GSJjCrEJqVujQo1cqmC7NmyWd2gxPnqHK";

        public void init(Telemetry telemetry) {

        /* ************INIT OPENCV:************* */

            telemetry.addLine("Initializing OpenCV");
            telemetry.update();

            if (!OpenCVLoader.initDebug()) {
                telemetry.addData("ERROR!", "OPENCV NOT LOADED!");
                telemetry.update();
                throw new Error("OpenCV Not Loaded!");
            }



        /* ************INIT VUFORIA:************* */

            telemetry.addLine("Initializing Vuforia");
            telemetry.update();

            //init the VuforiaLocalizer parameters object
            VuforiaLocalizer.Parameters params = new VuforiaLocalizer.Parameters(com.qualcomm.ftcrobotcontroller.R.id.cameraMonitorViewId);

            //Which side of the phone?
            params.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

            //License Key
            params.vuforiaLicenseKey = LICENSE_KEY;

            //What to display on top of visual matches?
            params.cameraMonitorFeedback = VuforiaLocalizer.Parameters.CameraMonitorFeedback.AXES;

            //Create Vuforia instance with params: -- takes 1-2s
            vuforia = ClassFactory.createVuforiaLocalizer(params);

            //save an rgb565 image for further processing each frame and only save current frame
            Vuforia.setFrameFormat(RGB565, true);
            vuforia.setFrameQueueCapacity(1);


            //Hints are suggestions to do if the phone can, but don't give error if it can't change
            //change to object if necessary
            //Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 3);



        /* ************INIT PICTOGRAPH ASSETS:************* */

            telemetry.addLine("Initializing Pictograph Assets");
            telemetry.update();

            //Prepares tracking asset group -- takes 1s
            pictographTrackables = vuforia.loadTrackablesFromAsset("Pictographs");
        }

        public void start() {
            //Begin tracking pictographs
            pictographTrackables.activate();
        }

        public int decide(Telemetry telemetry) {
        /* ************SETUP VARS FOR RUN!************* */
            //Suspected pictograph codes
            int[] pictographCodes = new int[NUM_OF_TAKES];

            //Final pictograph code --> 0: L, 1: C, 2: R, 3: N
            int finalPictographCode = 3;

            //Black offset from orange --> changes based on lighting and printout
            int blackOff = -50;

            //The number of times through the main loop
            int runCount = 0;

        /* ************BEGIN!************* */



            //Iterate through all trackables
            for (int j = 0; j < NUM_OF_TAKES + 3; j++) {
                for (VuforiaTrackable item : pictographTrackables) {
                /* ************GET POSE AND TRANSLATION OF PICTOGRAPH IF VISIBLE************* */

                    //create OpenGLMatrix containing the translation and rotation of the item relative to the robot if it is visible, else null
                    OpenGLMatrix pose = ((VuforiaTrackableDefaultListener) item.getListener()).getPose();

                    //if Vuforia can see this item...
                    if (pose != null) {
                    /* ************DECIDE WHICH PICTOGRAPH WE ARE LOOKING AT************* */

                        // calculate on first 5 runs through to have more resilience to shaking
                        if (runCount < NUM_OF_TAKES) {

                            //Get vuforia's raw Pose data to be converted to OpenCV
                            OpenGLMatrix rawPoseV = ((VuforiaTrackableDefaultListener) item.getListener()).getRawPose();

                            //Get position data in matrix form for Tool.projectPoint
                            Matrix34F rawPose = new Matrix34F();
                            float[] poseData = Arrays.copyOfRange(rawPoseV.transposed().getData(), 0, 12);
                            rawPose.setData(poseData);

                            //Get points of corners of item on 2d image (UR, UL, BL, BR) Units: Inch
                            float[][] srcPoints = new float[4][2];
                            srcPoints[0] = Tool.projectPoint(vuforia.getCameraCalibration(), rawPose, new Vec3F(2.75f, -0.25f, 0f)).getData();
                            srcPoints[1] = Tool.projectPoint(vuforia.getCameraCalibration(), rawPose, new Vec3F(1.5f, -0.25f, 0f)).getData();
                            srcPoints[2] = Tool.projectPoint(vuforia.getCameraCalibration(), rawPose, new Vec3F(1.5f, -2.75f, 0f)).getData();
                            srcPoints[3] = Tool.projectPoint(vuforia.getCameraCalibration(), rawPose, new Vec3F(2.75f, -2.75f, 0f)).getData();




                        /* ************Get Image From Camera************* */

                            //get frame: 5 types: 1 rgb and 4 grayscale --> We want rgb
                            try {
                                VuforiaLocalizer.CloseableFrame frame = vuforia.getFrameQueue().take();


                                //iterate to find rgb565
                                for (int i = 0; i < frame.getNumImages(); i++) {
                                    if (frame.getImage(i).getFormat() == PIXEL_FORMAT.RGB565) {
                                        Image vuforiaImage = frame.getImage(i);

                                        // make bitmap (Android default image format) from Vuforia Image
                                        Bitmap srcBmp = Bitmap.createBitmap(vuforiaImage.getWidth(), vuforiaImage.getHeight(), Bitmap.Config.RGB_565);
                                        srcBmp.copyPixelsFromBuffer(vuforiaImage.getPixels());

                                        //Make OpenCV Mat from the bitmap
                                        Mat rawMat = new Mat(srcBmp.getWidth(), srcBmp.getHeight(), CvType.CV_8UC3);
                                        Utils.bitmapToMat(srcBmp, rawMat);

                                        //Translate srcPoints (float[][]) to OpenCV Point array to make Mat from
                                        Point[] srcPointAr = new Point[4];
                                        srcPointAr[0] = new Point((double) srcPoints[0][0], (double) srcPoints[0][1]);
                                        srcPointAr[1] = new Point((double) srcPoints[1][0], (double) srcPoints[1][1]);
                                        srcPointAr[2] = new Point((double) srcPoints[2][0], (double) srcPoints[2][1]);
                                        srcPointAr[3] = new Point((double) srcPoints[3][0], (double) srcPoints[3][1]);

                                        //Create destination mat (simple rect to homography onto)
                                /*
                                     (0,0)             (30, 0)



                                     (0,60)            (30, 60)
                                 */
                                        Point[] destPointAr = new Point[4];
                                        destPointAr[0] = new Point(30, 0);
                                        destPointAr[1] = new Point(0, 0);
                                        destPointAr[2] = new Point(0, 60);
                                        destPointAr[3] = new Point(30, 60);

                                        //Create Mats from Point arrays
                                        MatOfPoint2f srcMat = new MatOfPoint2f(srcPointAr);
                                        MatOfPoint2f destMat = new MatOfPoint2f(destPointAr);

                                        //Find homography (Mat to warp mat by so that the specified points on srcMat now have the corresponding values in destMat)
                                        Mat homo = Calib3d.findHomography(srcMat, destMat);
                                        Mat mMat = new Mat();

                                        //Use Inter_Nearest to keep picture pixellated, not blurred, size must correspond to destMat
                                        Imgproc.warpPerspective(rawMat, mMat, homo, new Size(30, 60), Imgproc.INTER_NEAREST);

                                        //If SAVE_CROPPED, save mMat as png
                                        if (SAVE_CROPPED) {
                                            Bitmap outBmp = Bitmap.createBitmap(mMat.width(), mMat.height(), Bitmap.Config.RGB_565);
                                            Utils.matToBitmap(mMat, outBmp);//mat for output

                                            try {
                                                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Output_" + Math.random() + ".png");
                                                FileOutputStream outStream = new FileOutputStream(file);
                                                outBmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                                                outStream.close();
                                            } catch (Exception e) {
                                                telemetry.addLine(e.toString());
                                                telemetry.update();
                                            }
                                        }



                                /* ************SIMPLIFY IMAGE TO 'B' (BLACK) AND 'O' (ORANGE/RED)************* */

                                        //The result after this step: an array of 60 (y) Strings each 30 (x) characters long
                                        String[] textImage = new String[60];


                                        // telemetric chart used to get color values while debugging
                                /*for (int fy = 59; fy > 49; fy--) {
                                    textImage[59-fy] = "";
                                    for (int fx = 0; fx < 30; fx++) {
                                        if (output1.get(fy, fx)[2] < 100) {
                                            textImage[59-fy] += 'A';
                                        } else if (output1.get(fy, fx)[2] < 150) {
                                            textImage[59-fy] += 'B';
                                        } else if (output1.get(fy, fx)[2] < 175) {
                                            textImage[59-fy] += 'C';
                                        } else if (output1.get(fy, fx)[2] < 200) {
                                            textImage[59-fy] += 'D';
                                        } else if (output1.get(fy, fx)[2] < 220) {
                                            textImage[59-fy] += 'E';
                                        } else if (output1.get(fy, fx)[2] < 230) {
                                            textImage[59-fy] += 'F';
                                        } else {
                                            textImage[59-fy] += 'G';
                                        }
                                    }
                                }*/

                                        // 0:Left,1:Center,2:Right,3:Null/Can't tell
                                        int pictographCode = 3;

                                        //Use do loop so that condition (did we successfully find a valid code?) is checked at the end of the loop
                                        do {

                                            //Everything is checked in terms of amount of red
                                            //This checks the 2nd up from the far-right column of hexes for color calibration each run because that is always orange and is big enough to use fixed points.
                                            //Takes 5 pixels and averages to find offset
                                            double red = mMat.get(15, 25)[0] + mMat.get(14, 25)[0] + mMat.get(16, 25)[0] + mMat.get(15, 24)[0] + mMat.get(15, 26)[0];
                                            red /= 5;

                                            //adds the value gotten above to the overall blackOffset variable initialized before waitForStart(); which changes based on print brightness and external lighting furhter down.
                                            double black = red + blackOff;

                                            //For each pixel, if amount of red is less than the black threshold, it is black, else orange/red
                                            for (int fy = 59; fy > 0; fy--) {
                                                textImage[59 - fy] = "";
                                                for (int fx = 0; fx < 30; fx++) {
                                                    if (mMat.get(fy, fx)[0] < black) {
                                                        textImage[59 - fy] += 'B';
                                                    } else {
                                                        textImage[59 - fy] += 'O';
                                                    }
                                                }
                                            }

                                            //SHOW_TEXT_IMAGE, show the textual image representation in telemetry and wait 1 second to be able to see it
                                            if (SHOW_TEXT_IMAGE) {
                                                for (String line : textImage) {
                                                    telemetry.addLine(line);
                                                }
                                                telemetry.update();
                                            }


                                    /* ************USE BOTTOM TWO HEXES TO DECIDE:************* */

                                            //loop through bottom 10 lines of the image starting at 4: approx. middle of first hexes
                                            //should only go through loop once, but if it is all 'O' (below the beginning of the hexes), then keep looping until 10 at which point it is lighting issue
                                            for (int mloop = 4; (pictographCode == 3 && mloop < 10); mloop++) {

                                                //change to array to make easier access
                                                char[] charImage = textImage[mloop].toCharArray();
                                                //holds the number of black in a row: 5+ counts as one hex
                                                int inARow = 0;
                                                //defaults to Orange hex unless there is black
                                                char firsthex = 'O';
                                                char lasthex = 'O';

                                                boolean onFirstHex = false;

                                                for (int ch = 0; ch < charImage.length; ch++) {
                                                    if (charImage[ch] == 'B') {
                                                        //increase inARow
                                                        inARow++;

                                                        //if more than 13 in a row, reset counters as if interrupted because this is the end/past the end of the first hex
                                                        if (inARow > 9) {
                                                            onFirstHex = false;
                                                            inARow = 0;
                                                        }
                                                        if (inARow > 4 && !onFirstHex) {
                                                            onFirstHex = true;
                                                            //If past halfway, lasthex, else firsthex
                                                            if (ch > 15) {
                                                                lasthex = 'B';
                                                            } else {
                                                                firsthex = 'B';
                                                            }
                                                        }
                                                    } else {
                                                        //reset inARow because it has been interrupted by 1+ orange
                                                        inARow = 0;
                                                        onFirstHex = false;
                                                    }
                                                }

                                                //Decide based on first and second hex
                                                if (firsthex == 'B') {
                                                    if (lasthex == 'O') {
                                                        pictographCode = 0;
                                                    } else {
                                                        pictographCode = 2;
                                                    }
                                                } else {
                                                    if (lasthex == 'B') {
                                                        pictographCode = 1;
                                                    } else {
                                                        pictographCode = 3;
                                                    }
                                                }
                                            }

                                            //Both hexes orange... increase blackOff and go to beginning of loop
                                            if (pictographCode == 3) {
                                                blackOff += blackOff > 10 ? 7 : 15;
                                            }
                                        } while (pictographCode == 3);

                                        //save current code to array for later
                                        pictographCodes[runCount] = pictographCode;


                                        telemetry.addLine(pictographCode + "");
                                        telemetry.update();


                                    }

                                }
                            } catch (Exception e) {
                                telemetry.addLine(e.toString());
                            }
                            //When finished with enough tries, find most common answer (sometimes 1+ is different...)
                        } else if (runCount == NUM_OF_TAKES) {
                            int[] codes = new int[3];
                            for (int code : pictographCodes) {

                                //increase the code
                                if (code != 3) {
                                    codes[code]++;
                                }
                            }

                            String finalColumn = "RIGHT";
                            finalPictographCode = 2;

                            if (codes[0] > codes[1] && codes[0] > codes[2]) {
                                finalColumn = "LEFT!";
                                finalPictographCode = 0;
                            } else if (codes[1] > codes[0] && codes[1] > codes[2]) {
                                finalColumn = "CENTER!";
                                finalPictographCode = 1;
                            }

                            telemetry.addLine(finalColumn);
                            telemetry.addLine("" + codes[0] + ", " + codes[1] + ", " + codes[2]);
                            telemetry.update();
                        }
                        runCount++;
                    }
                }

            }
            return finalPictographCode;
        }
    }
