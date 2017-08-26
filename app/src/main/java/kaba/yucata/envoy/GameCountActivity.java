package kaba.yucata.envoy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import kaba.yucata.envoy.datalink.LoaderHelper;

public class GameCountActivity extends AppCompatActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener
{

    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_GAMES_WAITING = "games_waiting";
    public static final String PREF_KEY_GAMES_TOTAL = "games_total";
    private LoaderHelper loaderHelper;
    private TextView tvUsername, tvGamesWaiting, tvGamesTotal;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // app gets created - set up basic stuff - read savedI..S.. if not null
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_count);
        tvUsername = (TextView) findViewById(R.id.tv_username);
        tvGamesWaiting = (TextView) findViewById(R.id.tv_num_games_waiting);
        tvGamesTotal = (TextView) findViewById(R.id.tv_num_games_total);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // initialize display fields
        tvUsername.setText(sharedPrefs.getString(PREF_KEY_USERNAME, String.valueOf(R.string.username_init_txt)) );
        showPrefInTV(tvGamesWaiting,PREF_KEY_GAMES_WAITING,sharedPrefs);
        showPrefInTV(tvGamesTotal,PREF_KEY_GAMES_TOTAL,sharedPrefs);
        // listen for changes
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        loaderHelper = new LoaderHelper(this);
    }

    @Override
    protected void onDestroy() {
        // clean up
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        // app gets destoyes by framework - safe critical stuff, clean up
        // nearly never called - onStop more often (but need to handle that??)
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        // app already runs, but moves from background to foreground - maybe update info
        super.onStart();
        loaderHelper.loadInfoFromServer(getSupportLoaderManager(),"USERNAME");  // FIXME:
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // put code to safe info here: outState.putXYZ
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gamecount_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId() == R.id.mitem_settings ) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if( PREF_KEY_GAMES_WAITING.equals(key))
            showPrefInTV(tvGamesWaiting,PREF_KEY_GAMES_WAITING,sharedPreferences);
        else if( PREF_KEY_GAMES_TOTAL.equals(key))
            showPrefInTV(tvGamesTotal,PREF_KEY_GAMES_TOTAL,sharedPreferences);
        else if( PREF_KEY_USERNAME.equals(key))
        tvUsername.setText(sharedPrefs.getString(PREF_KEY_USERNAME, String.valueOf(R.string.username_init_txt)) );
    }

    private void showPrefInTV(TextView tv, String pref_key, SharedPreferences sharedPrefs) {
        final int pref_value = sharedPrefs.getInt(pref_key, -1);
        tv.setText((pref_value>-1?String.valueOf(pref_value):"X"));
    }

}
