package com.ihewro.focus.util

import com.blankj.ALog
import com.ihewro.focus.bean.Feed
import com.ihewro.focus.bean.FeedItem
import com.ihewro.focus.bean.FeedRequest
import com.ihewro.focus.bean.UserPreference
import com.ihewro.focus.parser.FeedParser
import org.litepal.LitePal.where
import retrofit2.Response
import java.io.IOException

/**
 * @author  张磊  on  2024/10/24 at 11:38
 * Email: 913305160@qq.com
 */
object FeedHelper {

	@Throws(IOException::class)
	fun HandleFeed(id: Int, response: Response<String?>, originUrl: String?): Feed? {
		return HandleFeed(id, response.code(), FeedParser.parseStr2Feed(response.body(), originUrl))
	}


	/**
	 * 获取到的数据与本地数据库的数据进行比对，重复的则恢复状态信息，不重复则入库，原子操作
	 *
	 * @param responseCode
	 * @param feed
	 * @return
	 */
	@Throws(IOException::class)
	private fun HandleFeed(id: Int, responseCode: Int, feed: Feed?): Feed? {
		if (feed != null) {
			ALog.d("请求订阅获取到的 feedItem 总数：" + feed.feedItemList.size)

			val count = where("feedid = ?", id.toString()).count(FeedItem::class.java)
			// 给feed下面所有feedItem设置feedName和feedId;
			// 获取当前feed的iid
			val tempFeeds = where("url = ?", feed.url).find(Feed::class.java)
			var feedId = 0
			if (tempFeeds.size <= 0) {
				// 我们获取feedItem内容是从找数据库的feed，所以不可能feedItem中的feed url 不在数据库中欧冠
				ALog.d("出现未订阅错误" + feed.url)
			} else {
				feedId = tempFeeds[0].id
			}
			feed.id = feedId
			if (UserPreference.queryValueByKey(UserPreference.AUTO_SET_FEED_NAME, "0") == "0") { // 没有选择，自动设置会手动设置name
				feed.name = tempFeeds[0].name // 因为在线请求的时候没有拉取Titile这个字段
			}
			//        tempFeeds.get(0).setLink(feed.getLink());
			//        tempFeeds.get(0).setDesc(feed.getDesc());
			//        tempFeeds.get(0).save();
			feed.update(feedId.toLong())

			//        feed.save();//更新数据！

			// 先查询该 feedId 下库中的所有 feedItem
			val localFeedItems = where("feedid = ?", feedId.toString()).find(FeedItem::class.java)
			ALog.d("本地获取到的 feedItem 总数：" + localFeedItems.size)

			// 给feed下所有feedItem绑定feed信息
			feed.feedItemList.forEach { feedItem ->
				val optionalFeedItem =
					localFeedItems.stream().filter { localFeedItem -> localFeedItem.guid == feedItem.guid }.findFirst()

				if (!optionalFeedItem.isPresent) {
					// 本地数据库中不存在当前feed时， 当前feed存储数据库
					feedItem.feedName = feed.name
					feedItem.feedId = feedId
					feedItem.saveThrows()
				} else {
					// 当前feedItem 已经存在数据库中了，此时要对feedItem进行状态字段的覆盖
					val localFeed = optionalFeedItem.get()
					if (feedItem.date != 0L) {
						// 有的feedItem 源地址中 没有时间，所以要恢复第一次加入数据库中的时间, 只有有时间的才去更新
						localFeed.date = feedItem.date
					}
					localFeed.content = feedItem.content
					localFeed.title = feedItem.title
					localFeed.summary = feedItem.summary
					if (UserPreference.queryValueByKey(UserPreference.AUTO_SET_FEED_NAME, "0") == "1") { // 没有选择，自动设置会手动设置name
						localFeed.feedName = feed.name
					}
					localFeed.saveThrows()
				}
			}

			val count2 = where("feedid = ?", id.toString()).count(FeedItem::class.java)
			ALog.d("请求前数目：$count，请求后数目：$count2")
			val feedRequire = FeedRequest(feed.id, true, count2 - count, "", responseCode, DateUtil.getNowDateRFCInt())
			feedRequire.save()
		}

		return feed
	}
}