package ca.uwaterloo.lab3_203_03;

import java.util.Arrays;

import ca.uwaterloo.lab3_203_03.LineGraphView;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GeneralSensorEventListener implements SensorEventListener{
	public static TextView valuesTextView;			// General Text View
	public static TextView orientation;
	//Max Values
	private float mNumOfAverage = 0;
	private float mAverageMax = 0;
	private float mCurrentMax = 0;
	private float x;
	private float y;
	private float z;
	private float maxAmp = (float)1.0;
	public static int stepCounter;
	// values for the FSM
	private final int mRest = 0;
	private final int mRising = 1;
	private final int mPeak = 2;
	private final int mFalling = 3;
	private final int mNegative = 4;
	//current state instantiated at rest
	private int currentState = 0;
	public static String ACCEL = "Acceleration";
	public static String value = null;
	private static float[] gravity = new float[3];
	private static float[] smoothGravity = new float[3];
	private static float[] geomagnetic = new float[3];
	private static float[] smoothGeomagnetic = new float[3];
	private float[] I;
	private float[] R;
	public static float[] valuesOrientation = new float[3];
	private float pastStep = 1;
	private static double NSHeading;
	private static double EWHeading;
	private final double STEP_DISTANCE = 1;
	public static LineGraphView graph;
	
	// Constructor calls addTextView and initializes string sensortype. 
	public GeneralSensorEventListener(Context context, String sensorType, LinearLayout layout){
		if (sensorType == "LinAccel"){
			graph = new LineGraphView(context,
					100,
					Arrays.asList("x", "y", "z"));
					layout.addView(graph);
		}
		stepCounter = 0;
	}
	
	// Creating a text view for the listener. 
	public void Reset(){
		stepCounter = 0;
		mNumOfAverage = 0;
		mAverageMax = 0;
		maxAmp = (float)1.0;
		EWHeading = 0;
		NSHeading = 0;
		pastStep = 1;
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent se) {
		
		//Accelerometer
		if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
			
			x += (se.values[0] - x)/5;
			y += (se.values[1] - y)/5;
			z += (se.values[2] - z)/5;
			
			// Storing smoothed values into array
			float[] values = new float[]{x, y, z};
						
			//adding points to graph
			graph.addPoint(values);
			
			value = String.format("\nX:%.4f "
					+ "\nY:%.4f"
					+ "\nZ:%.4f \n", x, y, z);
			
			//Switch statement to implement a FSM
			switch(currentState){
				//First state checking if the phone is a rest state eg. standing still
				case mRest:
					//If z value is 10% of the maxAmp a step is occurring
					if (z >= (0.1 * maxAmp)){
						currentState = 1;
					}
					else currentState = 0;
					break;
					
				//Second state checking if the user has begun taking a step
				case mRising:
					//If the z value is at least 70% of the maxAmp then the step is approaching its peak
					if (z >= (0.7 * maxAmp)){
						if (y < 0.5 && x < 0.5){
							currentState = 0;
						}
						else currentState = 2;
					}
					else if(z <= (0.1 * maxAmp)){
						currentState = 0;
					}
					break;
					
				//Third state checking if the  accelerometer has reached a max peak of a step
				case mPeak:
					//Setting the new max peak if the newest peak is higher
					if (z > mCurrentMax){
						mCurrentMax = z;
					}
					//If the z value is now 70% of the maxAmp then the peak has already occurred
					if (z <= (0.7 * maxAmp)){
						currentState = 3;
					}
					
					break;
					
				//Fourth state checking if the step is nearing completion 
				case mFalling:
					//If the z value is less than 10% of the max peak then the falling state is occurring
					if (z <= (maxAmp * 0.1)){
						currentState = 4;
					}
					break;//Exiting Case
					
				//Fifth state checking if the step is completed
				case mNegative:
					//If the z value is less than -0.5 then the step has been completed and a step is counted
					if (z <=-0.5){
						mNumOfAverage++;
						mAverageMax = (mAverageMax + mCurrentMax)/mNumOfAverage ;
						mCurrentMax = 0;
						stepCounter++;
			
						//Setting the new maxAmp every 5 steps
						if(mNumOfAverage == 5){
							if (mAverageMax > 0.7){
								maxAmp = mAverageMax;
								mAverageMax = 0;
								mNumOfAverage = 0;
							}
							else{
								mAverageMax = 0;
								mNumOfAverage = 0;
							}
				
						}
						//Reseting the state to 0 once a full step is completed
						currentState = 0;
					}
					break;//Exiting Case
			}
			
		}
		
		else if(se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
			for(int i =0; i < 3; i++){
			    geomagnetic[i] = se.values[i];
			    smoothGeomagnetic[i] += (geomagnetic[i] - smoothGeomagnetic[i])/30;
			}
		}
		
		else if(se.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			for(int i =0; i < 3; i++){
			    gravity[i] = se.values[i];
			    smoothGravity[i] += (gravity[i] - smoothGravity[i])/30;
			}
		}
		
		if (gravity != null && geomagnetic != null){
			R = new float[9];
			I = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, smoothGravity, smoothGeomagnetic);
			if (success){
				SensorManager.getOrientation(R, valuesOrientation);
				float azimuth = valuesOrientation[0];
				if (pastStep == stepCounter){
					double headingAzimuth = (double)Math.round(azimuth * 100) / 100;
					NSHeading = NSHeading + (STEP_DISTANCE * Math.cos(headingAzimuth));
					NSHeading = (double)Math.round(NSHeading * 100)/100;
					EWHeading = EWHeading + (STEP_DISTANCE * Math.sin(headingAzimuth));
					EWHeading = (double)Math.round(EWHeading * 100)/100;
					pastStep++;
				}
				
				GeneralSensorEventListener.orientation.setText(String.valueOf(azimuth) + "\nNorth-South Distance:" + String.valueOf(NSHeading)+  "\nEast-West Distance:" + String.valueOf(EWHeading));
				
			} 
		}
		valuesTextView.setText(value + "\n\nSteps: " + stepCounter );
		//Outputting the date values
	} 

}
