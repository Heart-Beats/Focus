package com.ihewro.focus.util;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.ihewro.focus.R;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.ImageViewerPopupView;
import com.lxj.xpopup.interfaces.XPopupImageLoader;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.List;

/**
 * @description:
 * @author: Match
 * @date: 1/27/17
 */

public class ImageLoadUtil {

    public static void init(Context context) {

        ColorDrawable defaultDrawable = new ColorDrawable(context.getResources().getColor(R.color.main_grey_light));

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultDrawable)
                .showImageForEmptyUri(defaultDrawable)
                .showImageOnFail(defaultDrawable)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(options)
                .memoryCache(new LruMemoryCache(8 * 1024 * 1024))
                .diskCacheSize(100 * 1024 * 1024)
                .diskCacheFileCount(100)
                .writeDebugLogs()
                .build();

        ImageLoader.getInstance().init(config);
    }


    public static void displayImage(ImageView imageView, String Url) {
        Drawable defaultDrawable;

        Drawable errorDrawable = imageView.getResources().getDrawable(R.drawable.loading_error);
        defaultDrawable = imageView.getResources().getDrawable(R.drawable.ic_loading);

        // Glide.with(imageView.getContext())
        //         .load(Url)
        //         .placeholder(defaultDrawable)
        //         .error(errorDrawable)
        //         .into(imageView);

        new MyImageLoader(imageView.getContext()).loadImage(0, Url, imageView);
    }


    public static void showSingleImageDialog(final Context context, final String imageUrl, View srcView){
        // 单张图片场景
        ImageViewerPopupView imageViewerPopupView = new XPopup.Builder(context)
                .asImageViewer((ImageView) srcView, imageUrl, new MyImageLoader(context));
        imageViewerPopupView.show();
    }

    public static void showMultipleImageDialog(final Context context, int position, final List<? extends Object> imageUrls, View srcView) {
        // 多图片场景（你有多张图片需要浏览）
        // srcView参数表示你点击的那个ImageView，动画从它开始，结束时回到它的位置。
        new XPopup.Builder(context).asImageViewer((ImageView) srcView, position, (List<Object>) imageUrls, (popupView, position1) -> {
                    // 注意这里：根据position找到你的itemView。根据你的itemView找到你的ImageView。
                    // Demo中RecyclerView里面只有ImageView所以这样写，不要原样COPY。
                    // 作用是当Pager切换了图片，需要更新源View，
                    popupView.updateSrcView((ImageView) srcView);
                }, new MyImageLoader(context))
                .show();
    }

    static class MyImageLoader implements XPopupImageLoader {

        private Context context;

        MyImageLoader(Context context) {
            this.context = context;
        }

        @Override
        public void loadImage(int position, @NonNull Object url, @NonNull ImageView imageView) {
            ImageLoader.getInstance().displayImage(StringUtil.trim(String.valueOf(url)), imageView, new DisplayImageOptions.Builder().build());
        }

        @Override
        public File getImageFile(@NonNull Context context, @NonNull Object uri) {
            try {
                return ImageLoader.getInstance().getDiskCache().get(String.valueOf(uri));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
