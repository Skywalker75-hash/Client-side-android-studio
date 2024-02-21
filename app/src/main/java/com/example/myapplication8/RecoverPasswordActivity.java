package com.example.myapplication8;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecoverPasswordActivity extends AppCompatActivity {

    private EditText usernameEditText, newPasswordEditText, verificationCodeEditText;
    private Button resetPasswordButton, getVerificationCodeButton;
    private String generatedVerificationCode; // 用于存储生成的验证码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoverpassword); // 确保你有一个相应的布局文件

        usernameEditText = findViewById(R.id.usernameEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        verificationCodeEditText = findViewById(R.id.verificationCodeEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        getVerificationCodeButton = findViewById(R.id.getVerificationCodeButton);

        // 获取验证码按钮点击事件
        getVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 生成一个4位数的随机验证码
                generatedVerificationCode = generateRandomCode(4);

                // 显示验证码给用户
                Toast.makeText(RecoverPasswordActivity.this, "验证码: " + generatedVerificationCode, Toast.LENGTH_LONG).show();
            }
        });

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String newPassword = newPasswordEditText.getText().toString();
                String verificationCode = verificationCodeEditText.getText().toString();

                // 检查验证码是否正确
                if (!verificationCode.equals(generatedVerificationCode)) {
                    Toast.makeText(RecoverPasswordActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 调用异步任务执行网络请求
                new ResetPasswordTask().execute(username, newPassword, verificationCode);
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

    private class ResetPasswordTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String newPassword = params[1];
            String verificationCode = params[2];

            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject json = new JSONObject();
            try {
                json.put("username", username);
                json.put("newPassword", newPassword);
                json.put("verificationCode", verificationCode);
            } catch (JSONException e) {

                return null;
            }

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:3000/recoverpassword") // Replace with your actual URL
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean success = jsonObject.getBoolean("success");
                    String message = jsonObject.getString("message");
                    if (success) {
                        Toast.makeText(RecoverPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                    } else {

                        Toast.makeText(RecoverPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(RecoverPasswordActivity.this, "响应解析失败", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(RecoverPasswordActivity.this, "收到空响应", Toast.LENGTH_LONG).show();
            }
        }
    }
}
