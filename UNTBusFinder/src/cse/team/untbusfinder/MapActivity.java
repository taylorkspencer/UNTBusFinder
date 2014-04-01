package cse.team.untbusfinder;

import java.util.List;
import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.PathOverlay;

import android.app.Activity;
import android.app.ActionBar;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MapActivity extends Activity
{
	// Variables declared here so that they can be accessed in the LocationListener
	MapView mapView;
	GPSretrieve gps;
	PointOverlay myLocOverlay;
	List<StationaryPointOverlay> busStopOverlays;
	StationaryPointOverlay busStopOverlayStyle;
	List<PointOverlay> busLocOverlays;
	PointOverlay busLocOverlayStyle;
	List<PathOverlay> busPathOverlays;
	PathOverlay busPathOverlayStyle;
	
	// Display the activity and register the controls
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		// Register the GPSretrieve class object
		gps = new GPSretrieve(getApplicationContext());
		
		// Set up the OpenStreetMaps view
		mapView = (MapView)findViewById(R.id.generalMap);
		mapView.setBuiltInZoomControls(true);
		// Set the initial zoom level
		mapView.getController().setZoom(15);
		
		// Set up the overlays
		busPathOverlayStyle = new PathOverlay(Color.TRANSPARENT, mapView.getContext());
		busStopOverlayStyle = new StationaryPointOverlay(mapView.getContext());
		
		// Set up the lists for the overlays
		busPathOverlays = new ArrayList<PathOverlay>();
		busStopOverlays = new ArrayList<StationaryPointOverlay>();
		busLocOverlays = new ArrayList<PointOverlay>();
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
		
		// Listen for a location update and if one is received, change the map
		// control to that location
		gps.requestLocationUpdates(new LocationListener()
		{
			@Override public void onLocationChanged(Location location)
			{
				// Make sure the new location isn't null
				if (location!=null)
				{
					// Center the map on the changed location
					mapView.getController().setCenter(new GeoPoint(location));
					
					//TODO: If there was a previous location, declare a MotionPointOverlay
					// and set its point in the direction of the change
					if ((gps.getLastLocation()!=null)&&(gps.hasBearing()))
					{
						myLocOverlay = new MotionPointOverlay(mapView.getContext());
						
						//TODO: Determine the direction of the change (this needs to be further tested)
						myLocOverlay.setBearing(location.getBearing());
					}
					else
					{
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
		
		// Adjust the action bar for this activity
		setupActionBar();
	}
	
	// Start and/or resume polling for location
	@Override protected void onResume()
	{
		// Begin polling for location
		gps.startPolling();
		
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
	
	// If the location is not being sent to the server, stop polling for location
	// when UNT Bus Finder closes
	@Override protected void onPause()
	{
		// Stop polling for location
		gps.stopPolling();
		super.onPause();
	}
}