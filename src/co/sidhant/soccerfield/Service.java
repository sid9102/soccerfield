package co.sidhant.soccerfield;

import rajawali.wallpaper.Wallpaper;
import android.content.Context;
import android.content.res.Configuration;
import android.view.Surface;
import android.view.WindowManager;

public class Service extends Wallpaper {
	private Renderer mRenderer;

	public Engine onCreateEngine() {
		mRenderer = new Renderer(this);
		
		if(getDeviceDefaultOrientation() == Configuration.ORIENTATION_LANDSCAPE)
		{
			mRenderer.defaultLandscape = true;
		}
		else
			mRenderer.defaultLandscape = false;
		
		return new WallpaperEngine(this.getSharedPreferences(SHARED_PREFS_NAME,
				Context.MODE_PRIVATE), getBaseContext(), mRenderer, false);
	}
	
	public int getDeviceDefaultOrientation() {

	    WindowManager windowManager =  (WindowManager) getSystemService(WINDOW_SERVICE);

	    Configuration config = getResources().getConfiguration();

	    int rotation = windowManager.getDefaultDisplay().getRotation();

	    if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
	            config.orientation == Configuration.ORIENTATION_LANDSCAPE)
	        || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&    
	            config.orientation == Configuration.ORIENTATION_PORTRAIT)) 
	      return Configuration.ORIENTATION_LANDSCAPE;
	    else 
	      return Configuration.ORIENTATION_PORTRAIT;
	}
}
