package com.phone.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Phone on 2017/6/1.
 */

public class TestView extends View {

	private Paint mPaint;
	private Path mPath;
	private Point mPoints[] = new Point[11];

	public TestView(Context context) {
		this(context, null);
	}

	public TestView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		for (int i = 0; i < mPoints.length; i++) {
			mPoints[i] = new Point();
		}
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.BLUE);
		mPath = new Path();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		int waveLength = 600;
		int waveHeight = 200;
		mPoints[0].x = 0;
		mPoints[0].y = 0;
		mPoints[10].x = 0;
		mPoints[10].y = getMeasuredHeight();

		mPoints[1].x = 0;
		mPoints[1].y = getMeasuredHeight() / 2 - waveLength / 2;
		mPoints[9].x = 0;
		mPoints[9].y = getMeasuredHeight() / 2 + waveLength / 2;

		mPoints[2].x = 0;
		mPoints[2].y = getMeasuredHeight() / 2 - waveLength / 6;
		mPoints[8].x = 0;
		mPoints[8].y = getMeasuredHeight() / 2 + waveLength / 6;

		mPoints[3].x = waveHeight / 3;
		mPoints[3].y = getMeasuredHeight() / 2 - waveLength / 6;
		mPoints[7].x = waveHeight / 3;
		mPoints[7].y = getMeasuredHeight() / 2 + waveLength / 6;

		mPoints[4].x = waveHeight;
		mPoints[4].y = getMeasuredHeight() / 2 - waveLength / 6;
		mPoints[6].x = waveHeight;
		mPoints[6].y = getMeasuredHeight() / 2 + waveLength / 6;

		mPoints[5].x = waveHeight;
		mPoints[5].y = getMeasuredHeight() / 2;

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPath.reset();
		mPath.moveTo(mPoints[0].x, mPoints[0].y);
		mPath.lineTo(mPoints[1].x, mPoints[1].y);
		mPath.quadTo(mPoints[2].x, mPoints[2].y, mPoints[3].x, mPoints[3].y);
		mPath.quadTo(mPoints[4].x, mPoints[4].y, mPoints[5].x, mPoints[5].y);
		mPath.quadTo(mPoints[6].x, mPoints[6].y, mPoints[7].x, mPoints[7].y);
		mPath.quadTo(mPoints[8].x, mPoints[8].y, mPoints[9].x, mPoints[9].y);
		mPath.lineTo(mPoints[10].x, mPoints[10].y);
		mPath.lineTo(mPoints[0].x, mPoints[0].y);
		mPath.close();
		canvas.drawPath(mPath, mPaint);
	}
}
