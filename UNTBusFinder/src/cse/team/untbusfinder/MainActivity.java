package cse.team.untbusfinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationManager;
import android.location.Criteria;
import android.location.LocationListener;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MainActivity extends Activity
{
	// Variables declared here so that they can be accessed in the LocationListener
	MapView mapView;
	String bestProvider;
	LocationManager locMgr;
	Criteria locCriteria;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//TODO: Gets the coordinates representing the user's location
	// Called when the user clicks the Get Coordinates button
	public void get_coordinates(View view)
	{
	    // Do something in response to button

	}
	
	//TODO: Show coordinates as a toast
	// Called when the user clicks the Bounce button
	public void bounce_coordinates(View view)
	{
		//Need to get the coordinates from their internal location in storage
		Context context = getApplicationContext();
		CharSequence temptext = "Coordinates will be shown here."; //Placeholder for coordinates
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, temptext, duration);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
		
	}
	
	// Take the user's coordinates and displays them on a map
	// Called when the user clicks the Map Coordinates button
	//TODO: Stop polling for location when UNT Bus Finder closes (currently must
	// dismiss app to stop location polling)
	public void map_coordinates(View view)
	{
	    // Create a OpenStreetMaps view (I am using OSM here because Google
		// requires an API key)
		// In order to build, you will need to download osmdroid-android-4.1.jar
		// and slf4j-android-1.5.8.jar to libs/ since those files are not on the
		// git repository
		
		// Set up the map control
		mapView = new MapView(this, 256);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		
		// Get the user's location and display it on the map control
		locMgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		// Determine the best LocationProvider for now
		locCriteria = new Criteria();
		locCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		bestProvider = locMgr.getBestProvider(locCriteria, true);
		// Set the initial zoom level
		mapView.getController().setZoom(15);
		
		// Get the last known location from the LocationProvider and set the
		// map control to that
		if (locMgr.getLastKnownLocation(bestProvider)!=null)
		{
			mapView.getController().setCenter(new GeoPoint(locMgr.getLastKnownLocation(bestProvider)));
		}
		
		
		// Listen for a location update and if one is received, change the map
		// control to that location
		locMgr.requestLocationUpdates(bestProvider, 1000, 1, new LocationListener()
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
				if (provider.equals(bestProvider))
				{
					// Get a new best LocationProvider
					locMgr.getBestProvider(locCriteria, true);
				}
			}
			
			@Override public void onProviderEnabled(String provider)
			{
				// Get a new best LocationProvider
				locMgr.getBestProvider(locCriteria, true);
			}
			
			@Override public void onStatusChanged(String provider, int status, Bundle extras)
			{
				// Get a new best LocationProvider
				locMgr.getBestProvider(locCriteria, true);
			}
		});
		
		// Display the map control (unfortunately, this knocks off all the
		// other controls!)
		setContentView(mapView);
	}
}