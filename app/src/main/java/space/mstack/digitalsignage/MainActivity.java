package space.mstack.digitalsignage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.VideoView;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private List<String> vdoList = new ArrayList<String>();
    private SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss:EEEE");
    private Handler mHandler = new Handler();

    private Camera mCamera;
    private SurfaceView mSurfaceViewer;
    private SurfaceHolder mSurfaceHolder;
    private boolean cameraCondition = false;

    private TextView textView_date;
    private TextView textView_days;
    private EditText editText_uid;

    private WebView webView0;
    private WebView webView1;
    private VideoView vdoView1;

    private String urlPath = "";
    private String webPath = "";

    private String studentID = "0000000000";
    private int vdoCount = 0;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);

        mSurfaceViewer = (SurfaceView) findViewById(R.id.surfaceView);

        textView_date = (TextView) findViewById(R.id.textView_date);
        textView_days = (TextView) findViewById(R.id.textView_days);

        editText_uid = (EditText) findViewById(R.id.editText_uid);

        vdoView1 = (VideoView) findViewById(R.id.vdoView1);
        webView0 = (WebView) findViewById(R.id.webView0);
        webView1 = (WebView) findViewById(R.id.webView1);

        getConfigurations();

        vdoView1.setVisibility(View.VISIBLE);
        webView1.setVisibility(View.GONE);

        mSurfaceViewer.setZOrderOnTop(true);

        mSurfaceHolder = mSurfaceViewer.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

        mSurfaceViewer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);

                startActivity(intent);

                return false;
            }
        });

        editText_uid.requestFocus();
        editText_uid.setInputType(InputType.TYPE_NULL);

        editText_uid.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        studentID = editText_uid.getText().toString();

                        if (mCamera != null) {
                            mCamera.takePicture(null, null, mJpegCallback);
                            //mCamera.release();
                        }

                        webView1.clearView();
                        webView1.clearHistory();
                        webView1.loadUrl(urlPath + studentID);
                        webView1.setWebViewClient (new WebViewClient());

                        vdoView1.setVisibility(View.GONE);
                        webView1.setVisibility(View.VISIBLE);
                        vdoView1.stopPlayback();

                        editText_uid.setText("");

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                vdoView1.setVisibility(View.VISIBLE);
                                webView1.setVisibility(View.GONE);

                                webView1.clearView();
                                webView1.clearHistory();
                                webView1.loadUrl("about:blank");
                                webView1.setWebViewClient (new WebViewClient());
                                //webView1.clearCache(true);
                                //webView1.destroy();

                                vdoView1.setZOrderOnTop(true);
                                vdoView1.start();
                            }
                        }, 4000);
                    }

                    return true;
                }

                return false;
            }
        });

        editText_uid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    editText_uid.requestFocus();
                }
            }
        });

        webView0.getSettings().setJavaScriptEnabled(true);
        webView0.getSettings().setLoadWithOverviewMode(true);
        webView0.getSettings().setUseWideViewPort(true);
        webView0.loadUrl(webPath);
        webView0.setWebViewClient(new WebViewClient());

        webView1.getSettings().setJavaScriptEnabled(true);
        webView1.getSettings().setLoadWithOverviewMode(true);
        webView1.getSettings().setUseWideViewPort(true);

        updateVideo();

        if (vdoList.size() > 0) {
            vdoView1.setVideoPath(vdoList.get(0));
            vdoView1.start();
            vdoView1.setZOrderOnTop(true);
        }

        vdoView1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    mp.reset();

                    if (vdoCount == vdoList.size() - 1) {
                        vdoCount = 0;
                    } else {
                        vdoCount++;
                    }

                    vdoView1.setVideoPath(vdoList.get(vdoCount));
                    vdoView1.start();
                    vdoView1.setZOrderOnTop(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Delete old file from local storage
        File dirs = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/school-signage");

        if (dirs.exists()) {
            final int keeptime = 24 * 60 * 60 * 1000;  // Keep only 1 day

            File[] files = dirs.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String name) {
                    if (name.toLowerCase().endsWith(".jpg")) {
                        Date created = new Date(file.lastModified());
                        Date current = new Date();

                        return ((current.getTime() - created.getTime()) > keeptime);
                    } else {
                        return false;
                    }
                }
            });

            for (File file: files) {
                file.delete();
            }
        }

        DownloadVideoTask downloadVideoTask = new DownloadVideoTask();

        try {
            if (downloadVideoTask.execute().get()) {
                updateVideo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();
        String timeString = timeFormat.format(calendar.getTime());
        String[] splitTime = timeString.split(":");

        textView_date.setText(splitTime[0]);
        textView_days.setText(splitTime[4]);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getConfigurations();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (isDeviceSupportCamera()) {
            try {
                mCamera = Camera.open(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (cameraCondition) {
            if (mCamera != null) {
                mCamera.stopPreview();
                cameraCondition = false;
            }
        }

        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();

                mCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();

                cameraCondition = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(MainActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        */
    }

    Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (mCamera != null) {
                try {
                    Camera.Parameters parameters = mCamera.getParameters();

                    mCamera.setParameters(parameters);
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.startPreview();

                    cameraCondition = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat fileFormat = new SimpleDateFormat("yyyyMMddHHmm", Locale.US);
            String timeString = fileFormat.format(calendar.getTime());
            String fileName = studentID + "-" + timeString + ".jpg";

            SaveImageTask saveImageTask = new SaveImageTask(MainActivity.this, fileName, studentID);

            try {
                saveImageTask.execute(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private boolean isDeviceSupportCamera() {
        return (Camera.getNumberOfCameras() > 0);
    }

    private void getConfigurations() {
        SharedPreferences myPrefs = this.getSharedPreferences("SIGNAGE_PREF", Context.MODE_PRIVATE);

        urlPath = myPrefs.getString("_signage_url", "");
        webPath = myPrefs.getString("_signage_web", "");
    }

    private void updateVideo() {
        File dirs = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/school-signage");

        if (dirs.exists()) {
            File[] files = dirs.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".mp4");
                }
            });

            for (File file: files) {
                vdoList.add(file.getAbsolutePath());
            }
        } else {
            dirs.mkdirs();
        }
    }
}
