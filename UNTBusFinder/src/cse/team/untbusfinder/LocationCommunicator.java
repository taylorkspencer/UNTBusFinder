package cse.team.untbusfinder;

import java.util.ArrayList;
import java.lang.Runnable;

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
import android.os.Handler;

public class LocationCommunicator extends Service implements Runnable
{
	// Variables declared here so that they can be accessed in the LocationListener
	boolean isNetworkEnabled = false;
	boolean isPolling = false;
	
	Handler locationQueryTimer;
	
	// An ArrayList of LocationListeners is declared here so that other classes
	// can listen for location updates
	ArrayList<LocationListener> listeners = new ArrayList<LocationListener>();
	
	// Constants
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BTWN_UPDATES = 1000*10; // In milliseconds (10s interval)
	
	public LocationCommunicator(String serverURL)
	{
		//TODO: Initialize the locationQueryTimer
		locationQueryTimer = new Handler(this);
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
	
	@Override public void run()
	{
		// Notify any listening threads about the location change
		for (int lIndex=0; listeners.size()>lIndex; lIndex++)
		{
			listeners.get(lIndex).onLocationChanged(location);
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