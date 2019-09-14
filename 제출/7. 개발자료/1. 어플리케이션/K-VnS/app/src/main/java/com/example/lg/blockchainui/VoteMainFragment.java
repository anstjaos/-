package com.example.lg.blockchainui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class VoteMainFragment extends Fragment implements MainActivity.OnBackPressedListener{

    //  private String[] items1 = {""};

    Button registerVoteButton;

    ListView votinglistView = null; // 시험용 투표참여 리스트뷰 실제로는 항목 받아옴
    ListView votedlistView = null; // 시험용 투표결과 리스트뷰 실제로는 항목 받아옴
    ArrayList<HashMap<String, String>> votingNameAndEndDateList; // 진행중
    ArrayList<HashMap<String, String>> votedNameAndEndDateList; // 종료
    SimpleAdapter votingAdapter;
    SimpleAdapter votedAdapter;
    HomeFragment homeFragment;
    String kakao_id;
    MainActivity.VoteObject vot;
    Toolbar toolbar;

    public void setKakao_id(String kakao_id){
        this.kakao_id = kakao_id;
    }

    String listName;
    public static VoteMainFragment newInstance() {
        return new VoteMainFragment();
    }

    public VoteMainFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final Activity root = getActivity();

        View view = inflater.inflate(R.layout.activity_vote_main,container,false);
        registerVoteButton = (Button) view.findViewById(R.id.registerVote);
        homeFragment = new HomeFragment();

        toolbar = ((MainActivity)getActivity()).getToolbar();
        toolbar.setTitle("투표메인");

        registerVoteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setFragmentData(4);
            }
        });

        votingNameAndEndDateList = new ArrayList<HashMap<String, String>>();
        votedNameAndEndDateList = new ArrayList<HashMap<String, String>>();

        votingListViewItemAdd();
        votedListViewItemAdd();

        votinglistView = (ListView) view.findViewById(R.id.vote_ing_listView);
        votedlistView = (ListView) view.findViewById(R.id.vote_ed_listView);

        votingAdapter = new SimpleAdapter((MainActivity) getActivity(), votingNameAndEndDateList, android.R.layout.simple_list_item_2, new String[]{"item1", "item2"}, new int[]{android.R.id.text1, android.R.id.text2});
        votedAdapter = new SimpleAdapter((MainActivity) getActivity(), votedNameAndEndDateList, android.R.layout.simple_list_item_2, new String[]{"item1", "item2"}, new int[]{android.R.id.text1, android.R.id.text2});

        votinglistView.setAdapter(votingAdapter);
        votedlistView.setAdapter(votedAdapter);

        votingAdapter.notifyDataSetChanged();
        votedAdapter.notifyDataSetChanged();

        votinglistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity) getActivity()).setPosition(position);
                vot = ((MainActivity) getActivity()).getVoteList().get(((MainActivity) getActivity()).getPosition());
                try{
                    String result;
                    DeleteTask task = new DeleteTask();
                    result = task.execute(vot.getVoteNum(), kakao_id).get();
                    String[] result1 = result.split(",");
                    Log.i("position", String.valueOf(position));
                    if(result1[1].equals("true")) {
                        Toast.makeText(root.getApplicationContext(), "삭제 성공", Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).removeVoteObject(position);
                        votingNameAndEndDateList.remove(position);
                        ((MainActivity)getActivity()).getVotingNameAndEndDateList().remove(position);
                    }
                    else
                        Toast.makeText(root.getApplicationContext(), "삭제 실패", Toast.LENGTH_SHORT).show();
                }catch(Exception e){

                }
                votinglistView.clearChoices();
                votingAdapter.notifyDataSetChanged();
                return true;
            }
        });

        votinglistView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //리스트뷰 아이템클릭했을때 일어나는함수
                ((MainActivity) getActivity()).setPosition(position);// 어떤아이템을 클릭햇는지 같이보내줌

                vot = ((MainActivity) getActivity()).getVoteList().get(((MainActivity) getActivity()).getPosition());

                try{
                    CustomTask task = new CustomTask(root);
                    task.execute(kakao_id, vot.getVoteNum()).get();
                }catch(Exception e){

                }
            }
        });

        votedlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity) getActivity()).setPosition(position);
                vot = ((MainActivity) getActivity()).getEndVoteList().get(((MainActivity) getActivity()).getPosition());
                ((MainActivity) getActivity()).setFragmentData(6);
            }
        });
        /*


        listView6.setOnLongClickListener(new View.OnLongClickListener(){ // 꾹눌렀을때

            @Override
            public boolean onLongClick(View v) {
                ((MainActivity) getActivity()).setFragmentData(6);
                return true;
            }
        });
        */
        return view;
    }





    public void votingListViewItemAdd(){
        if(((MainActivity)getActivity()).getName() != ""){
            votingNameAndEndDateList.clear();
            ArrayList<HashMap<String, String>> temp = ((MainActivity)getActivity()).getVotingNameAndEndDateList();
            for(int i = 0; i < temp.size();i++){
                votingNameAndEndDateList.add(temp.get(i));
            }
        }
    }

    public void votedListViewItemAdd(){
        if(((MainActivity)getActivity()).getName() != ""){
            votedNameAndEndDateList.clear();
            ArrayList<HashMap<String, String>> temp = ((MainActivity)getActivity()).getVotedNameAndEndDateList();
            for(int i = 0; i < temp.size();i++){
                votedNameAndEndDateList.add(temp.get(i));
            }
        }
    }

    @Override
    public void onBack() {
        MainActivity activity = (MainActivity) getActivity();
        activity.setOnBackPressedListener(null);
        ((MainActivity) getActivity()).setFragmentData(2);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) context).setOnBackPressedListener(this);
    }


    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        Activity ac;
        CustomTask(Activity a) {
            ac = a;
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/checkParticipation.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "kakao_id=" + strings[0] + "&voteNum=" + strings[1];
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
            if(result1[1].equals("true")){
                ((MainActivity) getActivity()).setFragmentData(5); // 나와라 voteParticipationfrg
            }
            else if(result1[1].equals("false")){
                Toast.makeText(ac.getApplicationContext(), "이미 투표하셨습니다.", Toast.LENGTH_SHORT).show();
            }
            else if(result1[1].equals("departmentfail")){
                Toast.makeText(ac.getApplicationContext(), "투표 가능한 학과가 아닙니다.", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(ac.getApplicationContext(), "연동 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class DeleteTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://121.151.244.47:8080/System1/voteDelete.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "vote_num=" + strings[0] + "&kakao_id=" + strings[1];
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
