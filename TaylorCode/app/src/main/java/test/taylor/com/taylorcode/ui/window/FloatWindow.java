package test.taylor.com.taylorcode.ui.window;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import test.taylor.com.taylorcode.R;
import test.taylor.com.taylorcode.util.DimensionUtil;

/**
 * suspending window in app,it shows throughout the whole app
 */
public class FloatWindow implements View.OnTouchListener {

    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 100;
    /**
     * the content view of window
     */
    private View windowView;
    /**
     * the layout param for windowView
     */
    private WindowManager.LayoutParams layoutParam;
    private int width;
    private int height;
    private float x;
    private float y;
    private float lastTouchX;
    private float lastTouchY;
    private int screenWidth;
    private int screenHeight;
    private Context context;
    private GestureDetector gestureDetector;
    private OnWindowViewClickListener clickListener;
    private OnWindowStatusChangeListener onWindowStatusChangeListener;
    private OnWindowMoveListener onWindowMoveListener;
    /**
     * this list records the activities which shows this window
     */
    private List<Class> whiteList;
    /**
     * if true,whiteList will be used to depend which activity could show window
     * if false,all activities in app is allow to show window
     */
    private boolean enableWhileList;
    /**
     * whether to show or hide this window
     * true by default
     */
    private boolean enable = true;

    private static volatile FloatWindow INSTANCE;

    public static FloatWindow getInstance() {
        if (INSTANCE == null) {
            synchronized (FloatWindow.class) {
                if (INSTANCE == null) {
                    //in case of memory leak for singleton
                    INSTANCE = new FloatWindow();
                }
            }
        }
        return INSTANCE;
    }

    private FloatWindow() {
        whiteList = new ArrayList<>();
    }

    public void updateWindowView(IWindowUpdater updater) {
        if (updater != null) {
            updater.updateWindowView(windowView);
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.updateViewLayout(windowView, layoutParam);
        }
    }

    public void setOnWindowMoveListener(OnWindowMoveListener onWindowMoveListener) {
        this.onWindowMoveListener = onWindowMoveListener;
        }

    public void setOnClickListener(OnWindowViewClickListener listener) {
        clickListener = listener;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setOnWindowStatusChangeListener(OnWindowStatusChangeListener onWindowStatusChangeListener) {
        this.onWindowStatusChangeListener = onWindowStatusChangeListener;
    }

    public void show(Context context) {
        if (!enable) {
            return;
        }
        if (windowView == null) {
            return;
        }
        if (context == null) {
            return;
        }
        this.context = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }
        prepareScreenDimension(context);
        if (gestureDetector == null) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        }
        layoutParam = generateLayoutParam(screenWidth, screenHeight);
        //in case of "IllegalStateException :has already been added to the window manager."
        if (windowView.getParent() == null) {
            windowManager.addView(windowView, layoutParam);
            if (onWindowStatusChangeListener != null) {
                onWindowStatusChangeListener.onShow();
            }
        }
    }


    private WindowManager.LayoutParams generateLayoutParam(int screenWidth, int screenHeight) {
        if (context == null) {
            return new WindowManager.LayoutParams();
        }

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;//this is the key point let window be above all activity
//        layoutParams.token = getWindow().getDecorView().getWindowToken();
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.width = this.width == 0 ? DEFAULT_WIDTH : this.width;
        layoutParams.height = this.height == 0 ? DEFAULT_HEIGHT : this.height;
        int defaultX = screenWidth - layoutParams.width;
        int defaultY = 2 * screenHeight / 3;
        layoutParams.x = layoutParam == null ? defaultX : layoutParam.x;
        layoutParams.y = layoutParam == null ? defaultY : layoutParam.y;
        return layoutParams;
    }

    public WindowManager.LayoutParams getLayoutParam() {
        return layoutParam;
    }

    public void dismiss() {
        if (context == null) {
            return;
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            //in case of "IllegalStateException :not attached to window manager."
            if (windowView.getParent() != null) {
                windowManager.removeViewImmediate(windowView);
                if (onWindowStatusChangeListener != null) {
                    onWindowStatusChangeListener.onDismiss();
                }
            }
        }
    }

    public void setWhiteList(List<Class> whiteList) {
        enableWhileList = true;
        this.whiteList = whiteList;
    }

    /**
     * invoking this method is a must ,or window has no content to show
     *
     * @param view the content view of window
     * @return
     */
    public FloatWindow setView(View view) {
        this.windowView = view;
        windowView.setOnTouchListener(this);
        return this;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        int action = event.getAction();
//
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                onActionDown(event);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                onActionMove(event);
//                break;
//            default:
//                break;
//        }
        gestureDetector.onTouchEvent(event);
        return true;
    }


    private void onActionMove(MotionEvent event) {
        float dx = event.getRawX() - lastTouchX;
        float dy = event.getRawY() - lastTouchY;
        lastTouchX = event.getRawX();
        lastTouchY = event.getRawY();
        layoutParam.x += dx;
        layoutParam.y += dy;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            WindowManager.LayoutParams partnerParams = null;
            if (onWindowMoveListener != null) {
                partnerParams = onWindowMoveListener.onWindowMove(dx, dy, screenWidth, screenHeight, layoutParam);
            }
            int rightMost = screenWidth - layoutParam.width;
            int leftMost = 0;
//            if (partnerParams != null) {
//                int partnerVisibleWidth = partnerParams.width - layoutParam.width / 2;
//                if (partnerParams.x < layoutParam.x) {
//                    leftMost = leftMost + partnerVisibleWidth;
//                } else {
//                    rightMost = rightMost - partnerVisibleWidth;
//                }
//            }
            if (layoutParam.x < leftMost) {
                layoutParam.x = leftMost;
            }
            if (layoutParam.x > rightMost) {
                layoutParam.x = rightMost;
            }
            int topMost = 0;
            int bottomMost = screenHeight - layoutParam.height;
            if (layoutParam.y < topMost) {
                layoutParam.y = topMost;
            }
            if (layoutParam.y > bottomMost) {
                layoutParam.y = bottomMost;
            }
            windowManager.updateViewLayout(windowView, layoutParam);
        }

    }

    private void onActionDown(MotionEvent event) {
        x = event.getRawX();
        y = event.getRawY();
        lastTouchX = x;
        lastTouchY = y;
    }


    private void prepareScreenDimension(Context activity) {
        if (screenWidth != 0 && screenHeight != 0) {
            return;
        }
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            DisplayMetrics dm = new DisplayMetrics();
            Display display = windowManager.getDefaultDisplay();
            if (display != null) {
                windowManager.getDefaultDisplay().getMetrics(dm);
                screenWidth = dm.widthPixels;
                screenHeight = dm.heightPixels;
            }
        }
    }

    /**
     * let ui decide how to update window
     */
    public interface IWindowUpdater {
        void updateWindowView(View windowView);
    }

    public interface OnWindowViewClickListener {
        void onWindowViewClick();
    }

    public interface OnWindowStatusChangeListener {
        void onShow();

        void onDismiss();
    }

    public interface OnWindowMoveListener {
        WindowManager.LayoutParams onWindowMove(float dx, float dy, int screenWidth, int screenHeight, WindowManager.LayoutParams hostParams);
    }

    private class GestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            onActionDown(e);
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (clickListener != null) {
                clickListener.onWindowViewClick();
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            onActionMove(e2);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

    }
}