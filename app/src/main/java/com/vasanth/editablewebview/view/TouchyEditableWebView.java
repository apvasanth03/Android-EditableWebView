package com.vasanth.editablewebview.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ScrollView;

/**
 * Touchy Editable WebView.
 * <p>
 * 1. Custom {@link EditableWebView}, Helps us to be use it inside the scrollview.
 * 2. WebView which itself is a scrollview hence placing webView inside a scrollview will not work properly.
 * 3. So to fix it, we have created a custom webView.
 * <p>
 * NOTE
 * 1. Use this view inside Vertical ScrollView.
 * 2. Set ParentScrollView reference using setParentScrollView() method, to scroll to user typing position if required.
 *
 * @author vasanth
 */
public class TouchyEditableWebView extends EditableWebView {

    private float lastX;
    private float lastY;
    private ScrollView svParent;
    private int lineHeightThreshold = 0;

    public TouchyEditableWebView(Context context) {
        super(context);

        initialize();
    }

    public TouchyEditableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
    }

    public TouchyEditableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize();
    }

    /**
     * On Touch Event.
     * <p>
     * 1. If user uses 2 or more fingers (scroll, pinch zoom etc) - we will disable all those events.
     * <p>
     * 2. If user uses one finger then we need to decide whether we need to scroll webView or pass event to parent.
     * 2.1. We will find out whether user has scrolled horizontally or Vertically.
     * 2.2. If he has scrolled Horizontally - Then we will handle it in WebView itself.
     * 2.3. If he has scrolled Vertically - Then we will pass the event to parent.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            return true;
        } else {
            int action = MotionEventCompat.getActionMasked(event);
            if (action == MotionEvent.ACTION_DOWN) {
                lastX = event.getX();
                lastY = event.getY();
            } else if (action == MotionEvent.ACTION_MOVE) {
                float xDelta = event.getX() - lastX;
                float yDelta = event.getY() - lastY;

                boolean isScrolledHorizontally = ((Math.abs(xDelta) - Math.abs(yDelta)) > 0);
                if (isScrolledHorizontally) {
                    // Don't pass touch event to parent.
                    requestDisallowInterceptTouchEvent(true);
                } else {
                    // Pass touch event to parent.
                    requestDisallowInterceptTouchEvent(false);
                }

                lastX = event.getX();
                lastY = event.getY();
            }
            return super.onTouchEvent(event);
        }
    }

    // FSEditableWebView METHODS.
    @Override
    protected void onContentKeyUpEventPosition(int x, int y) {
        super.onContentKeyUpEventPosition(x, y);

        checkAndScrollToPositionIfRequired(x, y);
    }

    // PUBLIC METHODS.
    public void setParentScrollView(@NonNull ScrollView svParent) {
        this.svParent = svParent;
    }

    // PRIVATE METHODS.

    private void initialize() {
        setVerticalScrollBarEnabled(false);
        lineHeightThreshold = (int) convertDpToPixel(20, getContext()); // LineHeight 20Dp Approx.
    }

    /**
     * 1. If we using WebView inside ScrollView, If user start typing multiple line's then webView will not scroll automatically to the cursor position. That we need to take care.
     * 2. Check if the input cursor position is visible to user.
     * 3. If Yes - Don't do any thing.
     * 4. If No - Then scroll to the position.
     */
    private void checkAndScrollToPositionIfRequired(int x, int y) {
        if (svParent != null) {
            // WebView, may not be the first child of the scrollView, there may be few views above it. Hence also add those offset values.
            // (We need to find the position relative to ParentScrollView)
            Point wvOffset = getWebViewOffsetFromParent(svParent);
            int cursorXPos = x + wvOffset.x;
            int cursorYPos = y + wvOffset.y;

            // Get the current visible ParentScrollView Rect.
            Rect scrollBounds = new Rect();
            svParent.getDrawingRect(scrollBounds);

            // Check if cursor position is visible.
            boolean isCursorPositionVisible = (scrollBounds.top < (cursorYPos + lineHeightThreshold)) && (scrollBounds.bottom > (cursorYPos + lineHeightThreshold));
            if (!isCursorPositionVisible) {
                svParent.smoothScrollTo(cursorXPos, cursorYPos);
            }
        }
    }

    // Used to get WebView offset from ScrollView, because there may be some views before WebView.
    private Point getWebViewOffsetFromParent(@NonNull ScrollView svParent) {
        Point childOffset = new Point();
        getDeepChildOffset(svParent, getParent(), this, childOffset);
        return childOffset;
    }

    // The child may not the direct child to scrollview, So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
    private void getDeepChildOffset(final ViewGroup mainParent, final ViewParent parent, final View child, final Point accumulatedOffset) {
        ViewGroup parentGroup = (ViewGroup) parent;
        accumulatedOffset.x += child.getLeft();
        accumulatedOffset.y += child.getTop();
        if (parentGroup.equals(mainParent)) {
            return;
        }
        getDeepChildOffset(mainParent, parentGroup.getParent(), parentGroup, accumulatedOffset);
    }
}
