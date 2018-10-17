package cn.bertsir.qrtest.take.ui;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import cn.bertsir.qrtest.R;
import cn.bertsir.qrtest.take.camera.Cameramanager;
import cn.bertsir.qrtest.take.camera.ImageInterface;
import cn.bertsir.qrtest.take.util.DisplayUtil;
import cn.bertsir.qrtest.take.util.ImageUtil;
import cn.bertsir.qrtest.take.widget.OverlayerView;
import cn.bertsir.zbar.view.MaskSurfaceView;

import static android.util.TypedValue.COMPLEX_UNIT_MM;


/**
 * 希望有一天可以开源出来 org.zhx
 *
 * @author zhx
 * @version 1.0, 2015-11-15 下午5:22:17
 */
public class ZCameraBaseAcitivy extends Activity implements ImageInterface<byte[]>,
        Callback, OnClickListener {
    private static final String TAG = ZCameraBaseAcitivy.class.getSimpleName();

    public static BitmapFactory.Options opt;

    static {
        // 缩小原图片大小
        opt = new BitmapFactory.Options();
        opt.inSampleSize = 2;
    }

    private SurfaceView mPreView;
    private SurfaceHolder mHolder;
    private ImageView tpImg, showImg;
    private Button saveBtn;
    // 取景框
    private OverlayerView mLayer;
    private Rect rect;
    /**
     * 切换摄像头
     */
    private ImageView swImg;
    private ImageView flashModelImg;
    private Cameramanager mCameramanager;

    private ImageView imageView, iv_flash;
    //	拍照
    private Button btn_capture;
    //	重拍
    private Button btn_recapture;
    //	取消
    private Button btn_cancel;
    //	确认
    private Button btn_ok;

    //	拍照后得到的保存的文件路径
    private String filepath;

    private String result;//扫描结果

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.zcamera_base_layout);

        this.imageView = (ImageView) findViewById(R.id.image_view);
        this.iv_flash = (ImageView) findViewById(R.id.iv_flash);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.height = (int) TypedValue.applyDimension(COMPLEX_UNIT_MM, 37, getResources().getDisplayMetrics());
        params.width = (int) TypedValue.applyDimension(COMPLEX_UNIT_MM, 37, getResources().getDisplayMetrics());
        imageView.setLayoutParams(params);
        btn_capture = (Button) findViewById(R.id.btn_capture);
        btn_recapture = (Button) findViewById(R.id.btn_recapture);
        btn_ok = (Button) findViewById(R.id.btn_ok);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_capture.setOnClickListener(this);
        btn_recapture.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        iv_flash.setOnClickListener(this);
        findViewById(R.id.take_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filepath != null && !filepath.equals("")) {
                    Intent intent = new Intent();
                    intent.putExtra("filepath", filepath);
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        });

        result = (String) getIntent().getExtras().get("result");

        mPreView = (SurfaceView) findViewById(R.id.z_base_camera_preview);
        tpImg = (ImageView) findViewById(R.id.z_take_pictrue_img);
        saveBtn = (Button) findViewById(R.id.z_base_camera_save);
        showImg = (ImageView) findViewById(R.id.z_base_camera_showImg);
        mLayer = (OverlayerView) findViewById(R.id.z_base_camera_over_img);
        swImg = (ImageView) findViewById(R.id.btn_switch_camera);
        swImg.setOnClickListener(this);
        flashModelImg = (ImageView) findViewById(R.id.btn_flash_mode);
        flashModelImg.setOnClickListener(this);
        // 设置取景框的 magin 这里最好 将 这些从dp 转化为px; 距 左 、上 、右、下的 距离 单位是dp
        rect = DisplayUtil.createCenterScreenRect(this, new Rect(100, 100, 100,
                100));
        mLayer.setmCenterRect(rect);
        saveBtn.setOnClickListener(this);
        showImg.setOnClickListener(this);
        tpImg.setOnClickListener(this);
        mHolder = mPreView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCameramanager = new Cameramanager(this);
        mCameramanager.setDisplyHolder(mHolder);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.e(TAG, "surfaceCreated");
        mCameramanager.openCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mCameramanager.restartPreview();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mCameramanager.stopPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        Log.e(TAG, "surfaceChanged");
        mCameramanager.initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.e(TAG, "surfaceDestroyed");
        // 当holder被回收时 释放硬件
        mCameramanager.releaseCamera();

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.z_take_pictrue_img:
                // 手动对焦
                mCameramanager.takePicture(ZCameraBaseAcitivy.this);
                break;
            case R.id.btn_switch_camera:
                mCameramanager.switchCamera();
                break;
            case R.id.btn_flash_mode:
                flashModelImg.setImageResource(mCameramanager.switchFlashMode());
                break;

            case R.id.btn_capture:
                // 手动对焦
                mCameramanager.takePicture(ZCameraBaseAcitivy.this);
                break;
            case R.id.btn_recapture:
                break;
            case R.id.btn_cancel:
                break;
            case R.id.iv_flash:
                flashModelImg.setImageResource(mCameramanager.switchFlashMode());
                break;
            default:
                break;
        }
    }

    @Override
    public void ImageCallback(byte[] data) {
        // TODO Auto-generated method stub
        // 拍照回掉回来的 图片数据。
        Bitmap bitmap = BitmapFactory
                .decodeByteArray(data, 0, data.length, opt);
        Bitmap bm = null;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Matrix matrix = new Matrix();
            matrix.setRotate(90, 0.1f, 0.1f);
            bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, false);
            if (mCameramanager.isFrontCamera()) {
                //前置摄像头旋转图片270度。
                matrix.setRotate(270);
                bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            }
        } else {
            bm = bitmap;
        }

        if (rect != null) {
            bitmap = ImageUtil.getRectBmp(rect, bm, DisplayUtil.getScreenMetrics(this));
        }
        ImageUtil.recycleBitmap(bm);
        showImg.setImageBitmap(bitmap);
        imageView.setImageBitmap(bitmap);
        mCameramanager.restartPreview();
    }
}
