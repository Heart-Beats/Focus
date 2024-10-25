package com.ihewro.focus.util;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebView;

import com.blankj.ALog;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ihewro.focus.view.ImageManagePopupView;
import com.lxj.xpopup.XPopup;

import java.util.List;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/05/11
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class MJavascriptInterface {
    private Context activity;
    private String[] imageUrls;
    private WebView webView;

    public MJavascriptInterface(Context context, String[] imageUrls, WebView webView) {
        this.activity = context;
        this.imageUrls = imageUrls;
        this.webView = webView;
    }

    @android.webkit.JavascriptInterface
    public void openImage(String img) {
        ALog.d("点击了图片" +img);
        ImageLoadUtil.showSingleImageDialog(activity, img, null);
    }

    @android.webkit.JavascriptInterface
    public void onImageClick(int position, String imageUrlsJson) {
        // 使用 Gson 将 JSON 字符串解析为 Java List
        List<String> imageUrls = new Gson().fromJson(imageUrlsJson, new TypeToken<List<String>>() {}.getType());

        ALog.d("点击图片" + imageUrls.get(position));

        if (imageUrls.size() == 1) {
            ImageLoadUtil.showSingleImageDialog(activity, imageUrls.get(position), null);
        } else {
            ImageLoadUtil.showMultipleImageDialog(activity, position, imageUrls, null);
        }
    }


    @android.webkit.JavascriptInterface
    public void onImageLongPress(String img) {
        ALog.d("长按图片" +img);
        //显示下拉底部弹窗
        new XPopup.Builder(activity)
                .asCustom(new ImageManagePopupView(activity,img,null))
                .show();
    }

    @android.webkit.JavascriptInterface
    public void openUrl(final String url) {
        // 直接打开链接不进行提示
        WebViewUtil.openLink(url, (Activity) activity);
    }

}