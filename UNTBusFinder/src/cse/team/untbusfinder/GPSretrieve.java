package cse.team.untbusfinder;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

public class GPSretrieve extends Service implements LocationListener
{
	// Variables declared here so that they can be accessed in the LocationListener
	private final Context mContext;
	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;
	boolean isPolling = false;
	
	protected LocationManager locMgr;
	Location lastLocation;
	
	// An ArrayList of LocationListeners is declared here so that other classes
	// can listen for location updates
	ArrayList<LocationListener> listeners = new ArrayList<LocationListener>();
	
	// Constants
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BTWN_UPDATES = 1000*60*1;
		
	public GPSretrieve(Context context)
	{
		// Store the context and the LocationManager from the system
		mContext = context;
		locMgr = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
	}
	
	// Start polling for location updates
	public void startPolling()
	{
		try
		{
			// Listen for location updates from the network provider and if one is
			// received, change the location variable to that location
			locMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
											MIN_TIME_BTWN_UPDATES,
											MIN_DISTANCE_CHANGE_FOR_UPDATES,
											this);
			// Listen for location updates from the GPS provider and if one is
			// received, change the location variable to that location
			locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
											MIN_TIME_BTWN_UPDATES,
											MIN_DISTANCE_CHANGE_FOR_UPDATES,
											this);
			
			// If all this is successful, set isPolling to true to indicate that
			// polling for location has begun
			isPolling = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// Stop polling for location updates
	public void stopPolling()
	{
		try
		{
			// Stop listening for location updates
			locMgr.removeUpdates(this);
			
			// If all this is successful, set isPolling to false to indicate that
			// polling for location has stopped
			isPolling = false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// Returns whether GPSretrieve is actively polling for location
	public boolean isPolling()
	{
		return isPolling;
	}
	
	// Return the last location, or null if there isn't any
	// Used for comparing locations
	public Location getLastLocation()
	{
		return lastLocation;
	}
	
	// Return the most recent location available
	public Location getLocation()
	{
		// If lastLocation is null, query the passive location providers for
		// its last known location to provide a location until we get one
		// from one of the providers
		if (lastLocation==null)
		{
			locMgr.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		}
		return lastLocation;
	}
	
	// Return the latitude from the most recent location
	public double getLatitude()
	{
		return getLocation().getLatitude();
	}
	
	// Return the longitude from the most recent location
	public double getLongitude()
	{
		return getLocation().getLongitude();
	}
	
	// Return the bearing from the most recent location
	public double getBearing()
	{
		return getLocation().getBearing();
	}
	
	// Returns whether the most recent location has a bearing
	public boolean hasBearing()
	{
		return getLocation().hasBearing();
	}
	
	public boolean canGetLocation()
	{
		return this.canGetLocation;
	}
	
	//TODO: This should be moved to the main activity - this class shouldn't have
	// any GUI aspects
	//TODO: I think I know where you're going with this - let's use the methods
	// startPolling() and stopPolling() so we turn off all the providers
	public void showSettingsAlert()
	{
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		
		alertDialog.setTitle("GPS in settings");
		
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
		
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});
		
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.cancel();
			}
		});
		
		alertDialog.show();
	}
	
	@Override public void onLocationChanged(Location location)
	{
		// Notify any listening threads about the location change
		for (int lIndex=0; listeners.size()>lIndex; lIndex++)
		{
			listeners.get(lIndex).onLocationChanged(location);
		}
		
		if (location!=null)
		{
			lastLocation = location;
		}
	}
	
	@Override public void onProviderDisabled(String provider)
	{
		// Notify any listening threads about the provider change
		for (int lIndex=0; listeners.size()>lIndex; lIndex++)
		{
			listeners.get(lIndex).onProviderDisabled(provider);
		}
		
		if (provider.equals(LocationManager.GPS_PROVIDER))
		{
			// Indicate that the GPS provider has been disabled
			isGPSEnabled = false;
		}
		else if (provider.equals(LocationManager.NETWORK_PROVIDER))
		{
			// Indicate that the network provider has been disabled
			isNetworkEnabled = false;
		}
		
		// If neither the GPS nor the network is enabled, we won't be able to
		// get a location, so set canGetLocation to false
		if((!isGPSEnabled)&&(!isNetworkEnabled))
		{
			this.canGetLocation = false;
		}
		else
		{
			this.canGetLocation = true;
		}
	}
	
	@Override public void onProviderEnabled(String provider)
	{
		// Notify any listening threads about the provider change
		for (int lIndex=0; listeners.size()>lIndex; lIndex++)
		{
			listeners.get(lIndex).onProviderEnabled(provider);
		}
		
		if (provider.equals(LocationManager.GPS_PROVIDER))
		{
			// Indicate that the GPS provider has been enabled
			isGPSEnabled = true;
		}
		else if (provider.equals(LocationManager.NETWORK_PROVIDER))
		{
			// Indicate that the network provider has been enabled
			isNetworkEnabled = true;
		}
		
		// If neither the GPS nor the network is enabled, we won't be able to
		// get a location, so set canGetLocation to false
		if((!isGPSEnabled)&&(!isNetworkEnabled))
		{
			this.canGetLocation = false;
		}
		else
		{
			this.canGetLocation = true;
		}
	}
	
	@Override public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// Notify any listening threads about the status change
		for (int lIndex=0; listeners.size()>lIndex; lIndex++)
		{
			listeners.get(lIndex).onStatusChanged(provider, status, extras);
		}
		
		// Determine if the GPS provider's status has changed
		if (provider.equals(LocationManager.GPS_PROVIDER))
		{
			// Determine if the provider has become available
			if (status==LocationProvider.AVAILABLE)
			{
				// Indicate that the GPS provider has become available
				isGPSEnabled = true;
			}
			// Determine if the provider has become unavailable
			else if ((status==LocationProvider.TEMPORARILY_UNAVAILABLE)||
					(status==LocationProvider.OUT_OF_SERVICE))
			{
				// Indicate that the GPS provider has become unavailable
				isGPSEnabled = false;
			}
		}
		// Determine if the network provider's status has changed
		else if (provider.equals(LocationManager.NETWORK_PROVIDER))
		{
			// Determine if the provider has become available
			if (status==LocationProvider.AVAILABLE)
			{
				// Indicate that the network provider has become available
				isNetworkEnabled = true;
			}
			// Determine if the provider has become unavailable
			else if ((status==LocationProvider.TEMPORARILY_UNAVAILABLE)||
					(status==LocationProvider.OUT_OF_SERVICE))
			{
				// Indicate that the network provider has become unavailable
				isNetworkEnabled = false;
			}
		}
		
		// If neither the GPS nor the network is enabled, we won't be able to
		// get a location, so set canGetLocation to false
		if((!isGPSEnabled)&&(!isNetworkEnabled))
		{
			this.canGetLocation = false;
		}
		else
		{
			this.canGetLocation = true;
		}
	}
	
	@Override public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	// Add a LocationListener to the class so other classes can listen for
	// location changes
	public void requestLocationUpdates(LocationListener listener)
	{
		listeners.add(listener);
	}
	
	// Remove a LocationListener from the class
	public void removeUpdates(LocationListener listener)
	{
		listeners.remove(listener);
	}
}