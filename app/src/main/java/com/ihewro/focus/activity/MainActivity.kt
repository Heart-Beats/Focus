package com.ihewro.focus.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.ButtonBarLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.blankj.ALog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.tabs.TabLayout
import com.ihewro.focus.GlobalConfig
import com.ihewro.focus.R
import com.ihewro.focus.adapter.BaseViewPagerAdapter
import com.ihewro.focus.bean.EventMessage
import com.ihewro.focus.bean.Feed
import com.ihewro.focus.bean.FeedFolder
import com.ihewro.focus.bean.FeedItem
import com.ihewro.focus.bean.Help
import com.ihewro.focus.bean.UserPreference
import com.ihewro.focus.fragemnt.UserFeedUpdateContentFragment
import com.ihewro.focus.fragemnt.search.SearchFeedFolderFragment
import com.ihewro.focus.fragemnt.search.SearchFeedItemListFragment
import com.ihewro.focus.fragemnt.search.SearchLocalFeedListFragment
import com.ihewro.focus.task.TimingService
import com.ihewro.focus.util.UIUtil
import com.ihewro.focus.view.FeedFolderOperationPopupView
import com.ihewro.focus.view.FeedListShadowPopupView
import com.ihewro.focus.view.FeedOperationPopupView
import com.ihewro.focus.view.FilterPopupView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.enums.PopupPosition
import com.lxj.xpopup.interfaces.SimpleCallback
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.miguelcatalan.materialsearchview.MaterialSearchView.SearchViewListener
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.holder.BadgeStyle
import com.mikepenz.materialdrawer.model.ExpandableBadgeDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.SectionDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal.count
import org.litepal.LitePal.order
import org.litepal.LitePal.where
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import skin.support.SkinCompatManager
import skin.support.utils.SkinPreference
import java.util.Objects

class MainActivity : BaseActivity() {
	@JvmField
	@BindView(R.id.toolbar)
	var toolbar: Toolbar? = null

	@JvmField
	@BindView(R.id.search_view)
	var searchView: MaterialSearchView? = null

	@JvmField
	@BindView(R.id.playButton)
	var playButton: ButtonBarLayout? = null

	@JvmField
	@BindView(R.id.fl_main_body)
	var flMainBody: FrameLayout? = null

	@JvmField
	@BindView(R.id.toolbar_title)
	var toolbarTitle: TextView? = null

	@JvmField
	@BindView(R.id.toolbar_container)
	var toolbarContainer: FrameLayout? = null

	@JvmField
	@BindView(R.id.tab_layout)
	var tabLayout: TabLayout? = null

	@JvmField
	@BindView(R.id.viewPager)
	var viewPager: ViewPager? = null

	@JvmField
	@BindView(R.id.search_view_content)
	var searchViewContent: LinearLayout? = null

	@JvmField
	@BindView(R.id.subtitle)
	var subtitle: TextView? = null

	private lateinit var expandPositions: IntArray


	private var feedPostsFragment: UserFeedUpdateContentFragment? = null
	private var currentFragment: Fragment? = null
	private val subItems: MutableList<IDrawerItem<*, *>?> = ArrayList()
	private var drawer: Drawer? = null
	private var popupView: FeedListShadowPopupView? = null // 点击顶部标题的弹窗
	private var drawerPopupView: FilterPopupView? = null // 右侧边栏弹窗
	private val errorFeedIdList: MutableList<String> = ArrayList()

	private val fragmentList: MutableList<Fragment> = ArrayList()
	private var searchLocalFeedListFragment: SearchLocalFeedListFragment? = null
	private var searchFeedFolderFragment: SearchFeedFolderFragment? = null
	private var searchFeedItemListFragment: SearchFeedItemListFragment? = null
	private var AllDrawerItem: IDrawerItem<*, *>? = null

	private var selectIdentify: Long = 0

	private val expandFolderIdentify: List<Long> = ArrayList()

