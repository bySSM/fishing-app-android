package com.example.fishingapp.api;

import com.example.fishingapp.model.Catch;
import com.example.fishingapp.model.Comment;
import com.example.fishingapp.model.CommentsResponse;
import com.example.fishingapp.model.LikeResponse;
import com.example.fishingapp.model.LikeStatusResponse;
import com.example.fishingapp.model.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FishingApi {

    // ============================================================
    // Auth
    // ============================================================

    @POST("api/auth/login")
    Call<Map<String, Object>> login(
            @Body Map<String, String> credentials
    );

    @POST("api/auth/register")
    Call<Map<String, Object>> register(
            @Body Map<String, String> userData
    );


    // ============================================================
    // Catches
    // ============================================================

    @GET("api/catches/my")
    Call<List<Catch>> getMyCatches(
            @Header("Authorization") String token
    );

    @GET("api/catches/nearby")
    Call<List<Catch>> getNearbyCatches(
            @Header("Authorization") String token,
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("radiusKm") double radiusKm
    );

    @POST("api/catches")
    Call<Catch> createCatch(
            @Header("Authorization") String token,
            @Body Map<String, Object> catchData
    );

    @Multipart
    @POST("api/catches/with-photo")
    Call<Map<String, Object>> createCatchWithPhoto(
            @Header("Authorization") String token,
            @Part MultipartBody.Part photo,
            @Part("fishType") RequestBody fishType,
            @Part("weight") RequestBody weight,
            @Part("length") RequestBody length,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("bait") RequestBody bait,
            @Part("description") RequestBody description,
            @Part("isLocationHidden") RequestBody isLocationHidden
    );

    @PUT("api/catches/{id}")
    Call<Catch> updateCatch(
            @Header("Authorization") String token,
            @Path("id") Long id,
            @Body Map<String, Object> catchData
    );

    @DELETE("api/catches/{id}")
    Call<Void> deleteCatch(
            @Header("Authorization") String token,
            @Path("id") Long id
    );


    // ============================================================
    // Aquarium
    // ============================================================

    @GET("api/aquarium/my")
    Call<Map<String, Object>> getMyAquarium(
            @Header("Authorization") String token
    );

    @GET("api/aquarium/user/{userId}")
    Call<Map<String, Object>> getUserAquarium(
            @Header("Authorization") String token,
            @Path("userId") Long userId
    );


    // ============================================================
    // Rating
    // ============================================================

    @GET("api/rating/top100")
    Call<List<Map<String, Object>>> getTop100();


    // ============================================================
    // Search
    // ============================================================

    @GET("api/search/users")
    Call<List<Map<String, Object>>> searchUsers(
            @Header("Authorization") String token,
            @Query("query") String query
    );


    // ============================================================
    // Likes
    // ============================================================

    @POST("api/likes/catch/{catchId}/toggle")
    Call<LikeResponse> toggleLike(
            @Header("Authorization") String token,
            @Path("catchId") Long catchId
    );

    @GET("api/likes/catch/{catchId}/status")
    Call<LikeStatusResponse> getLikeStatus(
            @Header("Authorization") String token,
            @Path("catchId") Long catchId
    );


    // ============================================================
    // Comments
    // ============================================================

    @POST("api/comments/catch/{catchId}")
    Call<Comment> addComment(
            @Header("Authorization") String token,
            @Path("catchId") Long catchId,
            @Body Map<String, String> request
    );

    @GET("api/comments/catch/{catchId}")
    Call<CommentsResponse> getCatchComments(
            @Header("Authorization") String token,
            @Path("catchId") Long catchId
    );

    @DELETE("api/comments/{commentId}")
    Call<Map<String, String>> deleteComment(
            @Header("Authorization") String token,
            @Path("commentId") Long commentId
    );

    @GET("api/catches/{id}")
    Call<Catch> getCatchById(
            @Header("Authorization") String token,
            @Path("id") Long id
    );
}