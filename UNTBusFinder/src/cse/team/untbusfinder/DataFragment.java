package cse.team.untbusfinder;

//TODO: Clean out imports
import java.util.List;
import java.util.ArrayList;

import android.app.Fragment;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class DataFragment extends Fragment
{
	// Display the fragment and register the controls
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_data, container, false);
		
		//TODO: Load information for the route selected
		
		return v;
	}
	
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
	@Override public void onStart()
	{
		super.onStart();
	}
	
	// Start and/or resume polling for location
	// (here as a placeholder for when we start polling for location in this
	// activity)
	@Override public void onResume()
	{
		super.onResume();
	}
	
	// If the location is not being sent to the server, stop polling for location
	// when UNT Bus Finder closes
	// (here as a placeholder for when we start polling for location in this
	// activity)
	@Override public void onPause()
	{
		super.onPause();
	}
}