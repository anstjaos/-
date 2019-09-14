package com.example.lg.blockchainui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class UserAssignmentActivity extends AppCompatActivity {

    Toolbar toolbar;

    Button deleteButton;
    Button assignmentButton;
    Button cancelButton;

    EditText[] itemList;
    LinearLayout itemLayout;
    String kakao_id;
    int userAssignmentCount = 0;
    ArrayList<requestUser> userList = new ArrayList<requestUser>();
    ArrayList<CheckBox> checkBoxList = new ArrayList<CheckBox>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_assignment);
        itemLayout = (LinearLayout) findViewById(R.id.itemLayout);
        deleteButton = (Button) findViewById(R.id.delete_assignment_button);
        assignmentButton = (Button) findViewById(R.id.assignment_button);
        cancelButton = (Button) findViewById(R.id.cancel_assignment_button);
        toolbar = (Toolbar) findViewById(R.id.toolbar_assignment);

        Intent intent = getIntent();
        kakao_id = intent.getExtras().getString("kakao_id");

        load();

        toolbar.setTitle("사용자 승인 신청");

        cancelButton.setOnClickListener(new View.OnClickListener() { //취소버튼
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(UserAssignmentActivity.this, MainActivity.class);
                registerIntent.putExtra("kakao_id", kakao_id);
                UserAssignmentActivity.this.startActivity(registerIntent);
            }
        });

        assignmentButton.setOnClickListener(new View.OnClickListener(){ //사용자로 받아들이기
            @Override
            public void onClick(View v) {
                String result = "";
                for(int i = 0; i < userAssignmentCount; i++) {
                    if (checkBoxList.get(i).isChecked()) {
                        try {
                            CustomTask task = new CustomTask();
                            result = task.execute(userList.get(i).getKakao_id(), "appointment").get();
                            String[] result1 = result.split(",");
                            if (result1[1].equals("true")) {
                                Toast.makeText(UserAssignmentActivity.this, "승인 완료!!", Toast.LENGTH_SHORT).show();
                                Intent registerIntent = new Intent(UserAssignmentActivity.this, MainActivity.class);
                                registerIntent.putExtra("kakao_id", kakao_id);
                                UserAssignmentActivity.this.startActivity(registerIntent);
                            } else {
                                Toast.makeText(UserAssignmentActivity.this, "승인 실패!!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }
        } );
        deleteButton.setOnClickListener(new View.OnClickListener(){ //대기목록에서 삭제!

            @Override
            public void onClick(View v) {
                String result = "";
                for(int i = 0; i < userAssignmentCount; i++){
                    if(checkBoxList.get(i).isChecked()){
                        try{
                            CustomTask task = new CustomTask();
                            result = task.execute(userList.get(i).getKakao_id(), "delete").get();
                            String[] result1 = result.split(",");
                            if(result1[1].equals("true")){
                                userList.remove(i);
                                Toast.makeText(UserAssignmentActivity.this, "삭제 완료!!", Toast.LENGTH_SHORT).show();
                                Intent registerIntent = new Intent(UserAssignmentActivity.this, MainActivity.class);
                                registerIntent.putExtra("kakao_id", kakao_id);
                                UserAssignmentActivity.this.startActivity(registerIntent);
                            }
                            else{
                                Toast.makeText(UserAssignmentActivity.this, "삭제 실패!!", Toast.LENGTH_SHORT).show();
                            }
                        }catch(Exception e){

                        }
                    }
                }
            }
        });

        for(int i = 0; i < userAssignmentCount; i++) { //라디오버튼 동적생성
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            final CheckBox checkBox = new CheckBox(this);

            float mScale = getResources().getDisplayMetrics().density;
            final int width = (int)(355*mScale);

            checkBox.setText(userList.get(i).getKakao_id()+ " " + userList.get(i).getName());; //여다가 사용자 이름 넣기
            checkBox.setWidth(width);
            checkBox.setTextSize(20);

            checkBox.setLayoutParams(params);
            //radioButton.setButtonTintList(colorStateList);
            checkBox.setId(i);
            checkBoxList.add(checkBox);
            itemLayout.addView(checkBox);
        }
    }

    public void load() {
        try {
            String result;
            UserTask task = new UserTask();
            result = task.execute(kakao_id).get();
            String[] result1 = result.split("-");
            for(int i = 1; i < result1.length; i++) {
                String[] result2 = result1[i].split("&");
                String id = result2[1];
                String name = result2[2];
                String stNum = result2[3];
                Log.e("result", result2[3]);
                requestUser member = new requestUser(id, name, stNum);
                userList.add(member); // 0번째부터 담음
                userAssignmentCount++;
            }
        } catch (Exception e) {

        }
    }

    public class requestUser{
        private String kakao_id;
        private String name;
        private String stNum;

        public requestUser(String kakao_id, String name, String stNum) {
            this.kakao_id = kakao_id;
            this.name = name;
            this.stNum = stNum;
        }

        public String getKakao_id(){return kakao_id;}
        public String getName(){return name;}
        public String getStNum(){return stNum;}
    }

    class UserTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/userRequestInfo.jsp");
                sendMsg = "kakao_id=" + strings[0];
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                osw.write(sendMsg);
                osw.flush();
                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();

                } else {
                    Log.i("통신 결과", conn.getResponseCode() + "에러");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/user.jsp");
                sendMsg = "kakao_id=" + strings[0] + "&type=" + strings[1];
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                osw.write(sendMsg);
                osw.flush();
                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();

                } else {
                    Log.i("통신 결과", conn.getResponseCode() + "에러");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }
}
