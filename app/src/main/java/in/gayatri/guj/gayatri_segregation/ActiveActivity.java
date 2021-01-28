package in.gayatri.guj.gayatri_segregation;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

public class ActiveActivity extends AppCompatActivity
{
    SurfaceView surfaceView;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    TextView tvbarcode;
    TextView tvmobile;
    TextView tvcolorcode;
    TextView tvcolornumber;
    ToneGenerator tg;
    RequestQueue requestQueue;
    String currentBarcode;
    String IpAddress;
    String barPrefix;
    String userfullname;
    AlertDialog dialog;
    MediaPlayer ring;

    String varVibrate;
    String varSound;
    String varScreen;

    LinearLayout scrlayout;

//    ObjectAnimator animator;
//    View scannerLayout;
//    View scannerBar;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String UserID = "";
    public static final String IpAdd = "";
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);


        //Scanner overlay
//        scannerLayout = findViewById(R.id.scannerLayout);
//        scannerBar = findViewById(R.id.scannerBar);
//        animator = null;


        varVibrate = getIntent().getStringExtra("VIBRATE");
        varSound = getIntent().getStringExtra("SOUND");
        varScreen = getIntent().getStringExtra("SCREEN");

        //Toast.makeText(getBaseContext(), "Vibrate :" + varVibrate, Toast.LENGTH_LONG).show();


        surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
        tvbarcode = (TextView) findViewById(R.id.barcode);
        tvmobile = (TextView) findViewById(R.id.mobile);
        tvcolorcode = (TextView) findViewById(R.id.colorcode);
        tvcolornumber = (TextView) findViewById(R.id.colornumber);
        scrlayout = (LinearLayout)findViewById(R.id.scrlayout);
        currentBarcode = "";

        //ring= MediaPlayer.create(MainActivity.this,R.raw.scanner);

        if(varScreen.equals("Yes")){
            scrlayout.setRotation(180);
        }

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String totalstring =sharedpreferences.getString(UserID,"");
        String strings[] = totalstring.split(Pattern.quote("^"));
        IpAddress = strings[1];
        try{
            barPrefix = strings[3];
        }catch(Exception e){
            barPrefix = "";
        }


