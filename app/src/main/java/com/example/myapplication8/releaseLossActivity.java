package com.example.myapplication8;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class releaseLossActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText editTextItemName, editTextCategory, editTextPrice;
    private ImageView imageViewSelectedImage;
    private Button uploadButton, uploadImageButton;
    private Uri imageUri; // 使用Uri代替路径字符串
    private String username;
    // private static final String TAG = "UploadTask";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release_loss);

        // 初始化视图组件
        editTextItemName = findViewById(R.id.editTextItemName);

        imageViewSelectedImage = findViewById(R.id.imageViewSelectedImage);
        uploadButton = findViewById(R.id.uploadbutton);
        uploadImageButton = findViewById(R.id.uploadImageButton);

        username = getIntent().getStringExtra("username");
        //返回按钮：
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 结束当前Activity，返回上一个Activity
                finish();
            }
        });

        //上传图片按钮点击事件：
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        //发布商品按钮点击事件：
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new releaseLossActivity.UploadTask().execute();
            }
        });
    }

    private void openImageChooser() {
        //隐式意图
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);//允许用户永久地授权文件的访问权限给应用
        intent.addCategory(Intent.CATEGORY_OPENABLE);//确保选中的文件可以被打开和读取
        intent.setType("image/*");//文件选择器将只显示所有类型的图片
        startActivityForResult(intent, PICK_IMAGE_REQUEST);//这个方法启动文件选择器，并期待返回结果
    }

    @Override
    //此方法用于选择图片活动结束后，将数据返回给上传商品活动
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewSelectedImage.setImageURI(imageUri);
            getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    public class UploadTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", username)
                    .addFormDataPart("LossItemName", editTextItemName.getText().toString());
            if (imageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);//输入流
                    if (inputStream != null) {
                        //把图片从输入流中获取并转化为二进制：
                        byte[] imageData = inputStream.readAllBytes();
                        //把图片放入请求体：
                        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), imageData);
                        builder.addFormDataPart("Image", "image.jpg", fileBody);
                    }
                } catch (IOException e) {

                    return null;
                }
            }

            RequestBody requestBody = builder.build();
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:3000/releaseLoss")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                } else {
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    String message = jsonObj.optString("message", "未知错误");
                    Toast.makeText(releaseLossActivity.this, message, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(releaseLossActivity.this, "响应解析失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(releaseLossActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
            }
        }

    }
}