package cse.team.untbusfinder;

import java.util.List;
import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.util.constants.MapViewConstants;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;

import android.app.Fragment;
import android.content.Intent;
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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.Toast;

public class MapFragment extends Fragment
{
	// Variables declared here so that they can be accessed in the LocationListeners
	// and MapListener
	MapView mapView;
	GPSretrieve gps;
	LocationCommunicator link;
	PointOverlay myLocOverlay;
	View mapToolbar;
	ImageButton toolbarZoomOutButton;
	ImageButton toolbarZoomInButton;
	ImageButton toolbarCenterOnLocationButton;
	Handler mapToolbarTimeoutTimer;
	List<PointOverlay> busStopOverlays;
	PointOverlay busStopOverlayStyle;
	List<PointOverlay> busLocOverlays;
	PointOverlay busLocOverlayStyle;
	List<PathOverlay> busPathOverlays;
	PathOverlay busPathOverlayStyle;
	
	boolean centerOnMyLocation, sendingLocation = false;
	
	// Constants
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MAX_TIME_BTWN_UPDATES = 1000*100; // In milliseconds (100s currently)
	private static final long MIN_TIME_BTWN_UPDATES = 1000*10; // In milliseconds (10s interval)
	private static final int MAX_TIME_TO_WAIT = 1000*10; // In milliseconds (10 seconds)
	private static final long MAP_TOOLBAR_TIMEOUT = 1000*10; // In milliseconds (10s interval)
	
