package co.sidhant.soccerfield;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import rajawali.BaseObject3D;
import rajawali.animation.Animation3D;
import rajawali.animation.RotateAnimation3D;
import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.materials.AMaterial;
import rajawali.materials.DiffuseMaterial;
import rajawali.materials.SimpleMaterial;
import rajawali.materials.TextureInfo;
import rajawali.math.Number3D;
import rajawali.parser.AParser.ParsingException;
import rajawali.parser.ObjParser;
import rajawali.primitives.Cube;
import rajawali.primitives.Plane;
import rajawali.primitives.Sphere;
import rajawali.renderer.RajawaliRenderer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.VelocityTracker;
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
	private VelocityTracker velocity;
	private boolean landscape;
	private boolean scoring;
	private boolean scoreChanged;
	private int homeScore;
	private int awayScore;
	private UserPrefs mUserPrefs;
	private Canvas scoreC;
	private String scoreStr;
	private Bitmap scoreBitmap;
	private Plane scorePlane;
	private TextureInfo scoreTexture;
	private int maxScore;
	private BaseObject3D entireView;
	private boolean leftLandscape;
	
	public Renderer(Context context) {
		super(context);
		mSensorManager = (SensorManager) context.getSystemService("sensor");
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mUserPrefs = UserPrefs.getInstance(context.getApplicationContext());
	}

	public void initScene() {
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		ALight light = new DirectionalLight();
		light.setPower(5);
		light.setPosition(3, 0, 0);
		mCamera.setPosition(3, -0.19f, 0);
		mCamera.setLookAt(0, 0, 0);
		
		homeScore = 0;
		awayScore = 0;
		
		scoring = mUserPrefs.getScoring();
		maxScore = mUserPrefs.getMaxScore();
		Log.v("maxScore", Integer.toString(maxScore));
		scoreChanged = true;
		
		TextureInfo fieldTex = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sf_texture));
		SimpleMaterial fieldMat = new SimpleMaterial(AMaterial.ALPHA_MASKING);
		
		ObjParser fieldParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.field);
		try {
			fieldParser.parse();
			field = fieldParser.getParsedObject();
			field.setMaterial(fieldMat);
			field.addTexture(fieldTex); 
		} catch (ParsingException e) {
			e.printStackTrace();
		}
		
		BaseObject3D goal;
		
		ball = new Sphere(0.05f, 16, 16);
		DiffuseMaterial ballMat = new DiffuseMaterial();
		ball.setMaterial(ballMat);
		ball.addTexture(mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ball)));
		ball.addLight(light);
		ball.setX(0.2f);
		
		
		scorePlane = new Plane(0.4f, 0.2f, 1, 1);
		SimpleMaterial scoreMat = new SimpleMaterial(AMaterial.ALPHA_MASKING);
		scorePlane.setMaterial(scoreMat);
		scorePlane.setPosition(0.1f, 0.1f, 0);
		generateScoreTexture();
		scoreTexture = mTextureManager.addTexture(scoreBitmap);
		scorePlane.addTexture(scoreTexture);
		scorePlane.setRotY(270);
		
		ObjParser goalParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.goal);
		try {
			goalParser.parse();
			goal = goalParser.getParsedObject();
			goal.setMaterial(fieldMat);
			goal.addTexture(fieldTex);
			field.addChild(goal);
		} catch (ParsingException e) {
			e.printStackTrace();
		}
		
		entireView = new BaseObject3D();
		// Use this object to rotate the entire screen if the surface is in landscape
		entireView.addChild(ball);
		entireView.addChild(field);
		entireView.addChild(scorePlane);
		addChild(entireView);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		super.onSurfaceCreated(gl, config);
		xSpeed = 0;
		ySpeed = 0;
	}
	
	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{
		if(width > height)
		{
			landscape = true;
			super.onSurfaceChanged(gl, width, height);
		}
		else
		{
			landscape = false;
			super.onSurfaceChanged(gl, width, height);
		}
		// Wallpaper is designed to display perfectly on a 1280 x 720 screen, so scale objects accordingly.
		float scaleY = (float) height / 1280.0f;
		float scaleZ = (float) width / 720.0f;
		if(landscape)
		{
			scaleY = (float) width / 1280.0f;
			scaleY += 0.2f;
			scaleZ =  (float) height / 720.0f;
			scaleZ += 0.07f;
			if(accX > 0)
			{
				leftLandscape = true;
				entireView.setRotX(-90);
			}
			else
			{
				leftLandscape = false;
				entireView.setRotX(90);
			}
			mCamera.setPosition(2, 0.06f, 0);
			mCamera.setLookAt(0, 0.06f, 0);
		}
		else
		{
			entireView.setRotX(0);
			mCamera.setPosition(3, -0.19f, 0);
			mCamera.setLookAt(0, 0, 0);
		}
		field.setScaleY(scaleY);
		field.setScaleZ(scaleZ);
		ball.setScale(scaleY);
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
		
		ball.setRotY(ball.getRotY() + speed);
		ball.setRotX(result);
		
		float yBound;
		float zBound;
		
		if(landscape)
		{
			yBound = (float) mViewportWidth / 1280.0f;
			yBound += 0.2f;
			zBound = (float) mViewportHeight / 720.0f;
			zBound += 0.07f;
		}
		else
		{
			yBound = (float) mViewportHeight / 1280.0f;
			zBound = (float) mViewportWidth / 720.0f;
		}
		
		yBound *= 1.1f;
		float goalBound = 0.2f * zBound;
		zBound *= 0.65f;
		
		
		//move the ball
		
		if(ball.getZ() < zBound && ball.getZ() > -zBound)
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
			if(ball.getZ() < 0)
			{
				xSpeed = 0.005f;
			}
			else
				xSpeed = -0.005f;
			
			ball.setZ(ball.getZ() + xSpeed);
		}
		
		if(ball.getY() < yBound && ball.getY() > -yBound)
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
			if(ball.getY() < 0)
			{
				if(ball.getZ() < goalBound && ball.getZ() > -goalBound && ySpeed > 0)
				{
					awayScore++;
					scoreChanged = true;
				}
				ySpeed = -0.01f; 
			}
			else
			{
				if(ball.getZ() < goalBound && ball.getZ() > -goalBound && ySpeed < 0)
				{
					homeScore++;
					scoreChanged = true;
				}
				ySpeed = 0.01f;
			}
			
			ball.setY(ball.getY() - ySpeed);
		}
		

		if(scoreChanged)
		{
			scoreChanged = false;
			if(homeScore == maxScore || awayScore == maxScore)
			{
				homeScore = 0;
				awayScore = 0;
				ball.setY(0);
				ball.setZ(0);
				xSpeed = 0;
				ySpeed = 0;
			}
			generateScoreTexture();
			mTextureManager.updateTexture(scoreTexture, scoreBitmap);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}
	
	@Override
	public void onTouchEvent(MotionEvent event)
	{
		int action = event.getAction();
		if(velocity == null)
		{
			velocity = VelocityTracker.obtain();
		}

		float xVelocity;
		float yVelocity;
		if(action == MotionEvent.ACTION_MOVE)
		{
			velocity.addMovement(event);
			velocity.computeCurrentVelocity(1);
			if(!landscape)
			{
				xVelocity = velocity.getXVelocity();
				yVelocity = velocity.getYVelocity();
			}
			else
			{
				if(!leftLandscape)
				{
					xVelocity = velocity.getYVelocity();
					yVelocity = -velocity.getXVelocity();
				}
				else
				{
					xVelocity = -velocity.getYVelocity();
					yVelocity = velocity.getXVelocity();
				}
			}
		}
		else if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_UP)
		{
			velocity.computeCurrentVelocity(1);
			if(!landscape)
			{
				xVelocity = velocity.getXVelocity();
				yVelocity = velocity.getYVelocity();
			}
			else
			{
				if(!leftLandscape)
				{
					xVelocity = velocity.getYVelocity();
					yVelocity = -velocity.getXVelocity();
				}
				else
				{
					xVelocity = -velocity.getYVelocity();
					yVelocity = velocity.getXVelocity();
				}
			}
			velocity.recycle();
			velocity = null;
		}
		else
		{
			velocity.computeCurrentVelocity(1);
			if(!landscape)
			{
				xVelocity = velocity.getXVelocity();
				yVelocity = velocity.getYVelocity();
			}
			else
			{
				if(!leftLandscape)
				{
					xVelocity = velocity.getYVelocity();
					yVelocity = -velocity.getXVelocity();
				}
				else
				{
					xVelocity = -velocity.getYVelocity();
					yVelocity = velocity.getXVelocity();
				}
			}
		}
		
		yVelocity /= 400;
		xVelocity /= 400;
		
		xSpeed = -xVelocity;
		ySpeed = yVelocity;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if(!(event.values[1] > 10 || event.values[1] < -10 || event.values[0] < -10 || event.values[0] > 10))
            {
            	accY =  Math.round(event.values[1]);
            	accX =  Math.round(event.values[0]);
            }
		}
	}
	
	@Override
	public void onVisibilityChanged(boolean visible)
	{
		super.onVisibilityChanged(visible);
		if((scoring != mUserPrefs.getScoring() || maxScore != mUserPrefs.getMaxScore()))
		{
			homeScore = 0;
			awayScore = 0;
		}
		scoring = mUserPrefs.getScoring();
		scoreChanged = true;
		maxScore = mUserPrefs.getMaxScore();
		
		if(!visible)
		{
			mSensorManager.unregisterListener(this);
		}
		else
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private void generateScoreTexture()
	{
		scoreBitmap = Bitmap.createBitmap(128, 64, Bitmap.Config.ARGB_8888);
		scoreC = new Canvas(scoreBitmap);
		Paint scorePaint = new Paint();
		scorePaint.setColor(Color.WHITE);
		scorePaint.setTextSize(40);
		scorePaint.setAntiAlias(true);
		scorePaint.setAlpha(220);
		scoreC.drawColor(Color.TRANSPARENT);
		if(scoring)
		{
			String homeString = String.format("%2d", homeScore);
			if(homeScore < 10)
			{
				homeString += " ";
			}
			String awayString = String.format("%2d", awayScore);
			scoreStr = (homeString + "-" + awayString);
		}
		else
		{
			scoreStr = " ";
		}
		scoreC.drawText(scoreStr, 10, 50, scorePaint);
	}
}
