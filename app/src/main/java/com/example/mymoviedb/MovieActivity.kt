package com.example.mymoviedb

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymoviedb.databinding.ActivityMovieBinding
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MovieActivity : AppCompatActivity() {

    var page = 1
    var totalPage = 0

    val reviewList = ArrayList<ReviewsItem>()
    var adapterReviewList: AdapterReviewList? = null
    private lateinit var binding: ActivityMovieBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val movieId = intent.getIntExtra("movieId", 0)
        adapterReviewList = AdapterReviewList(this@MovieActivity, reviewList)
        binding.apply {
            listReview.adapter = adapterReviewList
            listReview.layoutManager = LinearLayoutManager(this@MovieActivity)
            listReview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int
                ) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!listReview.canScrollVertically(1)) {
                        if (page<totalPage) {
                            page += 1
                            getReview(movieId)
                        }
                    }
                }
            })
        }

        showProgress()

        MyApiClient().getClient().getMovie(movieId, MyApiClient().API_KEY)
            .enqueue(object : Callback<MovieDetail> {
                override fun onResponse(call: Call<MovieDetail>, response: Response<MovieDetail>) {
                    val movieDetail = response.body()
                    if (movieDetail != null) {
                        binding.apply {
                            movieTitle.text = movieDetail.title
                            if (movieDetail.adult!!)
                                warningAdult.visibility = View.VISIBLE
                            else
                                warningAdult.visibility = View.GONE
                            Picasso.get()
                                .load("https://image.tmdb.org/t/p/original" + movieDetail.backdropPath)
                                .placeholder(R.drawable.progress).fit()
                                .into(movieBackdrop)
                            val genres = StringBuilder()
                            for (i in movieDetail.genres!!.indices) {
                                genres.append(movieDetail.genres[i]!!.name)
                                if (i < movieDetail.genres.size - 1)
                                    genres.append(", ")
                            }
                            movieGenres.text = genres
                            movieOverview.text = movieDetail.overview
                            rating.text = String.format("%.1f", movieDetail.voteAverage)
                            getVideo(movieId)
                        }
                    }

                }

                override fun onFailure(call: Call<MovieDetail>, t: Throwable) {
                    Log.e("onFailure", t.localizedMessage!!)
                }

            })
    }

    private fun getVideo(movieId: Int) {
        MyApiClient().getClient().getVideo(movieId, MyApiClient().API_KEY)
            .enqueue(object : Callback<Video> {
                override fun onResponse(call: Call<Video>, response: Response<Video>) {
                    var videoKey = ""
                    for (video in response.body()!!.results) {
                        if (video.name == "Official Trailer")
                            videoKey = video.key
                    }
                    binding.apply {
                        buttonYoutube.setOnClickListener {
                            val browserIntent =
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/$videoKey"))
                            startActivity(browserIntent)
                        }
                        getReview(movieId)
                    }
                }

                override fun onFailure(call: Call<Video>, t: Throwable) {
                    Log.e("onFailure", t.localizedMessage!!)
                }

            })

    }

    private fun getReview(movieId: Int) {
        MyApiClient().getClient().getReviews(movieId, MyApiClient().API_KEY, page)
            .enqueue(object : Callback<Review> {
                override fun onResponse(call: Call<Review>, response: Response<Review>) {
                    binding.apply {
                        if (response.body()!!.results.isNotEmpty()) {
                            emptyReview.visibility = View.GONE
                            reviewList.addAll(response.body()!!.results)
                            adapterReviewList!!.notifyDataSetChanged()
                            totalPage = response.body()!!.totalPages
                        }

                        hideProgress()
                    }
                }

                override fun onFailure(call: Call<Review>, t: Throwable) {
                    Log.e("onFailure", t.localizedMessage!!)
                }

            })
    }

    private fun showProgress() {
        binding.apply {
            progressCircular.visibility = View.VISIBLE
            movieDetailLayout.visibility = View.INVISIBLE
        }
    }

    private fun hideProgress() {
        binding.apply {
            progressCircular.visibility = View.GONE
            movieDetailLayout.visibility = View.VISIBLE
        }
    }

    class AdapterReviewList(
        var context: Context,
        var itemList: List<ReviewsItem>,
    ) : RecyclerView.Adapter<AdapterReviewList.Holder>() {
        class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var author: TextView = itemView.findViewById(R.id.review_author)
            var content: TextView = itemView.findViewById(R.id.review_content)
            var dateUpdate: TextView = itemView.findViewById(R.id.review_updated)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(
                LayoutInflater.from(context).inflate(R.layout.item_review, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = itemList[position]
            holder.dateUpdate.text = item.updatedAt
            holder.author.text = item.author
            holder.content.text = item.content
        }

        override fun getItemCount(): Int {
            return itemList.size
        }
    }
}