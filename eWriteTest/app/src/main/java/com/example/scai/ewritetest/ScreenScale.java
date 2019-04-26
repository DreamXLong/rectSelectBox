package com.example.scai.ewritetest;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.nfc.Tag;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.TextView;

/***
 * 对布局进行屏幕适配操作
 */
public class ScreenScale {

    /**
     * 参考屏幕的宽度
     */
    private final static float BASE_SCREEN_WIDTH = 1024;
    /**
     * 参考屏幕的高度
     */
    private final static float BASE_SCREEN_HEIGHT = 768;

    public static Application application;

    public static void init(Application a) {
        if (application == null) {
            application = a;
        }
    }

    /**
     * 对一个view或者布局进行适配操作（按照宽的比例）
     *
     * @param view
     */
    public static void scaleView(View view, String Tag) {
        List<View> list = getAllChildViews(view);
        for (View temp : list) {
            //对列表中的所有view进行缩放操作
            scaleViewSize(temp, 1);
        }
    }

    /**
     * 对指定view进行缩放操作
     *
     * @param view
     * @param type 0  宽高都按照设计图的比例分别进行缩放        1  宽高都按照和设计图的宽的比例进行缩放
     */
    private static void scaleViewSize(View view, int type) {
        if (null != view) {
            int paddingLeft = getScaleValueByWidth(view.getPaddingLeft());
            int paddingRight = getScaleValueByWidth(view.getPaddingRight());

            int paddingTop = 0, paddingBottom = 0;
            if (type == 0) {
                paddingTop = getScaleValueByHeight(view.getPaddingTop());
                paddingBottom = getScaleValueByHeight(view.getPaddingBottom());
            } else {
                paddingTop = getScaleValueByWidth(view.getPaddingTop());
                paddingBottom = getScaleValueByWidth(view.getPaddingBottom());
            }

            view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

            LayoutParams layoutParams = view.getLayoutParams();

            if (null != layoutParams) {
                if (layoutParams.width > 0) {
                    layoutParams.width = getScaleValueByWidth(layoutParams.width);
                }
                if (layoutParams.height > 0) {
                    if (type == 0) {
                        layoutParams.height = getScaleValueByHeight(layoutParams.height);
                    } else {
                        layoutParams.height = getScaleValueByWidth(layoutParams.height);
                    }
                }

                if (layoutParams instanceof MarginLayoutParams) {
                    MarginLayoutParams marginLayoutParams = (MarginLayoutParams) layoutParams;
                    int leftMargin = getScaleValueByWidth(marginLayoutParams.leftMargin);
                    int rightMargin = getScaleValueByWidth(marginLayoutParams.rightMargin);
                    int topMargin = 0, bottomMargin = 0;
                    if (type == 0) {
                        topMargin = getScaleValueByHeight(marginLayoutParams.topMargin);
                        bottomMargin = getScaleValueByHeight(marginLayoutParams.bottomMargin);
                    } else {
                        topMargin = getScaleValueByWidth(marginLayoutParams.topMargin);
                        bottomMargin = getScaleValueByWidth(marginLayoutParams.bottomMargin);
                    }

                    marginLayoutParams.topMargin = topMargin;
                    marginLayoutParams.leftMargin = leftMargin;
                    marginLayoutParams.bottomMargin = bottomMargin;
                    marginLayoutParams.rightMargin = rightMargin;
                }
            }

            if (view instanceof TextView) {
                //如果对象view为textview，那么也要对其文字的大小进行缩放
                float size = ((TextView) view).getTextSize();
                size = size * getWidthScale();
                ((TextView) view).setTextSize(0, size);
            }
            view.setLayoutParams(layoutParams);
        }
    }


    /**
     * 得到一个view中所有的view列表
     *
     * @param view
     * @return
     */
    private static List<View> getAllChildViews(View view) {
        List<View> allchildren = new ArrayList<View>();
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewchild = vp.getChildAt(i);
                allchildren.add(viewchild);
                //再次 调用本身（递归）  
                allchildren.addAll(getAllChildViews(viewchild));
            }
        }
        return allchildren;
    }

    /**
     * 得到宽度的缩放比例
     *
     * @return
     */
    public static float getWidthScale() {
        return getScreenWidth() / BASE_SCREEN_WIDTH;
    }

    /**
     * 得到高度的缩放比例
     *
     * @return
     */
    public static float getHeightScale() {
        return getScreenHeight() / BASE_SCREEN_HEIGHT;
    }

    /**
     * 得到宽度的缩放数值
     *
     * @param number
     * @return
     */
    private static int getScaleValueByWidth(int number) {
        float temp = getWidthScale();
        int result = (int) (number * temp);
        return result;
    }

    /**
     * 得到高度的缩放数值
     *
     * @param number
     * @return
     */
    private static int getScaleValueByHeight(int number) {
        float temp = getHeightScale();
        int result = (int) (number * temp);
        return result;
    }

    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        return width;
    }


    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;       // 屏幕高度（像素）
        return height;

    }

    public static void getAndroiodScreenProperty() {
        WindowManager wm = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）
        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        int screenHeight = (int) (height / density);// 屏幕高度(dp)

        Log.e("h_bl", "屏幕宽度（像素）：" + width);
        Log.e("h_bl", "屏幕高度（像素）：" + height);
        Log.e("h_bl", "屏幕密度（0.75 / 1.0 / 1.5）：" + density);
        Log.e("h_bl", "屏幕密度dpi（120 / 160 / 240）：" + densityDpi);
        Log.e("h_bl", "屏幕宽度（dp）：" + screenWidth);
        Log.e("h_bl", "屏幕高度（dp）：" + screenHeight);
    }

}
