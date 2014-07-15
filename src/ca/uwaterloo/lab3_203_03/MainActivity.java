package ca.uwaterloo.lab3_203_03;

import java.io.File;

import mapper.MapLoader;
import mapper.MapView;
import mapper.NavigationalMap;
import android.app.Activity;
import android.app.Fragment;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	static MapView mapView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       
        //Initializing mapView with the dimensions of the map
  		mapView = new MapView(getApplicationContext(), 800, 600 , 45, 45);
  		
  		//Registering the Context Menu for mapView
  		registerForContextMenu(mapView);
        
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
    }
    
    //Creating the Context Menu for mapView
  	@Override
  	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
  		super.onCreateContextMenu(menu, v, menuInfo);
  		mapView.onCreateContextMenu(menu, v, menuInfo);
  	}
  	
  	//Adding an item to the Context Menu
  	@Override
  	public boolean onContextItemSelected(MenuItem item){
  		return super.onContextItemSelected(item) || mapView.onContextItemSelected(item);
  	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            
            //Linear Layout
			LinearLayout vw = (LinearLayout) rootView.findViewById(R.id.relayout);
			vw.setOrientation(LinearLayout.VERTICAL);
			
			 //MapView
	         //Initializing the variable dir with the directory pointing to the .SVG file
			final File dir = new File("/sdcard/Android/data/ca.uwaterloo.Lab3_203_03/files");
				 
			//Loading the map from the directory and adding it to the mainlayout
			NavigationalMap map = MapLoader.loadMap( dir, "Lab-room-peninsula.svg");
			mapView.setMap(map);
			vw.addView(mapView);
			
			//Setting up TextView
			GeneralSensorEventListener.valuesTextView = new TextView(rootView.getContext());
			vw.addView(GeneralSensorEventListener.valuesTextView);
			GeneralSensorEventListener.orientation = new TextView(rootView.getContext());
			vw.addView(GeneralSensorEventListener.orientation);
			
			Button resetMax = new Button(rootView.getContext());
			resetMax.setText("RESET");
			vw.addView(resetMax);

			SensorManager sensorManager = (SensorManager)rootView.getContext().getSystemService(SENSOR_SERVICE);
			
			//Setting up all the different sensors
			Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			Sensor accelGravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			
			//Outputting values for accelerometer
			final GeneralSensorEventListener accelerometerListener = new GeneralSensorEventListener(rootView.getContext(),"LinAccel", vw);
			sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
			
			//Outputting values for magnetic field
			final GeneralSensorEventListener magneticListener = new GeneralSensorEventListener(rootView.getContext(),"Magnetic", vw);
			sensorManager.registerListener(magneticListener, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST);
			
			//Outputting values for regular acceleration
			final GeneralSensorEventListener accelGravityListener = new GeneralSensorEventListener(rootView.getContext(), "GravAccel", vw);
			sensorManager.registerListener(accelGravityListener, accelGravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
			
			resetMax.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					accelerometerListener.Reset();
				}		
			});
            return rootView;
        }
    }
}
