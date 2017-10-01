package kaba.yucata.envoy.util;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.widget.TextView;

import java.util.Map;

import kaba.yucata.envoy.BuildConfig;
import kaba.yucata.envoy.PrefsHelper;
import kaba.yucata.envoy.R;

import static kaba.yucata.envoy.PrefsHelper.PREF_KEYS;
import static kaba.yucata.envoy.PrefsHelper.PREF_KEY_SECRET;

public class DiagnosticActivity extends AppCompatActivity {
    private TextView tvDiag;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic);
        tvDiag= (TextView) findViewById(R.id.tv_diagnstic);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Map<String, ?> prefs = sharedPrefs.getAll();
        final StringBuilder builder = new StringBuilder();
        builder.append("version ").append(BuildConfig.VERSION_NAME).append('\n');
        builder.append("andriod api ").append(Build.VERSION.SDK_INT).append('\n');
        for( String key : PREF_KEYS ) {
            builder.append(key).append(" = ");
            final Object value = prefs.get(key);
            if(value==null)
                builder.append("<null>").append('\n');
            else if(key.equals(PREF_KEY_SECRET)) {
                if(((String)value).isEmpty())
                    builder.append("<empty>").append('\n');
                else
                    builder.append("[hidden]").append('\n');
            } else
                builder.append(value.toString()).append('\n');
        }
        tvDiag.setText( builder.toString() );
    }
}
