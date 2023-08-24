package com.example.firetvwelcomevids

import android.util.Log
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        if (fileProperty != property || fileProperty == "all") return null;
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
                "$serverURL$vidsDir$fileName",
                type
            )
            Log.i(MainFragment.TAG, "parseFileIntoMovie: $newMovie")
            return newMovie;
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

        // Disable strict host key checking (for testing purposes; consider removing in production)
        session.setConfig("StrictHostKeyChecking", "no")
        session.connect()

        val channel: ChannelSftp = session.openChannel("sftp") as ChannelSftp
        channel.connect()

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
        } finally {
            channel.disconnect()
            session.disconnect()
        }

        fileMap
    }
}

internal const val TAG = "ServerReader"
