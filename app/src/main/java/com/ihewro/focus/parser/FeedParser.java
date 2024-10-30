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
import java.nio.charset.StandardCharsets;
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
public abstract class FeedParser extends XmlParser {

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
        String encode = StandardCharsets.UTF_8.toString();// 默认编码
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

        if (!encode.equals(StandardCharsets.UTF_8.toString())) {
            ALog.d("Xml 文件非 UTF_8 编码，转换为 UTF_8 编码");
            xmlStr = new String(xmlStr.getBytes(encode), StandardCharsets.UTF_8);
        } else {
            ALog.d("Xml 文件为 UTF_8 编码，无需进行转换");
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
}
