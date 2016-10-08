package website.jonreynolds.www.glasstraveler;

import com.google.android.glass.content.Intents;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;


public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;

    public CameraActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.

            mCamera.startPreview();


        }
        else {
            mCamera = getCameraInstance();
            mCamera.startPreview();
        }
        //mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        //mCardScroller.deactivate();

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
        super.onPause();
    }

    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance

            Camera.Parameters parameters = c.getParameters();
            List<Camera.Size> localSizes = c.getParameters().getSupportedPreviewSizes();
            Camera.Size mPreviewSize = localSizes.get(0);
            Log.d(TAG, String.valueOf(mPreviewSize));
            //parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            parameters.setPreviewFpsRange(30000, 30000);
            parameters.setPreviewSize(640, 360);
            c.setParameters(parameters);
        }
        catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}

