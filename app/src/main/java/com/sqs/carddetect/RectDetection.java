package com.sqs.carddetect;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class RectDetection extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, Camera.PictureCallback {

    private ArrayList<Point> point = new ArrayList<>();
    static Corners corners = new Corners();
    int witdhtsta;
    int heighrsta;
    double xScaleFactor;
    double yScaleFactor;
    static boolean fromBack;
    //view holder
    private CameraBridgeViewBase cameraBridgeViewBase;
    //camera listener callback
    private BaseLoaderCallback baseLoaderCallback;
    //image holder
    private Mat bwIMG, hsvIMG, lrrIMG, urrIMG, dsIMG, usIMG, cIMG, hovIMG;
    private MatOfPoint2f approxCurve;
    private int threshold;
    private ImageView imageView,preview;
    private Mat img;
    private double max_rect = 0;
    private MatOfPoint cntMax = null;
    private DrawView draw_layout;
    static Mat rgb90;
    private boolean isClicked;
    private Button reset;

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
        reset = findViewById(R.id.reset);
        draw_layout = findViewById(R.id.draw_layout);
        //initialize treshold
        threshold = 100;
        point.clear();
        imageView = findViewById(R.id.pic);
        preview=findViewById(R.id.preview);
        cameraBridgeViewBase = findViewById(R.id.cameraViewer);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        reset.setOnClickListener(view -> {
            point.clear();
            max_rect = 0;
            cntMax = null;
            draw_layout.setPath(getPath(point));
            draw_layout.invalidate();
        });
        imageView.setOnClickListener(v -> {
            try {
                if (!isClicked) {
                    isClicked = true;
                    callNext().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                        startActivity(new Intent(RectDetection.this, RectPreView.class));
                        isClicked = false;
                    });
                }
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

               /* saveFile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> {
                            startActivity(new Intent(RectDetection.this, RectPreView.class));

                        });*/

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

    Observable<Object> callNext() {
        return Observable.create(emitter -> {
            rgb90 = new Mat(img.rows(), img.cols(), CvType.CV_8UC1);//CV_8UC1
            Core.flip(img.t(), rgb90, 1);
            emitter.onNext(new Path());
            emitter.onComplete();
        });
    }

    Observable<Object> saveFile() {
        return Observable.create(emitter -> {
            Bitmap bmp = null;
            try {
                if (img.cols() > 0 && img.rows() > 0) {
                   /* bmp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(img, bmp);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), true);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
*/

//                    FileOutputStream out = null;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
                    String filename = dateFormat.format(new Date());
                    File sd = new File(Environment.getExternalStorageDirectory() + "/frames");
                    boolean success = true;
                    if (!sd.exists()) {
                        success = sd.mkdir();
                    }
                    if (success) {
                        File dest = new File(sd, filename);

                        try {
                            /*out = new FileOutputStream(dest);
                            rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);*/
                            Mat rgb90 = new Mat(img.rows(), img.cols(), CvType.CV_8UC1);//CV_8UC1
                            Core.flip(img.t(), rgb90, 1);
                            String matoto = StaticCall.matToJson(rgb90);
                            try {
                                OutputStream outputStream = new FileOutputStream(new File(dest.getAbsolutePath()));
                                outputStream.write(matoto.getBytes());
                                outputStream.close();
                            } catch (IOException e) {
                                Log.e("Exception", "File write failed: " + e.toString());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("TAG", e.getMessage());
                        } finally {
                            /*try {
                                if (out != null) {
                                    out.close();
                                    Log.d("TAG", "OK!!");
                                }
                            } catch (IOException e) {
                                Log.d("TAG", e.getMessage() + "Error");
                                e.printStackTrace();
                            }*/
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

        /*Mat dstnew = new Mat();//CV_8UC1
        Core.flip(dst.t(), dstnew, 1);
        Mat resizeImage = new Mat();
        Size sz = new Size(dst.cols(), dst.rows()); // Scale up to 800x600
        Imgproc.resize(dstnew, resizeImage, sz);*/

//        Point src_center=new Point(dst.cols()/2.0F, dst.rows()/2.0F);
//        Mat rot_mat = Imgproc.getRotationMatrix2D(src_center, -90, 1.0);
//        Mat dstnew=new Mat(gray.cols() , gray.rows(), CvType.CV_8UC1);
//       Imgproc. warpAffine(dst, dstnew, rot_mat, dst.size());

//        Mat rgb = new Mat(dst.rows() , dst.cols(), CvType.CV_8UC1);
//        Core.flip(dst.t(), dst, 1);
//        Imgproc.threshold(gray, dsIMG, 120, 255, Imgproc.THRESH_BINARY);
        //Imgproc.adaptiveThreshold(gray, dsIMG, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 40);
        Mat grayH = new Mat(gray.rows(), gray.cols(), CvType.CV_8UC1);//CV_8UC1
        Core.flip(gray.t(), grayH, 1);
//        System.out.println("dst  height " + grayH.rows());
//        System.out.println("dst  width " + grayH.cols());

        Imgproc.pyrDown(grayH, dsIMG, new Size(grayH.cols() / 2, grayH.rows() / 2));
        Imgproc.pyrUp(dsIMG, usIMG, grayH.size());
        Imgproc.Canny(usIMG, bwIMG, 0, threshold);
        Imgproc.dilate(bwIMG, bwIMG, new Mat(), new Point(-1, 1), 1);


        new Thread(() -> {
            Bitmap ob = Bitmap.createBitmap(bwIMG.cols(), bwIMG.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(bwIMG, ob);
//            Matrix matrix = new Matrix();

//            matrix.postRotate(90);

//            Bitmap scaledBitmap = Bitmap.createScaledBitmap(ob, ob.getWidth(), ob.getHeight(), true);

//            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

            runOnUiThread(() -> {
                preview.setImageBitmap(ob);
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
                        List<Point> points = Arrays.asList(approxCurve.toArray());
                        max_rect = area;
                        cntMax = cnt;
//                        img = dst;

                        setLabel(dst, "X", cntMax, points, grayH);
                    }


                }

            }


        }
        img = dst;

        return dst;

    }

    /* ArrayList<Point> minValue(List<Point> point) {
         ArrayList<Point> retu = new ArrayList<>();
         Point p0 = Collections.min(point, (o1, o2) -> (int) (o1.x + o2.y));
         Point p1 = Collections.min(point, (o1, o2) -> (int) (o1.y - o2.x));
         Point p2 = Collections.max(point, (o1, o2) -> (int) (o1.x + o2.y));
         Point p3 = Collections.max(point, (o1, o2) -> (int) (o1.y - o2.x));
         retu.add(p0);
         retu.add(p1);
         retu.add(p2);
         retu.add(p3);
         return retu;
     }*/
    Path getPath(List<Point> list) {

        Path path = new Path();

        if (list.size() >= 4) {

            Collections.sort(list, (o1, o2) -> {
                double total = o1.y / o2.y;
                int result = 0;
                if (total >= 0.9 && total <= 1.4) {
                    result = Double.compare(o1.x, o2.x);
                }
                return result;
            });
            Collections.sort(list, (o1, o2) -> Double.compare(o1.y, o2.y));

//            for (android.graphics.Point s : list) {
//                System.out.println("x  " + s.x + "  y" + s.y);
//            }

            path.moveTo((float) list.get(0).x, (float) list.get(0).y);
            path.lineTo((float) list.get(1).x, (float) list.get(1).y);
            path.lineTo((float) list.get(3).x, (float) list.get(3).y);
            path.lineTo((float) list.get(2).x, (float) list.get(2).y);
            path.close();
        }
        return path;
    }

    List<Point> getPathSorted(List<Point> list) {


        if (list.size() >= 4) {

            Collections.sort(list, (o1, o2) -> {
                double total = o1.y / o2.y;
                int result = 0;
                if (total >= 0.9 && total <= 1.4) {
                    result = Double.compare(o1.x, o2.x);
                }
                return result;
            });
            Collections.sort(list, (o1, o2) -> Double.compare(o1.y, o2.y));

        }
        return list;
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
        if (fromBack) {
            fromBack = false;
            point.clear();
            max_rect = 0;
            cntMax = null;
            draw_layout.setPath(getPath(point));
            draw_layout.invalidate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    private void setLabel(Mat im, String label, MatOfPoint contour, List<Point> points, Mat grayH) {
        Rect r = Imgproc.boundingRect(contour);
//        rstatic = r;
//        System.out.println("r.x " + r.x + "   r.y " + (r.y));
//        System.out.println("r.x " + r.x + r.width + "   r.y   " + r.y);

//        int width = cameraBridgeViewBase.getWidth();
//        int height = cameraBridgeViewBase.getHeight();
        int matwidth = grayH.cols();
        int matheight = grayH.rows();
        xScaleFactor = (double) matwidth / Resources.getSystem().getDisplayMetrics().widthPixels;
        yScaleFactor = (double) matheight / Resources.getSystem().getDisplayMetrics().heightPixels;

//        System.out.println("xScaleFactor  RAJ W"+matwidth);
//        System.out.println("xScaleFactor  RAJ W "+Resources.getSystem().getDisplayMetrics().widthPixels);
//        System.out.println("xScaleFactor RAJ Y "+matheight);
//        System.out.println("xScaleFactor  RAJ Y "+Resources.getSystem().getDisplayMetrics().heightPixels);
//        System.out.println("xScaleFactor  "+xScaleFactor);
//        System.out.println("xScaleFactor  "+yScaleFactor);
        point.clear();
        /*TODO After Scaled image */
//        cameraBridgeViewBase.floatgetxScaleJava();
//        cameraBridgeViewBase.floatgetyScaleJava();
//        System.out.println("cameraBridgeViewBase.getxoffset() "+cameraBridgeViewBase.getXFinalScal());
//        System.out.println("cameraBridgeViewBase.getxoffset() Y "+cameraBridgeViewBase.getYFinalScal());
        point.add(new Point((int) (r.x / xScaleFactor)/*cameraBridgeViewBase.floatgetxScaleJava())+cameraBridgeViewBase.getxoffset()*/, (int) (r.y / yScaleFactor/*cameraBridgeViewBase.floatgetyScaleJava())+(int)cameraBridgeViewBase.getyoffset())*/)));//+cameraBridgeViewBase.getxoffset()    +cameraBridgeViewBase.getyoffset()
        witdhtsta = (int) ((r.x + r.width) / xScaleFactor/* cameraBridgeViewBase.floatgetxScaleJava()+cameraBridgeViewBase.getxoffset()*/);//+cameraBridgeViewBase.getxoffset()
        heighrsta = (int) ((r.y + r.height) / yScaleFactor/*cameraBridgeViewBase.floatgetyScaleJava()+cameraBridgeViewBase.getyoffset()*/);//+cameraBridgeViewBase.getyoffset()
//        point.add(new android.graphics.Point(r.x , r.y));
//        witdhtsta=r.width;//r.x +
//        heighrsta= r.height;//r.y +
        point.add(new Point((r.x + r.width) / xScaleFactor, r.y / yScaleFactor));
        point.add(new Point((r.x + r.width) / xScaleFactor, (r.y + r.height) / yScaleFactor));
        point.add(new Point(r.x / xScaleFactor, (r.y + r.height) / yScaleFactor));

        ArrayList<Point> pointlocal = new ArrayList<>();
        pointlocal.add(new Point((r.x), (r.y)));
        pointlocal.add(new Point((r.x + r.width), r.y));
        pointlocal.add(new Point((r.x + r.width), (r.y + r.height)));
        pointlocal.add(new Point(r.x, (r.y + r.height)));

        corners.setCorners(getPathSorted(pointlocal));
        corners.setSize(grayH.size());

        runOnUiThread(() -> {
            draw_layout.setPath(getPath(point));
//    draw_layout.setPath((float) point.get(0).x, points, r.x + r.width, r.y + r.height);
//    draw_layout.setPath((float) point.get(0).x, (float) point.get(0).y, witdhtsta, heighrsta);
//    draw_layout.setPath(r.x*xScaleFactor, r.y*yScaleFactor, (r.x + r.width)*xScaleFactor, (r.y +  r.height)*yScaleFactor);

            draw_layout.invalidate();

        });

//        Core.circle(im, new Point(r.x, r.y), 20, new Scalar(0, 255, 0), 5);
//        Core.circle(im, new Point(r.x + r.width, r.y), 20, new Scalar(0, 255, 0), 5);
//        Core.rectangle(im,  new Point(r.x, r.y),new Point(witdhtsta, heighrsta), new Scalar(0, 255, 0), 5);
    }


   /* Path drawPath(List<Point> points) {

        Path path = new Path();
        path.moveTo((float) points.get(0).x, (float) points.get(0).y);
        path.lineTo((float) points.get(1).x, (float) points.get(1).y);
        path.lineTo((float) points.get(2).x, (float) points.get(2).y);
        path.lineTo((float) points.get(3).x, (float) points.get(3).y);
        path.close();
//        path.lineTo((float) points.get(0).x,(float) points.get(0).y);
        return path;
    }*/

    @Override
    public void onPictureTaken(byte[] data, Camera mCamera) {
        mCamera.startPreview();
        mCamera.setPreviewCallback(null);
// Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream("mPictureFileName");

            fos.write(data);
            fos.close();

        } catch (IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }
}
