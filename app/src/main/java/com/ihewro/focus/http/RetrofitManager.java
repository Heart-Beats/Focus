package com.ihewro.focus.http;

import android.util.Log;

import com.ihewro.focus.util.HttpsUtil;
import com.ihewro.focus.util.Tls12SocketFactory;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/07/02
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RetrofitManager {

    private static Retrofit retrofit = null;

    private static class Holder {
        private static RetrofitManager instance = new RetrofitManager();
    }

    private RetrofitManager() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://www.baidu.com")
                .client(getOkHttpClient(30, 30, 30))
                .addConverterFactory(SmartConverterFactory.Companion.create())  // retrofit已经把Json解析封装在内部了 你需要传入你想要的解析工具就行了
                .build();
    }

    /**
     * 自定义okhttp拦截器，以便能够打印请求地址、请求头等请求信息
     * @param readTimeout 单位s
     * @param writeTimeout 单位s
     * @param connectTimeout 单位s
     * @return OkHttpClient
     */
    private OkHttpClient getOkHttpClient(int readTimeout, int writeTimeout, int connectTimeout) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
            // 打印retrofit日志
            Log.d("RetrofitLog", message);
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(100);
        dispatcher.setMaxRequests(100);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .dispatcher(dispatcher)
                .readTimeout(readTimeout, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true);// 设置连接超时时间

        // if (type.equals("String")){
        //     ALog.d("添加了编码了");
        //     builder.addInterceptor(new EncodingInterceptor("ISO-8859-1"));//全部转换成这个编码
        // }

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            try {
                sslContext.init(null, null, null);
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        SSLSocketFactory socketFactory = new Tls12SocketFactory(sslContext.getSocketFactory());
        builder.sslSocketFactory(socketFactory,new HttpsUtil.UnSafeTrustManager());

        OkHttpClient client = builder.build();
      /*  client.dispatcher().setMaxRequests(100);//最大并发数
        client.dispatcher().setMaxRequestsPerHost(100);//单域名的并发数*/

        return client;
    }

    public static <T> T create(Class<T> clz) {
        return Holder.instance.retrofit.create(clz);
    }
}
