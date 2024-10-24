package com.ihewro.focus.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.blankj.ALog;
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.R;
import com.ihewro.focus.adapter.FeedListAdapter;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.http.HttpInterface;
import com.ihewro.focus.http.RetrofitManager;
import com.ihewro.focus.util.Constants;
import com.ihewro.focus.util.UIUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedListActivity extends BackActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    private FeedListAdapter feedListAdapter;
    private List<Feed> feedList = new ArrayList<>();

    public static void activityStart(Activity activity, String websiteName) {
        Intent intent = new Intent(activity, FeedListActivity.class);
        intent.putExtra(Constants.KEY_STRING_WEBSITE_ID, websiteName);
        activity.startActivity(intent);
    }


    String mName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_list);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        mName = intent.getStringExtra(Constants.KEY_STRING_WEBSITE_ID);

        setSupportActionBar(toolbar);
        toolbar.setTitle(mName+"的可订阅列表");
        initView();
        bindListener();
        refreshLayout.autoRefresh();
        refreshLayout.setEnableLoadMore(false);
    }


    public void initView(){
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //初始化列表
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        feedListAdapter = new FeedListAdapter(feedList,FeedListActivity.this,mName);


        feedListAdapter.bindToRecyclerView(recyclerView);
//        feedListAdapter.setEmptyView(R.layout.simple_loading_view,recyclerView);
    }

    /**
     * 请求一个网站的可订阅列表
     */
    public void requestData(){
        ALog.d("名称为" + mName);
        Call<List<Feed>> request = RetrofitManager.create(HttpInterface.class).getFeedListByWebsite(GlobalConfig.serverUrl + "feedlist", mName);

        request.enqueue(new Callback<List<Feed>>() {
            @Override
            public void onResponse(Call<List<Feed>> call, Response<List<Feed>> response) {
                if (response.isSuccessful()){
                    feedList.clear();
                    feedList.addAll(response.body());

                    feedListAdapter.setNewData(feedList);

                    if (feedList.size()==0){
                        feedListAdapter.setEmptyView(R.layout.simple_empty_view,recyclerView);
                    }
                    Toasty.success(UIUtil.getContext(),"请求成功", Toast.LENGTH_SHORT).show();
                }else {
                    Toasty.error(UIUtil.getContext(),"请求失败2" + response.errorBody(), Toast.LENGTH_SHORT).show();
                }
                refreshLayout.finishRefresh(true);
            }

            @SuppressLint("CheckResult")
            @Override
            public void onFailure(Call<List<Feed>> call, Throwable t) {
                Toasty.error(UIUtil.getContext(),"请求失败2" + t.toString(), Toast.LENGTH_SHORT).show();
                refreshLayout.finishRefresh(false);
            }
        });

    }


    public void bindListener(){
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                requestData();
            }
        });
    }

}
