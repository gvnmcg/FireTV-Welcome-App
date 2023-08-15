package com.example.firetvwelcomevids

import java.io.Serializable

/**
 * Movie class represents video entity with title, description, image thumbs and video url.
 */
data class Movie(
        var id: Long = 0,
        var title: String? = null,
        var description: String? = null,
        var backgroundImageUrl: String? = null,
        var cardImageUrl: String? = null,
        var videoUrl: String? = null,
        var studio: String? = null
) : Serializable {

    override fun toString(): String {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", backgroundImageUrl='" + backgroundImageUrl + '\'' +
                ", cardImageUrl='" + cardImageUrl + '\'' +
                '}'
    }

    public fun titleCase(): String? {
        val articles = setOf("a", "an", "the")
        return title?.split("_")
            ?.map { it.lowercase() }
            ?.map { it -> it.replaceFirstChar { it2 ->  it2.uppercase()  } }
            ?.map { it -> if (articles.contains(it.lowercase())) it.lowercase()}
            ?.joinToString(" ")
    }

    companion object {
        internal const val serialVersionUID = 727566175075960653L
    }
}