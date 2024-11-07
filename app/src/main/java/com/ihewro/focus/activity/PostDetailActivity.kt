package com.ihewro.focus.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.blankj.ALog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.appbar.AppBarLayout
import com.ihewro.focus.GlobalConfig
import com.ihewro.focus.R
import com.ihewro.focus.adapter.PostDetailListPagerAdapter
import com.ihewro.focus.adapter.ReadBackgroundAdapter
import com.ihewro.focus.bean.Background
import com.ihewro.focus.bean.EventMessage
import com.ihewro.focus.bean.FeedItem
import com.ihewro.focus.bean.PostSetting
import com.ihewro.focus.bean.UserPreference
import com.ihewro.focus.callback.UICallback
import com.ihewro.focus.helper.ParallaxTransformer
import com.ihewro.focus.util.Constants
import com.ihewro.focus.util.ShareUtil
import com.ihewro.focus.util.StatusBarUtil
import com.ihewro.focus.util.UIUtil
import com.ihewro.focus.util.WebViewUtil
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import skin.support.utils.SkinPreference
import java.util.regex.Pattern

class PostDetailActivity : BackActivity() {
	@JvmField
	@BindView(R.id.toolbar)
	var toolbar: Toolbar? = null

	@JvmField
	@BindView(R.id.appbar)
	var appbar: AppBarLayout? = null

	@JvmField
	@BindView(R.id.viewPager)
	var viewPager: ViewPager? = null


	private var currentItemReady = false
	private var starItemReady = false
	private var starIconReady = false


	private val viewList: List<View> = ArrayList()
	private val readList: MutableList<Int> = ArrayList()

	private val backgroundList: MutableList<Background> = ArrayList()

	private var adapter: PostDetailListPagerAdapter? = null

	private var ReadSettingDialog: MaterialDialog? = null
	private val postSetting: PostSetting? = null
	var useClass: Class<*>? = null

	private var mIndex = 0
	private var currentFeedItem: FeedItem? = null
	private var starItem: MenuItem? = null

	private var origin = 0

	private var notReadNum = 0

	private var linearLayoutManager: LinearLayoutManager? = null

	private val feedItemList: MutableList<FeedItem> = ArrayList()


	companion object {
		const val ORIGIN_SEARCH: Int = 688
		const val ORIGIN_MAIN: Int = 350
		const val ORIGIN_STAR: Int = 836


		private val colorList: List<Int> = listOf(
			R.color.white,
			R.color.green,
			R.color.yellow,
			R.color.pink,
			R.color.blue,
			R.color.blue2,
			R.color.color3,
			R.color.color4,
			R.color.color5
		)

		@JvmStatic
		fun activityStart(activity: Activity, indexInList: Int, feedItemList: List<FeedItem?>?, origin: Int) {
			val intent = Intent(activity, PostDetailActivity::class.java)

			val bundle = Bundle()

			// 使用静态变量传递数据
			GlobalConfig.feedItemList = feedItemList

			bundle.putInt(Constants.KEY_INT_INDEX, indexInList)
			bundle.putInt(Constants.POST_DETAIL_ORIGIN, origin)

			intent.putExtras(bundle)
			activity.startActivity(intent)
		}
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_post_detail)
		ButterKnife.bind(this)
		setSupportActionBar(toolbar)


		if (supportActionBar != null) {
			supportActionBar!!.setDisplayHomeAsUpEnabled(true)
		}
		val intent = intent
		val bundle = intent.extras
		mIndex = bundle!!.getInt(Constants.KEY_INT_INDEX, 0)


		Thread {
			feedItemList.addAll(GlobalConfig.feedItemList)
			UIUtil.runOnUiThread(this@PostDetailActivity) { // 加载完menu才去加载后面的内容
				initRecyclerView()
			}
		}.start()

