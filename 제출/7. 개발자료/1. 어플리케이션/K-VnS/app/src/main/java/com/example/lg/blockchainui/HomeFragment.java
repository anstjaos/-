package com.example.lg.blockchainui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements MainActivity.OnBackPressedListener{
    ImageView manageImage;

    TextView userNameText;
    Toolbar toolbar;
    String kakao_id;

    LinearLayout itemLayout;
    LinearLayout surveyLayout;
    private int itemListCnt = 0;
    private int surveyItemListCnt = 0;// 여기에 목록 갯수 넣으셈
    EditText[] itemList;

    public HomeFragment() {
        // Required empty public constructor
    }

    public void setKakao_id(String kakao_id){
        this.kakao_id = kakao_id;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.activity_home,container,false);
        manageImage = (ImageView) view.findViewById(R.id.manage_btn);
        final Activity root = getActivity();

        toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.setTitle("메인");


        itemLayout = (LinearLayout) view.findViewById(R.id.myPaticipation);
        surveyLayout = (LinearLayout) view.findViewById(R.id.mySurvey);

        //itemListCnt = ?; 내가 투표한 목록의 갯수
        ArrayList<String> voteName = new ArrayList<String>();
        ArrayList<String> surveyName = new ArrayList<String>();
        try {
            String result;
            CustomTask task = new CustomTask();
            result = task.execute(kakao_id).get();
            String result1[] = result.split(",");
            for(int i = 0; i < result1.length; i++){
                voteName.add(result1[i+1]);
                itemListCnt++;
            }
            Log.i("result", result);
        }catch(Exception e){
            Log.i("result", "error");
        }
        for(int i = 0; i < itemListCnt; i++) //목록으로 editText 동적생성
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            final TextView editText = new TextView(root.getApplicationContext());

            float mScale = getResources().getDisplayMetrics().density;
            final int width = (int)(260*mScale);

            editText.setText(voteName.get(i));
            editText.setWidth(width);
            editText.setTextSize(20);
            editText.setLayoutParams(params);
            editText.setTextColor(Color.rgb(0,0,0));
            itemLayout.addView(editText);
        }

        try {
            String result;
            SurveyCustomTask task = new SurveyCustomTask();
            result = task.execute(kakao_id).get();
            String result1[] = result.split(",");
            for(int i = 0; i < result1.length; i++){
                surveyName.add(result1[i+1]);
                surveyItemListCnt++;
            }
            Log.i("result", result);
        }catch(Exception e){
            Log.i("result", "error");
        }
        for(int i = 0; i < surveyItemListCnt; i++) //목록으로 editText 동적생성
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            final TextView editText = new TextView(root.getApplicationContext());

            float mScale = getResources().getDisplayMetrics().density;
            final int width = (int)(260*mScale);

            editText.setText(surveyName.get(i));
            editText.setWidth(width);
            editText.setTextSize(20);
            editText.setLayoutParams(params);
            editText.setTextColor(Color.rgb(0,0,0));
            surveyLayout.addView(editText);
        }

        manageImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                PopupMenu p = new PopupMenu(getContext(), v);
                MenuInflater inflater = p.getMenuInflater();
                Menu menu = p.getMenu();
                inflater.inflate(R.menu.manage_menu, menu);

                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // TODO Auto-generated method stub
                        //각 메뉴별 아이디를 조사한후 할일을 적어줌(인텐트 및 다양한 이벤트)
                        switch(item.getItemId()){
                            case R.id.manage_menu1:
                                ((MainActivity) getActivity()).setActivityData(1);
                                break;

                            case R.id.manage_menu2:
                                ((MainActivity) getActivity()).setActivityData(2);
                                break;
                            case R.id.manage_menu3:
                                ((MainActivity) getActivity()).setActivityData(3);
                                break;
                            case R.id.manage_menu4:
                                ((MainActivity) getActivity()).setActivityData(4);
                                break;
                        }
                        return false;
                    }
                });
                p.show();
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == 1) {
            Log.d("","CLICK");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/ParticipationInfo.jsp");
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

    class SurveyCustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/surveyParticipationInfo.jsp");
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

    //폰에서 뒤로가기 누르면 에러안뜨게해줌
    @Override
    public void onBack() {
        MainActivity activity = (MainActivity) getActivity();
        activity.setOnBackPressedListener(null);
        ActivityCompat.finishAffinity(activity);
        //  getActivity().getSupportFragmentManager().beginTransaction()
        //      .replace(R.id.main_fragment_container, voteMainFragment).commit();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) context).setOnBackPressedListener(this);
    }
}