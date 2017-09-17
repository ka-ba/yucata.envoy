package kaba.yucata.envoy;

import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * Created by kaba on 23/08/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_gamecount);
        // pre-set summaries
        setSummary( findPreference(getString(R.string.k_pref_username)), null);
        setSummary( findPreference(getString(R.string.k_pref_password)), null);
        setSummary( findPreference(getString(R.string.k_pref_interval_min)), null);
        // register listeners for updating summaries
        findPreference(getString(R.string.k_pref_username)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.k_pref_password)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.k_pref_interval_min)).setOnPreferenceChangeListener(this);
    }

    private void setSummary(Preference pref, Object new_val) {
        // if newVal is pecified, use it; else obtain below
        String summary=( new_val==null ? null : new_val.toString() );

        // username
        if(pref.getKey().equals( getString(R.string.k_pref_username) )) {
            if(summary==null)
                summary = ((EditTextPreference) pref).getText();
            if( (summary==null) || (summary.length()<1) )
                summary=getText(R.string.username_init_txt).toString();
        // password / secret
        } else if(pref.getKey().equals( getString(R.string.k_pref_password) )) {
            if (new_val != null) {
                if (((String) new_val).length() == 0)
                    summary = "Pasword not specified";
                else
                    summary = "Password set";  // TODO: mark if successfully used or rejected
            } else {
                final String pw = ((EditTextPreference) pref).getText();
                if ((pw == null) || (pw.length() == 0))
                    summary = "Pasword not specified";
                else
                    summary = "Password set";  // TODO: mark if successfully used or rejected
            }
        // polling interval
        } else if(pref.getKey().equals( getString(R.string.k_pref_interval_min) )) {
            final ListPreference lpref = (ListPreference) pref;
            if (summary == null)
                summary = ""+lpref.getEntry();
            else
                summary = lpref.getEntries()[lpref.findIndexOfValue((String)new_val)].toString();
            summary = "Interval set to "+summary;
        }

        if(summary!=null) {
            pref.setSummary(summary);
            System.out.println("setting "+pref.getKey()+"'s summary to "+summary);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        setSummary(pref, newValue);
        return true;
    }
}
