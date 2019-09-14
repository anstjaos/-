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
import android.widget.RadioGroup;
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

public class RegisterActivity extends AppCompatActivity {
    String department;
    String department2;

    EditText etPassword;
    EditText etCheckPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("회원 등록");
        setSupportActionBar(toolbar);

        // 테이블에 있는 모든 데이터 출력

        final EditText etName = (EditText) findViewById(R.id.nameText);
        etPassword = (EditText) findViewById(R.id.passwordText);
        etCheckPassword = (EditText) findViewById(R.id.passwordCheckText);
        final EditText etStNum = (EditText) findViewById(R.id.studentIdText);

        final RadioButton yes = (RadioButton) findViewById(R.id.yesButton);
        final RadioButton no = (RadioButton) findViewById(R.id.noButton);

        final Spinner spinner = (Spinner) findViewById(R.id.departmentSpinner);
        final Spinner spinner2 = (Spinner) findViewById(R.id.departmentSpinner2);

        final RadioButton studentBtn = (RadioButton) findViewById(R.id.studentButton);
        final RadioButton professorBtn = (RadioButton) findViewById(R.id.professorButton);
        final RadioButton graduaateBtn = (RadioButton) findViewById(R.id.graduateStudentButton);
        final RadioButton employeeBtn = (RadioButton) findViewById(R.id.employeeButton);

        Button registerButton = (Button) findViewById(R.id.registerButton);
        Button cancelButton = (Button) findViewById(R.id.cancelButton);

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

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner2.setVisibility(View.VISIBLE);
                department2 = spinner2.getSelectedItem().toString();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner2.setVisibility(View.INVISIBLE);
                department2 = "";
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent logInIntent = new Intent(RegisterActivity.this, LogInActivity.class);
                RegisterActivity.this.startActivity(logInIntent);
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


        // DB에 데이터 추가
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();

                String kakao_id = String.valueOf(intent.getExtras().getLong("kakao_id"));
                String name = etName.getText().toString();
                String password = etPassword.getText().toString();
                String sha1 = "";
                String checkPassword = etCheckPassword.getText().toString();
                String stNum = etStNum.getText().toString();
                String multiMajor = "";
                String type = "";
                while (true) {
                    if (yes.isChecked() == true)
                        multiMajor = yes.getText().toString();
                    else if (no.isChecked() == true)
                        multiMajor = no.getText().toString();
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
                        Toast.makeText(RegisterActivity.this, "미 입력 정보가 있습니다.", Toast.LENGTH_SHORT).show();
                        break;
                    } else if (password.equals(checkPassword) == false) {
                        Toast.makeText(RegisterActivity.this, "비밀번호가 다릅니다.", Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        if (department.equals(department2)) {
                            Toast.makeText(RegisterActivity.this, "학과가 중복됩니다.", Toast.LENGTH_SHORT).show();
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
                            CustomTask task = new CustomTask();
                            //result = task.execute(kakao_id, name, password, stNum, multiMajor, department, department2, type).get();
                            result = task.execute(kakao_id, name, sha1, stNum, multiMajor, department, department2, type).get();
                            String result1[] = result.split(",");
                            //result = task.execute("rain48333","123433","133", "133", "133", "133", "133", "133").get();
                            Log.i("리턴 값", result1[1]);

                            if (result1[1].equals("stNumfail") || result1[1].equals("fail")) {
                                Toast.makeText(RegisterActivity.this, "중복된 정보가 있습니다..", Toast.LENGTH_SHORT).show();
                                break;
                            } else {
                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.putExtra("kakao_id",kakao_id);
                                RegisterActivity.this.startActivity(mainIntent);
                                break;
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }
        });
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/userRegister.jsp");
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
}