package cse.team.untbusfinder;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.Runnable;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Handler;

import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.json.JSONObject;
import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import cse.team.untbusfinder.RouteActivity.LocationSendingTimer;

public class LocationCommunicator extends Service
{
	// Variables declared here so that they can be accessed in the LocationListener
	boolean isNetworkEnabled = false;
	boolean isPolling = false;
	boolean pollingStateLock = false;
	
	LocationReceivingTimer locationQueryTimer;
	HttpParams communicatorParams;
	HttpClient locServerClient;
	String serverURL;
	
	//TODO: To be converted to an array to store multiple locations
	GeoPoint location;
	
	// Static instance of this Service (so Activities can access it)
	static LocationCommunicator sInstance;
	
	// Static instance of the LocationSendingTimer (so RouteActivity can access it)
	static AsyncTask lstInstance;
	
	// An ArrayList of LocationListeners is declared here so that other classes
	// can listen for location updates
	ArrayList<LocationListener> listeners = new ArrayList<LocationListener>();
	
	// Constants
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MAX_TIME_BTWN_UPDATES = 1000*100; // In milliseconds (100s currently)
	private static final long MIN_TIME_BTWN_UPDATES = 1000*10; // In milliseconds (10s interval)
	private static final int MAX_TIME_TO_WAIT = 1000*10; // In milliseconds (10 seconds)
	
	@Override public void onCreate()
	{
		super.onCreate();
		
		// Store a static instance of this Service in sInstance
		sInstance = this;
		
		// Set the parameters for the LocationCommunicator HTTP client
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
		
		// Create the HTTP client and set its timeout
		locServerClient = new DefaultHttpClient(communicatorParams);
	}
	
	// Return a static instance of this Service for Activities
	static public LocationCommunicator getInstance()
	{
		return sInstance;
	}
	
	// Return the LocationSendingTimer
	static public AsyncTask getLocationSendingTimer()
	{
		return lstInstance;
	}
	
	// Create a LocationSendingTimer
	public void setLocationSendingTimer(AsyncTask newLst)
	{
		lstInstance = newLst;
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
	
	// Start polling the server for location updates
	public void startPolling()
	{
		// If the polling state is locked, do nothing
		if (!isPollingStateLocked())
		{
			// Only enable polling if it is not already enabled
			if (!isPolling())
			{
				// Check to make sure the server URL is not null or empty
				if ((serverURL!=null)&&(serverURL.length()>0))
				{
					// Query the server for location updates and if one is received,
					// send the new location to any listeners
					locationQueryTimer = new LocationReceivingTimer();
					locationQueryTimer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MIN_TIME_BTWN_UPDATES);
					
					// If all this is successful, set isPolling to true to indicate that
					// polling for location has begun
					isPolling = true;
				}
			}
		}
	}
	
	// Stop polling the server for location updates
	public void stopPolling()
	{
		// If the polling state is locked, do nothing
		if (!isPollingStateLocked())
		{
			// Only disable polling if it is already enabled
			if (isPolling())
			{
				// Stop querying the server for location updates
				locationQueryTimer.cancel(false);
				
				// Set isPolling to false to indicate that polling for location has stopped
				isPolling = false;
			}
		}
	}
	
	// Returns the state of the polling lock (used by the location
	// sending methods to prevent GPS polling from going off)
	public boolean isPollingStateLocked()
	{
		return pollingStateLock;
	}
	
	// Returns whether LocationCommunicator is actively polling the server for locations
	public boolean isPolling()
	{
		return isPolling;
	}
	
	// Return the last location, or null if there isn't any
	// Used for comparing locations
	//TODO: This method will need to be changed once we start getting multiple
	// locations
	public GeoPoint getLastLocation()
	{
		return location;
	}
	
	// Return the most recent location available
	//TODO: This method will need to be changed once we start getting multiple
	// locations
	public GeoPoint getLocation()
	{
		// If location is null, return null (We aren't going to do requests on
		// demand because we don't want to over-stress the server)
		if (location==null)
		{
			return null;
		}
		return location;
	}
	
	// Send a location to the server
	// Returns true if sending location was successful, sends false if not
	//TODO: Send bus route along with location
	public boolean sendLocation(GeoPoint sendingLoc)
	{
		// Create a HttpPost request containing the latitude and longitude from the location
		HttpPost sendingLocPost = new HttpPost(serverURL);
		List<NameValuePair> locPair = new ArrayList<NameValuePair>(2);
		locPair.add(new BasicNameValuePair("lat", Double.toString(sendingLoc.getLatitude())));
		locPair.add(new BasicNameValuePair("long", Double.toString(sendingLoc.getLongitude())));
		try
		{
			sendingLocPost.setEntity(new UrlEncodedFormEntity(locPair));
		}
		// If the syntax could not be encoded, return false to indicate a failure
		catch (UnsupportedEncodingException badSyntax)
		{
			return false;
		}
		
		// Send the location to the server as a POST request
		HttpResponse locServerResponse;
		try
		{
			locServerResponse = locServerClient.execute(sendingLocPost);
		}
		// If the server is not a HTTP server, return false to indicate a failure
		catch (ClientProtocolException notHTTPserver)
		{
			return false;
		}
		// If the connection could not be established, return false to indicate a failure
		catch (IOException serverConnectionError)
		{
			return false;
		}
		// If all this is successful, return true
		return true;
	}
	
