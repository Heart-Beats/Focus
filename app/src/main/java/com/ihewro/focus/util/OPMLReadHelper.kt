package com.ihewro.focus.util

import android.Manifest
import android.app.Activity
import android.os.Handler
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.FileFilter
import com.afollestad.materialdialogs.files.fileChooser
import com.blankj.ALog
import com.google.common.base.Strings
import com.ihewro.focus.GlobalConfig
import com.ihewro.focus.bean.EventMessage
import com.ihewro.focus.bean.FeedFolder
import com.ihewro.focus.parser.OPMLParser
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import org.xmlpull.v1.XmlPullParserException
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
import java.io.IOException

/**
 * <pre>
 * author : hewro
 * e-mail : ihewro@163.com
 * time   : 2019/05/11
 * desc   :
 * thx: 该类大部分直接使用的feeder2 开源项目的代码OPMLHelper
 * version: 1.0
</pre> *
 */
class OPMLReadHelper(private val activity: Activity) {
	companion object {
		const val RQUEST_STORAGE_READ: Int = 8
	}


	fun run() {
		val perms = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
		if (EasyPermissions.hasPermissions(activity, *perms)) {
			// 有权限
			MaterialDialog(activity).show {
				val initialFolder = File(GlobalConfig.appDirPath) // 初始显示了目录
				val myFilter: FileFilter = { it.extension == "opml" || it.extension == "xml" } // 选择的文件类型
				fileChooser(activity, initialDirectory = initialFolder, filter = myFilter) { _, file ->
					// File selected
					add(file.absolutePath)
				}
			}
		} else {
			// 没有权限 1. 申请权限
			EasyPermissions.requestPermissions(
				PermissionRequest.Builder(activity, RQUEST_STORAGE_READ, *perms)
					.setRationale("必须读存储器才能解析OPML文件")
					.setPositiveButtonText("确定")
					.setNegativeButtonText("取消")
					.build()
			)
		}
	}

	private fun add(filePath: String) {
		if (Strings.isNullOrEmpty(filePath)) {
			return
		}
		val file = File(filePath)
		if (!file.exists()) {
			return
		}
		try {
			val feedFolders: List<FeedFolder> = OPMLParser.readOPML(filePath)
			// 保存到指定的文件夹中
			saveFeedToFeedFolder(feedFolders)

			Toasty.success(activity, "导入成功", Toast.LENGTH_SHORT).show()
		} catch (e: XmlPullParserException) {
			Toast.makeText(activity, "文件格式错误", Toast.LENGTH_SHORT).show()
		} catch (e: IOException) {
			Toast.makeText(activity, "文件导入失败", Toast.LENGTH_SHORT).show()
		}
	}

	private fun saveFeedToFeedFolder(feedFolders: List<FeedFolder>) {
		// 显示feedFolderList 弹窗

		for (i in feedFolders.indices) {
			// 首先检查这个feedFolder名称是否存在。
			val feedFolder: FeedFolder = feedFolders[i]
			val temp: List<FeedFolder> = LitePal.where("name = ?", feedFolder.name).find(FeedFolder::class.java)
			if (temp.isEmpty()) {
				if (feedFolder.name == null || feedFolder.name == "") {
					feedFolder.name = "导入的未命名文件夹"
				}
				// 新建一个feedFolder
				feedFolder.save()
			} else { // 存在这个名称
				feedFolder.id = temp[0].id // 使用是数据库的对象
			}
			// 将导入的feed全部保存
			for (feed in feedFolder.getFeedList()) {
				feed.feedFolderId = feedFolder.id
				ALog.d(feed.url)
				feed.save()
			}

			Handler().postDelayed({ EventBus.getDefault().post(EventMessage(EventMessage.IMPORT_OPML_FEED)) }, 500)
		}

		Toasty.success(activity, "导入成功！").show()
	}
}
