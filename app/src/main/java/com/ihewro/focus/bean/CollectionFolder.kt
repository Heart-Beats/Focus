package com.ihewro.focus.bean

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.ihewro.focus.callback.OperationCallback
import es.dmoral.toasty.Toasty
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport
import org.litepal.exceptions.LitePalSupportException

/**
 * <pre>
 * author : hewro
 * e-mail : ihewro@163.com
 * time   : 2019/06/23
 * desc   : 收藏文件夹表
 * version: 1.0
</pre> *
 */
class CollectionFolder( // 分类的名称
	@JvmField @field:Column(unique = true, defaultValue = "") var name: String
) : LitePalSupport() {

	@JvmField
	var id: Int = 0


	@JvmField
	@Column(defaultValue = "1.0")
	var orderValue: Double = 0.0 // 顺序权限，用来排序的

	var password: String? = null // 密码

	@Column(ignore = true)
	var isSelect: Boolean = false // 当前收藏分类是否被选择了


	companion object {

		@SuppressLint("CheckResult")
		@JvmStatic
		fun addNewFolder(context: Context, callback: OperationCallback) {

			MaterialDialog(context).show {
				title(text = "输入新增的收藏分类名称：")
				input(inputType = InputType.TYPE_CLASS_TEXT) { _, inputText ->
					// TODO:不能重命名
					val name = inputText.toString().trim()
					val collectionFolder = CollectionFolder(name)
					try {
						collectionFolder.saveThrows()
						Toasty.success(context!!, "新建成功！").show()
						callback.run(collectionFolder)
					} catch (e: LitePalSupportException) {
						// 名称重复了
						Toasty.info(context!!, "已经有该收藏分类了！").show()
					}
				}
			}
		}
	}
}
