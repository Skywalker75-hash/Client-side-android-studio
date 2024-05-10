package com.example.myapplication8;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.Arrays;
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

        new ShowCoursesTask().execute();

        int spanCount = 2; // 列数
        int spacing = 10; // 间隔大小

        coursesRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));

        Button showSchoolCoursesButton=findViewById(R.id.ChooseSchoolButton);//根据学院查询选课的按钮
        //根据学院信息查询课程信息的按钮点击事件
        showSchoolCoursesButton.setOnClickListener(new View.OnClickListener() {//new View是匿名类
            @Override
            public void onClick(View v) {
                String department = ((EditText) findViewById(R.id.School)).getText().toString();//输入的学院数据
                new ShowCoursesByDepartmentTask().execute(department);
            }
        });

        //获取从MenuActivity传递过来的用户名
        String username = getIntent().getStringExtra("username");

        Button SelectCourseButton=findViewById(R.id.SelectCourseButton);
        //选课按钮点击事件：
        SelectCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> selectedCourseCodes = new ArrayList<>();
                for (Course course : courseAdapter.getCourseList()) {
                    if (course.isSelected()) {
                        selectedCourseCodes.add(course.getCourseCode());
                    }
                }
                if (!selectedCourseCodes.isEmpty()) {
                    String courseCodesString = TextUtils.join(",", selectedCourseCodes);
                    new SelectCoursesTask().execute(username, courseCodesString);
                } else {
                    Toast.makeText(getApplicationContext(), "请选择至少一门课程", Toast.LENGTH_SHORT).show();
                }
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
    //根据学院查询开设课程：
    private class ShowCoursesByDepartmentTask extends AsyncTask<String, Void, List<Course>> {
        private Exception taskException = null; // 用于记录doInBackground中发生的异常

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Course> doInBackground(String... departmentNames) {
            List<Course> courseList = new ArrayList<>(); // 用一个动态数组courseList来存储Course数据
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("department", departmentNames[0]); // 假设第一个参数是学院名称
                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/showSchoolCourses")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                JSONObject responseJson = new JSONObject(responseBody);
                JSONArray data = responseJson.getJSONArray("data");
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
                Toast.makeText(SelectCourseActivity.this, "出现异常", Toast.LENGTH_SHORT).show();

            }
            return courseList; // 返回courseList动态数组
        }

        @Override
        protected void onPostExecute(List<Course> courses) {
            loadingProgressBar.setVisibility(View.GONE);
           if (courses == null ) {
                // 处理无数据情况
                Toast.makeText(SelectCourseActivity.this, "未找到课程信息", Toast.LENGTH_SHORT).show();
            } else {
                // 成功加载数据
                courseAdapter.updateCourses(courses);
            }
        }
    }
    //选课：
    private class SelectCoursesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String selectedCourseCodes = params[1]; // 选中的课程代码，逗号分隔

            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("username", username);
                jsonParam.put("courseCodes", new JSONArray(Arrays.asList(selectedCourseCodes.split(","))));

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonParam.toString(), JSON);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/selectCourses")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                return response.body().string();
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
                    String message = jsonObj.optString("message", "Error");
                    Toast.makeText(SelectCourseActivity.this, message, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(SelectCourseActivity.this, "错误，未能成功解析", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SelectCourseActivity.this, "收到空响应", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //展示课程：
    private class ShowCoursesTask extends AsyncTask<Void, Void, List<Course>> {
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
                Toast.makeText(SelectCourseActivity.this, "出现异常", Toast.LENGTH_SHORT).show();
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

    public class Course {
        private String courseName;
        private String courseCode;
        private int creditHours;
        private String department;
        private String classTime;
        private boolean isSelected;  // 添加标记是否被选中

        public Course(String courseName, String courseCode, int creditHours, String department, String classTime) {
            this.courseName = courseName;
            this.courseCode = courseCode;
            this.creditHours = creditHours;
            this.department = department;
            this.classTime = classTime;
            this.isSelected = false;  // 默认未选中
        }


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

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }

    public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
        private List<Course> courseList;

        public CourseAdapter(List<Course> courseList) {
            this.courseList = courseList;
        }
        public List<Course> getCourseList() {
            return courseList;
        }
        @NonNull
        @Override
        public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_item, parent, false);
            return new CourseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
            Course course = courseList.get(position);
            holder.courseNameTextView.setText(course.getCourseName());
            holder.courseCodeTextView.setText(course.getCourseCode());
            holder.creditHoursTextView.setText(String.valueOf(course.getCreditHours()));
            holder.departmentTextView.setText(course.getDepartment());
            holder.classTimeTextView.setText(course.getClassTime());
            holder.checkBoxSelectCourse.setChecked(course.isSelected());
            holder.checkBoxSelectCourse.setOnCheckedChangeListener((buttonView, isChecked) -> {
                courseList.get(holder.getAdapterPosition()).setSelected(isChecked);
            });
        }

        @Override
        public int getItemCount() {
            return courseList.size();
        }

        //更新数据的方法
        public void updateCourses(List<Course> courses) {
            courseList.clear(); // 清除当前列表
            courseList.addAll(courses); // 添加新数据
            notifyDataSetChanged(); // 通知数据已改变
        }

        public class CourseViewHolder extends RecyclerView.ViewHolder {
            TextView courseNameTextView;
            TextView courseCodeTextView;
            TextView creditHoursTextView;
            TextView departmentTextView;
            TextView classTimeTextView;
            CheckBox checkBoxSelectCourse;

            public CourseViewHolder(View itemView) {
                super(itemView);
                courseNameTextView = itemView.findViewById(R.id.courseNameTextView);
                courseCodeTextView = itemView.findViewById(R.id.courseCodeTextView);
                creditHoursTextView = itemView.findViewById(R.id.creditHoursTextView);
                departmentTextView = itemView.findViewById(R.id.departmentTextView);
                classTimeTextView = itemView.findViewById(R.id.classTimeTextView);
                checkBoxSelectCourse = itemView.findViewById(R.id.checkBoxSelectCourse);
            }
        }
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

