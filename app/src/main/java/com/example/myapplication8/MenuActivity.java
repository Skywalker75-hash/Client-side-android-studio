package com.example.myapplication8;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button cardButton = findViewById(R.id.buttonCampusCard);//一卡通管理按钮
        // 获取从MainActivity传递过来的用户名
        String username = getIntent().getStringExtra("username");
        cardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 创建跳转到CardManageActivity的Intent
                Intent intent = new Intent(v.getContext(), CardManageActivity.class);
                // 将用户名作为额外数据传递
                intent.putExtra("username", username);
                // 启动CardManageActivity
                startActivity(intent);

            }
        });


    }
}