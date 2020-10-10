package com.sqs.carddetect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


import static android.content.ContentValues.TAG;
import static android.content.Context.WINDOW_SERVICE;

public class DrawRectangle extends View {
    Point defaultTl = new Point(180.0, 320.0);
    Point defaultTr = new Point(900.0, 320.0);
    Point defaultBr = new Point(900.0, 1600.0);
    Point defaultBl = new Point(180.0, 1600.0);
    private Paint rectPaint = new Paint();
    private Paint circlePaint = new Paint();
    private Double ratioX = 1.0;
    private Double ratioY = 1.0;
    private Point tl = new Point();
    private Point tr = new Point();
    private Point br = new Point();
    private Point bl = new Point();
    private Path path = new Path();
    private Point point2Move = new Point();
    private boolean cropMode = false;
    private float latestDownX = 0.0F;
    private float latestDownY = 0.0F;


    public DrawRectangle(Context context) {
        super(context);
        init();
    }

    public DrawRectangle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawRectangle(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        rectPaint.setColor(Color.GREEN);
        rectPaint.setAntiAlias(true);
        rectPaint.setDither(true);
        rectPaint.setStrokeWidth(6f);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeJoin(Paint.Join.ROUND);      // set the join to round you want
        rectPaint.setStrokeCap(Paint.Cap.ROUND);        // set the paint cap to round too
        rectPaint.setPathEffect(new CornerPathEffect(10f));

        circlePaint.setColor(Color.LTGRAY);
        circlePaint.setDither(true);
        circlePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth(4f);
        circlePaint.setStyle(Paint.Style.STROKE);
    }

    public void onCornersDetected(Corners corners) {
        ratioX = corners.getSize().width / (getMeasuredWidth());
        ratioY = corners.getSize().height / (getMeasuredHeight());
        tl = corners.getCorners().get(0);
        tr = corners.getCorners().get(1);
        br = corners.getCorners().get(3);
        bl = corners.getCorners().get(2);

        Log.i(TAG, "POINTS ------>  ${tl.toString()} corners");

        resize();
        path.reset();
        path.moveTo((float) tl.x, (float) tl.y);
        path.lineTo((float) tr.x, (float) tr.y);
        path.lineTo((float) br.x, (float) br.y);
        path.lineTo((float) bl.x, (float) bl.y);
        path.close();
        invalidate();
    }

    void onCornersNotDetected() {
        path.reset();
        invalidate();
    }

    void onCorners2Crop(Corners corners, Size size) {

        cropMode = true;
        tl = corners.getCorners().get(0) != null ? corners.getCorners().get(0) : defaultTl;
        tr = corners.getCorners().get(1) != null ? corners.getCorners().get(1) : defaultTr;
        br = corners.getCorners().get(3) != null ? corners.getCorners().get(3) : defaultBr;
        bl = corners.getCorners().get(2) != null ? corners.getCorners().get(2) : defaultBl;

        WindowManager wm = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
//        int height = displayMetrics.heightPixels;
//        int width = displayMetrics.widthPixels;


        //exclude status bar height
//        val statusBarHeight = getStatusBarHeight(context);
        ratioX = size.width / (displayMetrics.widthPixels);
        ratioY = size.height / (displayMetrics.heightPixels);
        resize();
        movePoints();
    }


    List<Point> getCorners2Crop() {
        reverseSize();
        List<Point> points = new ArrayList<>();
        points.add(tl);
        points.add(tr);
        points.add(br);
        points.add(bl);
        return points;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, rectPaint);
        if (cropMode) {
            canvas.drawCircle((float) tl.x, (float) tl.y, 20F, circlePaint);
            canvas.drawCircle((float) tr.x, (float) tr.y, 20F, circlePaint);
            canvas.drawCircle((float) bl.x, (float) bl.y, 20F, circlePaint);
            canvas.drawCircle((float) br.x, (float) br.y, 20F, circlePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!cropMode) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                latestDownX = event.getX();
                latestDownY = event.getY();
                calculatePoint2Move(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                point2Move.x = (event.getX() - latestDownX) + point2Move.x;
                point2Move.y = (event.getY() - latestDownY) + point2Move.y;
                movePoints();
                latestDownY = event.getY();
                latestDownX = event.getX();
                break;

        }
        return true;

    }

    public Point minBy(List<Point> points,Float downX, Float downY) {
        Iterator<Point> iterator = points.iterator();
        if (!iterator.hasNext()) return null;
        Point minElem = iterator.next();
        int minValue =Math.abs((int) (minElem.x - downX) * (int) (minElem.y - downY)) ;
        while (iterator.hasNext()) {
            Point e = iterator.next();
            int v = Math.abs((int) (e.x - downX) * (int) (e.y - downY));
            if (minValue > v) {
                minElem = e;
                minValue = v;
            }
        }
        return minElem;
    }

    private void calculatePoint2Move(Float downX, Float downY) {
        List<Point> points = new ArrayList<>();
        points.add(tl);
        points.add(tr);
        points.add(br);
        points.add(bl);
        Point dd=  minBy(points,downX,downY);
//      Point dd=  Collections.min(points, (o1, o2) -> Math.abs((int) (o1.x - downX) * (int) (o2.y - downY)));
        point2Move = dd !=null?dd:tl;
    }

    private void movePoints() {
        path.reset();
        path.moveTo((float) tl.x, (float) tl.y);
        path.lineTo((float) tr.x, (float) tr.y);
        path.lineTo((float) br.x, (float) br.y);
        path.lineTo((float) bl.x, (float) bl.y);
        path.close();
        invalidate();
    }

    private void resize() {
        tl.x =(double) tl.x / (ratioX);
        tl.y = (double)tl.y / (ratioY);
        tr.x =(double) tr.x / (ratioX);
        tr.y =(double) tr.y / (ratioY);
        br.x =(double) br.x / (ratioX);
        br.y =(double) br.y / (ratioY);
        bl.x =(double) bl.x / (ratioX);
        bl.y =(double) bl.y / (ratioY);
    }


    private void reverseSize() {
        tl.x =(double) tl.x * (ratioX);
        tl.y =(double) tl.y * (ratioY);
        tr.x = (double)tr.x * (ratioX);
        tr.y =(double) tr.y * (ratioY);
        br.x =(double) br.x * (ratioX);
        br.y = (double)br.y * (ratioY);
        bl.x = (double)bl.x * (ratioX);
        bl.y = (double)bl.y * (ratioY);
    }


}
