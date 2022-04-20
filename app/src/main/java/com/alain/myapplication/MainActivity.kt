package com.alain.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.properties.Delegates
import kotlin.system.measureTimeMillis

var data: String = "Temperature 00.00 Humidite 00.00 duree 1 frequence 0.00 xxx"
var runningThread: Boolean = true
var tps by Delegates.notNull<Long>()

//testée une fois de plus
class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val txduree = findViewById<TextView>(R.id.tx_duree)
        val txfreq = findViewById<TextView>(R.id.tx_freq)
        val btnConex = this.findViewById<Button>(R.id.btn_connexion)

        val monThread = Thread {
            val url = URL("http://192.168.1.47/")
            tps = measureTimeMillis {
//            if (!runningThread) {
//                return@Thread
//            }
                try {
                    val httpClient = url.openConnection() as HttpURLConnection
                    if (httpClient.responseCode == HttpURLConnection.HTTP_OK) {
                        try {
                            val stream = BufferedInputStream(httpClient.inputStream)
                            readStream(inputStream = stream)
                        } catch (e: Exception) {
                            e.printStackTrace()

                        } finally {
                            httpClient.disconnect()
                        }
                    } else {
                        println("---------------------ERROR ${httpClient.responseCode}")
                    }
                } catch (e: Error) {
                    println(" --------------------code erreur :$e")

                } finally {
                    println("--------------------Connexion  terminée")
                    runningThread = false

                }
            }
        }//*************************fin du Thread
        departThread(monThread)
        // une fois le thread terminé le bouton connexion est visible

        while (monThread.isAlive) {
            runningThread = true
        }
        println(" ----------------duree du thread = $tps")
        btnConex.setOnClickListener {
            finishAndRemoveTask()
        }
        println(" -------------------thread alive = " + monThread.isAlive)
        var position: List<Int> = data.indexesOf("humidi", true)
        findViewById<TextView>(R.id.tx_humid).text =
            data.substring(position[0], position[0] + 13) + "%"
        position = data.indexesOf("Temper", true)
        findViewById<TextView>(R.id.tx_temp).text =
            data.substring(position[0], position[0] + 16) + " °C"

        position = data.indexesOf("duree ", true)
        var tx = data.substring(position[0], position[0] + 8)
        var z = tx.length
        txduree.text = "Arrosage pendant " + tx.substring(z - 2) + " s"

        position = data.indexesOf("frequence ", true)
        tx = data.substring(position[0], position[0] + 12)
        z = tx.length
        txfreq.text = "toutes les " + tx.substring(z - 3) + " heures"

    }
}

private fun departThread(monThread: Thread) {
    println("---------- debut du thread")
    println(" -------------------------nom du thread " + monThread.name)

    monThread.start()
}


private fun readStream(inputStream: BufferedInputStream) {
    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
    val stringBuilder = StringBuilder()
    bufferedReader.forEachLine { stringBuilder.append(it) }

    data = stringBuilder.toString()

}

//************* recherche des correspondances dans le texte ********************
private fun String?.indexesOf(pat: String, ignoreCase: Boolean = true): List<Int> =
    pat.toRegex(ignoreCaseOpt(ignoreCase))
        .findAll(this ?: "")
        .map { it.range.first }
        .toList()

private fun ignoreCaseOpt(ignoreCase: Boolean) =
    if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()


