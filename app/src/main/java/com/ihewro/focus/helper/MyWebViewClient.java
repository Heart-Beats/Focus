package com.ihewro.focus.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.blankj.ALog;
import com.ihewro.focus.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/05/11
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MyWebViewClient extends com.just.agentweb.WebViewClient {

    private Context context;
    private String url;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onPageFinished(WebView view, String url) {
        view.getSettings().setJavaScriptEnabled(true);
        super.onPageFinished(view, url);
//        view.getSettings().setBlockNetworkImage(false);
        addClickListener(view);//待网页加载完全后设置图片点击的监听方法
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        view.getSettings().setJavaScriptEnabled(true);

        //显示加载动画
        super.onPageStarted(view, url, favicon);
    }




    public MyWebViewClient() {
    }

    public MyWebViewClient(Context context,String url) {
        this.context = context;
        this.url = StringUtil.getUrlPrefix(url);
    }

    private void addClickListener(WebView webView) {
    }


    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//        ALog.d("请求的url有" + url);

        //拦截https://focus.com/content.css 替换成本地的css
        AssetManager am = context.getAssets();
       if (url.equals("https://focus.com/content.css")){
           try {
               InputStream is = am.open("css/" + "webview.css");
//               ALog.i("shouldInterceptRequest", "use offline resource for: " + url);
               return new WebResourceResponse("text/css", "UTF-8", is);
           } catch (IOException e) {
               e.printStackTrace();
           }
       }else if (url.equals("https://focus.com/content.js")){
           try {
               InputStream is = am.open("js/" + "content.js");
               ALog.i("shouldInterceptRequest", "use offline resource for: " + url);
               return new WebResourceResponse("application/javascript", "UTF-8", is);
           } catch (IOException e) {
               e.printStackTrace();
           }
       }

        return super.shouldInterceptRequest(view, url);
    }
}