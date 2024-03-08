package com.example.myapplication8;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class classScheduleActivity extends AppCompatActivity {

    private GridView gridView;
    // private List<Course> courseList = new ArrayList<>();
    private List<Course> scheduleList = new ArrayList<>(Collections.nCopies(28, (Course) null));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_schedule);
        gridView = findViewById(R.id.gridView);
        //获取从MenuActivity传递过来的用户名
        String username = getIntent().getStringExtra("username");
        new FetchCoursesTask().execute(username);
    }

    private class FetchCoursesTask extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("username", username);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonParam.toString(), JSON);

                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/classSchedule")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();

                        return responseBody;
                    } else {
                        return null;
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null) {
                parseJSONWithGSON(jsonData);
            }
        }
    }


    private void parseJSONWithGSON(String jsonData) {
        Gson gson = new Gson();
        CourseResponse response = gson.fromJson(jsonData, CourseResponse.class);
        if (response != null && response.success) {
            fillScheduleList(response.data); // 调整填充方法
            updateUI();
        } else {
            Toast.makeText(getApplicationContext(), "错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void fillScheduleList(List<Course> courses) {
        // 清空课程表
        Collections.fill(scheduleList, null);
        for (Course course : courses) {
            int position = mapCourseToPosition(course); // 实现该方法以映射课程到位置
            if (position != -1) {
                scheduleList.set(position, course);
            }
        }
    }
    private int mapCourseToPosition(Course course) {
        String classTime = course.getClassTime();
        if (classTime == null || classTime.length() < 6) {
            return -1; // 格式不符
        }

        try {
            int dayOfWeek = Integer.parseInt(classTime.substring(1, 2)); //
            String classPeriod = classTime.substring(3, 5); //
            int periodIndex;
            switch (classPeriod) {
                case "12":
                    periodIndex = 1;
                    break;
                case "34":
                    periodIndex = 2;
                    break;
                case "56":
                    periodIndex = 3;
                    break;
                case "78":
                    periodIndex = 4;
                    break;
                default:
                    return -1; // 不识别的课程节次
            }
            //由于gridview是横向计数的，所以采用这种算法：
            return (periodIndex - 1) * 5 + (dayOfWeek - 1);
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    private void updateUI() {
        CourseAdapter adapter = new CourseAdapter(this, R.layout.schedule_item, scheduleList);
        gridView.setAdapter(adapter);
        Log.d("updateUI", "UI updated successfully");
    }

    public class Course {
        private String CourseName;
        private String ClassTime;


        public String getCourseName(){return CourseName;}
        public String getClassTime(){return ClassTime;}
        // Getters and Setters
    }
    public class CourseResponse {
        private boolean success;
        private String message;
        private List<Course> data;
    }
    public class CourseAdapter extends ArrayAdapter<Course> {
        private int resourceLayout;
        private Context mContext;

        public CourseAdapter(Context context, int resource, List<Course> courses) {
            super(context, resource, courses);
            this.resourceLayout = resource;
            this.mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.schedule_item, parent, false);
            }
            Course course = getItem(position);
            TextView courseName = convertView.findViewById(R.id.courseName);
            TextView classTime = convertView.findViewById(R.id.classTime);

            if (course != null) {
                courseName.setText(course.getCourseName() != null ? course.getCourseName() : "未知课程");
                classTime.setText(course.getClassTime() != null ? course.getClassTime() : "未知时间");
            } else {
                //如果没有课程
                courseName.setText("");
                classTime.setText("");
            }

            return convertView;
        }

    }


}


