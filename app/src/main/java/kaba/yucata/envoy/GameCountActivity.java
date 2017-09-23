package kaba.yucata.envoy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import kaba.yucata.envoy.datalink.CommunicationException;
import kaba.yucata.envoy.datalink.LoaderTask;
import kaba.yucata.envoy.service.DataService;

import static kaba.yucata.envoy.GameCountActivity.STATES.STATE_OK;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_INTERVAL_MIN;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_TIME_LAST_LOAD;

public class GameCountActivity extends AppCompatActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener,
        View.OnClickListener
{
    public final boolean DEBUG=true;
    private static final long RELOAD_WAIT_MILLIS = 120000;  // TODO: 300000 = 5 min better / necessary?
    private CountDownTimer reloadCountdown=null;

    public enum STATES { STATE_OK,STATE_ERROR};
    private STATES state;
    private boolean buttonErrorHidden=false;
    private DataService dataService=null;
    private TextView tvUsername, tvGamesWaiting, tvGamesTotal, tvInvites, tvState;
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
        tvState = (TextView) findViewById(R.id.tv_state);
        bReload.setOnClickListener(this);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // initialize display fields
        refreshDisplayedValues();
        if( PrefsHelper.canReload(sharedPrefs) )
            releaseButtonFromError();
        else
            hideButtonDueToError();
        // listen for changes
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        dataService = DataService.getService(this, 999999);  // FIXME: eliminate interval from constructor?
        setDataServiceInterval();
    }

    private void setDataServiceInterval() {
        final int interval = PrefsHelper.stringPrefToInt(sharedPrefs, PREF_KEY_INTERVAL_MIN, 60, this);
        if( interval != 999999 ) {  // 999999 means: no service
            dataService.setParamenters(interval);
            dataService.ensureRunning();
        } else
            dataService.ensureStopped();
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
        hideButtonByCountdown();  // also tests if necessary
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopButtonCountdown();
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
        if( PrefsHelper.PREF_KEY_GAMES_WAITING.equals(key))
            showIntPrefInTV(tvGamesWaiting, PrefsHelper.PREF_KEY_GAMES_WAITING,sharedPreferences);
        else if( PrefsHelper.PREF_KEY_GAMES_TOTAL.equals(key))
            showIntPrefInTV(tvGamesTotal, PrefsHelper.PREF_KEY_GAMES_TOTAL,sharedPreferences);
        else if( PrefsHelper.PREF_KEY_INVITES.equals(key))
            showIntPrefInTV(tvInvites, PrefsHelper.PREF_KEY_INVITES,sharedPreferences);

        else if( PrefsHelper.PREF_KEY_USERNAME.equals(key)) {
            tvUsername.setText(sharedPrefs.getString(PrefsHelper.PREF_KEY_USERNAME, String.valueOf(R.string.username_init_txt)) );
            PrefsHelper.prefChangeUsername(sharedPreferences);
        } else if( PrefsHelper.PREF_KEY_SECRET.equals(key)) {
            PrefsHelper.prefChangePassword(sharedPreferences);
        } else if( PrefsHelper.PREF_KEY_INTERVAL_MIN.equals(key))
            setDataServiceInterval();

        else if( PrefsHelper.PREF_KEY_TIME_LAST_LOAD.equals(key))
            hideButtonByCountdown();

        PrefsHelper.clearPrefsBecausePrefChanged(sharedPreferences,key);

        if( PrefsHelper.canReload(sharedPreferences) )
            releaseButtonFromError();
        else
            hideButtonDueToError();
        updateStateTV();
    }

    private void refreshDisplayedValues() {
        tvUsername.setText(sharedPrefs.getString(PrefsHelper.PREF_KEY_USERNAME, String.valueOf(R.string.username_init_txt)) );
        showIntPrefInTV(tvGamesWaiting, PrefsHelper.PREF_KEY_GAMES_WAITING,sharedPrefs);
        showIntPrefInTV(tvGamesTotal, PrefsHelper.PREF_KEY_GAMES_TOTAL,sharedPrefs);
        showIntPrefInTV(tvInvites, PrefsHelper.PREF_KEY_INVITES,sharedPrefs);
        updateStateTV();
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
        if(view.getId()==R.id.b_reload) {
            final int interval = PrefsHelper.stringPrefToInt(sharedPrefs, PREF_KEY_INTERVAL_MIN, 60, this);
            if( interval != 999999 )  // 999999 means: no service
                dataService.resetTimer();
            loadInfo();
        }
        // FIXME: super...?
    }

    public void setState(STATES s) {
        STATES old=state;
        state=s;
        if(old!=state)
            refreshDisplayedValues();
    }

    private void loadInfo() {
        if(true&&DEBUG) System.out.println("loading info");
        new LoaderTask.LTActivity(this,sharedPrefs).execute(this);
    }

    private void hideButtonByCountdown() {
        if(true&&DEBUG) System.out.println("hiding button by countdown");
        final long tload = sharedPrefs.getLong(PREF_KEY_TIME_LAST_LOAD, 1L);
        final long tdiff = tload + RELOAD_WAIT_MILLIS - System.currentTimeMillis();
        if( tdiff >= 2000 ) {
            bReload.setEnabled(false);
            reloadCountdown = new CountDownTimer(tdiff,1000) {
                @Override
                public void onTick(long tleft) {
                    final long mleft = tleft / 60000;
                    final long sleft = (tleft-mleft) / 1000;
                    bReload.setText( String.format("%d:%02d",mleft,sleft));
                }
                @Override
                public void onFinish() {
                    stopButtonCountdown(false);
                }
            }.start();
        } else
            stopButtonCountdown(true);
    }

    private void stopButtonCountdown() {
        stopButtonCountdown(true);
    }
    private void stopButtonCountdown( boolean stop_timer ) {
        if(true&&DEBUG) System.out.println("stopping countdown");
        if(stop_timer)
            stopCountdownTimer();
        if( ! buttonErrorHidden ) {
            bReload.setText(R.string.button_reload);
            bReload.setEnabled(true);
        }
    }

    private void stopCountdownTimer() {
        if(true&&DEBUG) System.out.println("stopping countdown timer");
        if(reloadCountdown!=null) {
            reloadCountdown.cancel();
            reloadCountdown = null;
        }
    }

    private void hideButtonDueToError() {
        if(true&&DEBUG) System.out.println("hiding button due to error");
        buttonErrorHidden=true;
        stopCountdownTimer();
        bReload.setText( R.string.button_reload_disabled );
        bReload.setEnabled(false);
    }

    private void releaseButtonFromError() {
        if(true&&DEBUG) System.out.println("releasing button from error");
        buttonErrorHidden=false;
        if(reloadCountdown==null) {
            bReload.setText( R.string.button_reload );
            bReload.setEnabled(true);
        }
    }

    private void updateStateTV() {
        final String state_text = PrefsHelper.getCurrentStateText(this,sharedPrefs);
        tvState.setText( state_text );
        if( (state_text==null) || (state_text.isEmpty()) )
            tvState.setVisibility(View.INVISIBLE);
        else
            tvState.setVisibility(View.VISIBLE);
    }
}
