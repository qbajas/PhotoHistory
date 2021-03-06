package com.pastexplorer;

import java.io.IOException;

import pastexplorer.util.StackTraceUtil;
import pastexplorer.util.StorageUtil;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class TakePhotoActivity extends Activity implements OnClickListener {	
	private static final String DEBUG_TAG = "PE TakePhoto";
	
	private PowerManager.WakeLock mWakeLock;
	
	private PhotoPreview mPreview;
    Camera mCamera;
    int numberOfCameras;
    int cameraCurrentlyLocked;

    // The first rear facing camera
    int defaultCameraId;
    
    private Button mTakePhotoButton = null;
    private Button mConfirmPhotoButton = null;
    private Button mRetakePhotoButton = null;
    private ImageView mImage = null;
    private Bitmap mTakenPhoto = null;
    
    private boolean mTakingPhoto;
    
    private int mAlbumId;
    private Bitmap mLastPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAlbumId = getIntent().getExtras().getInt("album_id");

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.take_photo);
        mPreview = (PhotoPreview)findViewById(R.id.preview);
        
        mImage = (ImageView)findViewById(R.id.photo);
        
        mTakePhotoButton = (Button)findViewById(R.id.take_photo);
        if (mTakePhotoButton != null) {
        	mTakePhotoButton.setOnClickListener(this);
        }
        mConfirmPhotoButton = (Button)findViewById(R.id.confirm_photo);
        if (mConfirmPhotoButton != null) {
        	mConfirmPhotoButton.setOnClickListener(this);
        }
        mRetakePhotoButton = (Button)findViewById(R.id.retake_photo);
        if (mRetakePhotoButton != null) {
        	mRetakePhotoButton.setOnClickListener(this);
        }

        // Find the total number of cameras available
        numberOfCameras = Camera.getNumberOfCameras();
        Log.d(DEBUG_TAG, "numberOfCameras = " + numberOfCameras);

        // Find the ID of the default camera
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
                Log.d(DEBUG_TAG, "defaultCameraId = " + defaultCameraId);
                break;
            }
        }
        
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        
        mTakingPhoto = true;
        
        // read last photo thumbnail bitmap
        if (getIntent().getExtras().containsKey("last_photo")) {
            try {
				mLastPhoto = StorageUtil.loadBitmapFromPrivateStorage(this, getIntent().getExtras().getString("last_photo"));
			} catch (IOException e) {
				Log.e(DEBUG_TAG, "failed to load last photo bitmap from private storage: " + e);
			}
        }
        else {
        	mLastPhoto = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Open the default i.e. the first rear facing camera.
 		mCamera = null;
 		try {
 	        mCamera = Camera.open();
 		}
 		catch (Exception e) {
 			Log.e(DEBUG_TAG, "failed to acquire camera object");
 			Toast.makeText(this, "Failed to acquire camera", Toast.LENGTH_SHORT);
 		}
 		cameraCurrentlyLocked = defaultCameraId;
        mPreview.setCamera(mCamera);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (mTakingPhoto) { 
        	prepareToTakePhoto();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
		
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        if (mTakingPhoto) {
        	finalizeTakingPhoto();
        }
    }

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.take_photo:
			takePhoto();
			break;
		case R.id.confirm_photo:
			Intent intent = new Intent(this, UploadPhotoActivity.class);
			try {
				intent.putExtra("photo", StorageUtil.saveBitmapToPrivateStorage(this, mTakenPhoto));
			} catch (IOException e) {
				Log.e(DEBUG_TAG, "failed to save photo bitmap to private internal storage: " 
						+ StackTraceUtil.getStackTrace(e));
			}
			intent.putExtra("album_id", mAlbumId);
			startActivity(intent);
			break;
		case R.id.retake_photo:
			prepareToTakePhoto();
			mTakingPhoto = true;
			break;
		}
	};
	
	private void prepareToTakePhoto() {
		mTakePhotoButton.setVisibility(View.VISIBLE);
		mConfirmPhotoButton.setVisibility(View.GONE);
		mRetakePhotoButton.setVisibility(View.GONE);
		
		if (mImage != null) {
			if (mLastPhoto != null) {
				mImage.setImageBitmap(mLastPhoto);
	    		mImage.setAlpha(130);
			}
			else {
				mImage.setImageBitmap(null);
			}
		}

        mWakeLock.acquire();
        
        mCamera.startPreview();
	}
	
	private void finalizeTakingPhoto() {
		mTakePhotoButton.setVisibility(View.GONE);
		mConfirmPhotoButton.setVisibility(View.VISIBLE);
		mRetakePhotoButton.setVisibility(View.VISIBLE);
        
		mWakeLock.release();
	}
	
	private void takePhoto() {
		mCamera.takePicture(null, null, new Camera.PictureCallback() {
			public void onPictureTaken(byte[] data, Camera camera) {
				//new SavePhotoTask().execute(data);
				
				try {
					Log.d(DEBUG_TAG, "Converting captured photo to a bitmap...");
					mTakenPhoto = BitmapFactory.decodeByteArray(data, 0, data.length);
					Log.d(DEBUG_TAG, "Successfully converted");
					mImage.setImageDrawable(new BitmapDrawable(mTakenPhoto));
					mImage.setAlpha(255);
				}
				catch (Exception e) {
					Log.e(DEBUG_TAG, "failed to decode captured photo data into a bitmap: " 
							+ StackTraceUtil.getStackTrace(e));
				}
				
				finalizeTakingPhoto();
				mTakingPhoto = false;
			}
		});	
	}
	

}