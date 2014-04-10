package cse.team.untbusfinder;

import java.util.ArrayList;
import java.io.IOException;
import java.lang.Runnable;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.os.Handler;

import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.json.JSONObject;
import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

public class LocationCommunicator extends Service implements Runnable
{
	// Variables declared here so that they can be accessed in the LocationListener
	boolean isNetworkEnabled = false;
	boolean isPolling = false;
	
	Handler locationQueryTimer;
	HttpParams communicatorParams;
	DefaultHttpClient locServerClient;
	CookieStore locServerCookieStore;
	HttpPost locServerHTTPpost;
	HttpGet locServerHTTPget;
	String serverURL;
	
	//TODO: To be converted to an array to store multiple locations
	GeoPoint location;
	
	// Static instance of this Service (so Activities can access it)
	static LocationCommunicator sInstance;
	
	// An ArrayList of LocationListeners is declared here so that other classes
	// can listen for location updates
	ArrayList<LocationListener> listeners = new ArrayList<LocationListener>();
	
	// Constants
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BTWN_UPDATES = 1000*10; // In milliseconds (10s interval)
	private static final int MAX_TIME_TO_WAIT = 1000*10; // In milliseconds (10 seconds)
	
	@Override public void onCreate()
	{
		super.onCreate();
		
		//TODO: Initialize the locationQueryTimer
		locationQueryTimer = new Handler();
		
		// Store a static instance of this Service in sInstance
		sInstance = this;
		
		//TODO: Set the parameters for the LocationCommunicator HTTP client
		communicatorParams = new BasicHttpParams();
		try
		{
			HttpProtocolParams.setUserAgent(communicatorParams, "UNTBusFinder/"+getPackageManager().getPackageInfo(getPackageName(), 0).versionName+" (Android)");
		}
		catch (PackageManager.NameNotFoundException notInstalled)
		{
			// Do nothing
		}
		HttpConnectionParams.setConnectionTimeout(communicatorParams, MAX_TIME_TO_WAIT);
		HttpConnectionParams.setSoTimeout(communicatorParams, MAX_TIME_TO_WAIT);
		
		//TODO: Create the HTTP client and its cookie store
		locServerClient = new DefaultHttpClient(communicatorParams);
		locServerCookieStore = new BasicCookieStore();
		locServerClient.setCookieStore(locServerCookieStore);
	}
	
	// Return a static instance of this Service for Activities
	static public LocationCommunicator getInstance()
	{
		return sInstance;
	}
	
	// Sets the URL for the server to be queried by the LocationCommunicator
	public void setServerURL(String newServerURL)
	{
		serverURL = newServerURL;
	}
	
	// Returns the URL for the server to be queried by the LocationCommunicator
	public String getServerURL()
	{
		return serverURL;
	}
	
	//TODO: Start polling the server for location updates
	public void startPolling()
	{
		//TODO: Check to make sure the server URL is not null or empty
		if ((serverURL!=null)&&(serverURL.length()>0))
		{
			//TODO: Query the server for location updates and if one is received,
			// send the new location to any listeners
			if (locationQueryTimer.postDelayed(this, MIN_TIME_BTWN_UPDATES))
			{
				// If all this is successful, set isPolling to true to indicate that
				// polling for location has begun
				isPolling = true;
			}
		}
	}
	
	//TODO: Stop polling the server for location updates
	public void stopPolling()
	{
		//TODO: Stop querying the server for location updates
		locationQueryTimer.removeCallbacks(this);
		
		// Set isPolling to false to indicate that polling for location has stopped
		isPolling = false;
	}
	
	// Returns whether LocationCommunicator is actively polling the server for locations
	public boolean isPolling()
	{
		return isPolling;
	}
	
	@Override public void run()
	{
		//TODO: Attempt to connect to the server
		try
		{
			locServerHTTPpost = new HttpPost(serverURL);
		}
		//TODO: If the URL is invalid, stop polling and exit the function
		catch (IllegalArgumentException invalidURL)
		{
			stopPolling();
			return;
		}
		//TODO: Query the server for location updates
		
		
		//TODO: Send the query to the server
		HttpResponse locServerResponse = null;
		try
		{
			locServerResponse = locServerClient.execute(locServerHTTPpost);
		}
		//TODO: If the server is not a HTTP server, stop polling and exit the function
		catch (ClientProtocolException notHTTPserver)
		{
			stopPolling();
			return;
		}
		//TODO: If the connection could not be established, skip the next lines of code
		catch (IOException serverConnectionError)
		{
			// Do nothing
		}
		finally
		{
			//TODO: Parse the JSON returned by the server
			JSONObject responseJSON;
			try
			{
				responseJSON = new JSONObject(EntityUtils.toString(locServerResponse.getEntity()));
			}
			//TODO: If the JSON is invalid, stop polling and exit the function
			catch (JSONException invalidJSON)
			{
				stopPolling();
				return;
			}
			//TODO: If the JSON is invalid, stop polling and exit the function
			catch (ParseException JSONparseException)
			{
				stopPolling();
				return;
			}
			//TODO: If the JSON is invalid, stop polling and exit the function
			catch (IOException JSONioException)
			{
				stopPolling();
				return;
			}
			//TODO: Allow for retrieval of multiple locations
			//TODO: Convert the JSON into a GeoPoint location
			GeoPoint newLocation;
			try
			{
				newLocation = new GeoPoint(responseJSON.getDouble("lat"), responseJSON.getDouble("long"));
			}
			//TODO: If there is no location in the JSON, stop polling and exit the function
			catch (JSONException noLocationInJSON)
			{
				stopPolling();
				return;
			}
			//TODO: If a location is changed by MIN_DISTANCE_CHANGE_FOR_UPDATES or more
			if (newLocation.distanceTo(location)>=MIN_DISTANCE_CHANGE_FOR_UPDATES)
			{
				//TODO: Convert the GeoPoint to a Location
				Location toSend = new Location("LocationCommunicator");
				toSend.setLatitude(newLocation.getLatitude());
				toSend.setLongitude(newLocation.getLongitude());
				
				// Notify any listening threads about the location change
				for (int lIndex=0; listeners.size()>lIndex; lIndex++)
				{
					listeners.get(lIndex).onLocationChanged(toSend);
				}
			}
			//TODO: Set the location equal to the new location
			location = newLocation;
		}
		// Renew the locationQueryTimer
		locationQueryTimer.postDelayed(this, MIN_TIME_BTWN_UPDATES);
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