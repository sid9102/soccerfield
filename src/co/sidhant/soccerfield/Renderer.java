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
import android.view.animation.AccelerateDecelerateInterpolator;
import co.sidhant.soccerfield.R;

public class Renderer extends RajawaliRenderer implements SensorEventListener{
	private BaseObject3D field;
	private final SensorManager mSensorManager;
	private final Sensor mAccelerometer;
	
	public Renderer(Context context) {
		super(context);
		mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	public void initScene() {
		ALight light = new DirectionalLight();
		light.setPower(2);
		light.setPosition(3, 0, 0);
		mCamera.setPosition(3, -0.17f, 0);
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
		
		Sphere ball = new Sphere(0.05f, 16, 16);
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
	}

	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);
//		field.setRotY(field.getRotY() + 0.1f); 
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
	}
}
