package com.ihewro.focus.task

import android.app.Activity
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.AsyncTask
import android.os.Environment
import android.widget.ProgressBar
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.files.FileFilter
import com.afollestad.materialdialogs.files.fileChooser
import com.afollestad.materialdialogs.list.customListAdapter
import com.blankj.ALog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ihewro.focus.GlobalConfig
import com.ihewro.focus.bean.Collection
import com.ihewro.focus.bean.CollectionAndFolderRelation
import com.ihewro.focus.bean.CollectionFolder
import com.ihewro.focus.bean.EventMessage
import com.ihewro.focus.bean.Feed
import com.ihewro.focus.bean.FeedFolder
import com.ihewro.focus.bean.FeedItem
import com.ihewro.focus.bean.UserPreference
import com.ihewro.focus.helper.DatabaseHelper
import com.ihewro.focus.util.DataCleanManager
import com.ihewro.focus.util.DataUtil
import com.ihewro.focus.util.DateUtil
import com.ihewro.focus.util.StringUtil
import com.ihewro.focus.util.UIUtil
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import java.io.File


/**
 * <pre>
 * author : hewro
 * e-mail : ihewro@163.com
 * time   : 2019/05/19
 * desc   :
 * version: 1.0
</pre> *
 */
class RecoverDataTask(private val activity: Activity) : AsyncTask<Void, Void, Boolean>() {

	private val backupFilesPath: MutableList<String> = ArrayList()
	private val backupFilesName: MutableList<String> = ArrayList()

	init {
		val helper = DatabaseHelper(activity, "focus")
		val db: SQLiteDatabase = helper.readableDatabase
		db.disableWriteAheadLogging() // 禁用WAL模式
	}

	override fun doInBackground(vararg params: Void): Boolean {
		val dir: File = File(GlobalConfig.appDirPath + "database")
		val autoDir: File = File(GlobalConfig.appDirPath + "database/autobackup")

		if (!dir.exists() || !dir.isDirectory) {
			dir.mkdir()
			return false
		} else {
			var backupFiles = dir.listFiles()
			for (backupFile in backupFiles) {
				if (backupFile.isFile) {
					backupFilesPath.add(GlobalConfig.appDirPath + "database/" + backupFile.name)
					backupFilesName.add(backupFile.name + "(" + DataCleanManager.getFormatSize(backupFile.length().toDouble()) + ")")
				}
			}

			if (!autoDir.exists() || !autoDir.isDirectory) {
				autoDir.mkdir()
			} else {
				backupFiles = autoDir.listFiles()
				for (autoBackupFile in backupFiles) {
					ALog.d(autoBackupFile.name + autoBackupFile.absolutePath)
					if (autoBackupFile.isFile) {
						backupFilesPath.add(GlobalConfig.appDirPath + "database/autobackup/" + autoBackupFile.name)
						backupFilesName.add(
							autoBackupFile.name + "(" + DataCleanManager.getFormatSize(
								autoBackupFile.length().toDouble()
							) + ")"
						)
					}
				}
			}
			return true
		}
	}


	override fun onPostExecute(aVoid: Boolean) {
		super.onPostExecute(aVoid)
		if (aVoid) {
			val backupFileAdapter = object :
				BaseQuickAdapter<String, BaseViewHolder>(com.afollestad.materialdialogs.R.layout.md_listitem, backupFilesName) {

				override fun convert(helper: BaseViewHolder, item: String) {
					helper.setText(com.afollestad.materialdialogs.R.id.md_title, item)
				}
			}

			backupFileAdapter.setOnItemClickListener { adapter, view, position ->
				MaterialDialog(activity).show {
					title(text = "确认恢复此备份吗？")
					message(text = "一旦恢复数据后，无法撤销操作。但是你可以稍后继续选择恢复其他备份文件")
					positiveButton(text = "确定") {
						val dialog1 = MaterialDialog(activity).show {
							message(text = "马上就好……")
							this.customView(view = ProgressBar(activity))
						}

						Thread {
							importData(backupFilesPath[position])
							UIUtil.runOnUiThread(activity) {
								dialog1.dismiss()
								Toasty.success(activity, "恢复数据成功！", Toast.LENGTH_SHORT).show()
								EventBus.getDefault().post(EventMessage(EventMessage.DATABASE_RECOVER))
							}
						}.start()
					}
					negativeButton(text = "取消")
				}
			}

			backupFileAdapter.setOnItemLongClickListener { adapter, _, position ->
				MaterialDialog(activity).show {
					title(text = "确认删除此备份吗？")
					message(text = "该操作无法撤销，删除前先确定你不需要该备份数据了")
					positiveButton(text = "确定") {
						val file = File(backupFilesPath[position])
						if (file.exists()) {
							file.delete()
						}
						Toasty.success(activity, "删除该备份成功", Toast.LENGTH_SHORT).show()
						adapter.remove(position)
					}
					negativeButton(text = "取消")
				}
				true
			}

			MaterialDialog(activity).show {
				title(text = "备份列表")
				message(text = "单击恢复数据，长按删除备份")
				customListAdapter(backupFileAdapter)
				neutralButton(text = "导入备份") {
					MaterialDialog(activity).show {
						val initialFolder = Environment.getExternalStorageDirectory() // 初始显示了目录
						val myFilter: FileFilter = { it.extension == "db" } // 选择的文件类型
						fileChooser(activity, initialDirectory = initialFolder, filter = myFilter) { _, file ->
							// File selected
							// todo
						}
					}
				}
			}
		} else {
			Toasty.info(activity, "暂无任何备份文件，请先备份数据", Toast.LENGTH_SHORT).show()
		}
	}


