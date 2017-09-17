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
        final EditTextPreference p_username = (EditTextPreference) findPreference(getString(R.string.k_pref_username));
        final EditTextPreference p_password = (EditTextPreference) findPreference(getString(R.string.k_pref_password));
        final ListPreference p_interval = (ListPreference) findPreference(getString(R.string.k_pref_interval_min));
        // p_notification not needed here
        // pre-set summaries
        setSummary(p_username,p_username.getText());
        setSummary(p_password,p_password.getText());
        setSummary(p_interval,p_interval.getValue());
        setNotiActivityAfterInterval(p_interval.getValue());
        // register listeners for updating summaries
        p_username.setOnPreferenceChangeListener(this);
        p_password.setOnPreferenceChangeListener(this);
        p_interval.setOnPreferenceChangeListener(this);
    }

//    private void setSummary(Preference pref) {
//        if( pref instanceof EditTextPreference )
//            setSummary( pref, ((EditTextPreference) pref).getText() );
//        else if( pref instanceof ListPreference )
//            setSummary( pref, ((ListPreference) pref).getEntry() );
//        else
//            throw new InternalError("implement additional preferences type here");
//    }

    private void setSummary(Preference pref, Object val) {
        String summary=null;

        // username
        if(pref.getKey().equals( getString(R.string.k_pref_username) )) {
            final String sval = (String) val;
            summary = ( (sval==null)||(sval.length()<1)
                    ? getText(R.string.username_init_txt).toString() : sval );
        // password / secret
        } else if(pref.getKey().equals( getString(R.string.k_pref_password) )) {
            final String sval = (String) val;
            summary = ( (sval==null)||(sval.length()<1)
                    ? "Pasword not specified" : "Password set" );
        // polling interval
        } else if(pref.getKey().equals( getString(R.string.k_pref_interval_min) )) {
            final String sval = (String) val;
            final ListPreference lpref = (ListPreference) pref;
            final CharSequence text = lpref.getEntries()[lpref.findIndexOfValue(sval)];
            summary = ( (sval==null)||(sval.equals(getText(R.string.polling_intervals_minutes_never).toString()) )
                    ? "no automatic updates" : "Interval set to "+text );
        }
        if(summary!=null) {
            pref.setSummary(summary);
            System.out.println("setting "+pref.getKey()+"'s summary to "+summary);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        setSummary(pref, newValue);
        if(pref.getKey().equals( getString(R.string.k_pref_interval_min) ))
            setNotiActivityAfterInterval(newValue);
        return true;
    }

    private void setNotiActivityAfterInterval(Object newValue) {
        final String cval = (String) newValue;
        final Preference noti_pref = findPreference(getString(R.string.k_pref_notifications));
        if( cval.equals(getText(R.string.polling_intervals_minutes_never).toString()) )
            noti_pref.setEnabled(false);
        else
            noti_pref.setEnabled(true);
    }
}
