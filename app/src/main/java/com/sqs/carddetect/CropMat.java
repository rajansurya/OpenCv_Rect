package com.sqs.carddetect;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.sqs.carddetect.RectDetection.corners;
import static com.sqs.carddetect.RectDetection.rgb90;

public class CropMat {
    private Context context;
    private ICropView iCropView;
    private Mat matsrc;

    CropMat(Context context, ICropView iCropView) {
        this.context = context;
        this.iCropView = iCropView;
        matsrc =rgb90; //StaticCall.matFromJson(readFromFile(context));

        Bitmap bmp = null;
        bmp = Bitmap.createBitmap(matsrc.cols(), matsrc.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matsrc, bmp);
        iCropView.getPaper().setImageBitmap(bmp);
        iCropView.getPaperRect().onCorners2Crop(corners, matsrc.size());
    }

    void crop() {
        Mat pc = cropPicture(matsrc, iCropView.getPaperRect().getCorners2Crop());
        Bitmap croppedBitmap = Bitmap.createBitmap(pc.width(), pc.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(pc, croppedBitmap);
        iCropView.getCroppedPaper().setImageBitmap(resize(croppedBitmap, Resources.getSystem().getDisplayMetrics().widthPixels-50,Resources.getSystem().getDisplayMetrics().widthPixels-50));
        iCropView.getPaper().setVisibility(View.GONE);
        iCropView.getPaperRect().setVisibility(View.GONE);
    }

    Mat cropPicture(Mat picture, List<Point> pts) {

//        pts.forEach { Log.i(TAG, "point: " + it.toString()) }
        Point tl = pts.get(0);
        Point tr = pts.get(1);
        Point br = pts.get(2);
        Point bl = pts.get(3);

        double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2.0) + Math.pow(br.y - bl.y, 2.0));
        double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2.0) + Math.pow(tr.y - tl.y, 2.0));

        double dw = Math.max(widthA, widthB);
        int maxWidth = (int) dw;


        double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2.0) + Math.pow(tr.y - br.y, 2.0));
        double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2.0) + Math.pow(tl.y - bl.y, 2.0));

        double dh = Math.max(heightA, heightB);
        int maxHeight = (int) dh;

        Mat croppedPic = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x, tl.y, tr.x, tr.y, br.x, br.y, bl.x, bl.y);
        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh);

        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Imgproc.warpPerspective(picture, croppedPic, m, croppedPic.size());
        m.release();
        src_mat.release();
        dst_mat.release();
        Log.i(TAG, "crop finish");
        return croppedPic;
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            File sd = new File(Environment.getExternalStorageDirectory() + "/frames");
            String filename = "frame.txt";
            File dest = new File(sd, filename);

//            InputStream inputStream = context.openFileInput(dest.getAbsolutePath());
            FileInputStream inputStream = new FileInputStream(new File(dest.getAbsolutePath()));
            if (inputStream != null) {

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return ret;
    }
}
