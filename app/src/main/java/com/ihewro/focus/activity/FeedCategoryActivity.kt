package com.ihewro.focus.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.blankj.ALog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.tabs.TabLayout
import com.ihewro.focus.GlobalConfig
import com.ihewro.focus.R
import com.ihewro.focus.adapter.BaseViewPagerAdapter
import com.ihewro.focus.adapter.FeedCategoryLeftAdapter
import com.ihewro.focus.adapter.FeedCategoryRightAdapter
import com.ihewro.focus.adapter.FeedListAdapter
import com.ihewro.focus.bean.Feed
import com.ihewro.focus.bean.FeedRequire
import com.ihewro.focus.bean.Help
import com.ihewro.focus.bean.Website
import com.ihewro.focus.bean.WebsiteCategory
import com.ihewro.focus.fragemnt.search.SearchWebFeedListFragment
import com.ihewro.focus.fragemnt.search.SearchWebListFragment
import com.ihewro.focus.http.HttpInterface
import com.ihewro.focus.http.RetrofitManager
import com.ihewro.focus.util.UIUtil
import com.ihewro.focus.view.RequireListPopupView
import com.lxj.xpopup.XPopup
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.miguelcatalan.materialsearchview.MaterialSearchView.SearchViewListener
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import skin.support.utils.SkinPreference

class FeedCategoryActivity : BackActivity() {
	@JvmField
	@BindView(R.id.toolbar)
	var toolbar: Toolbar? = null

	@JvmField
	@BindView(R.id.recycler_left)
	var recyclerLeft: RecyclerView? = null

	@JvmField
	@BindView(R.id.recycler_right)
	var recyclerRight: RecyclerView? = null
	var leftAdapter: FeedCategoryLeftAdapter? = null
	var rightAdapter: FeedCategoryRightAdapter? = null

	var websiteCategoryList: MutableList<WebsiteCategory> = ArrayList()
	var websiteList: MutableList<Website> = ArrayList()

	@JvmField
	@BindView(R.id.search_view)
	var searchView: MaterialSearchView? = null
	var searchWebListFragment: SearchWebListFragment? = null
	var searchFeedListFragment: SearchWebFeedListFragment? = null

	@JvmField
	@BindView(R.id.tab_layout)
	var tabLayout: TabLayout? = null

	@JvmField
	@BindView(R.id.viewPager)
	var viewPager: ViewPager? = null

	@JvmField
	@BindView(R.id.search_view_content)
	var searchViewContent: LinearLayout? = null

	private val adapter: FeedListAdapter? = null
	private val feedList: List<Feed> = ArrayList()
	private val fragmentList: MutableList<Fragment> = ArrayList()

	companion object {

		@JvmStatic
		fun activityStart(activity: Activity) {
			val intent = Intent(activity, FeedCategoryActivity::class.java)
			activity.startActivity(intent)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_feed_category)
		ButterKnife.bind(this)

		initEmptyView()

		bindListener()

		requestLeftData()

		setSearchTabLayout("", false)
	}

	private fun setSearchTabLayout(search: String, isUpdate: Boolean) {
		// 碎片列表
		fragmentList.clear()
		searchWebListFragment = SearchWebListFragment(this)
		searchFeedListFragment = SearchWebFeedListFragment(this)
		fragmentList.add(searchWebListFragment!!)
		fragmentList.add(searchFeedListFragment!!)

		// 标题列表
		val pageTitleList: MutableList<String> = ArrayList()
		pageTitleList.add("网站")
		pageTitleList.add("订阅")

		// 新建适配器
		val adapter = BaseViewPagerAdapter(supportFragmentManager, fragmentList, pageTitleList)

		// 设置ViewPager
		viewPager!!.adapter = adapter

		tabLayout!!.setupWithViewPager(viewPager)
		tabLayout!!.tabMode = TabLayout.MODE_FIXED

		if (SkinPreference.getInstance().skinName == "night") {
			tabLayout!!.setBackgroundColor(resources.getColor(R.color.colorPrimary_night))
		} else {
			tabLayout!!.setBackgroundColor(resources.getColor(R.color.colorPrimary))
		}
	}

	fun updateSearchTabLayout(content: String?) {
		// 更新界面为加载数据的状态

		searchFeedListFragment!!.showLoading()
		searchWebListFragment!!.showLoading()

		// 请求网站列表
		val request = RetrofitManager.create(
			HttpInterface::class.java
		).searchWebsiteByName(GlobalConfig.serverUrl + "searchWebsiteByName", content)

		request.enqueue(object : Callback<List<Website?>?> {
			override fun onResponse(call: Call<List<Website?>?>, response: Response<List<Website?>?>) {
				searchWebListFragment!!.updateData(response.body())
			}

			override fun onFailure(call: Call<List<Website?>?>, t: Throwable) {
				ALog.d("请求失败2" + t.message)
			}
		})

		// 请求feedList
		val request2 = RetrofitManager.create(HttpInterface::class.java)
			.searchFeedListByName(GlobalConfig.serverUrl + "searchFeedListByName", content)
		request2.enqueue(object : Callback<List<Feed?>?> {
			override fun onResponse(call: Call<List<Feed?>?>, response: Response<List<Feed?>?>) {
				searchFeedListFragment!!.updateData(response.body())
			}

			override fun onFailure(call: Call<List<Feed?>?>, t: Throwable) {
				searchFeedListFragment!!.showError()
				Toasty.error(this@FeedCategoryActivity, "请求失败了").show()
			}
		})
	}

