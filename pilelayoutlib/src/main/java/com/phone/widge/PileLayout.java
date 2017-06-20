package com.phone.widge;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Phone on 2017/5/27.
 */

public class PileLayout extends ViewGroup {

	private static final int INVALID_POSITION = -1;
	private static final int DEFAULT_ITEM_MARGIN_TOP = 50;
	private static final int DEFAULT_ITEM_MARGIN_BOTTOM = 50;
	private static final int DEFAULT_HORIZONTAL_THRESHOLD = 200;
	private final static long LONG_CLICK_LIMIT = 300;
	//轻微滑动阈值
	private int mTouchSlop;
	//相邻item间顶部的距离
	private int mItemsMarginTop;
	//相邻item间底部的距离
	private int mItemsMarginBottom;
	//初始化时子View的偏移量
	private int mHorizontalThreshold;
	//当前的滑动状态
	private State mState = State.CLOSE;
	//上一次的滑动状态
	private State oldState = State.CLOSE;
	//将要进入的状态
	private State newState = State.CLOSE;

	private ListAdapter adapter;
	//记录所有子View位置、阈值的集合
	private Map<View, ItemDesc> descMap = new HashMap<>();
	//子View的高度
	private int itemHeight;
	//down事件的x、y坐标
	private float mDownX;
	private float mDownY;
	//上次事件的x、y坐标
	private float mLastX;
	private float mLastY;
	//判定的滑动方向
	private Direction mDirection = Direction.NONE;
	//当前x方向的偏移总量
	private float mDistanceX;
	//手指松开后的动画
	private ValueAnimator mItemAnimator;
	//背景贝塞尔曲线的画笔
	private Paint mBezierPaint;
	private Paint testPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	//背景绘制路径
	private Path mPath;
	//背景颜色渐变
	private Shader mShader;
	//别塞尔曲线的控制点集合
	private PointDesc mPoints[] = new PointDesc[11];
	//贝塞尔曲线波长
	private int waveLength;
	//贝塞尔曲线波峰-波谷间的距离
	private int waveHeight = 50;
	//最近一次down事件触发的时间
	private long mLastDownTime;
	//是否触发了长按响应
	private boolean isLongClick = false;
	//是否触发了点击事件
	private boolean isClick = false;

