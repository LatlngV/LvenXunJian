package cn.eyesw.lvenxunjian.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import cn.eyesw.lvenxunjian.R;

/**
 * 圆形图片
 */
public class CircleImageView extends AppCompatImageView {

    private int mBorderWidth;
    private int mBorderColor;
    private Paint mBorderPaint;

    private int mViewWidth;
    private int mViewHeight;
    private Bitmap mBitmap;

    public CircleImageView(Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 初始化
        init(context, attrs);
    }

    /**
     * 初始化
     *
     * @param context 上下文对象
     * @param attrs   自定义属性
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView);
        int indexCount = array.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = array.getIndex(i);
            switch (index) {
                case R.styleable.CircleImageView_border_width:
                    mBorderWidth = (int) array.getDimension(R.styleable.CircleImageView_border_width, 5);
                    break;
                case R.styleable.CircleImageView_border_color:
                    mBorderColor = array.getColor(R.styleable.CircleImageView_border_color, Color.YELLOW);
                    break;
            }
        }
        array.recycle();

        mBorderPaint = new Paint();
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getMeasureSpec(widthMeasureSpec);
        int height = getMeasureSpec(heightMeasureSpec);

        mViewWidth = width - (mBorderWidth * 2);
        mViewHeight = height - (mBorderWidth * 2);

        setMeasuredDimension(width, height);
    }

    private int getMeasureSpec(int measureSpec) {
        int result;

        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);

        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = 200;
        }
        return result;
    }

    /**
     * 设置底图颜色
     */
    public void setBorderColor(int borderColor) {
        if (mBorderPaint != null)
            mBorderPaint.setColor(borderColor);
        invalidate();
    }

    /**
     * 设置外圈的宽度
     */
    public void setBorderWidth(int outCircleWidth) {
        mBorderWidth = outCircleWidth;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        loadBitmap();

        if (mBitmap != null) {
            int min = Math.min(mViewWidth, mViewHeight);
            int circleCenter = min / 2;
            // 画外圆
            canvas.drawCircle(circleCenter + mBorderWidth, circleCenter + mBorderWidth,
                    circleCenter + mBorderWidth, mBorderPaint);

            Bitmap bitmap = Bitmap.createScaledBitmap(mBitmap, min, min, false);
            // 画图片
            canvas.drawBitmap(creatCircleBitmap(bitmap, min), mBorderWidth, mBorderWidth, null);
        }
    }

    /**
     * 加载 Bitmap
     */
    private void loadBitmap() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable();
        if (bitmapDrawable != null) {
            mBitmap = bitmapDrawable.getBitmap();
        }
    }

    /**
     * 创建圆形图片
     *
     * @param source 需要变为圆形的 Bitmap
     * @param min    圆形的 Bitmap 最小值
     * @return 圆形图片
     */
    private Bitmap creatCircleBitmap(Bitmap source, int min) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Bitmap bitmap = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 画内圆
        canvas.drawCircle(min / 2, min / 2, min / 2, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // 绘制图片
        canvas.drawBitmap(source, 0, 0, paint);
        return bitmap;
    }

}
