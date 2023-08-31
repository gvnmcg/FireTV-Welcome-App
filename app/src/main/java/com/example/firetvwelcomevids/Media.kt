package com.example.firetvwelcomevids

import java.io.File
import java.io.Serializable

/**
 * Movie class represents video entity with title, description, image thumbs and video url.
 */
data class Media(
    var id: Long = 0,
    var title: String,
    var description: String,
    var backgroundImage: File? = null,
    var cardImage: File? = null,
    var pdfFile: File? = null,
    var videoUrl: String? = null,
    var studio: String
) : Serializable {

    override fun toString(): String {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", studio='" + studio + '\'' +
                ", description='" + description + '\'' +
//                ", backgroundImageUrl='" + backgroundImageUrl + '\'' +
//                ", cardImageUrl='" + cardImageUrl + '\'' +
                '}'
    }

    companion object {
        internal const val serialVersionUID = 727566175075960653L
    }
}