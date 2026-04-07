package com.univpm.unirun

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // NavHostFragment è configurato direttamente nel layout XML.
        // Nessuna logica aggiuntiva necessaria qui.
    }
}