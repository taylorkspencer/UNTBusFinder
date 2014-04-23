package cse.team.untbusfinder;

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
	
	// Start and/or resume polling for location
	@Override protected void onResume()
	{
		super.onResume();
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
	
	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present
		getMenuInflater().inflate(R.menu.map, menu);
		
		// If the user is not sending location, set the toggle text to
		// Send My Location
		if (!((MapFragment)getFragmentManager().findFragmentById(R.id.mapActivityFragment)).isSendingLocation())
		{
			menu.findItem(R.id.toggle_send_location).setTitle(R.string.action_send_my_location);
		}
		// If the user is sending location, set the toggle text to Stop
		// Sending My Location
		else
		{
			menu.findItem(R.id.toggle_send_location).setTitle(R.string.action_stop_sending_my_location);
		}
		return true;
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		//TODO: To be eliminated as these options are moved to the RouteActivity
		// Determine if the user chose the Send My Location toggle item
		if (item.getItemId()==R.id.toggle_send_location)
		{
			// If the user is not sending location, start sending location
			if (!((MapFragment)getFragmentManager().findFragmentById(R.id.mapActivityFragment)).isSendingLocation())
			{
				// Turn on location sending
				((MapFragment)getFragmentManager().findFragmentById(R.id.mapActivityFragment)).startSendingLocation();
				
				// If location sending was successfully turned on, change
				// the toggle text to Stop Sending My Location
				item.setTitle(R.string.action_stop_sending_my_location);
			}
			// If the user is sending location, stop sending location
			else
			{
				// Turn off location sending
				((MapFragment)getFragmentManager().findFragmentById(R.id.mapActivityFragment)).stopSendingLocation();
				
				// If location sending was successfully turned off, change
				// the toggle text to Send My Location
				item.setTitle(R.string.action_send_my_location);
			}
			return true;
		}
		// For other items, return false
		else
		{
			return false;
		}
	}
	
	// Display an error message toast and change the toggle
	// text to Send My Location
	protected void onLocationSendingError()
	{
		Toast.makeText(this, "Could not send the location to the server.", Toast.LENGTH_SHORT).show();
		invalidateOptionsMenu();
	}
}