	companion object {
		private const val DRAWER_FOLDER_ITEM = 847
		private const val DRAWER_FOLDER = 301
		private const val SHOW_ALL = 14
		private const val SHOW_STAR = 876
		private const val SHOW_DISCOVER = 509
		private const val ADD_AUTH = 24
		private const val FEED_MANAGE = 460
		private const val SETTING = 911
		private const val PAY_SUPPORT = 71
		private const val FEED_FOLDER_IDENTIFY_PLUS = 9999
		const val RQUEST_STORAGE_READ: Int = 8

		@JvmStatic
		fun activityStart(activity: Activity) {
			val intent = Intent(activity, MainActivity::class.java)
			activity.startActivity(intent)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		ButterKnife.bind(this)

		if (SkinPreference.getInstance().skinName == "night") {
			toolbar?.inflateMenu(R.menu.main_night)
		} else {
			toolbar?.inflateMenu(R.menu.main)
		}

		if (intent != null) {
			val flag = intent.getBooleanExtra(GlobalConfig.is_need_update_main, false)
			if (flag) {
				// 更新数据
				/*updateDrawer();
                clickAndUpdateMainFragmentData(new ArrayList<String>(), "全部文章");*/
			}
		}
		setSupportActionBar(toolbar)
		toolbarTitle!!.text = "全部文章"
		EventBus.getDefault().register(this)


		initEmptyView()

		clickFeedPostsFragment(ArrayList())

		initListener()

		initTapView()

		createTabLayout()

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if (!Environment.isExternalStorageManager()) {
				MaterialDialog(this).show {
					title(text = "权限申请")
					message(text = "为确保后续备份和导入导出功能使用，Android 10 及以上设备需要授予管理所有文件权限")
					positiveButton(text = "确定") {
						val intent = Intent(
							Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
							Uri.parse("package:$packageName")
						)
						startActivity(intent)
					}
					negativeButton(text = "取消授权") {
						Toasty.info(
							this@MainActivity,
							"你已取消授权，可能会影响后续正常使用",
							Toasty.LENGTH_SHORT
						).show()
					}

					cancelable(false)
					cancelOnTouchOutside(false)
				}
			}
		} else {
			val perms = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
			if (!EasyPermissions.hasPermissions(this, *perms)) {
				// 没有权限 1. 申请权限
				EasyPermissions.requestPermissions(
					PermissionRequest.Builder(this, RQUEST_STORAGE_READ, *perms)
						.setRationale("需要存储器读写权限以便后续备份和导入导出功能使用")
						.setPositiveButtonText("确定")
						.setNegativeButtonText("取消")
						.build()
				)
			}
		}

		// 开启定时任务
		startTimeService()
	}

	private fun startTimeService() {
		TimingService.startService(this, false)
	}


	/**
	 * 新手教程，第一次打开app，会自动弹出教程
	 */
	private fun initTapView() {
		if (UserPreference.queryValueByKey(UserPreference.FIRST_USE_LOCAL_SEARCH_AND_FILTER, "0") == "0") {
			if (count(FeedItem::class.java) > 0) {
				TapTargetSequence(this)
					.targets(
						TapTarget.forToolbarMenuItem(toolbar, R.id.action_search, "搜索", "在这里，搜索本地内容的一切。")
							.cancelable(false)
							.drawShadow(true)
							.titleTextColor(R.color.colorAccent)
							.descriptionTextColor(R.color.text_secondary_dark)
							.tintTarget(true)
							.targetCircleColor(android.R.color.black) // 内圈的颜色
							.id(1),

						TapTarget.forToolbarMenuItem(toolbar, R.id.action_filter, "过滤设置", "开始按照你想要的方式显示内容吧！")
							.cancelable(false)
							.drawShadow(true)
							.titleTextColor(R.color.colorAccent)
							.descriptionTextColor(R.color.text_secondary_dark)
							.tintTarget(true)
							.targetCircleColor(android.R.color.black) // 内圈的颜色
							.id(2)
					)
					.listener(object : TapTargetSequence.Listener {
						override fun onSequenceFinish() {
							// 设置该功能已经使用过了
							UserPreference.updateOrSaveValueByKey(UserPreference.FIRST_USE_LOCAL_SEARCH_AND_FILTER, "1")
						}

						override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {
							when (lastTarget.id()) {
								1 -> {}
								2 -> drawerPopupView!!.toggle()
							}
						}

						override fun onSequenceCanceled(lastTarget: TapTarget) {
							// Boo
						}
					}).start()
			}
		}
	}


