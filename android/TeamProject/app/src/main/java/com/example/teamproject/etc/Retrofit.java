package com.example.teamproject.etc;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.converter.gson.GsonConverterFactory;

public class Retrofit {
    public static final String URL = "http://35.243.90.95:11000/";
    static retrofit2.Retrofit mRetrofit;

    public static retrofit2.Retrofit getRetrofit2(){

        if(mRetrofit == null){
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            httpClient.connectTimeout(5, TimeUnit.MINUTES)
                    .writeTimeout(5,TimeUnit.MINUTES)
                    .readTimeout(5,TimeUnit.MINUTES)
                    .addInterceptor(logging)
                    .build();

            mRetrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return mRetrofit;
    }
}
