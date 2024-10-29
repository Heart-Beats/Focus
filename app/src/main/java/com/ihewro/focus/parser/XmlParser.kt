package com.ihewro.focus.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

/**
 * @author  张磊  on  2024/10/28 at 18:29
 * Email: 913305160@qq.com
 */
open class XmlParser {

	@Throws(IOException::class, XmlPullParserException::class)
	protected fun skip(parser: XmlPullParser) {
		check(parser.eventType == XmlPullParser.START_TAG)
		var depth = 1
		while (depth != 0) {
			when (parser.next()) {
				XmlPullParser.END_TAG -> depth--
				XmlPullParser.START_TAG -> depth++
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
	@Throws(IOException::class, XmlPullParserException::class)
	protected fun readTagByTagName(parser: XmlPullParser, tagName: String?): String {
		parser.require(XmlPullParser.START_TAG, null, tagName)
		val dateStr = readText(parser)
		parser.require(XmlPullParser.END_TAG, null, tagName)
		return dateStr
	}

	@Throws(IOException::class, XmlPullParserException::class)
	private fun readText(parser: XmlPullParser): String {
		var result = ""
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.text
			parser.nextTag()
		}
		return result
	}
}