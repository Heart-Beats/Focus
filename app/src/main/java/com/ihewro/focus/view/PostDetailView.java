package com.ihewro.focus.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;

import com.ihewro.focus.R;
import com.ihewro.focus.bean.FeedItem;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/08/25
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class PostDetailView{
    private FeedItem item;

    private View view = null;
    private Context context;
    public PostDetailView(Context context, FeedItem feedItem) {
        this.context = context;
        this.item = feedItem;
    }


    public View getView(){
        view = View.inflate(context,R.layout.item_post_detail,null);

/*
        //根据偏好设置的背景颜色，设置标题栏位置的背景颜色
        if (!SkinPreference.getInstance().getSkinName().equals("night")){
//            helper.setBackgroundColor(R.id.container, PostSetting.getBackgroundInt(context));
            this.setBackgroundColor(R.id.post_title, PostSetting.getBackgroundInt(context));
            this.setBackgroundColor(R.id.post_turn, PostSetting.getBackgroundInt(context));
        }

        //设置文章内容
        PostUtil.setContent(context, item, ((WebView) this.getChildView(R.id.post_content)), (ViewGroup) this.getChildView(R.id.container));
        this.setText(R.id.post_title, item.getTitle());
        this.setText(R.id.post_time, DateUtil.getTimeStringByInt(item.getDate()));
        this.setText(R.id.feed_name, item.getFeedName());
*/


        return view;
    }


    private void setText(@IdRes int viewId, CharSequence value){
        TextView childView = getChildView(viewId);
        childView.setText(value);
    }


    public void setBackgroundColor(@IdRes int viewId, @ColorInt int color) {
        View childView = getChildView(viewId);
        childView.setBackgroundColor(color);
    }

    private <T extends View> T getChildView(@IdRes int viewId) {
        View childView = null;
        if (view == null) {
            childView = view.findViewById(viewId);
        }
        return (T) childView;
    }



}
