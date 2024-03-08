package com.example.myapplication8;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button cardButton = findViewById(R.id.buttonCampusCard);//一卡通管理按钮
        Button academicButton = findViewById(R.id.buttonAcademicManagement);//教务管理按钮
        Button classScheduleButton=findViewById(R.id.buttonClassSchedule);//课程表按钮
        Button MarketButton = findViewById(R.id.buttonSecondHandMarket);//二手市场按钮
        //获取从MainActivity传递过来的用户名
        String username = getIntent().getStringExtra("username");

        //一卡通管理点击事件
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
        //教务管理点击事件
        academicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //加载自定义对话框布局
                LayoutInflater inflater = LayoutInflater.from(MenuActivity.this);
                View dialog1 = inflater.inflate(R.layout.dialog_academic_management, null);

                //创建对话框构造器
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                //设置对话框自定义布局
                builder.setView(dialog1);

                //创建对话框
                AlertDialog dialog = builder.create();

                //选课按钮点击事件：
                Button selectCourseButton = dialog1.findViewById(R.id.button_select_course);
                selectCourseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //创建跳转到SelectCourseActivity的Intent
                        Intent intent = new Intent(v.getContext(), SelectCourseActivity.class);
                        //将用户名作为额外数据传递
                        intent.putExtra("username", username);
                        //启动SelectCourseActivity
                        startActivity(intent);
                        dialog.dismiss(); // 关闭对话框
                    }
                });
                //选课情况按钮点击事件
                Button courseStatusButton = dialog1.findViewById(R.id.button_course_status);
                courseStatusButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //创建跳转到ShowSelectedCoursesActivity的Intent
                        Intent intent = new Intent(v.getContext(), ShowSelectedCoursesActivity.class);
                        //将用户名作为额外数据传递
                        intent.putExtra("username", username);
                        //启动ShowSelectedCoursesActivity
                        startActivity(intent);
                        dialog.dismiss(); // 关闭对话框
                    }
                });

                // 显示对话框
                dialog.show();
            }
        });
        //课程表按钮点击事件
        classScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建跳转到classScheduleActivity的Intent
                Intent intent = new Intent(v.getContext(), classScheduleActivity.class);
                // 将用户名作为额外数据传递
                intent.putExtra("username", username);
                // 启动classScheduleActivity
                startActivity(intent);

            }
        });
        //商城按钮点击事件
        MarketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //加载自定义对话框布局
                LayoutInflater inflater = LayoutInflater.from(MenuActivity.this);
                View dialog1 = inflater.inflate(R.layout.dialog_choose_market, null);

                //创建对话框构造器
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                //设置对话框自定义布局
                builder.setView(dialog1);

                //创建对话框
                AlertDialog dialog = builder.create();

                //发布商品按钮点击事件：
                Button releaseButton = dialog1.findViewById(R.id.releaseButton);
                releaseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //创建跳转到ReleaseThingsActivity的Intent
                        Intent intent = new Intent(v.getContext(), ReleaseThingsActivity.class);
                        //将用户名作为额外数据传递
                        intent.putExtra("username", username);
                        //启动ReleaseThingsActivity
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
                //购买商品按钮点击事件
                Button buyButton = dialog1.findViewById(R.id.buyButton);
                buyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //创建跳转到ShowThingsActivity的Intent
                        Intent intent = new Intent(v.getContext(), ShowThingsActivity.class);
                        //将用户名作为额外数据传递
                        intent.putExtra("username", username);
                        //启动ReleaseThingsActivity
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

                // 显示对话框
                dialog.show();
            }
        });

    }
}