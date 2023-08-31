package com.example.firetvwelcomevids

import android.util.Log
import com.contentful.java.cda.CDAArray
import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDAEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentfulClient (
    val token:String,
    val space:String,
) {
    // Create the Contentful client.
    private val client = CDAClient
        .builder()
        .setToken(token) // required
        .setSpace(space) // required
        .setEnvironment("master") // optional, defaults to `master`
        .build()

//    private val client = CDAClient
//        .builder()
//        .setToken("Q1HdeN6hmMMw8-CQFFaE52UppSoU_xga1VQ-6J_qRK8") // required
//        .setSpace("r82o8v93np7l") // required
//        .setEnvironment("master") // optional, defaults to `master`
//        .build()

    val one: CDAEntry = client
        .fetch(CDAEntry::class.java)
        .one("1rANuN6qnknhumHLgcjYLK")

    suspend fun getOne(){
        try {
            val one: CDAEntry = client
                .fetch(CDAEntry::class.java)
                .one("1rANuN6qnknhumHLgcjYLK")
        } catch (e: Exception) {
            Log.e("ContentfulClient", "Error fetching entry")
        }
    }
    suspend fun fetchContentfulEntry(): CDAEntry? {
        return withContext(Dispatchers.IO) {
            try {
//                val client = CDAClient
//                    .builder()
//                    .setToken("Q1HdeN6hmMMw8-CQFFaE52UppSoU_xga1VQ-6J_qRK8") // required
//                    .setSpace("r82o8v93np7l") // required
//                    .setEnvironment("master") // optional, defaults to `master`
//                    .build()

                val one: CDAEntry = client
                    .fetch(CDAEntry::class.java)
                    .one("1rANuN6qnknhumHLgcjYLK")


                // You can return the fetched CDAEntry here
                one
            } catch (e: Exception) {
                // Handle exceptions here
                null
            }
        }
    }
    suspend fun fetchContentfulAll(): CDAArray? {
        return withContext(Dispatchers.IO) {
            try {

                val all: CDAArray = client
                    .fetch(CDAEntry::class.java)
                    .all()

                // You can return the fetched CDAEntry here
                all
            } catch (e: Exception) {
                // Handle exceptions here
                null
            }
        }
    }
    suspend fun fetchMedia(){
        return withContext(Dispatchers.IO) {
            try {

                val all: CDAArray = client
                    .fetch(CDAEntry::class.java)
                    .all()

                var list: List<Media> = mutableListOf()

                // You can return the fetched CDAEntry here
                var id = 0
                for (entry in all.items()) {
                    Log.i(TAG, "fetchMedia: ${entry.id()}")
                    id++
                    var item: CDAEntry = client
                        .fetch(CDAEntry::class.java)
                        .one(entry.id())
                    Log.i(TAG, "fetchMedia: ${item.getField<String>("title")}")
                    list.plus(
                        Media(
                            id.toLong(),
                            item.getField<String>("title"),
                            item.getField<String>("category"),
                            null,
                            null,
                            null,
                            "videoUrl",
                            "studio"
                        )
                    )
                }
                Log.i(TAG, "fetchMedia: $list")

//                list
            } catch (e: Exception) {
//                emptyList<Media>()
                // Handle exceptions here
            }
        }
    }



//    companion object {
//        private val TRANSLUCENT = true
////        private TAG = "ContentfulClient"
//    }
}
