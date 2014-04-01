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

public class LocationCommunicator extends Service implements LocationListener
{
	// Variables declared here so that they can be accessed in the LocationListener
	boolean isNetworkEnabled = false;
	boolean isPolling = false;
	
	protected LocationManager locMgr;
	Location lastLocation;
	
	// An ArrayList of LocationListeners is declared here so that other classes
	// can listen for location updates
	ArrayList<LocationListener> listeners = new ArrayList<LocationListener>();
	
	// Constants
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BTWN_UPDATES = 1000*60*1;
		
	public LocationCommunicator()
	{
		
	}
	
	//TODO: Start polling the server for location updates
	public void startPolling()
	{
		try
		{
			
			
			// If all this is successful, set isPolling to true to indicate that
			// polling for location has begun
			isPolling = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//TODO: Stop polling the server for location updates
	public void stopPolling()
	{
		try
		{
			
			
			// If all this is successful, set isPolling to false to indicate that
			// polling for location has stopped
			isPolling = false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// Returns whether LocationCommunicator is actively polling the server for locations
	public boolean isPolling()
	{
		return isPolling;
	}
	
	@Override public void onLocationChanged(Location location)
	{
		// Notify any listening threads about the location change
		for (int lIndex=0; listeners.size()>lIndex; lIndex++)
		{
			listeners.get(lIndex).onLocationChanged(location);
		}
	}
	
	@Override public void onProviderDisabled(String provider)
	{
		// Notify any listening threads about the provider change
		for (int lIndex=0; listeners.size()>lIndex; lIndex++)
		{
			listeners.get(lIndex).onProviderDisabled(provider);
		}
	}
	
	@Override public void onProviderEnabled(String provider)
	{
		// Notify any listening threads about the provider change
		for (int lIndex=0; listeners.size()>lIndex; lIndex++)
		{
			listeners.get(lIndex).onProviderEnabled(provider);
		}
	}
	
	@Override public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// Notify any listening threads about the status change
		for (int lIndex=0; listeners.size()>lIndex; lIndex++)
		{
			listeners.get(lIndex).onStatusChanged(provider, status, extras);
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