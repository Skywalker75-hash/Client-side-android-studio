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

import okhttp3.MediaType;
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
        textViewName.setText("商品名称：" + itemName);
        textViewPublisher.setText("发布者：" + userName);
        textViewCategory.setText("类别：" + category);
        textViewPrice.setText("价格：" + String.format("%.2f", itemPrice));

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
    }
    private class buyItemTask extends AsyncTask<Void, Void, String> {
        private String username;
        private int itemID;

        public buyItemTask(String username, int itemID) {
            this.username = username;
            this.itemID = itemID;
        }

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            JSONObject jsonParam = new JSONObject();
            try {
                jsonParam.put("username", username);
                jsonParam.put("itemID", itemID);
            } catch (JSONException e) {
                e.printStackTrace();
                return null; // 返回null表示JSON构造失败
            }

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonParam.toString(), JSON);

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:3000/buyThings") // 确保URL是正确的
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string(); // 直接返回服务器响应的内容
                } else {
                    return null; // 直接返回null，不构造错误消息
                }
            } catch (IOException e) {
                Log.e("buyItemTask", "Request failed", e);
                return null; // 返回null表示请求失败
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");
                    Toast.makeText(ItemDetailActivity.this, message, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Toast.makeText(ItemDetailActivity.this, "解析响应失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(ItemDetailActivity.this, "没有从服务器收到响应", Toast.LENGTH_LONG).show();
            }
        }
    }


}