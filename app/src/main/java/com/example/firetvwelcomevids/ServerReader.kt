package com.example.firetvwelcomevids

import android.content.res.Resources
import android.util.Log
import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException
//import java.io.File
//import java.util.Properties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Vector

//class ServerReader(resources: Resources) {

//    val REMOTE_HOST = "xxx.xxx.xxx.xxx"
val REMOTE_HOST = "access960789971.webspace-data.io"
val USERNAME = "u112089698"
val PASSWORD = "505NotFound!"
val REMOTE_PORT = 22

val house_number = "h1"

//    val resources = resources

val serverURL = "https://piruproperties.com/android/vids/"
//    val backgroundSlug = "${resources.getString(R.string.house_number)}-bg"


fun connectToSftp(): Boolean {

//    var jschSession: Session? = null
    GlobalScope.launch(Dispatchers.IO) {

        val jsch = JSch()
        val jschSession: Session? = jsch.getSession(USERNAME, REMOTE_HOST, REMOTE_PORT)
        try {
//            jsch.setKnownHosts("/home/mkyong/.ssh/known_hosts")
            jschSession?.setPassword(PASSWORD)

//            val config = Properties()
//            config["StrictHostKeyChecking"] = "no"
//            jschSession.setConfig(config)
            jschSession?.setConfig("StrictHostKeyChecking", "no")

            jschSession?.connect(10000)
            val sftp: Channel? = jschSession?.openChannel("sftp")
            Log.i(TAG, "connectToSftp: $sftp")
            sftp?.connect(5000)
            val channelSftp: ChannelSftp = sftp as ChannelSftp
            Log.i(TAG, "connectToSftp: ${sftp.pwd()}")
//            Log.i(TAG, "connectToSftp: ${sftp.cd("/android/vids")}")
            Log.i(TAG, "connectToSftp: ${sftp.ls("/android/vids").map { it.toString() }}")

//            channelSftp.put(srcFile.absolutePath, ftpPath)
            channelSftp.exit()

        } catch (e: JSchException) {
            e.printStackTrace()
        } catch (e: SftpException) {
            e.printStackTrace()
        } finally {
            jschSession?.disconnect()
        }
    }
    return true
}


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
                "$serverURL$slug.mp4",
                type
            )
            Log.i(MainFragment.TAG, "parseFileName: $newMovie")
            return newMovie;
        }
    }
    return null;
}

public fun filenamesFromSftp(): List<String>? {
    Log.i(TAG, "filenamesFromSftp: ")

    var videoFileNames: List<String>? = listOf();
//    var jschSession: Session? = null
    GlobalScope.launch(Dispatchers.IO) {

        val jsch = JSch()
        val jschSession: Session? = jsch.getSession(USERNAME, REMOTE_HOST, REMOTE_PORT)
        try {
            jschSession?.setPassword(PASSWORD)
            jschSession?.setConfig("StrictHostKeyChecking", "no")
            jschSession?.connect(10000)

            val sftp: Channel? = jschSession?.openChannel("sftp")
            sftp?.connect(5000)
            val channelSftp: ChannelSftp = sftp as ChannelSftp
            Log.i(TAG, "sftp channel: $channelSftp")

            val files = sftp.ls("/android/vids")

            videoFileNames = files.map { if (it is ChannelSftp.LsEntry) it.filename else null }
                .filterNotNull()
                .filter { it.startsWith("h1") }
                .filter { it.endsWith(".mp4") || it.endsWith(".pdf") }
                .sortedBy { it }

            for (filename in videoFileNames!!) {
                Log.i(TAG, "sftp file name: $filename")
            }

            channelSftp.exit()

        } catch (e: JSchException) {
            e.printStackTrace()
        } catch (e: SftpException) {
            e.printStackTrace()
        } finally {
            jschSession?.disconnect()
        }
    }
    return videoFileNames
}

fun moviesFromSftp(): List<Movie?>? {

    var videoFileNames: List<Movie?>? = null;
//    var jschSession: Session? = null
    GlobalScope.launch(Dispatchers.IO) {

        val jsch = JSch()
        val jschSession: Session? = jsch.getSession(USERNAME, REMOTE_HOST, REMOTE_PORT)
        try {
//            jsch.setKnownHosts("/home/mkyong/.ssh/known_hosts")
            jschSession?.setPassword(PASSWORD)

//            val config = Properties()
//            config["StrictHostKeyChecking"] = "no"
//            jschSession.setConfig(config)
            jschSession?.setConfig("StrictHostKeyChecking", "no")

            jschSession?.connect(10000)
            val sftp: Channel? = jschSession?.openChannel("sftp")
            Log.i(TAG, "connectToSftp: $sftp")
            sftp?.connect(5000)
            val channelSftp: ChannelSftp = sftp as ChannelSftp
//            Log.i(TAG, "connectToSftp: ${sftp.pwd()}")
//            Log.i(TAG, "connectToSftp: ${sftp.cd("/android/vids")}")
            val files = sftp.ls("/android/vids")

            var count = 0


            videoFileNames = files.map { if (it is ChannelSftp.LsEntry) it.filename else null }
                .filterNotNull()
                .filter { it.startsWith("h1") }
                .filter { it.endsWith(".mp4") || it.endsWith(".pdf") }
//                .map { it.replace(".mp4", "") }
                .sortedBy { it }
                .map { parseFileName(count++, it) }

            for (filename in videoFileNames!!) {
                Log.i(TAG, "sftp file name: $filename")
            }

//            channelSftp.put(srcFile.absolutePath, ftpPath)
            channelSftp.exit()

        } catch (e: JSchException) {
            e.printStackTrace()
        } catch (e: SftpException) {
            e.printStackTrace()
        } finally {
            jschSession?.disconnect()
        }
    }
    return videoFileNames
}

//    companion object {

internal const val TAG = "ServerReader"
