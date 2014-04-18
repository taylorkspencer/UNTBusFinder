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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MapActivity extends Activity
{
	// Variables declared here so that they can be accessed in the LocationListeners
	// and MapListener
	MapView mapView;
	GPSretrieve gps;
	LocationCommunicator link;
	PointOverlay myLocOverlay;
	List<StationaryPointOverlay> busStopOverlays;
	StationaryPointOverlay busStopOverlayStyle;
	List<PointOverlay> busLocOverlays;
	PointOverlay busLocOverlayStyle;
	List<PathOverlay> busPathOverlays;
	PathOverlay busPathOverlayStyle;
	LocationSendingTimer locUpdTimer;
	
	boolean centerOnMyLocation, isSendingLocation = false;
	
	// Constants
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BTWN_UPDATES = 1000*10; // In milliseconds (10s interval)
	private static final int MAX_TIME_TO_WAIT = 1000*10; // In milliseconds (10 seconds)
	
	// Display the activity and register the controls
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		// Retrieve the GPSretrieve and LocationCommunicator services
		gps = GPSretrieve.getInstance();
		link = LocationCommunicator.getInstance();
		
		// Set up the OpenStreetMaps view
		mapView = (MapView)findViewById(R.id.generalMap);
		mapView.setBuiltInZoomControls(true);
		// Set the initial zoom level
		mapView.getController().setZoom(15);
		
		//TODO: Set the URL of the server to be queried by the LocationCommunicator
		link.setServerURL("http://untbusfinder.no-ip.org/");
		
		// Set up the overlays
		busPathOverlayStyle = new PathOverlay(Color.TRANSPARENT, mapView.getContext());
		busStopOverlayStyle = new StationaryPointOverlay(mapView.getContext());
		
		// Set up the lists for the overlays
		busPathOverlays = new ArrayList<PathOverlay>();
		busStopOverlays = new ArrayList<StationaryPointOverlay>();
		busLocOverlays = new ArrayList<PointOverlay>();
		
		// Initialize the LocationSendingTimer
		locUpdTimer = new LocationSendingTimer();
	}
	
	// Set the initial locations of the MapView and its Overlays
	@Override protected void onStart()
	{
		super.onStart();
		
		// Get the last known location from GPSretrieve and set the map
		// control to that
		if (gps.getLocation()!=null)
		{
			mapView.getController().setCenter(new GeoPoint(gps.getLocation()));
		}
		
		// Listen for a location update from GPSretrieve and if one is received,
		// change the map control to that location
		gps.requestLocationUpdates(new LocationListener()
		{
			@Override public void onLocationChanged(Location location)
			{
				// Make sure the new location isn't null
				if (location!=null)
				{
					// If the user has not moved the focus, center the map on the changed location
					if (centerOnMyLocation)
					{
						mapView.getController().setCenter(new GeoPoint(location));
						
						//TODO: Since changing the center of the map will set centerOnMyLocation to
						// false, 
						centerOnMyLocation = true;
					}
					
					//TODO: If there was a previous location, declare a MotionPointOverlay
					// and set its point in the direction of the change
					if ((gps.getLastLocation()!=null)&&(gps.hasBearing()))
					{
						// Declare the myLocOverlay as a MotionPointOverlay
						myLocOverlay = new MotionPointOverlay(mapView.getContext());
						
						//TODO: Determine the direction of the change (this needs to be further tested)
						myLocOverlay.setBearing(location.getBearing());
					}
					else
					{
						// Declare the myLocOverlay as a StationaryPointOverlay
						myLocOverlay = new StationaryPointOverlay(mapView.getContext());
					}
					// Set the shared default attributes of the point
					myLocOverlay = setMyLocOverlayAttributes(myLocOverlay);
					
					// Update the myLocOverlay to the changed location
					myLocOverlay.setLocation(new GeoPoint(location));
					
					// Remove the old overlays and replace them with the new ones
					reloadOverlays();
				}
			}
			
			@Override public void onProviderDisabled(String provider)
			{
				// Do nothing - we don't care about this change
			}
			
			@Override public void onProviderEnabled(String provider)
			{
				// Do nothing - we don't care about this change
			}
			
			@Override public void onStatusChanged(String provider, int status, Bundle bundle)
			{
				// Do nothing - we don't care about this change
			}
		});
		
		//TODO: Listen for a location update from LocationCommunicator and if one is
		// received, change the map control to that location
		link.requestLocationUpdates(new LocationListener()
		{
			@Override public void onLocationChanged(Location location)
			{
				// Make sure the new location isn't null
				if (location!=null)
				{
					PointOverlay busLocOverlay;
					//TODO: If there was a previous location, remove the previous
					// location from the array, declare a MotionPointOverlay,
					// and set its point in the direction of the change
					if (link.getLastLocation()!=null)
					{
						//TODO: Look through the busLocOverlays list for a PointOverlay
						// with the same location as the last location
						for (int point=0, pntFound=0; ((point<busLocOverlays.size())&&(pntFound==0)); point++)
						{
							//TODO: If the location of this point is equal to the last location, remove
							// it and exit the loop
							if (busLocOverlays.get(point).getLocation().equals(link.getLastLocation()))
							{
								busLocOverlays.remove(location);
								pntFound = 1;
							}
						}
						
						// Declare the busLocOverlay as a MotionPointOverlay
						busLocOverlay = new MotionPointOverlay(mapView.getContext());
						
						//TODO: Determine the direction of the change (this needs to be further tested)
						busLocOverlay.setBearing(location.getBearing());
					}
					else
					{
						// Declare the busLocOverlay as a StationaryPointOverlay
						busLocOverlay = new StationaryPointOverlay(mapView.getContext());
					}
					// Set the shared default attributes of the point
					busLocOverlay = setBusLocOverlayAttributes(busLocOverlay);
					
					// Set the location of the busLocOverlay to the changed location
					busLocOverlay.setLocation(new GeoPoint(location));
					
					// Add the point to the busLocOverlays array
					busLocOverlays.add(busLocOverlay);
					
					// Remove the old overlays and replace them with the new ones
					reloadOverlays();
				}
			}
			
			@Override public void onProviderDisabled(String provider)
			{
				// Do nothing - we don't care about this change
			}
			
			@Override public void onProviderEnabled(String provider)
			{
				// Do nothing - we don't care about this change
			}
			
			@Override public void onStatusChanged(String provider, int status, Bundle bundle)
			{
				// Do nothing - we don't care about this change
			}
		});
		
		// Adjust the action bar for this activity
		setupActionBar();
		
		// Display a path from Discovery Park to UNT (temporary implementation
		// until we start getting this from the server)
		PathOverlay discoveryParkPath = busPathOverlayStyle;
		discoveryParkPath.setColor(Color.parseColor("#009933"));
		discoveryParkPath.addPoint(new GeoPoint(33.253977, -97.151756));
		discoveryParkPath.addPoint(new GeoPoint(33.211635, -97.147468));
		busPathOverlays.add(discoveryParkPath);
		
		//TODO: Listen for events from the MapView to turn off centering of the map
		// on the user's location when the user changes the focus
		mapView.setMapListener(new MapListener()
		{
			@Override public boolean onScroll(ScrollEvent event)
			{
				// Turn off centering on the user's location
				centerOnMyLocation = false;
				return true;
			}
			
			@Override public boolean onZoom(ZoomEvent event)
			{
				// Do nothing - we don't care about this change
				return true;
			}
		});
		
		//TODO: Have the map center on the user's location until the user changes the focus
		centerOnMyLocation = true;
	}
	
	// Start and/or resume polling for location
	@Override protected void onResume()
	{
		//TODO: Determine if the location is being sent to the server
		if (!isSendingLocation)
		{
			// Begin polling for location from GPSretrieve
			gps.startPolling();
		}
		// Begin polling for location from LocationCommunicator
		link.startPolling();
		
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
	
	// Set the shared default attributes for the myLocOverlay point
	protected PointOverlay setMyLocOverlayAttributes(PointOverlay point)
	{
		// Set the appearance attributes of the point
		point.setAlpha(255);
		point.setColor(Color.BLUE);
		point.setRadius(10);
		return point;
	}
	
	// Set the shared default attributes for the busLocOverlay point
	protected PointOverlay setBusLocOverlayAttributes(PointOverlay point)
	{
		// Set the appearance attributes of the point
		point.setAlpha(255);
		point.setRadius(10);
		return point;
	}
	
	// Set the shared default attributes for the busStopOverlay point
	protected PointOverlay setBusStopOverlayAttributes(PointOverlay point)
	{
		// Set the appearance attributes of the point
		point.setAlpha(255);
		point.setRadius(10);
		return point;
	}
	
	// Remove the old overlays and replace them with the new ones
	protected void reloadOverlays()
	{
		mapView.getOverlays().clear();
		mapView.getOverlays().addAll(busPathOverlays);
		mapView.getOverlays().addAll(busStopOverlays);
		mapView.getOverlays().addAll(busLocOverlays);
		mapView.getOverlays().add(myLocOverlay);
	}
	
	//TODO: Start sending locations to the server
	public void startSendingLocation()
	{
		//TODO: Query the server for location updates and if one is received,
		// send the new location to any listeners
		locUpdTimer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MIN_TIME_BTWN_UPDATES);
		
		// If all this is successful, set isSendingLocation to true to 
		// indicate that sending location has begun
		isSendingLocation = true;
	}
	
	//TODO: Stop sending locations to the server
	public void stopSendingLocation()
	{
		//TODO: Stop sending the location to the server
		locUpdTimer.cancel(true);
		
		// Set isSendingLocation to false to indicate that sending location has stopped
		isSendingLocation = false;
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
		return true;
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		//TODO: Determine if the user chose Send My Location
		if (item.getItemId()==R.id.send_my_location)
		{
			//TODO: If the menu item is checked, start sending location
			if (!item.isChecked())
			{
				//TODO: Turn on location sending
				startSendingLocation();
				
				//TODO: If location sending was successfully turned on, check
				// Send My Location
				item.setChecked(true);
			}
			//TODO: If the menu item is not checked, stop sending location
			else
			{
				//TODO: Turn off location sending
				stopSendingLocation();
				
				//TODO: If location sending was successfully turned off, uncheck
				// Send My Location
				item.setChecked(false);
			}
		}
		return true;
	}
	
	// If the location is not being sent to the server, stop polling for location
	// when UNT Bus Finder closes
	@Override protected void onPause()
	{
		//TODO: Determine if the location is being sent to the server
		if (!isSendingLocation)
		{
			// Stop polling for location from GPSretrieve
			gps.stopPolling();
		}
		// Stop polling for location from LocationCommunicator
		link.stopPolling();
		super.onPause();
	}
	
	class LocationSendingTimer extends AsyncTask<Long, Void, Long>
	{
		//TODO: Send the user's location to the server
		@Override protected Long doInBackground(Long... interval)
		{
			//TODO: Wait for the timer interval to pass
			try
			{
				Thread.sleep(interval[0]);
			}
			catch (InterruptedException cancelled)
			{
				// This means location sending is being cancelled - do nothing
			}
			finally
			{
				//TODO: If sending the location was successful, renew the locationSenderTimer
				if (link.sendLocation(new GeoPoint(gps.getLocation())))
				{
					locUpdTimer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MIN_TIME_BTWN_UPDATES);
				}
				//TODO: If not, stop sending location to the server
				else
				{
					stopSendingLocation();
				}
			}
			return interval[0];
		}
	}
}