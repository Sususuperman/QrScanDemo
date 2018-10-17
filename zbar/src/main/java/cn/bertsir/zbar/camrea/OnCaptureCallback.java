package cn.bertsir.zbar.camrea;


public interface OnCaptureCallback {

    public void onCapture(boolean success, String filePath);
}