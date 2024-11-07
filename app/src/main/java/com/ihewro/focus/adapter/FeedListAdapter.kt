package com.ihewro.focus.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.blankj.ALog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
import com.chad.library.adapter.base.BaseViewHolder
import com.ihewro.focus.GlobalConfig
import com.ihewro.focus.R
import com.ihewro.focus.bean.Feed
import com.ihewro.focus.bean.FeedRequire
import com.ihewro.focus.bean.Help
import com.ihewro.focus.bean.UserPreference
import com.ihewro.focus.http.HttpInterface
import com.ihewro.focus.http.RetrofitManager
import com.ihewro.focus.util.ImageLoadUtil
import com.ihewro.focus.util.RSSUtil
import com.ihewro.focus.util.StringUtil
import com.ihewro.focus.util.UIUtil
import com.ihewro.focus.view.RequireListPopupView
import com.lxj.xpopup.XPopup
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * <pre>
 * author : hewro
 * e-mail : ihewro@163.com
 * time   : 2019/04/08
 * desc   :
 * version: 1.0
</pre> *
 */
class FeedListAdapter(data: List<Feed?>?, private val activity: Activity, private val mName: String) :
	BaseQuickAdapter<Feed, BaseViewHolder>(R.layout.item_feed, data) {
	private val feedRequireList: MutableList<FeedRequire> = ArrayList()

	init {
		initListener()
	}

	override fun convert(helper: BaseViewHolder, item: Feed) {
		helper.setText(R.id.name, item.name)
		helper.setText(R.id.desc, item.desc)
		if (StringUtil.trim(item.icon) != "") { // 显示图标
			ImageLoadUtil.displayImage(helper.getView(R.id.account_avatar), StringUtil.trim(item.icon.toString()))
		} else {
			helper.setImageResource(R.id.account_avatar, R.drawable.ic_rss_feed_grey_24dp)
		}
	}

	private fun initListener() {
		this.onItemClickListener = OnItemClickListener { _, _, position ->
			showRequireList(
				position,
				this@FeedListAdapter.data[position]
			)
		}
	}


	/**
	 * 显示参数列表
	 * @param position
	 */
	private fun showRequireList(position: Int, feed: Feed) {
		// 显示弹窗，填写参数进行订阅
		val loading: MaterialDialog = MaterialDialog(activity).show {
			title(text = "加载参数")
			message(text = "正在网络请求参数")
			this.customView(view = ProgressBar(activity))
		}

		val request =
			RetrofitManager.create(HttpInterface::class.java)
				.getFeedRequireListByWebsite(GlobalConfig.serverUrl + "feedRequireList", feed.iid)
		request.enqueue(object : Callback<List<FeedRequire>?> {
			@SuppressLint("CheckResult")
			override fun onResponse(call: Call<List<FeedRequire>?>, response: Response<List<FeedRequire>?>) {
				if (response.isSuccessful) {
					// 结束ui
					loading.dismiss()

					checkNotNull(response.body())
					feedRequireList.clear()
					// feed更新到当前的时间流中。
					feedRequireList.addAll(response.body()!!)
					feedRequireList.add(FeedRequire("订阅名称", "取一个名字吧", FeedRequire.SET_NAME, mName + "的" + feed.name))


					var url = feed.url
					if (feed.url[0] != '/') {
						url = "/$url"
					}
					if (feed.type == "rsshub" && RSSUtil.urlIsContainsRSSHub(feed.url) == -1) { // 只有当在线市场的源标记为rsshub，且url中没有rsshub前缀，才会添加当前选择的前缀。
						feed.url = UserPreference.getRssHubUrl() + url
					}
					val help = if (StringUtil.trim(feed.extra) != "") {
						Help(true, feed.extra)
					} else {
						Help(false)
					}
					// 用一个弹窗显示参数列表
					XPopup.Builder(activity)
						.asCustom(
							RequireListPopupView(
								activity,
								feedRequireList,
								"订阅参数填写",
								"",
								help,
								feed,
								(activity as FragmentActivity).supportFragmentManager
							)
						)
						.show()
				} else {
					ALog.d("请求失败" + response.errorBody())
					Toasty.error(UIUtil.getContext(), "请求失败" + response.errorBody(), Toast.LENGTH_SHORT).show()
				}
			}

			override fun onFailure(call: Call<List<FeedRequire>?>, t: Throwable) {
				loading.dismiss()
			}
		})
	}
}
