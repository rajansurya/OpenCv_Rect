package io.github.iyotetsuya.rectangledetection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.Point;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.github.iyotetsuya.rectangledetection.utils.OpenCVHelper;
import io.github.iyotetsuya.rectangledetection.views.DrawView;

import static io.github.iyotetsuya.rectangledetection.RectDetection.heighrsta;
import static io.github.iyotetsuya.rectangledetection.RectDetection.point;
import static io.github.iyotetsuya.rectangledetection.RectDetection.witdhtsta;

public class RectPreView extends Activity {
    DrawView draw_layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rectpreview);
        ImageView preview = (ImageView) findViewById(R.id.preview);
        draw_layout = (DrawView) findViewById(R.id.draw_layout);
        File sd = new File(Environment.getExternalStorageDirectory() + "/frames");
        String filename = "frame.txt";
        File dest = new File(sd, filename);
        if (dest.exists()) {
         Mat matsrc= StaticCall.matFromJson(  readFromFile(this));
            System.out.println("trry" + matsrc.width());
//            Bitmap myBitmap = BitmapFactory.decodeFile(dest.getAbsolutePath());
//            System.out.println(" myBitmap.getHeight() "+myBitmap.getHeight());
//            System.out.println(" myBitmap.getHeight() "+myBitmap.getWidth());
//            Mat matsrc = Highgui.imread(dest.getAbsolutePath());
//            Mat mat = new Mat(myBitmap.getHeight(), myBitmap.getWidth(), CvType.CV_8UC1);
//            Bitmap bmp32 = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
//            Utils.bitmapToMat(bmp32, mat);
            System.out.println("matsrc" + point.get(0).x);
            Core.rectangle(matsrc, new org.opencv.core.Point(point.get(0).x, point.get(0).y), new org.opencv.core.Point(witdhtsta, heighrsta), new Scalar(0, 255, 0), 5);

            System.out.println("matsrc " + matsrc.height());
            System.out.println("matsrc " + matsrc.width());

            Mat tmp = new Mat();
            Bitmap bmp = null;
            try {
//                Imgproc.cvtColor(matsrc, tmp, Imgproc.COLOR_RGB2BGRA);
//                Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_GRAY2RGBA,4);

                bmp = Bitmap.createBitmap(matsrc.cols(), matsrc.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(matsrc, bmp);
            } catch (CvException e) {
                Log.d("Exception", e.getMessage());
            }
            preview.setImageBitmap(bmp);

            draw_layout.setPath(point.get(0).x, point.get(0).y, witdhtsta, heighrsta);
//            draw_layout.setRotation(90);
            draw_layout.invalidate();

        }
    }

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

            for (Point s : list) {
                System.out.println("x  " + s.x + "  y" + s.y);
            }

            // val points = list.sortedWith(Comparator { lhs: Point, rhs: Point -> getDistance(lhs) - getDistance(rhs) })
            path.moveTo(list.get(0).x, list.get(0).y);
            path.lineTo(list.get(1).x, list.get(1).y);
            path.lineTo(list.get(3).x, list.get(3).y);
            path.lineTo(list.get(2).x, list.get(2).y);
            path.lineTo(list.get(0).x, list.get(0).y);

        }
        return path;
    }

    private int getDistance(Point point) {
        double x1 = 0.0;
        double x2 = point.x;
        double y1 = 0.0;
        double y2 = point.y;
        return (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
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
