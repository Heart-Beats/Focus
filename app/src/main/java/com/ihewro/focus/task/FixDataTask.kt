package com.ihewro.focus.task

import android.app.Activity
import android.os.AsyncTask
import android.widget.ProgressBar
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.blankj.ALog
import com.ihewro.focus.bean.Collection
import com.ihewro.focus.bean.CollectionAndFolderRelation
import com.ihewro.focus.bean.CollectionFolder
import com.ihewro.focus.bean.EventMessage
import com.ihewro.focus.bean.FeedItem
import com.ihewro.focus.util.DateUtil
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal.deleteAll
import org.litepal.LitePal.where
import org.litepal.exceptions.LitePalSupportException

/**
 * <pre>
 * author : hewro
 * e-mail : ihewro@163.com
 * time   : 2019/07/03
 * desc   : 修复数据库功能
 * 功能：删除错误数据（收藏关系表中id为<=0的数据）、导入以前的feeditem里面的收藏数据、删除多余的collection内容（在关系表中不存在该collection）
 * version: 1.0
</pre> *
 */
class FixDataTask(private val activity: Activity) : AsyncTask<Void, Void, Boolean>() {

	private var loading: MaterialDialog? = null


	override fun onPreExecute() {
		loading = MaterialDialog(activity).show {
			message(text = "马上就好……")
			customView(view = ProgressBar(activity))
		}
	}

	override fun doInBackground(vararg voids: Void): Boolean {
		val collectionFolder = CollectionFolder("旧版本数据迁移")
		try {
			collectionFolder.saveThrows()
		} catch (e: LitePalSupportException) {
			ALog.d(e)
			collectionFolder.id = where("name = ?", collectionFolder.name).find(CollectionFolder::class.java)[0].id
		}

		val feedItems = where("favorite = ?", "1").find(FeedItem::class.java) // 所有收藏的文章
		for (feedItem in feedItems) {
			val collection = Collection(
				feedItem.title, feedItem.feedName, feedItem.date, feedItem.summary,
				feedItem.content, feedItem.url, feedItem.feedUrl, feedItem.guid, Collection.FEED_ITEM, DateUtil.getNowDateRFCInt()
			)

			try {
				collection.saveThrows()
			} catch (e: LitePalSupportException) {
				ALog.d(e)
				collection.id = where("url = ?", collection.url).find(Collection::class.java)[0].id
			}


			// 查询数据库是否有这样的关系，如果有就不需要存储的了
			val temp = where(
				"collectionid = ? and collectionfolderid = ?",
				collection.id.toString() + "", collectionFolder.id.toString() + ""
			).find(
				CollectionAndFolderRelation::class.java
			)

			// 保存新的关系
			if (temp.size == 0) {
				val collectionAndFolderRelation = CollectionAndFolderRelation(collection.id, collectionFolder.id)
				collectionAndFolderRelation.save()
			}
		}


		// 删除错误数据
		deleteAll(CollectionAndFolderRelation::class.java, "collectionid = ? or collectionfolderid = ?", "0", "0")

		return true
	}

	override fun onPostExecute(aBoolean: Boolean) {
		// 显示成功

		if (loading!!.isShowing) {
			loading!!.dismiss()
			Toasty.success(activity!!, "修复成功").show()
			EventBus.getDefault().post(EventMessage(EventMessage.DATABASE_RECOVER))
		}
	}
}
