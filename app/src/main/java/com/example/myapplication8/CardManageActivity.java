package com.example.myapplication8;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
        Button registerButton=findViewById(R.id.registerButton); //注册账户按钮
        Button accountButton = findViewById(R.id.btnQueryAccount); //查询账户按钮
        Button rechargeButton = findViewById(R.id.btnRecharge);//充值按钮
        Button reportLossButton = findViewById(R.id.btnReportLoss);//挂失按钮
        Button cannelButton = findViewById(R.id.btnCancelLoss);//解挂按钮
        ImageButton backButton = findViewById(R.id.backButton);//返回按钮

        //返回按钮：
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 结束当前Activity，返回上一个Activity
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText cardInput = new EditText(CardManageActivity.this);
                cardInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(CardManageActivity.this)
                        .setTitle("注册校园卡")
                        .setMessage("请输入校园卡号:")
                        .setView(cardInput)
                        .setPositiveButton("注册", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String cardNumber = cardInput.getText().toString();
                                new RegisterCardTask().execute(username, cardNumber); // 执行异步任务进行注册
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        //查询账户按钮点击事件
        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new QueryAccountTask().execute(username);
            }
        });
        //充值按钮点击事件
        rechargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 弹出对话框让用户输入充值金额
                final EditText input = new EditText(CardManageActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                new AlertDialog.Builder(CardManageActivity.this)
                        .setTitle("校园卡充值")
                        .setMessage("输入金额:")
                        .setView(input)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String amount = input.getText().toString();
                                new RechargeTask().execute(username, amount); // 执行异步任务进行充值
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        //挂失按钮点击事件
        reportLossButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                new ReportLossTask().execute(username);
            }
        });
        //解挂按钮点击事件
        cannelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                new CancelLossTask().execute(username);
            }
        });

    }
    private class RegisterCardTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String cardNumber = params[1];

            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userName", username);
                jsonParam.put("cardNumber", cardNumber);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonParam.toString(), JSON);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/registercard")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    return response.body().string();
                }
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Toast.makeText(getApplicationContext(), "网络请求失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObj = new JSONObject(result);
                boolean success = jsonObj.getBoolean("success");
                String message = jsonObj.getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "响应解析失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //查询账户：
    private class QueryAccountTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userName", username);
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonParam.toString(), JSON);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/queryAccount")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    String responseBody = response.body().string();

                    return responseBody;
                }
            } catch (Exception e) {

                return null;
            }
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Toast.makeText(getApplicationContext(),"收到空响应", Toast.LENGTH_SHORT).show();
                //showDialog("错误", "接收到空响应");
                return;
            }

            try {
                JSONObject jsonObj = new JSONObject(result);
                boolean success = jsonObj.getBoolean("success");
                String message = jsonObj.getString("message");
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
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        //showDialog("解析data失败", "未找到校园卡账户信息");
                    }
                } else {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"响应解析失败", Toast.LENGTH_SHORT).show();

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

    //挂失校园卡:
    private class ReportLossTask extends AsyncTask<String, Void, String> {
        private final String TAG = ReportLossTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];

            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userName", username); // 假设服务器端接受用户名来标识需要挂失的账户

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonParam.toString(), JSON);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/reportLoss") // 注意替换为你的实际URL
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    String responseBody = response.body().string();

                    return responseBody;
                }
            } catch (Exception e) {

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Toast.makeText(getApplicationContext(),"收到空响应", Toast.LENGTH_SHORT).show();
                //showDialog("错误", "网络请求失败，请检查网络连接");
                return;
            }

            try {
                JSONObject jsonObj = new JSONObject(result);
                boolean success = jsonObj.getBoolean("success");
                String message = jsonObj.getString("message");
                Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
                //showDialog(success ? "挂失成功" : "挂失失败", message);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"响应解析失败", Toast.LENGTH_SHORT).show();
                //showDialog("错误", "解析响应失败");
            }
        }


    }


    //解挂:
    private class CancelLossTask extends AsyncTask<String, Void, String> {
       // private final String TAG = CancelLossTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];

            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userName", username); // 假设服务器端接受用户名来标识需要解挂的账户

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonParam.toString(), JSON);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/cancelLoss") // 注意替换为你的实际URL
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    String responseBody = response.body().string();

                    return responseBody;
                }
            } catch (Exception e) {

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Toast.makeText(getApplicationContext(),"收到空相应", Toast.LENGTH_SHORT).show();
               // showDialog("错误", "网络请求失败，请检查网络连接");
                return;
            }

            try {
                JSONObject jsonObj = new JSONObject(result);
                boolean success = jsonObj.getBoolean("success");
                String message = jsonObj.getString("message");

                //showDialog(success ? "解挂成功" : "解挂失败", message);
                Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"响应解析失败", Toast.LENGTH_SHORT).show();
               // showDialog("错误", "解析响应失败");
            }
        }


    }


    // 充值校园卡任务
    private class RechargeTask extends AsyncTask<String, Void, String> {
        //private final String TAG = RechargeTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String amount = params[1]; // 假设第二个参数为充值金额


            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userName", username);
                jsonParam.put("amount", amount); // 假设服务器端接受一个名为"amount"的参数作为充值金额

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonParam.toString(), JSON);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/rechargeCard") // 注意替换为你的实际URL
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    String responseBody = response.body().string();

                    return responseBody;
                }
            } catch (Exception e) {

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Toast.makeText(getApplicationContext(),"收到空相应", Toast.LENGTH_SHORT).show();
                //showDialog("错误", "网络请求失败，请检查网络连接");
                return;
            }

            try {
                JSONObject jsonObj = new JSONObject(result);
                boolean success = jsonObj.getBoolean("success");
                String message = jsonObj.getString("message");
                Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
                //showDialog(success ? "充值成功" : "充值失败", message);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"响应解析失败", Toast.LENGTH_SHORT).show();
                //showDialog("错误", "解析响应失败");
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
