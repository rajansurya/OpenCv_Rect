package io.github.iyotetsuya.rectangledetection;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static org.opencv.core.Core.getTextSize;

public class RectDetection extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, Camera.PictureCallback {

    static ArrayList<android.graphics.Point> point = new ArrayList<>();
    static int witdhtsta;
    static int heighrsta;
    //view holder
    CameraBridgeViewBase cameraBridgeViewBase;
    //camera listener callback
    BaseLoaderCallback baseLoaderCallback;
    //image holder
    Mat bwIMG, hsvIMG, lrrIMG, urrIMG, dsIMG, usIMG, cIMG, hovIMG;
    MatOfPoint2f approxCurve;
    int threshold;
    ImageView imageView;
    Mat img;
    double max_rect = 0;
    MatOfPoint cntMax = null;

    private static double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.javacamera);

        //initialize treshold
        threshold = 100;
        point.clear();
        imageView = findViewById(R.id.pic);
        cameraBridgeViewBase = findViewById(R.id.cameraViewer);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        imageView.setOnClickListener(v -> {
            try {

               /* SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String currentDateandTime = sdf.format(new Date());
                String fileName = Environment.getExternalStorageDirectory().getPath() +
                        "/sample_picture_" + currentDateandTime + ".jpg";

                cameraBridgeViewBase.takePicture(fileName);*/
                /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String currentDateandTime = sdf.format(new Date());
                String fileName = Environment.getExternalStorageDirectory().getPath() +
                        "/sample_picture_" + currentDateandTime + ".jpg";
                Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
               // String filename = "/storage/emulated/0/DCIM/Camera/samplepass.jpg";
                Highgui.imwrite(fileName, img);*/

                saveFile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> {
                            System.out.println("HHHHHHHHH");
                            startActivity(new Intent(RectDetection.this, RectPreView.class));

                        });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //create camera listener callback
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        Log.v("aashari-log", "Loader interface success");
                        bwIMG = new Mat();
                        dsIMG = new Mat();
                        hsvIMG = new Mat();
                        lrrIMG = new Mat();
                        urrIMG = new Mat();
                        usIMG = new Mat();
                        cIMG = new Mat();
                        hovIMG = new Mat();
                        approxCurve = new MatOfPoint2f();
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

    }

    Observable<Object> saveFile() {
        return Observable.create(emitter -> {
            Bitmap bmp = null;
            try {
                if (img.cols() > 0 && img.rows() > 0) {
                    bmp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(img, bmp);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), true);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);


                    FileOutputStream out = null;
                    String filename = "frame.txt";
                    File sd = new File(Environment.getExternalStorageDirectory() + "/frames");
                    boolean success = true;
                    if (!sd.exists()) {
                        success = sd.mkdir();
                    }
                    if (success) {
                        File dest = new File(sd, filename);

                        try {
                            out = new FileOutputStream(dest);
                         String matoto= StaticCall.  matToJson(img);
                            try {
                                OutputStream outputStream = new FileOutputStream(new File(dest.getAbsolutePath()));
//                                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(dest.getAbsolutePath(), Context.MODE_PRIVATE));
                                outputStream.write(matoto.getBytes());
                                outputStream.close();
                            }
                            catch (IOException e) {
                                Log.e("Exception", "File write failed: " + e.toString());
                            }
//                            rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                            // PNG is a lossless format, the compression factor (100) is ignored

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("TAG", e.getMessage());
                        } finally {
                            try {
                                if (out != null) {
                                    out.close();
                                    Log.d("TAG", "OK!!");
                                }
                            } catch (IOException e) {
                                Log.d("TAG", e.getMessage() + "Error");
                                e.printStackTrace();
                            }
                        }
                    }
                }
                emitter.onNext(new Path());
                emitter.onComplete();
            } catch (CvException e) {
                Log.d("TAG", e.getMessage());
            }
        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat gray = inputFrame.gray();
        Mat dst = inputFrame.rgba();

//        Imgproc.threshold(gray, dsIMG, 120, 255, Imgproc.THRESH_BINARY);
        //Imgproc.adaptiveThreshold(gray, dsIMG, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 40);
        Imgproc.pyrDown(gray, dsIMG, new Size(gray.cols() / 2, gray.rows() / 2));
        Imgproc.pyrUp(dsIMG, usIMG, gray.size());

        Imgproc.Canny(usIMG, bwIMG, 0, threshold);

        Imgproc.dilate(bwIMG, bwIMG, new Mat(), new Point(-1, 1), 1);
        new Thread(() -> {

            Bitmap ob = Bitmap.createBitmap(bwIMG.cols(), bwIMG.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(bwIMG, ob);
            Matrix matrix = new Matrix();

            matrix.postRotate(90);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(ob, ob.getWidth(), ob.getHeight(), true);

            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

            runOnUiThread(() -> {
                imageView.setImageBitmap(rotatedBitmap);
            });
        }).start();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        cIMG = bwIMG.clone();

        Imgproc.findContours(cIMG, contours, hovIMG, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        for (MatOfPoint cnt : contours) {

            MatOfPoint2f curve = new MatOfPoint2f(cnt.toArray());

            Imgproc.approxPolyDP(curve, approxCurve, 0.02 * Imgproc.arcLength(curve, true), true);

            int numberVertices = (int) approxCurve.total();

            double contourArea = Imgproc.contourArea(cnt);

            if (Math.abs(contourArea) < 100) {
                continue;
            }

            //Rectangle detected
            if (numberVertices >= 4 && numberVertices <= 6) {

                List<Double> cos = new ArrayList<>();

                for (int j = 2; j < numberVertices + 1; j++) {
                    cos.add(angle(approxCurve.toArray()[j % numberVertices], approxCurve.toArray()[j - 2], approxCurve.toArray()[j - 1]));
                }

                Collections.sort(cos);

                double mincos = cos.get(0);
                double maxcos = cos.get(cos.size() - 1);

                if (numberVertices == 4 && mincos >= -0.1 && maxcos <= 0.3) {
                    Rect rect = Imgproc.boundingRect(cnt);
                    double area = rect.area();
                    if (max_rect < area) {
                        max_rect = area;
                        cntMax = cnt;
                        img = dst;
                        setLabel(dst, "X", cntMax);
                    }


                }

            }


        }

        return dst;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There is a problem", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    private void setLabel(Mat im, String label, MatOfPoint contour) {
        int fontface = Core.FONT_HERSHEY_SIMPLEX;
        double scale = 3;//0.4;
        int thickness = 3;//1;
        int[] baseline = new int[1];
//        Size text = getTextSize(label, fontface, scale, thickness, baseline);
        Rect r = Imgproc.boundingRect(contour);
//        System.out.println("r.x " + r.x + "   r.y " + r.y + r.height);
//        System.out.println("r.x " + r.x + r.width + "   r.y   " + r.y);
        point.clear();
        point.add(new android.graphics.Point(r.x , r.y));
//        point.add(new android.graphics.Point((int)(r.x*cameraBridgeViewBase.getScaledValue()+cameraBridgeViewBase.getxoffset()), (int)(r.y*cameraBridgeViewBase.getScaledValue()+cameraBridgeViewBase.getyoffset())));
//        witdhtsta=(int)((r.x + r.width)*cameraBridgeViewBase.getScaledValue()+cameraBridgeViewBase.getxoffset());
//        heighrsta=(int)((r.y + r.height)*cameraBridgeViewBase.getScaledValue()+cameraBridgeViewBase.getyoffset());
        witdhtsta=r.x + r.width;
        heighrsta=r.y + r.height;
        point.add(new android.graphics.Point(r.x + r.width, r.y));
        point.add(new android.graphics.Point(r.x, r.y + r.height));
        point.add(new android.graphics.Point(r.x + r.width, r.y + r.height));





//        Core.circle(im, new Point(r.x, r.y), 20, new Scalar(0, 255, 0), 5);
//        Core.circle(im, new Point(r.x + r.width, r.y), 20, new Scalar(0, 255, 0), 5);
//        Core.circle(im, new Point(r.x, r.y + r.height), 20, new Scalar(0, 255, 0), 5);
        Core.rectangle(im,  new Point(r.x, r.y),new Point(r.x + r.width, r.y + r.height), new Scalar(0, 255, 0), 5);
        //Point pt = new Point(r.x + ((r.width - text.width) / 2), r.y + ((r.height + text.height) / 2));

        //putText(im, label, pt, fontface, scale, new Scalar(255, 0, 0), thickness);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera mCamera) {
        mCamera.startPreview();
        mCamera.setPreviewCallback(null);
// Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream("mPictureFileName");

            fos.write(data);
            fos.close();

        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }
}