		origin = bundle.getInt(Constants.POST_DETAIL_ORIGIN)
	}


	private fun initToolbarColor() {
		// 根据偏好设置背景颜色修改toolbar的背景颜色
		if (SkinPreference.getInstance().skinName != "night") {
			toolbar!!.setBackgroundColor(PostSetting.getBackgroundInt(this@PostDetailActivity))
			StatusBarUtil.setColor(this, PostSetting.getBackgroundInt(this@PostDetailActivity), 0)
		}
	}


	fun initData() {
		currentFeedItem = feedItemList[mIndex]
		this.currentItemReady = true
	}

	private fun initRecyclerView() {
		adapter = PostDetailListPagerAdapter(supportFragmentManager, this@PostDetailActivity)

		// 初始化当前文章的对象
		initData()

		// 显示未读数目
		Thread {
			this@PostDetailActivity.notReadNum = 0
			if (origin != ORIGIN_STAR) {
				for (feedItem in feedItemList) {
					if (!feedItem.isRead) {
						notReadNum++
					}
				}
			}

			adapter!!.setData(feedItemList)
			UIUtil.runOnUiThread(this@PostDetailActivity) {
				viewPager!!.adapter = adapter
				val PARALLAX_COEFFICIENT = 0.5f
				val DISTANCE_COEFFICIENT = 0.1f

				viewPager!!.setPageTransformer(true, ParallaxTransformer(adapter, null, PARALLAX_COEFFICIENT, DISTANCE_COEFFICIENT))

				// 移动到当前文章的位置
				viewPager!!.currentItem = mIndex


				ALog.d("首次加载")
				setLikeButton()
				setCurrentItemStatus()
				initPostClickListener()
				viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
					override fun onPageScrolled(i: Int, v: Float, i1: Int) {
					}

					override fun onPageSelected(i: Int) {
						ALog.d("onPageSelected")
						mIndex = i
						// UI修改
						starIconReady = false
						initData()
						setLikeButton()
						// 修改顶部导航栏的收藏状态
						setCurrentItemStatus()
						initPostClickListener()
					}

					override fun onPageScrollStateChanged(i: Int) {
					}
				})
			}
		}.start()
	}


	/**
	 * 为什么不在adapter里面写，因为recyclerview有缓存机制，没滑到这个时候就给标记为已读了
	 */
	private fun setCurrentItemStatus() {
		// 更新标题栏，指示当前文章位置
		toolbar!!.title = (mIndex + 1).toString() + "/" + feedItemList.size

		// 将该文章标记为已读，并且通知首页修改布局
		if (!currentFeedItem!!.isRead) {
			currentFeedItem!!.isRead = true
			updateNotReadNum()
			currentFeedItem!!.saveAsync().listen { success: Boolean ->
				if (origin == ORIGIN_SEARCH) { // isUpdateMainReadMark 为false表示不是首页进来的
					readList.add(currentFeedItem!!.id)
				} else if (origin == ORIGIN_MAIN) {
					readList.add(mIndex)
				}
			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	private fun initPostClickListener() {
		// 文章双击收藏事件

		val gestureDetector = GestureDetector(this@PostDetailActivity, object : SimpleOnGestureListener() {
			override fun onDoubleTap(e: MotionEvent): Boolean { // 双击事件
				ALog.d("双击")

				clickStarButton()

				return true
			}
		})


		// 双击顶栏回顶部事件
		val gestureDetector1 = GestureDetector(this@PostDetailActivity, object : SimpleOnGestureListener() {
			override fun onDoubleTap(e: MotionEvent): Boolean {
				val scrollView = adapter!!.getViewByPosition(mIndex, R.id.post_turn) as NestedScrollView
				scrollView?.fullScroll(View.FOCUS_UP)
				return super.onDoubleTap(e)
			}
		})


		if (UserPreference.queryValueByKey(UserPreference.notToTop, "0") == "0") {
			toolbar!!.setOnTouchListener { _, motionEvent ->
				gestureDetector1.onTouchEvent(
					motionEvent!!
				)
			}
		}


		if (UserPreference.queryValueByKey(UserPreference.notStar, "0") == "0") {
			// 第一篇文章进入的时候这个view为null，我也不知道为什么！
			// 做一个延迟绑定
			Handler().postDelayed({
				val content = adapter!!.getViewByPosition(mIndex, R.id.post_content)
				content?.setOnTouchListener { v: View?, event: MotionEvent? ->
					gestureDetector.onTouchEvent(
						event!!
					)
				}
			}, 500)
		}
	}


	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		toolbar!!.title = ""
		if (SkinPreference.getInstance().skinName == "night") {
			menuInflater.inflate(R.menu.post_night, menu)
		} else {
			menuInflater.inflate(R.menu.post, menu)
		}

		starItem = menu.findItem(R.id.action_star)
		this.starItemReady = true

		setLikeButton()
		initToolbarColor()


		return true
	}


	/**
	 * 目录按钮的点击事件
	 *
	 * @param item
	 * @return
	 */
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_link -> openLink(currentFeedItem)
			R.id.action_share -> ShareUtil.shareBySystem(
				this@PostDetailActivity, "text", """
 	${currentFeedItem!!.title}
 	${currentFeedItem!!.url}
 	""".trimIndent()
			)

			R.id.text_setting -> {
				ReadSettingDialog = MaterialDialog(this).show {
					customView(viewRes = R.layout.read_setting, scrollable = true)
					positiveButton(text = "确定")
					neutralButton(text = "重置") {
						// 重置设置
						UserPreference.updateOrSaveValueByKey(PostSetting.FONT_SIZE, PostSetting.FONT_SIZE_DEFAULT)
						UserPreference.updateOrSaveValueByKey(PostSetting.FONT_SPACING, PostSetting.FONT_SPACING_DEFAULT)
						UserPreference.updateOrSaveValueByKey(PostSetting.LINE_SPACING, PostSetting.LINE_SPACING_DEFAULT)
					}
				}

				initReadSettingView()
				initReadSettingListener()


				initReadBackgroundView()
			}

			R.id.action_star -> clickStarButton()

			android.R.id.home -> finish()
		}
		return super.onOptionsItemSelected(item)
	}


	private fun clickStarButton() {
		FeedItem.clickWhenNotFavorite(this@PostDetailActivity, currentFeedItem, object : UICallback() {
			override fun doUIWithFlag(flag: Boolean) {
				currentFeedItem!!.isFavorite = flag
				if (flag) { // 收藏了
					Toasty.success(this@PostDetailActivity, "收藏成功").show()
				} else {
					Toasty.success(this@PostDetailActivity, "取消收藏成功").show()
				}

				setLikeButton()

				currentFeedItem!!.saveAsync().listen { success: Boolean ->
					if (origin == ORIGIN_SEARCH) {
						EventBus.getDefault().post(
							EventMessage(
								EventMessage.MAKE_STAR_STATUS_BY_ID,
								currentFeedItem!!.id,
								currentFeedItem!!.isFavorite
							)
						)
					} else if (origin == ORIGIN_MAIN) {
						EventBus.getDefault()
							.post(EventMessage(EventMessage.MAKE_STAR_STATUS_BY_INDEX, mIndex, currentFeedItem!!.isFavorite))
					}
				}
			}
		})
	}

	private fun initReadBackgroundView() {
		if (ReadSettingDialog!!.isShowing) {
			val recyclerView = ReadSettingDialog!!.findViewById<View>(R.id.recycler_view) as RecyclerView

			backgroundList.clear()
			for (color in colorList) {
				backgroundList.add(Background(ContextCompat.getColor(this@PostDetailActivity, color)))
			}
			linearLayoutManager = LinearLayoutManager(this@PostDetailActivity)
			linearLayoutManager!!.orientation = LinearLayoutManager.HORIZONTAL
			recyclerView.layoutManager = linearLayoutManager
			val adapter1 = ReadBackgroundAdapter(this@PostDetailActivity, backgroundList)
			adapter1.bindToRecyclerView(recyclerView)
			adapter1.onItemClickListener =
				BaseQuickAdapter.OnItemClickListener { _, _, position ->
					// 改变背景颜色，并写入到数据库
					UserPreference.updateOrSaveValueByKey(UserPreference.READ_BACKGROUND, backgroundList[position].color.toString())
					// 刷新页面
					// 更新UI
					adapter1.notifyDataSetChanged()
					// 修改背景颜色
					// 根据偏好设置背景颜色修改toolbar的背景颜色
					initToolbarColor()
					adapter!!.notifyItemChanged(mIndex)
				}
		}
	}

	// 根据现有的设置，恢复布局
	private fun initReadSettingView() {
		if (ReadSettingDialog!!.isShowing) {
			// 设置字号
			(ReadSettingDialog!!.findViewById<View>(R.id.size_setting) as SeekBar).progress = PostSetting.getFontSize().toInt()
			(ReadSettingDialog!!.findViewById<View>(R.id.size_setting_info) as TextView).text = PostSetting.getFontSize()

			// 设置字间距
			(ReadSettingDialog!!.findViewById<View>(R.id.font_space_setting) as SeekBar).progress =
				PostSetting.getFontSpace().toInt()
			(ReadSettingDialog!!.findViewById<View>(R.id.font_space_setting_info) as TextView).text = PostSetting.getFontSpace()


			// 设置行间距
			(ReadSettingDialog!!.findViewById<View>(R.id.line_space_setting) as SeekBar).progress = PostSetting.getLineSpace().toInt()
			(ReadSettingDialog!!.findViewById<View>(R.id.line_space_setting_info) as TextView).text = PostSetting.getLineSpace()
		}
	}

	private fun initReadSettingListener() {
		if (ReadSettingDialog!!.isShowing) {
			// 字号改变
			(ReadSettingDialog!!.findViewById<View>(R.id.size_setting) as SeekBar).setOnSeekBarChangeListener(object :
				OnSeekBarChangeListener {
				override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
					// 修改左侧数字
					(ReadSettingDialog!!.findViewById<View>(R.id.size_setting_info) as TextView).text = seekBar.progress.toString()
				}

				override fun onStartTrackingTouch(seekBar: SeekBar) {
				}

				override fun onStopTrackingTouch(seekBar: SeekBar) {
					// 修改文章配置UI
					UserPreference.updateOrSaveValueByKey(PostSetting.FONT_SIZE, seekBar.progress.toString())
					adapter!!.notifyItemChanged(mIndex) // 更新UI
				}
			})

			// 字间距改变
			(ReadSettingDialog!!.findViewById<View>(R.id.font_space_setting) as SeekBar).setOnSeekBarChangeListener(object :
				OnSeekBarChangeListener {
				override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
					// 修改左侧数字
					ALog.d("")
					(ReadSettingDialog!!.findViewById<View>(R.id.font_space_setting_info) as TextView).text =
						seekBar.progress.toString()
				}

				override fun onStartTrackingTouch(seekBar: SeekBar) {
				}

				override fun onStopTrackingTouch(seekBar: SeekBar) {
					// 修改文章配置UI
					UserPreference.updateOrSaveValueByKey(PostSetting.FONT_SPACING, seekBar.progress.toString())
					adapter!!.notifyItemChanged(mIndex) // 更新UI
				}
			})

			// 行间距改变
			(ReadSettingDialog!!.findViewById<View>(R.id.line_space_setting) as SeekBar).setOnSeekBarChangeListener(object :
				OnSeekBarChangeListener {
				override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
					// 修改左侧数字
					(ReadSettingDialog!!.findViewById<View>(R.id.line_space_setting_info) as TextView).text =
						seekBar.progress.toString()
				}

				override fun onStartTrackingTouch(seekBar: SeekBar) {
					ALog.d("")
				}

				override fun onStopTrackingTouch(seekBar: SeekBar) {
					// 修改文章配置UI
					UserPreference.updateOrSaveValueByKey(PostSetting.LINE_SPACING, seekBar.progress.toString())
					adapter!!.notifyItemChanged(mIndex) // 更新UI
				}
			})
		}
	}


	/**
	 * 显示自定义的收藏的图标
	 */
	private fun showStarActionView(item: MenuItem) {
		starItem = item
	}

	private fun setLikeButton() {
		// 设置收藏状态
		if (currentItemReady && starItemReady && !starIconReady && starItem != null && currentFeedItem != null) {
			ALog.d("设置收藏状态")
			if (currentFeedItem!!.isFavorite) {
				starItem!!.setIcon(R.drawable.star_on)
			} else {
				if (SkinPreference.getInstance().skinName == "night") {
					starItem!!.setIcon(R.drawable.star_off_night)
				} else {
					starItem!!.setIcon(R.drawable.star_off)
				}
			}

			starIconReady = true
		}
	}


	private fun openLink(feedItem: FeedItem?) {
		val url = currentFeedItem!!.url
		val pattern = Pattern.compile("^[-\\+]?[\\d]*$")
		if (pattern.matcher(url).matches()) {
			Toasty.info(this, "该文章没有外链哦").show()
		} else {
			/*if (url.startsWith("/")){//相对地址
                Feed feed = LitePal.find(Feed.class,currentFeedItem.getFeedId());
                String origin = feed.getLink();
                if (!origin.endsWith("/")){
                    origin = origin + "/";
                }
                url = origin + url;
            }*/
			WebViewUtil.openLink(url, this@PostDetailActivity)
		}
	}


	private fun updateNotReadNum() {
		notReadNum--

		// UI修改
		// if (notReadNum <= 0) {
		//     toolbar.setTitle("");
		// } else {
		//     toolbar.setTitle(notReadNum + "");
		// }
	}

	override fun onDestroy() {
		// 将首页中已读的文章样式标记为已读

		if (readList.size > 0) {
			if (origin == ORIGIN_SEARCH) { // isUpdateMainReadMark 为false表示不是首页进来的
				EventBus.getDefault().post(EventMessage(EventMessage.MAKE_READ_STATUS_BY_ID_LIST, readList))
			} else if (origin == ORIGIN_MAIN) {
				EventBus.getDefault().post(EventMessage(EventMessage.MAKE_READ_STATUS_BY_INDEX_LIST, readList))
			}
			// 修改首页未读数目
			EventBus.getDefault().post(EventMessage(EventMessage.MAIN_READ_NUM_EDIT, mIndex))
		}


		super.onDestroy()
		EventBus.getDefault().unregister(this)
		ALog.d("postDetail 被销毁")
	}
}
