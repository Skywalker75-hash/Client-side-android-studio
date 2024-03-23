package com.example.myapplication8;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        //获取传递过来的数据
        int itemID = getIntent().getIntExtra("itemID", -1);
        String itemName = getIntent().getStringExtra("itemName");
        double itemPrice = getIntent().getDoubleExtra("itemPrice", 0);
        String itemImageData = getIntent().getStringExtra("itemImageData");
        String userName = getIntent().getStringExtra("userName");
        String category = getIntent().getStringExtra("Category");
        String username = getIntent().getStringExtra("username");

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
                new buyItemTask(username,itemID).execute();
            }
        });
    }private class buyItemTask extends AsyncTask<Void, Void, String> {
        private String username;
        private int itemID; // 保留itemID为int类型
        private boolean x = false;

        public buyItemTask(String username, int itemID) {
            this.username = username;
            this.itemID = itemID;
        }

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("itemID", String.valueOf(itemID)) // 将itemID转换为String
                    .build();

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:3000/buyThings") // 替换为实际的服务器地址
                    .post(formBody)
                    .build();

            try {
                Log.d("buyItemTask", "Sending buy request for user: " + username + ", itemID: " + itemID);
                Response response = client.newCall(request).execute();
                x = true;
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return responseBody;
                }
            } catch (IOException e) {
                Log.e("buyItemTask", "Request failed", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.isEmpty()) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String message = jsonResponse.optString("message");
                    Toast.makeText(ItemDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(ItemDetailActivity.this, "解析响应失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (x) {
                    Toast.makeText(ItemDetailActivity.this, "服务器未响应，请稍后再试", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ItemDetailActivity.this, "请求未发送成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}