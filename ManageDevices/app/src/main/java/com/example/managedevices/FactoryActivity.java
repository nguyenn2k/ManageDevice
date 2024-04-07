package com.example.managedevices;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.managedevices.Database.ConnectToDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class FactoryActivity extends AppCompatActivity {
    //Tạo biến để kết nối
    Connection connection;
    //Khai báo biến ArrayList Cho danh sách Tên Factory:
    private ArrayList<String> listFactory;
    //Khai báo Array Adapter:
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factory);

        //Ánh xạ đến activity_factory.xml:
        EditText editNameFactory = findViewById(R.id.edtNameFactory);
        Button buttonAddFactory = findViewById(R.id.btnAddFactory);
        ListView listViewFactory = findViewById(R.id.lvFactory);

        //Tạo mảng Factory mới:
        listFactory = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listFactory);
        listViewFactory.setAdapter(adapter);

        //LoadData liên quan đến Tên Nhà Máy khi FactoryActivity được khởi tạo:
        loadDataFactoryFromDatabase();

        //Tạo hành động cho nút "Thêm Nhà Máy"
        buttonAddFactory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Tạo một biến item khi nhập vào ô Tên Nhà Máy để thêm dưới ListView lvFactory:
                String item = editNameFactory.getText().toString();
                //Tạo Kết nối:
                connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
                try {
                    //Nếu kết nối ổn & chỗ Nhập TÊN NHÀ MÁY để THÊM KHÔNG NULL:
                    if (connection != null && item != null) {
                        //Tạo ra đối tượng Statement để kết nối vs CSDL:
                        Statement statement = connection.createStatement();
                        //Tạo câu truy vấn để NHẬP "TÊN NHÀ MÁY" (NAME_FACTORY) từ Bảng [FACTORY]:
                        String query = "INSERT INTO FACTORY (NAME_FACTORY) VALUES ('" + item + "')";
                        //Vận hành câu lệnh SQL:
                        int rowsAffected = statement.executeUpdate(query);
                        if (rowsAffected > 0) {
                            reloadData();
                            Toast.makeText(getApplicationContext(), "Nhập thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Nhập không thành công", Toast.LENGTH_SHORT).show();
                        }
                        connection.close();
                    } else {
                        Toast.makeText(getApplicationContext(), "Kết nối không thành công", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    Log.e("Error : ", ex.getMessage());
                }
            }
            //Khi NHẬP "TÊN NHÀ MÁY" (NAME_FACTORY) xong thì CẦN LOAD LẠI DATA lên LISTVIEW:
            private void reloadData() {
                listFactory.clear();
                connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
                if (connection != null) {
                    try {
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT NAME_FACTORY FROM FACTORY");
                        while (resultSet.next()) {
                            String factoryName = resultSet.getString("NAME_FACTORY");
                            listFactory.add(factoryName);
                        }
                        connection.close();
                        adapter.notifyDataSetChanged();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Lỗi khi tải dữ liệu từ cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Kết nối không thành công", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //NHẤN GIỮ XÓA MỖI ITEM trong LISTVIEW FACTORY (lvFactory):
        listViewFactory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                //Khi nhấn Giữ Item để XÓA:
                AlertDialog.Builder builder = new AlertDialog.Builder(FactoryActivity.this);
                //Hộp thoại dialog sẽ xuất hiện:
                builder.setMessage("Bạn có muốn xóa nhà máy này không?");
                //Nếu chọn "Có"
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(position);
                    }
                });
                //Nếu chọn "Không":
                builder.setNegativeButton("Không", null);
                builder.show();
                return true;
            }
            //Nếu chọn "Có" để xóa Item:
            private void deleteItem(int position) {
                String factoryName = listFactory.get(position);
                connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
                if (connection != null) {
                    try {
                        Statement statement = connection.createStatement();
                        String query = "DELETE FROM FACTORY WHERE NAME_FACTORY = '" + factoryName + "'";
                        int rowsAffected = statement.executeUpdate(query);
                        if (rowsAffected > 0) {
                            listFactory.remove(position);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), "Xóa thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Xóa không thành công", Toast.LENGTH_SHORT).show();
                        }
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Lỗi khi xóa từ cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Kết nối không thành công", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Khi nhấn vào mỗi Item TÊN NHÀ MÁY trong ListViewFactory (lvFactory), sẽ đến màn hình gồm các Khu Vực & Thiết Bị thuộc Nhà Máy đó:
        listViewFactory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedFactory = listFactory.get(i);
                Intent intent = new Intent(FactoryActivity.this, PlaceFactory.class);
                //Truyền dữ liệu factory_name qua "PlaceFactoryActivity":
                intent.putExtra("factory_name", selectedFactory);
                startActivity(intent);
            }
        });
    }
    //Load Data lên ListView Factory (lvFactory) khi được khởi tạo:
    private void loadDataFactoryFromDatabase() {
        connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
        if (connection != null) {
            try {
                //Tạo ra đối tượng Statement để kết nối vs CSDL:
                Statement statement = connection.createStatement();
                //Thực hiện câu Truy Vấn để lấy toàn bộ NAME_FACTORY trong bảng FACTORY:
                ResultSet resultSet = statement.executeQuery("SELECT NAME_FACTORY FROM FACTORY");
                while (resultSet.next()) {
                    String factoryName = resultSet.getString("NAME_FACTORY");
                    listFactory.add(factoryName);
                }
                connection.close();
                adapter.notifyDataSetChanged();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Lỗi khi tải dữ liệu từ cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Kết nối không thành công", Toast.LENGTH_SHORT).show();
        }
    }
    //Kết nối đến CSDL SQL Server:
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