package cse.team.untbusfinder;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class GPSretrieve extends Service implements LocationManager{
	private final Context mContext;
	boolean isGPSEnabled = false;
	boolean isNetworkEnabled= false;
	boolean canGetLocation;
	
	Location location;
	double latitude;
	double longitude;
	
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BTWN_UPDATES = 1000*60*1;
	
	protected LocationManager locationManager;
	
	public GPSretrieve(Context context){
		this.mContext = context;
		getLocation();
	}
	
	public Location getLocation(){
		try{
			locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
		
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		
		if(!isGPSEnabled && !isNetworkEnabled){
		}
		else {
			this.canGetLocation = true;
			
			if(isNetworkEnabled){
				locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
			Log.d("Network", "Network");
				if(locationManager != null){
					latitude = location.getLatitude();
					longitude = location.getLongitude();
				}
			}
		}
			
			}
			}
		}
	}

}
