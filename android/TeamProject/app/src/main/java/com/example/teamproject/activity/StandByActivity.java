package com.example.teamproject.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.teamproject.R;
import com.example.teamproject.etc.AppHelper;
import com.example.teamproject.etc.MYURL;
import com.example.teamproject.object.Member;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

public class StandByActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    TextView game_name_tv;

    ImageView user0_clothes_iv;
    TextView user0_id_tv;
    TextView user0_ready_tv;

    ImageView user1_clothes_iv;
    TextView user1_id_tv;
    TextView user1_ready_tv;

    ImageView user2_clothes_iv;
    TextView user2_id_tv;
    TextView user2_ready_tv;

    ImageView user3_clothes_iv;
    TextView user3_id_tv;
    TextView user3_ready_tv;

    Button ready_btn;

    String game_no;
    String game_name;
    String game_join_user;
    String game_admin_id;
    String[] game_join_user_arr;
    Boolean[] ready_state_arr;

    LinearLayout timer_ll; // 타이머 레이아웃
    TextView timer_tv; // 타이머 텍스트뷰

    Member login_member;

    Socket socket; // 소켓
    ReceiveThread receiveThread; // 소켓으로부터 오는 메시지를 읽기위한 쓰레드

    Handler handler;

    boolean is_game_start;

    String random;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stand_by);
        Log.d(TAG,"onCreate()");

        is_game_start=false;

        /*
         * 쉐어드에 저장된 로그인 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"), Member.class);

        game_name_tv = findViewById(R.id.game_name_tv);
        user0_clothes_iv = findViewById(R.id.user0_clothes_iv);
        user0_id_tv = findViewById(R.id.user0_id_tv);
        user0_ready_tv = findViewById(R.id.user0_ready_tv);
        user1_clothes_iv = findViewById(R.id.user1_clothes_iv);
        user1_id_tv = findViewById(R.id.user1_id_tv);
        user1_ready_tv = findViewById(R.id.user1_ready_tv);
        user2_clothes_iv = findViewById(R.id.user2_clothes_iv);
        user2_id_tv = findViewById(R.id.user2_id_tv);
        user2_ready_tv = findViewById(R.id.user2_ready_tv);
        user3_clothes_iv = findViewById(R.id.user3_clothes_iv);
        user3_id_tv = findViewById(R.id.user3_id_tv);
        user3_ready_tv = findViewById(R.id.user3_ready_tv);
        ready_btn = findViewById(R.id.ready_btn);
        timer_ll = findViewById(R.id.timer_ll);
        timer_tv = findViewById(R.id.timer_tv);

        game_join_user_arr = new String[4];
        ready_state_arr = new Boolean[4];

        user0_id_tv.setText("기다리는중..");
        user0_ready_tv.setVisibility(GONE);
        user1_id_tv.setText("기다리는중..");
        user1_ready_tv.setVisibility(GONE);
        user2_id_tv.setText("기다리는중..");
        user2_ready_tv.setVisibility(GONE);
        user3_id_tv.setText("기다리는중..");
        user3_ready_tv.setVisibility(GONE);

        final Intent intent = getIntent();
        if(intent.getStringExtra("create_game_room")!=null){ // 게임 생성 한 경우
            Log.d(TAG,"게임방 생성");
            game_no = intent.getStringExtra("game_no");
            game_name = intent.getStringExtra("game_name");
            game_name_tv.setText(game_name);
            game_admin_id = intent.getStringExtra("game_admin_id");
            user0_id_tv.setText("id : "+game_admin_id);
            user0_ready_tv.setVisibility(View.VISIBLE);
            game_join_user_arr[0] = game_admin_id;
            game_join_user_arr[1] = null;
            game_join_user_arr[2] = null;
            game_join_user_arr[3] = null;
            game_join_user = game_admin_id+",null,null,null";
            receiveThread = new ReceiveThread();
            receiveThread.start(); // 소켓연결
        }else{ // 게임에 참여한 경우
            Log.d(TAG,"게임 참여");
            game_no = intent.getStringExtra("game_no");
            game_name = intent.getStringExtra("game_name");
            game_name_tv.setText(game_name);
            game_join_user = intent.getStringExtra("game_join_user");

            game_join_user_arr = game_join_user.split(","); // 문자열 배열화

            /*
                ui 동기화
             */
            boolean apply_my_id = false;
            for(int i=0;i<game_join_user_arr.length;i++){
                if("null".equals(game_join_user_arr[i])){
                    game_join_user_arr[i]=null;
                    if(!apply_my_id){
                        if(game_join_user_arr[0]==null){
                            game_join_user_arr[0]=login_member.member_id;
                            user0_id_tv.setText("id : "+login_member.member_id);
                            user0_ready_tv.setVisibility(View.VISIBLE);
                            apply_my_id=true;
                        }else if(game_join_user_arr[1]==null){
                            game_join_user_arr[1]=login_member.member_id;
                            user1_id_tv.setText("id : "+login_member.member_id);
                            user1_ready_tv.setVisibility(View.VISIBLE);
                            apply_my_id=true;
                        }else if(game_join_user_arr[2]==null){
                            game_join_user_arr[2]=login_member.member_id;
                            user2_id_tv.setText("id : "+login_member.member_id);
                            user2_ready_tv.setVisibility(View.VISIBLE);
                            apply_my_id=true;
                        }else if(game_join_user_arr[3]==null){
                            game_join_user_arr[3]=login_member.member_id;
                            user3_id_tv.setText("id : "+login_member.member_id);
                            user3_ready_tv.setVisibility(View.VISIBLE);
                            apply_my_id=true;
                        }
                    }
                }else{
                    if(i==0){
                        user0_id_tv.setText("id : "+game_join_user_arr[i]);
                        user0_ready_tv.setVisibility(View.VISIBLE);
                    }else if(i==1){
                        user1_id_tv.setText("id : "+game_join_user_arr[i]);
                        user1_ready_tv.setVisibility(View.VISIBLE);
                    }else if(i==2){
                        user2_id_tv.setText("id : "+game_join_user_arr[i]);
                        user2_ready_tv.setVisibility(View.VISIBLE);
                    }else if(i==3){
                        user3_id_tv.setText("id : "+game_join_user_arr[i]);
                        user3_ready_tv.setVisibility(View.VISIBLE);
                    }
                }
            }

            game_join_user = game_join_user_arr_To_game_join_user(game_join_user_arr); // 배열을 문자열로 바꾸어줌
            Log.d(TAG,"temp_game_join_user : " + game_join_user);

            receiveThread = new ReceiveThread();
            receiveThread.start(); // 소켓연결

            gameJoinRequest(game_join_user); // 게임 참여 멤버 서버와 동기화
        } // end 게임 참여

        ready_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 준비 버튼을 누른 경우 이벤트
                ready();
                ready_btn.setVisibility(GONE);
                timer_ll.setVisibility(View.VISIBLE);
            }
        });


        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what==10){
                    timer_tv.setText("10초");
                }else if(msg.what==9){
                    timer_tv.setText("9초");
                }else if(msg.what==8){
                    timer_tv.setText("8초");
                }else if(msg.what==7){
                    timer_tv.setText("7초");
                }else if(msg.what==6){
                    timer_tv.setText("6초");
                }else if(msg.what==5){
                    timer_tv.setText("5초");
                }else if(msg.what==4){
                    timer_tv.setText("4초");
                }else if(msg.what==3){
                    timer_tv.setText("3초");
                }else if(msg.what==2){
                    timer_tv.setText("2초");
                }else if(msg.what==1){
                    timer_tv.setText("1초");
                }else if(msg.what==0){ // 게임 시작
                    Intent intent1 = new Intent(getApplicationContext(),PlayActivity.class);
                    intent1.putExtra("game_join_user",game_join_user);
                    intent1.putExtra("random",random);
                    startActivity(intent1);
                    gameQuitRequest(game_join_user); // 게임방 나감 요청
                    disconnectWithSocket();
                    if(receiveThread!=null){
                        receiveThread.setIs_stop();
                        receiveThread=null;
                    }
                    exitGameRequest(); // 게임방 종료 요청
                    finish();
                }
            }
        };

    } // onCreate()

    @Override
    protected void onDestroy() {
        super.onDestroy();
        int count=0;

        for (int i = 0; i< game_join_user_arr.length; i++){

            if(login_member.member_id.equals(game_join_user_arr[i])){
                game_join_user_arr[i]=null;
                if(i==0){
                    user0_id_tv.setText("기다리는중..");
                    user0_ready_tv.setVisibility(GONE);
                }else if(i==1){
                    user1_id_tv.setText("기다리는중..");
                    user1_ready_tv.setVisibility(GONE);
                }else if(i==2){
                    user2_id_tv.setText("기다리는중..");
                    user2_ready_tv.setVisibility(GONE);
                }else if(i==3){
                    user3_id_tv.setText("기다리는중..");
                    user3_ready_tv.setVisibility(GONE);
                }
            }else{
                if(game_join_user_arr[i]==null){
                    count++;
                }
            }
        }

        String temp_game_join_user=game_join_user_arr_To_game_join_user(game_join_user_arr);
        gameQuitRequest(temp_game_join_user); // 게임방 나감 요청

        disconnectWithSocket();
        if(receiveThread!=null){
            receiveThread.setIs_stop();
            receiveThread=null;
        }

        if(count==3) {
            exitGameRequest(); // 게임방 종료 요청
        }
    } // onDestroy()

    /**
     * 뒤로가기 버튼을 두번 연속으로 눌러야 종료되게끔 하는 메소드
     */
    private long time= 0;
    @Override
    public void onBackPressed(){
        if(is_game_start){
            Toast.makeText(getApplicationContext(),"이미 게임이 시작하였습니다",Toast.LENGTH_SHORT).show();
        }else{
            if(System.currentTimeMillis()-time>=2000){
                time=System.currentTimeMillis();
                Toast.makeText(getApplicationContext(),"뒤로 버튼을 한번 더 누르면 게임방에서 나가집니다.",Toast.LENGTH_SHORT).show();
            }else if(System.currentTimeMillis()-time<2000){
                Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        }
    } // onBackPressed()

    /**
     * 게임 참여 멤버 서버와 동기화
     */
    private void gameJoinRequest(final String temp_game_join_user){
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response);

                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"응답 에러: "+error.toString());
                        Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","game_join");
                params.put("game_no",game_no);
                params.put("temp_game_join_user",temp_game_join_user);
                return params;
            }
        };

        /*
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음

    } // gameJoinRequest()

    /**
     * 게임방 나감 요청
     */
    private void gameQuitRequest(final String temp_game_join_user){
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response);

                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"응답 에러: "+error.toString());
                        Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","game_quit");
                params.put("game_no",game_no);
                params.put("temp_game_join_user",temp_game_join_user);
                return params;
            }
        };

        /*
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음

    } // gameQuitRequest()

    /**
     * 게임 종료 요청
     */
    private void exitGameRequest(){
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response);
                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"응답 에러: "+error.toString());
                        Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","exit_game");
                params.put("game_no",game_no);
                return params;
            }
        };

        /*
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음
    } // exitGameRequest()

    /**
     * 배열을 스트링으로 바꾸어주는 메소드
     */
    private String game_join_user_arr_To_game_join_user(String[] game_join_user_arr){
        String temp_game_join_user = "";
        for(int i=0;i<game_join_user_arr.length;i++){
            if(i==game_join_user_arr.length-1){
                if(game_join_user_arr[i]==null){
                    temp_game_join_user = temp_game_join_user +"null";
                }else{
                    temp_game_join_user = temp_game_join_user +game_join_user_arr[i];
                }
            }else{
                if(game_join_user_arr[i]==null){
                    temp_game_join_user = temp_game_join_user +"null,";
                }else{
                    temp_game_join_user = temp_game_join_user +game_join_user_arr[i]+",";
                }
            }
        }
        return temp_game_join_user;
    } // game_join_user_arr_To_game_join_user()

    /**
     * 준비완료 소켓에 보내는 메소드
     */
    private void ready(){
        new Thread(){
            @Override
            public void run(){
                try{
                    Log.d(TAG,login_member.member_id+"님 준비 버튼 클릭!");
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                    pw.println("ready¡"+game_join_user+"ㅣ\r\n"); // 준비상태 소켓에 보내기
                    pw.flush();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 소켓서버에서 메시지 받는 쓰레드
     */
    class ReceiveThread extends Thread{

        boolean is_stop;

        private ReceiveThread(){
            is_stop=false;
        }

        @Override
        public void run() {
            super.run();
            Log.d(TAG, "쓰레드 run()");
//            String server_ip = "35.224.156.8";
            String server_ip = "35.243.90.95";

            BufferedReader br = null;
            PrintWriter pw = null;

            try {

                socket = new Socket(server_ip, 5050); // 소켓 연결

                pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                /*
                 * 게임방 참여 및 참여 멤버 아이디 서버에 보내기
                 */
                pw.println("join¡"+login_member.member_id+"ㅣ"+game_join_user+"\r\n");
                pw.flush();

                /*
                 * 소켓서버 응답 받는 곳
                 */
                while(true){
                    String line = br.readLine(); // 여기서 대기
                    if(line==null){
                        Log.d(TAG,"서버가 종료되었습니다");
                        break;
                    }else{ // 서버에서 정상적으로 응답 받은 경우
                        Log.d(TAG,"[서버] "+line);

                        String[] response = line.split("ㅣ");
                        if("join".equals(response[0])){ // 게임방 참여
                            final String join_member_id = response[1];
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),join_member_id+"님 참여",Toast.LENGTH_SHORT).show();
                                    if(game_join_user_arr[0]==null){
                                        game_join_user_arr[0]=join_member_id;
                                        user0_id_tv.setText("id : "+join_member_id);
                                        user0_ready_tv.setVisibility(View.VISIBLE);
                                        ready_state_arr[0]=false;
                                    }else if(game_join_user_arr[1]==null){
                                        game_join_user_arr[1]=join_member_id;
                                        user1_id_tv.setText("id : "+join_member_id);
                                        user1_ready_tv.setVisibility(View.VISIBLE);
                                        ready_state_arr[1]=false;
                                    }else if(game_join_user_arr[2]==null){
                                        game_join_user_arr[2]=join_member_id;
                                        user2_id_tv.setText("id : "+join_member_id);
                                        user2_ready_tv.setVisibility(View.VISIBLE);
                                        ready_state_arr[2]=false;
                                    }else if(game_join_user_arr[3]==null){
                                        game_join_user_arr[3]=join_member_id;
                                        user3_id_tv.setText("id : "+join_member_id);
                                        user3_ready_tv.setVisibility(View.VISIBLE);
                                        ready_state_arr[3]=false;
                                    }
                                    game_join_user = game_join_user_arr_To_game_join_user(game_join_user_arr);
                                }
                            });

                        }else if("quit".equals(response[0])) { // 게임방 나감
                            final String quit_member_id = response[1];
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
//                                    Toast.makeText(getApplicationContext(), quit_member_id + "님 나감", Toast.LENGTH_SHORT).show();
                                    for (int i = 0; i < game_join_user_arr.length; i++) {
                                        if (quit_member_id.equals(game_join_user_arr[i])) {
                                            game_join_user_arr[i] = null;
                                            if (i == 0) {
                                                user0_id_tv.setText("기다리는중..");
                                                user0_ready_tv.setText("준비해주세요!!");
                                                user0_ready_tv.setVisibility(GONE);
                                                ready_state_arr[0]=false;
                                            } else if (i == 1) {
                                                user1_id_tv.setText("기다리는중..");
                                                user1_ready_tv.setText("준비해주세요!!");
                                                user1_ready_tv.setVisibility(GONE);
                                                ready_state_arr[1]=false;
                                            } else if (i == 2) {
                                                user2_id_tv.setText("기다리는중..");
                                                user2_ready_tv.setText("준비해주세요!!");
                                                user2_ready_tv.setVisibility(GONE);
                                                ready_state_arr[2]=false;
                                            } else if (i == 3) {
                                                user3_id_tv.setText("기다리는중..");
                                                user3_ready_tv.setText("준비해주세요!!");
                                                user3_ready_tv.setVisibility(GONE);
                                                ready_state_arr[3]=false;
                                            }
                                        }
                                    }
                                    game_join_user = game_join_user_arr_To_game_join_user(game_join_user_arr);
                                }
                            });

                        }else if("ready".equals(response[0])){ // 준비
                            final String ready_member_id = response[1];
                            random = response[2];
                            Log.d(TAG,ready_member_id+"님 준비 버튼 클릭! [소켓]");

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), ready_member_id + "님 준비", Toast.LENGTH_SHORT).show();
                                    for (int i = 0; i < game_join_user_arr.length; i++) {
                                        if (ready_member_id.equals(game_join_user_arr[i])) {
                                            if (i == 0) {
                                                user0_ready_tv.setVisibility(View.VISIBLE);
                                                user0_ready_tv.setText("준비완료!!");
                                                ready_state_arr[0]=true;
                                            } else if (i == 1) {
                                                user1_ready_tv.setVisibility(View.VISIBLE);
                                                user1_ready_tv.setText("준비완료!!");
                                                ready_state_arr[1]=true;
                                            } else if (i == 2) {
                                                user2_ready_tv.setVisibility(View.VISIBLE);
                                                user2_ready_tv.setText("준비완료!!");
                                                ready_state_arr[2]=true;
                                            } else if (i == 3) {
                                                user3_ready_tv.setVisibility(View.VISIBLE);
                                                user3_ready_tv.setText("준비완료!!");
                                                ready_state_arr[3]=true;
                                            }
                                        }
                                    }

                                    if(ready_state_arr[0]!=null && ready_state_arr[1]!=null && ready_state_arr[2]!=null && ready_state_arr[3]!=null){
                                        if(ready_state_arr[0] && ready_state_arr[1] && ready_state_arr[2] && ready_state_arr[3]){ // game_start
                                            is_game_start = true;
                                            Toast.makeText(getApplicationContext(),"게임이 곧 시작됩니다. 옷을 입어주세요!!",Toast.LENGTH_SHORT).show();
                                            Log.d(TAG,"random : "+random);
                                            String[] random_arr = random.split(",");

                                            /*
                                                1. cat
                                                2. heart
                                                3. owl
                                                4. koo
                                             */

                                            if("1".equals(random_arr[0])){
                                                user0_clothes_iv.setImageResource(R.drawable.cat);
                                            }else if("1".equals(random_arr[1])){
                                                user1_clothes_iv.setImageResource(R.drawable.cat);
                                            }else if("1".equals(random_arr[2])){
                                                user2_clothes_iv.setImageResource(R.drawable.cat);
                                            }else if("1".equals(random_arr[3])){
                                                user3_clothes_iv.setImageResource(R.drawable.cat);
                                            }

                                            if("2".equals(random_arr[0])){
                                                user0_clothes_iv.setImageResource(R.drawable.heart);
                                            }else if("2".equals(random_arr[1])){
                                                user1_clothes_iv.setImageResource(R.drawable.heart);
                                            }else if("2".equals(random_arr[2])){
                                                user2_clothes_iv.setImageResource(R.drawable.heart);
                                            }else if("2".equals(random_arr[3])){
                                                user3_clothes_iv.setImageResource(R.drawable.heart);
                                            }

                                            if("3".equals(random_arr[0])){
                                                user0_clothes_iv.setImageResource(R.drawable.owl);
                                            }else if("3".equals(random_arr[1])){
                                                user1_clothes_iv.setImageResource(R.drawable.owl);
                                            }else if("3".equals(random_arr[2])){
                                                user2_clothes_iv.setImageResource(R.drawable.owl);
                                            }else if("3".equals(random_arr[3])){
                                                user3_clothes_iv.setImageResource(R.drawable.owl);
                                            }

                                            if("4".equals(random_arr[0])){
                                                user0_clothes_iv.setImageResource(R.drawable.koo);
                                            }else if("4".equals(random_arr[1])){
                                                user1_clothes_iv.setImageResource(R.drawable.koo);
                                            }else if("4".equals(random_arr[2])){
                                                user2_clothes_iv.setImageResource(R.drawable.koo);
                                            }else if("4".equals(random_arr[3])){
                                                user3_clothes_iv.setImageResource(R.drawable.koo);
                                            }

                                            /*
                                                타이머 시작
                                             */
                                            Log.d(TAG,"타이머 시작!!");
                                            timer_tv.setVisibility(View.VISIBLE);
                                            new Thread(){
                                                @Override
                                                public void run(){
                                                    try{Thread.sleep(1000);}catch (Exception e){}
                                                    handler.sendEmptyMessage(10);
                                                    try{Thread.sleep(1000);}catch (Exception e){}
                                                    handler.sendEmptyMessage(9);
                                                    try{Thread.sleep(1000);}catch (Exception e){}
                                                    handler.sendEmptyMessage(8);
                                                    try{Thread.sleep(1000);}catch (Exception e){}
                                                    handler.sendEmptyMessage(7);
                                                    try{Thread.sleep(1000);}catch (Exception e){}
                                                    handler.sendEmptyMessage(6);
                                                    try{Thread.sleep(1000);}catch (Exception e){}
                                                    handler.sendEmptyMessage(5);
                                                    try{Thread.sleep(1000);}catch (Exception e){}
                                                    handler.sendEmptyMessage(4);
                                                    try{Thread.sleep(1000);}catch (Exception e){}
                                                    handler.sendEmptyMessage(3);
                                                    try{Thread.sleep(1000);}catch (Exception e){}
                                                    handler.sendEmptyMessage(2);
                                                    try{Thread.sleep(1000);}catch (Exception e){}
                                                    handler.sendEmptyMessage(1);
                                                    try{Thread.sleep(1000);}catch (Exception e){}
                                                    handler.sendEmptyMessage(0);
                                                }
                                            }.start();
                                            // 화면 전환
                                        }
                                    }

                                }
                            });

                        }

                    } // end 소켓에서 메시지 받는 부준
                } // end while loop

            }catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG,"IOException : "+e.toString());
            }catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"Exception : "+e.toString());
            }finally {
                try {
                    if(pw != null) {
                        pw.close();
                    }
                    if(br != null) {
                        br.close();
                    }
                    if(socket != null) {
                        socket.close();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        public void setIs_stop(){
            Log.d(TAG, "setIs_stop()");
            this.is_stop=true;
        }
    } // RecieveThread 클래스

    /**
     * 소켓과 연결 해제하는 메소드
     */
    void disconnectWithSocket(){
        /*
         * 소켓과 연결 종료
         */
        new Thread(){
            public void run(){
                try {
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                    String request = "quit¡"+game_join_user+"ㅣ\r\n";
                    pw.println(request);
                }catch (IOException e) {
                    e.printStackTrace();
                }catch (NullPointerException e){
                    e.printStackTrace();
                    Log.d(TAG,"java.lang.NullPointerException: Attempt to invoke virtual method 'java.io.OutputStream java.net.Socket.getOutputStream()' on a null object reference at com.example.realtrip.service.ChatService$cat.run");
                }
            }
        }.start();
    } // disconnectWithSocket() 메소드

} // StandByActivity class