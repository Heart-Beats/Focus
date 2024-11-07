package com.ihewro.focus.task

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.text.InputType
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.ihewro.focus.bean.EventMessage
import com.ihewro.focus.bean.FeedFolder
import com.ihewro.focus.callback.DialogCallback
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal

/**
 * <pre>
 * author : hewro
 * e-mail : ihewro@163.com
 * time   : 2019/05/09
 * desc   :
 * version: 1.0
</pre> *
 */
class ShowFeedFolderListDialogTask(private val listener: DialogCallback, private val activity: Context, private val title: String, content: String) : AsyncTask<Void, Int, MutableList<String>>() {

	private var feedFolders: MutableList<FeedFolder>? = null
	private var hasContent = false

	init {
		hasContent = content.trim { it <= ' ' } != ""
	}


	@Deprecated("Deprecated in Java")
	override fun doInBackground(vararg voids: Void): MutableList<String> {
		feedFolders = LitePal.findAll(FeedFolder::class.java)
		val list: MutableList<String> = ArrayList()
		for (i in feedFolders!!.indices) {
			list.add(feedFolders!![i].name)
		}

		return list
	}

	@Deprecated("Deprecated in Java")
	override fun onPostExecute(list: MutableList<String>) {
		showDialog(list)
	}

	@SuppressLint("CheckResult")
	private fun showDialog(list: MutableList<String>) {
		MaterialDialog(activity).show {
			title(text = title)
			listItems(items = list) { dialog, index, text ->
				// 移动到指定的目录下
				listener.onFinish(dialog, view, index, text, feedFolders!![index].id)
			}

			neutralButton(text = "新增") {
				MaterialDialog(activity).show {
					title(text = "输入新增加的文件夹名称：")
					input(inputType = InputType.TYPE_CLASS_TEXT) { _, inputText ->
						// TODO:不能重命名
						val name = inputText.toString().trim()
						val feedFolder = FeedFolder(name)
						feedFolder.save()
						list.add(name)
						feedFolders!!.add(feedFolder) // 这个列表也必须添加上
						EventBus.getDefault().post(EventMessage(EventMessage.ADD_FEED_FOLDER))
						showDialog(list)
					}
				}
			}
		}
	}
}
