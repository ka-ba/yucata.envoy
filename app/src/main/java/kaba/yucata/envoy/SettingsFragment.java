package kaba.yucata.envoy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import static kaba.yucata.envoy.PrefsHelper.PREF_VALUE_S_PW_ACCEPTED;
import static kaba.yucata.envoy.PrefsHelper.PREF_VALUE_S_PW_FAILED;
import static kaba.yucata.envoy.PrefsHelper.PREF_VALUE_S_PW_REJECTED;
import static kaba.yucata.envoy.PrefsHelper.PREF_VALUE_S_UN_ACCEPTED;
import static kaba.yucata.envoy.PrefsHelper.PREF_VALUE_S_UN_FAILED;
import static kaba.yucata.envoy.PrefsHelper.PREF_VALUE_S_UN_REJECTED;

/**
 * Created by kaba on 23/08/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    private EditTextPreference p_username;
    private EditTextPreference p_password;
    private ListPreference p_interval;
    private CheckBoxPreference p_noti;
    private SharedPreferences sharedPrefs;
    private boolean have_serv;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_gamecount);
        p_username = (EditTextPreference) findPreference(getString(R.string.k_pref_username));
        p_password = (EditTextPreference) findPreference(getString(R.string.k_pref_password));
        p_interval = (ListPreference) findPreference(getString(R.string.k_pref_interval_min));
        p_noti = (CheckBoxPreference) findPreference(getString(R.string.k_pref_notifications));
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        have_serv = PrefsHelper.haveService(sharedPrefs,true);
        // p_notification not needed here
        // pre-set summaries
        setSummary(p_username,p_username.getText());
        setSummary(p_password,p_password.getText());
        if(have_serv) {
            setSummary(p_interval, p_interval.getValue());
            setNotiActivityAfterInterval(p_interval.getValue());
        } else {
            p_interval.setEnabled(false);
            p_noti.setEnabled(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // register listeners for updating summaries
        p_username.setOnPreferenceChangeListener(this);
        p_password.setOnPreferenceChangeListener(this);
        if(have_serv)
            p_interval.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        p_username.setOnPreferenceChangeListener(null);
        p_password.setOnPreferenceChangeListener(null);
        if(have_serv)
            p_interval.setOnPreferenceChangeListener(null);
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
                    ? getString(R.string.username_init_txt) : sval );
            switch( PrefsHelper.getStateUsername(sharedPrefs,-1) ) {
                case PREF_VALUE_S_UN_ACCEPTED:
                    summary = summary+" " + getString(R.string.accepted);
                    break;
                case PREF_VALUE_S_UN_REJECTED:
                    summary = summary+" " + getString(R.string.rejected);
                    break;
                case PREF_VALUE_S_UN_FAILED:
                    summary = summary+" " + getString(R.string.login_failed);
                    break;
            }
        // password / secret
        } else if(pref.getKey().equals( getString(R.string.k_pref_password) )) {
            final String sval = (String) val;
            summary = ( (sval==null)||(sval.length()<1)
                    ? getString(R.string.password_nopw) : getString(R.string.password_set) );
            switch( PrefsHelper.getStatePassword(sharedPrefs,-1) ) {
                case PREF_VALUE_S_PW_ACCEPTED:
                    summary = summary+" " + getString(R.string.accepted);
                    break;
                case PREF_VALUE_S_PW_REJECTED:
                    summary = summary+" " + getString(R.string.rejected);
                    break;
                case PREF_VALUE_S_PW_FAILED:
                    summary = summary+" " + getString(R.string.login_failed);
                    break;
            }
        // polling interval
        } else if(pref.getKey().equals( getString(R.string.k_pref_interval_min) )) {
            try {
                final String sval = (String) val;
                final ListPreference lpref = (ListPreference) pref;
                final CharSequence text = lpref.getEntries()[lpref.findIndexOfValue(sval)];
                summary = ((sval == null) || (sval.equals(getText(R.string.polling_intervals_minutes_never).toString()))
                        ? getString(R.string.interval_no) : getString(R.string.interval_set, text));
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                summary="";
            }
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
