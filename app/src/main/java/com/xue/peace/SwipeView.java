package com.xue.peace;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 实现右滑删除功能
 */
public class SwipeView extends ViewGroup {
	private static final String TAG = SwipeView.class.getSimpleName();
	
	//按下时view的scorll距离，上次的事件位置
	private int downScrollX,lastX;
	//按下时的事件位置
	private float downX,downY;
	//滑动过程中view是否移动过，如果没有则action up后可能会触发click事件
    private boolean hasMoved;
    //删除按钮的宽度，参与计算布局
    private int deleteWidth;
    //SwipeView只能左右滑动，如果起始滑动时移动的角度倾向于Y方向则将事件传递给容器
    private boolean firstOnMove = true;
    private boolean sendToParent = false;

	private static SwipeView sView;
    private Scroller scroller = new Scroller(getContext());

    public SwipeView(Context context) {
        super(context);
    }

    public SwipeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = 0,heightSize = 0;
        
        View child0 = this.getChildAt(0);
        MarginLayoutParams lParams = (MarginLayoutParams) child0.getLayoutParams();
        if(widthMode == MeasureSpec.EXACTLY) {
            widthSize = MeasureSpec.getSize(widthMeasureSpec);
        }else if(lParams.width > 0){
            widthSize = getPaddingLeft() + lParams.leftMargin + lParams.width;
        }

        if(heightMode == MeasureSpec.EXACTLY) {
            heightSize = MeasureSpec.getSize(heightMeasureSpec);
        }else if(lParams.height > 0){
            heightSize = getPaddingTop() + lParams.topMargin + lParams.height;
        }
		
		for(int i = 0;i<getChildCount();i++) {//getChildCount==2
			View child = this.getChildAt(i);
			this.measureChild(child, widthMeasureSpec, heightMeasureSpec);
			MarginLayoutParams st =
	                (MarginLayoutParams) child.getLayoutParams();
			st.width = child.getMeasuredWidth();
			st.height = child.getMeasuredHeight();
			if(widthSize < st.width) widthSize = st.width;
			if(heightSize < st.height) heightSize = st.height;
		}
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	View child = getChildAt(0);
    	MarginLayoutParams st =
                (MarginLayoutParams) child.getLayoutParams();
    	child.layout(st.leftMargin+ getPaddingLeft(), st.topMargin + getPaddingTop(),
    			getPaddingLeft() + st.width, st.topMargin + st.height + getPaddingTop());
    	//删除按钮放在SwipeView右边,只有向左边滑动才会显示出来
        View delBtnView = getChildAt(1);
        MarginLayoutParams lp2 =
                (MarginLayoutParams) delBtnView.getLayoutParams();
        delBtnView.layout(r, lp2.topMargin,r + delBtnView.getMeasuredWidth(),
        		lp2.bottomMargin + delBtnView.getMeasuredHeight() + getPaddingBottom());
    }

	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	//只允许一个view显示删除按钮
                if (sView != null && sView != this) {
                    sView.close();
                }
                deleteWidth = getChildAt(1).getWidth();
                //不允许父控件拦截触摸事件
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
            	//不应该走到这里
            	Log.i(TAG, "onInterceptTouchEvent ACTION_MOVE");
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				if(firstOnMove) {//action down后面的第一个action move事件
					float curX = event.getRawX();
					float curY = event.getRawY();
					float distance_pow = (curX - downX) * (curX - downX) + (curY - downY) * (curY - downY);
					//防抖
					if(distance_pow < 20 * 20){
					    break;
                    }
					//起始滑动方向与X轴角度超过一定角度
					if(Math.abs((curY-downY)/(curX-downX)) > 0.5) {
						sendToParent = true;
					}
					firstOnMove = false;
				}
				if(sendToParent) {
					getParent().requestDisallowInterceptTouchEvent(false);
				}else {
					int moveX = (int) event.getRawX();
					int moved = moveX - lastX;// 手指移动距离=当前位置-按下时的位置
					int scrollX = getScrollX();
					int destX = scrollX - moved;
					if (destX > deleteWidth) {
						destX = deleteWidth;
					} else if (destX < 0) {
						destX = 0;
					}
					if(downScrollX != destX) {
						hasMoved = true;
					}
					scrollTo(destX, 0);
					lastX = moveX;
				}
				break;
        	case MotionEvent.ACTION_DOWN:
        		downScrollX = getScrollX();
        		downX = event.getRawX();
        		downY = event.getRawY();
        		lastX = (int)downX;
        		hasMoved = false;
        		firstOnMove = true;
        		sendToParent = false;
        		
        		if(!scroller.isFinished()){
        			scroller.abortAnimation();
                }
        		break;
            case MotionEvent.ACTION_CANCEL:
            	//触摸时间被父控件拦截,onInterceptTouchEvent中已经禁止该行为
            	Log.e(TAG, "ACTION_CANCEL");
            case MotionEvent.ACTION_UP:
            	Log.i(TAG, "ACTION_UP");
                if (getScrollX() >= deleteWidth / 2) {
                    sView = this;
                    smoothScrollToPosition(deleteWidth);
                } else {
                    smoothScrollToPosition(0);
                }
                //1、如果SwipeView左右滚动了则不触发onClick
                //2、如果container(RecyclerView)上下滑动则不触发onCilck
                //3、如果action up时的位置脱离了当前view，则不触发onCilck
                if(!hasMoved && !sendToParent) {
                	int[] location = new int[2];
                    // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
                    getLocationOnScreen(location);
                    RectF rectF = new RectF(location[0], location[1],
                    		location[0] + getWidth(), location[1] + getHeight());
                    if(rectF.contains(event.getRawX(), event.getRawY())) {
                    	performClick();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }
    
	@Override
	public boolean performClick() {
		return super.performClick();
	}

	private void smoothScrollToPosition(int destX) {
        int width = getScrollX();
        int delta = destX - width;
        scroller.startScroll(width, 0, delta, 0, 500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), 0);
            postInvalidate();
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (sView != null) {
            sView.close();
        }
    }

    public void closeSwipe() {
        if (sView != null) {
            sView.close();
        }
    }

    private void close() {
        smoothScrollToPosition(0);
    }

    public static boolean closeMenu(View v) {
        boolean flag = false;
        if (sView != null) {
            sView.close();
            flag = sView.getChildAt(0) == v || sView.getChildAt(1) == v;
            sView = null;
        }
        return flag;
    }
    
    public void setOnClickRightView(OnClickListener listener) {
    	getChildAt(1).setOnClickListener(listener);
    }
}
