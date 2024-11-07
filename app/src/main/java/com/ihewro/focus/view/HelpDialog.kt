package com.ihewro.focus.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.ihewro.focus.R
import com.ihewro.focus.util.WebViewUtil

/**
 * <pre>
 * author : hewro
 * e-mail : ihewro@163.com
 * time   : 2019/05/16
 * desc   :
 * version: 1.0
</pre> *
 */
class HelpDialog : DialogFragment() {

	companion object {
		@JvmStatic
		fun create(darkTheme: Boolean, accentColor: Int, content: String?): HelpDialog {
			val dialog = HelpDialog()
			val args = Bundle()
			args.putBoolean("dark_theme", darkTheme)
			args.putInt("accent_color", accentColor)
			dialog.arguments = args
			dialog.content = content
			return dialog
		}
	}

	private var content: String? = null

	@SuppressLint("InflateParams")
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val customView: View
		try {
			customView = LayoutInflater.from(activity).inflate(R.layout.dialog_help_view, null)
		} catch (e: InflateException) {
			throw IllegalStateException("This device does not support Web Views.")
		}

		return MaterialDialog(requireContext()).show {
			title(text = "帮助信息")
			customView(view = customView)
			positiveButton(text = "确定")
			val webView: WebView = customView.findViewById(R.id.webview)
			//        webView.loadData(this.content, "text/html", "UTF-8");
			WebViewUtil.LoadHtmlIntoWebView(webView, content, activity, "")
		}
	}

}