//        ViewTreeObserver vto = scannerLayout.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//
//                scannerLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//                    scannerLayout.getViewTreeObserver().
//                            removeGlobalOnLayoutListener(this);
//
//                } else {
//                    scannerLayout.getViewTreeObserver().
//                            removeOnGlobalLayoutListener(this);
//                }
//
//                float destination = (float)(scannerLayout.getY() +
//                        scannerLayout.getHeight());
//
//                animator = ObjectAnimator.ofFloat(scannerBar, "translationY",
//                        scannerLayout.getY(),
//                        destination);
//
//                animator.setRepeatMode(ValueAnimator.REVERSE);
//                animator.setRepeatCount(ValueAnimator.INFINITE);
//                animator.setInterpolator(new AccelerateDecelerateInterpolator());
//                animator.setDuration(3000);
//                animator.start();
//
//            }
//        });





        userfullname = getIntent().getStringExtra("FULLNAME");
        //Toast.makeText(getBaseContext(), "User Full Name :" + userfullname, Toast.LENGTH_LONG).show();
        setTitle("GS -" + userfullname);

        //tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION,100);
        tg = new ToneGenerator(0,ToneGenerator.MAX_VOLUME);
        //dtmfGenerator.startTone(ToneGenerator.TONE_DTMF_0, 500); // all types of tones are available...
        //dtmfGenerator.stopTone();


        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.CODE_128 | Barcode.ITF).build();

        cameraSource = new CameraSource.Builder(this,barcodeDetector).setRequestedPreviewSize(1600,1024).setAutoFocusEnabled(true).setRequestedFps(15.0f).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    return ;
                }
                try{
                    cameraSource.start(holder);
                }catch (IOException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if(qrCodes.size() != 0) {
                    String qrcode = "";
                    for(int i=0; i<qrCodes.size(); i++){
                        qrcode = qrCodes.valueAt(i).displayValue;
                        if (!barPrefix.equals("")) {
                            //Toast.makeText(getBaseContext(), "Barcode : " + qrcode, Toast.LENGTH_LONG).show();
                            if (qrcode.substring(0, barPrefix.length()).equals(barPrefix)) {
                                break;
                            }
                        }
                    }
                    if (dialog == null || !dialog.isShowing()) {
                        if (!barPrefix.equals("")) {
                            if (qrcode.substring(0, barPrefix.length()).equals(barPrefix)) {
                                if (!qrcode.equals(currentBarcode) && !tvbarcode.getText().equals("Last Scanned :" + qrcode)) {
                                    currentBarcode = qrcode;

                                    String totalstring = sharedpreferences.getString(UserID, "");
                                    String strings[] = totalstring.split(Pattern.quote("^"));
                                    String UID = strings[0];
                                    String UserType = strings[2];
                                    volleyrequest(UID, qrcode, UserType);

//                                    Vibrator vibrate = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
//                                    vibrate.vibrate(500);
//                                    tg.startTone(ToneGenerator.TONE_PROP_BEEP);

                                    tvbarcode.setText("Last Scanned :" + qrcode);
                                } else {
                                    tvbarcode.setText("Last Scanned :" + currentBarcode);
                                }
                            } else {
                                tvmobile.setText("Barcode Prefix not Match");
                            }
                        } else {
                            if (!qrcode.equals(currentBarcode) && !tvbarcode.getText().equals("Last Scanned :" + qrcode)) {
                                currentBarcode = qrcode;

                                String totalstring = sharedpreferences.getString(UserID, "");
                                String strings[] = totalstring.split(Pattern.quote("^"));
                                String UID = strings[0];
                                String UserType = strings[2];
                                volleyrequest(UID, qrcode, UserType);

//                                Vibrator vibrate = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
//                                vibrate.vibrate(500);
//                                tg.startTone(ToneGenerator.TONE_PROP_BEEP);

                                tvbarcode.setText("Last Scanned :" + qrcode);
                            } else {
                                tvbarcode.setText("Last Scanned :" + currentBarcode);
                            }
                        }
                    }

                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
        barcodeDetector.release();
    }

    public void volleyrequest(String userid, String barcode, String usertype){
        JSONObject json = new JSONObject();
        try {
            json.put("userid", userid);
            json.put("barcode", barcode);
            json.put("usertype",usertype);
        }catch(JSONException e){
            e.printStackTrace();
        }
        //String url = "http://192.168.43.111:8081/gayatrisegregation/segregation.php";
        //String url = "http://192.168.43.111:6655/segregation.aspx?userid=" + userid + "&barcode=" + barcode;
        //String url = "http://192.168.0.243:6655/segregation.aspx?userid=" + userid + "&barcode=" + barcode;
        //String url = "http://192.168.1.141:6655/segregation.aspx?userid=" + userid + "&barcode=" + barcode;

        String url = "http://" + IpAddress + ":6655/newsegregation.aspx?userid=" + userid + "&barcode=" + barcode + "&usertype=" + usertype;
        //tvmobile.setText(barcode.toString());
        requestQueue = Volley.newRequestQueue(getApplicationContext());


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, json,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.toString());
                            String result1 = jsonObject.getString("result").toString().trim();

                            if (result1.equals("Success")) {
                                tvmobile.setText(jsonObject.getString("mobile").toString());
                                //tvcolorcode.setText(jsonObject.getString("color").toString());
                                tvcolorcode.setText(jsonObject.getString("reason").toString());
                                String message = jsonObject.getString("message").toString();
                                setTitle("GS -" + userfullname + "-" + jsonObject.getString("cafcount").toString());
                                //change Color
                                String colcode = jsonObject.getString("color").toString().trim();
                                switch (colcode) {
                                    case "RED":
                                        tvcolorcode.setBackgroundColor(Color.RED);
                                        tvcolornumber.setBackgroundColor(Color.RED);
                                        tvcolornumber.setText("4");
                                        break;
                                    case "BLUE":
                                        tvcolorcode.setBackgroundColor(Color.BLUE);
                                        tvcolornumber.setBackgroundColor(Color.BLUE);
                                        tvcolornumber.setText("1");
                                        break;
                                    case "GREEN":
                                        tvcolorcode.setBackgroundColor(Color.GREEN);
                                        tvcolornumber.setBackgroundColor(Color.GREEN);
                                        tvcolornumber.setText("0");
                                        break;
                                    case "YELLOW":
                                        tvcolorcode.setBackgroundColor(Color.YELLOW);
                                        tvcolornumber.setBackgroundColor(Color.YELLOW);
                                        tvcolornumber.setText("2");
                                        break;
                                    case "MAGENTA":
                                        tvcolorcode.setBackgroundColor(Color.MAGENTA);
                                        tvcolornumber.setBackgroundColor(Color.MAGENTA);
                                        tvcolornumber.setText("3");
                                        break;
                                    case "BLACK":
                                        tvcolorcode.setBackgroundColor(Color.BLACK);
                                        tvcolornumber.setBackgroundColor(Color.BLACK);
                                        tvcolornumber.setText("-");
                                        break;
                                    default:        //Toast.makeText(getBaseContext(), "Barcode" + barcode, Toast.LENGTH_LONG).show();

                                        tvcolorcode.setBackgroundColor(Color.WHITE);
                                        tvcolornumber.setBackgroundColor(Color.WHITE);
                                        tvcolornumber.setText("-");
                                        break;
                                }

                                if(varVibrate.equals("Yes")) {
                                    Vibrator vibrate = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                    vibrate.vibrate(500);
                                }

                                if(varSound.equals("Yes")) {
                                    //tg.startTone(ToneGenerator.TONE_PROP_BEEP);
                                    //TONE_CDMA_HIGH_PBX_SS
                                    tg.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_SS,250);
                                }



                                //ring.start();

                                //Toast.makeText(getBaseContext(), jsonObject.getString("reason"), Toast.LENGTH_LONG).show();
                                if(!message.equals("")){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActiveActivity.this);
                                    builder.setCancelable(false);
                                    builder.setMessage(message)
                                            .setTitle("Alert");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // User clicked OK button
                                            dialog.cancel();
                                        }
                                    });
                                    dialog = builder.create();
                                    dialog.show();
                                }
                            } else {
                                Toast.makeText(getBaseContext(), "Could not Save due to " + jsonObject.getString("reason"), Toast.LENGTH_LONG).show();
                            }
                        }catch(JSONException e){
                            Toast.makeText(getBaseContext(), "Error " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //pd.dismiss();
                        //Failure Callback
                        Toast.makeText(getBaseContext(), "Error " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        // Adding the request to the queue along with a unique string tag
        requestQueue.add(jsonObjReq);
    }
}
