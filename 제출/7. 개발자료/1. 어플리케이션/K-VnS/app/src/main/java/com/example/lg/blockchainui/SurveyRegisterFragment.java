package com.example.lg.blockchainui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SurveyRegisterFragment extends Fragment implements MainActivity.OnBackPressedListener {

    public static SurveyRegisterFragment newInstance(){
        return new SurveyRegisterFragment();
    }
    EditText nameEditText, endDateEditText,descriptionEditText, itemEditText;
    EditText[] itemList;
    Button createNewItemBtn, createEndTimeBtn,registerBtn, cancelBtn;
    LinearLayout itemLayout;
    private int itemListCnt = 0;
    final int DIALOG_DATE = 1;
    private int mYear = 2018;
    private int mMonth = 11;
    private int mDay = 1;
    String kakao_id;
    Toolbar toolbar;
    Context context;

    SurveyMainFragment surveyMainFragment;
    MainActivity.SurveyObject sot;

    EditText editText;

    CheckBox allDepartmentCheckBox;
    public void setKakao_id(String kakao_id){
        this.kakao_id = kakao_id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fv = inflater.inflate(R.layout.activity_survey_register, container, false);

        itemList = new EditText[20];
        final Activity root = getActivity();

        surveyMainFragment = new SurveyMainFragment();

        context = new ContextThemeWrapper((MainActivity) root, android.R.style.Theme_Holo_Light_Dialog);

        nameEditText = (EditText) fv.findViewById(R.id.surveyNameEditText);
        endDateEditText = (EditText) fv.findViewById(R.id.surveyEndDateEditText);
        descriptionEditText = (EditText) fv.findViewById(R.id.surveyDescriptionEditText);
        itemEditText = (EditText) fv.findViewById(R.id.surveyItemEditText);

        createNewItemBtn = (Button) fv.findViewById(R.id.surveyCreateNewItemBtn);
        registerBtn = (Button) fv.findViewById(R.id.surveyRegisterBtn);
        cancelBtn = (Button) fv.findViewById(R.id.surveyCancelBtn);
        createEndTimeBtn = (Button) fv.findViewById(R.id.surveyCreateEndTimeBtn);

        itemLayout = (LinearLayout) fv.findViewById(R.id.surveyItemLayout);

        allDepartmentCheckBox = (CheckBox) fv.findViewById(R.id.surveyAllDepartmentCheckBox);

        itemList[itemListCnt++] = itemEditText;

        toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.setTitle("설문조사등록");



        createNewItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //항목추가 동적생성
                if(itemListCnt >= 20){

                    Toast.makeText(root.getApplicationContext(), "더 이상 추가할 수 없습니다!", Toast.LENGTH_LONG).show();
                    return;
                }

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                editText = new EditText(root.getApplicationContext());

                float mScale = getResources().getDisplayMetrics().density;
                final int width = (int)(180*mScale);

                editText.setText("");
                editText.setWidth(width);
                editText.setTextSize(20);
                editText.setLayoutParams(params);
                editText.setTextColor(Color.rgb(0,0,0));
                editText.getBackground().setColorFilter(Color.rgb(0,0,0),PorterDuff.Mode.SRC_ATOP);
                // editText.getBackground().clearColorFilter();
                itemLayout.addView(editText);

                itemList[itemListCnt++] = editText;
            }
        });
        createEndTimeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onCreateDialog(DIALOG_DATE).show();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //설문조사등록
                String s = nameEditText.getText().toString();
                String t = endDateEditText.getText().toString();
                String d = descriptionEditText.getText().toString();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy년MM월dd일");
                Date date = null;
                String endTime = null;
                String currentDate = String.valueOf(System.currentTimeMillis() / 1000);
                String type = "1";

                if(allDepartmentCheckBox.isChecked() == true){
                    type = "0";
                }

                try {
                    date = formatter.parse(t);
                    endTime = String.valueOf(date.getTime() / 1000);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                if(s.equals(""))
                {
                    Toast.makeText(root.getApplication(), "제목을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if(t.equals(""))
                {
                    Toast.makeText(root.getApplication(), "종료날짜를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if(d.equals(""))
                {
                    Toast.makeText(root.getApplication(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                          CustomTask task = new CustomTask(s, currentDate, endTime, d, root);
                          task.execute(kakao_id, s, currentDate, endTime, d, String.valueOf(itemListCnt), type);
//                        String result1[] = result.split(",");
//                        Log.i("result", result1[1]);
//
//                        if(result1[1].equals("fali")){
//                            Toast.makeText(root.getApplicationContext(), "연동실패", Toast.LENGTH_SHORT).show();
//                        }
//                        else{
//                            Toast.makeText(root.getApplicationContext(), "등록 완료", Toast.LENGTH_LONG).show();
//                            vot = new MainActivity().new VoteObject(result1[1],s,currentDate,t,d);
//                            for(int i = 0 ; i < itemListCnt; i++) {
//                                msg += itemList[i].getText() + "\n";
//                                vot.setItemList(itemList[i].getText().toString());
//                            }
//                        sot = new MainActivity().new SurveyObject("1",s,currentDate,t,d);

                        //((MainActivity) getActivity()).setSurveyingData(s, t);
                        //((MainActivity) getActivity()).setEndSurveyObject(sot);
                        //((MainActivity) getActivity()).setFragmentData(1);
                        //sot.setSurveyItemList(itemList[0].getText().toString());
                        //sot.setSurveyItemList(itemList[1].getText().toString());
//                        }
                    } catch (Exception e) {

                    }
                }
            }

        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(root.getApplicationContext(), "취소 완료", Toast.LENGTH_LONG).show();

                ((MainActivity) getActivity()).setFragmentData(1);
            }
        });

        return fv;
    }

    private void updateDate(){
        String str = mYear + "년" + (mMonth + 1) + "월" + mDay + "일";
        endDateEditText.setText(str);
    }
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mYear = year;
            mMonth = month;
            mDay = dayOfMonth;
            updateDate();
        }
    };

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new DatePickerDialog(context, mDateSetListener, mYear, mMonth, mDay);
        }
        return null;
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
        String name, currentDate, endDate, description;
        Activity ac;

        public CustomTask(String n, String cd, String ed, String d,Activity a)
        {
            name = n ;
            currentDate = cd;
            endDate = ed;
            description = d;
            ac = a;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/surveyRegister.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "kakao_id=" + strings[0] + "&surveyName=" + strings[1] + "&curDate=" + strings[2] + "&endDate=" + strings[3] + "&description=" + strings[4] +"&itemCount=" + strings[5] + "&type=" + strings[6];
                for(int i = 0; i < itemListCnt; i++)
                {
                    sendMsg = sendMsg + "&item" + String.valueOf(i) + "=" + itemList[i].getText();
                }
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
            String result1[] = s.split(",");
            Log.i("result", result1[1]);

            if(result1[1].equals("fali")){
                Toast.makeText(ac.getApplicationContext(), "연동실패", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(ac.getApplicationContext(), "등록 완료", Toast.LENGTH_LONG).show();
                sot = new MainActivity().new SurveyObject(result1[1],name,currentDate,endDate,description);
                for(int i = 0 ; i < itemListCnt; i++) {
                    sot.setSurveyItemList(itemList[i].getText().toString());
                }
                ((MainActivity) getActivity()).setSurveyingData(name, endDate);
                ((MainActivity) getActivity()).setSurveyObject(sot);
                ((MainActivity) getActivity()).setFragmentData(1);
            }
        }
    }
}