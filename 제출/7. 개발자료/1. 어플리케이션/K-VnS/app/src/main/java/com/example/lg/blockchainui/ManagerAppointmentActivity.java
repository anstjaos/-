package com.example.lg.blockchainui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.ArrayList;
import java.util.HashMap;

public class ManagerAppointmentActivity extends AppCompatActivity {

    Toolbar toolbar;

    Button deleteButton;
    Button appointmentButton;
    Button cancelButton;

    EditText[] itemList;
    LinearLayout itemLayout;
    int appointmentCount = 0;
    String kakao_id;
    ArrayList<requestManager> requestList = new ArrayList<requestManager>();
    ArrayList<CheckBox> checkBoxList = new ArrayList<CheckBox>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_appointment);
        itemLayout = (LinearLayout) findViewById(R.id.manager_itemLayout);
        deleteButton = (Button) findViewById(R.id.delete_manager_button);
        appointmentButton = (Button) findViewById(R.id.manager_appointment_button);
        cancelButton = (Button) findViewById(R.id.manager_cancel_button);
        toolbar = (Toolbar) findViewById(R.id.toolbar_manager_appointment);

        Intent intent = getIntent();
        kakao_id = intent.getExtras().getString("kakao_id");

        load();

        toolbar.setTitle("관리자 임명");

        AlertDialog.Builder ad = new AlertDialog.Builder(ManagerAppointmentActivity.this);

        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() { //취소버튼 눌렀을때
            @Override
            public void onClick(DialogInterface dialog, int which) { //
                dialog.dismiss();     // 다이얼로그 걍 꺼줌
                // Event
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() { //취소버튼
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(ManagerAppointmentActivity.this, MainActivity.class);
                registerIntent.putExtra("kakao_id", kakao_id);
                ManagerAppointmentActivity.this.startActivity(registerIntent);
            }
        });

        appointmentButton.setOnClickListener(new View.OnClickListener() { //사용자로 받아들이기
            @Override
            public void onClick(View view) {
                alertDialogShow();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() { //대기목록에서 삭제!

            @Override
            public void onClick(View v) {
                String result = "";
                for(int i = 0; i < appointmentCount; i++){
                    if(checkBoxList.get(i).isChecked()){
                        try{
                            CustomTask task = new CustomTask();
                            result = task.execute(requestList.get(i).getKakao_id(), "delete").get();
                            String[] result1 = result.split(",");
                            if(result1[1].equals("true")){
                                requestList.remove(i);
                                Toast.makeText(ManagerAppointmentActivity.this, "삭제 완료!!", Toast.LENGTH_SHORT).show();
                                Intent registerIntent = new Intent(ManagerAppointmentActivity.this, MainActivity.class);
                                registerIntent.putExtra("kakao_id", kakao_id);
                                ManagerAppointmentActivity.this.startActivity(registerIntent);
                            }
                            else{
                                Toast.makeText(ManagerAppointmentActivity.this, "삭제 실패!!", Toast.LENGTH_SHORT).show();
                            }
                        }catch(Exception e){

                        }
                    }
                }
            }
        });

        for (int i = 0; i < appointmentCount; i++) { //체크박스 동적생성
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            final CheckBox checkBox = new CheckBox(this);

            float mScale = getResources().getDisplayMetrics().density;
            final int width = (int) (355 * mScale);

            checkBox.setText(requestList.get(i).getKakao_id()+ " " + requestList.get(i).getName()); //여다가 사용자 이름 넣기
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
            ManagerTask task = new ManagerTask();
            result = task.execute(kakao_id).get();
            String[] result1 = result.split("-");
            Log.e("result", result);
            for(int i = 1; i < result1.length; i++) {
                String[] result2 = result1[i].split("&");
                String id = result2[1];
                String name = result2[2];
                String stNum = result2[3];
                requestManager member = new requestManager(id, name, stNum);
                requestList.add(member); // 0번째부터 담음
                appointmentCount++;
            }
        } catch (Exception e) {

        }
    }

    public void alertDialogShow()
    {
        final EditText et = new EditText(this);
        AlertDialog.Builder ad = new AlertDialog.Builder(this);

        et.setTextColor(Color.rgb(0,0,0));
        et.getBackground().setColorFilter(Color.rgb(0,0,0), PorterDuff.Mode.SRC_ATOP);
        ad.setTitle("비밀번호");       // 제목 설정
        ad.setMessage("비밀번호를 입력해주세요!");   // 내용 설정

        ad.setView(et);

        final AlertDialog dialog = ad.create();
        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() { //다이얼로그에서 확인버튼 누를때 발생하는거
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Text 값 받아서 로그 남기기
                String value = et.getText().toString();
                String sha1 = "";
                String result = "";
                String[] result1;
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-1");
                    digest.reset();
                    digest.update(value.getBytes("utf8"));
                    sha1 = String.format("%040x", new BigInteger(1, digest.digest()));
                }catch(Exception e){
                    e.printStackTrace();
                }
                try{
                    CustomTask task = new CustomTask();
                    result = task.execute(kakao_id, "pwd").get();
                    result1 = result.split(",");
                    if(result1[1].equals(sha1)) //입력한 값이 비밀번호와 같을경우
                    {
                        //비활성화된 임명버튼을 활성화(누를 수 있도록 해줌) 처음 xml에서 비활성화 해놓음
                        dialog.dismiss(); //다이얼로그 끄기
                        result = "";
                        result1 = null;
                        for(int i = 0; i < appointmentCount; i++){
                            if(checkBoxList.get(i).isChecked()){
                                try{
                                    task = new CustomTask();
                                    result = task.execute(requestList.get(i).getKakao_id(), "appointment").get();
                                    result1 = result.split(",");
                                    if(result1[1].equals("true")){
                                        Toast.makeText(ManagerAppointmentActivity.this, "임명 완료!!", Toast.LENGTH_SHORT).show();
                                        Intent registerIntent = new Intent(ManagerAppointmentActivity.this, MainActivity.class);
                                        registerIntent.putExtra("kakao_id", kakao_id);
                                        ManagerAppointmentActivity.this.startActivity(registerIntent);
                                    }
                                    else{
                                        Toast.makeText(ManagerAppointmentActivity.this, "임명 실패!!", Toast.LENGTH_SHORT).show();
                                    }
                                }catch(Exception e){

                                }
                            }
                        }
                    }
                    else //입력한 값이 비밀번호와 다를경우
                    {
                        Toast.makeText(ManagerAppointmentActivity.this, "비밀번호가 다릅니다!", Toast.LENGTH_SHORT).show();
                    }
                }catch(Exception e){

                }
                //닫기
                // Event
            }
        });
        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() { //취소버튼 눌렀을때
            @Override
            public void onClick(DialogInterface dialog, int which) { //
                //dialog.dismiss();     // 다이얼로그 걍 꺼줌
                // Event
            }
        });

        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.YELLOW);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.YELLOW);
            }
        });
        ad.show(); ///??????
    }

    public class requestManager{
        private String kakao_id;
        private String name;
        private String stNum;

        public requestManager(String kakao_id, String name, String stNum) {
            this.kakao_id = kakao_id;
            this.name = name;
            this.stNum = stNum;
        }

        public String getKakao_id(){return kakao_id;}
        public String getName(){return name;}
        public String getStNum(){return stNum;}
    }

    class ManagerTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/managerRequestInfo.jsp");
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
                URL url = new URL("http://121.151.244.47:8080/System1/manager.jsp");
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
