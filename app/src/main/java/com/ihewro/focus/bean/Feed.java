package com.ihewro.focus.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/03/09
 *     desc   : 某个订阅，比如微博的用户动态订阅
 *     version: 1.0
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed extends LitePalSupport {

    public static final int DEFAULT_TIMEOUT = 25;

    @Column(unique = true)
    private int id;//真实主键

    @Column(ignore = true)
    private String iid;//这个参数是因为服务端主键是字符串，而lietepal 固定主键为int的id，这个字段需要保留，以便获取服务器上对应的参数列表，该字段仅仅在获取参数列表使用，其他任何使用禁止使用！

    private String name;
    private String desc;

    @Column(unique = true)
    private String url;//feed的URL，包括参数
    private String link;// 订阅指向的网站
    private String websiteName;
    private String websiteCategoryName;
    private Long time;
    private int feedFolderId;//feed文件夹id
    private String type;//属于哪个来源，比如本地或者别的第三方平台

    private double orderValue;//顺序权限，用来排序的
    private String rsshub;//源地址
    private String password;//密码


    @Column(ignore = true)
    private int totalNum;//总文章数
    @Column(ignore = true)
    private int unreadNum;//未读文章数

    private String logoPath;//feed的图标路径

    @Column(ignore = true)
    private List<FeedItem> feedItemList = new ArrayList<>();
    @Column(ignore = true)
    private List<FeedRequire> feedRequireList = new ArrayList<>();//当前feed所需要的参数

    @Column(ignore = true)
    private String extra;//feed的订阅参数额外信息，只会在在线订阅的时候会使用到该参数

    private int timeout;//feed拉取的超时时间

    private boolean errorGet;//获取信息失败

    private boolean isBadGuy;//是否需要反盗链图片
    private boolean isChina;//是否需要反盗链图片
    private boolean isOffline;//同步的时候是否请求数据

    @Column(ignore = true)
    private String icon;//图标

    @Override
    public String toString() {
        return "Feed{" +
                "feedId=" + id +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", 文章数目 ='" + feedItemList.size() + '\'' +
                ", feedItemList=" + feedItemList +
                '}';
    }

    public Feed(String name, String desc, String url, String link, String websiteName, String websiteCategoryName, Long time, int feedFolderId, String type, int timeout, boolean errorGet) {
        this.name = name;
        this.desc = desc;
        this.url = url;
        this.link = link;
        this.websiteName = websiteName;
        this.websiteCategoryName = websiteCategoryName;
        this.time = time;
        this.feedFolderId = feedFolderId;
        this.type = type;
        this.timeout = timeout;
        this.errorGet = errorGet;
    }

    public Feed() {
    }

    public Feed(String name, String url, String desc, int timeout) {
        this.name = name;
        this.url = url;
        this.desc = desc;
        this.timeout = timeout;
    }




    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFeedFolderId() {
        return feedFolderId;
    }

    public void setFeedFolderId(int feedFolderId) {
        this.feedFolderId = feedFolderId;
    }

    public String getIid() {//该字段仅仅在获取参数列表使用，其他任何使用禁止使用！
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public int getUnreadNum() {
        return unreadNum;
    }

    public void setUnreadNum(int unreadNum) {
        this.unreadNum = unreadNum;
    }

    public List<FeedRequire> getFeedRequireList() {
        return feedRequireList;
    }

    public void setFeedRequireList(List<FeedRequire> feedRequireList) {
        this.feedRequireList = feedRequireList;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FeedItem> getFeedItemList() {
        return feedItemList;
    }

    public void setFeedItemList(List<FeedItem> feedItemList) {
        this.feedItemList = feedItemList;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getWebsiteName() {
        return websiteName;
    }

    public void setWebsiteName(String websiteName) {
        this.websiteName = websiteName;
    }

    public String getWebsiteCategoryName() {
        return websiteCategoryName;
    }

    public void setWebsiteCategoryName(String websiteCategoryName) {
        this.websiteCategoryName = websiteCategoryName;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isErrorGet() {
        return errorGet;
    }

    public void setErrorGet(boolean errorGet) {
        this.errorGet = errorGet;
    }

    public double getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(double orderValue) {
        this.orderValue = orderValue;
    }

    public String getRsshub() {
        return rsshub;
    }

    public void setRsshub(String rsshub) {
        this.rsshub = rsshub;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isBadGuy() {
        return isBadGuy;
    }

    public void setBadGuy(boolean badGuy) {
        isBadGuy = badGuy;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isChina() {
        return isChina;
    }

    public void setChina(boolean china) {
        isChina = china;
    }
}
