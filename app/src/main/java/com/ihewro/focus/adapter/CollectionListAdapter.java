package com.ihewro.focus.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ihewro.focus.R;
import com.ihewro.focus.activity.PostDetailActivity;
import com.ihewro.focus.bean.Collection;
import com.ihewro.focus.bean.EventMessage;
import com.ihewro.focus.bean.FeedItem;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.callback.UICallback;
import com.ihewro.focus.util.DataUtil;
import com.ihewro.focus.util.DateUtil;
import com.ihewro.focus.util.ImageLoadUtil;
import com.ihewro.focus.util.StringUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/06/30
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class CollectionListAdapter extends BaseMultiItemQuickAdapter<Collection, BaseViewHolder> {

    private List<Collection> collectionList;
    private Activity activity;
    private int folderid;

    public CollectionListAdapter(int folderid, @Nullable List<Collection> data, Activity activity) {
        super(data);
        this.activity = activity;
        addItemType(Collection.FEED_ITEM, R.layout.item_post);
        addItemType(Collection.WEBSITE, R.layout.item_website);

        this.folderid = folderid;
        collectionList = data;
    }

    @Override
    protected void convert(BaseViewHolder helper, Collection item) {
        switch (helper.getItemViewType()) {
            case Collection.FEED_ITEM:
                //文章item
                helper.setText(R.id.post_title,item.getTitle());
                helper.setText(R.id.rss_name,item.getFeedName());
                helper.setText(R.id.post_summay, DataUtil.getOptimizedDesc(item.getSummary()));
                helper.setText(R.id.post_time, DateUtil.getTTimeStringByInt(item.getDate()));

                if (UserPreference.queryValueByKey(UserPreference.not_show_image_in_list,"0").equals("0")){
                    String imageUrl = DataUtil.getCollectionItemImageUrl(item);
                    if (!StringUtil.trim(imageUrl).equals("")){
                        if (!imageUrl.startsWith("http://")&& !imageUrl.startsWith("https://")){
                            //说明是相对地址
                            if (!imageUrl.substring(0,1).equals("/")){
                                imageUrl = "/" + imageUrl;//前面如果没有/，补足一个
                            }
                            imageUrl =  StringUtil.getUrlPrefix(item.getUrl()) + imageUrl;
                        }
                        helper.getView(R.id.post_pic).setVisibility(View.VISIBLE);

                        ImageLoadUtil.displayImage(helper.getView(R.id.post_pic), StringUtil.trim(imageUrl));

                    }else {
                        helper.getView(R.id.post_pic).setVisibility(View.GONE);
                    }
                }else {//无图列表
                    helper.getView(R.id.post_pic).setVisibility(View.GONE);
                }

                initListener(helper,item);

                break;
            case Collection.WEBSITE:
                //
                break;
        }
    }

    private void initListener(final BaseViewHolder helper, final Collection item){



        helper.getView(R.id.content_container).setOnLongClickListener(v -> {
            FeedItem.clickWhenNotFavorite(activity, collectionList.get(helper.getAdapterPosition()), new UICallback() {
                @Override
                public void doUIWithIds(List<Integer> ids) {
                    //刷新一下界面
                    EventBus.getDefault().post(new EventMessage(EventMessage.COLLECTION_FOLDER_OPERATION));
/*

                    if (!ids.contains(folderid)){
                        remove(helper.getAdapterPosition());
                    }
                    notifyDataSetChanged();
*/

                }
            });
            return true;
        });


        helper.getView(R.id.content_container).setOnClickListener(v -> {
            //跳转到文章页面

            List<FeedItem> list = new ArrayList<>();

            list.add(new FeedItem(item.getTitle(), item.getDate(), item.getSummary(), item.getContent(), item.getUrl(), item.getFeedUrl(), item.getGuid(), true, true));

            PostDetailActivity.activityStart(activity, 0, list, PostDetailActivity.ORIGIN_STAR);
        });
    }
}