	private fun initEmptyView() {
		setSupportActionBar(toolbar)

		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		val linearLayoutManager = LinearLayoutManager(this)
		recyclerLeft!!.layoutManager = linearLayoutManager

		//        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
		val linearLayoutManager2 = GridLayoutManager(UIUtil.getContext(), 1)

		recyclerRight!!.layoutManager = linearLayoutManager2


		leftAdapter = FeedCategoryLeftAdapter(websiteCategoryList)
		rightAdapter = FeedCategoryRightAdapter(websiteList, this@FeedCategoryActivity)


		leftAdapter!!.bindToRecyclerView(recyclerLeft)
		rightAdapter!!.bindToRecyclerView(recyclerRight)

		leftAdapter!!.setEmptyView(R.layout.simple_loading_view, recyclerLeft)
		rightAdapter!!.setEmptyView(R.layout.simple_loading_view, recyclerRight)
	}


	private fun bindListener() {
		// 搜索在线源🔍


		searchView!!.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean {
				searchViewContent!!.visibility = View.VISIBLE
				updateSearchTabLayout(query)
				return true
			}

			override fun onQueryTextChange(newText: String): Boolean {
				// 输入过程中不做任何操作
				return false
			}
		})

		searchView!!.setOnSearchViewListener(object : SearchViewListener {
			override fun onSearchViewShown() {
				// Do some magic
				searchViewContent!!.visibility = View.VISIBLE
			}

			override fun onSearchViewClosed() {
				// Do some magic
				searchViewContent!!.visibility = View.GONE
			}
		})


		leftAdapter!!.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, _, position ->
			(adapter as FeedCategoryLeftAdapter).setCurrentPosition(position)
			leftAdapter!!.notifyDataSetChanged()
			requestRightData(websiteCategoryList[position].name)
		}
	}

	private fun requestLeftData() {
		val request = RetrofitManager.create(HttpInterface::class.java).getCategoryList(GlobalConfig.serverUrl + "webcategory")
		request.enqueue(object : Callback<List<WebsiteCategory>?> {
			override fun onResponse(call: Call<List<WebsiteCategory>?>, response: Response<List<WebsiteCategory>?>) {
				if (response.isSuccessful) {
					checkNotNull(response.body())
					websiteCategoryList.addAll(response.body()!!)
					leftAdapter!!.setNewData(websiteCategoryList)
					requestRightData(websiteCategoryList[0].name)
				} else {
					ALog.d("请求失败" + response.errorBody())
				}
			}

			override fun onFailure(call: Call<List<WebsiteCategory>?>, t: Throwable) {
				ALog.d("请求失败2" + t.message)
			}
		})
	}

	fun requestRightData(categoryName: String?) {
		rightAdapter!!.setNewData(null)
		rightAdapter!!.setEmptyView(R.layout.simple_loading_view, recyclerRight)
		val request = RetrofitManager.create(
			HttpInterface::class.java
		).getWebsiteListByCategory(GlobalConfig.serverUrl + "weblist", categoryName)

		request.enqueue(object : Callback<List<Website>?> {
			override fun onResponse(call: Call<List<Website>?>, response: Response<List<Website>?>) {
				if (response.isSuccessful) {
					websiteList.clear()
					websiteList.addAll(response.body()!!)
					rightAdapter!!.setNewData(websiteList)
					if (websiteList.size == 0) {
						rightAdapter!!.setNewData(null)
						rightAdapter!!.setEmptyView(R.layout.simple_empty_view)
					}
				} else {
					ALog.d("请求失败" + response.errorBody())
				}
			}

			override fun onFailure(call: Call<List<Website>?>, t: Throwable) {
				ALog.d("请求失败2" + t.message)
			}
		})
	}


	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		if (SkinPreference.getInstance().skinName == "night") {
			menuInflater.inflate(R.menu.feed_night, menu)
		} else {
			menuInflater.inflate(R.menu.feed, menu)
		}
		val item = menu.findItem(R.id.action_search)
		searchView!!.setMenuItem(item)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		super.onOptionsItemSelected(item)
		when (item.itemId) {
			R.id.action_add_by_url -> {
				// 弹窗
				val list: MutableList<FeedRequire> = ArrayList()
				list.add(FeedRequire("订阅地址", "举例：https://www.ihewro.com/feed", FeedRequire.SET_URL))
				list.add(FeedRequire("订阅名称", "随意给订阅取一个名字", FeedRequire.SET_NAME))
				XPopup.Builder(this@FeedCategoryActivity) //                        .moveUpToKeyboard(false) //如果不加这个，评论弹窗会移动到软键盘上面
					.asCustom(
						RequireListPopupView(
							this@FeedCategoryActivity,
							list,
							"手动订阅",
							"适用于高级玩家",
							Help(false),
							Feed(),
							supportFragmentManager
						)
					)
					.show()
			}

			R.id.action_share -> MaterialDialog(this).show {
				title(text = "：）")
				message(text = "分享市场开发中……")
				positiveButton(text = "确定")
				negativeButton(text = "取消")
			}
		}
		return true
	}
}
