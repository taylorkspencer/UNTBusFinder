package cse.team.untbusfinder;

import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;

public class GPSretrieve extends Service implements LocationListener
{
	// Variables declared here so that they can be accessed in the LocationListener
	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;
	boolean isPolling = false;
	boolean pollingStateLock = false;
	
	protected LocationManager locMgr;
	Location lastLocation;
	
	// An ArrayList of LocationListeners is declared here so that other classes
	// can listen for location updates
	ArrayList<LocationListener> listeners = new ArrayList<LocationListener>();
	
	// Static instance of this Service (so Activities can access it)
	static GPSretrieve sInstance;
	
	// Constants
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MAX_TIME_BTWN_UPDATES = 1000*100; // In milliseconds (100s currently)
	private static final long MIN_TIME_BTWN_UPDATES = 1000*10; // In milliseconds (10s currently)
	private static final int MAX_TIME_TO_WAIT = 1000*10; // In milliseconds (10 seconds)

	@Override public void onCreate()
	{
		super.onCreate();
		
		// Store the LocationManager from the system
		locMgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		// Store a static instance of this Service in sInstance
		sInstance = this;
	}
	
	// Return a static instance of this Service for Activities
	static public GPSretrieve getInstance()
	{
		return sInstance;
	}
	
	// Enables the polling lock (used by the location sending methods
	// to prevent GPS polling from going off)
	public void lockPollingState()
	{
		pollingStateLock = true;
	}
	
	// Disables the polling lock (used by the location sending methods
	// to prevent GPS polling from going off)
	public void unlockPollingState()
	{
		pollingStateLock = false;
	}
	
	// Start polling for location updates
	public void startPolling()
	{
		// If the polling state is locked, do nothing
		if (!isPollingStateLocked())
		{
			// Only enable polling if it is not already enabled
			if (!isPolling())
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
		}
	}
	
	// Stop polling for location updates
	public void stopPolling()
	{
		// If the polling state is locked, do nothing
		if (!isPollingStateLocked())
		{
			// Only disable polling if it is already enabled
			if (isPolling())
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
		}
	}
	
	// Returns the state of the polling lock (used by the location
	// sending methods to prevent GPS polling from going off)
	public boolean isPollingStateLocked()
	{
		return pollingStateLock;
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
	@SuppressLint("NewApi")
	public Location getLocation()
	{
		// If lastLocation is null, query the passive location provider for its
		// last known location to provide a location until we get one from one
		// of the providers
		if (lastLocation==null)
		{
			return locMgr.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		}
		else
		{
			// Determine if the previous location has expired
			long timeSinceLastLocation;
			
			if (Build.VERSION.SDK_INT>=17)
			{
				timeSinceLastLocation = SystemClock.elapsedRealtimeNanos()-lastLocation.getElapsedRealtimeNanos();
			}
			else
			{
				Date now = new Date();
				timeSinceLastLocation = now.getTime()-lastLocation.getTime();
			}
			
			// If so, return a location from the passive location provider
			if (timeSinceLastLocation>=MAX_TIME_BTWN_UPDATES)
			{
				return locMgr.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
			}
			else
			{
				return lastLocation;
			}
		}
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
		// Determine if this location has a bearing
		if (hasBearing())
		{
			return getLocation().getBearing();
		}
		// If this location does not have a bearing, return 0.0
		else
		{
			return 0.0;
		}
	}
	
	// Returns whether the most recent location has a bearing
	public boolean hasBearing()
	{
		// Determine if there is a location
		if (getLocation()!=null)
		{
			return getLocation().hasBearing();
		}
		// If there is not a location, return false
		else
		{
			return false;
		}
	}
	
	public boolean canGetLocation()
	{
		return this.canGetLocation;
	}
	
	@SuppressLint("NewApi")
	@Override public void onLocationChanged(Location location)
	{
		// If this location is null, just ignore it
		if (location!=null)
		{
			// If there is a previous location, check if the previous location
			// is more accurate
			if (lastLocation!=null)
			{
				// Determine if the previous location has expired
				long timeSinceLastLocation;
				
				if (Build.VERSION.SDK_INT>=17)
				{
					timeSinceLastLocation = location.getElapsedRealtimeNanos()-getLocation().getElapsedRealtimeNanos();
				}
				else
				{
					timeSinceLastLocation = location.getTime()-getLocation().getTime();
				}
				
				if (timeSinceLastLocation>=MAX_TIME_BTWN_UPDATES)
				{
					// If so, accept this location
				}
				// If not, determine which provider this is coming from
				else if (location.getProvider()==LocationManager.GPS_PROVIDER)
				{
					// If this is coming from the GPS provider, always accept, as
					// the GPS provider is the most accurate provider we have
				}
				// If this is not coming from the GPS provider, see if a more
				// accurate location is available
				else
				{
					// Determine if the GPS provider is still enabled
					if (isGPSEnabled)
					{
						// If so, discard this location
						return;
					}
					else
					{
						// If not, determine if the previous location came from a
						// network provider
						if (lastLocation.getProvider()==LocationManager.NETWORK_PROVIDER)
						{
							// If so, compare the accuracy of this location to the
							// previous location
							if (location.getAccuracy()<lastLocation.getAccuracy())
							{
								// If the last location was more accurate, discard
								// this location
								return;
							}
						}
					}
				}
			}
			
			// If there is a previous location and it is different, calculate
			// a bearing for this location based on the direction of change
			if ((lastLocation!=null)&&(location.distanceTo(lastLocation)>0))
			{
				location.setBearing((float)lastLocation.bearingTo(location));
			}
			
			// Notify any listening threads about the location change
			for (int lIndex=0; listeners.size()>lIndex; lIndex++)
			{
				listeners.get(lIndex).onLocationChanged(location);
			}
			
			// Set the last location equal to this location
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