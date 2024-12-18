package com.ihewro.focus.bean;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ihewro.focus.callback.UICallback;
import com.ihewro.focus.decoration.ISuspensionInterface;
import com.ihewro.focus.helper.SwipeInterface;
import com.ihewro.focus.util.DateUtil;
import com.ihewro.focus.view.CollectionFolderListLoadingPopupView;
import com.lxj.xpopup.XPopup;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/03/13
 *     desc   : 具体的某篇文章的数据结构
 *     version: 1.0
 * </pre>
 */
public class FeedItem extends LitePalSupport implements ISuspensionInterface, Serializable, SwipeInterface {

    @Column(unique = true)
    private int id;

    private String title;//文章标题
    private Long date;//文章发布日期

    private String summary;//文章简介
    private String content;//文章内容
    private int feedId;
    private String feedName;

    private String url;// 文章指向的链接，是用户点击该内容时会跳转的页面地址，通常是该项的永久链接

    private String feedUrl; // 订阅地址，  这里需要与 guid 一起组合作为数据库唯一约束， 因为订阅存在中英文的原因，可能会导致 guid 相同
    private String guid; // 用于唯一标识 RSS 内容项。RSS 读取器依赖 <guid> 来区分每个项是否是新的或已经处理过的，避免重复展示

    private boolean read;//是否已经阅读,true 表示已阅读
    private boolean favorite;//是否收藏，true 表示已收藏

    @Column(ignore = true)
    private boolean notHaveExtractTime;//feedItem 获取的时候，没有时间这个字段

    @Column(ignore = true)
    private boolean isBadGuy;//是否需要反盗链图片

    private boolean isChina;//是否使用中国模式


    @NonNull
    @Override
    public String toString() {
        return "title" + title + "\n"
                + "date" + DateUtil.getTimeStringByInt(date) + "\n"
                + "summary" + summary + "\n";
    }

    public FeedItem(Long date) {
        this.date = date;
    }

    public FeedItem() {
    }

    public FeedItem(String title, Long date, String summary, String content, int feedId, String feedName, String url, String feedUrl, String guid, boolean read, boolean favorite) {
        this.title = title;
        this.date = date;
        this.summary = summary;
        this.content = content;
        this.feedId = feedId;
        this.feedName = feedName;
        this.url = url;
        this.feedUrl = feedUrl;
        this.guid = guid;
        this.read = read;
        this.favorite = favorite;
    }

    public FeedItem(String title, Long date, String summary, String content, String url, String feedUrl, String guid, boolean read, boolean favorite) {
        this.title = title;
        this.date = date;
        this.summary = summary;
        this.content = content;
        this.url = url;
        this.feedUrl = feedUrl;
        this.guid = guid;
        this.read = read;
        this.favorite = favorite;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    @Override
    public boolean isShowSuspension() {
        return true;
    }

    @Override
    public String getSuspensionTag() {
        return DateUtil.getTimeStringByInt(date);//返回年/月/日格式的日期
    }

    public int getId() {
        if (!this.isSaved()){
            this.id = LitePal.where("guid = ?", this.guid).find(FeedItem.class).get(0).getId();
        }
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        FeedItem feedItem = ((FeedItem)obj);
        assert feedItem != null;
        if (this.feedUrl.equals(feedItem.getFeedUrl()) && this.guid.equals(feedItem.getGuid())) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (this.feedUrl != null && this.guid != null) {
            return this.feedUrl.hashCode() + this.guid.hashCode();
        }

        return this.id;
    }

    public boolean isNotHaveExtractTime() {
        return notHaveExtractTime;
    }

    public void setNotHaveExtractTime(boolean notHaveExtractTime) {
        this.notHaveExtractTime = notHaveExtractTime;
    }

    @Override
    public boolean save() {
        return super.save();
    }

    public static void clickWhenNotFavorite(Activity activity,Collection collection,UICallback uiCallback){
        //点击进行收藏

        new XPopup.Builder(activity)
                .asCustom(new CollectionFolderListLoadingPopupView(activity,collection,uiCallback))
                .show();

    }


    public boolean isBadGuy() {
        return isBadGuy;
    }

    public void setBadGuy(boolean badGuy) {
        isBadGuy = badGuy;
    }


    public boolean isChina() {
        return isChina;
    }

    public void setChina(boolean china) {
        isChina = china;
    }

    public static void clickWhenNotFavorite(Activity activity, FeedItem feedItem, UICallback uiCallback){
        //点击进行收藏
        Collection collection = new Collection(feedItem.getTitle(), feedItem.getFeedName(), feedItem.getDate(), feedItem.getSummary(),
                feedItem.getContent(), feedItem.getUrl(), feedItem.feedUrl, feedItem.getGuid(), Collection.FEED_ITEM, DateUtil.getNowDateRFCInt());

        clickWhenNotFavorite(activity,collection,uiCallback);
    }
}
