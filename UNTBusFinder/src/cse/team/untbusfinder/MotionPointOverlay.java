package cse.team.untbusfinder;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	protected final Matrix directionRotater = new Matrix();
	protected final Bitmap arrow;
	
	protected final float arrowCenterX;
	protected final float arrowCenterY;
	protected final int arrowWidth;
	protected final int arrowHeight;
	
	// Context constructor for MotionPointOverlay
	public MotionPointOverlay(final Context context)
	{
		super(context);
		
		// Initialize the arrow and the variables related to it
		arrow = mResourceProxy.getBitmap(ResourceProxy.bitmap.direction_arrow);
		
		arrowWidth = arrow.getWidth();
		arrowHeight = arrow.getHeight();
		
		arrowCenterX = arrowWidth/2-.5f;
		arrowCenterY = arrowHeight/2-.5f;
	}
	
	// ResourceProxy constructor for MotionPointOverlay
	public MotionPointOverlay(final ResourceProxy resourceProxy)
	{
		super(resourceProxy);
		
		// Initialize the arrow and the variables related to it
		arrow = resourceProxy.getBitmap(ResourceProxy.bitmap.direction_arrow);
		
		arrowWidth = arrow.getWidth();
		arrowHeight = arrow.getHeight();
		
		arrowCenterX = arrowWidth/2-.5f;
		arrowCenterY = arrowHeight/2-.5f;
	}
	
	// Copy constructor for MotionPointOverlay
	public MotionPointOverlay(PointOverlay copying)
	{
		super(copying);
		
		// Initialize the arrow and the variables related to it
		arrow = mResourceProxy.getBitmap(ResourceProxy.bitmap.direction_arrow);
		
		arrowWidth = arrow.getWidth();
		arrowHeight = arrow.getHeight();
		
		arrowCenterX = arrowWidth/2-.5f;
		arrowCenterY = arrowHeight/2-.5f;
	}
	
	// Set the bearing for the MotionPointOverlay
	public void setBearing(float newBearing)
	{
		pBearing = newBearing;
	}
	
	// Get the bearing for the MotionPointOverlay
	public float getBearing()
	{
		return pBearing;
	}
	
	// Draw the MotionPointOverlay to the MapView at the location
	// given with the attributes defined
	@Override protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow)
	{
		// Before drawing, make sure the MotionPointOverlay has a location
		// and a bearing
		if ((pLocation!=null)&&(pBearing!=0))
		{
			// For the pre-shadow draw
			if (!shadow)
			{
				final Projection proj = mapView.getProjection();
				proj.toPixels(pLocation, screenCoords);
				
				// Draw the circle with the color and radius defined
				canvas.drawCircle(screenCoords.x, screenCoords.y, radius, pPaint);
				
				// Rotate the canvas to indicate the direction
				directionRotater.setRotate(pBearing, arrowCenterX, arrowCenterY);
				
				// Draw the arrow that indicates the direction of the point
				final Bitmap resizedArrow = Bitmap.createScaledBitmap(arrow, (int)(radius*2),
						(int)(radius*2), false);
				final Bitmap rotatedArrow = Bitmap.createBitmap(resizedArrow, 0, 0, resizedArrow.getWidth(),
						resizedArrow.getHeight(), directionRotater, false);
				canvas.drawBitmap(rotatedArrow, screenCoords.x-rotatedArrow.getWidth()/2,
						screenCoords.y-rotatedArrow.getHeight()/2, pPaint);
			}
			// For the shadow draw
			else
			{
				// Do nothing
			}
		}
	}
}