	// Display the fragment and register the controls
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_map, container, false);
		
		// Retrieve the GPSretrieve and LocationCommunicator services
		gps = GPSretrieve.getInstance();
		link = LocationCommunicator.getInstance();
		
		// Set up the OpenStreetMaps view
		mapView = (MapView)v.findViewById(R.id.generalMap);
		
		// Set the initial zoom level
		mapView.getController().setZoom(15);
		
		// Enable pinch-to-zoom
		mapView.setMultiTouchControls(true);
		
		// Set the URL of the server to be queried by the LocationCommunicator
		link.setServerURL("http://untbusfinder.no-ip.org/");
		
		// Set up the overlays
		busPathOverlayStyle = new PathOverlay(Color.TRANSPARENT, mapView.getContext());
		busStopOverlayStyle = new StationaryPointOverlay(mapView.getContext());
		
		// Set up the lists for the overlays
		busPathOverlays = new ArrayList<PathOverlay>();
		busStopOverlays = new ArrayList<PointOverlay>();
		busLocOverlays = new ArrayList<PointOverlay>();
		
		// Listen for a location update from GPSretrieve and if one is received,
		// change the map control to that location
		gps.requestLocationUpdates(new LocationListener()
		{
			@Override public void onLocationChanged(Location location)
			{
				// Make sure the new location isn't null
				if (location!=null)
				{
					// If the user has not moved the focus, center the map on the changed location
					if (centerOnMyLocation)
					{
						mapView.getController().setCenter(new GeoPoint(location));
						
						// Since changing the center of the map will set centerOnMyLocation to
						// false, reset centerOnMyLocation to true
						centerOnMyLocation = true;
					}
					
					// If the point has a bearing, declare a MotionPointOverlay
					// and set its point in the direction of the change
					if (location.hasBearing())
					{
						// Declare the myLocOverlay as a MotionPointOverlay
						myLocOverlay = new MotionPointOverlay(mapView.getContext());
						
						// Determine the direction of the change (this needs to be further tested)
						myLocOverlay.setBearing(location.getBearing());
					}
					else
					{
						// Declare the myLocOverlay as a StationaryPointOverlay
						myLocOverlay = new StationaryPointOverlay(mapView.getContext());
					}
					// Set the shared default attributes of the point
					myLocOverlay = setMyLocOverlayAttributes(myLocOverlay);
					
					// Update the myLocOverlay to the changed location
					myLocOverlay.setLocation(new GeoPoint(location));
					
					// Remove the old overlays and replace them with the new ones
					reloadOverlays();
				}
			}
			
			@Override public void onProviderDisabled(String provider)
			{
				// Do nothing - we don't care about this change
			}
			
			@Override public void onProviderEnabled(String provider)
			{
				// Do nothing - we don't care about this change
			}
			
			@Override public void onStatusChanged(String provider, int status, Bundle bundle)
			{
				// Do nothing - we don't care about this change
			}
		});
		
		// Listen for a location update from LocationCommunicator
		link.requestLocationUpdates(new LocationListener()
		{
			@Override public void onLocationChanged(Location location)
			{
				// Make sure the new location isn't null
				if (location!=null)
				{
					PointOverlay busLocOverlay;
					// If there was a previous location, remove the previous
					// location from the array, declare a MotionPointOverlay,
					// and set its point in the direction of the change
					if (link.getLastLocation()!=null)
					{
						// Look through the busLocOverlays list for a PointOverlay
						// with the same location as the last location
						for (int point=0, pntFound=0; ((point<busLocOverlays.size())&&(pntFound==0)); point++)
						{
							// If the location of this point is equal to the last location, remove
							// it and exit the loop
							if (busLocOverlays.get(point).getLocation().equals(link.getLastLocation()))
							{
								busLocOverlays.remove(location);
								pntFound = 1;
							}
						}
						
						// Declare the busLocOverlay as a MotionPointOverlay
						busLocOverlay = new MotionPointOverlay(mapView.getContext());
						
						// Determine the direction of the change (this needs to be further tested)
						busLocOverlay.setBearing(location.getBearing());
					}
					else
					{
						// Declare the busLocOverlay as a StationaryPointOverlay
						busLocOverlay = new StationaryPointOverlay(mapView.getContext());
					}
					// Set the shared default attributes of the point
					busLocOverlay = setBusLocOverlayAttributes(busLocOverlay);
					
					// Set the location of the busLocOverlay to the changed location
					busLocOverlay.setLocation(new GeoPoint(location));
					
					// Add the point to the busLocOverlays array
					busLocOverlays.add(busLocOverlay);
					
					// Remove the old overlays and replace them with the new ones
					reloadOverlays();
				}
			}
			
			@Override public void onProviderDisabled(String provider)
			{
				// Do nothing - we don't care about this change
			}
			
			@Override public void onProviderEnabled(String provider)
			{
				// Do nothing - we don't care about this change
			}
			
			@Override public void onStatusChanged(String provider, int status, Bundle bundle)
			{
				// Do nothing - we don't care about this change
			}
		});
		
		// Display the path for the Discovery Park route (temporary implementation
		// until we start getting this from the server)
		addDiscParkBusRoute();
		
		// Add the bus stops for the Discovery Park route (temporary implementation
		// until we start getting this from the server)
		addDiscParkBusRouteStops();
		
		// Listen for events from the MapView to turn off centering of the map
		// on the user's location when the user changes the focus
		mapView.setMapListener(new MapListener()
		{
			@Override public boolean onScroll(ScrollEvent event)
			{
				// Turn off centering on the user's location
				centerOnMyLocation = false;
				return true;
			}
			
			@Override public boolean onZoom(ZoomEvent event)
			{
				// Re-set the state of the items in the map toolbar in
				// case an item has to be disabled
				setMapToolbarButtonsState();
				return true;
			}
		});
		
		// Save the controls for the map toolbar
		mapToolbar = v.findViewById(R.id.generalMapToolbar);
		toolbarZoomOutButton = (ImageButton)v.findViewById(R.id.generalMapZoomOut);
		toolbarZoomInButton = (ImageButton)v.findViewById(R.id.generalMapZoomIn);
		toolbarCenterOnLocationButton = (ImageButton)v.findViewById(R.id.generalMapCenterOnLocation);
		
		// Set up the map toolbar for the MapView
		setUpMapToolbar();
		
		// Add a TouchListener to the MapView to show the map toolbar
		mapView.setOnTouchListener(new OnTouchListener()
		{
			@Override public boolean onTouch(View view, MotionEvent event)
			{
				// If the map toolbar is not visible, show the map toolbar
				if (mapToolbar.getVisibility()!=View.VISIBLE)
				{
					showMapToolbar();
				}
				// Return false so other listeners can receive their events
				return false;
			}
		});
		
		// Have the map center on the user's location until the user changes the focus
		centerOnMyLocation = true;
		
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
	@Override public void onResume()
	{
		// Begin polling for location from GPSretrieve
		gps.startPolling();
		
		// Begin polling for location from LocationCommunicator
		link.startPolling();
		
		// If available, display the most recent location from GPSretrieve
		if (gps.getLocation()!=null)
		{
			// If the user has not moved the focus, center the map on the changed location
			if (centerOnMyLocation)
			{
				mapView.getController().setCenter(new GeoPoint(gps.getLocation()));
				
				// Since changing the center of the map will set centerOnMyLocation to
				// false, reset centerOnMyLocation to true
				centerOnMyLocation = true;
			}
			
			// If the location has a bearing, declare a MotionPointOverlay
			// and set its point in the direction of the change
			if (gps.hasBearing())
			{
				// Declare the myLocOverlay as a MotionPointOverlay
				myLocOverlay = new MotionPointOverlay(mapView.getContext());
				
				// Determine the direction of the change (this needs to be further tested)
				myLocOverlay.setBearing(gps.getLocation().getBearing());
			}
			else
			{
				// Declare the myLocOverlay as a StationaryPointOverlay
				myLocOverlay = new StationaryPointOverlay(mapView.getContext());
			}
			// Set the shared default attributes of the point
			myLocOverlay = setMyLocOverlayAttributes(myLocOverlay);
			
			// Update the myLocOverlay to the changed location
			myLocOverlay.setLocation(new GeoPoint(gps.getLocation()));
		}
		
		// If available, display the most recent location from LocationCommunicator
		if (link.getLocation()!=null)
		{
			PointOverlay busLocOverlay = new StationaryPointOverlay(mapView.getContext());;
			
			// Set the shared default attributes of the point
			busLocOverlay = setBusLocOverlayAttributes(busLocOverlay);
			
			// Set the location of the busLocOverlay to the changed location
			busLocOverlay.setLocation(new GeoPoint(link.getLocation()));
			
			// Add the point to the busLocOverlays array
			busLocOverlays.add(busLocOverlay);
		}
		super.onResume();
	}
	
	// Sets up the actions for the items in the map toolbar
	protected void setUpMapToolbar()
	{
		final Runnable hideMapToolbar = new Runnable()
		{
			@Override public void run()
			{
				hideMapToolbar();
			}
		};
		
		// Add the action for the Zoom Out button
		toolbarZoomOutButton.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View view)
			{
				// Zoom out on the map
				mapView.getController().zoomOut();
				
				// Reset the map toolbar timeout timer
				mapToolbarTimeoutTimer.removeCallbacks(hideMapToolbar);
				mapToolbarTimeoutTimer.postDelayed(hideMapToolbar, MAP_TOOLBAR_TIMEOUT);
			}
		});
		
		// Add the action for the Zoom In button
		toolbarZoomInButton.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View view)
			{
				// Zoom in on the map
				mapView.getController().zoomIn();
				
				// Reset the map toolbar timeout timer
				mapToolbarTimeoutTimer.removeCallbacks(hideMapToolbar);
				mapToolbarTimeoutTimer.postDelayed(hideMapToolbar, MAP_TOOLBAR_TIMEOUT);
			}
		});
		
		// Add the action for the Center on Location button
		toolbarCenterOnLocationButton.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View view)
			{
				// Center the map on the location
				mapView.getController().animateTo(new GeoPoint(gps.getLocation()));
				
				// Have the map center on the user's location until the user changes the focus
				centerOnMyLocation = true;
				
				// Reset the map toolbar timeout timer
				mapToolbarTimeoutTimer.removeCallbacks(hideMapToolbar);
				mapToolbarTimeoutTimer.postDelayed(hideMapToolbar, MAP_TOOLBAR_TIMEOUT);
			}
		});
		
		// Create a timer to hide the map 
		mapToolbarTimeoutTimer = new Handler();
		mapToolbarTimeoutTimer.postDelayed(hideMapToolbar, MAP_TOOLBAR_TIMEOUT);
	}
	
	// Set the states for the buttons on the map toolbar
	public void setMapToolbarButtonsState()
	{
		// Determine if we can zoom out
		if (mapView.canZoomOut())
		{
			// If so, enable the Zoom Out button
			toolbarZoomOutButton.setEnabled(true);
			toolbarZoomOutButton.setImageResource(R.drawable.zoom_out_button);
		}
		else
		{
			// If so, disable the Zoom Out button
			toolbarZoomOutButton.setEnabled(false);
			toolbarZoomOutButton.setImageResource(R.drawable.zoom_out_disabled);
		}
		
		// Determine if we can zoom in
		if (mapView.canZoomIn())
		{
			// If so, enable the Zoom In button
			toolbarZoomInButton.setEnabled(true);
			toolbarZoomInButton.setImageResource(R.drawable.zoom_in_button);
		}
		else
		{
			// If so, disable the Zoom In button
			toolbarZoomInButton.setEnabled(false);
			toolbarZoomInButton.setImageResource(R.drawable.zoom_in_disabled);
		}
		
		// Determine if we have a location
		if (gps.getLocation()!=null)
		{
			// If so, enable the Center on Location button
			toolbarCenterOnLocationButton.setEnabled(true);
			toolbarCenterOnLocationButton.setImageResource(R.drawable.center_on_location_button);
		}
		else
		{
			// If not, disable the Center on Location button
			toolbarCenterOnLocationButton.setEnabled(false);
			toolbarCenterOnLocationButton.setImageResource(R.drawable.center_on_location_disabled);
		}
	}
	
	// Show the map toolbar from the MapView
	public void showMapToolbar()
	{
		// Set the states for the buttons on the map toolbar
		setMapToolbarButtonsState();
		
		// Show the map toolbar
		mapToolbar.setVisibility(View.VISIBLE);
	}
	
	// Hide the map toolbar from the MapView
	public void hideMapToolbar()
	{
		mapToolbar.setVisibility(View.GONE);
	}
	
	// Display the path for the Discovery Park route (temporary implementation
	// until we start getting this from the server)
	protected void addDiscParkBusRoute()
	{
		PathOverlay discoveryParkRoutePath = busPathOverlayStyle;
		discoveryParkRoutePath.setColor(Color.parseColor("#009933"));
		
		// From Discovery Park to UNT main campus
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25352, -97.15418));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25267, -97.15481));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25105, -97.15173));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25107, -97.15104));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25143, -97.15036));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25343, -97.14891));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25367, -97.14884));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25452, -97.15052));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25387, -97.15100));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25292, -97.14923));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25143, -97.15036));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25107, -97.15104));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25105, -97.15173));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25110, -97.15185));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25066, -97.15208));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25483, -97.16008));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25437, -97.16039));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25367, -97.16032));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.22065, -97.16130));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.22027, -97.16145));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21973, -97.16191));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21937, -97.16202));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21632, -97.16136));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21473, -97.16139));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21475, -97.15503));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21152, -97.15507));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21150, -97.15366));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21156, -97.15317));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21156, -97.15289));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21371, -97.15288));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21367, -97.14853));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21369, -97.14846));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21376, -97.14844));
		
		// From UNT main campus to Discovery Park
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21569, -97.14840));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21580, -97.16137));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21632, -97.16136));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21937, -97.16202));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.21973, -97.16191));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.22027, -97.16145));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.22065, -97.16130));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25367, -97.16032));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25437, -97.16039));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25471, -97.16020));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25044, -97.15207));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25105, -97.15173));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25267, -97.15481));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25288, -97.15465));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25271, -97.15428));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25333, -97.15379));
		discoveryParkRoutePath.addPoint(new GeoPoint(33.25352, -97.15418));
		busPathOverlays.add(discoveryParkRoutePath);
	}
	
	// Add the bus stops for the Discovery Park route (temporary implementation
	// until we start getting this from the server)
	protected void addDiscParkBusRouteStops()
	{
		// Add the first bus stop for the Discovery Park route
		PointOverlay dpRouteStop1 = new StationaryPointOverlay(busStopOverlayStyle);
		dpRouteStop1 = setBusStopOverlayAttributes(dpRouteStop1);
		dpRouteStop1.setColor(Color.parseColor("#009933"));
		dpRouteStop1.setLocation(new GeoPoint(33.25340, -97.15394));
		busStopOverlays.add(dpRouteStop1);
		
		// Add the second bus stop for the Discovery Park route
		PointOverlay dpRouteStop2 = new StationaryPointOverlay(busStopOverlayStyle);
		dpRouteStop2 = setBusStopOverlayAttributes(dpRouteStop2);
		dpRouteStop2.setColor(Color.parseColor("#009933"));
		dpRouteStop2.setLocation(new GeoPoint(33.25372, -97.15073));
		busStopOverlays.add(dpRouteStop2);
		
		// Add the third bus stop for the Discovery Park route
		PointOverlay dpRouteStop3 = new StationaryPointOverlay(busStopOverlayStyle);
		dpRouteStop3 = setBusStopOverlayAttributes(dpRouteStop3);
		dpRouteStop3.setColor(Color.parseColor("#009933"));
		dpRouteStop3.setLocation(new GeoPoint(33.21396, -97.14843));
		busStopOverlays.add(dpRouteStop3);
	}
	
	// Set the shared default attributes for the myLocOverlay point
	protected PointOverlay setMyLocOverlayAttributes(PointOverlay point)
	{
		// Set the appearance attributes of the point
		point.setAlpha(255);
		point.setColor(Color.BLUE);
		point.setRadius(15);
		return point;
	}
	
	// Set the shared default attributes for the busLocOverlay point
	protected PointOverlay setBusLocOverlayAttributes(PointOverlay point)
	{
		// Set the appearance attributes of the point
		point.setAlpha(255);
		point.setRadius(15);
		return point;
	}
	
	// Set the shared default attributes for the busStopOverlay point
	protected PointOverlay setBusStopOverlayAttributes(PointOverlay point)
	{
		// Set the appearance attributes of the point
		point.setAlpha(255);
		point.setRadius(15);
		return point;
	}
	
	// Remove the old overlays and replace them with the new ones
	protected void reloadOverlays()
	{
		mapView.getOverlays().clear();
		mapView.getOverlays().addAll(busPathOverlays);
		mapView.getOverlays().addAll(busStopOverlays);
		mapView.getOverlays().addAll(busLocOverlays);
		mapView.getOverlays().add(myLocOverlay);
	}
	
	// If the location is not being sent to the server, stop polling for location
	// when UNT Bus Finder closes
	@Override public void onPause()
	{
		// Stop polling for location from GPSretrieve
		gps.stopPolling();
		
		// Stop polling for location from LocationCommunicator
		link.stopPolling();
		
		super.onPause();
	}
	
	// Display an error message toast and change the toggle
	// text to Send My Location
	protected void onLocationSendingError()
	{
		Toast.makeText(getActivity(), "Could not send the location to the server.", Toast.LENGTH_SHORT).show();
		getActivity().invalidateOptionsMenu();
	}
}