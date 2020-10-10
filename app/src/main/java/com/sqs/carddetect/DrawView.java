package com.sqs.carddetect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class DrawView extends ImageView {
    private Paint paint = new Paint();
    private Path path = new Path();

    Float xx=0f;
    Float yy=0f;
    Float widthx=0f;
    Float heightx=0f;

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    void init(){
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3f);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    void setPath( Path path) {
        this.path = path;
    }
    void setPath(Float x,Float y,Float width,Float height) {
        xx=x;
        yy=y;
        widthx=width;
        heightx=height;
    }

}
