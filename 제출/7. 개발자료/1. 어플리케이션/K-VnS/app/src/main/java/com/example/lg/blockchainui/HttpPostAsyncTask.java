package com.example.lg.blockchainui;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class HttpPostAsyncTask {
    public String request(String _url, JSONObject _params){

        // HttpURLConnection 참조 변수.
        HttpURLConnection urlConn = null;
        String result = "error";
        // URL 뒤에 붙여서 보낼 파라미터.
        /**
         * 2. HttpURLConnection을 통해 web의 데이터를 가져온다.
         * */
        try{
            URL url = new URL(_url);
            urlConn = (HttpURLConnection) url.openConnection();

            // [2-1]. urlConn 설정.
            urlConn.setRequestMethod("POST"); // URL 요청에 대한 메소드 설정 : POST.
            urlConn.setConnectTimeout(3000);
            urlConn.setReadTimeout(3000);
            urlConn.setRequestProperty("Cache-Control", "no-cache");
            urlConn.setRequestProperty("Content-Type", "application/json");
            urlConn.setRequestProperty("Accept", "application/json");
            // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            urlConn.setDoOutput(true);
            // InputStream으로 서버로 부터 응답을 받겠다는 옵션.
            urlConn.setDoInput(true);
            urlConn.connect();

            // [2-2]. parameter 전달 및 데이터 읽어오기.
            String strParams = _params.toString(); //sbParams에 정리한 파라미터들을 스트링으로 저장. 예)id=id1&pw=123;
            OutputStream os = urlConn.getOutputStream();
            os.write(strParams.getBytes());
            os.flush(); // 출력 스트림을 플러시(비운다)하고 버퍼링 된 모든 출력 바이트를 강제 실행.
            os.close(); // 출력 스트림을 닫고 모든 시스템 자원을 해제.

            // [2-3]. 연결 요청 확인.
            // 실패 시 null을 리턴하고 메서드를 종료.
            if (urlConn.getResponseCode() != 201)
                return result;

            InputStream is = urlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(is, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();
            String line;


            while ((line = bufferedReader.readLine()) != null) {
                Log.d("line:", line);
                sb.append(line);
            }

            bufferedReader.close();
            urlConn.disconnect();

            result = sb.toString().trim();

        } catch (MalformedURLException e) { // for URL
            e.printStackTrace();
        } catch(SocketTimeoutException e){
            e.printStackTrace();
        } catch (IOException e) { // for openConnection().
            e.printStackTrace();
        } finally
        {
            if (urlConn != null)
                urlConn.disconnect();
        }

        return result;

    }
}