	private fun importData(path: String) {
		// 子线程
		val database: SQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(path, null)
		database.disableWriteAheadLogging()

		// 删除数据库
		LitePal.deleteAll(FeedFolder::class.java)
		LitePal.deleteAll(Feed::class.java)
		LitePal.deleteAll(FeedItem::class.java)
		LitePal.deleteAll(UserPreference::class.java)
		LitePal.deleteAll(Collection::class.java)
		LitePal.deleteAll(CollectionFolder::class.java)
		LitePal.deleteAll(CollectionAndFolderRelation::class.java)

		// TODO: 对于新加入的字段的恢复
		val feedFolderCur: Cursor = database.rawQuery("SELECT * from feedfolder", null)
		if (feedFolderCur.moveToFirst()) {
			do {
				val feedFolder: FeedFolder = FeedFolder()
				val name = feedFolderCur.getString(feedFolderCur.getColumnIndexOrThrow("name"))
				val rsshub = DataUtil.getColumnString(feedFolderCur, "rsshub", UserPreference.DEFAULT_RSSHUB)
				val timeout = DataUtil.getColumnInt(feedFolderCur, "timeout", Feed.DEFAULT_TIMEOUT)
				val feedfolderId = feedFolderCur.getInt(feedFolderCur.getColumnIndexOrThrow("id"))

				feedFolder.name = name
				feedFolder.rsshub = rsshub
				feedFolder.timeout = timeout
				feedFolder.save()

				val feedCur: Cursor = database.rawQuery("select * from feed where feedfolderid = ?", arrayOf("" + feedfolderId))
				if (feedCur.moveToFirst()) {
					do {
						val name2 = feedCur.getString(feedCur.getColumnIndexOrThrow("name"))
						val desc = feedCur.getString(feedCur.getColumnIndexOrThrow("desc_lpcolumn"))
						val url = feedCur.getString(feedCur.getColumnIndexOrThrow("url"))
						val link = feedCur.getString(feedCur.getColumnIndexOrThrow("link"))
						val websiteName = feedCur.getString(feedCur.getColumnIndexOrThrow("websitename"))
						val websiteCategoryName = feedCur.getString(feedCur.getColumnIndexOrThrow("websitecategoryname"))
						val time = feedCur.getLong(feedCur.getColumnIndexOrThrow("time"))
						val feedFolderId: Int = feedFolder.id
						val type = feedCur.getString(feedCur.getColumnIndexOrThrow("type"))
						val timeout2 = feedCur.getInt(feedCur.getColumnIndexOrThrow("timeout"))
						var temp = feedCur.getString(feedCur.getColumnIndexOrThrow("errorget"))
						val errorGet = StringUtil.trim(temp) == "1"
						val rsshub2 = DataUtil.getColumnString(feedCur, "rsshub", UserPreference.DEFAULT_RSSHUB)
						val feedId = feedCur.getInt(feedCur.getColumnIndexOrThrow("id"))
						val feed: Feed = Feed(
							name2,
							desc,
							url,
							link,
							websiteName,
							websiteCategoryName,
							time,
							feedFolderId,
							type,
							timeout2,
							errorGet
						)
						feed.rsshub = rsshub2
						feed.save()

						// 绑定该feed下面的所有文章
						val feedItemCur: Cursor =
							database.rawQuery("SELECT * from feeditem where feedid = ?", arrayOf(feedId.toString() + ""))
						if (feedItemCur.moveToFirst()) {
							if (feedItemCur.moveToFirst()) {
								do {
									val title = feedItemCur.getString(feedItemCur.getColumnIndexOrThrow("title"))
									val date = feedItemCur.getLong(feedItemCur.getColumnIndexOrThrow("date"))
									val summary = feedItemCur.getString(feedItemCur.getColumnIndexOrThrow("summary"))
									val content = feedItemCur.getString(feedItemCur.getColumnIndexOrThrow("content"))
									val feedId2: Int = feed.id
									val feedName = feedItemCur.getString(feedItemCur.getColumnIndexOrThrow("feedname"))
									val url2 = feedItemCur.getString(feedItemCur.getColumnIndexOrThrow("url"))
									val feedUrl = feedItemCur.getString(feedItemCur.getColumnIndexOrThrow("feedurl"))
									val guid = feedItemCur.getString(feedItemCur.getColumnIndexOrThrow("guid"))
									temp = StringUtil.trim(feedItemCur.getString(feedItemCur.getColumnIndexOrThrow("read")))
									val read = temp == "1"
									temp = StringUtil.trim(feedItemCur.getString(feedItemCur.getColumnIndexOrThrow("favorite")))
									val favorite = temp == "1"
									val feedItem =
										FeedItem(title, date, summary, content, feedId2, feedName, url2, feedUrl, guid, read, favorite)
									feedItem.save()
								} while (feedItemCur.moveToNext())
							}
							feedItemCur.close()
						}
					} while (feedCur.moveToNext())
					feedCur.close()
				}
			} while (feedFolderCur.moveToNext())
			feedFolderCur.close()


			// 恢复用户设置表
			val userCur: Cursor = database.rawQuery("select * from userpreference", null)
			if (userCur.moveToFirst()) {
				do {
					val key = userCur.getString(userCur.getColumnIndexOrThrow("key"))
					val value = userCur.getString(userCur.getColumnIndexOrThrow("value"))
					var defaultValue: String? = ""
					try { // 早期表没有这个字段
						defaultValue = userCur.getString(userCur.getColumnIndexOrThrow("defalutvalue"))
					} catch (e: IllegalStateException) {
						ALog.d(e)
					}
					val userPreference = UserPreference(key, value, defaultValue)
					userPreference.save()
				} while (userCur.moveToNext())
				userCur.close()
			}


			val collectionIdMap = HashMap<Int, Int?>()

			// 恢复用户收藏表
			val collectionCur: Cursor
			try {
				collectionCur = database.rawQuery("select * from collection", null)

				if (collectionCur.moveToFirst()) {
					do {
						val id = DataUtil.getColumnInt(collectionCur, "id", 0)
						val title = collectionCur.getString(collectionCur.getColumnIndexOrThrow("title"))
						val date = collectionCur.getLong(collectionCur.getColumnIndexOrThrow("date"))
						val summary = collectionCur.getString(collectionCur.getColumnIndexOrThrow("summary"))
						val content = collectionCur.getString(collectionCur.getColumnIndexOrThrow("content"))
						val feedName = collectionCur.getString(collectionCur.getColumnIndexOrThrow("feedName"))
						val url2 = collectionCur.getString(collectionCur.getColumnIndexOrThrow("url"))
						val feedUrl = collectionCur.getString(collectionCur.getColumnIndexOrThrow("feedurl"))
						val guid = collectionCur.getString(collectionCur.getColumnIndexOrThrow("guid"))
						val time = DataUtil.getColumnLong(collectionCur, "time", DateUtil.getNowDateRFCInt())
						val itemType = DataUtil.getColumnInt(collectionCur, "itemtype", Collection.FEED_ITEM)
						val collection = Collection(title, feedName, date, summary, content, url2, feedUrl, guid, itemType, time)
						collection.save()
						collectionIdMap[id] = collection.id
					} while (collectionCur.moveToNext())
					collectionCur.close()
				}


				val collectionFolderIdMap = HashMap<Int, Int?>()


				// 恢复用户收藏分类表
				val collectionFolderCur: Cursor = database.rawQuery("select * from collectionfolder", null)
				if (collectionFolderCur.moveToFirst()) {
					do {
						val id = DataUtil.getColumnInt(collectionFolderCur, "id", 0)

						val name = DataUtil.getColumnString(collectionFolderCur, "name", "")
						val collectionFolder = CollectionFolder(name)
						collectionFolder.save()

						collectionFolderIdMap[id] = collectionFolder.id
					} while (collectionFolderCur.moveToNext())
					collectionFolderCur.close()
				}

				// 恢复用户收藏分类关系表
				val collectionAndFolderRelationshipCur: Cursor = database.rawQuery("select * from collectionandfolderrelation", null)
				if (collectionAndFolderRelationshipCur.moveToFirst()) {
					do {
						var collectionId = DataUtil.getColumnInt(collectionAndFolderRelationshipCur, "collectionid", 1)
						var collectionFolderId = DataUtil.getColumnInt(collectionAndFolderRelationshipCur, "collectionfolderid", 1)

						if (collectionIdMap[collectionId] != null && collectionFolderIdMap[collectionFolderId] != null) {
							// 映射还原
							collectionId = collectionIdMap[collectionId]!!
							collectionFolderId = collectionFolderIdMap[collectionFolderId]!!
							val collectionAndFolderRelation = CollectionAndFolderRelation(collectionId, collectionFolderId)
							collectionAndFolderRelation.save()
						}
					} while (collectionAndFolderRelationshipCur.moveToNext())
					collectionAndFolderRelationshipCur.close()
				}
			} catch (exception: SQLiteException) {
				// 旧版本数据没有这个表
			}

			database.close()
		}
	}
}