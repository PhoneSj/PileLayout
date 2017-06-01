//package com.phone.widge;
//
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.LinearGradient;
//import android.graphics.Paint;
//import android.graphics.Path;
//import android.graphics.Point;
//import android.graphics.Shader;
//import android.util.Log;
//
///**
// * Created by Phone on 2017/6/1.
// */
//
//public class EffectControl {
//
//	private PileLayout pileLayout;
//	private Paint mPaint;
//	private Paint mBezierPaint;
//	private Path mPath;
//	private int mColor = Color.BLUE;
//	private Shader mShader;
//	private Point mPoints[] = new Point[11];
//	//贝塞尔曲线波长
//	private int waveLength;
//	//贝塞尔曲线波峰-波谷间的距离
//	private int waveHeight = 50;
//
//	public EffectControl(PileLayout pileLayout) {
//		this.pileLayout = pileLayout;
//		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		mPaint.setStyle(Paint.Style.FILL);
//		mBezierPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		mPaint.setStyle(Paint.Style.FILL);
//		mPath = new Path();
//		for (int i = 0; i < mPoints.length; i++) {
//			mPoints[i] = new Point();
//		}
//
//		mShader = new LinearGradient(0, 0, getMeasuredWidth(), 0, 0x0000ff00, 0xff00ff00, Shader.TileMode.CLAMP);
//		mPaint.setShader(mShader);
//		mBezierPaint.setColor(mColor);
//		waveLength = 600;
//	}
//
//	public void draw(Canvas canvas) {
//
//	}
//
//	private void calculatePoints() {
//		if (mMode == PileLayout.Mode.LEFT) {
//			waveHeight = (int) Math.abs(mDistanceX * 1.0f);
//			Log.i("phoneTest", "mDistanceX:" + mDistanceX);
//			Log.i("phoneTest", "waveHeight:" + waveHeight);
//			mPoints[0].x = 0;
//			mPoints[0].y = 0;
//			mPoints[10].x = 0;
//			mPoints[10].y = getMeasuredHeight();
//
//			mPoints[1].x = 0;
//			mPoints[1].y = getMeasuredHeight() / 2 - waveLength / 2;
//			mPoints[9].x = 0;
//			mPoints[9].y = getMeasuredHeight() / 2 + waveLength / 2;
//
//			mPoints[2].x = 0;
//			mPoints[2].y = getMeasuredHeight() / 2 - waveLength / 6;
//			mPoints[8].x = 0;
//			mPoints[8].y = getMeasuredHeight() / 2 + waveLength / 6;
//
//			mPoints[3].x = waveHeight / 3;
//			mPoints[3].y = getMeasuredHeight() / 2 - waveLength / 6;
//			mPoints[7].x = waveHeight / 3;
//			mPoints[7].y = getMeasuredHeight() / 2 + waveLength / 6;
//
//			mPoints[4].x = waveHeight;
//			mPoints[4].y = getMeasuredHeight() / 2 - waveLength / 6;
//			mPoints[6].x = waveHeight;
//			mPoints[6].y = getMeasuredHeight() / 2 + waveLength / 6;
//
//			mPoints[5].x = waveHeight;
//			mPoints[5].y = getMeasuredHeight() / 2;
//		} else {
//			//TODO
//		}
//	}
//}
