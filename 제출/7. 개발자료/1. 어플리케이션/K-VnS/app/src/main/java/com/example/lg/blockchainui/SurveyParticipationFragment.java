package com.example.lg.blockchainui;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class SurveyParticipationFragment extends Fragment implements MainActivity.OnBackPressedListener{
    public static SurveyParticipationFragment newInstance(){
        return new SurveyParticipationFragment();
    }
    boolean isParticipate = false;

    //RadioButton voteListBtn;
    TextView nameText;
    EditText[] itemList;
    Button participateBtn, cancelBtn;
    LinearLayout itemLayout;
    MainActivity.SurveyObject sot;
    SurveyMainFragment surveyMainFragment;
    String kakao_id;
    Toolbar toolbar;

    int radioGroupCount = 0; //라디오그룹 추가될떄마다 ++
    public void setKakao_id(String kakao_id){
        this.kakao_id = kakao_id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_survey_participation,container,false);
        surveyMainFragment = new SurveyMainFragment();
        final Activity root = getActivity();

        //voteListBtn = (RadioButton) view.findViewById(R.id.voting_radio_button);
        nameText = (TextView) view.findViewById(R.id.survey_name);
        participateBtn = (Button) view.findViewById(R.id.surveyPaticipation_button);
        cancelBtn = (Button) view.findViewById(R.id.surveyCancel_paticipation_button);

        itemLayout = (LinearLayout) view.findViewById(R.id.surveying_itemLayout);

        sot = ((MainActivity) getActivity()).getSurveyList().get(((MainActivity) getActivity()).getPosition()); //투표 객체 저장
        toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.setTitle("설문조사참가");

        nameText.setText(sot.getSurveyDescription()); //투표이름
        Log.e("a", String.valueOf(sot.sItemList.size()));

        for(int i = 0; i < sot.sItemList.size(); i++){ //라디오버튼 동적생성

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);


            final TextView textView = new TextView(root.getApplicationContext());
            float mScale = getResources().getDisplayMetrics().density;
            final int width = (int)(355*mScale);
            ColorStateList colorStateList;//라디오 버튼 색깔
            colorStateList = getradioButtonColor();

            itemLayout.setOrientation(LinearLayout.VERTICAL);


            textView.setText(sot.getSurveyItemList().get(i)); //디비에서 땡겨오기 sot.getSurveyItemList().get(i)
            textView.setId(i);
            textView.setWidth(width);
            textView.setTextSize(20);
            textView.setLayoutParams(params);
            textView.setTextColor(Color.rgb(0,0,0));
            itemLayout.addView(textView);

            final RadioGroup radioGroup_item = new RadioGroup(root.getApplicationContext());
            radioGroup_item.setId(i);

            for(int j = 1 ; j < 6;j++)
            {
                final RadioButton radioButton = new RadioButton(root.getApplicationContext());
                if(j == 1)
                {
                    radioButton.setText("매우나쁨");
                }
                else if(j == 2)
                {
                    radioButton.setText("나쁨");
                }
                else if(j == 3)
                {
                    radioButton.setText("보통");
                }
                else if(j == 4)
                {
                    radioButton.setText("좋음");
                }
                else if(j == 5)
                {
                    radioButton.setText("매우좋음");
                }

                Display display = ((WindowManager) root.getApplication().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                radioButton.setId(j);
                radioButton.setWidth(size.x/5);
                radioButton.setTextSize(10);
                radioButton.setTextColor(Color.rgb(0,0,0));
                radioButton.setLayoutParams(params);
                radioButton.setButtonTintList(colorStateList);
                radioGroup_item.addView(radioButton);
                radioGroup_item.setOrientation(RadioGroup.HORIZONTAL);

            }
            itemLayout.addView(radioGroup_item);
            radioGroupCount++;

        }

        cancelBtn.setOnClickListener(new View.OnClickListener() { //취소버튼
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).setFragmentData(1);
            }
        });

        participateBtn.setOnClickListener(new View.OnClickListener(){ // 투표하기 버튼 여기서 값보내기하면댈듯하오하오
            @Override
            public void onClick(View v) {
                try {
                    final JSONObject jsonObject = new JSONObject();
                    String surveyKey = "s" + sot.getSurveyNum();
                    try {
                        jsonObject.put("key", surveyKey);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JSONArray jsonArray = new JSONArray();
                    for(int i = 0 ; i < radioGroupCount; i++) {
                        View view = itemLayout.getChildAt(i*2 + 1);
                        RadioGroup radioGroup = (RadioGroup) view;
                        int checkButtonId = Integer.valueOf(radioGroup.getCheckedRadioButtonId());

                        if( checkButtonId < 0) {
                            Toast.makeText(root.getApplicationContext(), "미선택 항목이 있습니다.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String index = SHA256(surveyKey + String.valueOf(i+1));
                        String checked = SHA256(surveyKey + String.valueOf(i+1) + checkButtonId);
                        try {
                            JSONObject json = new JSONObject();
                            json.put("index", index);
                            json.put("checked", checked);
                            jsonArray.put(json);
                        } catch(JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    jsonObject.put("items", jsonArray);
                    final String url = "http://54.82.120.60:5000/transactions/new";
                    NetworkTask networkTask = new NetworkTask(url, jsonObject, root);
                    networkTask.execute();

                    /////////////////////////////////////////////////////////////
                    String currentDate = String.valueOf(System.currentTimeMillis() / 1000);
                    CustomTask task = new CustomTask();
                    String result = null;
                    try {
                        result = task.execute(kakao_id, sot.getSurveyNum(), currentDate).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    String result1[] = result.split(",");

                    if (result1[1].equals("success")) {
                        Toast.makeText(root.getApplicationContext(), "설문조사 참여 완료", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(root.getApplicationContext(), "연동실패", Toast.LENGTH_SHORT).show();
                    }


                    ((MainActivity) getActivity()).setFragmentData(1);

                } catch (Exception e) {

                }
            }
        });
        return view;
    }

    public String SHA256(String value)
    {
        String result = "";
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            //출력
            result = hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
        return result;
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private JSONObject jsonObject;
        Activity ac;

        public NetworkTask(String url, JSONObject values,Activity a) {
            this.url = url;
            this.jsonObject = values;
            ac = a;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result; // 요청 결과를 저장할 변수.
            HttpPostAsyncTask requestHttpURLConnection = new HttpPostAsyncTask();
            result = requestHttpURLConnection.request(url, jsonObject); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.equals("error")) {
                isParticipate = false;
                Toast.makeText(ac.getApplicationContext(), "투표 실패", Toast.LENGTH_LONG).show();
            }
            else {
                isParticipate = true;
            }


            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
        }
    }

    public ColorStateList getradioButtonColor() //라디오버튼 색깔만들기함수
    {
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{

                        new int[]{-android.R.attr.state_enabled}, //disabled
                        new int[]{android.R.attr.state_enabled} //enabled
                },
                new int[] {

                        Color.BLACK //disabled
                        ,Color.BLUE //enabled

                }
        );

        return colorStateList;
    }

    //폰에서 뒤로가기 누르면 에러안뜨게해줌
    @Override
    public void onBack() {
        MainActivity activity = (MainActivity) getActivity();
        activity.setOnBackPressedListener(null);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, surveyMainFragment).commit();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) context).setOnBackPressedListener(this);
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/surveyParticipation.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "kakao_id=" + strings[0] + "&surveyNum=" + strings[1] + "&curDate=" + strings[2];
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