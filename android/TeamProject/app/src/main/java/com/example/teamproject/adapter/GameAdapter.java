package com.example.teamproject.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.teamproject.R;
import com.example.teamproject.activity.StandByActivity;
import com.example.teamproject.etc.AppHelper;
import com.example.teamproject.etc.MYURL;
import com.example.teamproject.object.Game;
import com.example.teamproject.object.Member;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * GameAdapter 클래스
 * - 게시글 리싸이클러뷰를 위한 어댑터
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<Game> games; // 게시글 리스트
    Activity activity;
    Context context;
    Member login_member;

    public GameAdapter(ArrayList<Game> games, Activity activity){
        this.games = games;
        this.activity = activity;
        this.context = activity.getApplicationContext();
        /*
         * 쉐어드에 저장된 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = context.getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);
    } // GameAdapter 생성자

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // onCreateViewHolder() 메소드
        LayoutInflater inflater  = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_game_item,parent,false); // 뷰 객체 생성
        GameAdapter.ViewHolder viewHolder = new GameAdapter.ViewHolder(view);
        return viewHolder;
    } // onCreateViewHolder() 메소드

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { // onBindViewHolder() 메소드
        if(holder.getAdapterPosition()!= RecyclerView.NO_POSITION){ // 포지션 에러를 최소화 하기 위한 조건
            Game game = games.get(holder.getAdapterPosition());
            final int game_no = game.game_no;
            final String game_name = game.game_name;
            final String game_admin_id = game.game_admin_id;
            final String game_join_user = game.game_join_user;



            holder.game_admin_id_tv.setText("방번호 : "+game_no);
            holder.game_name_tv.setText(game_name);

            holder.item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getGameJoinUser(String.valueOf(game_no),game_name);
                }
            });

        }
    } // onBindViewHolder() 메소드

    @Override
    public int getItemCount() { // getItemCount() 메소드
        return games.size();
    } // getItemCount() 메소드

    /**
     * cat. 게임 참여자 얻어오기
     * 2. UI 동기화
     */
    private void getGameJoinUser(final String game_no, final String game_name){
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response);
                        String game_join_user = response;
                        String[] game_join_user_arr = game_join_user.split(","); // 문자열 배열화

                        /*
                            현재 게임 참여 인원이 4명이면 참여 불가 토스트 메시지 생성
                         */
                        int count=0;
                        for(int i=0;i<game_join_user_arr.length;i++){
                            if("null".equals(game_join_user_arr[i])){
                                count++;
                            }
                        }
                        if(count==0){
                            Toast.makeText(activity.getApplicationContext(),"해당 게임방에 이미 4명이 참여하였습니다. 다른 게임방에 참여해주세요.",Toast.LENGTH_SHORT).show();
                        }else{
                            Intent intent = new Intent(context, StandByActivity.class);
                            intent.putExtra("game_no",String.valueOf(game_no));
                            intent.putExtra("game_name",game_name);
                            intent.putExtra("game_join_user",game_join_user);
                            activity.startActivity(intent);
                        }


                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"응답 에러: "+error.toString());
                        Toast.makeText(activity.getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","get_game_join_user");
                params.put("game_no",game_no);
                params.put("member_id",login_member.member_id);
                return params;
            }
        };

        /*
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(context.getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음
    } // getGameJoinUser()

    public class ViewHolder extends RecyclerView.ViewHolder{

        View item_view;
        TextView game_admin_id_tv; // 게임 방장
        TextView game_name_tv; // 게임 이름

        /**
         * ViewHolder 생성자
         * @param item_view 아이템 뷰
         */
        ViewHolder(View item_view){
            super(item_view);
            this.item_view = item_view;
            this.game_admin_id_tv = item_view.findViewById(R.id.game_admin_id_tv);
            this.game_name_tv = item_view.findViewById(R.id.game_name_tv);

            item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // item 클릭시 게임방 참여

                }
            });
        } // ViewHolder 생성자

    } // ViewHolder 클래스



} // GameAdapter
