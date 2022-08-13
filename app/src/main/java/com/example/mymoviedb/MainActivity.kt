package com.example.mymoviedb

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymoviedb.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val genreList = ArrayList<GenresItem>()
    val movieList = ArrayList<ResultsItem>()
    val genreListName = ArrayList<String>()

    var adapterMovieList: AdapterMovieList? = null

    var genreId = 0
    var page = 1

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        adapterMovieList = AdapterMovieList(
            applicationContext,
            movieList,
            object : AdapterMovieList.OnClick {
                override fun onClick(pos: Int) {
                    val intent =
                        Intent(
                            this@MainActivity,
                            MovieActivity::class.java
                        ).apply { putExtra("movieId", movieList[pos].id) }
                    startActivity(intent)
                }

            })
        binding.apply {
            listMovie.adapter = adapterMovieList
            listMovie.layoutManager = LinearLayoutManager(this@MainActivity)
            listMovie.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int
                ) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!listMovie.canScrollVertically(1)) {
                        page += 1
                        getMovieGenre()
                    }
                }
            })
        }
        showProgress()
        MyApiClient().getClient().getGenre(MyApiClient().API_KEY).enqueue(object : Callback<Genre> {
            override fun onResponse(call: Call<Genre>, response: Response<Genre>) {
                genreList.addAll(response.body()!!.genres)
                for (item in genreList) {
                    genreListName.add(item.name)
                }
                val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item, genreListName
                )

                binding.apply {
                    spinnerGenre.adapter = adapter
                    spinnerGenre.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long
                            ) {
                                genreId = genreList[p2].id
                                movieList.clear()
                                page = 1
                                adapterMovieList!!.notifyDataSetChanged()
                                getMovieGenre()
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {

                            }

                        }
                }

                getMovieGenre()
            }

            override fun onFailure(call: Call<Genre>, t: Throwable) {
                Log.e("onFailure", t.localizedMessage!!)
            }

        })


    }

    private fun getMovieGenre() {
        showProgress()
        MyApiClient().getClient().getMovieByGenre(MyApiClient().API_KEY, genreId, page)
            .enqueue(object : Callback<Movie> {
                override fun onResponse(call: Call<Movie>, response: Response<Movie>) {
                    movieList.addAll(response.body()!!.results)
                    adapterMovieList!!.notifyDataSetChanged()
                    hideProgress()
                }

                override fun onFailure(call: Call<Movie>, t: Throwable) {
                    Log.e("onFailure", t.localizedMessage!!)
                }

            })
    }

    private fun showProgress() {
        binding.apply { progressCircular.visibility = View.VISIBLE }
    }

    private fun hideProgress() {
        binding.apply { progressCircular.visibility = View.GONE }
    }

    class AdapterMovieList(
        var context: Context,
        var itemList: List<ResultsItem>,
        var listener: OnClick
    ) : RecyclerView.Adapter<AdapterMovieList.Holder>() {

        interface OnClick {
            fun onClick(pos: Int)
        }

        class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var layout: CardView = itemView.findViewById(R.id.card_layout)
            var poster: ImageView = itemView.findViewById(R.id.movie_poster)
            var warningAdult: TextView = itemView.findViewById(R.id.warning_adult)
            var title: TextView = itemView.findViewById(R.id.movie_title)
            var releaseDate: TextView = itemView.findViewById(R.id.release_date)
            var rating: TextView = itemView.findViewById(R.id.rating)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(
                LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = itemList[position]
            holder.layout.setOnClickListener { listener.onClick(position) }
            Picasso.get().load("https://image.tmdb.org/t/p/w500" + item.posterPath)
                .placeholder(R.drawable.progress)
                .into(holder.poster)
            if (item.adult)
                holder.warningAdult.visibility = View.VISIBLE
            else
                holder.warningAdult.visibility = View.GONE
            holder.title.text = item.title
            holder.releaseDate.text = item.releaseDate
            holder.rating.text = item.voteAverage.toString()
        }

        override fun getItemCount(): Int {
            return itemList.size
        }
    }
}