package kaba.yucata.envoy;

import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v4.app.LoaderManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import kaba.yucata.envoy.datalink.LoaderHelper;

public class GameCountActivity extends AppCompatActivity {

    private LoaderHelper loaderHelper;
    private TextView tvUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // app gets created - set up basic stuff - read savedI..S.. if not null
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_count);
        tvUsername = (TextView) findViewById(R.id.tv_username);
        tvUsername.setText(
                PreferenceManager.getDefaultSharedPreferences(this).getString("username", String.valueOf(R.string.username_init_txt)) );
        loaderHelper = new LoaderHelper(this);
    }

    @Override
    protected void onDestroy() {
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
}
