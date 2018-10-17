package cn.bertsir.qrtest.take.camera;

/**
 * 希望有天可以对新手有帮助
 * 作者：zhx
 * 时间：2018\7\24 0024 12:07
 * 类名：CameraInterface
 * 邮箱：194093798@qq.com
 */
public interface CameraInterface {
    /**
     * 打开相机
     */
    public void openCamera();

    /**
     * 暂停预览
     */
    public void stopPreview();

    /**
     * 初始化相机
     */
    public void initCamera();

    /**
     * 释放相机
     */
    public void releaseCamera();

    /**
     * 切换相机
     */
    public void  switchCamera();
}
