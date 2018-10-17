package cn.bertsir.qrtest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import cn.bertsir.zbar.camrea.CameraHelper;
import cn.bertsir.zbar.camrea.OnCaptureCallback;
import cn.bertsir.zbar.utils.FlashUtils;
import cn.bertsir.zbar.view.MaskSurfaceView;

import static android.util.TypedValue.COMPLEX_UNIT_MM;

public class CameraActivity extends Activity implements OnCaptureCallback {
    private static final int STORAGE_REQUEST_CODE = 000;
    private static final int CAMREA = 111;

    private MaskSurfaceView surfaceview;
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
    private boolean isLight;
    private Camera camera;

    private FlashUtils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        this.setContentView(R.layout.activity_camera);

        this.surfaceview = (MaskSurfaceView) findViewById(R.id.surface_view);
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

        result = (String) getIntent().getExtras().get("result");
        utils = new FlashUtils(this);

        //设置矩形区域大小
        this.surfaceview.setMaskSize((int) TypedValue.applyDimension(COMPLEX_UNIT_MM, 37, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(COMPLEX_UNIT_MM, 37, getResources().getDisplayMetrics()));

        //拍照
        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_capture.setEnabled(false);
                btn_ok.setEnabled(true);
                btn_recapture.setEnabled(true);
                CameraHelper.getInstance().tackPicture(CameraActivity.this);
                if (result != null && !result.equals("")) {
                    CameraHelper.getInstance().setPhotoName(result);
                }
            }
        });

        //重拍
        btn_recapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_capture.setEnabled(true);
                btn_ok.setEnabled(false);
                btn_recapture.setEnabled(false);
                imageView.setVisibility(View.GONE);
                iv_flash.setVisibility(View.VISIBLE);
                surfaceview.setVisibility(View.VISIBLE);
                deleteFile();
                CameraHelper.getInstance().startPreview();
            }
        });

        //确认 可做服务器上传操作
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Bitmap bitmap = BitmapFactory.decodeFile(filepath);

//                Bitmap bm = scaleImgSize(bitmap, (int) mm2px(33, getResources().getDisplayMetrics()), (int) mm2px(33, getResources().getDisplayMetrics()));//33mm
//                Toast.makeText(CameraActivity.this, "图片高度：" + bitmap.getHeight() + "..." + "图片宽度：" + bitmap.getWidth() + "...图片大小：" + bitmap.getByteCount() / 10240, Toast.LENGTH_LONG).show();

            }
        });

        //取消
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                deleteFile();
                CameraHelper.getInstance().setFlashlight(CameraHelper.Flashlight.OFF);
                finish();
            }
        });

        findViewById(R.id.take_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filepath != null && !filepath.equals("")) {
                    Intent intent = new Intent();
                    intent.putExtra("filepath", filepath);
                    setResult(RESULT_OK, intent);
                }
                CameraHelper.getInstance().setFlashlight(CameraHelper.Flashlight.OFF);
                finish();
            }
        });
        iv_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (CameraHelper.getInstance().getFlashStatus().equals(Camera.Parameters.FLASH_MODE_ON)) {
//                    CameraHelper.getInstance().setFlashlight(CameraHelper.Flashlight.OFF);
//                } else {
//                    CameraHelper.getInstance().setFlashlight(CameraHelper.Flashlight.ON);
//                }
                if (isLight) {
                    CameraHelper.getInstance().setFlashlight(CameraHelper.Flashlight.OFF);
                    isLight = false;
                } else {
                    CameraHelper.getInstance().setFlashlight(CameraHelper.Flashlight.TORCH);
                    isLight = true;
                }
            }
        });
    }


    /**
     * 图片尺寸图片压缩
     *
     * @param bm
     * @param newWidth
     * @param newHeight
     * @return
     */
    public Bitmap scaleImgSize(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高.
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    /**
     * 删除图片文件呢
     */
    private void deleteFile() {
        if (this.filepath == null || this.filepath.equals("")) {
            return;
        }
        File f = new File(this.filepath);
        if (f.exists()) {
            f.delete();
        }
    }

    @Override
    public void onCapture(boolean success, String filepath) {
        this.filepath = filepath;
        String message = "拍照成功";
        if (!success) {
            message = "拍照失败";
            CameraHelper.getInstance().startPreview();
            this.imageView.setVisibility(View.GONE);
            this.surfaceview.setVisibility(View.VISIBLE);
            this.iv_flash.setVisibility(View.VISIBLE);
        } else {
            this.imageView.setVisibility(View.VISIBLE);
            this.surfaceview.setVisibility(View.GONE);
            this.iv_flash.setVisibility(View.GONE);
            this.imageView.setImageBitmap(BitmapFactory.decodeFile(filepath));
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    /**
     * 监听Back键按下事件,方法2:
     * 注意:
     * 返回值表示:是否能完全处理该事件
     * 在此处返回false,所以会继续传播该事件.
     * 在具体项目中此处的返回值视情况而定.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (filepath != null && !filepath.equals("")) {
                Intent intent = new Intent();
                intent.putExtra("filepath", filepath);
                setResult(RESULT_OK, intent);
            }
            CameraHelper.getInstance().setFlashlight(CameraHelper.Flashlight.OFF);
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
