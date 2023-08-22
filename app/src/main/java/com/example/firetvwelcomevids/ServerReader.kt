package com.example.firetvwelcomevids

import android.util.Log
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


val REMOTE_HOST = "access960789971.webspace-data.io"
val USERNAME = "u112089698"
val PASSWORD = "505NotFound!"
val REMOTE_PORT = 22

val house_number = "h1"

val serverURL = "https://piruproperties.com/android/vids/"

private fun parseFileName(id: Number, fileName: String): Movie? {
    val parts = fileName.split("-")
    val serverURL = "https://piruproperties.com/android/vids/"

    if (parts.size == 3) {
        val house = parts[0]
        if (house != house_number)
            return null;

        val category = parts[1]
        val typeAndTitle = parts[2].split(".")
        val slug = fileName.split(".")[0]

        if (typeAndTitle.size == 2) {
            val type = typeAndTitle[1]
            val title = typeAndTitle[0].split("_")
                .map { it -> it.replaceFirstChar { it2 -> it2.uppercase() } }
                .joinToString(" ")


            val backgroundSlug = "h1-bg"
            val newMovie = Movie(
                id.toLong(),
                title,
                category,
                "$serverURL$backgroundSlug.png",
                "$serverURL$slug.png",
                "$serverURL$fileName",
                type
            )
            Log.i(MainFragment.TAG, "parseFileName: $newMovie")
            return newMovie;
        }
    }
    return null;
}

suspend fun movieMapSFTP(): Map<String, MutableList<Movie>> {
    return withContext(Dispatchers.IO) {

        val jsch = JSch()
        val session: Session = jsch.getSession(USERNAME, REMOTE_HOST, REMOTE_PORT)
        session.setPassword(PASSWORD)

        // Disable strict host key checking (for testing purposes; consider removing in production)
        session.setConfig("StrictHostKeyChecking", "no")

        session.connect()

        val channel: ChannelSftp = session.openChannel("sftp") as ChannelSftp
        channel.connect()

        val fileMap: Map<String, MutableList<Movie>> = try {
            val files: List<Movie> = channel.ls("/android/vids")
            .filterIsInstance<ChannelSftp.LsEntry>().map { it.filename }
                .filter { it.startsWith("h1") }
                .filter { it.endsWith(".mp4") || it.endsWith(".pdf") }
                .sortedBy { it }
                .map { parseFileName(0, it) }
                .filterNotNull()

            var categorizedMap = mutableMapOf<String, MutableList<Movie>>()

            for (item in files!!) {
//                if (item is Movie) {
                    val category = item!!.description as String
                    val itemList = categorizedMap.getOrPut(category) { mutableListOf() }
                    itemList.add(item)
//                }
            }
            categorizedMap

        } catch (e: Exception) {
//            emptyList() // Return an empty list in case of an error
            emptyMap()
        } finally {
            channel.disconnect()
            session.disconnect()
        }

        fileMap
    }
}

internal const val TAG = "ServerReader"
