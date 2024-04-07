package com.example.managedevices;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    //Tạo biến:
    Button btnPlaces,btnInformApp,btnExit;
    int counter = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //Ánh xạ các biến button đến id button trong activity_home:
        btnPlaces = findViewById(R.id.buttonPlaces);
        btnInformApp = findViewById(R.id.ButtonInformApp);
        btnExit = findViewById(R.id.buttonExitHome);

        //Khi nhấn nút "Nhà máy" sẽ chuyển đến "Thêm nhà máy":
        btnPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(HomeActivity.this, PlaceFactoryActivity.class);
                Intent intent = new Intent(HomeActivity.this, FactoryActivity.class);
                startActivity(intent);
            }
        });
        //Khi nhấn nút "Thông tin App" sẽ hiển thị Dialog:
        btnInformApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInformApp();
            }
        });
        //Khi nhấn nút "Thoát" sẽ thoát ứng dụng:
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogExit();
            }
        });
    }

    //Dialog "Thông tin App"
    private void DialogInformApp() {
        //Tạo đối tượng cửa sổ Dialog:
        Dialog dialog  =  new Dialog(this);
        //Nạp layout vào:
        dialog.setContentView(R.layout.activity_dialog_informapp);
        dialog.show();
    }

    //Dialog "Thoát":
    private void DialogExit() {
        //Tạo đối tượng cửa sổ Dialog
        Dialog dialog  =  new Dialog(this);
        //Nạp layout vào:
        dialog.setContentView(R.layout.activity_dialogexit);
        //Click Không mới thoát, click ngoài ko thoát:
        dialog.setCanceledOnTouchOutside(false);
        //Ánh xạ các biến button đến dialogexit.xml:
        Button btnYes = dialog.findViewById(R.id.buttonYes);
        Button btnNo = dialog.findViewById(R.id.buttonNo);
        //Khi nhấn nút "Có":
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Khởi tạo lại HomeActivity
                Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                startActivity(intent);
                // Tạo sự kiện kết thúc app
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startActivity(startMain);
            }
        });
        //Nếu Không thì đóng dialog -> Vẫn ở màn hình chính:
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        //Show Dialog lên Activity:
        dialog.show();
    }
}
