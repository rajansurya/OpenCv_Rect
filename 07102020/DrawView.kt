package io.github.iyotetsuya.rectangledetection.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

class DrawView : ImageView {
    private var paint: Paint = Paint()
    private var path: Path = Path()

 var xx:Float=0f
    var yy:Float=0f
    var widthx:Float=0f
    var heightx:Float=0f


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        paint.color = Color.BLUE
        paint.strokeWidth = 7f
        paint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        /*this.path.let {
            canvas.drawPath(it, paint)

        }*/
//        println(" xx   $xx yy  $yy  widthx  $widthx  heightx $heightx ")
        canvas.drawRect(xx, yy, widthx, heightx, paint);
    }

    fun setPath(path: Path) {
        this.path = path
    }
    fun setPath(x:Float,y:Float,width:Float,height:Float) {
        xx=x
        yy=y
        widthx=width
        heightx=height
    }
}
