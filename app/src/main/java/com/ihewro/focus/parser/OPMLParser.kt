package com.ihewro.focus.parser

import android.util.Xml
import com.google.common.base.Strings
import com.ihewro.focus.bean.Feed
import com.ihewro.focus.bean.FeedFolder
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.FileInputStream
import java.io.IOException

/**
 * @author  张磊  on  2024/10/28 at 18:17
 * Email: 913305160@qq.com
 */
object OPMLParser : XmlParser() {

	private const val OPML: String = "opml"
	private const val BODY: String = "body"
	private const val OUTLINE: String = "outline"

	@Throws(IOException::class, XmlPullParserException::class)
	fun readOPML(filePath: String): MutableList<FeedFolder> {
		val parser = Xml.newPullParser()
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
		parser.setInput(FileInputStream(filePath), null)
		parser.nextTag()

		val feedFolders = mutableListOf<FeedFolder>()
		parser.require(XmlPullParser.START_TAG, null, OPML)
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.eventType != XmlPullParser.START_TAG) {
				continue
			}
			val name = parser.name
			// Starts by looking for the entry tag
			if (name == BODY) {
				feedFolders.addAll(readBody(parser))
			} else {
				skip(parser)
			}
		}
		return feedFolders
	}


	@Throws(IOException::class, XmlPullParserException::class)
	private fun readBody(parser: XmlPullParser): List<FeedFolder> {
		val feedFolders = mutableListOf<FeedFolder>()
		parser.require(XmlPullParser.START_TAG, null, BODY)

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.eventType != XmlPullParser.START_TAG) {
				continue
			}
			val name = parser.name
			// 解析 body 节点下每一个 outline 信息作为一个 feedFolder
			if (name == OUTLINE) {
				// 获取 outline 的 text 属性，作为 feedFolder 的名称， 有些 OMPL 文件没有 text 属性，使用 title 属性
				val text = (parser.getAttributeValue(null, "text") ?: parser.getAttributeValue(null, "title"))
				val outline = readOutline(parser, text)
				feedFolders.add(outline)
			} else {
				skip(parser)
			}
		}
		return feedFolders
	}

	@Throws(IOException::class, XmlPullParserException::class)
	private fun readOutline(parser: XmlPullParser, feedFolderName: String): FeedFolder {
		val feedFolder = FeedFolder(feedFolderName)

		val xmlUrl = parser.getAttributeValue(null, "xmlUrl")
		// 如果 outline 节点有 xmlUrl 属性，则表示是一个订阅源，没有 xmlUrl 属性，则表示是一个文件夹
		if (xmlUrl != null) {
			// 只有一个订阅源，且没有分组
			feedFolder.feedList.add(parseFeed(parser, feedFolderName))
			parser.nextTag()
		} else {
			// 是一个文件夹
			// 我们只处理二级目录
			feedFolder.feedList.addAll(readFeedFolderOutLine(parser, feedFolderName))
		}
		return feedFolder
	}


	/**
	 * 读取文件夹下的所有feed
	 * @param parser
	 * @param feedFolderName
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	@Throws(IOException::class, XmlPullParserException::class)
	private fun readFeedFolderOutLine(parser: XmlPullParser, feedFolderName: String): List<Feed> {
		val feedList = mutableListOf<Feed>()

		val xmlUrl = parser.getAttributeValue(null, "xmlUrl")
		// 如果 outline 节点有 xmlUrl 属性，则表示是一个订阅源，没有 xmlUrl 属性，则表示是一个文件夹
		if (xmlUrl != null) {
			feedList.add(parseFeed(parser, feedFolderName))
			parser.nextTag()
		} else {
			parser.require(XmlPullParser.START_TAG, null, OUTLINE)
			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.eventType != XmlPullParser.START_TAG) {
					continue
				}
				val name = parser.name
				if (name == OUTLINE) {
					feedList.addAll(readFeedFolderOutLine(parser, feedFolderName))
				} else {
					skip(parser)
				}
			}
		}
		return feedList
	}

	private fun parseFeed(parser: XmlPullParser, feedFolderName: String): Feed {
		val text = parser.getAttributeValue(null, "text")
		val title = parser.getAttributeValue(null, "title")
		val xmlUrl = parser.getAttributeValue(null, "xmlUrl")
		var htmlUrl = parser.getAttributeValue(null, "htmlUrl")


		// may be null
		val description = parser.getAttributeValue(null, "description")
		if (!Strings.isNullOrEmpty(htmlUrl)) {
			if (htmlUrl.endsWith("/")) {
				htmlUrl = htmlUrl.substring(0, htmlUrl.length - 2)
			}
		}
		val iconUrl = "$htmlUrl/favicon.ico"

		val feed = Feed(title, xmlUrl, description, Feed.DEFAULT_TIMEOUT)

		return feed
	}
}