package cse.team.untbusfinder;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;

// This is a custom overlay class to allow for better control over
// the look and feel of moving location overlays (for example,
// to allow each overlay to have a different color)

public class MotionPointOverlay extends PointOverlay
{
	public MotionPointOverlay(final Context context)
	{
		super(context);
	}
	
	public MotionPointOverlay(final ResourceProxy resourceProxy)
	{
		super(resourceProxy);
	}
	
	@Override protected void draw(Canvas arg0, MapView arg1, boolean arg2)
	{
		//TODO: Auto-generated method stub
		
	}
}