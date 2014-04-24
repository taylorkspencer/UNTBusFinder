package cse.team.untbusfinder;

//TODO: Clean out imports
import org.osmdroid.util.GeoPoint;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.app.Fragment;

public class RouteActivity extends Activity
{
	GPSretrieve gps;
	LocationCommunicator link;
	LocationSendingTimer locUpdTimer;
	String currentRoute;
	String defaultRoute;
	
	boolean sendingLocation;
	
	// Constants
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MAX_TIME_BTWN_UPDATES = 1000*100; // In milliseconds (100s currently)
	private static final long MIN_TIME_BTWN_UPDATES = 1000*10; // In milliseconds (10s interval)
	private static final int MAX_TIME_TO_WAIT = 1000*10; // In milliseconds (10 seconds)
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route);
		
		// Set the default route
		defaultRoute = getString(R.string.discParkRoute);
		
		// Retrieve the GPSretrieve and LocationCommunicator services
		gps = GPSretrieve.getInstance();
		link = LocationCommunicator.getInstance();
		
		// Set the URL of the server to be queried by the LocationCommunicator
		link.setServerURL("http://untbusfinder.no-ip.org/");
		
		// Adjust the action bar for this activity
		setupActionBar();
		
		// Set the current route for this Activity
		setRoute(defaultRoute);
		
		// Set sendingLocation to false
		//TODO: Determine the current state of location sending
		sendingLocation = false;
	}
	
	protected String getRoute()
	{
		return currentRoute;
	}
	
	protected void setRoute(String route)
	{
		// Set the Activity title to the route name
		setTitle(route);
		
		currentRoute = route;
		
		// Check the menu item with the route name
		invalidateOptionsMenu();
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
	
	// If the location is not being sent to the server, stop polling for
	// location when UNT Bus Finder closes
	// (here as a placeholder for when we start polling for location in
	// this activity)
	@Override protected void onPause()
	{
		super.onPause();
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present
		getMenuInflater().inflate(R.menu.route, menu);
		
		// If the user is not sending location, set the toggle text to Board
		if (!isSendingLocation())
		{
			menu.findItem(R.id.boardBusToggle).setTitle(R.string.actionBoardBus);
		}
		// If the user is sending location, set the toggle text to Leave
		else
		{
			menu.findItem(R.id.boardBusToggle).setTitle(R.string.actionLeaveBus);
		}
		
		// Check the menu item for the route that is currently set
		if (getRoute().equals(getString(R.string.discParkRoute)))
		{
			menu.findItem(R.id.discParkRoute).setChecked(true);
		}
		return true;
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		// Determine if the user chose the Board Bus toggle item
		if (item.getItemId()==R.id.boardBusToggle)
		{
			// If the user is not sending location, start sending location
			if (!isSendingLocation())
			{
				// Turn on location sending
				startSendingLocation();
				
				// If location sending was successfully turned on, change
				// the toggle text to Leave
				item.setTitle(R.string.actionLeaveBus);
			}
			// If the user is sending location, stop sending location
			else
			{
				// Turn off location sending
				stopSendingLocation();
				
				// If location sending was successfully turned off, change
				// the toggle text to Board
				item.setTitle(R.string.actionBoardBus);
			}
			return true;
		}
		// Determine if the user chose the Discovery Park menu item
		else if (item.getItemId()==R.id.discParkRoute)
		{
			// Set the route to Discovery Park
			setRoute(getString(R.string.discParkRoute));
			return true;
		}
		// For other items, return false
		else
		{
			return false;
		}
	}
	
	// Returns whether the RouteActivity is sending location
	public boolean isSendingLocation()
	{
		return sendingLocation;
	}
	
	// Start sending locations to the server
	public void startSendingLocation()
	{
		// Query the server for location updates and if one is received,
		// send the new location to any listeners
		locUpdTimer = new LocationSendingTimer();
		locUpdTimer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MIN_TIME_BTWN_UPDATES);
		
		// If GPS polling is not enabled, enable it
		if (!gps.isPolling())
		{
			gps.startPolling();
		}
		
		// To prevent other Fragments and Activities from stopping GPS polling,
		// place a polling lock on GPSretrieve
		gps.lockPollingState();
		
		// If all this is successful, set sendingLocation to true to 
		// indicate that sending location has begun
		sendingLocation = true;
	}
	
	// Stop sending locations to the server
	public void stopSendingLocation()
	{
		// Stop sending the location to the server
		locUpdTimer.cancel(false);
		
		// Release the lock on GPSretrieve placed when sending location
		gps.unlockPollingState();
		
		// Set sendingLocation to false to indicate that sending location has stopped
		sendingLocation = false;
	}
	
	// Display an error message toast and change the toggle
	// text to Board
	protected void onLocationSendingError()
	{
		Toast.makeText(this, "Could not send the location to the server.", Toast.LENGTH_SHORT).show();
		invalidateOptionsMenu();
	}
	
	class LocationSendingTimer extends AsyncTask<Long, Void, Boolean> implements Runnable
	{
		Handler taskTimer;
		long taskInterval;
		
		@Override public void run()
		{
			new LocationSendingTimer().execute(taskInterval);
		}
		
		@Override protected void onPreExecute()
		{
			// Initialize the Handler
			taskTimer = new Handler();
		}
		
		// Send the user's location to the server
		@Override protected Boolean doInBackground(Long... interval)
		{
			// Set the time interval for the Handler
			taskInterval = interval[0];
			
			// If sending the location was successful, return true to indicate success
			if (link.sendLocation(new GeoPoint(gps.getLocation())))
			{
				return true;
			}
			// If not, return false to indicate failure
			else
			{
				return false;
			}
		}
		
		@Override protected void onPostExecute(Boolean success)
		{
			// If sending the location was successful, renew the locationSenderTimer
			if (success)
			{
				taskTimer.postDelayed(this, taskInterval);
			}
			// If sending location failed, stop retrieving locations, display an error
			// message toast, and change the toggle text to Send My Location
			else
			{
				stopSendingLocation();
				onLocationSendingError();
			}
		}
	}
}