package website.jonreynolds.www.glasstraveler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;


public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    FrameLayout preview;
    ImageView screenshot;
    View translate;
    Bitmap screenshotBitmap;

    private GestureDetector mGestureDetector;
    private boolean translatePic = false;
    private boolean showingPic = false;
    private boolean translateAudio = false;

    private VisionServiceClient client;


    public CameraActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {


        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        Camera.PreviewCallback cb = new Camera.PreviewCallback(){
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if(translatePic && !showingPic){
                    Log.d(TAG, "nick likes honey");
                    screenshotBitmap = Bitmap.createBitmap(640, 360, Bitmap.Config.ARGB_8888);
                    Allocation bmData = renderScriptNV21ToRGBA888(
                            getBaseContext(),
                            640,
                            360,
                            data);
                    bmData.copyTo(screenshotBitmap);
                    screenshot = new ImageView(getBaseContext());
                    screenshot.setImageBitmap(screenshotBitmap);
                    preview.removeView(mPreview);
                    preview.addView(screenshot);
                    showingPic = true;
                    translateText(data);
                }
            }
        };

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera, cb);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        mGestureDetector = createGestureDetector(this);

    }

    public Allocation renderScriptNV21ToRGBA888(Context context, int width, int height, byte[] nv21) {
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(nv21);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        return out;
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP){
                    if(!translatePic) {
                        translatePic = true;
                    }
                    if(showingPic) {
                        endTranslation();
                        showingPic = false;
                        translatePic  = false;
                    }
                    return true;
                } else if (gesture == Gesture.TWO_TAP) {
                    // do something on two finger tap
                    return true;
                } else if (gesture == Gesture.SWIPE_RIGHT) {
                    // do something on right (forward) swipe
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    // do something on left (backwards) swipe
                    return true;
                }
                return false;
            }
        });
        return gestureDetector;
    }

    private void endTranslation() {
        preview.removeView(translate);
        preview.addView(mPreview);

    }

    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
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

    public void translateText(byte[] data) {

        try {
            new doRequest().execute(data);
        } catch (Exception e)
        {
        }
    }
    private String process(byte[] data) throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        screenshotBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());


        OCR ocr;
        ocr = this.client.recognizeText(inputStream, LanguageCodes.AutoDetect, true);

        String result = gson.toJson(ocr);

        return result;
    }

    private String translate(String foreign, String language) throws Exception {
        foreign = foreign.replace('\n', ' ');
        Translate.setClientId("mhacks2016couchsquad");
        Translate.setClientSecret("ZDlol0NO05RJeClPXuam5YY9JRRi6bI3PcXyjF/kpWk=");
        String translated = Translate.execute(foreign, Language.fromString(language), Language.ENGLISH);
        return translated;
    }

    private class doRequest extends AsyncTask<byte[], String , String> {
        // Store error message
        private Exception e = null;

        @Override
        protected String doInBackground(byte[]... data) {
            try {
                return process(data[0]);
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence
            String text = "";
            String language = "unk";
            if (e != null) {
                text+="Error: " + e.getMessage();
                this.e = null;
            } else {
                Gson gson = new Gson();

                OCR r = gson.fromJson(data, OCR.class);

                for (Region reg : r.regions) {
                    for (Line line : reg.lines) {
                        for (Word word : line.words) {
                            text += word.text + " ";
                        }
                        text += "\n";
                    }
                    text += "\n\n";
                }
                language = r.language;
            }
            new translateRequest().execute(text, language);
        }
    }

    private class translateRequest extends AsyncTask<String, String , String> {
        // Store error message
        private Exception e = null;

        @Override
        protected String doInBackground(String... data) {
            try {
                return translate(data[0], data[1]);
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String text) {
            super.onPostExecute(text);
            if(e != null){
                text = e.getMessage();
            }
            if(text == null){
                text = "Error: Failed to detect language.";
            }
            preview.removeView(screenshot);
            CardBuilder cb = new CardBuilder(getBaseContext(), CardBuilder.Layout.CAPTION).setText(text).addImage(screenshotBitmap);
            translate = cb.getView();
            preview.addView(translate);
        }
    }
}

