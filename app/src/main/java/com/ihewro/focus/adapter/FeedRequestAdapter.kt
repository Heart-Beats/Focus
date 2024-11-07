package com.ihewro.focus.adapter

import android.app.Activity
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ihewro.focus.R
import com.ihewro.focus.bean.FeedRequest
import com.ihewro.focus.util.DateUtil
import com.ihewro.focus.util.StringUtil

/**
 * <pre>
 * author : hewro
 * e-mail : ihewro@163.com
 * time   : 2019/06/02
 * desc   :
 * version: 1.0
</pre> *
 */
class FeedRequestAdapter(private val activity: Activity, data: List<FeedRequest?>?) :
	BaseQuickAdapter<FeedRequest, BaseViewHolder>(R.layout.item_request, data) {
	override fun convert(helper: BaseViewHolder, item: FeedRequest) {
		helper.setText(R.id.date, DateUtil.getRFCStringByInt(item.time))
		helper.setText(R.id.code, item.code.toString() + "")
		if (StringUtil.trim(item.reason) == "") {
			helper.setText(R.id.reason, "成功")
		} else {
			helper.setText(R.id.reason, "点击查看详情")
			helper.getView<View>(R.id.reason).setOnClickListener {
				MaterialDialog(activity).show {
					title(text = "失败原因")
					message(text = item.reason)
				}
			}
		}
		helper.setText(R.id.num, item.num.toString() + "")
	}
}
