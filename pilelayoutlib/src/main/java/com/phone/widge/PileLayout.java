package com.phone.widge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
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

	private static final int DEFAULT_ITEM_MARGIN_TOP = 30;
	private static final int DEFAULT_ITEM_MARGIN_BOTTOM = 30;
	private static final int DEFAULT_LEFT_THRESHOLD = -50;
	private static final int DEFAULT_RIGHT_THRESHOLD = 50;
	//轻微滑动阈值
	private int mTouchSlop;
	//相邻item间顶部的距离
	private int mItemsMarginTop;
	//相邻item间底部的距离
	private int mItemsMarginBottom;
	//子View左右边界的阈值
	private int mLeftThreshold;
	private int mRightThreshold;
	//当前设置的滑动出来的方向
	private Mode mMode = Mode.LEFT;
	//当前的滑动状态
	private State mState = State.CLOSE;

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
	private int mDistanceX;
	//水平滑动绘制效果
	//	private EffectControl effectControl;

	enum Direction {
		HORIZONTAL, VERTICAL, NONE
	}

	enum Mode {
		LEFT, RIGHT
	}

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
		//		effectControl=new EffectControl(this);
		//获取自定义属性及其值
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PileLayout);
		mItemsMarginTop = array.getInt(R.styleable.PileLayout_itemMarginTop, DEFAULT_ITEM_MARGIN_TOP);
		mItemsMarginBottom = array.getInt(R.styleable.PileLayout_itemMarginBottom, DEFAULT_ITEM_MARGIN_BOTTOM);
		mLeftThreshold = array.getInt(R.styleable.PileLayout_leftThreshold, DEFAULT_LEFT_THRESHOLD);
		mRightThreshold = array.getInt(R.styleable.PileLayout_rightThreshold, DEFAULT_RIGHT_THRESHOLD);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		//		ViewGroup.LayoutParams lp = getLayoutParams();
		//		lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
		//		setLayoutParams(lp);

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

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			//			int childWidth = child.getMeasuredWidth();
			//			int childHeight = child.getMeasuredHeight();
			ItemDesc desc = descMap.get(child);
			int childLeft;
			int childTop = desc.getCurY();
			if (mMode == Mode.LEFT) {
				//				desc.setCurX(mLeftThreshold);
				childLeft = desc.getCurX();
			} else {
				//				desc.setCurX(mRightThreshold);
				childLeft = desc.getCurX();
			}
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
				mDownX = event.getX();
				mDownY = event.getY();
				mLastX = event.getX();
				mLastY = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				float dx = event.getX() - mDownX;
				float dy = event.getY() - mDownY;
				if (Math.abs(dx) > mTouchSlop * 2 || Math.abs(dy) > mTouchSlop) {
					if (Math.abs(dx) > Math.abs(dy) * 2) {
						mDirection = Direction.HORIZONTAL;
					} else {
						mDirection = Direction.VERTICAL;
					}
				}

				if (mDirection == Direction.HORIZONTAL) {
					//响应水平方向滑动，这里记录累加值
					float mDistanceX = event.getX() - mDownX;
					mDistanceX = checkDiffX((int) mDistanceX);
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
				mDirection = Direction.NONE;
				mLastX = 0;
				mLastY = 0;
				break;
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//		effectControl.draw(canvas);
	}

	//	/**
	//	 * x方向上移动子View
	//	 *
	//	 * @param diffX
	//	 */
	//	private void offsetXForChildren(int diffX, MotionEvent event) {
	//		int count = getChildCount();
	//		for (int i = 0; i < count; i++) {
	//			final View child = getChildAt(i);
	//			int y = (int) event.getY();
	//			ItemDesc desc = descMap.get(child);
	//			int itemCenterY = desc.getCurY() + child.getMeasuredHeight() / 2;
	//			double fraction = calculateItemXFraction(itemCenterY, y);
	//			int curX;
	//			if (mMode == Mode.LEFT) {
	//				Log.i("phoneTest", "=====left=====");
	//				diffX = checkDiffX(diffX);
	//				curX = (int) (diffX * fraction - mLeftThreshold);
	//			} else {
	//				Log.i("phoneTest", "=====right=====");
	//				diffX = checkDiffX(diffX);
	//				curX = (int) (diffX * fraction + mRightThreshold);
	//			}
	//			Log.i("phoneTest", "curX:" + curX);
	//			desc.setCurX(curX);
	//		}
	//		requestLayout();
	//	}

	/**
	 * 校正水平滑动偏移量，不能超过水平滑动阈值
	 * 
	 * @param diffX
	 * @return
	 */
	private int checkDiffX(int diffX) {
		if (diffX > 0) {
			diffX = Math.min(diffX, -mLeftThreshold);
		} else if (diffX < 0) {
			diffX = -Math.max(diffX, -mRightThreshold);
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
		//		if (this.adapter != null) {
		//			this.adapter.unregisterDataSetObserver(dataSetObserver);
		//		}
		this.adapter = adapter;
		//		this.adapter.registerDataSetObserver(dataSetObserver);
		attachChildViews();
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
			int curX = getPaddingLeft();
			if (mMode == Mode.LEFT) {
				curX = mLeftThreshold;
			} else {
				curX = mRightThreshold;
			}
			desc.setCurX(curX);
			desc.setCurY(curY);
		}
		requestLayout();
	}
}