	private DataSetObserver dataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			attachChildViews();
		}
	};

	/**
	 * 当前判定的滑动方向
	 */
	enum Direction {
		HORIZONTAL, VERTICAL, NONE
	}

	/**
	 * 当前侧滑的状态
	 */
	enum State {
		OPEN, CLOSE, MIDDLE
	}

	public PileLayout(Context context) {
		this(context, null);
	}

	public PileLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PileLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		//获得系统的轻微滑动阈值
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		//获取自定义属性及其值
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PileLayout);
		mItemsMarginTop = (int) array.getDimension(R.styleable.PileLayout_itemMarginTop, DEFAULT_ITEM_MARGIN_TOP);
		mItemsMarginBottom = (int) array.getDimension(R.styleable.PileLayout_itemMarginBottom,
				DEFAULT_ITEM_MARGIN_BOTTOM);
		mHorizontalThreshold = (int) array.getDimension(R.styleable.PileLayout_horizontalThreshold,
				DEFAULT_HORIZONTAL_THRESHOLD);
		initDrawParams();
	}

	/**
	 * 初始化背景绘制的相关参数
	 */
	private void initDrawParams() {
		mBezierPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPath = new Path();
		for (int i = 0; i < mPoints.length; i++) {
			mPoints[i] = new PointDesc();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		waveLength = getMeasuredHeight();
		mShader = new LinearGradient(0, 0, getMeasuredWidth(), 0, Color.TRANSPARENT, Color.GREEN,
				Shader.TileMode.CLAMP);
		mBezierPaint.setStyle(Paint.Style.FILL);
		mBezierPaint.setShader(mShader);
		calculatePoints(0, 0);
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		int maxChildWidth = 0;
		int totalHeight = 0;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			measureChild(child, widthMeasureSpec, heightMeasureSpec);
			maxChildWidth = Math.max(child.getMeasuredWidth(), maxChildWidth);
			totalHeight += child.getMeasuredHeight() + mItemsMarginTop + mItemsMarginBottom;
			itemHeight = Math.max(itemHeight, child.getMeasuredHeight());
		}
		int width = widthMode == MeasureSpec.EXACTLY ? widthSize : maxChildWidth;
		int height = heightMode == MeasureSpec.EXACTLY ? heightSize : totalHeight;
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			ItemDesc desc = descMap.get(child);
			int upThreshold = calculateUpThreshold(i);
			int downThreshold = calculateDownThreshold(i);
			desc.setUpThreshold(upThreshold);
			desc.setDownThreshold(downThreshold);
			child.setZ(0);//默认z坐标为0
		}

		//对y坐标达到下边界的子View调整z坐标
		int z = 0;
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			ItemDesc desc = descMap.get(child);
			if (desc.getCurY() == desc.getDownThreshold()) {
				z--;
				child.setZ(z);
			}
		}

		//对y左边达到上边界的子View调整z坐标
		z = 0;
		for (int i = count - 1; i >= 0; i--) {
			final View child = getChildAt(i);
			ItemDesc desc = descMap.get(child);
			if (desc.getCurY() == desc.getUpThreshold()) {
				z--;
				child.setZ(z);
			}
		}

		//对每个子View布局，y坐标在上下阈值之间
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			ItemDesc desc = descMap.get(child);
			int childLeft;
			int childTop = desc.getCurY();
			childLeft = desc.getCurX();
			child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(),
					childTop + child.getMeasuredHeight());
		}
	}

	/**
	 * 计算指定位置子View的上边界阈值
	 *
	 * @param position
	 * @return
	 */
	private int calculateUpThreshold(int position) {
		int paddingTop = getPaddingTop();
		return paddingTop + mItemsMarginTop * position;
	}

	/**
	 * 计算指定位置子View的下边界阈值
	 *
	 * @param position
	 * @return
	 */
	private int calculateDownThreshold(int position) {
		int count = adapter.getCount();
		int height = getMeasuredHeight();
		int paddingBottom = getPaddingBottom();
		return height - paddingBottom - mItemsMarginBottom * (count - position - 1) - itemHeight;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				mDownX = event.getX();
				mDownY = event.getY();
				mLastX = event.getX();
				mLastY = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				float dx = event.getX() - mDownX;
				float dy = event.getY() - mDownY;
				//水平、竖直都需要拦截
				if (Math.abs(dx) > mTouchSlop || Math.abs(dy) > mTouchSlop) {
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
				break;
		}
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				mDirection = Direction.NONE;
				mDownX = event.getX();
				mDownY = event.getY();
				mLastX = event.getX();
				mLastY = event.getY();
				mLastDownTime = System.currentTimeMillis();
				postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mDirection == Direction.NONE) {
							//没有触发点击事件，才能触发长按事件
							if (!isClick && mOnItemLongClickListener != null) {
								int position = judgePosition();
								mOnItemLongClickListener.onItemLongClick(position);
								isLongClick = true;
							}
						}
					}
				}, LONG_CLICK_LIMIT);
				isClick = false;
				break;
			case MotionEvent.ACTION_MOVE:
				float dx = event.getX() - mDownX;
				float dy = event.getY() - mDownY;
				if (mDirection == Direction.NONE && (Math.abs(dx) > mTouchSlop || Math.abs(dy) > mTouchSlop)) {
					if (Math.abs(dx) > Math.abs(dy)) {
						mDirection = Direction.HORIZONTAL;
					} else {
						mDirection = Direction.VERTICAL;
					}
					oldState = mState;
					mState = State.MIDDLE;
				}

				if (mDirection == Direction.HORIZONTAL) {
					//响应水平方向滑动，这里记录累加值
					mDistanceX = event.getX() - mDownX;
					mDistanceX = checkDiffX((int) mDistanceX);
					offsetXForChildren(event);
				} else if (mDirection == Direction.VERTICAL) {
					//响应竖直方向滑动，这里记录当前的偏移值
					float diffY = event.getY() - mLastY;
					offsetYForChildren((int) diffY);
				}

				mLastX = event.getX();
				mLastY = event.getY();

				invalidate();
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				//只有横向滑动才需要执行释放操作
				if (mDirection == Direction.HORIZONTAL) {
					onRelease();
				}
				if (mDirection == Direction.NONE) {
					//没有触发长按事件才能触发点击事件
					if (!isLongClick && mOnItemClickListener != null) {
						int position = judgePosition();
						mOnItemClickListener.onItemclick(position);
						isClick = true;
					}
					isLongClick = false;
				}
				mLastX = 0;
				mLastY = 0;
				mLastDownTime = 0;
				break;
		}
		return true;
	}

	/**
	 * 判断点击的是哪个子View
	 * 
	 * @return
	 */
	private int judgePosition() {
		int count = getChildCount();
		View[] sortChildren = new View[count];
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			sortChildren[i] = child;
		}
		//按Z坐标排序，值大的在前面
		View temp;
		for (int i = 0; i < count; i++) {
			for (int j = i; j < count - 1; j++) {
				if (sortChildren[j].getZ() < sortChildren[j + 1].getZ()) {
					temp = sortChildren[j];
					sortChildren[j] = sortChildren[j + 1];
					sortChildren[j + 1] = temp;
				}

			}

		}
		//找出接收事件的子控件
		View targetView = null;
		for (int i = 0; i < count; i++) {
			final View child = sortChildren[i];
			RectF rectF = new RectF(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
			if (rectF.contains(mDownX, mDownY)) {
				targetView = child;
				break;
			}
		}
		//判定接收事件的子控件是第几个
		if (targetView == null) {
			return INVALID_POSITION;
		}
		int position = INVALID_POSITION;
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child == targetView) {
				position = i;
				break;
			}
		}
		return position;
	}

	private void onRelease() {
		judgeNewState();
		if (mItemAnimator != null && mItemAnimator.isRunning()) {
			mItemAnimator.cancel();
			mItemAnimator = null;
		}
		mItemAnimator = ValueAnimator.ofFloat(0f, 1f);
		mItemAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				float fraction = (float) valueAnimator.getAnimatedValue();
				//更新子View的位置
				int childCount = PileLayout.this.getChildCount();
				for (int i = 0; i < childCount; i++) {
					final View child = PileLayout.this.getChildAt(i);
					ItemDesc desc = descMap.get(child);
					int curX = (int) (desc.getStartX() + (desc.getEndX() - desc.getStartX()) * fraction);
					desc.setCurX(curX);
					PileLayout.this.requestLayout();
				}
				//更新贝塞尔曲线控制点的位置
				int pointCount = mPoints.length;
				for (int i = 0; i < pointCount; i++) {
					PointDesc point = mPoints[i];
					point.x = (int) (point.startX + (point.endX - point.startX) * fraction);
				}
				PileLayout.this.postInvalidate();
			}
		});
		mItemAnimator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				mState = newState;
				oldState = mState;
			}
		});
		//动画执行前初始化子控件的相关参数
		int childCount = PileLayout.this.getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = PileLayout.this.getChildAt(i);
			ItemDesc desc = descMap.get(child);
			desc.setStartX(desc.getCurX());
			if (newState == State.CLOSE) {
				desc.setEndX(mHorizontalThreshold);
			} else if (newState == State.OPEN) {
				desc.setEndX(0);
			} else {
				Log.i("phone", "PileLayout状态State出错");
			}
		}
		//动画执行前初始化贝塞尔控制点的相关参数
		int pointCount = mPoints.length;
		for (int i = 0; i < pointCount; i++) {
			PointDesc point = mPoints[i];
			point.startX = point.x;
			if (newState == State.CLOSE) {
				point.endX = getMeasuredWidth();
			} else if (newState == State.OPEN) {
				point.endX = 0;
			} else {
				Log.i("phone", "PileLayout状态State出错");
			}
		}
		mItemAnimator.setDuration(500);
		mItemAnimator.start();
	}

	/**
	 * 判断动画结束后State的值
	 */
	private void judgeNewState() {
		if (oldState == State.OPEN) {
			if (mDistanceX > mHorizontalThreshold / 2) {
				newState = State.CLOSE;
			} else {
				newState = State.OPEN;
			}
		} else if (oldState == State.CLOSE) {
			if (mDistanceX < -mHorizontalThreshold / 2) {
				newState = State.OPEN;
			} else {
				newState = State.CLOSE;
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		waveHeight = (int) Math.abs(mDistanceX * 1.0f);
		mPath.reset();
		mPath.moveTo(getMeasuredWidth(), 0);
		mPath.moveTo(mPoints[0].x, mPoints[0].y);
		mPath.lineTo(mPoints[1].x, mPoints[1].y);
		mPath.quadTo(mPoints[2].x, mPoints[2].y, mPoints[3].x, mPoints[3].y);
		mPath.quadTo(mPoints[4].x, mPoints[4].y, mPoints[5].x, mPoints[5].y);
		mPath.quadTo(mPoints[6].x, mPoints[6].y, mPoints[7].x, mPoints[7].y);
		mPath.quadTo(mPoints[8].x, mPoints[8].y, mPoints[9].x, mPoints[9].y);
		mPath.lineTo(mPoints[10].x, mPoints[10].y);
		mPath.lineTo(getMeasuredWidth(), getMeasuredHeight());
		mPath.lineTo(getMeasuredWidth(), 0);
		mPath.close();
		canvas.drawPath(mPath, mBezierPaint);

		//		testPaint.setStyle(Paint.Style.FILL);
		//		testPaint.setColor(Color.RED);
		//		for (int i = 0; i < mPoints.length; i++) {
		//			canvas.drawCircle(mPoints[i].x, mPoints[i].y, 10, testPaint);
		//		}
	}

	/**
	 * 计算绘制贝塞尔曲线时的11个点的坐标
	 */
	private void calculatePoints(float fraction, int eventY) {
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		waveHeight = (int) (getMeasuredWidth() / 2 * fraction);
		if (oldState == State.CLOSE) {
			mPoints[0].x = width;
			mPoints[0].y = 0;
			mPoints[10].x = width;
			mPoints[10].y = height;

			mPoints[1].x = width;
			mPoints[1].y = eventY - waveLength / 2;
			mPoints[9].x = width;
			mPoints[9].y = eventY + waveLength / 2;

			mPoints[2].x = width;
			mPoints[2].y = eventY - waveLength / 3;
			mPoints[8].x = width;
			mPoints[8].y = eventY + waveLength / 3;

			mPoints[3].x = width - waveHeight / 2;
			mPoints[3].y = eventY - waveLength / 6;
			mPoints[7].x = width - waveHeight / 2;
			mPoints[7].y = eventY + waveLength / 6;

			mPoints[4].x = width - waveHeight;
			mPoints[4].y = eventY - waveLength / 12;
			mPoints[6].x = width - waveHeight;
			mPoints[6].y = eventY + waveLength / 12;

			mPoints[5].x = width - waveHeight;
			mPoints[5].y = eventY;
		} else if (oldState == State.OPEN) {
			mPoints[0].x = 0;
			mPoints[0].y = 0;
			mPoints[10].x = 0;
			mPoints[10].y = getMeasuredHeight();

			mPoints[1].x = 0;
			mPoints[1].y = eventY - waveLength / 2;
			mPoints[9].x = 0;
			mPoints[9].y = eventY + waveLength / 2;

			mPoints[2].x = 0;
			mPoints[2].y = eventY - waveLength / 3;
			mPoints[8].x = 0;
			mPoints[8].y = eventY + waveLength / 3;

			mPoints[3].x = 0 + waveHeight / 2;
			mPoints[3].y = eventY - waveLength / 6;
			mPoints[7].x = 0 + waveHeight / 2;
			mPoints[7].y = eventY + waveLength / 6;

			mPoints[4].x = 0 + waveHeight;
			mPoints[4].y = eventY - waveLength / 12;
			mPoints[6].x = 0 + waveHeight;
			mPoints[6].y = eventY + waveLength / 12;

			mPoints[5].x = 0 + waveHeight;
			mPoints[5].y = eventY;
		} else {
			Log.e("phone", "oldState状态错误，不能为Middle状态");
		}
	}

	/**
	 * x方向上移动子View
	 *
	 * @param event
	 */
	private void offsetXForChildren(MotionEvent event) {
		int count = getChildCount();
		int eventY = (int) event.getY();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			ItemDesc desc = descMap.get(child);
			int itemCenterY = desc.getCurY() + child.getMeasuredHeight() / 2;
			double fraction = calculateItemXFraction(itemCenterY, eventY);
			//			int curX = (int) (diffX * fraction + mHorizontalThreshold);
			int curX;
			if (oldState == State.CLOSE) {
				curX = (int) (mHorizontalThreshold + mDistanceX * fraction);
			} else {
				curX = (int) (mDistanceX * fraction);
			}
			desc.setCurX(curX);
		}
		requestLayout();
		float fraction;
		if (oldState == State.CLOSE) {
			fraction = -mDistanceX * 1.0f / mHorizontalThreshold;
		} else {
			fraction = mDistanceX * 1.0f / mHorizontalThreshold;
		}
		calculatePoints(fraction, eventY);
		invalidate();
	}

	/**
	 * 校正水平滑动偏移量，不能超过水平滑动阈值
	 * 
	 * @param diffX
	 * @return
	 */
	private int checkDiffX(int diffX) {
		if (diffX > 0) {
			diffX = Math.min(diffX, mHorizontalThreshold);
		} else if (diffX < 0) {
			diffX = Math.max(diffX, -mHorizontalThreshold);
		}
		return diffX;
	}

	private double calculateItemXFraction(float itemCenterY, float y) {
		int l = getMeasuredHeight();
		double fraction = 0.5f * Math.sin(Math.PI / l * (itemCenterY - (y - l / 2))) + 0.5f;
		return fraction;
	}

	/**
	 * y方向上移动子View
	 * 
	 * @param diffY
	 */
	private void offsetYForChildren(int diffY) {
		int count = getChildCount();
		if (diffY > 0) {
			//往下滑，倒序遍历，先让底部的子View消费，然后剩余的偏移量让顶部的子View消费
			for (int i = count - 1; i >= 0; i--) {
				diffY = calculateConsumeYForScrollDown(i, diffY);
				if (diffY == 0) {
					break;
				}
			}
		} else if (diffY < 0) {
			//往上滑，顺序遍历，先让顶部的子View消费，然后剩余的偏移量让底部的子View消费
			for (int i = 0; i < count; i++) {
				diffY = calculateConsumeYForScrollUp(i, diffY);
				if (diffY == 0) {
					break;
				}
			}
		}
		requestLayout();
	}

	/**
	 * 往上滑动时，计算被消耗的偏移量以及各个子控件的y坐标
	 * 
	 * @param position
	 * @param diffY 这里为负数
	 * @return
	 */
	private int calculateConsumeYForScrollUp(int position, int diffY) {
		int count = adapter.getCount();
		View curChild = null;
		View nextChild = null;
		//已消耗的偏移量
		int consumeY;
		//未消耗的偏移量
		int unConsumeY = diffY;
		if (position >= 0 && position < count - 1) {
			curChild = getChildAt(position);
			nextChild = getChildAt(position + 1);

			ItemDesc curDesc = descMap.get(curChild);
			ItemDesc nextDesc = descMap.get(nextChild);

			//该子View已经滑动到顶部了，不能再往上滑动了
			if (curDesc.getCurY() == curDesc.getUpThreshold()) {
				return unConsumeY;
			}

			//该子View可以移动的y量
			int canUserDistance = curDesc.getCurY() + curChild.getMeasuredHeight() - nextDesc.getCurY();
			if (canUserDistance > -diffY) {
				consumeY = diffY;
				unConsumeY = 0;
			} else {
				consumeY = -canUserDistance;
				unConsumeY = diffY + canUserDistance;
			}
			//偏移当前子View及之前的子View的y坐标
			for (int i = position; i >= 0; i--) {
				final View child = getChildAt(i);
				adjustItemY(child, consumeY);
			}
		}
		return unConsumeY;
	}

	/**
	 * 往下滑动时，计算被消耗的偏移量以及各个子控件的y坐标
	 * 
	 * @param position
	 * @param diffY
	 * @return
	 */
	private int calculateConsumeYForScrollDown(int position, int diffY) {
		int count = adapter.getCount();
		View curChild = null;
		View lastChild = null;
		//已消耗的偏移量
		int consumeY;
		//未消耗的偏移量
		int unConsumeY = diffY;
		if (position > 0 && position < count) {
			curChild = getChildAt(position);
			lastChild = getChildAt(position - 1);

			ItemDesc curDesc = descMap.get(curChild);
			ItemDesc lastDesc = descMap.get(lastChild);

			//该子控件已经滑动到底部了，不能往下滑动了
			if (curDesc.getCurY() == curDesc.getDownThreshold()) {
				return unConsumeY;
			}
			//该子View可以移动的y量
			int canUserDistance = lastDesc.getCurY() + lastChild.getMeasuredHeight() - curDesc.getCurY();
			if (canUserDistance >= diffY) {
				consumeY = diffY;
				unConsumeY = 0;
			} else {
				consumeY = canUserDistance;
				unConsumeY = diffY - canUserDistance;
			}
			//偏移当前子View及后面的子View的y坐标
			for (int i = position; i < count; i++) {
				final View child = getChildAt(i);
				adjustItemY(child, consumeY);
			}
		}
		return unConsumeY;
	}

	/**
	 * 调整子View的y坐标
	 * 
	 * @param view
	 * @param consumeY
	 */
	private void adjustItemY(View view, int consumeY) {
		ItemDesc desc = descMap.get(view);
		int upThreshold = desc.getUpThreshold();
		int downThreshold = desc.getDownThreshold();
		if (desc.getCurY() + consumeY < upThreshold) {
			desc.setCurY(upThreshold);
		} else if (desc.getCurY() + consumeY > downThreshold) {
			desc.setCurY(downThreshold);
		} else {
			desc.setCurY(desc.getCurY() + consumeY);
		}
	}

	public void setAdapter(@NonNull ListAdapter adapter) {
		if (adapter == null) {
			throw new IllegalArgumentException("The adapter cannot be null.");
		}
		if (this.adapter != null) {
			this.adapter.unregisterDataSetObserver(dataSetObserver);
		}
		this.adapter = adapter;
		this.adapter.registerDataSetObserver(dataSetObserver);
		attachChildViews();
		requestLayout();
	}

	private void attachChildViews() {
		descMap.clear();
		removeAllViews();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			final View child = adapter.getView(i, null, this);
			addView(child);
			final ItemDesc desc = new ItemDesc();
			descMap.put(child, desc);
			int curY = mItemsMarginTop * i + getPaddingTop();
			int curX = mHorizontalThreshold;
			desc.setCurX(curX);
			desc.setCurY(curY);
		}
	}

	private OnItemClickListener mOnItemClickListener;
	private OnItemLongClickListener mOnItemLongClickListener;

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mOnItemClickListener = listener;
	}

	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		this.mOnItemLongClickListener = listener;
	}

	public interface OnItemClickListener {
		void onItemclick(int position);
	}

	public interface OnItemLongClickListener {
		void onItemLongClick(int position);
	}
}
