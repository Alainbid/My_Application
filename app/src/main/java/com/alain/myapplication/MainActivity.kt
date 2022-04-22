package com.alain.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections.emptySet


var data: String = ""//"Temperature 00.00 Humidite 00.00 duree 1 frequence 0.00 xxx"
var err: String = " OK"
var runningThread = true

//testée une fois de plus
class MainActivity : AppCompatActivity() {


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val hum = this.findViewById<TextView>(R.id.tx_humid)
        val tmp = this.findViewById<TextView>(R.id.tx_temp)
        val dur = this.findViewById<TextView>(R.id.tx_duree)
        val fre = this.findViewById<TextView>(R.id.tx_freq)

        val btnClose = this.findViewById<Button>(R.id.btn_connexion)
        val url = URL("http://192.168.1.47/")
        val policy = ThreadPolicy.Builder()
            .permitNetwork()
            .build()
        StrictMode.setThreadPolicy(policy)

        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection

        try {
            println(" ------------------------réponse " + urlConnection.responseCode)
            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val stream = BufferedInputStream(urlConnection.inputStream)
                    data = readStream(inputStream = stream)
                } catch (e: Exception) {
                    println("err $e")
                    Toast.makeText(this, "Erreur de connexion $e", Toast.LENGTH_LONG).show()
                } finally {
                    urlConnection.disconnect()
                }
            } else {
                Toast.makeText(this, "Erreur de connexion $url", Toast.LENGTH_LONG).show()
                println("---------------------ERROR ${urlConnection.responseCode}")
            }
        } catch (e: Exception) {
            println(" ------------------------erreur$e ")
            Toast.makeText(this, "Erreur de connexion $e", Toast.LENGTH_LONG).show()
        } finally {
            urlConnection.disconnect()
        }
        actualiser(data, hum, tmp, dur, fre)

        btnClose.setOnClickListener {
            finishAndRemoveTask()
        }

    }
}

//********************* lecture des données
fun readStream(inputStream: BufferedInputStream): String {
    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
    val stringBuilder = StringBuilder()
    bufferedReader.forEachLine { stringBuilder.append(it) }
    return stringBuilder.toString()
}

//************* recherche des correspondances dans le texte ********************
private fun String?.indexesOf(pat: String, ignoreCase: Boolean = true): List<Int> =
    pat.toRegex(ignoreCaseOpt(ignoreCase))
        .findAll(this ?: "")
        .map { it.range.first }
        .toList()

private fun ignoreCaseOpt(ignoreCase: Boolean) =
    if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()


//********************** mise à jour des champs
private fun actualiser(data: String, hum: TextView, tmp: TextView, dur: TextView, fre: TextView) {

    var position: List<Int> = data.indexesOf("humidi", true)
    (data.substring(position[0], position[0] + 13) + "%").also { hum.text = it }

    position = data.indexesOf("Temper", true)
    (data.substring(position[0], position[0] + 16) + " °C").also { tmp.text = it }

    position = data.indexesOf("duree ", true)
    var tx = data.substring(position[0], position[0] + 8)
    var z = tx.length
    ("Arrosage pendant " + tx.substring(z - 2) + " s").also { dur.text = it }

    position = data.indexesOf("frequence ", true)
    tx = data.substring(position[0], position[0] + 12)
    z = tx.length
    ("toutes les " + tx.substring(z - 3) + " heures").also { fre.text = it }
}


