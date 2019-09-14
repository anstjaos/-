package com.example.lg.blockchainui;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SetFragmentData{

    private final int FRAGMENT1 = 1;
    private final int FRAGMENT2 = 2;
    private final int FRAGMENT3 = 3;
    private Button bt_tab1, bt_tab2, bt_tab3;
    String kakao_id;
    String userName;
    String approval;
    VoteObject vot;
    SurveyObject sot;
    String title = "K-VnS";
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 위젯에 대한 참조
        bt_tab1 = (Button)findViewById(R.id.bt_tab1);
        bt_tab2 = (Button)findViewById(R.id.bt_tab2);
        bt_tab3 = (Button)findViewById(R.id.bt_tab3);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);

        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        kakao_id = intent.getExtras().getString("kakao_id");
        userName = intent.getExtras().getString("name");

        // 탭 버튼에 대한 리스너 연결
        bt_tab1.setOnClickListener(this);
        bt_tab2.setOnClickListener(this);
        bt_tab3.setOnClickListener(this);

        // 임의로 액티비티 호출 시점에 어느 프레그먼트를 프레임레이아웃에 띄울 것인지를 정함
        callFragment(FRAGMENT2);

        load();
        surveyLoad();
        getApproval();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_tab1 :
                // '버튼1' 클릭 시 '프래그먼트1' 호출
                callFragment(FRAGMENT1);
                break;

            case R.id.bt_tab2 :
                // '버튼2' 클릭 시 '프래그먼트2' 호출
                callFragment(FRAGMENT2);
                break;

            case R.id.bt_tab3 :
                callFragment(FRAGMENT3);
                break;
        }
    }

    private void callFragment(int frament_no){

        // 프래그먼트 사용을 위해
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (frament_no){
            case 1:
                // '프래그먼트1' 호출 설문조사 메인 프래그먼트
                if(approval.equals("0")) {
                    Toast.makeText(this, "사용 할 수 없는 기능입니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    SurveyMainFragment fragment1 = new SurveyMainFragment();
                    fragment1.setKakao_id(kakao_id);
                    transaction.replace(R.id.main_fragment_container, fragment1);
                    transaction.commit();
                }
                break;

            case 2:
                // '프래그먼트2' 호출 홈 프래그먼트
                HomeFragment fragment2 = new HomeFragment();
                fragment2.setKakao_id(kakao_id);
                transaction.replace(R.id.main_fragment_container, fragment2);
                transaction.commit();
                break;

            case 3: // 투표 메인 프래그먼트
                if(approval.equals("0")) {
                    Toast.makeText(this, "사용 할 수 없는 기능입니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    VoteMainFragment fragment3 = new VoteMainFragment();
                    fragment3.setKakao_id(kakao_id);
                    transaction.replace(R.id.main_fragment_container, fragment3);
                    transaction.commit();
                }
                break;

            case 4: // 투표 등록 프래그먼트
                VoteRegisterFragment fragment4 = new VoteRegisterFragment();
                fragment4.setKakao_id(kakao_id);
                transaction.replace(R.id.main_fragment_container,fragment4);
                transaction.commit();
                break;

            case 5: // 투표참여 프래그먼트
                VoteParticipationFragment fragment5 = new VoteParticipationFragment();
                fragment5.setKakao_id(kakao_id);
                transaction.replace(R.id.main_fragment_container, fragment5);
                transaction.commit();
                break;

            case 6: // 투표 결과 프래그먼트
                VoteResultFragment fragment6 = new VoteResultFragment();
                transaction.replace(R.id.main_fragment_container, fragment6);
                transaction.commit();
                break;

            case 7: //설문조사 등록 프래그먼트
                SurveyRegisterFragment fragment7 = new SurveyRegisterFragment();
                fragment7.setKakao_id(kakao_id);
                transaction.replace(R.id.main_fragment_container, fragment7);
                transaction.commit();
                break;

            case 8: // 투표참여 프래그먼트
                SurveyParticipationFragment fragment8 = new SurveyParticipationFragment();
                fragment8.setKakao_id(kakao_id);
                transaction.replace(R.id.main_fragment_container, fragment8);
                transaction.commit();
                break;

            case 9: // 투표 결과 프래그먼트
                SurveyResultFragment fragment9 = new SurveyResultFragment();
                transaction.replace(R.id.main_fragment_container, fragment9);
                transaction.commit();
                break;
        }
    }

    private void callActivity(int activity_no) {
        switch (activity_no) {
            case 1: { //사용자 승인
                if(approval.equals("0") || approval.equals("1") || approval.equals("3"))
                {
                        Toast.makeText(this, "사용 할 수 없는 기능입니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent it1 = new Intent(MainActivity.this, UserAssignmentActivity.class);
                    it1.putExtra("kakao_id", kakao_id);
                    MainActivity.this.startActivity(it1);
                }
                break;
            }
            case 2: { // 관리자 임명
                if(approval.equals("0") || approval.equals("1") || approval.equals("3"))
                {
                    Toast.makeText(this, "사용 할 수 없는 기능입니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent it2 = new Intent(MainActivity.this, ManagerAppointmentActivity.class);
                    it2.putExtra("kakao_id", kakao_id);
                    MainActivity.this.startActivity(it2);
                }
                break;
            }
            case 3: { // 이거누르면 관리자임명 리스트에 추가
                if(approval.equals("0"))
                {
                    Toast.makeText(this, "사용 할 수 없는 기능입니다.", Toast.LENGTH_SHORT).show();
                }
                else if(approval.equals("3"))
                {
                    Toast.makeText(this, "이미 신청 하셨습니다..", Toast.LENGTH_SHORT).show();
                }
                else if(approval.equals("4"))
                {
                    Toast.makeText(this, "사용 할 수 없는 기능입니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                        String result;
                        ManagerTask task = new ManagerTask();
                        result = task.execute(kakao_id).get();
                        String[] result1 = result.split(",");
                        if (result1[1].equals("true")) {
                            Toast.makeText(this, "신청 완료.", Toast.LENGTH_SHORT).show();
                            approval = "3";
                            break;
                        } else {
                            Toast.makeText(this, "신청 실패.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {

                    }
                }
                break;
            }
            case 4: { // 회원 정보
                Intent it3 = new Intent(MainActivity.this, MemberInfoActivity.class);
                it3.putExtra("kakao_id", kakao_id);
                MainActivity.this.startActivity(it3);
                break;
            }
        }
    }

    @Override
    public void setFragmentData(int fragment_no) {
        callFragment(fragment_no);
    }

    public void setActivityData(int Activity_no) {
        callActivity(Activity_no);
    }
    public String getUserName(){return userName;}
    // 리스트뷰1 추가
    String name = "1"; // null 값 방지
    String endTime = "1"; // null 값 방지
    ArrayList<HashMap<String, String>> votedNameAndEndDateList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> votingNameAndEndDateList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;
    HashMap<String, String> map2;

    public String getName(){return name;}
    public String getEndTime(){return endTime;}

    public void setVotingData(String name, String endTime){
        String currentDate = String.valueOf(System.currentTimeMillis() / 1000);
        long edDate = Long.valueOf(endTime) * 1000;
        Date date = new Date(edDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String edTime = String.valueOf(cal.get(Calendar.YEAR)) + "년" + String.valueOf(cal.get(Calendar.MONTH) + 1) + "월" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + "일";

        if(currentDate.compareTo(endTime) >= 0) {
            map = new HashMap<String, String>();
            this.name = name;
            this.endTime = edTime;
            map.put("item1", this.name);
            map.put("item2", this.endTime);
            votedNameAndEndDateList.add(map);
        }
        else {
            map2 = new HashMap<String, String>();
            this.name = name;
            this.endTime = edTime;
            map2.put("item1", this.name);
            map2.put("item2", this.endTime);
            votingNameAndEndDateList.add(map2);
        }
    }
    public ArrayList<HashMap<String, String>> getVotedNameAndEndDateList(){return votedNameAndEndDateList;}
    public ArrayList<HashMap<String, String>> getVotingNameAndEndDateList(){return votingNameAndEndDateList;}

    public class VoteObject{ //투표정보를 담고있는 객체데스네

        String voteNum; String name; String registerDate; String endDate; String description;String item; ArrayList<String> itemList = new ArrayList();
        int count = 0;

        VoteObject(String voteNum, String name,String registerDate,String endDate,String description)
        {
            this.voteNum = voteNum; this.name = name; this.registerDate = registerDate; this.endDate = endDate; this.description = description;
        }

        String getVoteNum(){return voteNum;}
        String getName(){return name;}
        String getRegisterDate(){return registerDate;}
        String getEndDate() {return endDate;}
        String getDescription() {return description;}
        void setItemList(String item){itemList.add(item); count++;}
        ArrayList<String> getItemList(){return itemList;}
        int getCount() {return count;}
    }

    VoteObject vo;
    ArrayList<VoteObject> voteObjectList = new ArrayList<VoteObject>();
    ArrayList<VoteObject> endVoteList = new ArrayList<VoteObject>();


    public ArrayList<VoteObject> getVoteList() {return voteObjectList;}
    public ArrayList<VoteObject> getEndVoteList() {return endVoteList;}
    public void setVoteObject(VoteObject vo){
        this.vo = vo;
        voteObjectList.add(vo);
    }
    public void removeVoteObject(int index){
        voteObjectList.remove(index);
    }
    public void removeSurveyObject(int index){ surveyObjectList.remove(index);}
    public void setEndVoteObject(VoteObject vo){
        this.vo = vo;
        endVoteList.add(vo);
    }
    public VoteObject getVoteObject(){return vo;}


    int listPosition = 0; // 선택된 리스트가 몇번째아이템인지 알려줌
    public void setPosition(int i){listPosition = i;}
    public int getPosition(){return listPosition;}

    public Toolbar getToolbar(){return toolbar; } //툴바 반환

    public interface OnBackPressedListener {
        public void onBack();
    }
    private OnBackPressedListener mBackListener;

    public void setOnBackPressedListener(OnBackPressedListener listener) {
        mBackListener = listener;
    }

    // 리스트뷰1 추가
    String surveyName = "1"; // null 값 방지
    String surveyEndTime = "1"; // null 값 방지
    ArrayList<HashMap<String, String>> surveyedNameAndEndDateList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> surveyingNameAndEndDateList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> surveyMap;
    HashMap<String, String> surveyMap2;

    public String getSurveyName() {
        return surveyName;
    }

    public String getSurveyEndTime() {
        return surveyEndTime;
    }

    public void setSurveyingData(String name, String endTime) {

        String currentDate = String.valueOf(System.currentTimeMillis() / 1000);
        long edDate = Long.valueOf(endTime) * 1000;
        Date date = new Date(edDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String edTime = String.valueOf(cal.get(Calendar.YEAR)) + "년" + String.valueOf(cal.get(Calendar.MONTH) + 1) + "월" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + "일";

        if(currentDate.compareTo(endTime) >= 0) {
            map = new HashMap<String, String>();
            this.surveyName = name;
            this.surveyEndTime = edTime;
            map.put("item1", this.surveyName);
            map.put("item2", this.surveyEndTime);
            surveyedNameAndEndDateList.add(map);
        } else {
            map2 = new HashMap<String, String>();
            this.surveyName = name;
            this.surveyEndTime = edTime;
            map2.put("item1", this.surveyName);
            map2.put("item2", this.surveyEndTime);
            surveyingNameAndEndDateList.add(map2);
        }

    }

    public ArrayList<HashMap<String, String>> getSurveyedNameAndEndDateList() {
        return surveyedNameAndEndDateList;
    }

    public ArrayList<HashMap<String, String>> getSurveyingNameAndEndDateList() {
        return surveyingNameAndEndDateList;
    }

    public class SurveyObject { //설문정보를 담고있는 객체데스네

        String sNum;
        String sName;
        String sRegisterDate;
        String sEndDate;
        String sDescription;
        String sItem;
        ArrayList<String> sItemList = new ArrayList();
        int sCount = 0;

        SurveyObject(String sNum, String sName, String sRegisterDate, String sEndDate, String sDescription) {
            this.sNum = sNum;
            this.sName = sName;
            this.sRegisterDate = sRegisterDate;
            this.sEndDate = sEndDate;
            this.sDescription = sDescription;
        }

        String getSurveyNum() {
            return sNum;
        }

        String getSurveyName() {
            return sName;
        }

        String getSurveyRegisterDate() {
            return sRegisterDate;
        }

        String getSurveyEndDate() {
            return sEndDate;
        }

        String getSurveyDescription() {
            return sDescription;
        }

        void setSurveyItemList(String item) {
            sItemList.add(item);
            sCount++;
        }

        ArrayList<String> getSurveyItemList() {
            return sItemList;
        }

        int getSurveyCount() {
            return sCount;
        }
    }

    SurveyObject so;
    ArrayList<SurveyObject> surveyObjectList = new ArrayList<SurveyObject>();
    ArrayList<SurveyObject> endSurveyList = new ArrayList<SurveyObject>();


    public ArrayList<SurveyObject> getSurveyList() {
        return surveyObjectList;
    }

    public ArrayList<SurveyObject> getEndSurveyList() {
        return endSurveyList;
    }

    public void setSurveyObject(SurveyObject so) {
        this.so = so;
        surveyObjectList.add(so);
    }

    public void setEndSurveyObject(SurveyObject so) {
        this.so = so;
        endSurveyList.add(so);
    }

    public SurveyObject getSurveyObject() {
        return so;
    }


    int surveyListPosition = 0; // 선택된 리스트가 몇번째아이템인지 알려줌

    public void setSurveyPosition(int i) {
        surveyListPosition = i;
    }

    public int getSurveyPosition() {
        return surveyListPosition;
    }


    public void load(){
        try {
            String result;
            CustomTask task = new CustomTask();
            task.execute();
        } catch (Exception e) {

        }
    }

    public void surveyLoad(){
        try{
            String result;
            SurveyTask task = new SurveyTask();
            task.execute();
        } catch(Exception e) {

        }
    }

    public void getApproval() {
        try{
            String result = "";
            ApprovalTask task = new ApprovalTask();
            result = task.execute(kakao_id).get();
            String result1[] = result.split(",");
            approval = result1[1];
            Log.e("approval", approval);
        }catch(Exception e){

        }
    }

    @Override
    public void onBackPressed() {
        // 다른 Fragment 에서 리스너를 설정했을 때 처리됩니다.
        if (mBackListener != null) {
            mBackListener.onBack();
            Log.e("!!!", "Listener is not null");
            // 리스너가 설정되지 않은 상태(예를들어 메인Fragment)라면
            // 뒤로가기 버튼을 연속적으로 두번 눌렀을 때 앱이 종료됩니다.
        }
        else{
            super.onBackPressed();
            finish();
            System.exit(0);
        }
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/voteInfo.jsp");
                sendMsg = "";
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String result1[] = s.split("-");
            for(int i = 1; i < result1.length; i++)
            {
                String result2[] = result1[i].split("&");
                String voteNum = result2[1];
                String voteName = result2[2];
                String registerDate = result2[3];
                String endDate = result2[4];
                String description = result2[5];
                String result3[] = result2[6].split(",");

                String currentDate = String.valueOf(System.currentTimeMillis() / 1000);

                vot = new VoteObject(voteNum, voteName, registerDate, endDate, description);
                for(int j = 1; j < result3.length; j++)
                {
                    vot.setItemList(result3[j]);
                }
                if(currentDate.compareTo(endDate) >= 0) {
                    endVoteList.add(vot);
                }
                else {
                    voteObjectList.add(vot);
                }
                setVotingData(voteName, endDate);
            }
        }
    }

    class ManagerTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/managerRequest.jsp");
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

    class ApprovalTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/getApproval.jsp");
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

    class SurveyTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/surveyInfo.jsp");
                sendMsg = "";
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String result1[] = s.split("-");
            Log.e("s", s);
            for(int i = 1; i < result1.length; i++)
            {
                String result2[] = result1[i].split("&");
                String surveyNum = result2[1];
                String surveyName = result2[2];
                String registerDate = result2[3];
                String endDate = result2[4];
                String description = result2[5];
                String result3[] = result2[6].split(",");

                String currentDate = String.valueOf(System.currentTimeMillis() / 1000);

                sot = new SurveyObject(surveyNum, surveyName, registerDate, endDate, description);
                for(int j = 1; j < result3.length; j++)
                {
                    sot.setSurveyItemList(result3[j]);
                }
                if(currentDate.compareTo(endDate) >= 0) {
                    endSurveyList.add(sot);
                }
                else {
                    surveyObjectList.add(sot);
                }
                setSurveyingData(surveyName, endDate);
            }
        }
    }
}