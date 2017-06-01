package com.phone.widge;

/**
 * Created by Phone on 2017/5/27.
 */

public class ItemDesc {

	//上阈值：View的y坐标不能小于该值
	private int upThreshold;
	//下阈值：View的y坐标不能大于该值
	private int downThreshold;
	//View当前的纵坐标
	private int curY;
	//View当前的横坐标
	private int curX;
	//记录View水平动画开始时的横坐标
	private int startX;
	//记录View水平动画结束时的横坐标
	private int endX;

	public int getUpThreshold() {
		return upThreshold;
	}

	public int getDownThreshold() {
		return downThreshold;
	}

	public int getCurY() {
		return curY;
	}

	public int getCurX() {
		return curX;
	}

	public int getStartX() {
		return startX;
	}

	public int getEndX() {
		return endX;
	}

	public void setUpThreshold(int upThreshold) {
		this.upThreshold = upThreshold;
	}

	public void setDownThreshold(int downThreshold) {
		this.downThreshold = downThreshold;
	}

	public void setCurY(int curY) {
		this.curY = curY;
	}

	public void setCurX(int curX) {
		this.curX = curX;
	}

	public void setStartX(int startX) {
		this.startX = startX;
	}

	public void setEndX(int endX) {
		this.endX = endX;
	}
}
