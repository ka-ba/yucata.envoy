package kaba.yucata.envoy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import kaba.yucata.envoy.util.DiagnosticActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mitem_about:
                final Intent about_intent = new Intent(this, AboutActivity.class);
                startActivity(about_intent);
                return true;
            case R.id.mitem_diagnostic:
                final Intent diagnostic_intent = new Intent(this, DiagnosticActivity.class);
                startActivity(diagnostic_intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
