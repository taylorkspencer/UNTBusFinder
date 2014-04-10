package cse.team.untbusfinder;

import android.app.Activity;
import android.app.ActionBar;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity
{
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
		//TODO: Start the GPSretrieve and LocationCommunicator services
		Intent gpsServInt = new Intent(this, GPSretrieve.class);
		Intent locComInt = new Intent(this, LocationCommunicator.class);
		startService(gpsServInt);
		startService(locComInt);
		
		// Adjust the action bar for this activity
		setupActionBar();
	}
	
	// Stub - where we will need to initialize any instances of GPSretrieve
	// or LocationCommunicator in this Activity (should we need to)
	@Override protected void onStart()
	{
		super.onStart();
	}
	
	// Start and/or resume polling for location
	// (here as a placeholder for when we start polling for location in this
	// activity)
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// When the user clicks on the Discovery Park button, take them to the view that shows
	// the map and route info
	public void showRoutes(View view)
	{
		Intent showRoutes = new Intent(this, RouteActivity.class);
		startActivity(showRoutes);
	}
	
	// Take the user's coordinates and displays them on a map
	// Called when the user clicks the Map button
	public void showGeneralMap(View view)
	{
		Intent showGeneralMap = new Intent(this, MapActivity.class);
		startActivity(showGeneralMap);
	}
	
	// If the location is not being sent to the server, stop polling for location
	// when UNT Bus Finder closes
	// (here as a placeholder for when we start polling for location in this
	// activity)
	@Override protected void onPause()
	{
		super.onPause();
	}
}