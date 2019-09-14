package com.example.lg.blockchainui;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SurveyResultFragment extends Fragment implements MainActivity.OnBackPressedListener{
    public static SurveyResultFragment newInstance(){
        return new SurveyResultFragment();
    }
    private Activity root;
    double sum[] = new double[50];
    double resultArr[][] = new double[50][50];
    LinearLayout itemLayout;
    TextView survey_name;
    private int itemListCnt = 0; // 여기에 목록 갯수 넣으셈
    EditText[] itemList;
    Button okBtn;
    SurveyMainFragment surveyMainFragment;
    MainActivity.SurveyObject sot;
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_survey_result,container,false);
        root = getActivity();
        okBtn = (Button) view.findViewById(R.id.surveyCheck_paticipation_button);
        surveyMainFragment = new SurveyMainFragment();
        survey_name = (TextView) view.findViewById (R.id.survey_name);
        itemLayout = (LinearLayout) view.findViewById(R.id.surveyed_itemLayout) ;
        toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.setTitle("투표결과");

        sot = ((MainActivity) getActivity()).getEndSurveyList().get(((MainActivity) getActivity()).getPosition());

        survey_name.setText(sot.getSurveyDescription());

        for(int i = 0 ; i < 50; i++) {
            sum[i] = 0;
            for(int j = 0 ; j < 50; j++) resultArr[i][j] = 0;
        }

        long start_time = Long.parseLong(sot.getSurveyRegisterDate());
        long end_time = Long.parseLong(sot.getSurveyEndDate());
        ///////////////////////////
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("key", "s"+sot.getSurveyNum());
            jsonObject.put("item_count", sot.getSurveyItemList().size());
            jsonObject.put("start_time", start_time);
            jsonObject.put("end_time",end_time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String url = "http://54.82.120.60:5000/chain/results";
        NetworkTask networkTask = new NetworkTask(url, jsonObject, sot);
        networkTask.execute();

        okBtn.setOnClickListener(new View.OnClickListener(){ //확인버튼

            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setFragmentData(1);
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
                .replace(R.id.main_fragment_container, surveyMainFragment).commit();
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
        private MainActivity.SurveyObject sur;
        private Map<String, Double> map = new HashMap<String, Double>();

        public NetworkTask(String url, JSONObject values, MainActivity.SurveyObject s) {
            this.url = url;
            this.jsonObject = values;
            sur = s;
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

                    String key = "s" + sur.getSurveyNum();
                    int count = sur.getSurveyItemList().size();
                    for(int i = 0; i < count; i++) {
                        for(int j = 1; j <=5; j++) {
                            String checked = SHA256(key + String.valueOf(i+1) + String.valueOf(j));
                            resultArr[i][j] = map.get(checked);
                            sum[i] = sum[i] + resultArr[i][j];
                        }
                    }
                }


                for(int i = 0; i < sot.getSurveyCount(); i++) //목록으로 editText 동적생성
                {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);


                    final TextView textView = new TextView(getActivity().getApplicationContext());
                    float mScale = getResources().getDisplayMetrics().density;
                    final int width = (int)(355*mScale);


                    itemLayout.setOrientation(LinearLayout.VERTICAL);

                    textView.setText(sot.getSurveyItemList().get(i));
                    textView.setId(i);
                    textView.setWidth(width);
                    textView.setTextSize(20);
                    textView.setLayoutParams(params);
                    textView.setTextColor(Color.rgb(0,0,0));
                    itemLayout.addView(textView);

                    LinearLayout textLayout = new LinearLayout(getActivity().getApplicationContext());

                    for(int j = 1 ; j < 6;j++)
                    {
                        final TextView textViewResult = new TextView(getActivity().getApplicationContext());
                        if(j == 1)
                        {
                            textViewResult.setText("매우나쁨"); //여다가 추가하면됨 표 추가
                        }
                        else if(j == 2)
                        {
                            textViewResult.setText("나쁨");
                        }
                        else if(j == 3)
                        {
                            textViewResult.setText("보통");
                        }
                        else if(j == 4)
                        {
                            textViewResult.setText("좋음");
                        }
                        else if(j == 5)
                        {
                            textViewResult.setText("매우좋음");
                        }

                        if(sum[i] != 0) {
                            textViewResult.setText(textViewResult.getText() + " : " +
                                    (int)resultArr[i][j] + " (" + String.format("%.2f",resultArr[i][j] / sum[i] * 100.0) + "%)");
                        }

                        Display display = ((WindowManager) root.getApplication().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);

                        textViewResult.setId(j);
                        textViewResult.setWidth(size.x/5);
                        textViewResult.setTextSize(10);
                        textViewResult.setLayoutParams(params);
                        textViewResult.setTextColor(Color.rgb(0,0,0));
                        textLayout.addView(textViewResult);
                        textLayout.setOrientation(LinearLayout.HORIZONTAL);
                        //radioGroup_item.addView(textViewResult);
                        //radioGroup_item.setOrientation(RadioGroup.HORIZONTAL);

                    }
                    itemLayout.addView(textLayout);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}