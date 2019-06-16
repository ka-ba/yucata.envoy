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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import kaba.yucata.envoy.datalink.LoaderTask;
import kaba.yucata.envoy.service.DataService;
import kaba.yucata.envoy.util.DebugHelper;
import kaba.yucata.envoy.util.DiagnosticActivity;

import static kaba.yucata.envoy.GameCountActivity.STATES.STATE_OK;

public class GameCountActivity extends AppCompatActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener,
        View.OnClickListener
{
    public final boolean DEBUG=BuildConfig.DEBUG;
    private CountDownTimer reloadCountdown=null;

    public enum STATES { STATE_OK,STATE_ERROR};
    private STATES state;
    private boolean buttonErrorHidden=false;
    private DataService dataService=null;
    private LinearLayout llContent;
    private TextView tvUsername, tvGamesWaiting, tvGamesTotal, tvInvites, tvState, tvError;
    private Button bReload;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // app gets created - set up basic stuff - read savedI..S.. if not null
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_count);
        state=STATE_OK;
        tvError = (TextView) findViewById(R.id.tv_error);
        try {
            llContent = (LinearLayout) findViewById(R.id.ll_content);
            tvUsername = (TextView) findViewById(R.id.tv_username);
            tvGamesWaiting = (TextView) findViewById(R.id.tv_num_games_waiting);
            tvGamesWaiting.setOnClickListener(this);
            tvGamesTotal = (TextView) findViewById(R.id.tv_num_games_total);
            tvGamesTotal.setOnClickListener(this);
            tvInvites = (TextView) findViewById(R.id.tv_num_pers_invites);
            bReload = (Button) findViewById(R.id.b_reload);
            tvState = (TextView) findViewById(R.id.tv_state);
            bReload.setOnClickListener(this);
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            // initialize display fields
            refreshDisplayedValues();
            if (PrefsHelper.canReload(sharedPrefs))
                releaseButtonFromError();
            else
                hideButtonDueToError();
            // listen for changes
            sharedPrefs.registerOnSharedPreferenceChangeListener(this);
            try {
                dataService = DataService.getService(this, 999999);  // FIXME: eliminate interval from constructor?
                PrefsHelper.setService(sharedPrefs,null,true);
            } catch (Exception e) {
                dataService = null;
                PrefsHelper.setService(sharedPrefs,null,false);
            }
            setDataServiceInterval();
        } catch(Throwable t) {
            llContent.setVisibility(View.INVISIBLE);
            if(true&&DEBUG) System.out.println(DebugHelper.allToString(t));
            tvError.setText( DebugHelper.allToString(t) );
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private void setDataServiceInterval() {
        if(dataService==null)
            return;
        final int interval = PrefsHelper.getIntervalMin(sharedPrefs, 60, this);
        if( interval != 999999 ) {  // 999999 means: no service
            dataService.setParamenters(interval);
            dataService.ensureRunning();
        } else
            dataService.ensureStopped();
    }

    public boolean hasDataService() { return dataService!=null; }

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
        try {
            hideButtonByCountdown();  // also tests if necessary
            if( (!hasDataService()) || (!dataService.isRunning()) )
                loadInfo();
        } catch(Throwable t) {
            llContent.setVisibility(View.INVISIBLE);
            tvError.setText( DebugHelper.allToString(t) );
            tvError.setVisibility(View.VISIBLE);
        }
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

    private void fireActivityIntent(Class<?> klasse) {
        final Intent settings_intent = new Intent(this,klasse);
        startActivity(settings_intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mitem_settings:
                fireActivityIntent(SettingsActivity.class);
//                final Intent settings_intent = new Intent(this, SettingsActivity.class);
//                startActivity(settings_intent);
                return true;
            case R.id.mitem_diagnostic:
                fireActivityIntent(DiagnosticActivity.class);
//                final Intent diagnostic_intent = new Intent(this, DiagnosticActivity.class);
//                startActivity(diagnostic_intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if( PrefsHelper.PREF_KEY_GAMES_WAITING.equals(key))
            showIntInTV( tvGamesWaiting, PrefsHelper.getGamesWaiting(sharedPreferences,-1) );
        else if( PrefsHelper.PREF_KEY_GAMES_TOTAL.equals(key))
            showIntInTV( tvGamesTotal, PrefsHelper.getGamesTotal(sharedPreferences,-1) );
        else if( PrefsHelper.PREF_KEY_INVITES.equals(key))
            showIntInTV( tvInvites, PrefsHelper.getPersInvites(sharedPreferences,-1) );

        else if( PrefsHelper.PREF_KEY_USERNAME.equals(key)) {
            tvUsername.setText( PrefsHelper.getUsername(sharedPrefs,getString(R.string.username_init_txt)) );
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
        tvUsername.setText( PrefsHelper.getUsername(sharedPrefs,getString(R.string.username_init_txt)) );
//        showIntPrefInTV(tvGamesWaiting, PrefsHelper.PREF_KEY_GAMES_WAITING,sharedPrefs);
//        showIntPrefInTV(tvGamesTotal, PrefsHelper.PREF_KEY_GAMES_TOTAL,sharedPrefs);
//        showIntPrefInTV(tvInvites, PrefsHelper.PREF_KEY_INVITES,sharedPrefs);
        showIntInTV( tvGamesWaiting, PrefsHelper.getGamesWaiting(sharedPrefs,-1) );
        showIntInTV( tvGamesTotal, PrefsHelper.getGamesTotal(sharedPrefs,-1) );
        showIntInTV( tvInvites, PrefsHelper.getPersInvites(sharedPrefs,-1) );
        updateStateTV();
    }

    private void showIntPrefInTV(TextView tv, String pref_key, SharedPreferences sharedPrefs) {
        showIntInTV( tv, sharedPrefs.getInt(pref_key,-1) );
//        int pref_value=9999;
//        switch(state) {
//            case STATE_OK:
//                pref_value = sharedPrefs.getInt(pref_key, -1);
//                break;
//            case STATE_ERROR:
//                pref_value = -1;  // yields X
//                break;
//        }
//        tv.setText((pref_value>-1?String.valueOf(pref_value):"X"));
    }

    private void showIntInTV(TextView tv, int i) {
        tv.setText( ( (state==STATE_OK)&&(i>-1) ? String.valueOf(i) : "X" ) );
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.b_reload) {
            final int interval = PrefsHelper.getIntervalMin(sharedPrefs, 60, this);
            if( (interval != 999999) && (dataService!=null) )  // 999999 means: no service
                dataService.resetTimer();
            loadInfo();
        } else if(view.getId()==R.id.tv_num_games_waiting) {
//            Toast.makeText(this,"WAITING",Toast.LENGTH_LONG).show();
            fireActivityIntent(GameListActivity.class);
        } else if(view.getId()==R.id.tv_num_games_total) {
//            Toast.makeText(this,"TOTAL",Toast.LENGTH_LONG).show();
            fireActivityIntent(GameListActivity.class);
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
        if( PrefsHelper.isLoadBlocked(sharedPrefs) ) {
            if(true&&DEBUG) System.out.println(DebugHelper.textAndTraceHead("loading blocked",4));
            return;  // no op
        }
        if(true&&DEBUG) System.out.println(DebugHelper.textAndTraceHead("loading info",4));
        new LoaderTask.LTActivity(this,sharedPrefs).execute(this);
    }

    private void hideButtonByCountdown() {
        if(true&&DEBUG) System.out.println(DebugHelper.textAndTraceHead("hiding button by countdown",4));
        final long t_to_wait = PrefsHelper.loadBlockingLeftMillis(sharedPrefs);
        if( t_to_wait >= 2000 ) {
            bReload.setEnabled(false);
            reloadCountdown = new CountDownTimer(t_to_wait,1000) {
                @Override
                public void onTick(long tleft) {
                    final long mleft = tleft / 60000;
                    final long sleft = (tleft-mleft*60000) / 1000;
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
        if(false&&DEBUG) System.out.println(DebugHelper.textAndTraceHead("stopping countdown",4));
        if(stop_timer)
            stopCountdownTimer();
        if( ! buttonErrorHidden ) {
            bReload.setText(R.string.button_reload);
            bReload.setEnabled(true);
        }
    }

    private void stopCountdownTimer() {
        if(false&&DEBUG) System.out.println(DebugHelper.textAndTraceHead("stopping countdown timer",4));
        if(reloadCountdown!=null) {
            reloadCountdown.cancel();
            reloadCountdown = null;
        }
    }

    private void hideButtonDueToError() {
        if(true&&DEBUG) System.out.println(DebugHelper.textAndTraceHead("hiding button due to error",4));
        buttonErrorHidden=true;
        stopCountdownTimer();
        bReload.setText( R.string.button_reload_disabled );
        bReload.setEnabled(false);
    }

    private void releaseButtonFromError() {
        if(true&&DEBUG) System.out.println(DebugHelper.textAndTraceHead("releasing button from error",4) );
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
