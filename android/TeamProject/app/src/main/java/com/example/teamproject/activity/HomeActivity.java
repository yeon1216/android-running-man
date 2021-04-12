package com.example.teamproject.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.widget.PullRefreshLayout;
import com.example.teamproject.R;
import com.example.teamproject.adapter.GameAdapter;
import com.example.teamproject.etc.AppHelper;
import com.example.teamproject.etc.MYURL;
import com.example.teamproject.object.Game;
import com.example.teamproject.object.Member;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    Button create_game_btn; // 게임 생성 버튼
    Button logout_btn; // 로그아웃 버튼
    TextView login_id_tv; // 로그인 아이디 텍스트뷰

    PullRefreshLayout pullRefreshLayout; // 게임 리스트 동기화
    RecyclerView game_list_recyclerview; // 게임 리스트 리싸이클러뷰

    ArrayList<Game> games;

    Member login_member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG,"onCreate()");

        /*
         * 쉐어드에 저장된 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);

        login_id_tv = findViewById(R.id.login_id_tv);
        login_id_tv.setText(login_member.member_id+"님 반갑습니다");

        logout_btn = findViewById(R.id.logout_btn);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                 * 쉐어드에서 로그인멤버 제거
                 */
                SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                Member login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"), Member.class);
                editor.remove("login_member").commit();

                Toast.makeText(getApplicationContext(),"로그아웃 되었습니다",Toast.LENGTH_SHORT).show();

                /*
                 * 로그인 화면으로 이동
                 */
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finishAffinity(); // 모든 액티비티 클리어
            }
        });

        create_game_btn = findViewById(R.id.create_game_btn);
        create_game_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 게임 생성 버튼 클릭
                Log.d(TAG,"게임 생성 버튼 클릭");
                final EditText editText = new EditText(getApplicationContext());
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("게임 제목을 입력해주세요");
                builder.setMessage("");
                builder.setView(editText);

                builder.setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(editText.getText().toString().length()==0){
                                    Toast.makeText(getApplicationContext(),"게임 제목을 입력해주세요",Toast.LENGTH_SHORT).show();
                                }else{
                                    String game_name = editText.getText().toString();
                                    addGameRequest(game_name);
                                }
                            }
                        });

                builder.setNeutralButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                builder.show(); // 다이얼로그 보이게 하기
            }
        });




    } // onCreate()

    @Override
    protected void onResume() {
        super.onResume();
        getGameListRequest(); // 게임방 리스트 가져오기

        pullRefreshLayout = findViewById(R.id.pull_refresh_layout);
        pullRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_RING); // 프로그레스 써클
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { // 동기화 이벤트
                getGameListRequest(); // 게임방 리스트 가져오기
            }
        });
    }

    private void addGameRequest(final String game_name){
        Log.d(TAG,"addGameRequest() game_name : "+game_name);
        Log.d(TAG,"game_name : "+game_name+", login_member_no : "+login_member.member_no+", login_member_id : "+login_member.member_id);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response);
                        if("-cat".equals(response)){ // 게임 추가 실패
                            Toast.makeText(getApplicationContext(),"게임 추가 실패",Toast.LENGTH_LONG).show();
                        }else{ // 게임 추가 성공
                            String game_no = response;
                            Intent intent = new Intent(getApplicationContext(),StandByActivity.class);
                            intent.putExtra("create_game_room","create_game_room");
                            intent.putExtra("game_no",game_no);
                            intent.putExtra("game_name",game_name);
                            intent.putExtra("game_admin_id",login_member.member_id);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                        }
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
                params.put("mode","add_game");
                params.put("game_name",game_name);
                params.put("game_admin_id",login_member.member_id);
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
    } // addGameRequest()

    private void getGameListRequest(){
        Log.d(TAG,"getGameListRequest() 호출");

        games = new ArrayList<>();

        // 요청 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("mode","get_game_list");

        JSONObject jsonObject = new JSONObject(params); // Map을 json으로 만듬

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, MYURL.URL, jsonArray,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response_arr) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response_arr.toString());

                        try{
                            for(int i=0; i<response_arr.length();i++){
                                JSONObject response = response_arr.getJSONObject(i);
                                Gson gson = new Gson();
                                Game game = gson.fromJson(response.toString(), Game.class);
                                if(game.is_game_exit==0){
                                    games.add(game);
                                }
                            }
                        }catch (JSONException e){
                            Log.d(TAG,"JSONException: "+e.toString());
                        }

                        /*
                         * game recyclerview 관련 코드
                         */
                        game_list_recyclerview = findViewById(R.id.game_list_recyclerview);
                        game_list_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        GameAdapter gameAdapter = new GameAdapter(games,HomeActivity.this);
                        sort(games);
                        game_list_recyclerview.setAdapter(gameAdapter);

                        /*
                         * 새로고침 프로그래스 바 멈추기
                         */
                        pullRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d(TAG,"응답 실패: "+error.toString());
            }
        });

        /*
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(jsonArrayRequest); // 요청 큐에 위 요청 추가

    } // getBoardRequest() 메소드

    ArrayList<Game> sort(ArrayList<Game> games){
        for (int i = 0; i < games.size()-1; i++) {
            for (int j = 1; j < games.size(); j++) {
                if(games.get(j-1).game_no < games.get(j).game_no){
                    Game temp_review_item = games.get(j-1);
                    games.set(j-1,games.get(j));
                    games.set(j,temp_review_item);
                }
            }
        }
        return games;
    } // 정렬 메소드

} // HomeActivity class