	@SuppressLint("ClickableViewAccessibility")
	private fun initListener() {
		searchView!!.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean {
				// Do some magic
				//                searchViewContent.setVisibility(View.GONE);
				if (query != "") {
					updateTabLayout(query)
				}
				return true
			}

			override fun onQueryTextChange(newText: String): Boolean {
				// Do some magic
				// 开始同步搜索
				if (newText != "") {
					updateTabLayout(newText)
				}
				return true
			}
		})


		searchView!!.setOnSearchViewListener(object : SearchViewListener {
			override fun onSearchViewShown() {
				searchViewContent!!.visibility = View.VISIBLE
			}

			override fun onSearchViewClosed() {
				searchViewContent!!.visibility = View.GONE
			}
		})


		/*playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ALog.d("单击");
                toggleFeedListPopupView();
            }
        });*/
		playButton!!.setOnLongClickListener {
			// 加载框
			val loading: MaterialDialog = MaterialDialog(this).show {
				message(text = "统计数据中……")
				this.customView(view = ProgressBar(this@MainActivity))
			}

			Thread {
				val feedItemNum = feedPostsFragment!!.feedItemNum
				val notReadNum = feedPostsFragment!!.notReadNum
				UIUtil.runOnUiThread(this@MainActivity) {
					loading.dismiss()
					MaterialDialog(this).show {
						title(text = toolbarTitle!!.text.toString())
						message(text = "全部数目$feedItemNum\n未读数目$notReadNum")
					}
				}
			}.start()
			true
		}

		val gestureDetector = GestureDetector(this, object : SimpleOnGestureListener() {
			override fun onDoubleTap(e: MotionEvent): Boolean { // 双击事件
				// 回顶部
				ALog.d("双击")
				EventBus.getDefault().post(EventMessage(EventMessage.GO_TO_LIST_TOP))

				return true
			}

			override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
				ALog.d("单击")
				toggleFeedListPopupView()
				return true
			} /*   @Override
            public void onLongPress(MotionEvent e) {
                //显示当前列表的的信息
                super.onLongPress(e);
                new MaterialDialog.Builder(MainActivity.this)
                        .title(toolbarTitle.getText())
                        .content("全部数目" + feedPostsFragment.getFeedItemNum() + "\n" + "未读数目" + feedPostsFragment.getNotReadNum())
                        .show();

            }*/
		})


		playButton!!.setOnTouchListener { _, event ->
			gestureDetector.onTouchEvent(
				event!!
			)
		}
	}

	private fun createTabLayout() {
		// 碎片列表
		fragmentList.clear()
		searchFeedFolderFragment = SearchFeedFolderFragment(this)
		searchLocalFeedListFragment = SearchLocalFeedListFragment(this)
		searchFeedItemListFragment = SearchFeedItemListFragment(this)
		fragmentList.add(searchFeedFolderFragment!!)
		fragmentList.add(searchLocalFeedListFragment!!)
		fragmentList.add(searchFeedItemListFragment!!)

		// 标题列表
		val pageTitleList: MutableList<String> = ArrayList()
		pageTitleList.add("文件夹")
		pageTitleList.add("订阅")
		pageTitleList.add("文章")

		// 新建适配器
		val adapter = BaseViewPagerAdapter(supportFragmentManager, fragmentList, pageTitleList)

		// 设置ViewPager
		viewPager!!.adapter = adapter
		viewPager!!.offscreenPageLimit = 3
		tabLayout!!.setupWithViewPager(viewPager)
		tabLayout!!.tabMode = TabLayout.MODE_FIXED

		// 适配夜间模式
		if (SkinPreference.getInstance().skinName == "night") {
			tabLayout!!.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary_night))
		} else {
			tabLayout!!.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
		}
	}

	private fun updateTabLayout(text: String) {
		// 显示动画

		searchFeedItemListFragment!!.showLoading()
		searchLocalFeedListFragment!!.showLoading()
		searchFeedFolderFragment!!.showLoading()


		Thread {
			val searchResults: List<FeedItem>
			var text2 = "%$text%"
			searchResults = where("title like ? or summary like ?", text2, text2).find(FeedItem::class.java)
			text2 = "%$text%"
			val searchResults2 = where("name like ? or desc like ?", text2, text2).find(Feed::class.java)
			text2 = "%$text%"
			val searchResult3s = where("name like ?", text2).find(FeedFolder::class.java)
			UIUtil.runOnUiThread(this@MainActivity) {
				searchFeedItemListFragment!!.updateData(searchResults)
				searchLocalFeedListFragment!!.updateData(searchResults2)
				searchFeedFolderFragment!!.updateData(searchResult3s)
			}
		}.start()
	}


	/**
	 * 全文搜索🔍
	 *
	 * @param text
	 * @return
	 */
	fun queryFeedItemByText(text: String) {
		var text = text
		text = "%$text%"
		val searchResults = where("title like ? or summary like ?", text, text).find(FeedItem::class.java)
		searchFeedItemListFragment!!.updateData(searchResults)
	}


	fun queryFeedByText(text: String) {
		var text = text
		text = "%$text%"
		val searchResults = where("name like ? or desc like ? or url like ?", text, text, text).find(Feed::class.java)
		searchLocalFeedListFragment!!.updateData(searchResults)
	}

	fun queryFeedFolderByText(text: String) {
		var text = text
		text = "%$text%"
		val searchResults = where("name like ?", text).find(FeedFolder::class.java)
		searchFeedFolderFragment!!.updateData(searchResults)
	}


	private fun toggleFeedListPopupView() {
		// 显示弹窗
		if (popupView == null) {
			popupView = XPopup.Builder(this@MainActivity)
				.atView(playButton)
				.hasShadowBg(true)
				.setPopupCallback(object : SimpleCallback() {
					override fun onShow(popup: BasePopupView?) {
						popupView?.adapter?.onItemChildClickListener =
							BaseQuickAdapter.OnItemChildClickListener { _, view, position ->
								if (view.id == R.id.item_view) {
									val feedFolderId = popupView!!.feedFolders[position].id
									val feeds = where("feedfolderid = ?", feedFolderId.toString()).find(Feed::class.java)
									val list = ArrayList<String>()

									for (i in feeds.indices) {
										list.add(feeds[i].id.toString())
									}
									// 切换到指定文件夹下
									clickAndUpdateMainFragmentData(list, popupView!!.feedFolders[position].name, -1)
									popupView!!.dismiss() // 关闭弹窗
								}
							}
					}
				})
				.asCustom(FeedListShadowPopupView(this@MainActivity)) as FeedListShadowPopupView
		}
		popupView!!.toggle()
	}


	private fun initEmptyView() {
		initDrawer()
	}

	// 初始化侧边栏
	private fun initDrawer() {
		// TODO:构造侧边栏项目 使用线程！

		createDrawer()

		// 构造右侧栏目
		createRightDrawer()
	}


	private fun createDrawer() {
		buildDrawer()

		Thread {
			// 初始化侧边栏  子线程刷新 不要阻塞
			refreshLeftDrawerFeedList(false)
			UIUtil.runOnUiThread(this@MainActivity) { drawer!!.setItems(subItems) }
		}.start()
	}


	private fun buildDrawer() {
		// 顶部
		// Create a few sample profile
		val profile = ProfileDrawerItem().withName("本地RSS").withEmail("数据备份在本地")
		val color = if (SkinPreference.getInstance().skinName == "night") {
			com.mikepenz.materialdrawer.R.color.material_drawer_dark_secondary_text
		} else {
			com.mikepenz.materialdrawer.R.color.material_drawer_secondary_text
		}
		// Create the AccountHeader
		val headerResult = AccountHeaderBuilder()
			.withActivity(this)
			.withCompactStyle(true) //                .withHeaderBackground(R.drawable.moecats)
			.withTextColorRes(color)
			.addProfiles(
				profile,
				ProfileSettingDrawerItem()
					.withName("添加第三方服务")
					.withDescription("添加内容源")
					.withIcon(GoogleMaterial.Icon.gmd_add)
					.withIdentifier(ADD_AUTH.toLong())
			)
			.withOnAccountHeaderListener { _, profile1, currentProfile ->
				if (!currentProfile) {
					when (profile1.identifier.toInt()) {
						ADD_AUTH -> AuthListActivity.activityStart(this@MainActivity)
					}
				}
				false
			}
			.build()

		headerResult.view.findViewById<View>(com.mikepenz.materialdrawer.R.id.material_drawer_account_header_current).visibility = View.GONE

		// 初始化侧边栏
		drawer = DrawerBuilder().withActivity(this)
			.withActivity(this)
			.withToolbar(toolbar!!)
			.withTranslucentStatusBar(true)
			.withAccountHeader(headerResult)
			.addDrawerItems(*Objects.requireNonNull(subItems.toTypedArray<IDrawerItem<*, *>?>()))
			.withOnDrawerItemClickListener { _, _, drawerItem ->
				drawerItemClick(drawerItem)
				false
			}
			.withOnDrawerItemLongClickListener { _, _, drawerItem ->
				drawerLongClick(drawerItem)
				true
			}
			.withStickyFooter(R.layout.component_drawer_foooter)
			.withStickyFooterShadow(false)
			.build()


		// 初始化顶部的内容包括颜色
		var flag = false
		if (SkinPreference.getInstance().skinName == "night") {
			flag = true
			(drawer?.stickyFooter?.findViewById<View>(R.id.mode_text) as TextView).text = "日间"
		} else {
			(drawer?.stickyFooter?.findViewById<View>(R.id.mode_text) as TextView).text = "夜间"
		}

		val finalFlag = flag
		drawer?.stickyFooter?.findViewById<View>(R.id.mode)?.setOnClickListener {
			if (!finalFlag) { // flag true 表示夜间模式
				SkinCompatManager.getInstance().loadSkin("night", null, SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN)
				Handler().postDelayed({ recreate() }, 200) // 延时1秒
			} else {
				SkinCompatManager.getInstance().restoreDefaultTheme()
				Handler().postDelayed({ recreate() }, 200) // 延时1秒
			}
		}


		drawer?.stickyFooter?.findViewById<View>(R.id.manage)?.setOnClickListener {
			FeedManageActivity.activityStart(
				this@MainActivity
			)
		}


		drawer?.stickyFooter?.findViewById<View>(R.id.setting)?.setOnClickListener {
			SettingActivity.activityStart(
				this@MainActivity
			)
		}
	}


	private fun updateDrawer() {
		// 初始化侧边栏
		Thread {
			selectIdentify = drawer!!.currentSelection
			ALog.d("选择项$selectIdentify")
			expandPositions = drawer!!.expandableExtension.expandedItems
			refreshLeftDrawerFeedList(true)
			UIUtil.runOnUiThread(this@MainActivity) {
				val templist: List<IDrawerItem<*, *>?> = ArrayList(subItems)
				drawer!!.setItems(templist)
				// 恢复折叠
				val temp = expandPositions.clone()
				for (i in temp.indices) {
					drawer!!.expandableExtension.expand(temp[i])
				}
			}
		}.start()
	}

	private fun drawerItemClick(drawerItem: IDrawerItem<*, *>) {
		if (drawerItem.tag != null) {
			when (drawerItem.tag as Int) {
				SHOW_ALL -> clickAndUpdateMainFragmentData(ArrayList(), "全部文章", drawerItem.identifier)
				SHOW_STAR -> StarActivity.activityStart(this@MainActivity)
				SHOW_DISCOVER -> FeedCategoryActivity.activityStart(this@MainActivity)
				DRAWER_FOLDER_ITEM -> {
					//                    ALog.d("名称为" + ((SecondaryDrawerItem) drawerItem).getName() + "id为" + drawerItem.getIdentifier());
					val list = ArrayList<String>()
					list.add(drawerItem.identifier.toString())
					clickAndUpdateMainFragmentData(
						list,
						(drawerItem as SecondaryDrawerItem).name.toString(),
						drawerItem.getIdentifier()
					)
				}

				DRAWER_FOLDER -> {}
			}
		}
	}


	private fun drawerLongClick(drawerItem: IDrawerItem<*, *>) {
		if (drawerItem.tag != null) {
			when (drawerItem.tag as Int) {
				DRAWER_FOLDER ->                     // 获取到这个文件夹的数据
					XPopup.Builder(this@MainActivity)
						.asCustom(
							FeedFolderOperationPopupView(
								this@MainActivity,
								drawerItem.identifier - FEED_FOLDER_IDENTIFY_PLUS,
								(drawerItem as ExpandableBadgeDrawerItem).name.toString(),
								"",
								Help(false)
							)
						)
						.show()

				DRAWER_FOLDER_ITEM ->                     // 获取到这个feed的数据
					XPopup.Builder(this@MainActivity)
						.asCustom(
							FeedOperationPopupView(
								this@MainActivity,
								drawerItem.identifier,
								(drawerItem as SecondaryDrawerItem).name.toString(),
								"",
								Help(false)
							)
						)
						.show()
			}
		}
	}

	/**
	 * 初始化主fragment
	 *
	 * @param feedIdList
	 */
	private fun clickFeedPostsFragment(feedIdList: ArrayList<String>) {
		if (feedPostsFragment == null) {
			feedPostsFragment = UserFeedUpdateContentFragment.newInstance(feedIdList, toolbarTitle, subtitle)
		}
		toolbar!!.title = "全部文章"
		addOrShowFragment(supportFragmentManager.beginTransaction(), feedPostsFragment)
	}

	/**
	 * 更新主fragment的内部数据并修改UI
	 *
	 * @param feedIdList
	 * @param title
	 */
	private fun clickAndUpdateMainFragmentData(feedIdList: ArrayList<String>, title: String, identify: Long) {
		if (feedPostsFragment == null) {
			ALog.d("出现未知错误")
		} else {
			toolbarTitle!!.text = title
			feedPostsFragment!!.updateData(feedIdList)
		}
	}


	/**
	 * 获取用户的订阅数据，显示在左侧边栏的drawer中
	 */
	@Synchronized
	fun refreshLeftDrawerFeedList(isUpdate: Boolean) {
		subItems.clear()

		AllDrawerItem = SecondaryDrawerItem().withName("全部").withIcon(GoogleMaterial.Icon.gmd_home).withSelectable(true).withTag(
			SHOW_ALL
		)
		subItems.add(AllDrawerItem)
		subItems.add(
			SecondaryDrawerItem().withName("收藏").withIcon(GoogleMaterial.Icon.gmd_star).withSelectable(false).withTag(
				SHOW_STAR
			)
		)
		subItems.add(
			SecondaryDrawerItem().withName("发现").withIcon(GoogleMaterial.Icon.gmd_explore).withSelectable(false).withTag(
				SHOW_DISCOVER
			)
		)
		subItems.add(SectionDrawerItem().withName("订阅源").withDivider(false))


		val feedFolderList = order("ordervalue").find(FeedFolder::class.java)
		for (i in feedFolderList.indices) {
			var notReadNum = 0

			val feedItems: MutableList<IDrawerItem<*, *>> = ArrayList()
			val feedList = where("feedfolderid = ?", feedFolderList[i].id.toString()).order("ordervalue").find(
				Feed::class.java
			)

			var haveErrorFeedInCurrentFolder = false
			for (j in feedList.indices) {
				val temp = feedList[j]
				val currentNotReadnum = where("read = ? and feedid = ?", "0", temp.id.toString()).count(FeedItem::class.java)

				val secondaryDrawerItem =
					SecondaryDrawerItem().withName(temp.name).withSelectable(true).withTag(DRAWER_FOLDER_ITEM).withIdentifier(
						feedList[j].id.toLong()
					)
				if (feedList[j].isOffline) {
					secondaryDrawerItem.withIcon(GoogleMaterial.Icon.gmd_cloud_off)
				} else if (feedList[j].isErrorGet) {
					haveErrorFeedInCurrentFolder = true
					secondaryDrawerItem.withIcon(GoogleMaterial.Icon.gmd_sync_problem)
				} else {
					// TODO: 加载订阅的图标
					secondaryDrawerItem.withIcon(GoogleMaterial.Icon.gmd_rss_feed)
				}

				if (currentNotReadnum != 0) {
					secondaryDrawerItem.withBadge(currentNotReadnum.toString() + "")
				}
				// 不需要这样了，因为都是直接setitems来更新的
				/*if (isUpdate) {
                    drawer.updateItem(secondaryDrawerItem);
                }*/
				feedItems.add(secondaryDrawerItem)

				notReadNum += currentNotReadnum
			}
			val one = ExpandableBadgeDrawerItem().withName(feedFolderList[i].name).withSelectable(true)
				.withIdentifier((feedFolderList[i].id + FEED_FOLDER_IDENTIFY_PLUS).toLong()).withTag(
					DRAWER_FOLDER
				).withBadgeStyle(BadgeStyle().withTextColor(Color.WHITE).withColorRes(com.mikepenz.materialize.R.color.md_red_700))
				.withSubItems(
					feedItems
				)

			ALog.d("文件夹的identity" + (feedFolderList[i].id + FEED_FOLDER_IDENTIFY_PLUS))

			// 恢复折叠状态

			//
			//            one.getViewHolder(R.)
			if (haveErrorFeedInCurrentFolder) {
				one.withTextColorRes(com.mikepenz.materialize.R.color.md_red_700)
			}
			if (notReadNum != 0) {
				one.withBadge(notReadNum.toString() + "")
			}
			// 添加文件夹
			subItems.add(one)
		}

		// 要记得把这个list置空
		errorFeedIdList.clear()
	}


	/**
	 * 添加或者显示 fragment
	 *
	 * @param transaction
	 * @param fragment
	 */
	private fun addOrShowFragment(transaction: FragmentTransaction, fragment: Fragment?) {
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

		// 当前的fragment就是点击切换的目标fragment，则不用操作
		if (currentFragment === fragment) {
			return
		}

		val willCloseFragment = currentFragment // 上一个要切换掉的碎片
		currentFragment = fragment // 当前要显示的碎片

		if (willCloseFragment != null) {
			transaction.hide(willCloseFragment)
		}
		if (!fragment!!.isAdded) { // 如果当前fragment未被添加，则添加到Fragment管理器中
			transaction.add(R.id.fl_main_body, currentFragment!!).commitAllowingStateLoss()
		} else {
			transaction.show(currentFragment!!).commitAllowingStateLoss()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		if (SkinPreference.getInstance().skinName == "night") {
			menuInflater.inflate(R.menu.main_night, menu)
		} else {
			menuInflater.inflate(R.menu.main, menu)
		}

		val item = menu.findItem(R.id.action_search)
		searchView!!.setMenuItem(item)


		return true
	}


	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_filter -> drawerPopupView!!.toggle()
			R.id.action_rsshub -> {
				// 显示弹窗
				// 之前选择的位置
				val select =
					GlobalConfig.rssHub.indexOf(UserPreference.queryValueByKey(UserPreference.RSS_HUB, GlobalConfig.OfficialRSSHUB))
				ALog.d(UserPreference.getRssHubUrl())
				val list = GlobalConfig.rssHub
				MaterialDialog(this).show {
					title(text = "rsshub源选择")
					listItemsSingleChoice(items = list, initialSelection = select) { _, index, _ ->
						if (index in 0..3) {
							UserPreference.updateOrSaveValueByKey(UserPreference.RSS_HUB, GlobalConfig.rssHub[index])
						}
					}
					positiveButton(text = "选择")
				}
			}
		}
		return true
	}

	var startTime: Long = 0

	override fun onBackPressed() {
		super.onBackPressed()
		// 返回键关闭🔍搜索
		if (searchView!!.isSearchOpen) {
			searchView!!.closeSearch()
		} else {
			val currentTime = System.currentTimeMillis()
			if ((currentTime - startTime) >= 2000) {
				Toast.makeText(this@MainActivity, "再按一次退出", Toast.LENGTH_SHORT).show()
				startTime = currentTime
			} else {
				finish()
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
	fun refreshUI(eventBusMessage: EventMessage) {
		if (EventMessage.feedAndFeedFolderAndItemOperation.contains(eventBusMessage.type)) { // 更新整个左侧边栏
			//            ALog.d("收到新的订阅添加，更新！" + eventBusMessage);
			Handler().postDelayed({
				ALog.d("重构")
				updateDrawer()
			}, 100) // 延迟一下，因为数据异步存储需要时间
		} else if (EventMessage.updateBadge.contains(eventBusMessage.type)) { // 只需要修改侧边栏阅读书目
			Handler().postDelayed({ // 打印map
				ALog.d("更新左侧边栏")
				updateDrawer()
			}, 100) // 延迟一下，因为数据异步存储需要时间
		} else if (eventBusMessage.type == EventMessage.FEED_PULL_DATA_ERROR) {
			//            ALog.d("收到错误FeedId List");
			//            errorFeedIdList = eventBusMessage.getIds();
		}
	}


	private fun createRightDrawer() {
		drawerPopupView = XPopup.Builder(this)
			.popupPosition(PopupPosition.Right) // 右边
			.hasStatusBarShadow(true) // 启用状态栏阴影
			.setPopupCallback(object : SimpleCallback() {

				override fun onDismiss(popupView: BasePopupView) {
					// 刷新当前页面的数据，因为筛选的规则变了
					if (drawerPopupView!!.isNeedUpdate) {
						clickAndUpdateMainFragmentData(feedPostsFragment!!.feedIdList, toolbarTitle!!.text.toString(), selectIdentify)
						drawerPopupView!!.isNeedUpdate = false
					}
				}
			})
			.asCustom(FilterPopupView(this@MainActivity)) as FilterPopupView
	}

	override fun onAttachFragment(fragment: Fragment) {
		if (currentFragment == null && fragment is UserFeedUpdateContentFragment) {
			currentFragment = fragment
		}
	}


	override fun onDestroy() {
		super.onDestroy()
		ALog.d("mainActivity 被销毁")
		if (EventBus.getDefault().isRegistered(this)) {
			EventBus.getDefault().unregister(this)
		}
	}


	@SuppressLint("MissingSuperCall")
	override fun onSaveInstanceState(outState: Bundle) {
		//        super.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState)

		// This is needed to prevent welcome screens from being
		// automatically shown multiple times

		// This is the only one needed because it is the only one that
		// is shown automatically. The others are only force shown.
	}
}
