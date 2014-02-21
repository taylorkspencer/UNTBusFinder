package cse.team.untbusfinder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity
{
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//TODO: Gets the coordinates representing the user's location
	// Called when the user clicks the Get Coordinates button
	public void get_coordinates(View view)
	{
	    // Do something in response to button

	}
	
	//TODO: Show coordinates as a toast
	// Called when the user clicks the Bounce button
	public void bounce_coordinates(View view)
	{
		//Need to get the coordinates from their internal location in storage
		Context context = getApplicationContext();
		CharSequence temptext = "Coordinates will be shown here."; //Placeholder for coordinates
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, temptext, duration);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
		
	}
	
	//TODO: Take the user's coordinates and displays them on a map (Taylor's)
	// Called when the user clicks the Map Coordinates button
	public void map_coordinates(View view)
	{
	    // Do something in response to button
		
	}
}
