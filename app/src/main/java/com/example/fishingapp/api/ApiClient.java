// app/src/main/java/com/example/fishingapp/api/ApiClient.java
package com.example.fishingapp.api;

import com.example.fishingapp.utils.Config;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Config.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static FishingApi getApi() {
        return getClient().create(FishingApi.class);
    }
}