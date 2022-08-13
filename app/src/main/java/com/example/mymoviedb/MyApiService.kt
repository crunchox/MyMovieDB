package com.example.mymoviedb

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MyApiService {

    @GET("genre/movie/list")
    fun getGenre(
        @Query("api_key") apiKey: String
    ): Call<Genre>

    @GET("discover/movie")
    fun getMovieByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int
    ): Call<Movie>

    @GET("movie/{movie_id}")
    fun getMovie(
        @Path(value = "movie_id", encoded = true) movieId: Int,
        @Query("api_key") apiKey: String
    ): Call<MovieDetail>

    @GET("movie/{movie_id}/videos")
    fun getVideo(
        @Path(value = "movie_id", encoded = true) movieId: Int,
        @Query("api_key") apiKey: String
    ): Call<Video>

    @GET("movie/{movie_id}/reviews")
    fun getReviews(
        @Path(value = "movie_id", encoded = true) movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): Call<Review>
}