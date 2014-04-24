package cse.team.untbusfinder;

import android.app.Activity;
import android.app.ActionBar;
import android.os.Bundle;

public class MapActivity extends Activity
{
	// Display the activity and register the controls
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		// Adjust the action bar for this activity
		setupActionBar();
	}
	
	@Override protected void onStart()
	{
		super.onStart();
	}
	
	// Adjust the action bar for this activity
	private void setupActionBar()
	{
		// Determine if this activity is a child
		if (!isTaskRoot())
		{
			// If so, enable the up option
			getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
		}
		else
		{
			// If not, disable the up option
			getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		}
	}
	
	// Go to the previous activity, or if this is the first activity, hide
	@Override public void onBackPressed()
	{
		finish();
	}
	
	// If there is a previous activity, navigate back to it
	// Returns true if navigation occurred, otherwise return false
	@Override public boolean onNavigateUp()
	{
		// Determine if this activity is a child
		if (!isTaskRoot())
		{
			// If so, go to the previous activity and return true
			onBackPressed();
			return true;
		}
		else
		{
			// If not, do nothing and return false
			return false;
		}
	}
}