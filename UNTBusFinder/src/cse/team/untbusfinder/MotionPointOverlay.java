package cse.team.untbusfinder;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

// This is a custom overlay class to allow for better control over
// the look and feel of moving location overlays (for example,
// to allow each overlay to have a different color)

// Based on code from DirectedLocationOverlay from the OSMDroid project,
// which is licensed under the Apache License.

public class MotionPointOverlay extends PointOverlay
{
	protected float pBearing;
	
	// Context constructor for MotionPointOverlay
	public MotionPointOverlay(final Context context)
	{
		super(context);
	}
	
	// ResourceProxy constructor for MotionPointOverlay
	public MotionPointOverlay(final ResourceProxy resourceProxy)
	{
		super(resourceProxy);
	}
	
	//TODO:
	public void setBearing(float newBearing)
	{
		pBearing = newBearing;
	}
	
	//TODO:
	public float getBearing()
	{
		return pBearing;
	}
	
	//TODO: Draw the MotionPointOverlay to the MapView at the location
	// given with the attributes defined
	@Override protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow)
	{
		//TODO: Before drawing, make sure the MotionPointOverlay has a location
		// and a bearing
		if ((pLocation!=null)&&(pBearing!=0))
		{
			//TODO: For the pre-shadow draw
			if (!shadow)
			{
				final Projection proj = mapView.getProjection();
				proj.toMapPixels(pLocation, screenCoords);
				
				//TODO: Draw the circle with the color and radius defined
				canvas.drawCircle(screenCoords.x, screenCoords.y, radius, pPaint);
				
				//TODO: Rotate the canvas to indicate the direction
				canvas.rotate(pBearing);
				
				//TODO: Draw the arrow that indicates the direction of the point
				canvas.drawText(">", screenCoords.x+radius, screenCoords.y+radius, pPaint);
			}
			//TODO: For the shadow draw
			else
			{
				// Do nothing
			}
		}
	}
}