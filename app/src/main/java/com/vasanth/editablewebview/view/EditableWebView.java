package com.vasanth.editablewebview.view;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Editable WebView.
 * <p>
 * 1. Allows user to input in WebView.
 *
 * @author Vasanth
 */
public class EditableWebView extends WebView {

    private static final String MIME_TYPE_TEXT_HTML = "text/html";
    private static final String ENCODING_UTF8 = "utf-8";
    private static final String ASSET_FOLDER_BASE_PATH = "file:///android_asset/";
    private static final String HTML_TEMPLATE = "<html>\n" +
            "  <head>\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
            "    %1$s\n" +
            "  </head>\n" +
            "  <body>\n" +
            "     %2$s\n" +
            "  </body>\n" +
            "</html>";
    private static final String HEADER_TEMPLATE = "<LINK href=\"html/EditableWebView.css\" rel=\"stylesheet\" type=\"text/css\"/>\n" +
            "<script src=\"html/EditableWebView.js\"></script>";
    private static final String BODY_TEMPLATE = "<div id=\"contentBody\" contentEditable=\"true\" style=\"min-height:%1$spx;\">%2$s</div>";

    private int contentMinHeightInPx;
    private JavaScriptInterface javaScriptInterface;
    private GetContentListener getContentListener;

    // Interface definition for a callback to be invoked when requested for getContent.
    public interface GetContentListener {
        void onContentReceived(@NonNull String content);
    }

    public EditableWebView(Context context) {
        super(context);
        initializeEditableWebView();
    }

    public EditableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeEditableWebView();
    }

    public EditableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeEditableWebView();
    }

    // PUBLIC METHODS.

    /**
     * Sets the minHeight for the content Div in WebView.
     * NOTE - Must be called before setting content.
     */
    public void setContentMinHeight(int contentMinHeightInPx) {
        this.contentMinHeightInPx = contentMinHeightInPx;
    }

    /**
     * Sets the content that this WebView is to display.
     * NOTE - Use {@link android.webkit.WebViewClient#onPageFinished(WebView, String)} to get callback after content has been loaded in WebView.
     */
    public void setContent(@NonNull String content) {
        String contentBody = String.format(BODY_TEMPLATE, contentMinHeightInPx, content);
        String html = String.format(HTML_TEMPLATE, HEADER_TEMPLATE, contentBody);
        loadDataWithBaseURL(ASSET_FOLDER_BASE_PATH, html, MIME_TYPE_TEXT_HTML, ENCODING_UTF8, null);
    }

    /**
     * Get the content this WebView is displaying.
     */
    public void getContent(@NonNull GetContentListener getContentListener) {
        this.getContentListener = getContentListener;
        javaScriptInterface.getContent();
    }

    /**
     * Inserts the given content at the userSelection.
     */
    public void insertContentAtUserSelection(@NonNull String content) {
        javaScriptInterface.insertContentAtUserSelection(content);
    }

    /**
     * Sets the cursor position at the given Html Document ElementId.
     */
    public void setTheCursorPositionAtTheGivenElementId(@NonNull String elementId) {
        javaScriptInterface.setTheCursorPositionAtTheGivenElementId(elementId);
    }

    // PROTECTED METHODS.

    /**
     * 1. Gets called on Content KeyUp Event with the cursor position.
     */
    protected void onContentKeyUpEventPosition(int x, int y) {

    }

    // PRIVATE METHODS.
    private void initializeEditableWebView() {
        WebSettings webSettings = getSettings();
        webSettings.setSupportZoom(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        javaScriptInterface = new JavaScriptInterface();
        addJavascriptInterface(javaScriptInterface, "injectedObject");

        contentMinHeightInPx = (int) convertDpToPixel(20f, getContext());
    }

    // JavaScriptInterface to communicate between WebView & Java.
    private class JavaScriptInterface {


        JavaScriptInterface() {
        }

        void getContent() {
            loadUrl("javascript:getContent();");
        }

        // Get called from WebView JavaScript.
        @JavascriptInterface
        public void onContentReceived(@Nullable final String content) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    EditableWebView.this.onContentReceived(content);
                }
            });
        }

        void insertContentAtUserSelection(@NonNull String content) {
            loadUrl("javascript:insertContentAtUserSelection(\"" + StringEscapeUtils.escapeEcmaScript(content) + "\");");
        }

        void setTheCursorPositionAtTheGivenElementId(@NonNull String elementId) {
            loadUrl("javascript:setTheCursorPositionAtTheGivenElementId(\"" + StringEscapeUtils.escapeEcmaScript(elementId) + "\");");
        }

        /**
         * 1. Get called from WebView JavaScript.
         * 2. Gets called on Content KeyUp Event with the cursor position.
         */
        @JavascriptInterface
        public void onContentKeyUpEventPosition(final int x, final int y) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    int xOffset = (int) (x * getResources().getDisplayMetrics().density);
                    int yOffset = (int) (y * getResources().getDisplayMetrics().density);
                    EditableWebView.this.onContentKeyUpEventPosition(xOffset, yOffset);
                }
            });
        }
    }

    private void onContentReceived(@Nullable String content) {
        if (getContentListener != null) {
            content = content != null ? content : "";
            getContentListener.onContentReceived(content);
        }
    }

    protected static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
