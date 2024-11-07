package com.ihewro.focus.view;

import android.content.Context;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ihewro.focus.R;
import com.ihewro.focus.adapter.RequireListAdapter;
import com.ihewro.focus.bean.EventMessage;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.FeedRequire;
import com.ihewro.focus.bean.Help;
import com.ihewro.focus.callback.DialogCallback;
import com.ihewro.focus.task.ShowFeedFolderListDialogTask;
import com.ihewro.focus.util.UIUtil;
import com.lxj.xpopup.core.BottomPopupView;

import org.greenrobot.eventbus.EventBus;
import org.litepal.exceptions.LitePalSupportException;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/05/10
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RequireListPopupView extends BottomPopupView {

    TextView listTitle;
    ImageView actionHelp;
    ImageView actionClose;
    RecyclerView recyclerView;
    TextView textInfo;
    Button btnFinish;

    private RequireListAdapter adapter;
    private List<FeedRequire> feedRequireList = new ArrayList<>();
    private String title;
    private String info;

    private Help help;
    private Feed feed;
    private FragmentManager fragmentManager;


    public RequireListPopupView(@NonNull Context context) {
        super(context);
    }

    public RequireListPopupView(@NonNull Context context, List<FeedRequire> feedRequireList,String title,String info,Help help,Feed feed,FragmentManager fragmentManager) {
        super(context);
        this.feedRequireList = feedRequireList;
        this.title = title;
        this.info = info;
        this.help = help;
        this.feed = new Feed(feed.getName(),feed.getUrl(),feed.getDesc(),Feed.DEFAULT_TIMEOUT);
        this.fragmentManager =fragmentManager;
    }


    @Override
    protected int getImplLayoutId() {
        return R.layout.component_require_list_popup_view;
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        initView();

        initRecycler();

        initListener();

    }


    private void initView() {
        listTitle = findViewById(R.id.list_title);
        actionHelp = findViewById(R.id.action_help);
        actionClose = findViewById(R.id.action_close);
        recyclerView = findViewById(R.id.recycler_view);
        textInfo = findViewById(R.id.text_info);
        btnFinish = findViewById(R.id.btn_finish);

        if (!help.isHelp()){
            actionHelp.setVisibility(View.GONE);
        }else {
            actionHelp.setOnClickListener(view -> {
                // 打开帮助页面。是一个网页
                // webview的dialog
                int accentColor = ContextCompat.getColor(getContext(), com.mikepenz.materialize.R.color.accent);
                HelpDialog.create(false, accentColor, help.getContent()).show(fragmentManager, "changelog");
            });
        }

        listTitle.setText(title);

        if (!info.trim().equals("")){
            textInfo.setText(info.trim());
        }


    }

    private void initRecycler() {
        //初始化列表
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RequireListAdapter(feedRequireList);
        adapter.bindToRecyclerView(recyclerView);
        adapter.setEmptyView(R.layout.simple_empty_view,recyclerView);
    }

    private void initListener(){


        btnFinish.setOnClickListener(view -> feedSave());

        actionClose.setOnClickListener(view -> {
            dismiss();// 关闭当前弹窗
        });
    }

    private void feedSave(){

        boolean isValidate = true;//参数是否合法


        //获取adapter的参数，然后保存Feed
        boolean isNeedJointUrl = false;
        StringBuilder jointUrl = new StringBuilder();
        if (feed.getUrl()!=null){
            jointUrl = new StringBuilder(feed.getUrl());//构建订阅的域名🌽
            if (jointUrl.charAt(jointUrl.length()-1) == '/'){//末尾统一去掉/
                jointUrl = jointUrl.deleteCharAt(jointUrl.length()-1);
            }
        }

        //循环遍历参数
        for (int i = 0;i< feedRequireList.size();i++){
            FeedRequire feedRequire = feedRequireList.get(i);
            EditText editTextView = (EditText) adapter.getViewByPosition(recyclerView,i,R.id.input);
            String editText = editTextView.getText().toString();

            if (feedRequire.getType() == FeedRequire.SET_URL){
                //TODO:需要判断一下这个url正确性
                if (!feedRequire.isOptional() && editText.trim().equals("")){
                    editTextView.setError("不能为空哦");
                    isValidate = false;
                }else if (!likeRssAddr(editText)){
                    editTextView.setError("格式不正确");
                    isValidate = false;
                }else {
                    //处理正确的URL
                    if (editText.startsWith("http://")) {
                        editText = editText;
                    } else if (editText.startsWith("https://")) {
//                        editText = editText.replace("https", "http");
                        editText = editText;
                    } else {
                        editText = "http://" + editText;
                    }
                    feed.setUrl(editText);
                }
            }else if (feedRequire.getType() == FeedRequire.SET_NAME){
                if (!feedRequire.isOptional() && editText.trim().equals("")){
                    editTextView.setError("不能为空哦");
                    isValidate = false;
                }else {
                    feed.setName(editText);
                }
            }else {//拼接url
                if (!feedRequire.isOptional() && editText.trim().equals("")){
                    editTextView.setError("不能为空哦");
                    isValidate = false;
                }else {
                    if (!editText.trim().equals("")) {
                        //仅仅可选的填写不为空时，才拼接路径
                        jointUrl.append("/" + editText);
                    }
                    isNeedJointUrl = true;
                }
            }
        }

        if (isValidate){
            if (isNeedJointUrl){
                feed.setUrl(jointUrl.toString());
            }
            saveFeedToFeedFolder(feed);
            dismiss();//关闭
        }//如果不合法就不需要关闭弹窗
    }
    @Override
    protected void onShow() {
        super.onShow();
    }

    @Override
    protected void onDismiss() {
        super.onDismiss();
    }

    private void saveFeedToFeedFolder(final Feed feed){
        //显示feedFolderList 弹窗
        new ShowFeedFolderListDialogTask(new DialogCallback() {
            @Override
            public void onFinish(MaterialDialog dialog, View view, int which, CharSequence text, int targetId) {
                //移动到指定的目录下
                feed.setFeedFolderId(targetId);
                feed.setTimeout(Feed.DEFAULT_TIMEOUT);
                try{
                    feed.saveThrows();
                    Toasty.success(UIUtil.getContext(),"订阅成功").show();
                    EventBus.getDefault().post(new EventMessage(EventMessage.ADD_FEED));
                }catch (LitePalSupportException exception){
                    Toasty.info(getContext(),"该订阅已经存在了哦！").show();
                }
            }
        }, getContext(),"添加到指定的文件夹下","").execute();
    }

    private boolean likeRssAddr(String str) {
        return Patterns.WEB_URL.matcher(str).matches();
    }
}
