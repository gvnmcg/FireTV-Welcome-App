package com.example.firetvwelcomevids

import android.util.Log
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


const val serverURL = "https://piruproperties.com"
const val vidsDir = "/android/media/"
const val imagesDir = "/android/images/"

private fun parseFileIntoMovie(id: Number, fileName: String, property: String): Movie? {
    val parts = fileName.lowercase().split(" ")

    // If the file name is in the format "property category title.type"
    // then we can parse it into a Movie object
    // and the title can have spaces in it
    if (parts.size == 3) {
        val fileProperty = parts[0]
        if (fileProperty != property || fileProperty == "all") return null
        val category = parts[1].replace("_", " ").replace("-", " ")
        val titleAndType = parts.subList(2, parts.size).joinToString(" ").split(".")
//        val titleAndType = parts[2].split(".")

        if (titleAndType.size == 2) {
            val type = titleAndType[1]
            val title = titleAndType[0]
                .replace("_", " ")
                .replace("-", " ")
                .split(" ")
                .map { it -> it.replaceFirstChar { it2 -> it2.uppercase() } }
                .joinToString(" ")

            // make the title title case form lower case, but exclude words like "and", "or", "the", etc.
            val titleCaseTitle = title.split(" ")
                .map { it -> it.replaceFirstChar { it2 -> it2.uppercase() } }
                .joinToString(" ") { it ->
                    if (setOf(
                            "A",
                            "An",
                            "And",
                            "Or"
                        ).contains(it)
                    ) it.lowercase() else it
                }

            val slug = fileName.split(".")[0]
            val backgroundSlug = "$property-bg"
            val newMovie = Movie(
                id.toLong(),
                titleCaseTitle,
                category,
                "$serverURL$imagesDir$backgroundSlug.png",
                "$serverURL$imagesDir$slug.png",
                fileName,
                type
            )
            Log.i(MainFragment.TAG, "parseFileIntoMovie: $newMovie")
            return newMovie
        }
    }
    return null;
}


suspend fun movieMapSFTP(
    property: String,
    username: String,
    password: String,
    remoteHost: String,
    remotePort: Int
): Map<String, MutableList<Movie>> {
    return withContext(Dispatchers.IO) {

        val jsch = JSch()
        val session: Session = jsch.getSession(username, remoteHost, remotePort)
        session.setPassword(password)
        Log.i(TAG, "movieMapSFTP: Connecting...")

        // Disable strict host key checking (for testing purposes; consider removing in production)
        session.setConfig("StrictHostKeyChecking", "no")
        session.connect()

        val channel: ChannelSftp = session.openChannel("sftp") as ChannelSftp
        channel.connect()
        Log.i(TAG, "movieMapSFTP: Connected to $remoteHost:$remotePort")


        //Get map of all media files on server
        val fileMap: Map<String, MutableList<Movie>> = try {

            // Get all "Movie" Media from file names in the server's media folder
            val files: List<Movie> = channel.ls("/android/media")
                .filterIsInstance<ChannelSftp.LsEntry>().map { it.filename }
                .sortedBy { it }
                .mapIndexed { ix, it -> parseFileIntoMovie(ix, it, property) }
                .filterNotNull()

            // Sort Movie Media into a Map of Categories to Lists of Movies
            var categorizedMap = mutableMapOf<String, MutableList<Movie>>()
            for (item in files!!) {
                val category = item!!.description as String
                val itemList = categorizedMap.getOrPut(category) { mutableListOf() }
                itemList.add(item)
            }
            categorizedMap

        } catch (e: Exception) {
            emptyMap()
        }


        fileMap
    }
}

suspend fun csvMediaList(
    property: String,
    username: String,
    password: String,
    remoteHost: String,
    remotePort: Int
): List<Movie> {
    return withContext(Dispatchers.IO) {

        val jsch = JSch()
        val session: Session = jsch.getSession(username, remoteHost, remotePort)
        session.setPassword(password)
        Log.i(TAG, "movieMapSFTP: Connecting...")

        // Disable strict host key checking (for testing purposes; consider removing in production)
        session.setConfig("StrictHostKeyChecking", "no")
        session.connect()

        val channel: ChannelSftp = session.openChannel("sftp") as ChannelSftp
        channel.connect()
        Log.i(TAG, "movieMapSFTP: Connected to $remoteHost:$remotePort")

        //        # A line beginning with a # (hash) gets ignored
        //        # This will explain the structure of this file
        //        # Each line is a piece of content separated by a comma
        //        # In this format:
        //        # (1) category, (2) title on the App, (3) filetype/youtube, (4) Video filename/Video ID, (5) card image filename
        //        # The location of the content is in the properties respective folder
        //        # the videos will appear in order as they are here.

        val stream: InputStream = channel.get("/android/media/$property.csv")
        val mediaList:List<Movie> = try {
            val ls = mutableListOf<Movie>()
            val br = BufferedReader(InputStreamReader(stream))
            var line: String?
            var count = 0
            while (br.readLine().also { line = it } != null) {
                if (!line!!.startsWith("#")
                    && line!!.isNotBlank()) {

                    Log.i(TAG, "csv Line: $line")
                    val parts = line!!.split(",")
                    Log.i(TAG, "line parts: $parts")
                    if (parts.size == 5) {
                        val category = parts[0]
                        val title = parts[1]
                        val type = parts[2] // "youtube" or "mp4"
                        val video = parts[3] // youtube video ID or mp4 slug
                        val imageSlug = parts[4] // image filename slug

                        val newMovie = Movie(
                            count++.toLong(),
                            title,
                            category,
                            "$property-bg.png",
                            imageSlug,
                            video,
                            type
                        )
                        ls.add(newMovie)
                    }
                }
            }
            ls
        } catch (io: IOException) {
            println("Exception occurred during reading file from SFTP server due to " + io.message)
            io.message
            emptyList<Movie>()
        } catch (e: java.lang.Exception) {
            println("Exception occurred during reading file from SFTP server due to " + e.message)
            e.message
            emptyList<Movie>()
        } finally {
            channel.disconnect()
            session.disconnect()
        }
        mediaList
    }

}

fun getMediaMapFromList(list: List<Movie>): Map<String, MutableList<Movie>> {
    var categorizedMap = mutableMapOf<String, MutableList<Movie>>()
    for (item in list!!) {
        val category = item!!.description as String
        val itemList = categorizedMap.getOrPut(category) { mutableListOf() }
        itemList.add(item)
    }
    return categorizedMap
}

internal const val TAG = "ServerReader"
