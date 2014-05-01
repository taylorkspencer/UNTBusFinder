package cse.team.untbusfinder;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

// This is a unified parent class for StationaryPointOverlay and
// MotionPointOverlay to allow overlays to be converted between
// the two types at runtime depending on their status

// Also allows for code sharing in areas where the two overlays
// act identically

public abstract class PointOverlay extends Overlay
{
    protected GeoPoint pLocation;
    protected Paint pPaint = new Paint();
    protected final Point screenCoords = new Point();
	protected float radius;
    
	// Context constructor for PointOverlay
	public PointOverlay(final Context context)
	{
		super(context);
	}
	
	// ResourceProxy constructor for PointOverlay
	public PointOverlay(final ResourceProxy resourceProxy)
	{
		super(resourceProxy);
	}
	
	// Copy constructor for PointOverlay
	public PointOverlay(PointOverlay copying)
	{
		super(copying.mResourceProxy);
	}
	
	// Set the color for the PointOverlay
	public void setColor(int newColor)
	{
		pPaint.setColor(newColor);
	}
	
	// Get the color for the PointOverlay
	public int getColor()
	{
		return pPaint.getColor();
	}
	
	// Set the alpha value for the PointOverlay
	public void setAlpha(int newAlpha)
	{
		pPaint.setAlpha(newAlpha);
	}
	
	// Get the alpha value for the PointOverlay
	public int getAlpha()
	{
		return pPaint.getAlpha();
	}
	
	// Set the location for the PointOverlay
	public void setLocation(GeoPoint newLocation)
	{
		pLocation = newLocation;
	}
	
	// Get the location for the PointOverlay
	public GeoPoint getLocation()
	{
		return pLocation;
	}
	
	// Set the radius for the PointOverlay
	public void setRadius(float newRadius)
	{
		radius = newRadius;
	}
	
	// Get the radius for the PointOverlay
	public float getRadius()
	{
		return radius;
	}
	
	// Set the bearing for the PointOverlay (unimplemented stub)
	public abstract void setBearing(float newBearing);
	
	// Get the bearing for the PointOverlay (unimplemented stub)
	public abstract float getBearing();
	
	// Unimplemented stub (PointOverlay, by design, isn't capable of drawing itself)
	protected abstract void draw(final Canvas canvas, final MapView mapView, final boolean shadow);
}