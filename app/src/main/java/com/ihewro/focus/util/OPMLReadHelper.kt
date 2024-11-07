package com.ihewro.focus.util;

import android.Manifest;
import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.blankj.ALog;
import com.google.common.base.Strings;
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.activity.FeedManageActivity;
import com.ihewro.focus.bean.EventMessage;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.FeedFolder;
import com.ihewro.focus.parser.OPMLParser;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/05/11
 *     desc   :
 *     thx: 该类大部分直接使用的feeder2 开源项目的代码OPMLHelper
 *     version: 1.0
 * </pre>
 */
public class OPMLReadHelper {

    public static final int RQUEST_STORAGE_READ = 8;

    private Activity activity;

    public OPMLReadHelper(Activity activity) {
        this.activity = activity;
    }

    public void run(){
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(activity, perms)) {
            //有权限
            new FileChooserDialog.Builder(activity)
                    .initialPath(GlobalConfig.appDirPath)  //初始显示了目录
                    .extensionsFilter(".opml",".xml") //选择的文件类型
                    .tag("optional-identifier")
                    .goUpLabel("上一级")
                    .show((FeedManageActivity)activity);


        } else {
            //没有权限 1. 申请权限
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(activity, RQUEST_STORAGE_READ, perms)
                            .setRationale("必须读存储器才能解析OPML文件")
                            .setPositiveButtonText("确定")
                            .setNegativeButtonText("取消")
                            .build());
        }


    }
    public void add(String filePath) {

        if (Strings.isNullOrEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        try {
            List<FeedFolder> feedFolders = OPMLParser.INSTANCE.readOPML(filePath);
            //保存到指定的文件夹中
            saveFeedToFeedFolder(feedFolders);

            Toasty.success(activity, "导入成功", Toast.LENGTH_SHORT).show();
        } catch (XmlPullParserException e) {

            Toast.makeText(activity, "文件格式错误", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(activity, "文件导入失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFeedToFeedFolder(final List<FeedFolder> feedFolders){
        //显示feedFolderList 弹窗

        for (int i = 0;i < feedFolders.size();i++){
            //首先检查这个feedFolder名称是否存在。
            FeedFolder feedFolder = feedFolders.get(i);
            List<FeedFolder> temp = LitePal.where("name = ?",feedFolder.getName()).find(FeedFolder.class);
            if (temp.size()<1){
                if (feedFolder.getName() == null || feedFolder.getName().equals("")){
                    feedFolder.setName("导入的未命名文件夹");
                }
                //新建一个feedFolder
                feedFolder.save();
            }else {//存在这个名称
                feedFolder.setId(temp.get(0).getId());//使用是数据库的对象
            }
            //将导入的feed全部保存
            for (Feed feed:feedFolder.getFeedList()) {
                feed.setFeedFolderId(feedFolder.getId());
                ALog.d(feed.getUrl());
                feed.save();
            }

            new Handler().postDelayed(() -> EventBus.getDefault().post(new EventMessage(EventMessage.IMPORT_OPML_FEED)), 500);
        }

        Toasty.success(activity,"导入成功！").show();
    }
}
