package cse.team.untbusfinder;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity
{
	// Variables declared here so that they can be accessed in the LocationListener
	MapView mapView;
	String bestProvider;
	LocationManager locMgr;
	Criteria locCriteria;
	GPSretrieve gps;
	Button get_coordinates;
	double latitude;
	double longitude;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// The get_coordinates button does not exist anymore
		//get_coordinates = (Button)findViewById(R.id.get_coordinates);
		
		// Show location button click event
		//get_coordinates.setOnClickListener(new View.OnClickListener()
		//{
			//@Override public void onClick(View view)
			//{
				//get_coordinates(view);
			//}
		//});
		
		// Register the GPSretrieve class object
		gps = new GPSretrieve(getApplicationContext());
		
		// Set up the OpenStreetMaps view
		mapView = new MapView(this, 256);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		// Set the initial zoom level
		mapView.getController().setZoom(15);
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// When the user clicks on the Discovery Park button, take them to the view that shows
	// the map and route info
	public void routes(View view)
	{
		Intent intent = new Intent(this, RouteActivity.class);
		startActivity(intent);
	}
	
	// Take the user's coordinates and displays them on a map
	// Called when the user clicks the Map button
	//TODO: Stop polling for location when UNT Bus Finder closes (currently must
	// dismiss app to stop location polling)
	public void general_map(View view)
	{
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
				if (location!=null)
				{
					mapView.getController().setCenter(new GeoPoint(location));
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
		
		// Display the map control (unfortunately, this knocks off all the
		// other controls!)
		setContentView(mapView);
		
		// Begin polling for location
		gps.startPolling();
	}
}