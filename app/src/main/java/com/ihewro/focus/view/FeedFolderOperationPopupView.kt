package com.ihewro.focus.view

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.text.InputType
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.ihewro.focus.GlobalConfig
import com.ihewro.focus.R
import com.ihewro.focus.bean.EventMessage
import com.ihewro.focus.bean.Feed
import com.ihewro.focus.bean.FeedFolder
import com.ihewro.focus.bean.FeedItem
import com.ihewro.focus.bean.Help
import com.ihewro.focus.bean.Operation
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal

/**
 * <pre>
 * author : hewro
 * e-mail : ihewro@163.com
 * time   : 2019/05/13
 * desc   :
 * version: 1.0
</pre> *
 */
class FeedFolderOperationPopupView(context: Context, id: Long, title: String?, subtitle: String?, help: Help?) :
	OperationBottomPopupView(context, null, title, subtitle, help) {
	private var feedFolder: FeedFolder? = null

	init {
		this.operationList = getFeedFolderOperationList(id)
	}

	@SuppressLint("CheckResult")
	private fun getFeedFolderOperationList(id: Long): List<Operation> {
		feedFolder = LitePal.find(FeedFolder::class.java, id)
		val operations: MutableList<Operation> = ArrayList()
		operations.add(
			Operation("重命名文件夹", "", resources.getDrawable(R.drawable.ic_rate_review_black_24dp), feedFolder) { o -> // 对文件夹进行重命名
				val finalO: FeedFolder = o as FeedFolder

				MaterialDialog(context).show {
					title(text = "修改文件夹名称")
					message(text = "输入新的名称：")
					input(inputType = InputType.TYPE_CLASS_TEXT) { _, inputText ->
						val name = inputText.toString().trim()
						if (name == "") {
							Toasty.info(context, "请勿填写空名字哦😯").show()
						} else {
							finalO.name = name
							finalO.save()
						}
						Toasty.success(context, "修改成功").show()
						EventBus.getDefault().post(EventMessage(EventMessage.EDIT_FEED_FOLDER_NAME))
						dismiss()
					}
				}
			}
		)

		operations.add(
			Operation("退订文件夹", "", resources.getDrawable(R.drawable.ic_exit_to_app_black_24dp), feedFolder) { o ->
				MaterialDialog(context).show {
					title(text = "操作通知")
					message(text = "确定退订该文件夹吗？确定会退订文件夹下所有订阅")
					positiveButton(text = "确定") {
						val feedFolder: FeedFolder = (o as FeedFolder)

						// 退订文件夹的内容

						// 1.删除该文件夹下的所有feedItem
						val temp: List<Feed> = LitePal.where("feedfolderid = ?", id.toString()).find(Feed::class.java)
						for (i in temp.indices) {
							// 删除没有收藏的
							LitePal.deleteAll(
								FeedItem::class.java,
								"feedid = ? and favorite = ?",
								temp[i].id.toString(),
								"0"
							)
							// 2.删除文件夹下的所有feed
							temp[i].delete()
						}

						// 3.删除文件夹
						LitePal.delete(FeedFolder::class.java, id)
						Toasty.success(context, "退订成功").show()
						EventBus.getDefault().post(EventMessage(EventMessage.DELETE_FEED_FOLDER, id.toInt()))
						dismiss()
					}
					negativeButton(text = "取消")
				}
			}
		)

		operations.add(
			Operation("标记全部已读", "", resources.getDrawable(R.drawable.ic_radio_button_checked_black_24dp), feedFolder) { o ->
				// 显示弹窗
				MaterialDialog(context).show {
					title(text = "操作通知")
					message(text = "确定将该订阅下所有文章标记已读吗？")
					positiveButton(text = "确定") {
						val feedFolder: FeedFolder = o as FeedFolder
						// 标记全部已读
						val feedList: List<Feed> = LitePal.where("feedfolderid = ?", feedFolder.id.toString()).find<Feed>(
							Feed::class.java
						)
						for (feed in feedList) {
							val values = ContentValues()
							values.put("read", "1")
							LitePal.updateAll(FeedItem::class.java, values, "feedid = ?", feed.id.toString())
						}
						Toasty.success(context, "标记成功").show()
						EventBus.getDefault().post(EventMessage(EventMessage.MARK_FEED_FOLDER_READ, id.toInt()))
						dismiss()
					}
					negativeButton(text = "取消")
				}
			}
		)


		operations.add(
			Operation("设置超时时间", "", resources.getDrawable(R.drawable.ic_timer_black_24dp), feedFolder) { o ->
				val item: FeedFolder = o as FeedFolder

				MaterialDialog(context).show {
					title(text = "设置超时时间")
					message(text = "单位是秒，与每个订阅的的超时时间取最大值：")
					input(
						hint = item.timeout.toString(),
						prefill = item.timeout.toString(),
						inputType = InputType.TYPE_CLASS_TEXT
					) { _, inputText ->
						val timeout = inputText.toString().trim()
						if (timeout == "") {
							Toasty.info(context, "请勿为空😯").show()
						} else {
							item.timeout = timeout.toInt()
							item.save()
							Toasty.success(context, "设置成功").show()
							EventBus.getDefault().post(EventMessage(EventMessage.EDIT_FEED_NAME))
							dismiss()
						}
					}
				}
			}
		)

		// operations.add( Operation("分享该文件夹","",getResources().getDrawable(R.drawable.ic_share_black_24dp_grey),feedFolder) { o ->
		// 	val item: FeedFolder = o as FeedFolder
		//
		// 	MaterialDialog(context).show {
		// 		title(text = "填写分享名称")
		// 		message(text = "填写一个独一无二的名称吧，彰显个性，方便搜索")
		// 		input(
		// 			hint = item.timeout.toString(),
		// 			prefill = item.timeout.toString(),
		// 			inputType = InputType.TYPE_CLASS_TEXT
		// 		) { _, inputText ->
		// 			val timeout = inputText.toString().trim()
		// 			if (timeout == "") {
		// 				Toasty.info(context, "请勿为空😯").show()
		// 			} else {
		// 				//上传到服务器
		//
		// 				dismiss()
		// 			}
		// 		}
		// 	}
		// })

		operations.add(
			Operation("设置rsshub源", "", resources.getDrawable(R.drawable.ic_autorenew_black_24dp_night_grey), feedFolder) { o ->
				val item: FeedFolder = o as FeedFolder
				var select: Int = GlobalConfig.feedRssHub.indexOf(item.rsshub)
				if (select == -1) {
					select = GlobalConfig.feedRssHub.size - 1 // 跟随主设置
				}
				MaterialDialog(context).show {
					title(text = "源设置")
					listItemsSingleChoice(items = GlobalConfig.feedRssHub, initialSelection = select) { _, index, _ ->
						if (index in 0..3) {
							item.rsshub = GlobalConfig.feedRssHub[index]
							item.save()
						}
					}
					positiveButton(text = "选择")
				}
			}
		)

		return operations
	}
}
