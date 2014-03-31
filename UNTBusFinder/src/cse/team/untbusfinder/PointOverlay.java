package cse.team.untbusfinder;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;

// This is a unified parent class for StationaryPointOverlay and
// MotionPointOverlay to allow overlays to be converted between
// the two types at runtime depending on their status

// Also allows for code sharing in areas where the two overlays
// act identically

public abstract class PointOverlay extends Overlay
{
	public PointOverlay(final Context context)
	{
		super(context);
	}
	
	public PointOverlay(final ResourceProxy resourceProxy)
	{
		super(resourceProxy);
	}
	
	protected abstract void draw(Canvas arg0, MapView arg1, boolean arg2);
}