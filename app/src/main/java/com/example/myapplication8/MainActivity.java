package com.example.myapplication8;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginButton = findViewById(R.id.loginButton);//登录按钮
        Button registerButton = findViewById(R.id.registerButton);//注册按钮
        Button recoverPasswordButton = findViewById(R.id.recoverPasswordButton);//找回密码按钮
        //登录按钮点击事件：
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = ((EditText) findViewById(R.id.usernameEditText)).getText().toString();
                String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
                new LoginTask().execute(username, password);

            }
        });
        //注册按钮点击事件：
        registerButton.setOnClickListener(new View.OnClickListener() {//new View是匿名类
            @Override
            public void onClick(View v) {
                // 创建跳转到RegisterActivity的Intent
                Intent intent = new Intent(v.getContext(), RegisterActivity.class);
                // 启动RegisterActivity
                startActivity(intent);
            }
        });
        //找回密码按钮点击事件
        recoverPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建跳转到RecoverPasswordActivity的Intent
                Intent intent = new Intent(v.getContext(), RecoverPasswordActivity.class);
                // 启动RecoverPasswordActivity
                startActivity(intent);
            }
        });
    }

//登录逻辑代码：
    //AsyncTask是一个异步操作类，execute方法可以使其启动，启动后先在后台执行doInBackground，再在主线程执行onPostExecute，
    //doInBackground的返回值将作为onPostExecute的参数
class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];

            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("username", username);
                jsonParam.put("password", password);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonParam.toString(), JSON);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/login")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    return response.body().string();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                boolean success = jsonObj.getBoolean("success");
                String message = jsonObj.getString("message");
                String username = ((EditText) findViewById(R.id.usernameEditText)).getText().toString();
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        // 创建跳转到MenuActivity的Intent
                        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                        // 将用户名作为额外数据传递
                        intent.putExtra("username", username);
                        // 启动MenuActivity
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "错误，未能成功解析", Toast.LENGTH_SHORT).show());
            }
        } else {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "收到空响应", Toast.LENGTH_SHORT).show());
        }
    }

  }
}
