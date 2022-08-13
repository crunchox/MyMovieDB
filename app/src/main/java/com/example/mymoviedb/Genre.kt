package com.example.mymoviedb

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

data class Genre(

    @field:SerializedName("genres")
    val genres: Collection<GenresItem> = ArrayList()
)

data class GenresItem(

    @field:SerializedName("name")
    val name: String = "",

    @field:SerializedName("id")
    val id: Int = 0
)
