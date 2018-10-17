/* 
 * Copyright (c) 2015-2020 Founder Ltd. All Rights Reserved. 
 * 
 *zhx for  org
 * 
 * 
 */

package cn.bertsir.qrtest.take.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import cn.bertsir.qrtest.take.util.DisplayUtil;

import static android.util.TypedValue.COMPLEX_UNIT_MM;


/**
 * 希望有一天可以开源出来  org.zhx
 *
 * @author zhx
 * @version 1.0, 2015-11-15 下午7:11:49
 */
public class OverlayerView extends ImageView {
    private static final String TAG = OverlayerView.class.getSimpleName();
    private Paint mLinePaint;
    private Paint mAreaPaint;
    private Rect mCenterRect = null;
    private Context mContext;
    private Paint paint;

    public OverlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        initPaint();
        mContext = context;
        Point p = DisplayUtil.getScreenMetrics(mContext);
        widthScreen = p.x;
        heightScreen = p.y;
    }

    private void initPaint() {
        // 绘制中间透明区域矩形边界的Paint
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.TRANSPARENT);
        mLinePaint.setStyle(Style.STROKE);
        mLinePaint.setStrokeWidth(3f);
        mLinePaint.setAlpha(0);

        // 绘制四周阴影区域
        mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAreaPaint.setColor(Color.BLACK);
        mAreaPaint.setStyle(Style.FILL);
        mAreaPaint.setAlpha(170);//取值范围为0~255，数值越小越透明
        paint = new Paint();

    }

    public void setCenterRect(Rect r) {
        Log.i(TAG, "setCenterRect...");
        this.mCenterRect = r;
        postInvalidate();
    }

    public void clearCenterRect(Rect r) {
        this.mCenterRect = null;
    }

    int widthScreen, heightScreen;
    int maskheight = (int) TypedValue.applyDimension(COMPLEX_UNIT_MM, 37, getResources().getDisplayMetrics());
    int maskwidth = (int) TypedValue.applyDimension(COMPLEX_UNIT_MM, 37, getResources().getDisplayMetrics());

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onDraw...");
        if (mCenterRect == null)
            return;

        if (maskheight == 0 && maskwidth == 0) {
            return;
        }
        if (maskheight == heightScreen || maskwidth == widthScreen) {
            return;
        }

        if ((heightScreen > widthScreen && maskheight < maskwidth) || (heightScreen < widthScreen && maskheight > maskwidth)) {
            int temp = maskheight;
            maskheight = maskwidth;
            maskwidth = temp;
        }

        //height：屏幕高度
        //width：屏幕宽度
        //maskHeight：中间透明区域高度
        //maskWidth：中间透明区域宽度
        int h = Math.abs((heightScreen - maskheight) / 2);//顶部阴影高度
        int w = Math.abs((widthScreen - maskwidth) / 2);//右侧阴影宽度

        //上阴影
        canvas.drawRect(0, 0, widthScreen, h, this.mAreaPaint);
        //右阴影
        canvas.drawRect(widthScreen - w, h, widthScreen, heightScreen - h, this.mAreaPaint);
        //下阴影
        canvas.drawRect(0, heightScreen - h, widthScreen, heightScreen, this.mAreaPaint);
        //左阴影
        canvas.drawRect(0, h, w, h + maskheight + 1f, this.mAreaPaint);
        //中透明
        canvas.drawRect(w, h, w + maskwidth, h + maskheight, this.mLinePaint);
        canvas.save();//保存上面的上下左右中
        //中-顶部-字体
        canvas.rotate(90, widthScreen - w / 2, heightScreen / 2);//把画布旋转旋转90度
//            canvas.drawText("请扫描二维码图像", width - w / 2, height / 2, topTextPaint);
        canvas.restore();//把画布恢复到上次保存的位置，防止本次旋转影响下面的操作
        canvas.save();//保存中顶部字体
        //中-底部-字体
        canvas.rotate(90, w / 2, heightScreen / 2);//旋转90度
//            canvas.drawText("请保持光线充足，背景干净", w / 2, height / 2, bottomTextPaint);
        canvas.restore();//把画布恢复到上次保存的位置，防止本次旋转影响下面的操作
        canvas.save();//保存中底部字体

//        // 绘制四周阴影区域
//        canvas.drawRect(0, 0, widthScreen, mCenterRect.top - 2, mAreaPaint);
//        canvas.drawRect(0, mCenterRect.bottom + 2, widthScreen, heightScreen,
//                mAreaPaint);
//        canvas.drawRect(0, mCenterRect.top - 2, mCenterRect.left - 2,
//                mCenterRect.bottom + 2, mAreaPaint);
//        canvas.drawRect(mCenterRect.right + 2, mCenterRect.top - 2,
//                widthScreen, mCenterRect.bottom + 2, mAreaPaint);
//
//        paint.setColor(Color.WHITE);
//        paint.setAlpha(150);
//
//        canvas.drawRect(mCenterRect.left - 2, mCenterRect.bottom,
//                mCenterRect.left + 50, mCenterRect.bottom + 2, paint);// 左下 底部
//
//        canvas.drawRect(mCenterRect.left - 2, mCenterRect.bottom - 50,
//                mCenterRect.left, mCenterRect.bottom, paint);// 左下 左侧
//
//        canvas.drawRect(mCenterRect.right - 50, mCenterRect.bottom,
//                mCenterRect.right + 2, mCenterRect.bottom + 2, paint);// 右下 右侧
//        canvas.drawRect(mCenterRect.right, mCenterRect.bottom - 50,
//                mCenterRect.right + 2, mCenterRect.bottom, paint);// 右下 底部
//
//        canvas.drawRect(mCenterRect.left - 2, mCenterRect.top - 2,
//                mCenterRect.left + 50, mCenterRect.top, paint);// 左上 顶部
//        canvas.drawRect(mCenterRect.left - 2, mCenterRect.top,
//                mCenterRect.left, mCenterRect.top + 50, paint);// 左上 侧边
//        canvas.drawRect(mCenterRect.right - 50, mCenterRect.top - 2,
//                mCenterRect.right + 2, mCenterRect.top, paint);// 右上 顶部
//        canvas.drawRect(mCenterRect.right, mCenterRect.top,
//                mCenterRect.right + 2, mCenterRect.top + 50, paint);// 右上 右侧
//
//        // 绘制目标透明区域
//        canvas.drawRect(mCenterRect, mLinePaint);
        super.onDraw(canvas);
    }

    public Rect getmCenterRect() {
        return mCenterRect;
    }

    public void setmCenterRect(Rect mCenterRect) {
        this.mCenterRect = mCenterRect;
    }
}

