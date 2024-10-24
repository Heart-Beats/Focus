package com.ihewro.focus.http;

import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.FeedRequire;
import com.ihewro.focus.bean.Website;
import com.ihewro.focus.bean.WebsiteCategory;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/04/06
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public interface HttpInterface {

    @GET
    @ResponseConverter(format = ConverterFormat.STRING)
    Call<String> getRSSData(@Url String with);

 /*   @GET("{with}")
    Call<String> getRSSDataWith(@Path(value = "with", encoded = true) String with);
*/

    @GET
    @ResponseConverter(format = ConverterFormat.STRING)
    Call<String> getRSSDataWith(@Url String with);


    @GET
    Call<List<WebsiteCategory>> getCategoryList(@Url String url);

    @GET
    Call<List<Website>> getWebsiteListByCategory(@Url String url, @Query("name") String name);

    @GET
    Call<List<Feed>> getFeedListByWebsite(@Url String url, @Query("name") String name);

    @GET
    Call<List<FeedRequire>> getFeedRequireListByWebsite(@Url String url, @Query("id") String id);

    @GET
    Call<List<Feed>> searchFeedListByName(@Url String url, @Query("name") String name);

    @GET
    Call<List<Website>> searchWebsiteByName(@Url String url, @Query("name") String name);

}
