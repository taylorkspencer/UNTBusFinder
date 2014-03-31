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
	
	//TODO:
	public void setColor(int newColor)
	{
		pPaint.setColor(newColor);
	}
	
	//TODO:
	public int getColor()
	{
		return pPaint.getColor();
	}
	
	//TODO:
	public void setAlpha(int newAlpha)
	{
		pPaint.setAlpha(newAlpha);
	}
	
	//TODO:
	public int getAlpha()
	{
		return pPaint.getAlpha();
	}
	
	//TODO:
	public void setLocation(GeoPoint newLocation)
	{
		pLocation = newLocation;
	}
	
	//TODO:
	public GeoPoint getLocation()
	{
		return pLocation;
	}
	
	//TODO:
	public void setRadius(float newRadius)
	{
		radius = newRadius;
	}
	
	//TODO:
	public float getRadius()
	{
		return radius;
	}
	
	// Unimplemented stub (PointOverlay, by design, isn't capable of drawing itself)
	protected abstract void draw(final Canvas canvas, final MapView mapView, final boolean shadow);
}