	class LocationReceivingTimer extends AsyncTask<Long, Void, String> implements Runnable
	{
		Handler taskTimer;
		long taskInterval;
		
		@Override public void run()
		{
			new LocationReceivingTimer().execute(taskInterval);
		}
		
		@Override protected void onPreExecute()
		{
			// Initialize the Handler
			taskTimer = new Handler();
		}
		
		// Get locations from the server
		@Override protected String doInBackground(Long... interval)
		{
			// Set the time interval for the Handler
			taskInterval = interval[0];
			
			// Attempt to connect to the server
			HttpGet locServerHTTPget;
			try
			{
				locServerHTTPget = new HttpGet(serverURL);
			}
			// If the URL is invalid, return null
			catch (IllegalArgumentException invalidURL)
			{
				return null;
			}
			
			// Add our user-agent
			try
			{
				locServerHTTPget.addHeader("User-Agent", "UNTBusFinder/"+getPackageManager().getPackageInfo(getPackageName(), 0).versionName+" (Android)");
			}
			catch (PackageManager.NameNotFoundException notInstalled)
			{
				// Do nothing
			}
			
			// Query the server for location updates
			HttpResponse locServerResponse = null;
			try
			{
				locServerResponse = locServerClient.execute(locServerHTTPget);
			}
			// If the server is not a HTTP server, return null
			catch (ClientProtocolException notHTTPserver)
			{
				return null;
			}
			// If the connection could not be established, return null
			catch (IOException serverConnectionError)
			{
				return null;
			}
			
			// Parse the server response to a string and return it
			try
			{
				return EntityUtils.toString(locServerResponse.getEntity());
			}
			// If an IOException occurred, return null
			catch (IOException responseIOexception)
			{
				return null;
			}
			// If an ParseException occurred, return null
			catch (ParseException responseParseException)
			{
				return null;
			}
		}
		
		@Override protected void onPostExecute(String locServerResponse)
		{
			// If the server returned a response, parse it into JSON
			if (locServerResponse!=null)
			{
				JSONObject responseJSON = null;
				try
				{
					responseJSON = new JSONObject(locServerResponse);
				}
				// If the JSON is invalid, stop polling and exit the function
				catch (JSONException invalidJSON)
				{
					stopPolling();
					return;
				}
				// If the JSON is invalid, stop polling and exit the function
				catch (ParseException JSONparseException)
				{
					stopPolling();
					return;
				}
				// If the server didn't return any JSON, stop polling and exit the function
				catch (NullPointerException JSONnullException)
				{
					stopPolling();
					return;
				}
				//TODO: Allow for retrieval of multiple locations
				// Convert the JSON into a GeoPoint location
				GeoPoint newLocation = null;
				try
				{
					newLocation = new GeoPoint(responseJSON.getDouble("lat"), responseJSON.getDouble("long"));
				}
				// If there is no location in the JSON, stop polling and exit the function
				catch (JSONException noLocationInJSON)
				{
					stopPolling();
					return;
				}
				// If the previous location is null, or if a location is changed by
				// MIN_DISTANCE_CHANGE_FOR_UPDATES or more, update the location
				if ((location==null)||(newLocation.distanceTo(location)>=MIN_DISTANCE_CHANGE_FOR_UPDATES))
				{
					// Convert the GeoPoint to a Location
					Location toSend = new Location("LocationCommunicator");
					toSend.setLatitude(newLocation.getLatitude());
					toSend.setLongitude(newLocation.getLongitude());
					
					// If the previous location is not null, set the bearing of the new location
					if (location!=null)
					{
						toSend.setBearing((float)location.bearingTo(newLocation));
					}
					
					// Notify any listening threads about the location change
					for (int lIndex=0; listeners.size()>lIndex; lIndex++)
					{
						listeners.get(lIndex).onLocationChanged(toSend);
					}
					
					// Set the location equal to the new location
					location = newLocation;
				}
				
				// Renew the locationQueryTimer
				taskTimer.postDelayed(this, MIN_TIME_BTWN_UPDATES);
			}
			// If polling failed, stop polling
			else
			{
				stopPolling();
			}
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