package kaba.yucata.envoy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.security.NoSuchAlgorithmException;

import kaba.yucata.envoy.datalink.LoaderHelper;

import static kaba.yucata.envoy.GameCountActivity.STATES.STATE_OK;

public class GameCountActivity extends AppCompatActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener,
        View.OnClickListener
{
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_SECRET = "secret";
    public static final String PREF_KEY_GAMES_WAITING = "games_waiting";
    public static final String PREF_KEY_GAMES_TOTAL = "games_total";
    public static final String PREF_KEY_INVITES = "pers_invites";
    public static final String PREF_KEY_TOKEN_BASE64 = "token";
    public static final String PREF_KEY_LAST_RESPONSE = "last_response_code";
    public enum STATES { STATE_OK,STATE_ERROR};
    private STATES state;
    private LoaderHelper loaderHelper;
    private TextView tvUsername, tvGamesWaiting, tvGamesTotal, tvInvites;
    private Button bReload;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // app gets created - set up basic stuff - read savedI..S.. if not null
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_count);
        state=STATE_OK;
        tvUsername = (TextView) findViewById(R.id.tv_username);
        tvGamesWaiting = (TextView) findViewById(R.id.tv_num_games_waiting);
        tvGamesTotal = (TextView) findViewById(R.id.tv_num_games_total);
        tvInvites = (TextView) findViewById(R.id.tv_num_pers_invites);
        bReload = (Button) findViewById(R.id.b_reload);
        bReload.setOnClickListener(this);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // initialize display fields
        refreshDisplayedValues();
        // listen for changes
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        try {
            loaderHelper = new LoaderHelper(this);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // FIXME: BIG BADABOOM
            throw new RuntimeException("missing hash algo",e);
        }
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
        loaderHelper.loadInfoFromServer(getSupportLoaderManager(),sharedPrefs.getString(PREF_KEY_USERNAME, "X"));
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
            showIntPrefInTV(tvGamesWaiting,PREF_KEY_GAMES_WAITING,sharedPreferences);
        else if( PREF_KEY_GAMES_TOTAL.equals(key))
            showIntPrefInTV(tvGamesTotal,PREF_KEY_GAMES_TOTAL,sharedPreferences);
        else if( PREF_KEY_INVITES.equals(key))
            showIntPrefInTV(tvInvites,PREF_KEY_INVITES,sharedPreferences);
        else if( PREF_KEY_USERNAME.equals(key))
            tvUsername.setText(sharedPrefs.getString(PREF_KEY_USERNAME, String.valueOf(R.string.username_init_txt)) );
    }

    private void refreshDisplayedValues() {
        tvUsername.setText(sharedPrefs.getString(PREF_KEY_USERNAME, String.valueOf(R.string.username_init_txt)) );
        showIntPrefInTV(tvGamesWaiting,PREF_KEY_GAMES_WAITING,sharedPrefs);
        showIntPrefInTV(tvGamesTotal,PREF_KEY_GAMES_TOTAL,sharedPrefs);
        showIntPrefInTV(tvInvites,PREF_KEY_INVITES,sharedPrefs);
    }

    private void showIntPrefInTV(TextView tv, String pref_key, SharedPreferences sharedPrefs) {
        int pref_value=9999;
        switch(state) {
            case STATE_OK:
                pref_value = sharedPrefs.getInt(pref_key, -1);
                break;
            case STATE_ERROR:
                pref_value = -1;  // yields X
                break;
        }
        tv.setText((pref_value>-1?String.valueOf(pref_value):"X"));
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.b_reload)
            loaderHelper.loadInfoFromServer(getSupportLoaderManager(),sharedPrefs.getString(PREF_KEY_USERNAME, "X"));
        // FIXME: super...?
    }

    public void setState(STATES s) {
        STATES old=state;
        state=s;
        if(old!=state)
            refreshDisplayedValues();
    }
}
