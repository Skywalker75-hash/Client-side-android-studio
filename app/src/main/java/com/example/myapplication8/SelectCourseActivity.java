package com.example.myapplication8;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SelectCourseActivity extends AppCompatActivity {

    private RecyclerView coursesRecyclerView;
    private CourseAdapter courseAdapter;
    private ProgressBar loadingProgressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_course);

        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));//回收视图线性分布
        courseAdapter = new CourseAdapter(new ArrayList<>());
        coursesRecyclerView.setAdapter(courseAdapter);//给回收视图设置适配器

        new FetchCoursesTask().execute();

        int spanCount = 2; // 列数
        int spacing = 10; // 间隔大小

        coursesRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));

        //获取从MenuActivity传递过来的用户名
        String username = getIntent().getStringExtra("username");

        Button SelectCourseButton=findViewById(R.id.SelectCourseButton);
        //选课按钮点击事件：
        SelectCourseButton.setOnClickListener(new View.OnClickListener() {//new View是匿名类
            @Override
            public void onClick(View v) {
                //从输入框中获取输入的课程号
                String CourseCode = ((EditText) findViewById(R.id.selectCourseInput)).getText().toString();
                //启动选课异步任务
                new SelectCoursesTask().execute(username,CourseCode);
            }
        });
        //返回按钮：
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 结束当前Activity，返回上一个Activity
                finish();
            }
        });
    }

    //展示课程：
    private class FetchCoursesTask extends AsyncTask<Void, Void, List<Course>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingProgressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected List<Course> doInBackground(Void... voids) {

            List<Course> courseList = new ArrayList<>();//用一个动态数组courseList来存储Course数据
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create("", JSON);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/getCourses")
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                //解析最外层的JSONObject
                JSONObject jsonObject = new JSONObject(responseBody);
                //从最外层的JSONObject中获取名为"data"的JSONArray
                JSONArray data = jsonObject.getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject courseObject = data.getJSONObject(i);
                    Course course = new Course(
                            courseObject.getString("CourseName"),
                            courseObject.getString("CourseCode"),
                            courseObject.getInt("CreditHours"),
                            courseObject.getString("Department"),
                            courseObject.getString("ClassTime")
                    );
                    courseList.add(course);
                }

            } catch (Exception e) {

            }
            return courseList;          //返回courseList动态数组
        }


        @Override
        protected void onPostExecute(List<Course> courses) {

            super.onPostExecute(courses);
            loadingProgressBar.setVisibility(View.GONE);
            if (courses != null ) {

                courseAdapter.updateCourses(courses);
            } else {
                Toast.makeText(SelectCourseActivity.this, "显示课程失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // 确保你有 Course 和 CourseAdapter 类
    public class Course {
        private String courseName;
        private String courseCode;
        private int creditHours;
        private String department;
        private String classTime; // 添加的新字段

        //更新构造函数以接受新的参数
        public Course(String courseName, String courseCode, int creditHours, String department, String classTime) {
            this.courseName = courseName;
            this.courseCode = courseCode;
            this.creditHours = creditHours;
            this.department = department;
            this.classTime = classTime; // 正确初始化新字段
        }

        //获取数据元素的函数
        public String getCourseName() {
            return courseName;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public int getCreditHours() {
            return creditHours;
        }

        public String getDepartment() {
            return department;
        }

        public String getClassTime() {
            return classTime;
        }


    }


    //适配器类
    public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
        private List<Course> courseList;
        //构造函数
        public CourseAdapter(List<Course> courseList) {
            this.courseList = courseList;
        }
        //更新数据
        public void updateCourses(List<Course> courses) {

            this.courseList.clear();
            this.courseList.addAll(courses);
            notifyDataSetChanged();
        }
        //计算有多少项需要显示
        @Override
        public int getItemCount() {
            return courseList.size();
        }

        @NonNull
        @Override
        //为每项数据创建视图
        public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_item, parent, false);
            return new CourseViewHolder(view);
        }

        @Override
        //将数据绑定到视图上
        public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
            Course course = courseList.get(position);
            holder.courseNameTextView.setText(course.getCourseName());
            holder.courseCodeTextView.setText(course.getCourseCode());
            holder.creditHoursTextView.setText(String.valueOf(course.getCreditHours()));
            holder.departmentTextView.setText(course.getDepartment());
            holder.classTimeTextView.setText(course.getClassTime());
        }


        public  class CourseViewHolder extends RecyclerView.ViewHolder {
            TextView courseNameTextView;
            TextView courseCodeTextView;
            TextView creditHoursTextView;
            TextView departmentTextView;
            TextView classTimeTextView;

            public CourseViewHolder(View itemView) {
                super(itemView);
                courseNameTextView = itemView.findViewById(R.id.courseNameTextView);
                courseCodeTextView = itemView.findViewById(R.id.courseCodeTextView);
                creditHoursTextView = itemView.findViewById(R.id.creditHoursTextView);
                departmentTextView = itemView.findViewById(R.id.departmentTextView);
                classTimeTextView = itemView.findViewById(R.id.classTimeTextView);
            }
        }
    }
    //选课：
    private class SelectCoursesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String CourseCode = params[1];

            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("username", username);
                jsonParam.put("courseCode", CourseCode);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonParam.toString(), JSON);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/selectCourses")
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
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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



