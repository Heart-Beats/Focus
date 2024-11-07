package com.ihewro.focus.fragemnt.setting

import android.annotation.SuppressLint
import android.text.InputType
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.blankj.ALog
import com.ihewro.focus.GlobalConfig
import com.ihewro.focus.R
import com.ihewro.focus.bean.UserPreference
import com.ihewro.focus.task.TimingService
import es.dmoral.toasty.Toasty


/**
 * 同步的设置
 */
class SynchroFragment : SettingFragment() {
	private var use_internet_while_open: SwitchPreferenceCompat? = null
	private var choose_rsshub: Preference? = null
	private var auto_name: SwitchPreferenceCompat? = null

	private var ownrsshub: Preference? = null
	private var time_interval: Preference? = null
	private var only_wifi: SwitchPreferenceCompat? = null


	override fun initView() {
		addPreferencesFromResource(R.xml.pref_synchro_setting)
	}

	override fun initPreferenceComponent() {
		use_internet_while_open = findPreference(getString(R.string.pref_key_use_internet_while_open))
		choose_rsshub = findPreference(getString(R.string.pref_key_rsshub_choice))
		auto_name = findPreference(getString(R.string.pref_key_auto_name))

		ownrsshub = findPreference(getString(R.string.pref_key_own_rsshub))
		time_interval = findPreference(getString(R.string.pref_key_refresh_interval))

		only_wifi = findPreference(getString(R.string.pref_key_only_use_wifi))
	}


	override fun initPreferencesData() {
		// 查询数据库
		if (UserPreference.queryValueByKey(UserPreference.USE_INTERNET_WHILE_OPEN, "0") == "0") {
			use_internet_while_open!!.isChecked = false
		} else {
			use_internet_while_open!!.isChecked = true
		}

		if (UserPreference.queryValueByKey(UserPreference.AUTO_SET_FEED_NAME, "0") == "0") {
			auto_name!!.isChecked = false
		} else {
			auto_name!!.isChecked = true
		}


		if (UserPreference.queryValueByKey(UserPreference.notWifi, "0") == "0") {
			only_wifi!!.isChecked = false
		} else {
			only_wifi!!.isChecked = true
		}


		val pos = GlobalConfig.rssHub.indexOf(UserPreference.queryValueByKey(UserPreference.RSS_HUB, GlobalConfig.OfficialRSSHUB))
		if (pos != 2) { // 如果不是自定义源，则自定义源应该禁止操作
			ownrsshub!!.isEnabled = false
		} else { // 否则自定义源可以操作
			ownrsshub!!.isEnabled = true
		}
	}

	@SuppressLint("CheckResult")
	override fun initListener() {
		time_interval!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			val select = GlobalConfig.refreshIntervalInt.indexOf(
				UserPreference.queryValueByKey(UserPreference.tim_interval, "-1").toInt()
			)

			// 默认选择最后一项，即-1
			MaterialDialog(requireContext()).show {
				title(text = "选择刷新间隔")
				listItemsSingleChoice(items = GlobalConfig.refreshInterval, initialSelection = select) { _, index, _ ->
					UserPreference.updateOrSaveValueByKey(
						UserPreference.tim_interval,
						GlobalConfig.refreshIntervalInt[index].toString()
					)
					TimingService.startService(activity, true)
				}
				positiveButton(text = "选择")
			}
			false
		}

		ownrsshub!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			MaterialDialog(requireContext()).show {
				title(text = "填写自定义RSSHub源")
				message(text = "输入你的地址：")
				input(prefill = UserPreference.queryValueByKey(UserPreference.OWN_RSSHUB, GlobalConfig.OfficialRSSHUB),inputType = InputType.TYPE_CLASS_TEXT) { _, inputText ->
					val name = inputText.toString().trim()
					if (name == "") {
						Toasty.info(requireContext(), "请勿为空😯").show()
					} else {
						UserPreference.updateOrSaveValueByKey(UserPreference.OWN_RSSHUB, name)
						Toasty.success(requireContext(), "填写成功").show()
					}
				}
			}
			false
		}

		use_internet_while_open!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			if (use_internet_while_open!!.isChecked) {
				UserPreference.updateOrSaveValueByKey(UserPreference.USE_INTERNET_WHILE_OPEN, "1")
			} else {
				UserPreference.updateOrSaveValueByKey(UserPreference.USE_INTERNET_WHILE_OPEN, "0")
			}
			false
		}


		only_wifi!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			if (only_wifi!!.isChecked) {
				UserPreference.updateOrSaveValueByKey(UserPreference.notWifi, "1")
			} else {
				UserPreference.updateOrSaveValueByKey(UserPreference.notWifi, "0")
			}
			false
		}


		choose_rsshub!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			// 显示弹窗
			// 之前选择的位置
			val select =
				GlobalConfig.rssHub.indexOf(UserPreference.queryValueByKey(UserPreference.RSS_HUB, GlobalConfig.OfficialRSSHUB))
			ALog.d(UserPreference.getRssHubUrl())
			val list = GlobalConfig.rssHub
			MaterialDialog(requireContext()).show {
				title(text = "rsshub源选择")
				listItemsSingleChoice(items = list, initialSelection = select) { _, index, _ ->
					if (index in 0..2) {
						UserPreference.updateOrSaveValueByKey(UserPreference.RSS_HUB, GlobalConfig.rssHub[index])
						ownrsshub!!.isEnabled = index === 2
					}
				}
				positiveButton(text = "选择")
			}
			false
		}

		auto_name!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
			if (auto_name!!.isChecked) {
				UserPreference.updateOrSaveValueByKey(UserPreference.AUTO_SET_FEED_NAME, "1")
			} else {
				UserPreference.updateOrSaveValueByKey(UserPreference.AUTO_SET_FEED_NAME, "0")
			}
			false
		}
	}
}
