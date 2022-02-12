package com.typical_coderr.banknote_assit.service;

import com.typical_coderr.banknote_assit.model.Prediction;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Android Studio.
 * User: Lahiru
 * Date: Tue
 * Time: 11:02 PM
 */
public interface FlaskClient {

    @Multipart
    @POST("predict")
    Call <ResponseBody> uploadBanknote (@Part MultipartBody.Part part);




}
