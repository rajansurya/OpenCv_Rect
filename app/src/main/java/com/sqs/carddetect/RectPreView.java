package com.sqs.carddetect;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import static com.sqs.carddetect.RectDetection.fromBack;


public class RectPreView extends Activity  implements ICropView{
    DrawRectangle paper_rect;
    TextView crop;
//    Mat grayH;
    ImageView preview,previewcrop;
    Mat ROI;
    CropMat cropMat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rectpreview);
        crop = findViewById(R.id.crop);
        preview = findViewById(R.id.preview);
        previewcrop=findViewById(R.id.previewcrop);
        paper_rect = findViewById(R.id.paper_rect);
        cropMat=new CropMat(this,this);

//        File sd = new File(Environment.getExternalStorageDirectory() + "/frames");
//        String filename = "frame.txt";
//        File dest = new File(sd, filename);


//        if (dest.exists()) {
//            Mat matsrc = StaticCall.matFromJson(readFromFile(this));
//            System.out.println("trry " + matsrc.width() + "  " + matsrc.height());

//            grayH = new Mat(matsrc.rows(), matsrc.cols(), CvType.CV_8UC1);
//            Core.flip(matsrc.t(), grayH, 1);
//            System.out.println("trry gray " + matsrc.width() + "  " + matsrc.height());
/*TODO*/
//            ROI = matsrc.submat( rstatic.y, rstatic.y + rstatic.height,rstatic.x, rstatic.x + rstatic.width);

//            System.out.println("trry gray" + grayH.width() + "  " + grayH.height());
//            Bitmap myBitmap = BitmapFactory.decodeFile(dest.getAbsolutePath());
//            System.out.println(" myBitmap.getHeight() "+myBitmap.getHeight());
//            System.out.println(" myBitmap.getHeight() "+myBitmap.getWidth());
//            Mat matsrc = Highgui.imread(dest.getAbsolutePath());
//            Mat mat = new Mat(myBitmap.getHeight(), myBitmap.getWidth(), CvType.CV_8UC1);
//            Bitmap bmp32 = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
//            Utils.bitmapToMat(bmp32, mat);
//            Imgproc. (matsrc,matsrc, 1);
//            System.out.println("matsrc" + point.get(0).x);

//            double y1=point.get(0).y*Math.cos(90) + point.get(0).x*Math.sin(90);
//            double x1 = - point.get(0).y*Math.sin(90) + point.get(0).x*Math.cos(90);

            /*int mWidth= this.getResources().getDisplayMetrics().widthPixels;
            int mHeight= this.getResources().getDisplayMetrics().heightPixels;

            int x2=mWidth/2-point.get(0).x;
            int y2=mHeight/2-point.get(0).y;

            double y1=y2*Math.cos(90) + x2*Math.sin(90);
            double x1 = - y2*Math.sin(90) + x2*Math.cos(90);*/


            //Core.rectangle(matsrc, new org.opencv.core.Point(point.get(0).x, point.get(0).y), new org.opencv.core.Point(point.get(0).x+witdhtsta, point.get(0).y+heighrsta), new Scalar(0, 206, 0), 5);

//            System.out.println("matsrc " + matsrc.height());
//            System.out.println("matsrc " + matsrc.width());

//            Mat tmp = new Mat();
            /*Bitmap bmp = null;
            try {
//                Imgproc.cvtColor(matsrc, tmp, Imgproc.COLOR_RGB2BGRA);
//                Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_GRAY2RGBA,4);

                bmp = Bitmap.createBitmap(matsrc.cols(), matsrc.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(matsrc, bmp);
            } catch (CvException e) {
                Log.d("Exception", e.getMessage());
            }*/
//            preview.setImageBitmap(bmp);
//            preview.setRotation(90);
//            draw_layout.setPath((float) point.get(0).x, (float) point.get(0).y, witdhtsta, heighrsta);
//            draw_layout.setPath(point.get(0).x, point.get(0).y, point.get(0).x+witdhtsta,point.get(0).y+ heighrsta);
//            draw_layout.setRotation(90);
//            draw_layout.setPath(getPath(point));
//            draw_layout.invalidate();

//        }
        crop.setOnClickListener(v -> {
//            Mat ROI =  grayH.submat((int) (point.get(0).x*xScaleFactor), (int) ((point.get(0).x + heighrsta)*xScaleFactor), (int) (point.get(0).y*xScaleFactor), (int)(( point.get(0).y + witdhtsta)*xScaleFactor));
//           Mat res= cropMat(grayH);
            cropMat.crop();
           /* preview.setImageBitmap(null);
            preview.setVisibility(View.GONE);
            Mat tmp = new Mat();
            Bitmap bmp = null;
            bmp = Bitmap.createBitmap(ROI.cols(), ROI.rows(), Bitmap.Config.ARGB_8888);
            Core.flip(ROI.t(), tmp, 1);
            Utils.matToBitmap(ROI, bmp);
            previewcrop.setImageBitmap(resize(bmp, Resources.getSystem().getDisplayMetrics().widthPixels-50,Resources.getSystem().getDisplayMetrics().widthPixels-50));

            point.clear();*/
//            draw_layout.setPath(getPath(point));
//            draw_layout.invalidate();

        });
    }



    Path getPath(List<org.opencv.core.Point> list) {

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

    @Override
    public void onBackPressed() {
//        rstatic=null;
//        fromBack=true;
        fromBack=true;
        super.onBackPressed();
    }

    Bitmap scaledImage(int width,int height,Bitmap originalImage ){
        Bitmap background = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);

        float originalWidth = originalImage.getWidth();
        float originalHeight = originalImage.getHeight();

        Canvas canvas = new Canvas(background);

        float scale = width / originalWidth;

        float xTranslation = 0.0f;
        float yTranslation = (height - originalHeight * scale) / 2.0f;

        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);

        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        canvas.drawBitmap(originalImage, transformation, paint);

        return background;
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

    @Override
    public ImageView getPaper() {
        return preview;
    }

    @Override
    public DrawRectangle getPaperRect() {
        return paper_rect;
    }

    @Override
    public ImageView getCroppedPaper() {
        return previewcrop;
    }
}
