package com.phone.widge;

import android.view.View;

/**
 * Created by Phone on 2017/5/27.
 */

public class WrapItem {

	//上阈值：View的y坐标不能小于该值
	private int mUpThreshold;
	//下阈值：View的y坐标不能大于该值
	private int mDownThreshold;
	private View view;

	public WrapItem(int mUpThreshold, int mDownThreshold, View view) {
		this.mUpThreshold = mUpThreshold;
		this.mDownThreshold = mDownThreshold;
		this.view = view;
	}

	public int getmUpThreshold() {
		return mUpThreshold;
	}

	public int getmDownThreshold() {
		return mDownThreshold;
	}

	public View getView() {
		return view;
	}
}
