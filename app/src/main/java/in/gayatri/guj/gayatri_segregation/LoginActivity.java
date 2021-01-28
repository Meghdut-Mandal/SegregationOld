package in.gayatri.guj.gayatri_segregation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    EditText userName;
    EditText userPsw;
    EditText ipAddress;
    EditText barPrefix;
    CheckBox chkVibrate;
    CheckBox chkSound;
    CheckBox chkScreen;

    Button btnLogin;
    RequestQueue requestQueue;
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String UserID = "";
    public static final String IpAdd = "";

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        btnLogin = (Button)findViewById(R.id.btnlogin);
        userName = (EditText)findViewById(R.id.username);
        userPsw = (EditText)findViewById(R.id.userpsw);
        ipAddress = (EditText)findViewById(R.id.Ipaddress);
        barPrefix = (EditText)findViewById(R.id.barcodeprefix);
        chkScreen = (CheckBox)findViewById(R.id.chkscreen);
        chkSound = (CheckBox)findViewById(R.id.chksound);
        chkVibrate = (CheckBox)findViewById(R.id.chkvibrate);

        String totalstring = sharedpreferences.getString(UserID,"^");
        try{
            String tipadd[] = totalstring.split(Pattern.quote("^"));
            if(!tipadd[1].equals("")){
                ipAddress.setText(tipadd[1]);
            }
            if(!tipadd[3].equals("")){
                barPrefix.setText(tipadd[3]);
            }
        }catch(Exception e){
            ipAddress.setText("");
        }


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = userName.getText().toString();
                String psw = userPsw.getText().toString();
                if(ipAddress.getText().toString().equals("")){
                    Toast.makeText(getBaseContext(), "IP Address cannot be left blank", Toast.LENGTH_LONG).show();
                }else {
                    volleyrequest(user, psw);
                }
            }
        });

    }

    public void volleyrequest(String userid, String psw){
        JSONObject json = new JSONObject();
        try {
            json.put("userid", userid);
            json.put("password", psw);
        }catch(JSONException e){
            e.printStackTrace();
        }
        //String url = "http://192.168.43.111:8081/gayatrisegregation/login.php";
        //String url = "http://192.168.0.243:6655/login.aspx?userid=" + userid + "&password=" + psw;
        String url = "http://" + ipAddress.getText().toString() + ":6655/login.aspx?userid=" + userid + "&password=" + psw;
        //String url = "http://192.168.43.111:6655/login.aspx?userid=" + userid + "&password=" + psw;
        //String url = "http://192.168.1.141:6655/login.aspx?userid=" + userid + "&password=" + psw;
        requestQueue = Volley.newRequestQueue(getApplicationContext());


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, json,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.toString());
                            if (jsonObject.getString("result").equals("Success")) {
                                Toast.makeText(getBaseContext(), jsonObject.getString("reason") , Toast.LENGTH_LONG).show();
                                String FinalText = jsonObject.getString("mobile") + "^" + ipAddress.getText().toString() + "^" + jsonObject.getString("color") + "^" + barPrefix.getText().toString();
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString(UserID, FinalText);
                                editor.commit();
                                //Toast.makeText(getBaseContext(), FinalText , Toast.LENGTH_LONG).show();

                                Intent i = new Intent(getBaseContext(), ActiveActivity.class);
                                        //attach the key value pair using putExtra to this intent
                                        String fullname = jsonObject.getString("reason");
                                        i.putExtra("FULLNAME", fullname);
                                        if(chkVibrate.isChecked()){
                                            i.putExtra("VIBRATE","Yes");
                                        }else{
                                            i.putExtra("VIBRATE","No");
                                        }
                                        if(chkSound.isChecked()){
                                            i.putExtra("SOUND","Yes");
                                        }else{
                                            i.putExtra("SOUND","No");
                                        }
                                        if(chkScreen.isChecked()){
                                            i.putExtra("SCREEN","Yes");
                                        }else{
                                            i.putExtra("SCREEN","No");
                                        }
                                startActivity(i);
                            } else {
                                Toast.makeText(getBaseContext(), "User ID and Password not Match", Toast.LENGTH_LONG).show();
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
