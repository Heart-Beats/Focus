package com.ihewro.focus.util

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.ihewro.focus.R
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.ImageViewerPopupView
import com.lxj.xpopup.util.SmartGlideImageLoader

/**
 * @description:
 * @author: Match
 * @date: 1/27/17
 */
object ImageLoadUtil {
	@JvmStatic
    fun displayImage(imageView: ImageView, Url: String?) {
		val errorDrawable = imageView.resources.getDrawable(R.drawable.loading_error)
		val defaultDrawable = imageView.resources.getDrawable(R.drawable.ic_loading)

		Glide.with(imageView.context)
			.load(Url)
			.placeholder(defaultDrawable)
			.error(errorDrawable)
			.into(imageView)
	}


	@JvmStatic
    fun showSingleImageDialog(context: Context?, imageUrl: String?, srcView: View?) {
		// 单张图片场景
		val imageViewerPopupView = XPopup.Builder(context)
			.asImageViewer(srcView as ImageView?, imageUrl, SmartGlideImageLoader())
		imageViewerPopupView.show()
	}

	@JvmStatic
	fun showMultipleImageDialog(context: Context?, position: Int, imageUrls: List<Any?>?, srcView: View?) {
		// 多图片场景（你有多张图片需要浏览）
		// srcView参数表示你点击的那个ImageView，动画从它开始，结束时回到它的位置。
		XPopup.Builder(context)
			.asImageViewer(srcView as ImageView?, position, imageUrls, { popupView: ImageViewerPopupView, _: Int ->
				// 注意这里：根据position找到你的itemView。根据你的itemView找到你的ImageView。
				// Demo中RecyclerView里面只有ImageView所以这样写，不要原样COPY。
				// 作用是当Pager切换了图片，需要更新源View，
				popupView.updateSrcView(srcView)
			}, SmartGlideImageLoader())
			.show()
	}
}
