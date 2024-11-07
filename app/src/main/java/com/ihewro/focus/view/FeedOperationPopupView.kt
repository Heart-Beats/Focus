package com.ihewro.focus.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
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
import com.ihewro.focus.bean.FeedItem
import com.ihewro.focus.bean.Help
import com.ihewro.focus.bean.Operation
import com.ihewro.focus.task.ShowFeedFolderListDialogTask
import com.lxj.xpopup.XPopup
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
class FeedOperationPopupView(context: Context, id: Long, title: String?, subtitle: String?, help: Help?) :
	OperationBottomPopupView(context, null, title, subtitle, help) {

	init {
		this.operationList = getFeedOperationList(id)
	}

	@SuppressLint("CheckResult")
	private fun getFeedOperationList(id: Long): List<Operation> {
		val operations = ArrayList<Operation>()
		val feed: Feed = LitePal.find(Feed::class.java, id)
		this.subtitle = feed.url
		operations.add(
			Operation("重命名", "", resources.getDrawable(R.drawable.ic_rate_review_black_24dp), feed) { o ->
				val item: Feed = o as Feed
				MaterialDialog(context).show {
					title(text = "修改订阅名称")
					message(text = "输入新的名称：")
					input(hint = item.name, prefill = item.name, inputType = InputType.TYPE_CLASS_TEXT) { _, inputText ->
						val name = inputText.toString().trim()
						if (name == "") {
							Toasty.info(context, "请勿填写空名字哦😯").show()
						} else {
							item.name = name
							item.save()
							Toasty.success(context, "修改成功").show()
							EventBus.getDefault().post(EventMessage(EventMessage.EDIT_FEED_NAME))
							dismiss()
						}
					}
				}
			}
		)

		operations.add(
			Operation("退订", "", resources.getDrawable(R.drawable.ic_exit_to_app_black_24dp), feed) { o ->
				val item: Feed = o as Feed
				MaterialDialog(context).show {
					title(text = "操作通知")
					message(text = "确定退订该订阅吗")
					positiveButton(text = "确定") {
						val id: Int = item.id
						// 先删除对应的feedITEM
						// 只删除没有收藏的
						LitePal.deleteAll(FeedItem::class.java, "feedid = ? and favorite = ?", item.id.toString(), "0")
						// 再删除feed
						LitePal.delete(Feed::class.java, id.toLong())
						Toasty.success(context, "退订成功").show()
						EventBus.getDefault().post(EventMessage(EventMessage.DELETE_FEED, id))
						dismiss()
					}
					negativeButton(text = "取消")
				}
			}
		)


		operations.add(
			Operation("标记全部已读", "", resources.getDrawable(R.drawable.ic_radio_button_checked_black_24dp), feed) { // 显示弹窗
				MaterialDialog(context).show {
					title(text = "操作通知")
					message(text = "确定将该订阅下所有文章标记已读吗？")
					positiveButton(text = "确定") {
						val values = ContentValues()
						values.put("read", "1")
						LitePal.updateAll(FeedItem::class.java, values, "feedid = ?", id.toString())
						Toasty.success(context, "操作成功").show()
						EventBus.getDefault().post(EventMessage(EventMessage.MARK_FEED_READ, id.toInt()))
						dismiss()
					}
					negativeButton(text = "取消")
				}
			}
		)


		operations.add(
			Operation("移动到其他文件夹", "", resources.getDrawable(R.drawable.ic_touch_app_black_24dp), feed) { o ->
				val item: Feed = o as Feed
				ShowFeedFolderListDialogTask({ _, _, _, _, targetId ->
					// 移动到指定的目录下
					item.feedFolderId = targetId
					item.save()
					Toasty.success(context, "移动成功").show()
					dismiss()
					EventBus.getDefault().post(EventMessage(EventMessage.MOVE_FEED))
				}, context, "移动到其他文件夹", "点击文件夹名称执行移动操作").execute()
			}
		)


		operations.add(
			Operation("复制RSS地址", "", resources.getDrawable(R.drawable.ic_content_copy_black_24dp), feed) { o ->
				val item: Feed = o as Feed
				val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
				clipboardManager.setPrimaryClip(ClipData.newPlainText(null, item.url))
				Toasty.success(context, "复制成功").show()
				dismiss()
			}
		)

		operations.add(
			Operation("修改RSS地址", "", resources.getDrawable(R.drawable.ic_touch_app_black_24dp), feed) { o ->
				val item: Feed = o as Feed
				MaterialDialog(context).show {
					title(text = "修改RSS地址")
					message(text = "输入修改后的RSS地址：")
					input(
						hint = item.url,
						prefill = item.url,
						inputType = InputType.TYPE_CLASS_TEXT
					) { _, inputText ->
						val url = inputText.toString().trim()
						if (url == "") {
							Toasty.info(context, "请勿为空😯").show()
						} else {
							item.url = url
							item.save()
							Toasty.success(context, "修改成功").show()
							EventBus.getDefault().post(EventMessage(EventMessage.EDIT_FEED_NAME))
							dismiss()
						}
					}
				}
			}
		)


		operations.add(
			Operation("设置超时时间", "", resources.getDrawable(R.drawable.ic_timer_black_24dp), feed) { o ->
				val item: Feed = o as Feed
				MaterialDialog(context).show {
					title(text = "设置超时时间")
					message(text = "单位是秒，默认25s，时间太短可能会导致部分源无法获取最新数据：")
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



		operations.add(
			Operation("显示请求记录", "", resources.getDrawable(R.drawable.ic_history_black_24dp), feed) { o ->
				val item: Feed = o as Feed
				XPopup.Builder(context)
					.enableDrag(false)
					.asCustom(
						FeedRequestPopupView(context as Activity, item.name + "请求记录", "", Help(false), feed.id)
					)
					.show()
			}
		)


		operations.add(
			Operation("设置rsshub源", "", resources.getDrawable(R.drawable.ic_autorenew_black_24dp_night_grey), feed) { o ->
				val item: Feed = o as Feed
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

		operations.add(
			Operation("图片反盗链开关", "", resources.getDrawable(R.drawable.ic_image_black_24dp), feed) { o -> // 弹框
				val isOrNot: List<Boolean> = mutableListOf(true, false)
				val isOrNotString = listOf("开启", "关闭")
				val item: Feed = o as Feed
				val select = isOrNot.indexOf(item.isBadGuy)

				MaterialDialog(context).show {
					title(text = "是否开启图片反盗链")
					message(text = "某些源（比如微信公众号）图片进行严格的反盗链机制，开启该开关可以使图片更大几率的加载")
					listItemsSingleChoice(items = isOrNotString, initialSelection = select) { _, index, _ ->
						if (index >= 0) {
							item.isBadGuy = isOrNot[index]
							item.save()
						}
					}
					positiveButton(text = "选择")
				}
			}
		)

		operations.add(
			Operation("中国模式", "", resources.getDrawable(R.drawable.ic_vpn_lock_black_24dp), feed) { o -> // 弹框
				val isOrNot: List<Boolean> = mutableListOf(true, false)
				val isOrNotString = listOf("开启", "关闭")
				val item: Feed = o as Feed
				val select = isOrNot.indexOf(item.isChina)

				MaterialDialog(context).show {
					title(text = "是否中国模式")
					message(text = "某些国外源中包含的图片无法打开，开启该开关可以大概率解决该问题（无法解决源本身无法打开的问题）")
					listItemsSingleChoice(items = isOrNotString, initialSelection = select) { _, index, _ ->
						if (index >= 0) {
							item.isChina = isOrNot[index]
							item.save()
						}
					}
					positiveButton(text = "选择")
				}
			}
		)


		operations.add(
			Operation("离线模式开关", "", resources.getDrawable(R.drawable.ic_cloud_download_black_24dp), feed) { o -> // 弹框
				val isOrNot: List<Boolean> = mutableListOf(true, false)
				val isOrNotString = listOf("离线", "在线")
				val item: Feed = o as Feed
				val select = isOrNot.indexOf(item.isOffline)

				MaterialDialog(context).show {
					title(text = "请求数据时候是否同步该订阅")
					message(text = "选择「离线」，则不会使用网络请求该订阅数据")
					listItemsSingleChoice(items = isOrNotString, initialSelection = select) { _, index, _ ->
						if (index >= 0) {
							item.isOffline = isOrNot[index]
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
