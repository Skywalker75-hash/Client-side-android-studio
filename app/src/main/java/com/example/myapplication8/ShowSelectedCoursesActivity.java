package com.example.myapplication8;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ShowSelectedCoursesActivity extends AppCompatActivity {

    private RecyclerView coursesRecyclerView;
    private CourseAdapter courseAdapter;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_selected_courses);
        //获取从MenuActivity传递过来的用户名
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseAdapter = new CourseAdapter(new ArrayList<>());
        coursesRecyclerView.setAdapter(courseAdapter);

        // 获取用户名
        String username = getIntent().getStringExtra("username");
        new FetchCoursesTask(username).execute();
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

        // 构造函数、getters 和 setters
        public Course(String courseName, String courseCode, int creditHours, String department, String classTime) {
            this.courseName = courseName;
            this.courseCode = courseCode;
            this.creditHours = creditHours;
            this.department = department;
            this.classTime = classTime;
        }

        public String getCourseName() { return courseName; }
        public String getCourseCode() { return courseCode; }
        public int getCreditHours() { return creditHours; }
        public String getDepartment() { return department; }
        public String getClassTime() { return classTime; }
    }
    public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
        private List<Course> courseList;

        public CourseAdapter(List<Course> courseList) {
            this.courseList = courseList;
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
        }

        @Override
        public int getItemCount() {
            return courseList.size();
        }

        public void updateCourses(List<Course> courses) {
            this.courseList = courses;
            notifyDataSetChanged();
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


}