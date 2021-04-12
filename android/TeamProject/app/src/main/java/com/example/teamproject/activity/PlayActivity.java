package com.example.teamproject.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.teamproject.R;
import com.example.teamproject.etc.Camera2APIs;
import com.example.teamproject.etc.Retrofit;
import com.example.teamproject.etc.UploadImage;
import com.example.teamproject.object.Member;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import static android.view.View.GONE;

public class PlayActivity extends AppCompatActivity implements Camera2APIs.Camera2Interface, TextureView.SurfaceTextureListener{

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그


//    add
    private TextureView mTextureView;
    private Camera2APIs mCamera;
    private boolean chooseClick=false;

    //화살쪽
    private ImageView ic_aim1_btn;
    public LottieAnimationView fail;
    LottieAnimationView animationView1;
    ImageView user0_clothes_iv;
    ImageView user1_clothes_iv;
    ImageView user2_clothes_iv;
    ImageView user3_clothes_iv;
    TextView user0_id_tv;
    TextView user1_id_tv;
    TextView user2_id_tv;
    TextView user3_id_tv;
    TextView user0_life_tv;
    TextView user1_life_tv;
    TextView user2_life_tv;
    TextView user3_life_tv;
    ImageView colony1_iv;
    ImageView colony2_iv;
    ImageView colony3_iv;
    ImageView colony4_iv;
    Button fire_btn;
    TextView timer_tv;

    String game_join_user;
    String[] game_join_user_arr;
    String random;
    String[] random_arr;

    Member login_member;

    Socket socket; // 소켓
    ReceiveThread receiveThread; // 소켓으로부터 오는 메시지를 읽기위한 쓰레드

    MyTimer myTimer; // 타이머

    private HashMap<String, String[]> game_state_hm;
    String[] colony_arr;

    int login_member_position; // 로그인 멤버 위치
    int cat_position; // 고양이 위치
    int heart_position; // 하트 위치
    int owl_position; // 부엉이 위치
    int koo_position; // 짱구 위치

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window win = getWindow();
        win.setContentView(R.layout.activity_play);


