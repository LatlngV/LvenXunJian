package cn.eyesw.lvenxunjian.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Environment;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.eyesw.lvenxunjian.R;
import cn.eyesw.lvenxunjian.base.BaseActivity;

public class WaterMarkerCameraActivity extends BaseActivity implements SurfaceHolder.Callback {

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private Display mDisplay;

    @BindView(R.id.camera_surface_view)
    protected SurfaceView mSurfaceView;
    @BindView(R.id.camera_tv_time)
    protected TextView mTvTime;
    @BindView(R.id.camera_tv_address)
    protected TextView mTvAddress;
    private String mAddress;
    private String mTime;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.activity_water_marker_camera;
    }

    @Override
    protected void initToolbar() {
        // 设置地址
        mAddress = getIntent().getStringExtra("address");
        mTvAddress.setText(mAddress);
        // 设置时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mTime = simpleDateFormat.format(new Date());
        mTvTime.setText(mTime);
    }

    @Override
    protected void initView() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            mDisplay = windowManager.getDefaultDisplay();
        }

        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCamera();
            if (mHolder != null) {
                setStartPreView(mCamera, mHolder);
            }
        }
    }

    /**
     * 获取相机
     */
    private Camera getCamera() {
        Camera camera;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
        }
        return camera;
    }

    /**
     * 相机预览
     */
    private void setStartPreView(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @OnClick({R.id.camera_surface_view, R.id.camera_fab_check, R.id.camera_fab_close})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.camera_surface_view:
                // 自动聚焦
                mCamera.autoFocus(null);
                break;
            case R.id.camera_fab_close:
                // 结束当前界面
                finish();
                break;
            case R.id.camera_fab_check:
                // 设置照片参数
                setPictureParameters();
                mCamera.autoFocus((b, camera) -> {
                    mCamera.takePicture(null, null, mPictureCallback);
                });
                break;
        }
    }

    /**
     * 设置参数
     */
    private void setPictureParameters() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setJpegQuality(80);
//        parameters.setPictureSize(1024, 768);


        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        int maxSize = Math.max(mDisplay.getWidth(), mDisplay.getHeight());
        if (maxSize > 0) {
            for (int i = 0, length = sizes.size(); i < length; i++) {
                int width = sizes.get(i).width;
                int height = sizes.get(i).height;
                if (maxSize <= Math.max(width, height)) {
                    parameters.setPictureSize(width, height);
                    break;
                }
            }
        }

        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        if (maxSize > 0) {
            for (int i = 0, length = previewSizes.size(); i < length; i++) {
                int width = previewSizes.get(i).width;
                int height = previewSizes.get(i).height;
                if (maxSize <= Math.max(width, height)) {
                    parameters.setPreviewSize(width, height);
                    break;
                }
            }
        }

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(parameters);
    }

    private Camera.PictureCallback mPictureCallback = (bytes, camera) -> {
        File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Matrix matrix = new Matrix();
            matrix.setRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            // 添加水印
            bitmap = waterMarkerBitmap(bitmap);
            String pictureName = System.currentTimeMillis() + ".jpg";
            File picture = new File(pictures, pictureName);
            FileOutputStream fos = new FileOutputStream(picture);
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            }
            fos.close();

            Intent intent = new Intent();
            intent.putExtra("filePath", picture.getAbsolutePath());
            setResult(2, intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    /**
     * 添加水印
     *
     * @param bitmap 图片
     * @return 返回带水印的图片
     */
    private Bitmap waterMarkerBitmap(Bitmap bitmap) {
        Paint paint = new Paint();
        paint.setTextSize(100);
        paint.setColor(Color.parseColor("#fcfcfc"));
        paint.setStyle(Paint.Style.FILL);
        Canvas canvas = new Canvas(bitmap);
        // 画文字(时间)
        canvas.drawText(mTime, 50, 100, paint);
        // 画文字(地点)
        canvas.drawText(mAddress, 50, 500, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // 预览
        setStartPreView(mCamera, mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // 停止预览
        mCamera.stopPreview();
        // 重新预览
        setStartPreView(mCamera, mHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // 释放相机资源
        releaseCamera();
    }

}
