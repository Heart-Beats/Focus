package com.ihewro.focus.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.ihewro.focus.R
import com.ihewro.focus.bean.CollectionAndFolderRelation
import com.ihewro.focus.bean.CollectionFolder
import com.ihewro.focus.bean.EventMessage
import com.ihewro.focus.bean.Help
import com.ihewro.focus.bean.Operation
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal

/**
 * <pre>
 * author : hewro
 * e-mail : ihewro@163.com
 * time   : 2019/07/03
 * desc   :
 * version: 1.0
</pre> *
 */
class CollectionFolderOperationPopupView(context: Context, id: Long, title: String?, subtitle: String?, help: Help?) :
	OperationBottomPopupView(context, null, title, subtitle, help) {
	private var folder: CollectionFolder? = null

	init {
		this.operationList = getFeedFolderOperationList(id)
	}


	@SuppressLint("CheckResult")
	private fun getFeedFolderOperationList(id: Long): List<Operation> {
		folder = LitePal.find(CollectionFolder::class.java, id)
		val operations: MutableList<Operation> = ArrayList()

		val editName = Operation("重命名", "", resources.getDrawable(R.drawable.ic_rate_review_black_24dp), folder) { o -> // 修改名称的弹窗
			// 对文件夹进行重命名

			val finalO: CollectionFolder = o as CollectionFolder
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
					EventBus.getDefault().post(EventMessage(EventMessage.COLLECTION_FOLDER_OPERATION))
					dismiss()
				}
			}
		}

		val delete =
			Operation("删除", "", resources.getDrawable(R.drawable.ic_exit_to_app_black_24dp), folder) {
				MaterialDialog(context).show {
					title(text = "操作通知")
					message(text = "确实删除该收藏分类吗？确定会删除该分类下的所有收藏内容")
					positiveButton(text = "确定") {
						// 1.删除该文件夹下的所有feedITEN
						LitePal.deleteAll(CollectionAndFolderRelation::class.java, "collectionfolderid = ?", id.toString())

						// 2.删除文件夹
						LitePal.delete(CollectionFolder::class.java, id)
						Toasty.success(context, "删除成功").show()
						EventBus.getDefault().post(EventMessage(EventMessage.COLLECTION_FOLDER_OPERATION, id.toInt()))
						dismiss()
					}
					negativeButton(text = "取消")
				}
			}

		operations.add(editName)
		operations.add(delete)
		return operations
	}
}
