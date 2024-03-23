package com.example.myapplication8;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ManageThingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Item> dataList = new ArrayList<>();
    private MyAdapter adapter;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 1;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_things);

        recyclerView = findViewById(R.id.recyclerView2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(dataList);
        recyclerView.setAdapter(adapter);
        username = getIntent().getStringExtra("username");//获取传过来的用户id
        Button manageButton=findViewById(R.id.buttonSubmit);//商品删除按钮
        //返回按钮：
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 结束当前Activity，返回上一个Activity
                finish();
            }
        });
        //按钮点击事件：
        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemID = ((EditText) findViewById(R.id.editTextInput)).getText().toString();
                new DeleteItemTask().execute(itemID);

            }
        });

        loadMoreItems();
        //recyclerView的滚动监听器
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //获取RecyclerView当前使用的布局管理器，强转为LinearLayoutManager
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //检查是否正在加载数据或已经是最后一页
                if (!isLoading && !isLastPage) {
                    // 获取当前RecyclerView的总条目数
                    int totalItemCount = layoutManager.getItemCount();
                    // 获取当前完全可见的最后一个条目的位置
                    int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                    // 如果总条目数小于或等于最后一个可见条目的位置加1，代表滚动到了列表底部
                    if (totalItemCount <= (lastVisibleItemPosition + 1)) {
                        loadMoreItems();
                        isLoading = true;
                    }
                }
            }
        });
    }

    private void loadMoreItems() {
        new FetchItemsTask().execute();
    }
    //刷新页面方法：
    private void refreshItemsList() {
        // 清空现有数据
        dataList.clear();
        // 重置页面计数器
        currentPage = 1;
        // 重新加载数据
        loadMoreItems();
    }

    public class Item {
        private String name;//商品名称
        private String imageData; //存储Base64编码的图片数据
        private double price; //价格
        private String userName;
        private String Category;

        private int ItemID;

        //构造器
        public Item(String name, String imageData, double price, String userName, String Category, int ItemID) {
            this.name = name;
            this.imageData = imageData;
            this.price = price;
            this.userName = userName;
            this.Category = Category;
            this.ItemID = ItemID;
        }

        public String getName() {
            return name;
        }

        public String getImageData() {
            return imageData;
        }

        public double getPrice() {
            return price;
        }

        public String getuserName() {
            return userName;
        }

        public String getCategory() {
            return Category;
        }

        public int getItemID() {
            return ItemID;
        }

    }

    public class DeleteItemTask extends AsyncTask<String, Void, String> {
        private String TAG;
        @Override
        protected String doInBackground(String... params) {
            String itemID=params[0];
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("itemID", itemID)
                    .build();

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:3000/removeThings")
                    .post(formBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                } else {
                    Log.e(TAG, "Error: " + response);
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error making delete request", e);
                return null;
            }
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                Log.d(TAG, "Server response: " + result);
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    boolean success = jsonResponse.optBoolean("success", false);
                    String message = jsonResponse.optString("message", "No message returned from server.");
                    Toast.makeText(ManageThingsActivity.this, message, Toast.LENGTH_LONG).show();
                    if (success) {
                        refreshItemsList();//刷新循环视图
                        refreshItemsList();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing JSON response", e);
                    Toast.makeText(ManageThingsActivity.this, "Error parsing server response", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "Error: No response from server");
                Toast.makeText(ManageThingsActivity.this, "No response from server", Toast.LENGTH_LONG).show();
            }
        }

    }

    private class FetchItemsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("page", String.valueOf(currentPage))
                    .add("pageSize", "5")
                    .build();

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:3000/showuserThings")
                    .post(formBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d("ShowThingsActivity", "Response body: " + responseBody);
                    return responseBody;
                }
            } catch (IOException e) {
                Log.e("ShowThingsActivity", "Error fetching items", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            isLoading = false; // 停止加载指示
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    JSONArray data = jsonResponse.getJSONArray("data");
                    Log.d("ShowThingsActivity", "Parsed JSON data: " + data.toString());

                    if (data.length() == 0) {
                        isLastPage = true;
                        Toast.makeText(ManageThingsActivity.this, "所有数据已加载完成", Toast.LENGTH_SHORT).show();
                    } else {
                        // 循环遍历解析到的JSON数组，并将其添加到dataList中
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject itemObj = data.getJSONObject(i);
                            String itemName = itemObj.getString("ItemName"); // 请确保这里的键名与JSON响应中的一致
                            String imageData = itemObj.getString("Image"); // 获取图片数据（Base64编码）
                            double price = itemObj.getDouble("Price"); // 获取价格
                            String userName = itemObj.getString("userName");//获取发布者名字
                            String Category = itemObj.getString("Category");//获取商品类别
                            int ItemID = itemObj.getInt("ItemID"); // 获取商品ID

                            // 将解析的数据添加到ArrayList中
                            Item it = new Item(itemName, imageData, price, userName, Category, ItemID);
                            dataList.add(it);
                        }
                        // 通知适配器数据集已改变，以便更新UI
                        adapter.notifyDataSetChanged();
                        // 更新当前页码
                        currentPage++;
                    }
                } catch (JSONException e) {
                    Log.e("ShowThingsActivity", "JSON parsing error", e);
                    Toast.makeText(ManageThingsActivity.this, "数据解析错误", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ManageThingsActivity.this, "加载数据失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //RecyclerView适配器
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private final ArrayList<Item> dataList;

        public MyAdapter(ArrayList<Item> dataList) {
            this.dataList = dataList;
        }

        @NonNull
        @Override
        //为每个列表项创建一个新的视图（创建子视图）
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        //将数据绑定到列表项的视图上（将数据绑定到子视图上）
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Item item = dataList.get(position);
            Log.d("ShowThingsActivity", "Item Name: " + item.getName() + ", Price: $" + item.getPrice());
            Log.d("ShowThingsActivity", "Binding view for item: " + item.getName());
            holder.textView.setText(item.getName());
            holder.idTextView.setText("商品id：" + item.getItemID());

            // 解码Base64图片字符串
            if (item.getImageData() != null && !item.getImageData().isEmpty()) {
                // 检查数据是否以Base64前缀开始
                if (item.getImageData().startsWith("data:image/jpeg;base64,")) {
                    String base64Image = item.getImageData().substring("data:image/jpeg;base64,".length());
                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    holder.imageView.setImageBitmap(decodedBitmap);
                    Log.d("ShowThingsActivity", "Image decoded successfully for item: " + item.getName());
                } else {
                    Log.d("ShowThingsActivity", "Image data for item: " + item.getName() + " does not start with expected prefix.");
                    holder.imageView.setImageDrawable(null); // 未能按预期解析数据，不显示图片
                }
            } else {
                holder.imageView.setImageDrawable(null); // 没有图片数据，不显示图片
                Log.d("ShowThingsActivity", "No image data available for item: " + item.getName());
            }
        }

        @Override
        //返回数据项数：
        public int getItemCount() {
            return dataList.size();
        }

        // 缓存列表项视图中的控件引用
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ImageView imageView;
            TextView idTextView; // 新增价格TextView

            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.name_text_view);
                imageView = itemView.findViewById(R.id.image_view);
                idTextView = itemView.findViewById(R.id.price_text_view); // 绑定价格TextView
            }
        }
    }
}

