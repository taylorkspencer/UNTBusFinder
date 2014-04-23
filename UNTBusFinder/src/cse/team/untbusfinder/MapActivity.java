package cse.team.untbusfinder;

//TODO: Clean out imports
import java.util.List;
import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;

import android.app.Activity;
import android.app.ActionBar;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MapActivity extends Activity
{
	// Display the activity and register the controls
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
	}
	
	// Set the initial locations of the MapView and its Overlays
	@Override protected void onStart()
	{
		super.onStart();
		
		// Adjust the action bar for this activity
		setupActionBar();
	}
	
	// Adjust the action bar for this activity
	private void setupActionBar()
	{
		// Determine if this activity is a child
		if (!isTaskRoot())
		{
			// If so, enable the up option
			getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
		}
		else
		{
			// If not, disable the up option
			getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		}
	}
	
	// Go to the previous activity, or if this is the first activity, hide
	@Override public void onBackPressed()
	{
		finish();
	}
	
	// If there is a previous activity, navigate back to it
	// Returns true if navigation occurred, otherwise return false
	@Override public boolean onNavigateUp()
	{
		// Determine if this activity is a child
		if (!isTaskRoot())
		{
			// If so, go to the previous activity and return true
			onBackPressed();
			return true;
		}
		else
		{
			// If not, do nothing and return false
			return false;
		}
	}
}