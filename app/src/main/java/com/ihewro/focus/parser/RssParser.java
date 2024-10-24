package com.ihewro.focus.parser;

import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.FeedItem;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.util.DateUtil;
import com.ihewro.focus.util.StringUtil;

import org.jsoup.Jsoup;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 张磊  on  2024/10/24 at 11:31
 * Email: 913305160@qq.com
 */
class RssParser extends FeedParser {

    // feed 与 item公用部分
    private static final String LINK = "link";
    private static final String DESC = "description";
    private static final String TITLE = "title";


    // feed信息
    public static final String RSS = "rss";
    private static final String CHANNEL = "channel";
    private static final String LAST_BUILD_DATE = "lastBuildDate";

    // item信息
    private static final String ITEM = "item";
    private static final String CONTENT = "content:encoded";
    private static final String PUB_DATE = "pubDate";
    private static final String GUID = "guid";

    /**
     *
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    @Override
    protected Feed transformXml2Feed(XmlPullParser parser, String url) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, RSS);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(CHANNEL)) {
                return readChannelForFeed(parser, url);
            } else {
                skip(parser);
            }
        }
        return null;
    }

    /**
     * 从channel标志开始构建feed类。
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Feed readChannelForFeed(XmlPullParser parser, String url) throws XmlPullParserException, IOException {
        Feed feed = new Feed();
        feed.setUrl(url);
        List<FeedItem> feedItems = new ArrayList<>();
        parser.require(XmlPullParser.START_TAG, null, CHANNEL);
        String netFeedName = "";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            switch (name) {
                case TITLE:
                    // 使用用户设置的title
                    netFeedName = readTitle(parser);
                    break;
                case LINK:
                    feed.setLink(readLink(parser));
                    break;
                case LAST_BUILD_DATE:
                    feed.setTime(readLastBuildDate(parser));
                    break;
                case DESC:
                    feed.setDesc(readDesc(parser));
                    break;
                case ITEM:
                    // 获取当前feed最新的文章列表
                    FeedItem feedItem = readItemForFeedItem(parser, url);
                    if (feedItem.isNotHaveExtractTime()) {
                        feedItem.setDate(feedItem.getDate() - feedItems.size() * 1000);// 越往后的时间越小，保证前面的时间大，后面的时间小
                    }
                    feedItems.add(feedItem);// 加到开头
                    break;
                default:
                    skip(parser);
                    break;
            }

        }
        if (UserPreference.queryValueByKey(UserPreference.AUTO_SET_FEED_NAME, "0").equals("1")) {// 自动通过网络设置名字
            feed.setName(netFeedName);// 因为在线请求的时候没有拉取Titile这个字段
        }
        feed.setFeedItemList(feedItems);
        feed.setWebsiteCategoryName("");


        return feed;
    }

    /**
     * 从XmlPullParser 解析出feedItem
     *
     * @param parser
     * @param url
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    private FeedItem readItemForFeedItem(XmlPullParser parser, String url) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, ITEM);
        String title = null;
        String link = null;
        String pubDate = null;
        String description = null;
        String content = null;
        String guid = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case TITLE:
                    title = readTitle(parser);
                    break;
                case LINK:
                    link = readLink(parser);
                    break;
                case PUB_DATE:
                    pubDate = readPubDate(parser);
                    break;
                case DESC:
                    description = readDesc(parser);
                    break;
                case CONTENT:
                    content = readContent(parser);
                    break;
                case GUID:
                    guid = readGUID(parser);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

        //        ALog.d("item名称：" + title + "时间为" + pubDate);
        if (StringUtil.trim(link).equals("")) {// link 与guid一定有一个是存在或者都存在，优先使用link的值
            link = guid;
        }

        if (StringUtil.trim(guid).equals("")) {// link 与guid一定有一个是存在或者都存在，优先使用link的值
            guid = link;
        }

        FeedItem feedItem = new FeedItem(title, DateUtil.date2TimeStamp(pubDate), description, content, link, url, guid, false, false);

        if (pubDate == null) {
            feedItem.setNotHaveExtractTime(true);
        }
        return feedItem;
    }


    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        return Jsoup.parse(readTagByTagName(parser, TITLE)).text();
    }

    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        return readTagByTagName(parser, LINK);
    }

    private String readPubDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        String pubData = readTagByTagName(parser, PUB_DATE);
        // TODO:对没有时间的feedItem 进行特殊处理
        if (pubData == null) {
            pubData = DateUtil.getNowDateRFCStr();
        }
        //        ALog.d("没有时间！");
        //        ALog.d(pubData);
        return pubData;
    }

    private String readDesc(XmlPullParser parser) throws IOException, XmlPullParserException {
        return readTagByTagName(parser, DESC);
    }


    private String readContent(XmlPullParser parser) throws IOException, XmlPullParserException {
        return readTagByTagName(parser, CONTENT);
    }


    private String readGUID(XmlPullParser parser) throws IOException, XmlPullParserException {
        return readTagByTagName(parser, GUID);
    }

    private Long readLastBuildDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        String dateStr = readTagByTagName(parser, LAST_BUILD_DATE);
        return DateUtil.date2TimeStamp(dateStr);
    }
}
