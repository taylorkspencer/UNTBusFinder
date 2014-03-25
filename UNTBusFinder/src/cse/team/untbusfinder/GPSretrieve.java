package cse.team.untbusfinder;

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

//TODO: Reconcile this code with the location code in map_coordinates
//TODO: Find a way to notify the main thread
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
	
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BTWN_UPDATES = 1000*60*1;
		
	public GPSretrieve(Context context)
	{
		//TODO: Store the context and the LocationManager from the system
		mContext = context;
		locMgr = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
	}
	
	//TODO: Start polling for location updates
	public void startPolling()
	{
		try
		{
			//TODO: Listen for location updates from the network provider and if one is
			// received, change the location variable to that location
			locMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
											MIN_TIME_BTWN_UPDATES,
											MIN_DISTANCE_CHANGE_FOR_UPDATES,
											this);
			//TODO: Listen for location updates from the GPS provider and if one is
			// received, change the location variable to that location
			locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
											MIN_TIME_BTWN_UPDATES,
											MIN_DISTANCE_CHANGE_FOR_UPDATES,
											this);
			
			//TODO: If all this is successful, set isPolling to true to indicate that
			// polling for location has begun
			isPolling = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//TODO: Stop polling for location updates
	public void stopPolling()
	{
		try
		{
			//TODO: Stop listening for location updates
			locMgr.removeUpdates(this);
			
			//TODO: If all this is successful, set isPolling to false to indicate that
			// polling for location has stopped
			isPolling = false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//TODO: Returns whether GPSretrieve is actively polling for location
	public boolean isPolling()
	{
		return isPolling;
	}
	
	//TODO: Return the most recent location available
	public Location getLocation()
	{
		//TODO: If lastLocation is null, query the passive location providers for
		// its last known location to provide a location until we get one
		// from one of the providers
		if (lastLocation==null)
		{
			locMgr.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		}
		return lastLocation;
	}
	
	public double getLatitude()
	{
		return getLocation().getLatitude();
	}
	
	public double getLongitude()
	{
		return getLocation().getLongitude();
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
		if (location!=null)
		{
			lastLocation = location;
		}
	}
	
	@Override public void onProviderDisabled(String provider)
	{
		if (provider.equals(LocationManager.GPS_PROVIDER))
		{
			//TODO: Indicate that the GPS provider has been disabled
			isGPSEnabled = false;
		}
		else if (provider.equals(LocationManager.NETWORK_PROVIDER))
		{
			//TODO: Indicate that the network provider has been disabled
			isNetworkEnabled = false;
		}
		
		//TODO: If neither the GPS nor the network is enabled, we won't be able to
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
		if (provider.equals(LocationManager.GPS_PROVIDER))
		{
			//TODO: Indicate that the GPS provider has been enabled
			isGPSEnabled = true;
		}
		else if (provider.equals(LocationManager.NETWORK_PROVIDER))
		{
			//TODO: Indicate that the network provider has been enabled
			isNetworkEnabled = true;
		}
		
		//TODO: If neither the GPS nor the network is enabled, we won't be able to
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
		//TODO: Determine if the GPS provider's status has changed
		if (provider.equals(LocationManager.GPS_PROVIDER))
		{
			//TODO: Determine if the provider has become available
			if (status==LocationProvider.AVAILABLE)
			{
				//TODO: Indicate that the GPS provider has become available
				isGPSEnabled = true;
			}
			//TODO: Determine if the provider has become unavailable
			else if ((status==LocationProvider.TEMPORARILY_UNAVAILABLE)||
					(status==LocationProvider.OUT_OF_SERVICE))
			{
				//TODO: Indicate that the GPS provider has become unavailable
				isGPSEnabled = false;
			}
		}
		//TODO: Determine if the network provider's status has changed
		else if (provider.equals(LocationManager.NETWORK_PROVIDER))
		{
			//TODO: Determine if the provider has become available
			if (status==LocationProvider.AVAILABLE)
			{
				//TODO: Indicate that the network provider has become available
				isNetworkEnabled = true;
			}
			//TODO: Determine if the provider has become unavailable
			else if ((status==LocationProvider.TEMPORARILY_UNAVAILABLE)||
					(status==LocationProvider.OUT_OF_SERVICE))
			{
				//TODO: Indicate that the network provider has become unavailable
				isNetworkEnabled = false;
			}
		}
		
		//TODO: If neither the GPS nor the network is enabled, we won't be able to
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
}