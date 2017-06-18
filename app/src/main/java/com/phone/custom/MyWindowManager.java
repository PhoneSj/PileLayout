package com.phone.custom;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.phone.widge.PileLayout;

import java.lang.reflect.Field;

/**
 * Created by Phone on 2017/5/26.
 */

public class MyWindowManager {

	private static PileLayout pileLayout;

	private static LayoutParams layoutParams;

	private static WindowManager mWindowManager;

	/**
	 * 记录系统状态栏的高度
	 */
	private static int statusBarHeight;

	/**
	 * 创建悬浮View
	 * 
	 * @param context
	 */
	public static void createFloatView(Context context) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (pileLayout == null) {
			pileLayout = new PileLayout(context);
			if (layoutParams == null) {
				layoutParams = new LayoutParams();
				layoutParams.type = LayoutParams.TYPE_PHONE;
				layoutParams.format = PixelFormat.RGBA_8888;
				layoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
				layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
				layoutParams.width = 200;
				layoutParams.height = 200;
				layoutParams.x = screenWidth;
				layoutParams.y = screenHeight / 2;
			}
			windowManager.addView(pileLayout, layoutParams);
		}
	}

	/**
	 * 移除悬浮View
	 * 
	 * @param context
	 */
	public static void removeFloatView(Context context) {
		if (pileLayout != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(pileLayout);
			pileLayout = null;
		}
	}

	/**
	 * 悬浮View是否显示了
	 * 
	 * @return
	 */
	public static boolean isWindowShowing() {
		return pileLayout != null;
	}

	private static WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}

	/**
	 * 用于获取状态栏的高度。
	 *
	 * @return 返回状态栏高度的像素值。
	 */
	private static int getStatusBarHeight(Context context) {
		if (statusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field field = c.getField("status_bar_height");
				int x = (Integer) field.get(o);
				statusBarHeight = context.getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusBarHeight;
	}

}
