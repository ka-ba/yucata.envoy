package kaba.yucata.envoy.datalink;

import android.support.v4.app.LoaderManager;
//import android.app.LoaderManager;
//import android.content.AsyncTaskLoader;
import android.content.Context;
//import android.content.Loader;
import android.icu.text.ScientificNumberFormatter;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import kaba.yucata.envoy.GameCountActivity;
import kaba.yucata.envoy.StateInfo;

/**
 * Created by kaba on 08/08/17.
 */

public class LoaderHelper implements LoaderManager.LoaderCallbacks<StateInfo> {
    protected final static int LOADER_ID=1502228361;
    private static final String USERNAME_KEY = "KEY-Username";
    private final Context context;

    public LoaderHelper(Context context) {
        this.context=context;
    }

    public void loadInfoFromServer(LoaderManager loaderManager, String username) {
        Bundle bundle=new Bundle();
        bundle.putString(USERNAME_KEY,username);
        final Loader<StateInfo> loader = loaderManager.getLoader(LOADER_ID);
        if(loader==null)
            loaderManager.initLoader(LOADER_ID,bundle,this);
        // FIXME: else restart??
    }

    @Override
    public Loader<StateInfo> onCreateLoader(int i, final Bundle bundle) {
        return new AsyncTaskLoader<StateInfo>(context) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
            }

            @Override
            protected void onStopLoading() {
                super.onStopLoading();
            }

            @Override
            public StateInfo loadInBackground() {
                final String username = bundle.getString(USERNAME_KEY);
                if( username==null || TextUtils.isEmpty(username) )
                    return null;
                StateInfo info;
                // fetch from network here
                // FIXME: dummy code
                info = new StateInfo(5,2);
                return info;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<StateInfo> loader, StateInfo stateInfo) {
        // use loaded info here

    }

    @Override
    public void onLoaderReset(Loader<StateInfo> loader) {

    }
}
