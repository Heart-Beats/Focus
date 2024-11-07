package com.ihewro.focus.adapter;

import android.app.Activity;
import android.view.View;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ihewro.focus.R;
import com.ihewro.focus.bean.CollectionFolder;
import com.ihewro.focus.bean.Help;
import com.ihewro.focus.view.CollectionFolderOperationPopupView;
import com.lxj.xpopup.XPopup;

import java.util.List;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/07/03
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class CollectionFolderManageAdapter extends BaseItemDraggableAdapter<CollectionFolder, BaseViewHolder> {


    List<CollectionFolder> data;
    private Activity activity;
    public CollectionFolderManageAdapter(List<CollectionFolder> data,Activity activity) {
        super(R.layout.item_collection_folder_manage,data);

        this.data = data;
        this.activity = activity;
        initListener();
    }

    @Override
    protected void convert(BaseViewHolder helper, CollectionFolder item) {
        helper.setText(R.id.title, item.name);


    }

    private void initListener(){

        this.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {

                //弹窗操作
                new XPopup.Builder(activity)
                        .asCustom(new CollectionFolderOperationPopupView(activity, data.get(position).id, data.get(position).name, "", new Help(false)))
                        .show();
                return true;
            }
        });




    }
}
