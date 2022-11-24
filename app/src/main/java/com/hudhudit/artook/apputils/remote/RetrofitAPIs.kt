package com.hudhudit.artook.apputils.remote

import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.competition.*
import com.hudhudit.artook.apputils.modules.notification.NotificationsResult
import com.hudhudit.artook.apputils.modules.post.CommentsResult
import com.hudhudit.artook.apputils.modules.post.CountsResult
import com.hudhudit.artook.apputils.modules.post.PostResult
import com.hudhudit.artook.apputils.modules.post.PostsResult
import com.hudhudit.artook.apputils.modules.profile.FollowersResult
import com.hudhudit.artook.apputils.modules.profile.FollowingsResult
import com.hudhudit.artook.apputils.modules.profile.ProfileCounts
import com.hudhudit.artook.apputils.modules.profile.UserProfileData
import com.hudhudit.artook.apputils.modules.search.Categories
import com.hudhudit.artook.apputils.modules.user.SearchUsersResult
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.modules.videosarticles.ArticlesResults
import com.hudhudit.artook.apputils.modules.videosarticles.VideosArticlesCountResult
import com.hudhudit.artook.apputils.modules.videosarticles.VideosResults
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitAPIs {

    //Registration

    @FormUrlEncoded
    @POST("user_login")
    fun logIn( @Field("email") email:String,
               @Field("password") password:String,
               @Field("fcm_token") fcm_token:String,
               @Field("type_token") type_token:String): Call<UserData>

    @FormUrlEncoded
    @POST("create_account")
    fun register( @Field("name") first_name:String,
                  @Field("email") email:String,
                  @Field("phone") phone:String,
                  @Field("password") password:String,
                  @Field("fcm_token") fcm_token:String,
                  @Field("type_token") type_token:String,
                  @Field("fb_id") fb_id:String,
                  @Field("google_id") google_id:String,
                  @Field("user_name") user_name:String): Call<UserData>

    @FormUrlEncoded
    @POST("forgot_password")
    fun resetPassword( @Field("phone") phone:String,
                       @Field("password") password:String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("update_token")
    fun updateToken( @Field("fcm_token") fcm_token:String,
                       @Field("type_token") type_token:String): Call<BooleanResponse>

    @Multipart
    @POST("update_account")
    fun updateProfile(@Part("name") name: RequestBody,
                      @Part("email") email:RequestBody,
                      @Part("phone") phone:RequestBody,
                      @Part image: MultipartBody.Part?,
                      @Part("bio") bio:RequestBody): Call<UserData>

    @FormUrlEncoded
    @POST("checkUserNameUpdateProfile")
    fun checkUserNameUpdate( @Field("user_name") user_name:String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("chang_password")
    fun changePassword( @Field("password") password:String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("checkUserName")
    fun checkUserName( @Field("user_name") user_name:String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("check_email")
    fun checkEmail( @Field("email") email:String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("check_phone")
    fun checkPhone( @Field("phone") phone:String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("get_client_profile")
    fun getProfileCounts( @Field("client_id") client_id:String): Call<ProfileCounts>

    @FormUrlEncoded
    @POST("get_client_followers")
    fun getFollowers(@Field("client_id") client_id:String,
                     @Field("page") page: String): Call<FollowersResult>

    @FormUrlEncoded
    @POST("get_client_following")
    fun getFollowings(@Field("client_id") client_id:String,
                     @Field("page") page: String): Call<FollowingsResult>

    @GET("get_my_posts")
    fun getMyPosts(@Query("page") page: String): Call<PostsResult>

    @GET("get_client_save_posts")
    fun getSavedPosts(@Query("page") page: String): Call<PostsResult>

    @GET("get_posts_home")
    fun getHomePosts(@Query("page") page: String): Call<PostsResult>

    @FormUrlEncoded
    @POST("add_likes")
    fun likePost(@Field("posts_id") posts_id:String): Call<CountsResult>

    @FormUrlEncoded
    @POST("remove_likes")
    fun unlikePost(@Field("posts_id") posts_id:String): Call<CountsResult>

    @FormUrlEncoded
    @POST("save_client_save_posts")
    fun savePost(@Field("posts_id") posts_id:String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("remove_client_save_posts")
    fun unSavePost(@Field("posts_id") posts_id:String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("add_report")
    fun reportPost(@Field("posts_id") posts_id:String,
                   @Field("note") note:String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("create_posts")
    fun createPost(@Field("category_id") category_id: String,
                   @Field("description") description: String,
                   @Field("files") image: String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("remove_posts")
    fun deletePost(@Field("posts_id") posts_id:String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("update_posts")
    fun updatePost(@Field("description") description:String,
                   @Field("posts_id") posts_id:String): Call<BooleanResponse>

    @GET("get_my_posts_by_id")
    fun getPostById(@Query("id") id: String): Call<PostResult>

    @GET("get_posts_comments")
    fun getPostComments(@Query("page") page: String,
                        @Query("posts_id") posts_id: String): Call<CommentsResult>

    @FormUrlEncoded
    @POST("add_comments")
    fun addComment(@Field("posts_id") posts_id:String,
                   @Field("title") title:String): Call<CountsResult>

    @FormUrlEncoded
    @POST("remove_comments")
    fun deleteComment(@Field("posts_id") posts_id:String,
                      @Field("id") id:String): Call<CountsResult>

    @GET("get_notifications")
    fun getNotifications(@Query("page") page: String): Call<NotificationsResult>

    @FormUrlEncoded
    @POST("delete_notifications")
    fun deleteNotification(@Field("id") id:String): Call<BooleanResponse>

    @GET("check_red_point")
    fun getOpenedNotifications(): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("update_read_notifications")
    fun readNotification(@Field("id") id:String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("get_user_profile")
    fun getUserProfile(@Field("client_id") client_id:String): Call<UserProfileData>

    @FormUrlEncoded
    @POST("get_user_profile_posts")
    fun getUserPosts(@Field("client_id") client_id:String,
                     @Field("page") page: String): Call<PostsResult>

    @FormUrlEncoded
    @POST("add_follow")
    fun followUser(@Field("client_id") client_id:String): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("remove_follow")
    fun unFollowUser(@Field("client_id") client_id:String): Call<BooleanResponse>

    @GET("get_videos_articles_number")
    fun getVideosArticlesCount(): Call<VideosArticlesCountResult>

    @GET("get_articles")
    fun getArticles(@Query("page") page: String): Call<ArticlesResults>

    @GET("get_videos")
    fun getVideos(@Query("page") page: String): Call<VideosResults>

    @FormUrlEncoded
    @POST("search_articles")
    fun searchArticles(@Field("page") page:String,
                       @Field("search") search:String): Call<ArticlesResults>

    @FormUrlEncoded
    @POST("search_videos")
    fun searchVideos(@Field("page") page:String,
                     @Field("search") search:String): Call<VideosResults>

    @GET("get_category")
    fun getCategories(): Call<Categories>

    @FormUrlEncoded
    @POST("search_name")
    fun search(@Field("page") page: String,
               @Field("category_id") category_id:String,
               @Field("search") search: String): Call<SearchUsersResult>

    @GET("get_contests")
    fun getContest(): Call<ContestResult>

    @GET("get_all_contests")
    fun getPreviousContests(@Query("page") page: String): Call<PreviousContestsResult>

    @FormUrlEncoded
    @POST("get_contests_winners")
    fun getWinners(@Field("contests_id") contests_id: String,
                   @Field("page") page:String,
                   @Field("last_contests") last_contests: String): Call<WinnersResult>

    @FormUrlEncoded
    @POST("check_participat_client")
    fun checkUserParticipation(@Field("contests_id") contests_id: String): Call<BooleanResponse>

    @Multipart
    @POST("contests_participat")
    fun participate(@Part("contests_id") contests_id: RequestBody,
                    @Part("description") description: RequestBody,
                    @Part image: MultipartBody.Part?): Call<BooleanResponse>

    @FormUrlEncoded
    @POST("get_contests_participat")
    fun getContestParticipants(@Field("contests_id") contests_id: String,
                               @Field("page") page:String): Call<ParticipantsResult>

    @FormUrlEncoded
    @POST("add_contests_participat_vote")
    fun vote(@Field("contests_participat_id") contests_participat_id: String): Call<VoteResult>



}