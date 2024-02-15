package com.example.myapplication8;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CardManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_manage);

        //获取从MenuActivity传过来的username
        String username = getIntent().getStringExtra("username");
        Button accountButton = findViewById(R.id.btnQueryAccount); //查询账户按钮

        //查询账户按钮点击事件
        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new QueryAccountTask().execute(username);
            }
        });
    }

    private class QueryAccountTask extends AsyncTask<String, Void, String> {
        private final String TAG = QueryAccountTask.class.getSimpleName(); // 定义TAG用于日志输出

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            Log.d(TAG, "doInBackground: username = " + username); // 输出用户名日志

            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userName", username);
                Log.d(TAG, "doInBackground: JSON Params = " + jsonParam.toString()); // 输出构造的JSON日志

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonParam.toString(), JSON);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/queryAccount")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "doInBackground: Response = " + responseBody); // 输出响应体日志
                    return responseBody;
                }
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: Error", e); // 输出异常日志
                return null;
            }
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Log.d(TAG, "onPostExecute: Received null response");
                showDialog("错误", "接收到空响应");
                return;
            }

            try {
                JSONObject jsonObj = new JSONObject(result);
                boolean success = jsonObj.getBoolean("success");
                if (success) {
                    JSONArray data = jsonObj.getJSONArray("data");
                    if (data.length() > 0) {
                        JSONObject firstItem = data.getJSONObject(0); // 获取数组的第一个元素
                        String balance = firstItem.getString("balance");
                        String cardNumber = firstItem.getString("cardNumber"); // 获取卡号
                        int lostStatusInt = firstItem.getInt("lostStatus");
                        boolean lostStatus = (lostStatusInt != 0); // 如果lostStatusInt不等于0，则为true

                        // 在显示信息中加入卡号
                        showDialog("查询成功", "卡号: " + cardNumber + "\n余额: " + balance + "\n挂失状态: " + (lostStatus ? "已挂失" : "未挂失"));
                    } else {
                        showDialog("查询失败", "未找到校园卡账户信息");
                    }
                } else {
                    String message = jsonObj.getString("message");
                    showDialog("查询失败", message);
                }
            } catch (Exception e) {
                Log.e(TAG, "onPostExecute: Error parsing JSON response", e);
                showDialog("错误", "解析响应失败");
            }
        }

        private void showDialog(String title, String message) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CardManageActivity.this);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }
}
