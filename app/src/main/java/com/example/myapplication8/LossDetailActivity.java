package com.example.myapplication8;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LossDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loss_detail);

        // 获取传递过来的数据
        final String LossItemName = getIntent().getStringExtra("LossItemName");
        final int LossItemID = getIntent().getIntExtra("LossItemID", -1);
        String itemImageData = getIntent().getStringExtra("itemImageData");
        String userName = getIntent().getStringExtra("userName");

        // 找到页面资源
        TextView textViewName = findViewById(R.id.text_view_name);
        TextView textViewPublisher = findViewById(R.id.text_view_publisher);
        ImageView imageView = findViewById(R.id.image_view);
        Button releaseButton = findViewById(R.id.releasebutton);
        EditText editTextPhone = findViewById(R.id.edit_view_phone);

        // 页面资源绑定数据
        textViewName.setText("物品名称："+LossItemName);
        textViewPublisher.setText("挂失者："+userName);
        // 图片数据解析并显示
        if (itemImageData != null && !itemImageData.isEmpty()) {
            if (itemImageData.startsWith("data:image/jpeg;base64,")) {
                itemImageData = itemImageData.substring("data:image/jpeg;base64,".length());
            }
            byte[] decodedString = Base64.decode(itemImageData, Base64.DEFAULT); // 解码成字节数据
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte); // 绑定到imageView
        }

        // 按钮点击事件
        releaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = editTextPhone.getText().toString();
                if (!phone.isEmpty()) {
                    new UpdatePhoneTask(LossItemID,phone).execute();
                } else {
                    Toast.makeText(LossDetailActivity.this, "请输入电话号码", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class UpdatePhoneTask extends AsyncTask<Void, Void, String> {
        private String phone;
        private int lossItemID;
        boolean success = false;

        public UpdatePhoneTask(int lossItemID, String phone) {
            this.lossItemID = lossItemID;
            this.phone = phone;
        }

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("LossItemID", String.valueOf(lossItemID)) // 将LossItemID转换为String
                    .add("Phone", phone)
                    .build();

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:3000/releasePhone") // 替换为实际的服务器地址
                    .post(formBody)
                    .build();

            try {
                Log.d("UpdatePhoneTask", "Sending update request for LossItemID: " + lossItemID + ", Phone: " + phone);
                Response response = client.newCall(request).execute();
                success = true;
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return responseBody;
                }
            } catch (IOException e) {
                Log.e("UpdatePhoneTask", "Request failed", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.isEmpty()) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String message = jsonResponse.optString("message");
                    Toast.makeText(LossDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(LossDetailActivity.this, "解析响应失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (success) {
                    Toast.makeText(LossDetailActivity.this, "服务器未响应，请稍后再试", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LossDetailActivity.this, "请求未发送成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
