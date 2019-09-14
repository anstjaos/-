package com.example.lg.blockchainui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

public class MemberInfoActivity extends AppCompatActivity {
    String department;
    String department2;

    EditText etPassword;
    EditText etCheckPassword;
    String kakao_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("회원 정보");
        setSupportActionBar(toolbar);

        final EditText etName = (EditText) findViewById(R.id.info_nameText);
        etPassword = (EditText) findViewById(R.id.info_passwordText);
        etCheckPassword = (EditText) findViewById(R.id.info_passwordCheckText);
        final EditText etStNum = (EditText) findViewById(R.id.info_studentIdText);

        final RadioButton yes = (RadioButton) findViewById(R.id.info_yesButton);
        final RadioButton no = (RadioButton) findViewById(R.id.info_noButton);

        final Spinner spinner = (Spinner) findViewById(R.id.info_departmentSpinner);
        final Spinner spinner2 = (Spinner) findViewById(R.id.info_departmentSpinner2);

        final RadioButton studentBtn = (RadioButton) findViewById(R.id.info_studentButton);
        final RadioButton professorBtn = (RadioButton) findViewById(R.id.info_professorButton);
        final RadioButton graduaateBtn = (RadioButton) findViewById(R.id.info_graduateStudentButton);
        final RadioButton employeeBtn = (RadioButton) findViewById(R.id.info_employeeButton);

        Button changeButton = (Button) findViewById(R.id.info_changeButton);
        Button cancelButton = (Button) findViewById(R.id.info_cancelButton);
        Button deleteButton = (Button) findViewById(R.id.deleteButton);

        Intent intent = getIntent();
        kakao_id = intent.getExtras().getString("kakao_id");


        try {
            String result;
            CustomTask task = new CustomTask();
            result = task.execute(String.valueOf(kakao_id)).get(); // 사용자정보 받아옴
            String result1[] = result.split("&");  // 사용자정보 &로 구분하여 자름

            etName.setText(result1[1]);
            //etPassword.setText(result1[2]);
            //etCheckPassword.setText(result1[2]);
            etStNum.setText(result1[3]);
            if(result1[4].equals("0")) {
                no.setChecked(true);
                spinner2.setVisibility(View.INVISIBLE);
            }
            else{
                yes.setChecked(true);
                spinner2.setVisibility(View.VISIBLE);
            }

            if(result1[7].equals("1")){
                studentBtn.setChecked(true);
            }
            else if(result1[7].equals("2")){
                professorBtn.setChecked(true);
            }
            else if(result1[7].equals("3")){
                employeeBtn.setChecked(true);
            }
            else if(result1[7].equals("4")){
                graduaateBtn.setChecked(true);
            }

            switch(result1[5])
            {
                case "컴퓨터소프트웨어공학":
                    spinner.setSelection(0);
                    break;
                case "컴퓨터공학":
                    spinner.setSelection(1);
                    break;
                case "전자공학":
                    spinner.setSelection(2);
                    break;
                case "기계공학":
                    spinner.setSelection(3);
                    break;
                case "신소재공학":
                    spinner.setSelection(4);
                    break;
                case "토목공학":
                    spinner.setSelection(5);
                    break;
            }

            switch(result1[6])
            {
                case "컴퓨터소프트웨어공학":
                    spinner2.setSelection(0);
                    break;
                case "컴퓨터공학":
                    spinner2.setSelection(1);
                    break;
                case "전자공학":
                    spinner2.setSelection(2);
                    break;
                case "기계공학":
                    spinner2.setSelection(3);
                    break;
                case "신소재공학":
                    spinner2.setSelection(4);
                    break;
                case "토목공학":
                    spinner2.setSelection(5);
                    break;
                case "":
                    break;
            }

        } catch (Exception e) {

        }

        studentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                professorBtn.setChecked(false);
                employeeBtn.setChecked(false);
                graduaateBtn.setChecked(false);
            }
        });

        professorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                studentBtn.setChecked(false);
                employeeBtn.setChecked(false);
                graduaateBtn.setChecked(false);
            }
        });

        employeeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                studentBtn.setChecked(false);
                professorBtn.setChecked(false);
                graduaateBtn.setChecked(false);
            }
        });

        graduaateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                studentBtn.setChecked(false);
                employeeBtn.setChecked(false);
                professorBtn.setChecked(false);
            }
        });

        yes.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                spinner2.setVisibility(View.VISIBLE);
            }
        });

        no.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                spinner2.setVisibility(View.INVISIBLE);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                department = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                department2 = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                String password = etPassword.getText().toString();
                String sha1 = "";
                String checkPassword = etCheckPassword.getText().toString();
                String stNum = etStNum.getText().toString();
                String multiMajor = "";
                String type = "";
                while (true) {
                    if (yes.isChecked() == true) {
                        multiMajor = yes.getText().toString();
                        department2 = spinner2.getSelectedItem().toString();
                    }
                    else if (no.isChecked() == true) {
                        multiMajor = no.getText().toString();
                        department2 = "";
                    }
                    else
                        multiMajor = "";

                    if (studentBtn.isChecked() == true)
                        type = studentBtn.getText().toString();
                    else if (employeeBtn.isChecked() == true)
                        type = employeeBtn.getText().toString();
                    else if (graduaateBtn.isChecked() == true)
                        type = graduaateBtn.getText().toString();
                    else if (professorBtn.isChecked() == true)
                        type = professorBtn.getText().toString();
                    else
                        type = "";

                    if (name.equals("") || password.equals("") || checkPassword.equals("") || stNum.equals("") || multiMajor.equals("") || type.equals("")) {
                        Toast.makeText(MemberInfoActivity.this, "미 입력 정보가 있습니다.", Toast.LENGTH_SHORT).show();
                        break;
                    } else if (password.equals(checkPassword) == false) {
                        Toast.makeText(MemberInfoActivity.this, "비밀번호가 다릅니다.", Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        if (department.equals(department2)) {
                            Toast.makeText(MemberInfoActivity.this, "학과가 중복됩니다.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        try {
                            MessageDigest digest = MessageDigest.getInstance("SHA-1");
                            digest.reset();
                            digest.update(password.getBytes("utf8"));
                            sha1 = String.format("%040x", new BigInteger(1, digest.digest()));
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        try {
                            String result;
                            updateCustomTask task = new updateCustomTask();
                            result = task.execute(kakao_id, name, sha1, stNum, multiMajor, department, department2, type).get();
                            String result1[] = result.split(",");
                            Log.i("리턴 값", result1[1]);

                            if (result1[1].equals("stNumfail") || result1[1].equals("fail")) {
                                Toast.makeText(MemberInfoActivity.this, "중복된 정보가 있습니다..", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else if(result1[1].equals("pwdfail")) {
                                Toast.makeText(MemberInfoActivity.this, "비밀번호가 맞지 않습니다...", Toast.LENGTH_SHORT).show();
                                break;
                            } else {
                                Intent mainIntent = new Intent(MemberInfoActivity.this, MainActivity.class);
                                mainIntent.putExtra("kakao_id",kakao_id);
                                MemberInfoActivity.this.startActivity(mainIntent);
                                break;
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String result;
                    deleteCustomTask task = new deleteCustomTask();
                    result = task.execute(kakao_id).get();
                    String result1[] = result.split(",");
                    Log.i("리턴 값", result1[1]);

                    if (result1[1].equals("fail")) {
                        Toast.makeText(MemberInfoActivity.this, "회원 탈퇴 실패", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MemberInfoActivity.this, "회원 탈퇴 완료", Toast.LENGTH_SHORT).show();
                        Intent mainIntent = new Intent(MemberInfoActivity.this, LogInActivity.class);
                        MemberInfoActivity.this.startActivity(mainIntent);
                    }
                } catch (Exception e) {

                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(MemberInfoActivity.this, MainActivity.class);
                mainIntent.putExtra("kakao_id",kakao_id);
                MemberInfoActivity.this.startActivity(mainIntent);
            }
        });
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/UserInfo.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "kakao_id=" + strings[0];
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

    class updateCustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/userUpdate.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "kakao_id=" + strings[0] + "&name=" + strings[1] + "&pwd=" + strings[2] + "&stNum=" + strings[3] + "&multiMajor=" + strings[4] + "&department=" + strings[5] + "&department2=" + strings[6] + "&type=" + strings[7];
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

    class deleteCustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/userDelete.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "kakao_id=" + strings[0];
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