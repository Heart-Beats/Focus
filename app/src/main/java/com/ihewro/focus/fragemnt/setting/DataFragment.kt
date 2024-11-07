package com.ihewro.focus.fragemnt.setting

import android.annotation.SuppressLint
import android.text.InputType
import android.widget.ProgressBar
import android.widget.Toast
import androidx.preference.Preference
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import com.ihewro.focus.GlobalConfig
import com.ihewro.focus.R
import com.ihewro.focus.bean.EventMessage
import com.ihewro.focus.bean.Feed
import com.ihewro.focus.bean.FeedFolder
import com.ihewro.focus.bean.FeedItem
import com.ihewro.focus.task.FixDataTask
import com.ihewro.focus.task.RecoverDataTask
import com.ihewro.focus.util.DateUtil
import com.ihewro.focus.util.FileUtil
import com.ihewro.focus.util.StringUtil
import com.ihewro.focus.util.UIUtil
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import org.litepal.LitePal.count
import org.litepal.LitePal.getDatabase
import org.litepal.LitePal.where
import java.util.regex.Pattern

/**
 * <pre>
 * author : hewro
 * e-mail : ihewro@163.com
 * time   : 2019/05/28
 * desc   :
 * version: 1.0
</pre> *
 */
class DataFragment : SettingFragment() {
	private var back_up: Preference? = null
	private var recover_data: Preference? = null
	private var feed_info: Preference? = null
	private var clean_data: Preference? = null
	private var database_version: Preference? = null
	private var fix_database: Preference? = null


	override fun initView() {
		addPreferencesFromResource(R.xml.pref_data_setting)
	}

	override fun initPreferenceComponent() {
		feed_info = findPreference(getString(R.string.pref_key_feed_num))

		back_up = findPreference(getString(R.string.pref_key_backup))
		recover_data = findPreference(getString(R.string.pref_key_recover))
		clean_data = findPreference(getString(R.string.pref_key_clean_database))
		database_version = findPreference(getString(R.string.pref_key_database_version))

		fix_database = findPreference(getString(R.string.pref_key_fix_database))
	}

	override fun initPreferencesData() {
		feed_info!!.summary = count(FeedFolder::class.java).toString() + "个分类 " + count(Feed::class.java) + "个订阅 " + count(
			FeedItem::class.java
		) + "篇文章"
		database_version!!.summary = getDatabase().version.toString() + ""
	}

	@SuppressLint("CheckResult")
	override fun initListener() {
		fix_database!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			MaterialDialog(requireContext()).show {
				title(text = "确定修复数据库？")
				message(text = "修复数据库可以删除错误数据。如果您是从旧版本升级到2.0+版本，使用该功能可以将您的收藏数据恢复。")
				positiveButton(text = "修复") {
					FixDataTask(requireActivity()).execute()
				}
				negativeButton(text = "取消")
			}
			false
		}

		back_up!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			MaterialDialog(requireContext()).show {
				title(text = "为什么需要备份？")
				message(
					text = "本应用没有云端同步功能，本地备份功能可以尽可能保证您的数据库安全。\n" +
							"数据库备份不同于OPML导出，不仅包含所有订阅信息，还包括您的已读、收藏信息。但仅限在Focus应用内部交换使用。\n" +
							"应用会在您有任何操作数据库的操作时候自动备份最新的一次数据库。"
				)
				positiveButton(text = "开始备份") {
					// 冒号 ( :) 是文件系统中不允许的字符，特别是在 Android 的文件路径,这里替换为 "-"
					val nowDateStr = DateUtil.getNowDateStr().replace(":", "-")
					FileUtil.copyFileToTarget(
						requireContext().getDatabasePath("focus.db").absolutePath,
						GlobalConfig.appDirPath + "database/" + nowDateStr + ".db"
					) { Toasty.success(requireContext(), "备份数据成功", Toast.LENGTH_SHORT).show() }
				}
				negativeButton(text = "取消")
			}
			false
		}

		recover_data!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			RecoverDataTask(requireActivity()).execute()
			false
		}

		clean_data!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			// 显示输入弹窗
			MaterialDialog(requireContext()).show {
				title(text = "输入每个订阅要保留的数目？")
				message(text = "每个订阅将只保留该数目的文章，如果订阅的文章数目小于该数字，则不会清理")
				input(inputType = InputType.TYPE_CLASS_TEXT) { _, input ->
					val num: String = input.toString().trim()
					val pattern = Pattern.compile("^[-\\+]?[\\d]*$")
					if (pattern.matcher(num).matches() && StringUtil.trim(num) != "") {
						// 清理数据库
						// 加载对话框
						val dialog1 = MaterialDialog(requireContext()).show {
							message(text = "马上就好……")
							this.customView(view = ProgressBar(activity))
						}

						Thread {
							val feedList = LitePal.findAll(Feed::class.java)
							for (feed in feedList) {
								// 每个订阅只保留指定数目的文章，从旧文章开始删除

								val feedItems = where("feedid = ?", feed.id.toString()).order("date").find(FeedItem::class.java)
								val size = feedItems.size
								val temp = size - num.toInt()
								if (temp < 0) {
									// 不要删除
									continue
								} else {
									for (i in 0 until temp) {
										feedItems[i].delete()
									}
								}
							}
							UIUtil.runOnUiThread(requireActivity()) {
								Toasty.info(requireContext(), "清理成功！").show()
								EventBus.getDefault().post(EventMessage(EventMessage.DATABASE_RECOVER))
								if (dialog1.isShowing) {
									dialog1.dismiss()
								}
							}
						}.start()
					} else {
						// 输入错误
						Toasty.info(requireContext(), "老实说你输入的是不是数字").show()
					}
				}
			}
			false
		}
	}
}
