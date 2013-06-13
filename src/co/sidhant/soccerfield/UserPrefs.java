package co.sidhant.soccerfield;

import rajawali.wallpaper.Wallpaper;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserPrefs {

	private static final String scoring = "pref_scoring";

	private static UserPrefs mUserPrefs;

	private SharedPreferences mSharedPrefs;

	private UserPrefs(final Context context) {
		mSharedPrefs = context.getSharedPreferences(Wallpaper.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		init();
	}

	public static final UserPrefs getInstance(final Context context) {
		if (mUserPrefs == null)
			mUserPrefs = new UserPrefs(context);

		return mUserPrefs;
	}

	public boolean getScoring() {
		return mSharedPrefs.getBoolean(scoring, false);
	}

	public void setScoring(boolean disembody) {
		mSharedPrefs.edit().putBoolean(scoring, disembody)
				.commit();
	}

	/**
	 * Good time to verify any data. In this instance I just want to see the
	 * default value for the box color. This is not really necessary I just
	 * wanted to have an example.
	 */
	private final void init() {
		final Editor editor = mSharedPrefs.edit();

		// This could easily be done in the preferences XML
		if (!mSharedPrefs.contains(scoring)) {
			editor.putBoolean(scoring, false);
		}

		editor.commit();
	}

}