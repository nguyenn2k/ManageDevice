package com.example.managedevices;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.managedevices.Database.ConnectToDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
public class DeviceInfo extends AppCompatActivity {
    // Tạo kết nối:
    private Connection connection;
    // Upload and Take a Picture:
    private static final int CAMERA_REQUEST = 1;
    private static final int RESULT_LOAD_IMAGE = 2;
    // Tạo biến cho các trường EditText:
    private EditText editTextDeviceName,editTextManageCode, editTextInstallationDate, editTextLocation, editTextInspector, editTextMaintenancePerson, editTextDescription;
    // Biến chứa ảnh Upload:
    private ImageView imageView;
    // Xem xét có Disable không?
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_info);
        // Nhận dữ liệu Intent:
        String deviceName = getIntent().getStringExtra("device_name");
        String factoryName = getIntent().getStringExtra("factory_name");
        //Integer factoryId = Integer.parseInt(getIntent().getStringExtra("factory_id"));

        // ---------- Khi Khởi tạo thì Tất cả các Trường EditText đều không được Nhập ----------//
        EditText edtDeviceName = findViewById(R.id.edtDeviceName);
        edtDeviceName.setText(deviceName);
        edtDeviceName.setEnabled(false);
        EditText editLocation = findViewById(R.id.edtLocation);
        editLocation.setText(factoryName);
        editLocation.setEnabled(false);

        // ---------- Ánh xạ ----------//
        edtDeviceName = findViewById(R.id.edtDeviceName);
        editTextManageCode = findViewById(R.id.edtManageCode);
        editTextInstallationDate = findViewById(R.id.edtInstallationDay);
        editTextLocation = findViewById(R.id.edtLocation);
        editTextInspector = findViewById(R.id.edtInspector);
        editTextMaintenancePerson = findViewById(R.id.edtMaintencePerson);
        editTextDescription = findViewById(R.id.edtDescription);

        // Nơi lưu ảnh khi Upload lên:
        imageView = findViewById(R.id.imgDeviceInfo);

        /**
         * Tạo hàm Load Database liên quan đến item 'Thiết bị' bên PlaceFactoryActivity(); trước khi khởi tạo DeviceInfo
         * liên quan đến item 'Thiết bị' đó:
         */
        LoadDataDeviceInfoFromDatabase(deviceName);


        // Định dạng ngày tháng (DD-MM-YYYY):
        editTextInstallationDate.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "DDMMYYYY";
            private Calendar cal = Calendar.getInstance();
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]", "");
                    String cleanC = current.replaceAll("[^\\d.]", "");
                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    if (clean.equals(cleanC)) sel--;
                    if (clean.length() < 8) {
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        int day  = Integer.parseInt(clean.substring(0,2));
                        int mon  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        if(mon > 12) mon = 12;
                        cal.set(Calendar.MONTH, mon-1);

                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);

                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d",day, mon, year);
                    }
                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    editTextInstallationDate.setText(current);
                    editTextInstallationDate.setSelection(sel < current.length() ? sel : current.length());
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        //Khi chụp ảnh:
        Button buttonCapture = findViewById(R.id.btnCapture);
        buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
        // Khi Upload Ảnh:
        Button buttonGallery = findViewById(R.id.btnGallery);
        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
            }
        });
        // Hành động cho nút 'Thêm':
        Button buttonInsertDeviceInfo = findViewById(R.id.btnInsertDeviceInfo);
        buttonInsertDeviceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Nhận dữ liệu Intent:
                String deviceName = getIntent().getStringExtra("device_name");
                String factoryName = getIntent().getStringExtra("factory_name");

                // Lấy dữ liệu từ các trường EditText
                String manageCode = editTextManageCode.getText().toString().trim();
                String installationDate = editTextInstallationDate.getText().toString().trim();
                String inspector = editTextInspector.getText().toString().trim();
                String maintenancePerson = editTextMaintenancePerson.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();

                // Chuyển đổi ImageView thành Bitmap
                Bitmap imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                // Chuyển đổi Bitmap thành chuỗi Base64
                String imageBase64 = imageViewToBase64(imageBitmap);

                // Kiểm tra xem các trường có rỗng không
                if (TextUtils.isEmpty(manageCode) || TextUtils.isEmpty(installationDate) ||
                        TextUtils.isEmpty(inspector) || TextUtils.isEmpty(maintenancePerson) || imageBase64 == null) {
                    Toast.makeText(DeviceInfo.this, "Vui lòng điền đầy đủ thông tin và tải ảnh lên.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Thực hiện câu lệnh SQL Insert
                try {
                    connection = connectionToDatabase(ConnectToDatabase.username.toString(),ConnectToDatabase.password.toString(),ConnectToDatabase.db.toString(),ConnectToDatabase.ip.toString());
                    if (connection != null) {
                        String query = "INSERT INTO DEVICE_INFO (DEVICE_NAME, MANAGE_CODE, INSTALLATION_DAY, LOCATION, INSPECTOR, MAINTENANCE_PERSON, DESCRIPTION, IMAGE_UPLOAD) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        //Nhận giá trị Intent factoryName
                        preparedStatement.setString(1, deviceName);
                        preparedStatement.setString(2, manageCode);
                        preparedStatement.setString(3, installationDate);
                        //Nhận giá trị Intent factoryName được xem như là 'location' của Thiết bị đó:
                        preparedStatement.setString(4, factoryName);
                        preparedStatement.setString(5, inspector);
                        preparedStatement.setString(6, maintenancePerson);
                        preparedStatement.setString(7, description);
                        preparedStatement.setString(8, imageBase64);
                        //Thực thi lệnh Insert vào Databse:
                        preparedStatement.executeUpdate();
                        //Đóng kết nối:
                        preparedStatement.close();
                        connection.close();
                        // Thông báo thêm thành công
                        Toast.makeText(DeviceInfo.this, "Thông tin đã được thêm vào cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();

                        // Disable tất cả các trường nhập EditText
                        disableEditTexts();
                    } else {
                        Toast.makeText(DeviceInfo.this, "Không thể kết nối đến cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    Toast.makeText(DeviceInfo.this, "Lỗi khi thêm thông tin vào cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Nút 'Sửa:
        Button buttonEditDeviceInfo = findViewById(R.id.btnEditDeviceInfo);
        buttonEditDeviceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDeviceInfo();
            }
        });

        // Nút 'Lưu':
        Button buttonUpdateDeviceInfo = findViewById(R.id.btnUpdateDeviceInfo);
        buttonUpdateDeviceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDeviceInfo();
            }
        });
    }
    /**
     * Hàm Load Database liên quan đến item 'Thiết bị' bên PlaceFactoryActivity(); trước khi khởi tạo DeviceInfo
     * liên quan đến item 'Thiết bị' đó:
     */

    // Hàm Load dữ liệu từ cơ sở dữ liệu
    private void LoadDataDeviceInfoFromDatabase(String deviceName) {
        connection = connectionToDatabase(ConnectToDatabase.username.toString(), ConnectToDatabase.password.toString(), ConnectToDatabase.db.toString(), ConnectToDatabase.ip.toString());
        if (connection != null) {
            try {
                String query = "SELECT * FROM DEVICE_INFO WHERE DEVICE_NAME = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, deviceName);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String manageCode = resultSet.getString("MANAGE_CODE");
                    String installationDate = resultSet.getString("INSTALLATION_DAY");
                    String inspector = resultSet.getString("INSPECTOR");
                    String maintenancePerson = resultSet.getString("MAINTENANCE_PERSON");
                    String description = resultSet.getString("DESCRIPTION");
                    String imageBase64 = resultSet.getString("IMAGE_UPLOAD");

                    // Hiển thị thông tin lên các trường EditText
                    editTextManageCode.setText(manageCode);
                    editTextInstallationDate.setText(installationDate);
                    editTextInspector.setText(inspector);
                    editTextMaintenancePerson.setText(maintenancePerson);
                    editTextDescription.setText(description);

                    // Chuyển đổi chuỗi Base64 thành mảng byte
                    byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                    // Chuyển đổi mảng byte thành Bitmap
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    // Hiển thị ảnh lên ImageView:
                    imageView.setImageBitmap(decodedByte);
                }
                disableEditTexts();
                // Đóng kết nối và preparedStatement
                resultSet.close();
                preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Lỗi khi tải dữ liệu từ cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Kết nối không thành công", Toast.LENGTH_SHORT).show();
        }
    }

    // onActivityResult:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            imageView.setVisibility(View.VISIBLE);
        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // Hàm 'editDeviceInfo()':
    private void editDeviceInfo() {
        isEditing = true;
        enableEditTexts();
        // 2 Trường 'Tên thiết bị' & 'Khu vực' sẽ không được Sửa hay Thêm hoặc Cập nhật:
        EditText edtDeviceName = findViewById(R.id.edtDeviceName);
        EditText edtLocation = findViewById(R.id.edtLocation);
        edtDeviceName.setEnabled(false);
        edtLocation.setEnabled(false);

        // Khi nhấn nút 'Sửa', các trường EditText dưới đây sẽ được cho nhập:
        EditText edtManageCode = findViewById(R.id.edtManageCode);
        edtManageCode.setEnabled(true);
        EditText edtInspector = findViewById(R.id.edtInspector);
        edtInspector.setEnabled(true);
        EditText edtMaintencePerson = findViewById(R.id.edtMaintencePerson);
        edtMaintencePerson.setEnabled(true);
        EditText edtInstallationDay = findViewById(R.id.edtMaintencePerson);
        edtInstallationDay.setEnabled(true);

        // Thông báo:
        Toast.makeText(DeviceInfo.this, "Giờ bạn có thể Sửa Thông Tin.", Toast.LENGTH_SHORT).show();
    }

    // Kiểm tra TextFiled xem có bị Null hoặc có giá trị NULL ko:
    private boolean checkEditTextFields() {
        return (!TextUtils.isEmpty(editTextDeviceName.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextManageCode.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextInstallationDate.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextInspector.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextMaintenancePerson.getText().toString().trim()));
    }

    private void updateDeviceInfo() {
        // Sau khi Update các thông tin xong thì:
        isEditing = false; // Không cho phép nhập EditText;
        disableEditTexts(); // Gọi 'Phương thức 'Không cho phép nhập' các EditText'
        // Chuyển đổi ImageView thành Bitmap
        Bitmap imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        // Chuyển đổi Bitmap thành chuỗi Base64
        String imageBase64 = imageViewToBase64(imageBitmap);
        // Kiểm tra xem các trường có giá trị không
        if (TextUtils.isEmpty(imageBase64)) {
            Toast.makeText(DeviceInfo.this, "Vui lòng chọn hoặc chụp ảnh.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Thông báo:
        Toast.makeText(DeviceInfo.this, "Thông tin đã được cập nhật.", Toast.LENGTH_SHORT).show();
    }
    // Phương thức 'Cho phép nhập' các EditText:
    private void enableEditTexts() {
        editTextManageCode.setEnabled(true);
        editTextLocation.setEnabled(true);
        editTextInstallationDate.setEnabled(true);
        editTextInspector.setEnabled(true);
        editTextMaintenancePerson.setEnabled(true);
        editTextDescription.setEnabled(true);
        imageView.setEnabled(true);
    }

    // Phương thức 'Không cho phép nhập' các EditText:
    private void disableEditTexts() {
        editTextManageCode.setEnabled(false);
        editTextLocation.setEnabled(false);
        editTextInstallationDate.setEnabled(false);
        editTextInspector.setEnabled(false);
        editTextMaintenancePerson.setEnabled(false);
        editTextDescription.setEnabled(false);
        imageView.setEnabled(false);
    }

    // Chuyển đổi Bitmap thành chuỗi Base64:
    private String imageViewToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Phương thức kết nối với Database SQL Server :
    private Connection connectionToDatabase(String username, String password, String db, String ip) {
        Connection connection;
        String connectionString = "jdbc:jtds:sqlserver://" + ip + ";databaseName=" + db + ";user=" + username + ";password=" + password + ";";
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            connection = DriverManager.getConnection(connectionString);
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
