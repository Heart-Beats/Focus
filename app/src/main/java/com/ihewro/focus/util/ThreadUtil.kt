package com.ihewro.focus.util

import android.os.Handler
import android.os.Looper

/**
 * @author  张磊  on  2024/10/23 at 11:37
 * Email: 913305160@qq.com
 */

object ThreadUtil {

	private val handler = Handler(Looper.getMainLooper())

	fun runOnUIThread(runnable: Runnable) {
		handler.post(runnable)
	}
}