package com.turing.sample.ai.book;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;


import androidx.annotation.StringDef;


import com.turing.os.util.SPUtils;
import com.turing.sample.R;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * The Camcorder activity.
 */
public class CameraVideo implements SurfaceHolder.Callback, Camera.PreviewCallback{
	private static final String TAG = "CameraVideo";
	public static boolean CAMERA_IS_OPEN = false;
	private SurfaceHolder mSurfaceHolder = null;
	private SurfaceView mSurfaceView;
	/* device w&h */
	private int viewWidth;
	private int viewHeight;

	/**
	 * 翻页检测的库仅支持640*480
	 */
	private int width = 640;
	private int height = 480;
	private int mCameraId = 0;

	protected Camera mCameraDevice;
	private Camera.Parameters mParameters;
	private Context mContext;
	private Activity mActivity;
	private ViewGroup mMainView;

	private boolean mbBackCamera = true;
	public final static String VGA = "VGA";
	public final static String QVGA = "QVGA";
	private Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

	private OnStateCallback stateCallback;
	public interface OnStateCallback{
		void onSuccess();
		void onFailed();
	}

	@StringDef({VGA, QVGA})
	@Retention(RetentionPolicy.SOURCE)
	public @interface VgaType {
	}

	private VideoOnFrameListener videoOnFrameListener;

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if(videoOnFrameListener == null) return;
		videoOnFrameListener.onFrameData(data, data.length);

	}

	public interface VideoOnFrameListener{
		void onFrameData(byte[] videodata, int length);
	}
	public void setOnFrameListener(VideoOnFrameListener listener){
		this.videoOnFrameListener = listener;
	}

	public CameraVideo(Context context, Activity activity, ViewGroup mainView, boolean isOcr, int viewheight, int viewwidth) {
		mActivity = activity;
		mContext = context;
		mMainView = mainView;
//		if(isOcr){
//			width = 640;
//			height = 480;
//		}else{
//			width = 320;
//			height = 240;
//		}

		width = 640;
		height = 480;
		mCameraId = Integer.parseInt(SPUtils.getStringPreference(mContext, mContext.getString(R.string.pre_key_camera_list), "1"));
		this.viewHeight = viewheight;
		this.viewWidth = viewwidth;
		onCreate();
	}

	private int getCameraDisplayOrientation() {
		int rotation = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
			default:
				break;
		}
		int result;
		if (!mbBackCamera) {
			result = (cameraInfo.orientation + degrees) % 360;
			result = (360 - result) % 360;
		} else {
			result = (cameraInfo.orientation - degrees + 360) % 360;
		}
        Log.d(TAG, "displayOrientation: " + result);
		return result;
	}
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void onCreate() {
//		mWindowsWidth = mActivity.getWindowManager().getDefaultDisplay()
//				.getWidth();
//		mWindowsHeight = mActivity.getWindowManager().getDefaultDisplay()
//				.getHeight();
		FrameLayout childView = (FrameLayout) LayoutInflater.from(mContext)
				.inflate(R.layout.preview_frame, null);
		if(viewWidth > 0 && viewHeight > 0){
			childView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth,
					viewHeight));
		}else{
			childView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT));
		}

		mSurfaceView = (SurfaceView) childView
				.findViewById(R.id.camera_preview);
		SurfaceHolder holder = mSurfaceView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
		mMainView.addView(childView);

	}

	public void initCamera() {
		try {
			mCameraDevice = Camera.open(mCameraId);
			Log.e(TAG, " Camera.open(mCameraId);");
			setCameraIPUDirect();
		}catch (Exception e){
			Log.e(TAG, e.toString());
			stateCallback.onFailed();
		}
	}

	private boolean closeCameraOver = true;

	public void setCameraIPUDirect() {
		Log.d(TAG, "set camera_ipu_direct record and restart preview.");
		if (mCameraDevice == null)
			return;
		try {
			int cameraNum = Camera.getNumberOfCameras();
			Log.d(TAG, "openCamera: 摄像头：" + (mCameraId == 1 ? "前置" : "后置") + "|摄像头个数：" + cameraNum);
			if (cameraNum < 2) {
				mCameraId = 0;
			}
			if (mCameraId == 1) {
				mbBackCamera = false;
			} else {
				mbBackCamera = true;
			}
			Camera.getCameraInfo(mCameraId, cameraInfo);
			mCameraDevice.setDisplayOrientation(getCameraDisplayOrientation());

			mParameters = mCameraDevice.getParameters();
			mParameters.setPreviewSize(width, height);
			List<Camera.Size> preSize = mParameters.getSupportedPreviewSizes();
			for(int i = 0; i < preSize.size(); i++){
				Log.i("size", "width" + preSize.get(i).width + "height" + preSize.get(i).height);
			}
			mParameters.setPictureSize(width, height);
			Log.e(TAG, "width: " + width + ", height: " + height);
			mParameters.setPreviewFormat(ImageFormat.NV21);
//			mParameters.setPreviewFrameRate(20);
			if (mbBackCamera) {
				List<String> supportedFocusModes = mParameters.getSupportedFocusModes();
				String mBackFoucsMode = supportedFocusModes.get(0);
				if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
					mBackFoucsMode = Camera.Parameters.FOCUS_MODE_AUTO;
				}
				mParameters.setFocusMode(mBackFoucsMode);
			} else {
				List<String> supportedFocusModes = mParameters.getSupportedFocusModes();
				String mFrontFoucsMode = supportedFocusModes.get(0);
				if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
					mFrontFoucsMode = Camera.Parameters.FOCUS_MODE_FIXED;
				}
				mParameters.setFocusMode(mFrontFoucsMode);
			}
			mCameraDevice.cancelAutoFocus();

			mCameraDevice.setParameters(mParameters);
			mCameraDevice.setPreviewDisplay(mSurfaceHolder);
			mCameraDevice.setPreviewCallback(this);
			mCameraDevice.startPreview();
			Log.e(TAG, " startPreview");
			if(stateCallback != null){
				stateCallback.onSuccess();
			}
		}catch (Exception e){
			e.printStackTrace();
			if(stateCallback != null){
				stateCallback.onFailed();
			}
		}
	}

	private void closeCamera() {
		if (mCameraDevice == null || !closeCameraOver) {
			return;
		}
		try {
			closeCameraOver = false;
			mCameraDevice.stopPreview();
			mCameraDevice.setPreviewDisplay(null);
			mCameraDevice.setPreviewCallback(null);
			mCameraDevice.setErrorCallback(null);
		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				mCameraDevice.release();
				mCameraDevice = null;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				closeCameraOver = true;
				mCameraDevice = null;
				CAMERA_IS_OPEN = false;
				Log.d(TAG, "closeCamera end=");
			}
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Make sure we have a surface in the holder before proceeding.
		if (holder.getSurface() == null) {
			Log.d(TAG, "holder.getSurface() == null");
			return;
		}
		Log.d(TAG, "surfaceChanged. w=" + w + ". h=" + h);
		mSurfaceHolder = holder;
		initCamera();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "===surfaceCreated. w=");
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mSurfaceHolder != null) {
			mSurfaceHolder = null;
			closeCamera();
		}
	}

	public boolean isOpenCamera() {
		if (mCameraDevice != null) {
			CAMERA_IS_OPEN = true;
			return true;
		} else {
			CAMERA_IS_OPEN = false;
		}
		return false;
	}

	private final class JpegPictureCallback implements PictureCallback {

		@Override
		public void onPictureTaken(byte[] jpegData,
				Camera camera) {
			if (jpegData != null && jpegData.length > 0) {
				new savePicThread(jpegData).start();
			}
		}
	}

	public void takeVideoPicture() {
		try {
			mCameraDevice.takePicture(null, null, null,
					new JpegPictureCallback());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class savePicThread extends Thread {
		private byte[] mJpegData;
		private savePicThread(byte[] JpegData) {
			mJpegData = JpegData;
		}
		@Override
		public void run() {
			if (mJpegData != null) {
				if (mJpegData.length > 0) {

				}
			}
		}
	}

	private SaveImgListener saveImgListener;
	private String mDevice;
	public interface SaveImgListener{
		void onSaveSuccess(String filepath, int length);
	}
	public void setSaveImgListener(SaveImgListener listener, String deviceID){
		this.saveImgListener = listener;
		this.mDevice = deviceID;
	}
	public void stopRecord(){
		closeCamera();
	}
}

