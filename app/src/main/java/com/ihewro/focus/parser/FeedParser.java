package com.ihewro.focus.parser;

import android.util.Xml;

import com.blankj.ALog;
import com.google.common.base.Strings;
import com.ihewro.focus.bean.Feed;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/04/07
 *     desc   : 解析xml工具
 *     version: 1.0
 * </pre>
 */
public abstract class FeedParser {

    /**
     * 从字符串解析出feed
     * @param xmlStr
     * @return[
     */
    public static Feed parseStr2Feed(String xmlStr, String url) throws UnsupportedEncodingException {
        if (Strings.isNullOrEmpty(xmlStr)) {
            return null;
        }

        //获取xml文件的编码
        String encode = "UTF-8";//默认编码
        String originCode = "ISO-8859-1";
        String temp = xmlStr.substring(0,100);
        Pattern p = Pattern.compile("encoding=\"(.*?)\"");
        Matcher m = p.matcher(temp);
        boolean flag = m.find();//【部分匹配】，返回true or false，而且指针会移动下次匹配的位置
        if (flag){
            int begin = m.start()+10;
            int end = m.end();
            encode = temp.substring(begin,end-1);
            ALog.d("编码："+encode);
        }//否则就是文件没有标明编码格式，按照utf-8进行解码


        //如果文件没有乱码，则不需要转换
        if (!Charset.forName("GBK").newEncoder().canEncode(xmlStr.substring(0, Math.min(xmlStr.length(), 3000)))) {
            xmlStr = new String(xmlStr.getBytes(originCode),encode);
        }else {
        }

        return beginParseStr2Feed(xmlStr,url);
    }


    private static Feed beginParseStr2Feed(String xmlStr, String url) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(xmlStr));

            while (parser.next() != XmlPullParser.END_TAG){
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();

                FeedParser feedParser = null;
                switch (name) {
                    case RssParser.RSS:
                        // RSS 协议
                        feedParser = new RssParser();
                        break;

                    case AtomParser.FEED:
                        // Atom 协议
                        feedParser = new AtomParser();
                        break;
                }
                if (feedParser != null) {
                    return feedParser.transformXml2Feed(parser, url);
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract Feed transformXml2Feed(XmlPullParser parser, String url) throws XmlPullParserException, IOException;

    public static void skip(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }


    /**
     * 根据tag名称获取tag内部的数据
     * @param parser
     * @param tagName
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    protected String readTagByTagName(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tagName);
        String dateStr = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tagName);
        return dateStr;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}
