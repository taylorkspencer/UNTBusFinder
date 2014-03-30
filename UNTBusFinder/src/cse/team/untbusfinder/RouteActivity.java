package cse.team.untbusfinder;

import android.os.Bundle;
import android.app.Activity;
import android.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.NavUtils;
import android.app.Fragment;

public class RouteActivity extends Activity
{
	public static class MapFragment extends Fragment
	{
		@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			// Inflate the layout for this fragment
			return inflater.inflate(R.layout.activity_route, container, false);
		}
	}
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route);
		
		// Adjust the action bar for this activity
		setupActionBar();
	}
	
	//TODO: Adjust the action bar for this activity
	private void setupActionBar()
	{
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
	}
	
	//TODO: Go to the previous fragment, or if this is the home, hide
	@Override public void onBackPressed()
	{
		
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}