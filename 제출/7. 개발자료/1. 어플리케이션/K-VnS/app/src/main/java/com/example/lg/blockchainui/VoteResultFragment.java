package com.example.lg.blockchainui;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class VoteResultFragment extends Fragment implements MainActivity.OnBackPressedListener{
    public static VoteResultFragment newInstance(){
        return new VoteResultFragment();
    }

    double sum = 0;
    double resultArr[] = new double[50];
    LinearLayout itemLayout;
    TextView vote_name;
    private int itemListCnt = 0; // 여기에 목록 갯수 넣으셈
    EditText[] itemList;
    Button okBtn;
    VoteMainFragment voteMainFragment;
    MainActivity.VoteObject vot;
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_vote_result,container,false);
        final Activity root = getActivity();
        okBtn = (Button) view.findViewById(R.id.check_paticipation_button);
        voteMainFragment = new VoteMainFragment();
        vote_name = (TextView) view.findViewById (R.id.vote_name);
        itemLayout = (LinearLayout) view.findViewById(R.id.voting_itemLayout) ;
        toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.setTitle("투표결과");

        vot = ((MainActivity) getActivity()).getEndVoteList().get(((MainActivity) getActivity()).getPosition());

        vote_name.setText(vot.getDescription());

        for(int i = 0 ; i < 50; i++) resultArr[i] = 0;

        long start_time = Long.parseLong(vot.getRegisterDate());
        long end_time = Long.parseLong(vot.getEndDate());
        ///////////////////////////
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("key", "v"+vot.getVoteNum());
            jsonObject.put("item_count", vot.getItemList().size());
            jsonObject.put("start_time", start_time);
            jsonObject.put("end_time",end_time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String url = "http://54.82.120.60:5000/chain/results";
        NetworkTask networkTask = new NetworkTask(url, jsonObject, vot);
        networkTask.execute();

        okBtn.setOnClickListener(new View.OnClickListener(){ //확인버튼

            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setFragmentData(3);
            }
        });

        return view;
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
        private MainActivity.VoteObject vote;
        private Map<String, Double> map = new HashMap<String, Double>();

        public NetworkTask(String url, JSONObject values, MainActivity.VoteObject v) {
            this.url = url;
            this.jsonObject = values;
            vote = v;
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

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            try {
                if(!s.equals("error")) {
                    JSONObject json = new JSONObject(s).getJSONObject("items");
                    Iterator keys = json.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        map.put(key, Double.parseDouble(json.get(key).toString()));
                    }

                    String key = "v" + vot.getVoteNum();
                    int count = vot.getItemList().size();
                    for(int i = 0; i < count; i++) {
                        String checked = SHA256(key + String.valueOf(i+1));
                        resultArr[i] = map.get(checked);
                        sum = sum + resultArr[i];
                    }
                }

                for(int i = 0; i < vot.getCount(); i++) //목록으로 editText 동적생성
                {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

                    final TextView editText = new TextView(getActivity().getApplicationContext());

                    float mScale = getResources().getDisplayMetrics().density;
                    final int width = (int)(355*mScale);

                    editText.setText(i+1 + ". " +vot.getItemList().get(i) ); // i+1 항목번호, vot.getItemList.get(i) 항목 내용
                    if(sum != 0) {
                        editText.setText(editText.getText() + " : " + (int)resultArr[i] + " (" + String.format("%.2f",resultArr[i] / sum * 100.0) + "%)");
                    }
                    editText.setId(i);
                    editText.setWidth(width);
                    editText.setTextSize(20);
                    editText.setLayoutParams(params);
                    editText.setTextColor(Color.rgb(0,0,0));
                    itemLayout.addView(editText);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}