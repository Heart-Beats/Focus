package com.ihewro.focus.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.ihewro.focus.R
import com.ihewro.focus.bean.UserPreference


class NewComerActivity : AppIntro() {

	companion object {
		fun activityStart(activity: Activity) {
			val intent = Intent(activity, NewComerActivity::class.java)
			activity.startActivity(intent)
		}
	}

	override fun onWindowFocusChanged(hasFocus: Boolean) {
		super.onWindowFocusChanged(hasFocus)
		if (hasFocus && Build.VERSION.SDK_INT >= 19) {
			val decorView = window.decorView
			decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					or View.SYSTEM_UI_FLAG_FULLSCREEN
					or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// Make sure you don't call setContentView!

		// Call addSlide passing your Fragments.
		// You can use AppIntroFragment to use a pre-built fragment
		addSlide(
			AppIntroFragment.createInstance(
				title = "过去",
				description = "“从不缺少信息”。那些刷新也刷不完的“推荐内容”真的需要阅读吗",
				imageDrawable = R.drawable.ic_history_white_24dp,
				backgroundColorRes = R.color.color6
			)
		)
		addSlide(
			AppIntroFragment.createInstance(
				title = "现在",
				description = "有新的选择。聚合关注的内容。追求减法，碎片阅读也要“适可而止”",
				imageDrawable = R.drawable.ic_center_focus_weak_white_24dp,
				backgroundColorRes = R.color.color7
			)
		)
		addSlide(
			AppIntroFragment.createInstance(
				title = "开始使用",
				description = "使用OPML轻松导入，或者尝试手动添加订阅吧，发现市场轻松寻找优质内容",
				imageDrawable = R.drawable.ic_thumb_up_white_24dp,
				backgroundColorRes = R.color.color8
			)
		)

		// 设置幻灯片过渡动画
		setTransformer(AppIntroPageTransformerType.Fade)
		// setTransformer(AppIntroPageTransformerType.Parallax(
		// 	titleParallaxFactor = 1.0,
		// 	imageParallaxFactor = -1.0,
		// 	descriptionParallaxFactor = 2.0
		// ))

		// 在两个幻灯片背景之间设置颜色过渡动画的可能性。此功能默认处于禁用状态
		isColorTransitionsEnabled = true

		// 支持向导模式，其中“跳过”按钮将被后退箭头取代
		isWizardMode = true

		// 启用沉浸式模式
		// setImmersiveMode()

		// 不希望用户从简介返回
		isSystemBackButtonLocked = true
	}

	override fun onSkipPressed(currentFragment: Fragment?) {
		super.onSkipPressed(currentFragment)
		// Decide what to do when the user clicks on "Skip"
		finish()
	}

	override fun onDonePressed(currentFragment: Fragment?) {
		super.onDonePressed(currentFragment)

		UserPreference.updateOrSaveValueByKeyAsync(
			UserPreference.FIST_USE_NEW_COMER, 1.toString()
		) { list: List<UserPreference?>? ->
			finish()
			MainActivity.activityStart(this)
		}
	}
}
