package co.sidhant.soccerfield;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import rajawali.BaseObject3D;
import rajawali.animation.Animation3D;
import rajawali.animation.RotateAnimation3D;
import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.SimpleMaterial;
import rajawali.materials.TextureInfo;
import rajawali.math.Number3D;
import rajawali.parser.AParser.ParsingException;
import rajawali.parser.ObjParser;
import rajawali.primitives.Cube;
import rajawali.primitives.Sphere;
import rajawali.renderer.RajawaliRenderer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.animation.AccelerateDecelerateInterpolator;
import co.sidhant.soccerfield.R;

public class Renderer extends RajawaliRenderer implements SensorEventListener{
	private BaseObject3D field;
	private final SensorManager mSensorManager;
	private final Sensor mAccelerometer;
	private float accY;
	private float accX;
	private Sphere ball;
	private float xSpeed;
	private float ySpeed;
	
	public Renderer(Context context) {
		super(context);
		mSensorManager = (SensorManager) context.getSystemService("sensor");
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	public void initScene() {
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		ALight light = new DirectionalLight();
		light.setPower(2);
		light.setPosition(3, 0, 0);
		mCamera.setPosition(3, -0.19f, 0);
		mCamera.setLookAt(0, 0, 0);
		
		TextureInfo fieldTex = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sf_texture));
		SimpleMaterial fieldMat = new SimpleMaterial();
		
		ObjParser fieldParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.field);
		try {
			fieldParser.parse();
			field = fieldParser.getParsedObject();
			field.setMaterial(fieldMat);
			field.addTexture(fieldTex); 
			addChild(field);
		} catch (ParsingException e) {
			e.printStackTrace();
		}
		
		BaseObject3D goal;
		
		ball = new Sphere(0.05f, 16, 16);
		DiffuseMaterial ballMat = new DiffuseMaterial();
		ball.setMaterial(ballMat);
		ball.addTexture(mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ball)));
		ball.addLight(light);
		ball.setX(0.13f);
		
		
		ObjParser goalParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.goal);
		try {
			goalParser.parse();
			goal = goalParser.getParsedObject();
			goal.setMaterial(fieldMat);
			goal.addTexture(fieldTex);
			goal.setTransparent(true);
			field.addChild(ball);
			field.addChild(goal);
		} catch (ParsingException e) {
			e.printStackTrace();
		}
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		super.onSurfaceCreated(gl, config);
		xSpeed = 0;
		ySpeed = 0;
	}
	

	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);
		
		//spin the ball
		float speed = Math.abs(xSpeed) + Math.abs(ySpeed);
		speed *= 700;
		float result = (float) Math.toDegrees(Math.atan2(-ySpeed, xSpeed));
		if(result < 0)
		{
			result += 360; 
		}
		
		
		Log.v("result", Float.toString(result));
		
		ball.setRotY(ball.getRotY() + speed);
		ball.setRotX(result);
		
		//move the ball
		
		if(ball.getZ() < 0.65f && ball.getZ() > -0.65f)
		{
			if(xSpeed < 0.65f && xSpeed > -0.65f)
			{
				xSpeed += accX * (0.005 / 60);
			}
			else if(xSpeed < 0)
			{
				xSpeed = -0.6f;
			}
			else if(xSpeed > 0)
			{
				xSpeed = 0.6f;
			}
			
			ball.setZ(ball.getZ() + xSpeed);
		}
		else
		{
			xSpeed = -0.5f * xSpeed;
			ball.setZ(ball.getZ() + xSpeed);
		}
		
		if(ball.getY() < 1 && ball.getY() > -1)
		{
			if(ySpeed < 0.65f && ySpeed > -0.65f)
			{
				ySpeed += accY * (0.004 / 60);
			}
			else if(ySpeed < 0)
			{
				ySpeed = -0.6f;
			}
			else if(ySpeed > 0)
			{
				ySpeed = 0.6f;
			}
			
			ball.setY(ball.getY() - ySpeed);
		}
		else
		{
			ySpeed = -0.5f * ySpeed;
			ball.setY(ball.getY() - ySpeed);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if(!(event.values[1] > 10 || event.values[1] < -10 || event.values[0] < -10 || event.values[0] > 10))
            {
            	accY =  event.values[1];
            	accX =  event.values[0];
            }
		}
	}
	
	@Override
	public void onVisibilityChanged(boolean visible)
	{
		super.onVisibilityChanged(visible);
		if(!visible)
		{
			mSensorManager.unregisterListener(this);
		}
		else
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
}
