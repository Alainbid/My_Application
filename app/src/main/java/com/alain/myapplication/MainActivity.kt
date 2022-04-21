package com.alain.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.properties.Delegates
import kotlin.system.measureTimeMillis

var data: String = " "//"Temperature 00.00 Humidite 00.00 duree 1 frequence 0.00 xxx"
var err: String = " "
var runningThread = true

//testée une fois de plus
class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val txduree = findViewById<TextView>(R.id.tx_duree)
        val txfreq = findViewById<TextView>(R.id.tx_freq)
        val btnClose = this.findViewById<Button>(R.id.btn_connexion)
        val url = URL("http://192.168.1.47/")

        var tps by Delegates.notNull<Long>()


        val monThread = Thread {

            tps = measureTimeMillis {
                try {
                    val httpClient = url.openConnection() as HttpURLConnection

                    if (httpClient.responseCode == HttpURLConnection.HTTP_OK) {
                        try {
                            val stream = BufferedInputStream(httpClient.inputStream)
                            readStream(inputStream = stream)
                        } catch (e: Exception) {
                            err = "pas de readStream"
                            e.printStackTrace()
                            println("err $e")


                        } finally {
                            httpClient.disconnect()
                        }
                    } else {
                        err = "pas de connexion"
                        println("---------------------ERROR ${httpClient.responseCode}")
                    }
                } catch (e: Error) {
                    runningThread = false
                    println(" --------------------code erreur :$e")
                    err = e.toString()


                } finally {
                    println("--------------------Connexion  terminée")
                    runningThread = false

                }
            }
        }//*************************fin du Thread
        departThread(monThread)

        while (monThread.isAlive) {
            runningThread = true
        }
        Toast.makeText(this, "resultat de connexion :  $url  $err", Toast.LENGTH_LONG).show()
        println(" ----------------duree du thread = $tps")
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


        btnClose.setOnClickListener {
            finishAndRemoveTask()
        }
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


