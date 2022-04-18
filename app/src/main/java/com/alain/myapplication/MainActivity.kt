package com.alain.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

//testée une fois de plus
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val btnConex: Button = this.findViewById<Button>(R.id.btn_connexion)
        btnConex.setOnClickListener {
            val laConnexion: Intent = Intent(this, Connexion::class.java)
            laConnexion.putExtra("url", "192.168.1.47")
            val res = startActivity(laConnexion)

        }
    }
}