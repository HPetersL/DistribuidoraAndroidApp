package com.example.midistribuidoraapp

import android.content.Context
import android.media.RingtoneManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.*

class TemperaturaActivity : AppCompatActivity() {

    private lateinit var tvCurrentTemp: TextView
    private lateinit var btnSaveAlert: Button

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("camiones/camion_01")
    private var valueEventListener: ValueEventListener? = null

    /*creamos una temperatura por defecto para que no sea null
    * pero siempre mostrara la de la BD*/
    private var alertTemperature: Float = -15.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperatura)

        tvCurrentTemp = findViewById(R.id.tv_current_temp)
        val tvCantidad = findViewById<TextView>(R.id.tv_cantidad)
        val btnSumar = findViewById<Button>(R.id.btn_sumar)
        val btnRestar = findViewById<Button>(R.id.btn_restar)
        btnSaveAlert = findViewById(R.id.btn_save_alert)

        /*cargar la temperatura  de alerta guardada*/
        loadAlertTemperature()
        tvCantidad.text = alertTemperature.toString()

        /*botones para aumentar temperatura de alerta*/
        btnSumar.setOnClickListener {
            alertTemperature += 1
            tvCantidad.text = alertTemperature.toString()
        }

        /*botoon para disminuir temperatura de alerta*/
        btnRestar.setOnClickListener {
            alertTemperature -= 1
            tvCantidad.text = alertTemperature.toString()
        }

        /*boton para guardar temp seleccionada*/
        btnSaveAlert.setOnClickListener {
            saveAlertTemperature(alertTemperature)
            Toast.makeText(this, "Límite de alerta guardado", Toast.LENGTH_SHORT).show()
        }

        /*escuchar temp desde la BD*/
        listenForTemperatureUpdates()
    }

    private fun listenForTemperatureUpdates() {
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                /*toma el valor en F° de firebase*/
                val tempFahrenheit = snapshot.child("temperatura_f").getValue(Float::class.java)

                if (tempFahrenheit != null) {
                    /*convertir F° a C°*/
                    val tempCelsius = (tempFahrenheit - 32) * 5.0f / 9.0f

                    /*actualizar interfazz con temp*/
                    tvCurrentTemp.text = "%.1f °C".format(tempCelsius)

                    /*comprobar si suepera temp de alerta*/
                    if (tempCelsius > alertTemperature) {
                        triggerAlarm(tempCelsius)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TemperaturaActivity, "Error al leer datos de Firebase", Toast.LENGTH_SHORT).show()
            }
        }
        database.addValueEventListener(valueEventListener!!)
    }

    private fun triggerAlarm(currentTemp: Float) {
        /*funcion para mostrar alerta visual*/
        AlertDialog.Builder(this)
            .setTitle("⚠️ Alerta de Temperatura ⚠️")
            .setMessage("¡Peligro en la cadena de frío! Temperatura actual: %.1f °C".format(currentTemp))
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            .show()

        /*funcion para sonido de alerta*/
        try {
            val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(applicationContext, notificationSound).play()
        } catch (e: Exception) {
            e.printStackTrace()
        }


        /*fiuncion para vibrar el dispositivo en alerta*/
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }

    /*funcion para guardar la temperatura seleccionada*/
    private fun saveAlertTemperature(temp: Float) {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit().putFloat("alert_temp", temp).apply()
        this.alertTemperature = temp
    }

    /*metodo para leer temperatura alerta de temperatura
    * agregamos una por defecto para evitar null*/
        private fun loadAlertTemperature() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        this.alertTemperature = prefs.getFloat("alert_temp", -15.0f)
    }

    override fun onDestroy() {
        super.onDestroy()
        /*eliminamos el listener para evigtar fugas de memoria*/
        valueEventListener?.let {
            database.removeEventListener(it)
        }
    }
}