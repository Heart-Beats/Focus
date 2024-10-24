package com.ihewro.focus.db;

import org.litepal.LitePal;
import org.litepal.tablemanager.callback.DatabaseListener;

/**
 * @author 张磊  on  2024/10/24 at 11:04
 * Email: 913305160@qq.com
 */
public class MyDatabaseListener implements DatabaseListener {

    @Override
    public void onCreate() {
    }

    @Override
    public void onUpgrade(int oldVersion, int newVersion) {
        if (newVersion >= 35) {
            // 升级表时手动添加组合唯一约束
            String SQL = "CREATE UNIQUE INDEX IF NOT EXISTS idx_feedItem_unique ON FeedItem (feedurl, guid)";
            LitePal.getDatabase().execSQL(SQL);

            SQL = "CREATE UNIQUE INDEX IF NOT EXISTS idx_collection_unique ON Collection (feedurl, guid)";
            LitePal.getDatabase().execSQL(SQL);
        }
    }


}