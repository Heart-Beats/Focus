package com.ihewro.focus.task;

import android.os.AsyncTask;
import android.util.Log;

import com.blankj.ALog;
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.FeedFolder;
import com.ihewro.focus.bean.FeedRequest;
import com.ihewro.focus.bean.Message;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.callback.RequestDataCallback;
import com.ihewro.focus.http.HttpInterface;
import com.ihewro.focus.http.RetrofitManager;
import com.ihewro.focus.util.DateUtil;
import com.ihewro.focus.util.FeedParser;
import com.ihewro.focus.util.StringUtil;
import com.ihewro.focus.util.ThreadUtil;
import com.ihewro.focus.util.UIUtil;

import org.litepal.LitePal;

import java.io.IOException;
import java.text.SimpleDateFormat;

import retrofit2.Call;
import retrofit2.Response;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/04/08
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RequestFeedListDataTask extends AsyncTask<Feed, Integer, Message> {


    private RequestDataCallback callback;


    RequestFeedListDataTask(RequestDataCallback callback) {
        this.callback = callback;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Message doInBackground(Feed... feeds) {

        boolean flag = true;
        if (!UIUtil.isWifi() && UserPreference.queryValueByKey(UserPreference.notWifi,"0").equals("1")){
            flag = false;//当前不是wifi并且开启了仅在WiFi下请求数据
        }

        if (flag){
            Feed feed = feeds[0];
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Log.e("多线程任务！！", "任务开始: " + feed.getUrl() + ", 当前时间" + DateUtil.getNowDateStr());
            String url = feed.getUrl();

            FeedFolder feedFolder = LitePal.find(FeedFolder.class,feed.getFeedFolderId());
            final String originUrl = url;

            //判断RSSHUB的源地址，替换成现在的地址
            for (int i = 0; i< GlobalConfig.rssHub.size(); i++){
                if (url.contains(GlobalConfig.rssHub.get(i))){//需要根据用户的设置替换rss源
                    String replace;
                    if (!StringUtil.trim(feed.getRsshub()).equals("") && !StringUtil.trim(feed.getRsshub()).equals(UserPreference.DEFAULT_RSSHUB)){
                        replace = feed.getRsshub();
                    }else {
                        if (!StringUtil.trim(feedFolder.getRsshub()).equals("") && !StringUtil.trim(feed.getRsshub()).equals(UserPreference.DEFAULT_RSSHUB)){
                            replace = feedFolder.getRsshub();
                        }else {
                            replace = UserPreference.getRssHubUrl();
                        }
                    }

                    url = url.replace(GlobalConfig.rssHub.get(i),replace);
                    ALog.d(originUrl + " 替换自定义源后结果：" + url);
                  break;
                }
            }

            Call<String> call;

            if (url.charAt(url.length() -1) == '/'){//去掉末尾的/
                url = url.substring(0,url.length()-1);
            }
            //比如https://www.dreamwings.cn/feed，with值就是feed,url最后就是根域名https://www.dreamwings.cn
            int pos0 = url.indexOf(".");


            int pos1 = url.indexOf("/",8);
            if (pos1 == -1){//说明这/是协议头的,比如https://www.ihewro.com
                url = url + "/";
                call = RetrofitManager.create(HttpInterface.class).getRSSData(url);
            }else {
                call = RetrofitManager.create(HttpInterface.class).getRSSDataWith(url);
            }


/*            Retrofit retrofit = HttpUtil.getRetrofit("String", url, timeout, timeout, timeout);
            HttpInterface request = retrofit.create(HttpInterface.class);
            call = request.getRSSData();*/


            try {
                Response<String> response = call.execute();
                if (response != null && response.isSuccessful()){
                    feed.setErrorGet(false);
                    feed.save();
                    //处理数据库的时候我们进行同步处理
                    synchronized(this){
                        Feed feed2 = FeedParser.HandleFeed(feed.getId(), response, originUrl);
                        // feed更新到当前的时间流中
                        if (feed2!=null){
                            return new Message(true,feed2.getFeedItemList());
                        }else {
                            ALog.d("解析失败");

                            ThreadUtil.INSTANCE.runOnUIThread(() -> callback.onFailure("服务数据解析内容为空"));

                            //当前解析的内容为空
                            return new Message(false);
                        }
                    }

                }else {
                    String reason;
                    if (response.errorBody()!=null){
                        reason = response.errorBody().string();
                        //编码转换
                        //获取xml文件的编码
                        String encode = "UTF-8";//默认编码
//                        reason = new String(reason.getBytes("ISO-8859-1"),encode);
                    }else {
                        reason = "接口请求失败无错误原因";
                        ALog.d("出问题了！");
                    }

                    FeedRequest feedRequire = new FeedRequest(feed.getId(),false,0,reason,response.code(), DateUtil.getNowDateRFCInt());
                    feedRequire.save();
                    feed.setErrorGet(true);
                    feed.save();

                    ALog.d("请求失败 ----> %d : %s ", response.code(), reason);
                    ThreadUtil.INSTANCE.runOnUIThread(() -> callback.onFailure(reason));
                    return new Message(false);
                }
            }  catch (IOException e) {
                ALog.e("请求失败", e);
                ThreadUtil.INSTANCE.runOnUIThread(() -> callback.onFailure(e.getMessage()));
                FeedRequest feedRequire = new FeedRequest(feed.getId(),false,0,e.getMessage(),-1, DateUtil.getNowDateRFCInt());
                feedRequire.save();

                feed.setErrorGet(true);
                feed.save();
            }
        }


        return new Message(false);
    }



    @Override
    protected void onPostExecute(Message message) {
        super.onPostExecute(message);
        //这个地方不需要返回了，都已经保存到本地数据库了
        callback.onSuccess(message.getFeedItemList());
    }
}
