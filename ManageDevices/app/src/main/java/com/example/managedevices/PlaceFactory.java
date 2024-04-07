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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.managedevices.Database.ConnectToDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PlaceFactory extends AppCompatActivity {
    //Tạo biến để kết nối
    Connection connection;
    //Khai báo mảng Array List cho LisView của Khu Vực thuộc Nhà Máy:
    private ArrayList<String> listPlace1;
    //Khai báo mảng Array List cho LisView của Thiết Bị thuộc Nhà Máy:
    private ArrayList<String> listDevice1;
    //Khai báo mảng Adapter cho LisView của Khu Vực thuộc Nhà Máy:
    private ArrayAdapter<String> placeAdapter;
    //Khai báo mảng Adapter cho LisView của Thiết Bị thuộc Nhà Máy:
    private ArrayAdapter<String> deviceAdapter;

    //Tạo biến 'factoryName' để truyền:
    private String factoryName;
    private Integer factoryId;

    // Khai báo mã yêu cầu để nhận kết quả từ DeviceInfoActivity
    private static final int REQUEST_DEVICE_INFO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_factory);

        //Nhận dữ liệu "factory_name" từ FactoryActivity:
        factoryName = getIntent().getStringExtra("factory_name");
        //factoryId =  Integer.parseInt(getIntent().getStringExtra("factory_id"));
        //Ánh xạ:
        EditText editTextPlaceFact1 = findViewById(R.id.edtPlaceFact1);
        Button buttonAddPlace1 = findViewById(R.id.btnAddPlace1);
        //Khu vực:
        ListView listViewPlaceFact1 = findViewById(R.id.lvPlaceFact1);

        EditText editTextDeviceFact1 = findViewById(R.id.edtDeviceFact1);
        Button buttonAddDevice1 = findViewById(R.id.btnAddDevice2);
        ListView listViewDeviceFact1 = findViewById(R.id.lvDeviceFact1);

        //Tạo 2 Mảng ArrayList mới cho Khu Vực & Thiết Bị
        listPlace1 = new ArrayList<>();
        listDevice1 = new ArrayList<>();

        //Tạo 2 đối tượng Array Adapter mới (Khu Vực & Thiết Bị):
        placeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listPlace1);
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listDevice1);

        //Thiết lập 2 Adapter đã tạo cho ListView:
        listViewPlaceFact1.setAdapter(placeAdapter);
        listViewDeviceFact1.setAdapter(deviceAdapter);

        // Kiểm tra xem ActionBar có được hỗ trợ không trước khi gán Name_Factory từ Factory:
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Tiến hành các tác vụ tương ứng với mỗi item, ví dụ:
            // Hiển thị tên của item trên ActionBar hoặc tiêu đề của Activity
            actionBar.setTitle(factoryName);
        }

        //Tạo hành động cho nút "Thêm Khu Vực":
        buttonAddPlace1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //lấy nội dung nhập từ EditText và biến thành kiểu String:
                String itemPlaceFact1 = editTextPlaceFact1.getText().toString();
                if (!itemPlaceFact1.isEmpty()) {
                    //Lệnh Nhập "Khu Vực" sẽ được Thực Thi:
                    insertPlace(itemPlaceFact1);
                }
            }
        });
        //Tạo hành động cho nút "Thêm Thiết Bị":
        buttonAddDevice1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //lấy nội dung nhập từ EditText và biến thành kiểu String:
                String itemDeviceFact1 = editTextDeviceFact1.getText().toString();
                if (!itemDeviceFact1.isEmpty()) {
                    //Lệnh Nhập "Thiết Bị" sẽ được Thực Thi:
                    insertDevice(itemDeviceFact1);
                }
            }
        });

        //Nhấn giữ Xóa Item trong ListView Khu Vực:
        listViewPlaceFact1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlaceFactory.this);
                builder.setMessage("Bạn có muốn xóa khu vực này không?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePlace(position);
                    }
                });
                builder.setNegativeButton("Không", null);
                builder.show();
                return true;
            }
        });
        //Nhấn giữ Xóa Item trong ListView Thiết Bị:
        listViewDeviceFact1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlaceFactory.this);
                builder.setMessage("Bạn có muốn xóa thiết bị này không?");
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDevice(position);
                        deleteDeviceInfo (position);
                    }
                });
                builder.setNegativeButton("Không", null);
                builder.show();
                return true;
            }
        });
        //Load Data Khu Vực & Thiết Bị:
        loadPlacesFromDatabase();
        loadDevicesFromDatabase();

        //Khi nhấn vào mỗi item 'Thiết Bị' trong ListView  sẽ ra một màn hình DeviceInfo tương ứng với item 'Thiết Bị' đó :
        listViewDeviceFact1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Tạo biến item DeviceName (item Tên thiết bị) khi nhấn:
                String deviceName = listDevice1.get(i);
                //Chuyển sang màng hình DeviceInfo tương ứng:
                Intent intent = new Intent(PlaceFactory.this, DeviceInfo.class);
                //Truyền tên thiết bị qua Intent
                intent.putExtra("device_name", deviceName); //Tên thiết bị
                intent.putExtra("factory_name", factoryName); //Khu vực
                intent.putExtra("factory_id", factoryId); // Factory_ID
                //Thực hiện việc chuyển đổi:
                startActivity(intent);
            }
        });
        //Khi nhấn vào mỗi area (khu vực) thuộc nhà máy đó sẽ cho ra khu vực & thiết bị thuộc nhà máy đó:
        listViewPlaceFact1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String areaName = listPlace1.get(i);
                Intent intent = new Intent(PlaceFactory.this, PlaceFactory.class);
                //Truyền dữ liệu factory_name qua "PlaceFactoryActivity":
                intent.putExtra("area_name", areaName);
                startActivity(intent);
            }
        });
    }

    //Lệnh Nhập "Khu Vực"
    private void insertPlace(String placeName) {
        connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
        if (connection != null) {
            try {
                /*
                 * Nhập Tên Khu Vực (PLACE_NAME) lấy theo Tên Nhà Máy (NAME_FACTORY)
                 * */
                String query = "INSERT INTO AREA_FACTORY (AREA_NAME, FACTORY_ID) VALUES (?, (SELECT FACTORY_ID FROM FACTORY WHERE NAME_FACTORY = ?))";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, placeName);
                preparedStatement.setString(2, factoryName);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    listPlace1.add(placeName);
                    placeAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Nhập thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Nhập không thành công", Toast.LENGTH_SHORT).show();
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Lỗi khi thêm vào cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Kết nối không thành công", Toast.LENGTH_SHORT).show();
        }
    }
    //Lệnh Nhập "Thiết Bị":
    private void insertDevice(String deviceName) {
        connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
        if (connection != null) {
            try {
                /*
                 * Nhập Tên Thiết Bị (DEVICE_NAME) lấy theo Tên Nhà Máy (NAME_FACTORY):
                 * */
                String query = "INSERT INTO DEVICE_FACTORY (DEVICE_NAME, FACTORY_ID) VALUES (?, (SELECT FACTORY_ID FROM FACTORY WHERE NAME_FACTORY = ?))";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, deviceName);
                preparedStatement.setString(2, factoryName);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    listDevice1.add(deviceName);
                    deviceAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Nhập thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Nhập không thành công", Toast.LENGTH_SHORT).show();
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Lỗi khi thêm vào cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Kết nối không thành công", Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePlace(int position) {
        String placeName = listPlace1.get(position);
        connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
        if (connection != null) {
            try {
                String query = "DELETE FROM AREA_FACTORY WHERE AREA_NAME = ? AND FACTORY_ID = (SELECT FACTORY_ID FROM FACTORY WHERE NAME_FACTORY = ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, placeName);
                preparedStatement.setString(2, factoryName);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    listPlace1.remove(position);
                    placeAdapter.notifyDataSetChanged();
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

    //Xóa itemThiết bị:
    private void deleteDevice(int position) {
        String deviceName = listDevice1.get(position);
        connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
        if (connection != null) {
            try {
                String query = "DELETE FROM DEVICE_FACTORY WHERE DEVICE_NAME = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, deviceName);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    listDevice1.remove(position);
                    deleteDeviceInfo(position);
                    deviceAdapter.notifyDataSetChanged();
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
    //Xóa thông tin DeviceInfo theo DeviceName
    private void deleteDeviceInfo(int position) {
        String deviceName = listDevice1.get(position);
        connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
        if (connection != null) {
            try {
                String query = "DELETE FROM DEVICE_INFO WHERE DEVICE_NAME = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, deviceName);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    listDevice1.remove(position);
                    deviceAdapter.notifyDataSetChanged();
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
    //Load Data:
    private void loadPlacesFromDatabase() {
        connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
        if (connection != null) {
            try {
                String query = "SELECT AREA_NAME FROM AREA_FACTORY WHERE FACTORY_ID = (SELECT FACTORY_ID FROM FACTORY WHERE NAME_FACTORY = ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, factoryName);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String placeName = resultSet.getString("AREA_NAME");
                    listPlace1.add(placeName);
                }
                connection.close();
                placeAdapter.notifyDataSetChanged();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Lỗi khi tải dữ liệu từ cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Kết nối không thành công", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDevicesFromDatabase() {
        //Tạo kết nối:
        connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
        if (connection != null) {
            try {
                String query = "SELECT DEVICE_NAME FROM DEVICE_FACTORY WHERE FACTORY_ID = (SELECT FACTORY_ID FROM FACTORY WHERE NAME_FACTORY = ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, factoryName);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String deviceName = resultSet.getString("DEVICE_NAME");
                    listDevice1.add(deviceName);
                }
                connection.close();
                deviceAdapter.notifyDataSetChanged();
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
