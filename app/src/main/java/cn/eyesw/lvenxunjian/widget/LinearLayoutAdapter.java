package cn.eyesw.lvenxunjian.widget;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import static android.content.Context.WINDOW_SERVICE;

/**
 * 适配线性布局
 */
public class LinearLayoutAdapter extends LinearLayout {

    private static final int DESIGN_WIDTH = 1080;
    private static final int DESIGN_HEIGHT = 1920;
    private static final float DESIGN_SCALE = 3.0f;
    private float mScale;
    private float mFontScale;
    private float mScaleX = 0;
    private float mScaleY = 0;

    public LinearLayoutAdapter(Context context) {
        super(context);
    }

    public LinearLayoutAdapter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutAdapter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Point point = new Point();
        ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(point);

        // 横屏
        if (point.x > point.y) {
            mScaleX = (point.x * 1.0f / DESIGN_HEIGHT);
            mScaleY = (point.y * 1.0f / DESIGN_WIDTH);
        } else { // 竖屏
            mScaleX = (point.x * 1.0f / DESIGN_WIDTH);
            mScaleY = (point.y * 1.0f / DESIGN_HEIGHT);
        }

        float density = DESIGN_SCALE / getResources().getDisplayMetrics().density;
        float scaleDensity = DESIGN_SCALE / getResources().getDisplayMetrics().scaledDensity;
        float minScale = Math.min(mScaleX, mScaleY);

        mScale = minScale * density;
        mScaleX *= density;
        mScaleY *= density;
        mFontScale = minScale * scaleDensity;

    }

    @Override
    public void addView(View child) {
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
    }

    @Override

    public void addView(View child, ViewGroup.LayoutParams params) {
        if (!isInEditMode()) {
            transformSize(child, (LinearLayout.LayoutParams) params);
        }
        super.addView(child, params);
    }

    private void transformSize(View child, LinearLayout.LayoutParams params) {
        if (params.width > 0 && params.height > 0) { // 按比列
            params.width *= mScale;
            params.height *= mScale;
        } else {
            // width
            if (params.width > 0) {
                params.width *= mScaleX;
            }
            // height
            if (params.height > 0) {
                params.height *= mScaleY;
            }
        }

        // font size
        if (child instanceof AppCompatTextView) {
            final float textSize = ((AppCompatTextView) child).getTextSize();
            ((AppCompatTextView) child).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * mFontScale);
        } else if (child instanceof AppCompatButton) {
            final float textSize = ((AppCompatButton) child).getTextSize();
            ((AppCompatButton) child).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * mFontScale);
        } else if (child instanceof AppCompatEditText) {
            final float textSize = ((AppCompatEditText) child).getTextSize();
            ((AppCompatEditText) child).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * mFontScale);

        }

        // margin
        params.leftMargin *= mScaleX;
        params.topMargin *= mScaleY;
        params.rightMargin *= mScaleX;
        params.bottomMargin *= mScaleY;

        // padding
        int paddingLeft = (int) (getPaddingLeft() * mScaleX);
        int paddingTop = (int) (getPaddingTop() * mScaleY);
        int paddingRight = (int) (getPaddingRight() * mScaleX);
        int paddingBottom = (int) (getPaddingBottom() * mScaleY);

        child.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }


}
