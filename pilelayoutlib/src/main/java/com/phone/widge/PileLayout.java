package com.phone.widge;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Phone on 2017/5/27.
 */

public class PileLayout extends ViewGroup {

	//相邻item间顶部的距离
	private int mItemsMarginTop;
	//相邻item间底部的距离
	private int mItemsMarginBottom;

	public PileLayout(Context context) {
		this(context, null);
	}

	public PileLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PileLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		ViewGroup.LayoutParams lp = getLayoutParams();
		lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
		setLayoutParams(lp);

		int maxChildWidth = 0;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			measureChild(child, widthMeasureSpec, heightMeasureSpec);
			maxChildWidth = Math.max(child.getMeasuredWidth(), maxChildWidth);
		}
		setMeasuredDimension(maxChildWidth, heightSize);
	}

	@Override
	protected void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			int upThreshold = calculateUpThreshold(i);
			int dowmThreshold = calculateDownThreshold(i);
		}
	}

	private int calculateUpThreshold(int position) {
		return mItemsMarginTop * position;
	}

	private int calculateDownThreshold(int position) {
		int count = getChildCount();
		return mItemsMarginBottom * (count - position);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}
}
