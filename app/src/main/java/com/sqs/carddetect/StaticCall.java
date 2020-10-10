package com.sqs.carddetect;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

import static android.content.ContentValues.TAG;

public class StaticCall {

//    Mat bwIMG, hsvIMG, lrrIMG, urrIMG, dsIMG, usIMG, cIMG, hovIMG;
//    MatOfPoint2f approxCurve;
/*static double max_rect = 0;
   static Mat bwIMG = new Mat();
    static  Mat  dsIMG = new Mat();
    static  Mat  hsvIMG = new Mat();
    static  Mat  lrrIMG = new Mat();
    static  Mat urrIMG = new Mat();
    static  Mat usIMG = new Mat();
    static  Mat cIMG = new Mat();
    static  Mat hovIMG = new Mat();
    static  MatOfPoint2f approxCurve = new MatOfPoint2f();*/

    /*public static Observable<List<Point>> convertMat(byte[] bytes, int width, int height){
        return Observable.create(emitter -> {
            System.out.println("bytes "+bytes.length);
            Mat gray = new Mat(height , width, CvType.CV_8UC1);//CV_8UC1
            gray.put(0, 0, bytes);

//            Mat gray = Highgui.imdecode(new MatOfByte(bytes), Highgui.CV_LOAD_IMAGE_ANYCOLOR);
            System.out.println("gray   "+gray.empty());
            //Mat gray = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);
//            Utils.bitmapToMat(bitmap, gray);
//            Mat rgbMat = new Mat();
//            Imgproc.cvtColor(orignal, rgbMat, Imgproc.COLOR_RGBA2BGR);

//            Mat gray = new Mat();
//        Mat dst = inputFrame.rgba();
//            Imgproc.cvtColor(rgbMat,gray,Imgproc.COLOR_BGR2GRAY);
//        Imgproc.threshold(gray, dsIMG, 120, 255, Imgproc.THRESH_BINARY);
            //Imgproc.adaptiveThreshold(gray, dsIMG, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 40);
            Imgproc.pyrDown(gray, dsIMG, new Size(gray.cols() / 2, gray.rows() / 2));
            Imgproc.pyrUp(dsIMG, usIMG, gray.size());

            Imgproc.Canny(usIMG, bwIMG, 0, 100);

            Imgproc.dilate(bwIMG, bwIMG, new Mat(), new org.opencv.core.Point(-1, 1), 1);
           *//* new Thread(() -> {

                Bitmap ob = Bitmap.createBitmap(bwIMG.cols(), bwIMG.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(bwIMG, ob);
                Matrix matrix = new Matrix();

                matrix.postRotate(90);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(ob, ob.getWidth(), ob.getHeight(), true);

                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                runOnUiThread(() -> {
                    imageView.setImageBitmap(rotatedBitmap);
                });
            }).start();*//*

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
                        Rect r = Imgproc.boundingRect(cnt);
                        double area = r.area();
                        if (max_rect < area) {
                            max_rect = area;
                            //cntMax = cnt;
//                        img = dst;
//                        setLabel(dst, "X", cntMax);
                            ArrayList points = new ArrayList<Point>();

                            points.add(new Point(r.x , r.y));
                            points.add(new Point(r.x + r.width, r.y));
                            points.add(new Point(r.x, r.y + r.height));
                            points.add(new Point(r.x + r.width, r.y + r.height));
                            emitter.onNext(points);
                            emitter.onComplete();
                        }


                    }

                }


            }
        });

    }*/
    private static double angle(org.opencv.core.Point pt1, org.opencv.core.Point pt2, org.opencv.core.Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    public static String matToJson(Mat mat){
        JsonObject obj = new JsonObject();

        if(mat.isContinuous()){
            int cols = mat.cols();
            int rows = mat.rows();
            int elemSize = (int) mat.elemSize();

            byte[] data = new byte[cols * rows * elemSize];

            mat.get(0, 0, data);

            obj.addProperty("rows", mat.rows());
            obj.addProperty("cols", mat.cols());
            obj.addProperty("type", mat.type());

            // We cannot set binary data to a json object, so:
            // Encoding data byte array to Base64.
            String dataString = new String(Base64.encode(data, Base64.DEFAULT));

            obj.addProperty("data", dataString);

            Gson gson = new Gson();
            String json = gson.toJson(obj);

            return json;
        } else {
            Log.e(TAG, "Mat not continuous.");
        }
        return "{}";
    }

    public static Mat matFromJson(String json){
        JsonParser parser = new JsonParser();
        JsonObject JsonObject = parser.parse(json).getAsJsonObject();

        int rows = JsonObject.get("rows").getAsInt();
        int cols = JsonObject.get("cols").getAsInt();
        int type = JsonObject.get("type").getAsInt();

        String dataString = JsonObject.get("data").getAsString();
        byte[] data = Base64.decode(dataString.getBytes(), Base64.DEFAULT);

        Mat mat = new Mat(rows, cols, type);
        mat.put(0, 0, data);

        return mat;
    }

}
