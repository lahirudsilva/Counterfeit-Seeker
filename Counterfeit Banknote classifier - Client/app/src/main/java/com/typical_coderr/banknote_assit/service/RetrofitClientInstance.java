package com.typical_coderr.banknote_assit.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Android Studio.
 * User: Lahiru
 * Date: Tue
 * Time: 10:57 PM
 */
public class RetrofitClientInstance {
    private static Retrofit retrofit;
    private static final String BASE_URL = "http://200a-112-134-146-107.ngrok.io/";

    public static Retrofit getRetrofitInstance(){
        if(retrofit == null){
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
