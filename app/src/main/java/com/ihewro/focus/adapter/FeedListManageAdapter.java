package com.ihewro.focus.adapter;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ihewro.focus.R;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.Help;
import com.ihewro.focus.view.FeedOperationPopupView;
import com.lxj.xpopup.XPopup;

import java.util.List;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/05/08
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class FeedListManageAdapter extends BaseItemDraggableAdapter<Feed, BaseViewHolder> {

    private Activity activity;


    public FeedListManageAdapter(@Nullable List<Feed> data,Activity activity) {
        super(R.layout.item_feed_folder,data);
        this.activity = activity;
    }

    @Override
    protected void convert(BaseViewHolder helper, Feed item) {
        helper.setText(R.id.title,item.getName());
        //TODO:如果有自己ico图标，则显示ico图标
        helper.setImageResource(R.id.main_logo,R.drawable.ic_rss_feed_grey_24dp);

        helper.setText(R.id.not_read_num,item.getUnreadNum() + "");

        initListener(helper,item);

    }

    private void initListener(final BaseViewHolder helper, final Feed item){
        //长按功能菜单
        helper.getView(R.id.item_view).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new XPopup.Builder(activity)
                        .asCustom(new FeedOperationPopupView(activity, item.getId(),item.getName(),item.getDesc(),new Help(false)))
                        .show();
                return true;
            }
        });


    }

}
