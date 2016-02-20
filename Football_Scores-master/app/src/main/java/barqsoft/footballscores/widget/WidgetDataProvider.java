package barqsoft.footballscores.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import barqsoft.footballscores.R;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by kevinthomas on 2/14/16.
 */
public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    List<String> mCollection = new ArrayList<>();
    Context mContext;
    Intent mIntent;
    private Cursor mData = null;

    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.TIME_COL
    };
    // these indices must match the projection
    static final int INDEX_SCORES_HOME = 0;
    static final int INDEX_SCORES_AWAY = 1;
    static final int INDEX_SCORES_TIME = 2;


    private void initData() {
        // Always clear out any existing data first
        if (mData != null) {
            mData.close();
        }
        // This method is called by the app hosting the widget (e.g., the launcher)
        // However, our ContentProvider is not exported so it doesn't have access to the
        // data. Therefore we need to clear (and finally restore) the calling identity so
        // that calls use our process and permission
        final long identityToken = Binder.clearCallingIdentity();
        Date fragmentdate = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        String date = mformat.format(fragmentdate);
        String[] today = new String[1];
        today[0] = date;

        Uri scoresForDayUri = DatabaseContract.scores_table.buildScoreWithDate();
        mData = mContext.getContentResolver().query(scoresForDayUri,
                SCORES_COLUMNS,
                null,
                today,
                null);
        Binder.restoreCallingIdentity(identityToken);
    }

    public WidgetDataProvider(Context mContext, Intent mIntent) {
        this.mContext = mContext;
        this.mIntent = mIntent;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        // Initialize data for scores
        initData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteView = new RemoteViews(mContext.getPackageName(),
                R.layout.collection_widget);
        if (position == AdapterView.INVALID_POSITION ||
                mData == null || !mData.moveToPosition(position)) {
            remoteView.setViewVisibility(R.id.no_data, View.VISIBLE);
            return remoteView;
        }

        int time = mData.getInt(INDEX_SCORES_TIME);
        String home = mData.getString(INDEX_SCORES_HOME);
        String away = mData.getString(INDEX_SCORES_AWAY);

        remoteView = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_list_item);
        remoteView.setTextViewText(R.id.text1, home);
        remoteView.setTextViewText(R.id.text2, away);
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        // yes because we're only using one type of widget
        // and we're not adding or deleting from our static data
        return true;
    }
}
