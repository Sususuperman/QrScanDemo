package cn.bertsir.qrtest.take.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;


import java.io.IOException;
import java.util.List;

import cn.bertsir.qrtest.R;
import cn.bertsir.qrtest.take.util.DisplayUtil;

/**
 * 希望有天可以对新手有帮助
 * 作者：zhx
 * 时间：2018\7\24 0024 12:05
 * 类名：Cameramanager
 * 邮箱：194093798@qq.com
 */
public class Cameramanager implements CameraInterface {
    private Context mContext;
    /**
     * 是否预览中
     */
    private boolean isPreview = false;
    private Camera mCamera;
    /**
     * 预览组件
     */
    private SurfaceHolder displyHolder;
    /**
     * 当前是否是前置摄像头
     */
    private boolean isFrontCamera = false;
    private Point displayPx;
    private String[] flashMedols = {Camera.Parameters.FLASH_MODE_AUTO, Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_OFF, Camera.Parameters.FLASH_MODE_TORCH};
    private int[] modelResId = {R.drawable.ic_camera_top_bar_flash_auto_normal, R.drawable.ic_camera_top_bar_flash_on_normal, R.drawable.ic_camera_top_bar_flash_off_normal, R.drawable.ic_camera_top_bar_flash_torch_normal};
    int modelIndex = 0;
    private ImageDataType currentType = ImageDataType.BYTES;

    public Cameramanager(Context mContext) {
        this.mContext = mContext;
        displayPx = DisplayUtil.getScreenMetrics(mContext);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void openCamera() {
        if (!isFrontCamera) {
            mCamera = Camera.open();
        } else {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                {
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        mCamera = Camera.open(i);
                        isFrontCamera = true;
                    }
                }
            }
        }
    }

    @Override
    public void stopPreview() {
        if (mCamera != null) {
            if (isPreview) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
                isPreview = false;
            }

        }
    }

    @Override
    public void initCamera() {
        if (mCamera != null && !isPreview) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                // 设置闪光灯为自动 前置摄像头时 不能设置
                if (!isFrontCamera) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                }

                resetCameraSize(parameters);
                // 设置图片格式
                parameters.setPictureFormat(ImageFormat.JPEG);
                // 设置JPG照片的质量
                parameters.set("jpeg-quality", 100);
                // 通过SurfaceView显示取景画面
                mCamera.setPreviewDisplay(displyHolder);
                // 开始预览
                mCamera.startPreview();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            isPreview = true;
        }
    }

    /**
     * 旋转相机和设置预览大小
     *
     * @param parameters
     */
    public void resetCameraSize(Camera.Parameters parameters) {
        if (mContext.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            mCamera.setDisplayOrientation(90);
        } else {
            mCamera.setDisplayOrientation(0);
        }
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        if (sizeList.size() > 0) {
            Camera.Size cameraSize = sizeList.get(0);
            // 设置预览图片大小 为设备长宽
            parameters.setPreviewSize(cameraSize.width, cameraSize.height);
        }
        sizeList = parameters.getSupportedPictureSizes();
        if (sizeList.size() > 0) {
            Camera.Size cameraSize = sizeList.get(0);
            for (Camera.Size size : sizeList) {

                if (size.width * size.height == displayPx.x * displayPx.y) {
                    cameraSize = size;
                    break;
                }
            }
            // 设置图片大小 为设备长宽
            parameters.setPictureSize(cameraSize.width, cameraSize.height);
        }

    }

    @Override
    public void releaseCamera() {
        if (mCamera != null) {
            if (isPreview) {
                mCamera.stopPreview();
            }
            mCamera.release();
            mCamera = null;
        }
        isPreview = false;
    }

    @Override
    public void switchCamera() {
        isFrontCamera = !isFrontCamera;
        releaseCamera();
        openCamera();
        initCamera();
    }

    public void setDisplyHolder(SurfaceHolder displyHolder) {
        this.displyHolder = displyHolder;
    }

    public void takePicture(final ImageInterface callback) {
        this.mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean flag, Camera camera) {
                if (mCamera != null) {
                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] bytes, Camera camera) {
                            switch (currentType) {
                                case PATH:
                                    break;
                                case BYTES:
                                    callback.ImageCallback(bytes);
                                    break;
                                case BITMAP:
                                    break;
                                default:
                                    callback.ImageCallback(bytes);
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }

    public int switchFlashMode() {
        int rec = 0;
        modelIndex++;
        if (modelIndex >= flashMedols.length) {
            modelIndex = 0;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> flashmodels = parameters.getSupportedFlashModes();
        if (flashmodels.contains(flashMedols[modelIndex])) {
            parameters.setFlashMode(flashMedols[modelIndex]);
            rec = modelResId[modelIndex];
        }
        mCamera.setParameters(parameters);
        return rec;
    }

    public void restartPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.startPreview();
            isPreview = true;
        } else {
            openCamera();
            initCamera();
        }
    }

    public boolean isFrontCamera() {
        return isFrontCamera;
    }
}
