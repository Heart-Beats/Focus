package com.ihewro.focus.view;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ihewro.focus.R;
import com.ihewro.focus.bean.UserPreference;
import com.lxj.xpopup.core.DrawerPopupView;

import org.litepal.crud.callback.FindMultiCallback;

import java.util.Arrays;
import java.util.List;

import skin.support.utils.SkinPreference;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/05/10
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class FilterPopupView extends DrawerPopupView {

    public static final String ORDER_BY_NEW = "ORDER_BY_NEW";
    public static final String ORDER_BY_OLD = "ORDER_BY_OLD";

    public static final String SHOW_ALL = "SHOW_ALL";
    public static final String SHOW_UNREAD = "SHOW_UNREAD";
    public static final String SHOW_STAR = "SHOW_STAR";

    TextView newestTv;
    LinearLayout newestCard;
    TextView oldTv;
    LinearLayout oldCard;
    TextView allTv;
    LinearLayout allCard;
    TextView readTv;
    LinearLayout readCard;
    TextView starTv;
    LinearLayout starCard;

    private LinearLayout newestC;
    private boolean isNeedUpdate = false;

    private String orderChoice = ORDER_BY_NEW;//当前排序选择
    private String filterChoice = SHOW_ALL;//当前筛选器的选择



    List<String> orderOperation = Arrays.asList(ORDER_BY_NEW,ORDER_BY_OLD);
    List<Integer> orderCardViews = Arrays.asList(R.id.newest_card, R.id.old_card);
    List<Integer> orderTextViews = Arrays.asList(R.id.newest_tv, R.id.old_tv);


    List<String> filterOperation = Arrays.asList(SHOW_ALL,SHOW_UNREAD,SHOW_STAR);
    List<Integer> filterCardViews = Arrays.asList(R.id.all_card, R.id.read_card, R.id.star_card);
    List<Integer> filterTextViews = Arrays.asList(R.id.all_tv, R.id.read_tv, R.id.star_tv);

    int normalTextColor;
    int normalBGColor;
    int highlightTextColor;
    int highlightBGColor;


    public FilterPopupView(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.component_filter_drawer;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        Log.e("tag", "CustomDrawerPopupView onCreate");


        initView();

        //初始化颜色

        if (SkinPreference.getInstance().getSkinName().equals("night")){
            normalTextColor = getResources().getColor(com.mikepenz.materialdrawer.R.color.material_drawer_dark_selected_text);
            normalBGColor = getResources().getColor(com.mikepenz.materialdrawer.R.color.material_drawer_dark_selected);

            highlightTextColor = getResources().getColor(com.mikepenz.materialdrawer.R.color.material_drawer_dark_primary_text);
            highlightBGColor = getResources().getColor(com.mikepenz.materialdrawer.R.color.material_drawer_dark_background);

        }else {
            normalTextColor = getResources().getColor(R.color.colorAccent);
            normalBGColor = getResources().getColor(com.mikepenz.materialdrawer.R.color.material_drawer_selected);

            highlightTextColor = getResources().getColor(com.mikepenz.materialdrawer.R.color.material_drawer_primary_text);
            highlightBGColor = getResources().getColor(com.mikepenz.materialdrawer.R.color.material_drawer_background);
        }

        //初始化选项
        //获取用户设置中的位置
        orderChoice = UserPreference.queryValueByKey(UserPreference.ODER_CHOICE,ORDER_BY_NEW);
        filterChoice = UserPreference.queryValueByKey(UserPreference.FILTER_CHOICE,SHOW_ALL);

        clickOrderList(orderOperation.indexOf(orderChoice));

        clickFilterList(filterOperation.indexOf(filterChoice));


        initListener();
    }

    private void initView(){
        newestTv = findViewById(R.id.newest_tv);
        newestCard = findViewById(R.id.newest_card);
        oldTv = findViewById(R.id.old_tv);
        oldCard = findViewById(R.id.old_card);
        allTv = findViewById(R.id.all_tv);
        allCard = findViewById(R.id.all_card);
        readTv = findViewById(R.id.read_tv);
        readCard = findViewById(R.id.read_card);
        starTv = findViewById(R.id.star_tv);
        starCard = findViewById(R.id.star_card);

    }

    private void initListener() {

        for (int i = 0; i < orderOperation.size();i++){
            final int finalI = i;
            findViewById(orderCardViews.get(i)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    isNeedUpdate = true;
                    clickOrderList(finalI);
                }
            });
        }

        for (int i = 0; i < filterOperation.size();i++){
            final int finalI = i;
            findViewById(filterCardViews.get(i)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    isNeedUpdate = true;
                    clickFilterList(finalI);
                }
            });
        }
    }

    @Override
    protected void onShow() {
        super.onShow();
        Log.e("tag", "CustomDrawerPopupView onShow");
    }

    @Override
    protected void onDismiss() {
        super.onDismiss();
        Log.e("tag", "CustomDrawerPopupView onDismiss");
    }






    /**
     * 点击了排序的列表
     * @param position
     */
    private void clickOrderList(final int position){
        orderChoice = orderOperation.get(position);
        UserPreference.updateOrSaveValueByKeyAsync(UserPreference.ODER_CHOICE, orderChoice, new FindMultiCallback<UserPreference>() {
            @Override
            public void onFinish(List<UserPreference> list) {
                //修改当前项为高亮
                ((TextView)findViewById(orderTextViews.get(position))).setTextColor(normalTextColor);
                findViewById(orderCardViews.get(position)).setBackgroundColor(normalBGColor);

                //修改其他项为普通颜色
                for (int i = 0; i < orderOperation.size(); i++){
                    if (i != position){
                        ((TextView)findViewById(orderTextViews.get(i))).setTextColor(highlightTextColor);
                        findViewById(orderCardViews.get(i)).setBackgroundColor(highlightBGColor);

                    }
                }
            }
        });


    }

    /**
     * 点击了筛选的列表
     * @param position
     */
    private void clickFilterList(int position){
        filterChoice = filterOperation.get(position);
        UserPreference.updateOrSaveValueByKey(UserPreference.FILTER_CHOICE,filterChoice);
        //修改当前项为高亮
        ((TextView)findViewById(filterTextViews.get(position))).setTextColor(normalTextColor);
        findViewById(filterCardViews.get(position)).setBackgroundColor(normalBGColor);

        //修改其他项为普通颜色
        for (int i = 0; i < filterOperation.size();i++){
            if (i != position){
                ((TextView)findViewById(filterTextViews.get(i))).setTextColor(highlightTextColor);
                findViewById(filterCardViews.get(i)).setBackgroundColor(highlightBGColor);
            }
        }
    }

    public String getOrderChoice() {
        return orderChoice;
    }

    public String getFilterChoice() {
        return filterChoice;
    }

    public boolean isNeedUpdate() {
        return isNeedUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        isNeedUpdate = needUpdate;
    }
}