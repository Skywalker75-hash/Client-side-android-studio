package com.example.myapplication8;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        //获取传递过来的数据
        String itemName = getIntent().getStringExtra("itemName");
        double itemPrice = getIntent().getDoubleExtra("itemPrice", 0);
        String itemImageData = getIntent().getStringExtra("itemImageData");
        String userName = getIntent().getStringExtra("userName");
        String category = getIntent().getStringExtra("Category");

        //找到页面资源
        TextView textViewName = findViewById(R.id.text_view_name);
        TextView textViewPublisher = findViewById(R.id.text_view_publisher);
        TextView textViewCategory = findViewById(R.id.text_view_category);
        TextView textViewPrice = findViewById(R.id.text_view_price);
        ImageView imageView = findViewById(R.id.image_view);

        //将数据动态绑定到页面资源上
        textViewName.setText(itemName);
        textViewPublisher.setText(userName);
        textViewCategory.setText(category);
        textViewPrice.setText(String.valueOf(itemPrice));
        //图片数据解析并显示
        if (itemImageData != null && !itemImageData.isEmpty()) {
            //去掉前缀
            if (itemImageData.startsWith("data:image/jpeg;base64,")) {
                itemImageData = itemImageData.substring("data:image/jpeg;base64,".length());
            }
            byte[] decodedString = Base64.decode(itemImageData, Base64.DEFAULT);//解码成字节数据
            //将字节数据转换成 Bitmap 对象
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte);//绑定到imageView
        }

        Button buybutton = findViewById(R.id.buybutton);//购买按钮

        //购买按钮点击事件
        buybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }


}