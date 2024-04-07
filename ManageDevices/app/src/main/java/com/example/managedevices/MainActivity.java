package com.example.managedevices;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.example.managedevices.Database.ConnectToDatabase;

public class MainActivity extends AppCompatActivity {
    Connection connection;
    private EditText editUserID, editTextPassword;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ánh xạ đến activity_main.xml (layout):
        editUserID = findViewById(R.id.editUserID);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MainActivity.checkLogin().execute("");
            }
        });
    }

    public class checkLogin extends AsyncTask<String, String, String> {
        String z = null;
        Boolean isSuccess = false;
        @Override
        protected void onPreExecute() {}
        @Override
        protected void onPostExecute(String s) {}
        @Override
        protected String doInBackground(String... strings) {
            //Tạo kết nối:
            connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
            if(connection == null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"Kiểm tra đường truyền Intternet",Toast.LENGTH_LONG).show();
                    }
                });
                z = "Đã kết nối";
            }
            else {
                try {
                    /*
                     * Trong bảng LOGIN (USERID  & USERPASSWORD) của DB:
                     * USERID = '" + editUserID.getText() -> USERID tương ứng vs chỗ nhập USERID;
                     * + "' AND USERPASSWORD = '" + editTextPassword.getText() + "' "; -> USERPASSWORD tương ứng vs chỗ nhập Password;
                     * */
                    String sql = "SELECT * FROM LOGIN WHERE USERID = '" + editUserID.getText() + "' AND USERPASSWORD = '" + editTextPassword.getText() + "' ";
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);

                    if (rs.next()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Đăng nhập thành công", Toast.LENGTH_LONG).show();
                            }
                        });
                        z = "Success";
                        //Nếu như đăng nhập thành công => Chuyển vào màn hình chính (HomeActivity):
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (Exception e) {
                    isSuccess = false;
                    Log.e("SQL Error : ", e.getMessage());
                }
            }
            return z;
        }
    }
    @SuppressLint("NewApi")
    public Connection connectionToDatabase(String user, String password, String database, String server){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String connectionURL = null;
        try{
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            connectionURL = "jdbc:jtds:sqlserver://" + server+"/" + database + ";user=" + user + ";password=" + password + ";";
            connection = DriverManager.getConnection(connectionURL);
        }catch (Exception e){
            Log.e("SQL Connection Error : ", e.getMessage());
        }
        return connection;
    }
}
