package cse.team.untbusfinder;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

// This is a custom overlay class to allow for better control over
// the look and feel of stationary location overlays (for example,
// to allow each overlay to have a different color)

// Based on code from SimpleLocationOverlay from the OSMDroid project,
// which is licensed under the Apache License.

public class StationaryPointOverlay extends PointOverlay
{
	// Context constructor for StationaryPointOverlay
	public StationaryPointOverlay(final Context context)
	{
		super(context);
	}
	
	// ResourceProxy constructor for StationaryPointOverlay
	public StationaryPointOverlay(final ResourceProxy resourceProxy)
	{
		super(resourceProxy);
	}
	
	//TODO: Draw the StationaryPointOverlay to the MapView at the location
	// given with the attributes defined
	@Override protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow)
	{
		//TODO: Before drawing, make sure the StationaryPointOverlay has a location
		if (pLocation!=null)
		{
			//TODO: For the pre-shadow draw
			if (!shadow)
			{
				final Projection proj = mapView.getProjection();
				proj.toMapPixels(pLocation, screenCoords);
				
				//TODO: Draw the circle with the color and radius defined
				canvas.drawCircle(screenCoords.x, screenCoords.y, radius, pPaint);
			}
			//TODO: For the shadow draw
			else
			{
				// Do nothing
			}
		}
	}
}