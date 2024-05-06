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

import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class RegisterActivity extends AppCompatActivity {

    private String generatedVerificationCode; // 用于存储生成的验证码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button registerButton = findViewById(R.id.registerButton); // 注册按钮
        Button getVerificationCodeButton = findViewById(R.id.getVerificationCodeButton); // 获取验证码按钮

        // 获取验证码按钮点击事件
        getVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 生成一个4位数的随机验证码
                generatedVerificationCode = generateRandomCode(4);
                // 显示验证码给用户
                Toast.makeText(RegisterActivity.this, "验证码: " + generatedVerificationCode, Toast.LENGTH_LONG).show();
            }
        });

        // 注册按钮点击事件
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = ((EditText) findViewById(R.id.usernameEditText)).getText().toString();
                String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
                String confirmPassword = ((EditText) findViewById(R.id.confirmPasswordEditText)).getText().toString();
                //String cardNumber = ((EditText) findViewById(R.id.campuscardEditText)).getText().toString();
                String verificationCode = ((EditText) findViewById(R.id.verificationCodeEditText)).getText().toString();

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "密码和确认密码不匹配", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 检查验证码是否正确
                if (!verificationCode.equals(generatedVerificationCode)) {
                    Toast.makeText(RegisterActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                new RegisterTask().execute(username, password, verificationCode);
            }
        });
    }

    // 生成指定长度的随机数字验证码
    private String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10)); // 生成一个0到9的随机数
        }
        return code.toString();
    }

    // 注册逻辑代码
    private class RegisterTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            //String cardNumber=params[2];
            String verificationCode = params[2];

            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("username", username);
                jsonParam.put("password", password);
                //jsonParam.put("cardNumber", cardNumber);
                jsonParam.put("verificationCode", verificationCode);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonParam.toString(), JSON);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/register")
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
                    runOnUiThread(() -> {
                        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            // 注册成功，跳转到登录页面
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // 关闭当前的注册活动

                        } else {
                            // 注册失败的处理
                            Toast.makeText(RegisterActivity.this, message , Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "响应解析失败", Toast.LENGTH_SHORT).show());
                }
            } else {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "收到空响应", Toast.LENGTH_SHORT).show());
            }
        }
    }
}


