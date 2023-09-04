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


fun levenshteinDistance(s1: String, s2: String): Int {
    val m = s1.length
    val n = s2.length
    val dp = Array(m + 1) { IntArray(n + 1) }

    for (i in 0..m) {
        for (j in 0..n) {
            if (i == 0) {
                dp[i][j] = j
            } else if (j == 0) {
                dp[i][j] = i
            } else {
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + if (s1[i - 1] == s2[j - 1]) 0 else 1
                )
            }
        }
    }

    return dp[m][n]
}

fun findMostSimilarString(target: String, strings: List<String>): String? {
    var mostSimilarString: String? = null
    var smallestDistance = Int.MAX_VALUE

    for (string in strings) {
        val distance = levenshteinDistance(target, string)

        if (distance < smallestDistance) {
            smallestDistance = distance
            mostSimilarString = string
        }
    }

    return mostSimilarString
}

suspend fun csvMediaList(
    property: String,
//    username: String,
//    password: String,
//    remoteHost: String,
//    remotePort: Int,
    session: Session,
    channel: ChannelSftp
): List<Movie> {
    return withContext(Dispatchers.IO) {

//        val jsch = JSch()
//        val session: Session = jsch.getSession(username, remoteHost, remotePort)
//        session.setPassword(password)
//        Log.i(TAG, "movieMapSFTP: Connecting...")
//
//        // Disable strict host key checking (for testing purposes; consider removing in production)
//        session.setConfig("StrictHostKeyChecking", "no")
//        session.connect()
//
//        val channel: ChannelSftp = session.openChannel("sftp") as ChannelSftp
//        channel.connect()
//        Log.i(TAG, "movieMapSFTP: Connected to $remoteHost:$remotePort")

        // Get all media filenames from the server's media folder
        // to be compared with the csv file
        val mediaFileNames: List<String> = channel.ls("/android/media")
            .filterIsInstance<ChannelSftp.LsEntry>().map { it.filename }
            .sortedBy { it }

        // Get all image filenames from the server's images folder
        // to be compared with the csv file
        val imageFileNames: List<String> = channel.ls("/android/images")
            .filterIsInstance<ChannelSftp.LsEntry>().map { it.filename }
            .sortedBy { it }

        //        # A line beginning with a # (hash) gets ignored
        //        # This will explain the structure of this file
        //        # Each line is a piece of content separated by a comma
        //        # In this format:
        //        # (1) category, (2) title on the App, (3) filetype/youtube, (4) Video filename/Video ID, (5) card image filename
        //        # The location of the content is in the properties respective folder
        //        # the videos will appear in order as they are here.

        val stream: InputStream = channel.get("/android/app-content/$property.csv")
        val mediaList: List<Movie> = try {
            val ls = mutableListOf<Movie>()
            val br = BufferedReader(InputStreamReader(stream))
            var line: String?
            var count = 0
            while (br.readLine().also { line = it } != null) {
                if (!line!!.startsWith("#")
                    && line!!.isNotBlank()
                ) {

                    val parts = line!!.split(",")
                    if (parts.size == 5) {
                        val category = parts[0]
                        val title = parts[1]
                        val type = parts[2] // "youtube" or "mp4"
                        val video = parts[3] // youtube video ID or mp4 slug
                        val imageSlug = parts[4] // image filename slug

                        val realMediaName =
                            if (type == "youtube") video
                            else
                                findMostSimilarString(video, mediaFileNames)

                        val realImageName = findMostSimilarString(imageSlug, imageFileNames)

//                        if (realMediaName != null && realImageName != null) {
//                            if (realMediaName.compareTo(video) != 0
//                                || realImageName.compareTo(imageSlug) != 0
//                            ) {
//                                Log.i(TAG, "video compare: $video -> $realMediaName")
//                                Log.i(TAG, "image Compare: $imageSlug -> $realImageName")
//                            }
//                        }

                        if ((realMediaName != null) && (realMediaName.compareTo(video) != 0)) {
                            Log.i(TAG, "video compare: $video -> $realMediaName")
                        }

                        if ((realImageName != null) && (realImageName.compareTo(imageSlug) != 0))
                            Log.i(TAG, "image Compare: $imageSlug -> $realImageName")

                        val newMovie = Movie(
                            count++.toLong(),
                            title,
                            category,
                            "$property-bg.png",
                            realImageName,
                            realMediaName,
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
        }
        mediaList
    }

}

suspend fun openSessionChannel(
    username: String,
    password: String,
    remoteHost: String,
    remotePort: Int,
): Pair<Session, ChannelSftp> {
    return withContext(Dispatchers.IO) {
        val jsch = JSch()
        val session: Session = jsch.getSession(username, remoteHost, remotePort)
        session.setPassword(password)
        Log.i(TAG, "openSessionChannel: Connecting...")

        // Disable strict host key checking (for testing purposes; consider removing in production)
        session.setConfig("StrictHostKeyChecking", "no")
        session.connect()

        Log.i(TAG, "openSessionChannel: Opening Channel...")
        val channel: ChannelSftp = session.openChannel("sftp") as ChannelSftp
        channel.connect()
        Log.i(TAG, "openSessionChannel: Connected to $remoteHost:$remotePort")

        Pair(session, channel)
    }
}

suspend fun csvPropertyMap(
    session: Session,
    channel: ChannelSftp
): Map<String, String> {
    return withContext(Dispatchers.IO) {
        Log.i(TAG, "csvPropertyMap: Getting property map...")
//        val jsch = JSch()
//        val session: Session = jsch.getSession(username, remoteHost, remotePort)
//        session.setPassword(password)
//        Log.i(TAG, "movieMapSFTP: Connecting...")
//
//        // Disable strict host key checking (for testing purposes; consider removing in production)
//        session.setConfig("StrictHostKeyChecking", "no")
//        session.connect()
//
//        val channel: ChannelSftp = session.openChannel("sftp") as ChannelSftp
//        channel.connect()
//        Log.i(TAG, "movieMapSFTP: Connected to $remoteHost:$remotePort")
//


        //        # A line beginning with a # (hash) gets ignored
        //        # This will explain the structure of this file
        //        # Each line is a piece of content separated by a comma
        //        # In this format:
        //        # (1) category, (2) title on the App, (3) filetype/youtube, (4) Video filename/Video ID, (5) card image filename
        //        # The location of the content is in the properties respective folder
        //        # the videos will appear in order as they are here.

        val stream: InputStream = channel.get("/android/app-content/properties.csv")
        val propertyMap: Map<String, String> = try {
            val map = mutableMapOf<String, String>()
            val br = BufferedReader(InputStreamReader(stream))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                if (!line!!.startsWith("#")
                    && line!!.isNotBlank()
                ) {

                    val parts = line!!.split(",")
                    if (parts.size == 2) {
                        val code = parts[0]
                        val address = parts[1]
                        map[code] = address
                    }
                }
            }
            map
        } catch (io: IOException) {
            println("Exception occurred during reading file from SFTP server due to " + io.message)
            io.message
            emptyMap<String, String>()
        } catch (e: java.lang.Exception) {
            println("Exception occurred during reading file from SFTP server due to " + e.message)
            e.message
            emptyMap<String, String>()
        }
//        finally {
//            channel.disconnect()
//            session.disconnect()
//        }
        Log.i(TAG, "csvPropertyMap: Got property map...")
        propertyMap.toMap()
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
