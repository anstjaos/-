package com.example.lg.blockchainui;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class VoteParticipationFragment extends Fragment implements MainActivity.OnBackPressedListener{
    public static VoteParticipationFragment newInstance(){
        return new VoteParticipationFragment();
    }
    boolean isParticipate = false;

    //RadioButton voteListBtn;
    TextView nameText;
    EditText[] itemList;
    Button participateBtn, cancelBtn;
    LinearLayout itemLayout;
    MainActivity.VoteObject vot;
    RadioGroup radioGroup_item;
    VoteMainFragment voteMainFragment;
    String kakao_id;
    Toolbar toolbar;

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
        View view = inflater.inflate(R.layout.activity_vote_participation,container,false);
        voteMainFragment = new VoteMainFragment();
        final Activity root = getActivity();

        //voteListBtn = (RadioButton) view.findViewById(R.id.voting_radio_button);
        nameText = (TextView) view.findViewById(R.id.vote_name);
        participateBtn = (Button) view.findViewById(R.id.paticipation_button);
        cancelBtn = (Button) view.findViewById(R.id.cancel_paticipation_button);

        itemLayout = (LinearLayout) view.findViewById(R.id.voting_itemLayout);
        radioGroup_item = (RadioGroup) view.findViewById(R.id.radio_group_vote);

        vot = ((MainActivity) getActivity()).getVoteList().get(((MainActivity) getActivity()).getPosition()); //투표 객체 저장
        toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.setTitle("투표참가");

        nameText.setText(vot.getDescription()); //투표이름

        for(int i = 0; i < vot.itemList.size(); i++){ //라디오버튼 동적생성
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            final RadioButton radioButton = new RadioButton(root.getApplicationContext());
            final EditText edt = new EditText(root.getApplicationContext());
            final RadioGroup radioGroup = new RadioGroup(root.getApplicationContext());
            float mScale = getResources().getDisplayMetrics().density;
            final int width = (int)(355*mScale);

            ColorStateList colorStateList;//라디오 버튼 색깔
            colorStateList = getradioButtonColor();

            radioButton.setText(vot.getItemList().get(i));
            radioButton.setWidth(width);
            radioButton.setTextSize(20);
            radioButton.setTextColor(Color.rgb(0,0,0));
            radioButton.setLayoutParams(params);
            radioButton.setButtonTintList(colorStateList);
            radioButton.setId(i+1);
            radioGroup_item.addView(radioButton);
        }

        cancelBtn.setOnClickListener(new View.OnClickListener() { //취소버튼
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).setFragmentData(3);
            }
        });

        participateBtn.setOnClickListener(new View.OnClickListener(){ // 투표하기 버튼 여기서 값보내기하면댈듯하오하오

            @Override
            public void onClick(View v) {
                try{
                    int selected = radioGroup_item.getCheckedRadioButtonId();
                    if(selected < 0) return;

                    String voteKey = "v" + vot.getVoteNum();
                    String hashChecked = SHA256(voteKey + String.valueOf(selected));

                    final JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("key", voteKey);
                        jsonObject.put("checked", hashChecked);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    final String url = "http://54.82.120.60:5000/transactions/new";
                    NetworkTask networkTask = new NetworkTask(url, jsonObject,root);
                    networkTask.execute();


                    String currentDate = String.valueOf(System.currentTimeMillis() / 1000);
                    CustomTask task = new CustomTask();
                    String result = null;
                    try {
                        result = task.execute(kakao_id,vot.getVoteNum(), currentDate).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    String result1[] = result.split(",");

                    if(result1[1].equals("success")) {
                        Toast.makeText(root.getApplicationContext(), "투표 참여 완료", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(root.getApplicationContext(), "연동실패", Toast.LENGTH_SHORT).show();
                    }
                    ((MainActivity) getActivity()).setFragmentData(3);

                }catch (Exception e){

                }
            }
        });
        return view;
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
                .replace(R.id.main_fragment_container, voteMainFragment).commit();
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
                URL url = new URL("http://121.151.244.47:8080/System1/voteParticipation.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "kakao_id=" + strings[0] + "&voteNum=" + strings[1] + "&curDate=" + strings[2];
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