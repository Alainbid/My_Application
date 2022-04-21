package com.alain.myapplication

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.measureTimeMillis

class Connecter(url: URL) {
    var tps: Long = 0

    fun connecter(url: URL): String {
        var res: String = " "
        val monThread = Thread {

            tps = measureTimeMillis {
                try {
                    val httpClient = url.openConnection() as HttpURLConnection

                    if (httpClient.responseCode == HttpURLConnection.HTTP_OK) {
                        try {
                            val stream = BufferedInputStream(httpClient.inputStream)
                            data = readStream(inputStream = stream)
                        } catch (e: Exception) {
                            res = "pas de readStream"
                            e.printStackTrace()
                            println("err $e")
                        } finally {
                            httpClient.disconnect()
                        }
                    } else {
                        res = "pas de connexion"
                        println("---------------------ERROR ${httpClient.responseCode}")
                    }
                } catch (e: Error) {
                    runningThread = false
                    res = e.toString()
                    println(" --------------------code erreur :$e")

                } finally {
                    println("--------------------Connexion  terminée  ---  $res")
                    runningThread = false


                }
            }
        }//*************************fin du Thread
        monThread.start()

        while (monThread.isAlive) {
            runningThread = true
        }

        runningThread = false
        println(" ----------------duree du thread = $tps")
        println(" -------------------thread alive = " + monThread.isAlive)
        return data
    }


    fun readStream(inputStream: BufferedInputStream): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        bufferedReader.forEachLine { stringBuilder.append(it) }
        return stringBuilder.toString()

    }
}