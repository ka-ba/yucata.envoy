package kaba.yucata.envoy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView tv = (TextView) findViewById(R.id.about_lic_tv);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        final String text = getString(R.string.about_lic_text,BuildConfig.VERSION_NAME);
        tv.setText(Html.fromHtml(text));
    }
}
