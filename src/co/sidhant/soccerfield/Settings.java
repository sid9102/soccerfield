package co.sidhant.soccerfield;

import rajawali.wallpaper.Wallpaper;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import co.sidhant.soccerfield.R;

// Deprecated PreferenceActivity methods are used for API Level 10 (and lower) compatibility
// https://developer.android.com/guide/topics/ui/settings.html#Overview
@SuppressWarnings("deprecation")
public class Settings extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private EditTextPreference mEditTextPreference;
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getPreferenceManager().setSharedPreferencesName(
				Wallpaper.SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.settings);
		mEditTextPreference = (EditTextPreference) getPreferenceScreen().findPreference("pref_maxScore");
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	protected void onResume() {
		super.onResume();
		mEditTextPreference.setSummary("Reset the game when a score reaches: " + mEditTextPreference.getText());
	}

	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("pref_maxScore"))
		{
			int maxScore = Integer.parseInt(mEditTextPreference.getText());
			if (maxScore > 99 || maxScore < 1)
			{
				mEditTextPreference.setText("99");
			}
			mEditTextPreference.setSummary("Reset the game when a score reaches: " + mEditTextPreference.getText());
		}
	}
}