        //클릭 애니메이션
        animationView1 = findViewById(R.id.animation_view1);
      //  setUpAnimation(animationView1);
        /*
         * 쉐어드에 저장된 로그인 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData", MODE_PRIVATE);
        Gson gson = new Gson();
        login_member = gson.fromJson(sharedPreferences.getString("login_member", "no_login"), Member.class);

        user0_clothes_iv = findViewById(R.id.user0_clothes_iv); // 유저 옷
        user1_clothes_iv = findViewById(R.id.user1_clothes_iv);
        user2_clothes_iv = findViewById(R.id.user2_clothes_iv);
        user3_clothes_iv = findViewById(R.id.user3_clothes_iv);
        user0_id_tv = findViewById(R.id.user0_id_tv); // 유저 아이디
        user1_id_tv = findViewById(R.id.user1_id_tv);
        user2_id_tv = findViewById(R.id.user2_id_tv);
        user3_id_tv = findViewById(R.id.user3_id_tv);
        user0_life_tv = findViewById(R.id.user0_life_tv); // 유저 생명
        user1_life_tv = findViewById(R.id.user1_life_tv);
        user2_life_tv = findViewById(R.id.user2_life_tv);
        user3_life_tv = findViewById(R.id.user3_life_tv);
        colony1_iv = findViewById(R.id.colony1_iv); // 점령지
        colony2_iv = findViewById(R.id.colony2_iv);
        colony3_iv = findViewById(R.id.colony3_iv);
        colony4_iv = findViewById(R.id.colony4_iv);
        mTextureView = findViewById(R.id.textureView); // 카메라 뷰
        mTextureView.setSurfaceTextureListener(this);
        fire_btn = findViewById(R.id.fire_btn); // 총발사 버튼
        ic_aim1_btn = findViewById(R.id.ic_aim1_btn); //frame
        timer_tv = findViewById(R.id.timer_tv); // 타이머 텍스트뷰
        myTimer = new MyTimer(240000,1000);
//        myTimer = new MyTimer(30000, 1000);
        myTimer.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 롤리팝 버전 이상이면
            mCamera = new Camera2APIs(this);
        }

        // ~~~~~~~ 실  제 ~~~~~~~ //
        Intent intent = getIntent(); // 게임 참여자, 참여자 옷 가지고오기
        game_join_user = intent.getStringExtra("game_join_user");
        random = intent.getStringExtra("random");
        // ~~~~~~~ 실  제 ~~~~~~~ //
        // ~~~~~~~ 테스트용 ~~~~~~~ //
//        game_join_user = "a,b,c,d";
//        random = "1,2,3,4";
        // ~~~~~~~ 테스트용 ~~~~~~~ //

        game_join_user_arr = game_join_user.split(",");
        random_arr = random.split(",");
        Log.d(TAG, "game_join_user : " + game_join_user + ", random : " + random);

        // 로그인 멤버 포지션 찾기
        for (int i = 0; i < game_join_user_arr.length; i++) {
            if (login_member.member_id.equals(game_join_user_arr[i])) {
                login_member_position = i;
            }
        }


        if (login_member_position == 0) {
            user0_id_tv.setTextColor(getResources().getColor(R.color.yellow));
            user0_id_tv.setBackgroundColor(getResources().getColor(R.color.blue));
        } else if (login_member_position == 1) {
            user1_id_tv.setTextColor(getResources().getColor(R.color.yellow));
            user1_id_tv.setBackgroundColor(getResources().getColor(R.color.blue));
        } else if (login_member_position == 2) {
            user2_id_tv.setTextColor(getResources().getColor(R.color.yellow));
            user2_id_tv.setBackgroundColor(getResources().getColor(R.color.blue));
        } else if (login_member_position == 3) {
            user3_id_tv.setTextColor(getResources().getColor(R.color.yellow));
            user3_id_tv.setBackgroundColor(getResources().getColor(R.color.blue));
        }

        colony_arr = new String[4]; // 점령지 초기화
        for (int i = 0; i < colony_arr.length; i++) {
            colony_arr[i] = "null";
        }

        receiveThread = new ReceiveThread(); // 소켓 연결 및 수신 대기
        receiveThread.start();

        user0_id_tv.setText(game_join_user_arr[0]); // 유저 아이디 ui 동기화
        user1_id_tv.setText(game_join_user_arr[1]);
        user2_id_tv.setText(game_join_user_arr[2]);
        user3_id_tv.setText(game_join_user_arr[3]);

        Log.d(TAG, "game_join_user_arr 확인");
        for (int i = 0; i < 4; i++) {
            Log.d(TAG, i + "번째  >  " + game_join_user_arr[i]);
        }
        Log.d(TAG, "random_arr 확인");
        for (int i = 0; i < 4; i++) {
            Log.d(TAG, i + "번째  >  " + random_arr[i]);
        }


        game_state_hm = new HashMap<>(); // 게임 상황판 {key : 멤버 위치 (0), value : 멤버 아이디, 옷, 생명}

        if ("1".equals(random_arr[0])) {
            user0_clothes_iv.setImageResource(R.drawable.cat); // 해당 유저의 옷 ui 동기화
            String[] str_arr = {game_join_user_arr[0], "cat", "3"};
            game_state_hm.put("0", str_arr); // 게임 상황판 동기화
        } else if ("2".equals(random_arr[0])) {
            user0_clothes_iv.setImageResource(R.drawable.heart);
            String[] str_arr = {game_join_user_arr[0], "heart", "3"};
            game_state_hm.put("0", str_arr);
        } else if ("3".equals(random_arr[0])) {
            user0_clothes_iv.setImageResource(R.drawable.owl);
            String[] str_arr = {game_join_user_arr[0], "owl", "3"};
            game_state_hm.put("0", str_arr);
        } else if ("4".equals(random_arr[0])) {
            user0_clothes_iv.setImageResource(R.drawable.koo);
            String[] str_arr = {game_join_user_arr[0], "koo", "3"};
            game_state_hm.put("0", str_arr);
        }

        if ("1".equals(random_arr[1])) {
            user1_clothes_iv.setImageResource(R.drawable.cat);
            String[] str_arr = {game_join_user_arr[1], "cat", "3"};
            game_state_hm.put("1", str_arr);
        } else if ("2".equals(random_arr[1])) {
            user1_clothes_iv.setImageResource(R.drawable.heart);
            String[] str_arr = {game_join_user_arr[1], "heart", "3"};
            game_state_hm.put("1", str_arr);
        } else if ("3".equals(random_arr[1])) {
            user1_clothes_iv.setImageResource(R.drawable.owl);
            String[] str_arr = {game_join_user_arr[1], "owl", "3"};
            game_state_hm.put("1", str_arr);
        } else if ("4".equals(random_arr[1])) {
            user1_clothes_iv.setImageResource(R.drawable.koo);
            String[] str_arr = {game_join_user_arr[1], "koo", "3"};
            game_state_hm.put("1", str_arr);
        }

        if ("1".equals(random_arr[2])) {
            user2_clothes_iv.setImageResource(R.drawable.cat);
            String[] str_arr = {game_join_user_arr[2], "cat", "3"};
            game_state_hm.put("2", str_arr);
        } else if ("2".equals(random_arr[2])) {
            user2_clothes_iv.setImageResource(R.drawable.heart);
            String[] str_arr = {game_join_user_arr[2], "heart", "3"};
            game_state_hm.put("2", str_arr);
        } else if ("3".equals(random_arr[2])) {
            user2_clothes_iv.setImageResource(R.drawable.owl);
            String[] str_arr = {game_join_user_arr[2], "owl", "3"};
            game_state_hm.put("2", str_arr);
        } else if ("4".equals(random_arr[2])) {
            user2_clothes_iv.setImageResource(R.drawable.koo);
            String[] str_arr = {game_join_user_arr[2], "koo", "3"};
            game_state_hm.put("2", str_arr);
        }


        if ("1".equals(random_arr[3])) {
            user3_clothes_iv.setImageResource(R.drawable.cat);
            String[] str_arr = {game_join_user_arr[3], "cat", "3"};
            game_state_hm.put("3", str_arr);
        } else if ("2".equals(random_arr[3])) {
            user3_clothes_iv.setImageResource(R.drawable.heart);
            String[] str_arr = {game_join_user_arr[3], "heart", "3"};
            game_state_hm.put("3", str_arr);
        } else if ("3".equals(random_arr[3])) {
            user3_clothes_iv.setImageResource(R.drawable.owl);
            String[] str_arr = {game_join_user_arr[3], "owl", "3"};
            game_state_hm.put("3", str_arr);
        } else if ("4".equals(random_arr[3])) {
            user3_clothes_iv.setImageResource(R.drawable.koo);
            String[] str_arr = {game_join_user_arr[3], "koo", "3"};
            game_state_hm.put("3", str_arr);
        }

        if ("cat".equals(game_state_hm.get("0")[1])) {
            cat_position = 0;
        } else if ("cat".equals(game_state_hm.get("1")[1])) {
            cat_position = 1;
        } else if ("cat".equals(game_state_hm.get("2")[1])) {
            cat_position = 2;
        } else if ("cat".equals(game_state_hm.get("3")[1])) {
            cat_position = 3;
        }

        if ("heart".equals(game_state_hm.get("0")[1])) {
            heart_position = 0;
        } else if ("heart".equals(game_state_hm.get("1")[1])) {
            heart_position = 1;
        } else if ("heart".equals(game_state_hm.get("2")[1])) {
            heart_position = 2;
        } else if ("heart".equals(game_state_hm.get("3")[1])) {
            heart_position = 3;
        }

        if ("koo".equals(game_state_hm.get("0")[1])) {
            koo_position = 0;
        } else if ("koo".equals(game_state_hm.get("1")[1])) {
            koo_position = 1;
        } else if ("koo".equals(game_state_hm.get("2")[1])) {
            koo_position = 2;
        } else if ("koo".equals(game_state_hm.get("3")[1])) {
            koo_position = 3;
        }

        if ("owl".equals(game_state_hm.get("0")[1])) {
            owl_position = 0;
        } else if ("owl".equals(game_state_hm.get("1")[1])) {
            owl_position = 1;
        } else if ("owl".equals(game_state_hm.get("2")[1])) {
            owl_position = 2;
        } else if ("owl".equals(game_state_hm.get("3")[1])) {
            owl_position = 3;
        }

        Log.d(TAG, "game_state_hm.get(0) 확인");
        for (int i = 0; i < 3; i++) {
            Log.d(TAG, game_state_hm.get("0")[i]);
        }
        Log.d(TAG, "game_state_hm.get(1) 확인");
        for (int i = 0; i < 3; i++) {
            Log.d(TAG, game_state_hm.get("1")[i]);
        }
        Log.d(TAG, "game_state_hm.get(2) 확인");
        for (int i = 0; i < 3; i++) {
            Log.d(TAG, game_state_hm.get("2")[i]);
        }
        Log.d(TAG, "game_state_hm.get(3) 확인");
        for (int i = 0; i < 3; i++) {
            Log.d(TAG, game_state_hm.get("3")[i]);
        }


    } // onCreate()

    //발사
    private void setUpAnimation1(LottieAnimationView animationView) {
        // 재생할 애니메이션 넣어준다.
        animationView.setAnimation("10410-uploading.json");
        // 반복횟수를 무한히 주고 싶을 땐 LottieDrawable.INFINITE or 원하는 횟수
        animationView.setRepeatCount(1);

        // 시작
        animationView.playAnimation();
    }
    //클릭 애니메이션
    private void setUpAnimation(LottieAnimationView animationView) {
        // 재생할 애니메이션 넣어준다.
        animationView.setAnimation("5798-the-shooting.json");
        // 반복횟수를 무한히 주고 싶을 땐 LottieDrawable.INFINITE or 원하는 횟수
        animationView.setRepeatCount(LottieDrawable.INFINITE);
        // 시작
        animationView.playAnimation();
    }
    //성공 애니메이션
    private void success(LottieAnimationView animationView) {
        // 재생할 애니메이션 넣어준다.
        animationView.setAnimation("6902.json");
        // 반복횟수를 무한히 주고 싶을 땐 LottieDrawable.INFINITE or 원하는 횟수
        animationView.setRepeatCount(1);
        // 시작
        animationView.playAnimation();
    }
    //실패 애니메이션
    private void fail(LottieAnimationView animationView) {
        // 재생할 애니메이션 넣어준다.
        animationView.setAnimation("294-delete-slash.json");
        // 반복횟수를 무한히 주고 싶을 땐 LottieDrawable.INFINITE or 원하는 횟수
        animationView.setRepeatCount(1);
        // 시작
        animationView.playAnimation();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //fire_btn
        //클릭 애니메이션
        animationView1 = findViewById(R.id.animation_view1);

        fire_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Log.e("HANDLER", "run: Outside Runnable");

              //  final Handler handler = new Handler();
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        ic_aim1_btn.setColorFilter(Color.parseColor("#FF0003"), PorterDuff.Mode.SRC_IN);
                        double a=0.8;
                        ic_aim1_btn.setAlpha((float) a);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i("what?","up!");
                        chooseClick=true;
                        double ab=0;
                        ic_aim1_btn.setAlpha((float) ab);
                        //발사할 때 애니메이션
                        LottieAnimationView animationView = findViewById(R.id.animation_view);
                        setUpAnimation1(animationView);
                        animationView1.setVisibility(GONE);
                        break;
                }

                return false;
            }
        });



        // ~~~~~~ test ~~~~~~ //
//        Button attack0_btn = findViewById(R.id.attack0_btn);
//        attack0_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Thread(){
//                    @Override
//                    public void run(){
//                        try{
//                            String[] str_arr = game_state_hm.get("0");


//                            Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
//                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
//                            pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":0\r\n"); // 준비상태 소켓에 보내기
//                            pw.flush();
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
//            }
//        });
//        Button attack1_btn = findViewById(R.id.attack1_btn);
//        attack1_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Thread(){
//                    @Override
//                    public void run(){
//                        try{
//                            String[] str_arr = game_state_hm.get("1");
//                            Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
//                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
//                            pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":1\r\n"); // 준비상태 소켓에 보내기
//                            pw.flush();
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
//            }
//        });
//        Button attack2_btn = findViewById(R.id.attack2_btn);
//        attack2_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Thread(){
//                    @Override
//                    public void run(){
//                        try{
//                            String[] str_arr = game_state_hm.get("2");
//                            Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
//                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
//                            pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":2\r\n"); // 준비상태 소켓에 보내기
//                            pw.flush();
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
//            }
//        });
//        Button attack3_btn = findViewById(R.id.attack3_btn);
//        attack3_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Thread(){
//                    @Override
//                    public void run(){
//                        try{
//                            String[] str_arr = game_state_hm.get("3");
//                            Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
//                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
//                            pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":3\r\n"); // 준비상태 소켓에 보내기
//                            pw.flush();
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
//            }
//        });
//        Button colony1_btn = findViewById(R.id.colony1_btn);
//        colony1_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Thread(){
//                    @Override
//                    public void run(){
//                        try{
//                            Log.d(TAG,login_member.member_id+"님이 colony1 점령!!");
//                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
//                            pw.println("event2:"+login_member.member_id+":colony1:"+login_member_position+"\r\n"); // 준비상태 소켓에 보내기
//                            pw.flush();
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
//            }
//        });
//        Button colony2_btn = findViewById(R.id.colony2_btn);
//        colony2_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Thread(){
//                    @Override
//                    public void run(){
//                        try{
//                            Log.d(TAG,login_member.member_id+"님이 colony2 점령!!");
//                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
//                            pw.println("event2:"+login_member.member_id+":colony2:"+login_member_position+"\r\n"); // 준비상태 소켓에 보내기
//                            pw.flush();
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
//            }
//        });
//        Button colony3_btn = findViewById(R.id.colony3_btn);
//        colony3_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Thread(){
//                    @Override
//                    public void run(){
//                        try{
//                            Log.d(TAG,login_member.member_id+"님이 colony3 점령!!");
//                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
//                            pw.println("event2:"+login_member.member_id+":colony3:"+login_member_position+"\r\n"); // 준비상태 소켓에 보내기
//                            pw.flush();
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
//            }
//        });
//        Button colony4_btn = findViewById(R.id.colony4_btn);
//        colony4_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Thread(){
//                    @Override
//                    public void run(){
//                        try{
//                            Log.d(TAG,login_member.member_id+"님이 colony4 점령!!");
//                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
//                            pw.println("event2:"+login_member.member_id+":colony4:"+login_member_position+"\r\n"); // 준비상태 소켓에 보내기
//                            pw.flush();
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
//            }
//        });

    } // onStart()

    protected void onResume() {
        super.onResume();
        if (mTextureView.isAvailable()) {
            openCamera(); // 카메라 오픈
        } else {
            mTextureView.setSurfaceTextureListener(this);
        }
    } // onResume()

    @Override
    protected void onPause() {
        closeCamera(); // 카메라 close
        super.onPause();
    } // onPause()

    /* Surface Callbacks */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        openCamera();
    } // onSurfaceTextureAvailable()

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
    } // onSurfaceTextureSizeChanged()

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    } // onSurfaceTextureDestroyed()

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        if(chooseClick==false){
        }else {

            // 버튼 비활성화
            fire_btn.setText("장전중!!");
            fire_btn.setEnabled(false);

            // 이미지 만듬
            Bitmap bmp = mTextureView.getBitmap();
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
            bmp.getPixels(pixels, 0, width, 0, 0, width, height);
            save(bmp);
            chooseClick=false;
        }
    } // onSurfaceTextureUpdated()


    public void save(Bitmap bmp){
        String filename = "a.jpg";
        File file = new File(Environment.getExternalStorageDirectory().getPath(), filename);
        //껍데기 파일에 이미지 저장하기
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //사진 90도 돌리고 , 좌우 반전 돌려서 저장하기

        // bitmap 파일 jpeg파일로 만들기
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);

        try {
            out.close();
            UploadImage uploadImage= Retrofit.getRetrofit2().create(UploadImage.class);
            // Log.i("filepath",file.getAbsolutePath());
            RequestBody requestFile=RequestBody.create(MediaType.parse("multipart/form-data"),file);
            MultipartBody.Part body1=MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            RequestBody id = RequestBody.create(MediaType.parse("multipart/form-data"), "a");
            Call<ResponseBody> call=uploadImage.uploadFile1(id,body1);

            call.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
//                        Log.i("success",response.body().string());

                        String end=response.body().string();

                        fire_btn.setText("총 발사!!");
                        fire_btn.setEnabled(true);

                        Log.d(TAG,end.length()+"");
//                        Toast.makeText(getApplicationContext(),"응답 : "+end,Toast.LENGTH_LONG).show();
                        // 여기에 결과 실행
                        String result = end.split("'")[1];
                        Log.d(TAG,"end.split(\"'\")[1] : "+result);
                        result = result.substring(0,result.length()-2);
                        Log.d(TAG,"result.substring(0,result.length()-2) : "+result);

                        if("test1".equals(result)){ // 실패
                            // 실패 애니메이션
                            //실패 애니메이션 핸들러 선언
                            final Handler handler = new Handler();
                            fail = findViewById(R.id.fail);
                            handler.postDelayed(new Runnable() {

                                //실패 애니메이션
                         //       LottieAnimationView fail = findViewById(R.id.fail);


                                @Override
                                public void run() {
                                    fail.setVisibility(View.VISIBLE);
                                    fail(fail);

                                }




                                //3초뒤에 사라짐
                            }, 500);

                            handler.removeMessages(1000);
                        }else{ // 성공

                            //승리 애니메이션 핸들러 선언
                            final Handler handler = new Handler();

                            handler.postDelayed(new Runnable() {

                                //승리 애니메이션
                                LottieAnimationView animationView11 = findViewById(R.id.success);


                                @Override
                                public void run() {
                                    animationView11.setVisibility(View.VISIBLE);
                                    success(animationView11);

                                    Log.e("    s "+animationView11,"    ");
                                }
                                //3초뒤에 사라짐
                            }, 500);

                            handler.removeMessages(1000);

                            // 성공 애니메이션

                            if("cat".equals(result)){ // 고양이 공격
                                if(cat_position==0){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("0");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":0\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }else if(cat_position==1){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("1");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":1\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }else if(cat_position==2){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("2");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":2\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }else if(cat_position==3){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("3");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":3\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }

                            }else if("heart".equals(result)){ // 하트 공격
                                if(heart_position==0){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("0");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":0\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }else if(heart_position==1){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("1");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":1\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }else if(heart_position==2){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("2");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":2\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }else if(heart_position==3){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("3");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":3\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }
                            }else if("koo".equals(result)){ // 짱구 공격
                                if(koo_position==0){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("0");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":0\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }else if(koo_position==1){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("1");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":1\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }else if(koo_position==2){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("2");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":2\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }else if(koo_position==3){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("3");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":3\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }
                            }else if("owl".equals(result)){ // 부엉이 공격
                                if(owl_position==0){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("0");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":0\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }else if(owl_position==1){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("1");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":1\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }else if(owl_position==2){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("2");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":2\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }else if(owl_position==3){
                                    new Thread(){
                                        @Override
                                        public void run(){
                                            try{
                                                String[] str_arr = game_state_hm.get("3");
                                                Log.d(TAG,str_arr[0]+"님의 옷 "+str_arr[1]+"인식 됨");
                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                pw.println("event1:"+login_member.member_id+":"+str_arr[0]+":3\r\n"); // 준비상태 소켓에 보내기
                                                pw.flush();
                                            }catch (IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }
                            }else if("colony1".equals(result)){ // 점령지1 점령
                                new Thread(){
                                    @Override
                                    public void run(){
                                        try{
                                            Log.d(TAG,login_member.member_id+"님이 colony1 점령!!");
                                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                            pw.println("event2:"+login_member.member_id+":colony1:"+login_member_position+"\r\n"); // 준비상태 소켓에 보내기
                                            pw.flush();
                                        }catch (IOException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }else if("colony2".equals(result)){ // 점령지2 점령
                                new Thread(){
                                    @Override
                                    public void run(){
                                        try{
                                            Log.d(TAG,login_member.member_id+"님이 colony2 점령!!");
                                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                            pw.println("event2:"+login_member.member_id+":colony2:"+login_member_position+"\r\n"); // 준비상태 소켓에 보내기
                                            pw.flush();
                                        }catch (IOException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }else if("colony3".equals(result)){ // 점령지3 점령
                                new Thread(){
                                    @Override
                                    public void run(){
                                        try{
                                            Log.d(TAG,login_member.member_id+"님이 colony3 점령!!");
                                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                            pw.println("event2:"+login_member.member_id+":colony3:"+login_member_position+"\r\n"); // 준비상태 소켓에 보내기
                                            pw.flush();
                                        }catch (IOException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }else if("colony4".equals(result)){ // 점령지4 점령
                                new Thread(){
                                    @Override
                                    public void run(){
                                        try{
                                            Log.d(TAG,login_member.member_id+"님이 colony4 점령!!");
                                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                            pw.println("event2:"+login_member.member_id+":colony4:"+login_member_position+"\r\n"); // 준비상태 소켓에 보내기
                                            pw.flush();
                                        }catch (IOException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                        }



                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i("fail",t.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // save()

    private void openCamera() {
        CameraManager cameraManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraManager = mCamera.CameraManager_1(this);
        }
        String cameraId = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraId = mCamera.CameraCharacteristics_2(cameraManager);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCamera.CameraDevice_3(cameraManager, cameraId);
        }
        // Log.d("test111","6");
    } // openCamera()

    @Override
    public void onCameraDeviceOpened(CameraDevice cameraDevice, Size cameraSize) {
        Log.d("test111","3");
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            texture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
        }
        Surface surface = new Surface(texture);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCamera.CaptureSession_4(cameraDevice, surface);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCamera.CaptureRequest_5(cameraDevice, surface);
        }
    } // onCameraDeviceOpened()

    private void closeCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCamera.closeCamera();
        }
    } // closeCamera()

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
            String server_ip = "35.243.90.95";

            BufferedReader br = null;
            PrintWriter pw = null;

            try {

                socket = new Socket(server_ip, 5005); // 소켓 연결

                pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                /*
                 * 게임 참여
                 */
                String join_request = "join:"+login_member.member_id+":";
                for(int i=0;i<game_join_user_arr.length;i++){
                    if(!login_member.member_id.equals(game_join_user_arr[i])){
                        if(i==game_join_user_arr.length-1){
                            join_request = join_request + game_join_user_arr[i];
                        }else{
                            join_request = join_request + game_join_user_arr[i]+":";
                        }
                    }
                }
                Log.d(TAG,"join_request : "+join_request);
                pw.println(join_request);
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

                        String[] response = line.split(":");

                        if("event1".equals(response[0])){ // 캐릭터 인식
                            final String attack_member_id = response[1]; // 공격한 멤버 아이디
                            final String attacked_member_id = response[2]; // 공격당한 멤버 아이디
                            final String attacked_member_position = response[3]; // 공격당한 멤버 위치
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
//                                    Toast.makeText(getApplicationContext(),attack_member_id+"님이 "+attacked_member_id+"님을 공격하였습니다!",Toast.LENGTH_SHORT).show();
                                    int temp_life = Integer.parseInt(game_state_hm.get(attacked_member_position)[2]);
                                    Log.d(TAG,"game_state_hm.get(attacked_member_position)[0] : "+game_state_hm.get(attacked_member_position)[0]);
                                    Log.d(TAG,"game_state_hm.get(attacked_member_position)[1] : "+game_state_hm.get(attacked_member_position)[1]);
                                    Log.d(TAG,"game_state_hm.get(attacked_member_position)[2] : "+game_state_hm.get(attacked_member_position)[2]);
                                    temp_life--;
                                    game_state_hm.get(attacked_member_position)[2] = String.valueOf(Integer.parseInt(game_state_hm.get(attacked_member_position)[2])-1);
                                    Log.d(TAG,"game_state_hm.get(attacked_member_position)[2] : "+game_state_hm.get(attacked_member_position)[2]);
                                    if(temp_life==0){ // 죽음
                                        if(Integer.parseInt(attacked_member_position)==0){
                                            user0_life_tv.setTextColor(getResources().getColor(R.color.red));
                                            user0_life_tv.setText("죽 음");
                                        }else if(Integer.parseInt(attacked_member_position)==1){
                                            user1_life_tv.setTextColor(getResources().getColor(R.color.red));
                                            user1_life_tv.setText("죽 음");
                                        }else if(Integer.parseInt(attacked_member_position)==2){
                                            user2_life_tv.setTextColor(getResources().getColor(R.color.red));
                                            user2_life_tv.setText("죽 음");
                                        }else if(Integer.parseInt(attacked_member_position)==3){
                                            user3_life_tv.setTextColor(getResources().getColor(R.color.red));
                                            user3_life_tv.setText("죽 음");
                                        }

                                        /*
                                            죽은 멤버가 자신인 경우
                                         */
                                        if(attacked_member_id.equals(login_member.member_id)){ // 죽은 멤버가 자신인 경우
                                            AlertDialog.Builder builder = new AlertDialog.Builder(PlayActivity.this);
                                            builder.setTitle("런닝맨");
                                            builder.setMessage("당신은 죽었습니다.");
                                            builder.setNegativeButton("머물기", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                            builder.setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // 소켓 해제
                                                    new Thread(){
                                                        @Override
                                                        public void run(){
                                                            try{
                                                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                                pw.println("event3:bye\r\n"); // 준비상태 소켓에 보내기
                                                                pw.flush();
                                                            }catch (IOException e){
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }.start();
                                                    if(receiveThread!=null){
                                                        receiveThread.setIs_stop();
                                                        receiveThread=null;
                                                    }
                                                    finish();
                                                }
                                            });
                                            builder.show(); // 다이얼로그 보이게 하기
                                            fire_btn.setVisibility(GONE);

                                        } // 죽은 멤버가 자신인 경우

                                        /*
                                            3명이 죽어서 게임이 끝난 경우
                                         */
                                        int survivor_count = 0;
                                        int survivor_member_position = -1;
                                        if(!game_state_hm.get("0")[2].equals("0")){ // user0 생존
                                            survivor_count++;
                                            survivor_member_position=0;
                                        }
                                        if(!game_state_hm.get("1")[2].equals("0")){ // user1 생존
                                            survivor_count++;
                                            survivor_member_position=1;
                                        }
                                        if(!game_state_hm.get("2")[2].equals("0")){ // user2 생존
                                            survivor_count++;
                                            survivor_member_position=2;
                                        }
                                        if(!game_state_hm.get("3")[2].equals("0")){ // user3 생존
                                            survivor_count++;
                                            survivor_member_position=3;
                                        }

                                        if(survivor_count==1){ // 게임 종료
                                            for(int i=0;i<game_join_user_arr.length;i++){
                                                if(survivor_member_position==i){
                                                    if(login_member.member_id.equals(game_join_user_arr[i])){
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(PlayActivity.this);
                                                        builder.setTitle("런닝맨");
                                                        builder.setMessage("축하합니다. 승리하였습니다!!");
                                                        builder.setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                // 소켓 해제
                                                                new Thread(){
                                                                    @Override
                                                                    public void run(){
                                                                        try{
                                                                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                                                            pw.println("event3:bye\r\n"); // 준비상태 소켓에 보내기
                                                                            pw.flush();




                                                                        }catch (IOException e){
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                }.start();
                                                                if(receiveThread!=null){
                                                                    receiveThread.setIs_stop();
                                                                    receiveThread=null;
                                                                }
                                                                finish();
                                                            }
                                                        });
                                                        builder.show(); // 다이얼로그 보이게 하기
                                                        fire_btn.setVisibility(GONE);
                                                    }
                                                }
                                            }
                                        }

                                        /*
                                            죽은 멤버 점령지 초기화
                                         */
                                        for(int i=0;i<colony_arr.length;i++){
                                            if(attacked_member_id.equals(colony_arr[i])){
                                                if(i==0){
                                                    colony1_iv.setImageResource(R.drawable.colony1);
                                                    colony_arr[0]="null";
                                                }else if(i==1){
                                                    colony2_iv.setImageResource(R.drawable.colony2);
                                                    colony_arr[1]="null";
                                                }else if(i==2){
                                                    colony3_iv.setImageResource(R.drawable.colony3);
                                                    colony_arr[2]="null";
                                                }else if(i==3){
                                                    colony4_iv.setImageResource(R.drawable.colony4);
                                                    colony_arr[3]="null";
                                                }
                                            }
                                        }

                                   }else if(temp_life==1){
                                        if(Integer.parseInt(attacked_member_position)==0){
                                            user0_life_tv.setText("★");
                                        }else if(Integer.parseInt(attacked_member_position)==1){
                                            user1_life_tv.setText("★");
                                        }else if(Integer.parseInt(attacked_member_position)==2){
                                            user2_life_tv.setText("★");
                                        }else if(Integer.parseInt(attacked_member_position)==3){
                                            user3_life_tv.setText("★");
                                        }
                                    }else if(temp_life==2){
                                        if(Integer.parseInt(attacked_member_position)==0){
                                            user0_life_tv.setText("★ ★");
                                        }else if(Integer.parseInt(attacked_member_position)==1){
                                            user1_life_tv.setText("★ ★");
                                        }else if(Integer.parseInt(attacked_member_position)==2){
                                            user2_life_tv.setText("★ ★");
                                        }else if(Integer.parseInt(attacked_member_position)==3){
                                            user3_life_tv.setText("★ ★");
                                        }
                                    }


                                }
                            });
                       }else if("event2".equals(response[0])) { // 점령지 점령
                            final String member_id = response[1]; // 점령한 멤버 아이디
                            final String colony = response[2];
                            final String member_position = response[3];
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
//                                    Toast.makeText(getApplicationContext(),member_id+"님이 "+colony+"를 점령하였습니다!",Toast.LENGTH_SHORT).show();
                                    if("colony1".equals(colony)){
                                        colony_arr[0] = member_id;

                                        if(Integer.parseInt(member_position)==0){
                                            colony1_iv.setImageResource(R.drawable.cat);
                                        }else if(Integer.parseInt(member_position)==1){
                                            colony1_iv.setImageResource(R.drawable.heart);
                                        }else if(Integer.parseInt(member_position)==2){
                                            colony1_iv.setImageResource(R.drawable.owl);
                                        }else if(Integer.parseInt(member_position)==3){
                                            colony1_iv.setImageResource(R.drawable.koo);
                                        }
                                    }else if("colony2".equals(colony)){
                                        colony_arr[1] = member_id;

                                        if(Integer.parseInt(member_position)==0){
                                            colony2_iv.setImageResource(R.drawable.cat);
                                        }else if(Integer.parseInt(member_position)==1){
                                            colony2_iv.setImageResource(R.drawable.heart);
                                        }else if(Integer.parseInt(member_position)==2){
                                            colony2_iv.setImageResource(R.drawable.owl);
                                        }else if(Integer.parseInt(member_position)==3){
                                            colony2_iv.setImageResource(R.drawable.koo);
                                        }
                                    }else if("colony3".equals(colony)){
                                        colony_arr[2] = member_id;

                                        if(Integer.parseInt(member_position)==0){
                                            colony3_iv.setImageResource(R.drawable.cat);
                                        }else if(Integer.parseInt(member_position)==1){
                                            colony3_iv.setImageResource(R.drawable.heart);
                                        }else if(Integer.parseInt(member_position)==2){
                                            colony3_iv.setImageResource(R.drawable.owl);
                                        }else if(Integer.parseInt(member_position)==3){
                                            colony3_iv.setImageResource(R.drawable.koo);
                                        }
                                    }else if("colony4".equals(colony)){
                                        colony_arr[3] = member_id;

                                        if(Integer.parseInt(member_position)==0){
                                            colony4_iv.setImageResource(R.drawable.cat);
                                        }else if(Integer.parseInt(member_position)==1){
                                            colony4_iv.setImageResource(R.drawable.heart);
                                        }else if(Integer.parseInt(member_position)==2){
                                            colony4_iv.setImageResource(R.drawable.owl);
                                        }else if(Integer.parseInt(member_position)==3){
                                            colony4_iv.setImageResource(R.drawable.koo);
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
     * 타이머 클래스
     */
    class MyTimer extends CountDownTimer {
        public MyTimer(long millisInFuture, long countDownInterval){
            super(millisInFuture,countDownInterval);
        } // constructor

        @Override
        public void onTick(long l) {
            if(l>240000){ // 4분
                long temp_l = l-240000;
                if(temp_l<10000){
                    timer_tv.setText("4:0"+temp_l/1000);
                }else{
                    timer_tv.setText("4:"+temp_l/1000);
                }
            }else if(180000<l && l<=240000){ // 3분
                long temp_l = l-180000;
                if(temp_l<10000){
                    timer_tv.setText("3:0"+temp_l/1000);
                }else{
                    timer_tv.setText("3:"+temp_l/1000);
                }
            }else if(120000<l && l<=180000){ // 2분
                long temp_l = l-120000;
                if(temp_l<10000){
                    timer_tv.setText("2:0"+temp_l/1000);
                }else{
                    timer_tv.setText("2:"+temp_l/1000);
                }
            }else if(60000<l && l<=120000){ // 1분
                long temp_l = l-60000;
                if(temp_l<10000){
                    timer_tv.setText("1:0"+temp_l/1000);
                }else{
                    timer_tv.setText("1:"+temp_l/1000);
                }
            }else if(l<=60000){ // 0분
                long temp_l = l-0;
                if(temp_l<10000){
                    timer_tv.setText("0:0"+temp_l/1000);
                }else{
                    timer_tv.setText("0:"+temp_l/1000);
                }
            }
        }

        @Override
        public void onFinish() {
            timer_tv.setText("00:00");

            int count_0 = 0;
            int count_1 = 0;
            int count_2 = 0;
            int count_3 = 0;
            int count_null = 0;

            String id_0 = game_state_hm.get("0")[0];
            String id_1 = game_state_hm.get("1")[0];
            String id_2 = game_state_hm.get("2")[0];
            String id_3 = game_state_hm.get("3")[0];

            int life_0 = Integer.parseInt(game_state_hm.get("0")[2]);
            int life_1 = Integer.parseInt(game_state_hm.get("1")[2]);
            int life_2 = Integer.parseInt(game_state_hm.get("2")[2]);
            int life_3 = Integer.parseInt(game_state_hm.get("3")[2]);

            for(int i=0;i<colony_arr.length;i++){
                    if(id_0.equals(colony_arr[i])){
                        count_0++;
                    }else if(id_1.equals(colony_arr[i])){
                        count_1++;
                    }else if(id_2.equals(colony_arr[i])){
                        count_2++;
                    }else if(id_3.equals(colony_arr[i])){
                        count_3++;
                    }else if("null".equals(colony_arr[i])){
                        count_null++;
                    }
            }

            /*
                점령지 동률이 없는 경우
             */
            if(count_0>count_1 && count_0>count_2 && count_0>count_3){ // 0 user win
                if(login_member_position==0){ // 내가 우승
                    winDialog();
                }else{ // 나는 짐
                    loseDialog();
                }
            }else if(count_1>count_0 && count_1>count_2 && count_1>count_3){ // 1 user win
                if(login_member_position==1){ // 내가 우승
                    winDialog();
                }else{ // 나는 짐
                    loseDialog();
                }
            }else if(count_2>count_0 && count_2>count_1 && count_2>count_3){ // 2 user win
                if(login_member_position==2){ // 내가 우승
                    winDialog();
                }else{ // 나는 짐
                    loseDialog();
                }
            }else if(count_3>count_0 && count_3>count_1 && count_3>count_2){ // 2 user win
                if(login_member_position==3){ // 내가 우승
                    winDialog();
                }else{ // 나는 짐
                    loseDialog();
                }
            }

            /*
                점령지 동률이 있는 경우
             */
            if(count_null==0){ // 모든 점령지가 점령된 경우
                // 2,2
                if(count_0==2 && count_1==2){
                    // 각 멤버의 체력 체크
                    if(life_0>life_1){ // user0 우승
                        if(login_member_position==0){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0<life_1){ // user1 우승
                        if(login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0==life_1){ // user0, user1 공동우승
                        if(login_member_position==0 || login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }else if(count_0==2 && count_2==2){
                    // 각 멤버의 체력 체크
                    if(life_0>life_2){ // user0 우승
                        if(login_member_position==0){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0<life_2){ // user2 우승
                        if(login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0==life_2){ // user0, user2 공동우승
                        if(login_member_position==0 || login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }else if(count_0==2 && count_3==2){
                    // 각 멤버의 체력 체크
                    if(life_0>life_3){ // user0 우승
                        if(login_member_position==0){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0<life_3){ // user3 우승
                        if(login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0==life_3){ // user0, user3 공동우승
                        if(login_member_position==0 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }else if(count_1==2 && count_2==2){
                    // 각 멤버의 체력 체크
                    if(life_1>life_2){ // user1 우승
                        if(login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1<life_2){ // user2 우승
                        if(login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1==life_2){ // user1, user2 공동우승
                        if(login_member_position==1 || login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }else if(count_1==2 && count_3==2){
                    // 각 멤버의 체력 체크
                    if(life_1>life_3){ // user1 우승
                        if(login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1<life_3){ // user3 우승
                        if(login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1==life_3){ // user1, user3 공동우승
                        if(login_member_position==1 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }else if(count_2==2 && count_3==2){
                    // 각 멤버의 체력 체크
                    if(life_2>life_3){ // user2 우승
                        if(login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_2<life_3){ // user3 우승
                        if(login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_2==life_3){ // user2, user3 공동우승
                        if(login_member_position==2 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }
                // 1,1,1,1
                if(count_0==1 && count_1==1 && count_2==1 && count_3==1){ // 각 멤버의 체력 체크
                    // 1명 우승
                    if(life_0>life_1 && life_0>life_2 && life_0>life_3){ // user0 우승
                        if(login_member_position==0){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1>life_0 && life_1>life_2 && life_1>life_3){ // user1 우승
                        if(login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_2>life_0 && life_2>life_1 && life_2>life_3){ // user2 우승
                        if(login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_3>life_0 && life_3>life_1 && life_3>life_2){ // user3 우승
                        if(login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                    // 2명 우승
                    if(life_0==life_1 && life_0>life_2 && life_0>life_3){ // user0, user1 공동 우승
                        if(login_member_position==0 || login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0==life_2 && life_0>life_1 && life_0>life_3){ // user0, user2 공동 우승
                        if(login_member_position==0 || login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0==life_3 && life_0>life_1 && life_0>life_2){ // user0, user3 공동 우승
                        if(login_member_position==0 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1==life_2 && life_1>life_0 && life_1>life_3){ // user1, user2 공동 우승
                        if(login_member_position==1 || login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1==life_3 && life_1>life_0 && life_1>life_2){ // user1, user3 공동 우승
                        if(login_member_position==1 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_2==life_3 && life_2>life_0 && life_2>life_1){ // user2, user3 공동 우승
                        if(login_member_position==2 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }

                    // 3명 우승
                    if(life_0==life_1 && life_0==life_2 && life_0>life_3){ // user0, user1, user2 우승
                        if(login_member_position==0 || login_member_position==1 || login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0==life_1 && life_0==life_3 && life_0>life_2){ // user0, user1, user3 우승
                        if(login_member_position==0 || login_member_position==1 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1==life_2 && life_1==life_3 && life_1>life_0){ // user1, user2, user3 우승
                        if(login_member_position==1 || login_member_position==2 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }

                    // 4명 우승
                    if(life_0==life_1 && life_0==life_2 && life_0==life_3){ // user0, user1, user2, user3 공동 우승
                        if(login_member_position==0 || login_member_position==1 || login_member_position==2 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }
            }else if(count_null==1){ // 3개의 점령지가 점령된 경우a
                // 1,1,1
                if(count_0==1 && count_1==1 && count_2==1){
                    // 1명 우승
                    if(life_0>life_1 && life_0>life_2){ // user0 우승
                        if(login_member_position==0){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1>life_0 && life_1>life_2){ // user1 우승
                        if(login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_2>life_0 && life_2>life_1){ // user2 우승
                        if(login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }

                    // 2명 공동 우승
                    if(life_0==life_1 && life_0>life_2){ // user0, user1 우승
                        if(login_member_position==0 || login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0==life_2 && life_0>life_1){ // user0, user2 우승
                        if(login_member_position==0 || login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1==life_2 && life_1>life_0){ // user1, user2 우승
                        if(login_member_position==1 || login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }

                    // 3명 공동 우승
                    if(life_0==life_1 && life_0==life_2 && life_1==life_2){ // user0, user1, user2 공동우승
                        if(login_member_position==0 || login_member_position==1 || login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }else if(count_0==1 && count_1==1 && count_3==1){
                    // 1명 우승
                    if(life_0>life_1 && life_0>life_3){ // user0 우승
                        if(login_member_position==0){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1>life_0 && life_1>life_3){ // user1 우승
                        if(login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_3>life_0 && life_3>life_1){ // user3 우승
                        if(login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }

                    // 2명 공동 우승
                    if(life_0==life_1 && life_0>life_3){ // user0, user1 우승
                        if(login_member_position==0 || login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0==life_3 && life_0>life_1){ // user0, user3 우승
                        if(login_member_position==0 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1==life_3 && life_1>life_0){ // user1, user3 우승
                        if(login_member_position==1 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }

                    // 3명 공동 우승
                    if(life_0==life_1 && life_0==life_3 && life_1==life_3){ // user0, user1, user3 공동우승
                        if(login_member_position==0 || login_member_position==1 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }else if(count_1==1 && count_2==1 && count_3==1){
                    // 1명 우승
                    if(life_2>life_1 && life_2>life_3){ // user2 우승
                        if(login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1>life_2 && life_1>life_3){ // user1 우승
                        if(login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_3>life_2 && life_3>life_1){ // user3 우승
                        if(login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }

                    // 2명 공동 우승
                    if(life_2==life_1 && life_2>life_3){ // user2, user1 우승
                        if(login_member_position==2 || login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_2==life_3 && life_2>life_1){ // user2, user3 우승
                        if(login_member_position==2 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1==life_3 && life_1>life_2){ // user1, user3 우승
                        if(login_member_position==1 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }

                    // 3명 공동 우승
                    if(life_2==life_1 && life_2==life_3 && life_1==life_3){ // user2, user1, user3 공동우승
                        if(login_member_position==2 || login_member_position==1 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }
            }else if(count_null==2){ // 2개의 점령지가 점령된 경우
                // 1,1
                if(count_0==1 && count_1==1){
                    // 각 멤버의 체력 체크
                    if(life_0>life_1){ // user0 우승
                        if(login_member_position==0){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0<life_1){ // user1 우승
                        if(login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0==life_1){ // user0, user1 공동우승
                        if(login_member_position==0 || login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }else if(count_0==1 && count_2==1){
                    // 각 멤버의 체력 체크
                    if(life_0>life_2){ // user0 우승
                        if(login_member_position==0){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0<life_2){ // user2 우승
                        if(login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0==life_2){ // user0, user2 공동우승
                        if(login_member_position==0 || login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }else if(count_0==1 && count_3==1){
                    // 각 멤버의 체력 체크
                    if(life_0>life_3){ // user0 우승
                        if(login_member_position==0){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0<life_3){ // user3 우승
                        if(login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_0==life_3){ // user0, user3 공동우승
                        if(login_member_position==0 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }else if(count_1==1 && count_2==1){
                    // 각 멤버의 체력 체크
                    if(life_1>life_2){ // user1 우승
                        if(login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1<life_2){ // user2 우승
                        if(login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1==life_2){ // user1, user2 공동우승
                        if(login_member_position==1 || login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }else if(count_1==1 && count_3==1){
                    // 각 멤버의 체력 체크
                    if(life_1>life_3){ // user1 우승
                        if(login_member_position==1){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1<life_3){ // user3 우승
                        if(login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_1==life_3){ // user1, user3 공동우승
                        if(login_member_position==1 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }else if(count_2==1 && count_3==1){
                    // 각 멤버의 체력 체크
                    if(life_2>life_3){ // user2 우승
                        if(login_member_position==2){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_2<life_3){ // user3 우승
                        if(login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }else if(life_2==life_3){ // user2, user3 공동우승
                        if(login_member_position==2 || login_member_position==3){ // 내가 우승
                            winDialog();
                        }else{ // 나는 짐
                            loseDialog();
                        }
                    }
                }
            }else if(count_null==3){ // 1개의 점령지가 점령된 경우
                // 아무것도 안해도 됨
            }else if(count_null==4){ // 점령지가 하나도 점령되지 않은 경우
                // 1명 우승
                if(life_0>life_1 && life_0>life_2 && life_0>life_3){ // user0 우승
                    if(login_member_position==0){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }else if(life_1>life_0 && life_1>life_2 && life_1>life_3){ // user1 우승
                    if(login_member_position==1){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }else if(life_2>life_0 && life_2>life_1 && life_2>life_3){ // user2 우승
                    if(login_member_position==2){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }else if(life_3>life_0 && life_3>life_1 && life_3>life_2){ // user3 우승
                    if(login_member_position==3){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }
                // 2명 우승
                if(life_0==life_1 && life_0>life_2 && life_0>life_3){ // user0, user1 공동 우승
                    if(login_member_position==0 || login_member_position==1){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }else if(life_0==life_2 && life_0>life_1 && life_0>life_3){ // user0, user2 공동 우승
                    if(login_member_position==0 || login_member_position==2){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }else if(life_0==life_3 && life_0>life_1 && life_0>life_2){ // user0, user3 공동 우승
                    if(login_member_position==0 || login_member_position==3){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }else if(life_1==life_2 && life_1>life_0 && life_1>life_3){ // user1, user2 공동 우승
                    if(login_member_position==1 || login_member_position==2){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }else if(life_1==life_3 && life_1>life_0 && life_1>life_2){ // user1, user3 공동 우승
                    if(login_member_position==1 || login_member_position==3){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }else if(life_2==life_3 && life_2>life_0 && life_2>life_1){ // user2, user3 공동 우승
                    if(login_member_position==2 || login_member_position==3){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }

                // 3명 우승
                if(life_0==life_1 && life_0==life_2 && life_0>life_3){ // user0, user1, user2 우승
                    if(login_member_position==0 || login_member_position==1 || login_member_position==2){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }else if(life_0==life_1 && life_0==life_3 && life_0>life_2){ // user0, user1, user3 우승
                    if(login_member_position==0 || login_member_position==1 || login_member_position==3){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }else if(life_1==life_2 && life_1==life_3 && life_1>life_0){ // user1, user2, user3 우승
                    if(login_member_position==1 || login_member_position==2 || login_member_position==3){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }

                // 4명 우승
                if(life_0==life_1 && life_0==life_2 && life_0==life_3){ // user0, user1, user2, user3 공동 우승
                    if(login_member_position==0 || login_member_position==1 || login_member_position==2 || login_member_position==3){ // 내가 우승
                        winDialog();
                    }else{ // 나는 짐
                        loseDialog();
                    }
                }
            }

        } // onFinish()

        private void winDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(PlayActivity.this);
            builder.setTitle("런닝맨");
            builder.setMessage("축하합니다. 우승하였습니다!!");
            builder.setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 소켓 해제
                    new Thread(){
                        @Override
                        public void run(){
                            try{
                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                pw.println("event3:bye\r\n"); // 준비상태 소켓에 보내기
                                pw.flush();
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    if(receiveThread!=null){
                        receiveThread.setIs_stop();
                        receiveThread=null;
                    }
                    finish();
                }
            });
            builder.show(); // 다이얼로그 보이게 하기
        } // winDialog()

        private void loseDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(PlayActivity.this);
            builder.setTitle("런닝맨");
            builder.setMessage("안타깝네요 ㅠㅠ 게임에서 패배하였습니다.");
            builder.setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 소켓 해제
                    new Thread(){
                        @Override
                        public void run(){
                            try{
                                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                                pw.println("event3:bye\r\n"); // 준비상태 소켓에 보내기
                                pw.flush();
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    if(receiveThread!=null){
                        receiveThread.setIs_stop();
                        receiveThread=null;
                    }
                    finish();
                }
            });
            builder.show(); // 다이얼로그 보이게 하기
        } // loseDialog()

    } // MyTimer class

} // PlayActivity class
