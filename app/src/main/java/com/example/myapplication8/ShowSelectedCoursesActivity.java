package com.example.myapplication8;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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

public class ShowSelectedCoursesActivity extends AppCompatActivity {

    private RecyclerView coursesRecyclerView;
    private CourseAdapter courseAdapter;
    private ProgressBar loadingProgressBar;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_selected_courses);
        //获取从MenuActivity传递过来的用户名
        username = getIntent().getStringExtra("username");
        //获取页面资源
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseAdapter = new CourseAdapter(new ArrayList<>());
        coursesRecyclerView.setAdapter(courseAdapter);
        Button deletebutton=findViewById(R.id.deletebutton);//删除选课按钮
        deletebutton.setOnClickListener(new View.OnClickListener() {
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
                    new deleteCourseTask().execute(username, courseCodesString);
                } else {
                    Toast.makeText(getApplicationContext(), "请选择至少一门课程", Toast.LENGTH_SHORT).show();
                }
            }
        });


        new FetchCoursesTask(username).execute();
        //返回按钮：
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //结束当前Activity，返回上一个Activity
                finish();
            }
        });

    }
    private class deleteCourseTask extends AsyncTask<String, Void, String> {
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
                        .url("http://10.0.2.2:3000/deleteCourse")
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
                    boolean success = jsonObj.getBoolean("success");
                    Toast.makeText(ShowSelectedCoursesActivity.this, jsonObj.getString("message"), Toast.LENGTH_SHORT).show();
                    if (success) {
                        //删除成功后重新加载课程列表
                        new FetchCoursesTask(username).execute();
                    }
                } catch (JSONException e) {
                    Toast.makeText(ShowSelectedCoursesActivity.this, "错误，未能成功解析", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ShowSelectedCoursesActivity.this, "收到空响应", Toast.LENGTH_SHORT).show();
            }
        }

    }


    public class FetchCoursesTask extends AsyncTask<Void, Void, String> {
        private String username;

        public FetchCoursesTask(String username) {
            this.username = username;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingProgressBar.setVisibility(View.VISIBLE);

        }
        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", username);
                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/showSelectedCourses")
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loadingProgressBar.setVisibility(View.GONE);
            if (s == null) {
                Toast.makeText(ShowSelectedCoursesActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    // 解析并更新UI
                    JSONArray data = jsonObject.getJSONArray("data");
                    List<Course> courses = new ArrayList<>();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject courseObject = data.getJSONObject(i);
                        Course course = new Course(
                                courseObject.getString("CourseName"),
                                courseObject.getString("CourseCode"),
                                courseObject.getInt("CreditHours"),
                                courseObject.getString("Department"),
                                courseObject.getString("ClassTime")
                        );
                        courses.add(course);
                    }
                    courseAdapter.updateCourses(courses);
                } else {
                    String message = jsonObject.getString("message");
                    Toast.makeText(ShowSelectedCoursesActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ShowSelectedCoursesActivity.this, "解析响应失败", Toast.LENGTH_SHORT).show();
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

        // Getters and setters
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
        public void updateCourses(List<Course> courses) {
            this.courseList.clear();  //清空现有数据
            this.courseList.addAll(courses);  //添加新数据
            notifyDataSetChanged();  //通知数据已更新
        }

        @Override
        public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
            Course course = courseList.get(position);
            holder.courseNameTextView.setText(course.getCourseName());
            holder.courseCodeTextView.setText(course.getCourseCode());
            holder.creditHoursTextView.setText(String.valueOf(course.getCreditHours()));
            holder.departmentTextView.setText(course.getDepartment());
            holder.classTimeTextView.setText(course.getClassTime());
            //移除旧的监听器
            holder.checkBoxSelectCourse.setOnCheckedChangeListener(null);
            //设置当前勾选状态
            holder.checkBoxSelectCourse.setChecked(course.isSelected());
            //设置新的监听器
            holder.checkBoxSelectCourse.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //更新课程的选中状态
                courseList.get(holder.getAdapterPosition()).setSelected(isChecked);
            });
        }


        @Override
        public int getItemCount() {
            return courseList.size();
        }

 

        public  class CourseViewHolder extends RecyclerView.ViewHolder {
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