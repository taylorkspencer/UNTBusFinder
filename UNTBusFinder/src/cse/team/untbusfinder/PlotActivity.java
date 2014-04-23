package cse.team.untbusfinder;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;

public class PlotActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plot);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.plot, menu);
		return true;
	}
	
	ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
	
	public void onToggleClicked(View view) {
	    boolean on = ((ToggleButton) view).isChecked();
	    
	    if (on) {
	       
	    } 
	    else {
	       
	    }
	}

}
