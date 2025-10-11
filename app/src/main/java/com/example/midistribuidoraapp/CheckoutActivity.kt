package com.example.midistribuidoraapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
/*imports para fase 3*/
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import java.text.NumberFormat
import java.util.*
import android.util.Log

/*clase que define la actividad para recibir el mapa*/
class CheckoutActivity : AppCompatActivity(), OnMapReadyCallback {

    /*componente visual del mapa*/
    private lateinit var mapView: MapView
    /*instancia del mapa*/
    private var googleMap: GoogleMap? = null
    /*ubicacion del dispositivo*/
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    /*actualizaciones de ubicacion*/
    private lateinit var locationCallback: LocationCallback
    /*marcador del usuario en el mapa*/
    private var userMarker: Marker? = null


    /*coordenadas de la bodega "plaza de armas de osorno"*/
    private val bodegaLat = -40.5736
    private val bodegaLon = -73.1356

    /*metodo para iniciar actividad, config del mapa y ubicacion de cliente*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        /*muestra los datos de la compra*/
        displayOrderSummary()

        /*configuracion del boton de pago simulado por el momento*/
        findViewById<Button>(R.id.btn_pagar_final).setOnClickListener {
            Toast.makeText(this, "Procesando pago...", Toast.LENGTH_LONG).show()
        }
    }

    /*extrae datos del intent de mainactivity*/
    private fun displayOrderSummary() {
        val extras = intent.extras ?: return

        /*formato para pesos chilenos*/
        val chileLocale = Locale.Builder().setLanguage("es").setRegion("CL").build()
        val currencyFormat = NumberFormat.getCurrencyInstance(chileLocale).apply {
            maximumFractionDigits = 0
        }

        /*valores de textviews de la interfaz*/
        findViewById<TextView>(R.id.tv_distancia_checkout).text = getString(R.string.label_distancia, extras.getDouble("distancia"))
        findViewById<TextView>(R.id.tv_valor_neto_checkout).text = getString(R.string.label_valor_neto, currencyFormat.format(extras.getDouble("neto")))
        findViewById<TextView>(R.id.tv_iva_checkout).text = getString(R.string.label_iva, currencyFormat.format(extras.getDouble("iva")))
        findViewById<TextView>(R.id.tv_subtotal_productos_checkout).text = getString(R.string.label_subtotal_productos, currencyFormat.format(extras.getDouble("subtotal")))
        findViewById<TextView>(R.id.tv_costo_envio_checkout).text = getString(R.string.label_costo_envio, currencyFormat.format(extras.getDouble("envio")))
        findViewById<TextView>(R.id.tv_total_pagar_checkout).text = getString(R.string.label_total_pagar, currencyFormat.format(extras.getDouble("total")))
    }

    /*ejecuta la ruta y ubicacion en mapa cuando el mapa esta listo*/
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d("MapDebug", "onMapReady iniciado. Mapa está listo.")

        /*verificar permisos de ubicacion*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("MapDebug", "Permiso de ubicación no concedido al momento de cargar el mapa.")
            Toast.makeText(this, "Permiso de ubicación no concedido.", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("MapDebug", "Permiso de ubicación concedido.")
        googleMap?.uiSettings?.isMyLocationButtonEnabled = true

        val extras = intent.extras
        if (extras == null) {
            Log.e("MapDebug", "Error: No se recibieron datos (extras) desde MainActivity.")
            return
        }

        /*polilinea para ruta y ubicacion de usuario mediante Lat y Lng*/
        /*se dejan los logs ya que los use para debugging*/
        val encodedPolyline = extras.getString("encodedPolyline")
        val userLat = extras.getDouble("userLat")
        val userLng = extras.getDouble("userLng")

        Log.d("MapDebug", "Datos del Intent recibidos. Polilínea: ${encodedPolyline?.substring(0, 10)}...")
        Log.d("MapDebug", "Ubicación Usuario: $userLat, $userLng")


        val userLocation = LatLng(userLat, userLng)
        val bodegaLocation = LatLng(bodegaLat, bodegaLon)

        /*se usa drawroute para mostrar la ruta en mapa*/
        Log.d("MapDebug", "Llamando a drawRoute...")
        drawRoute(encodedPolyline, userLocation, bodegaLocation)

        /*si hay polilinea se actualiza ubicacion en tiempo real*/
        if (encodedPolyline != null) {
            Log.d("MapDebug", "Llamando a startLocationUpdates...")
            startLocationUpdates()
        }
    }

    /*funcion drawroute, esto dibuja la linea en mapa decodificando polyutil, agrega los marcadores
    * de usuario y bodega y ajusta la camara para mostrar la ruta completa en el mapa*/
    private fun drawRoute(encodedPolyline: String?, userStartLocation: LatLng, bodegaLocation: LatLng) {
        if (encodedPolyline == null) {
            googleMap?.addMarker(MarkerOptions().position(bodegaLocation).title("Bodega"))
            googleMap?.addMarker(MarkerOptions().position(userStartLocation).title("Tu Ubicación"))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userStartLocation, 15f))
            return
        }

        /*color de la ruta de despacho en mapa*/
        val lineColor = ContextCompat.getColor(this, R.color.purple_500)

        val polylineOptions = PolylineOptions()
            .addAll(PolyUtil.decode(encodedPolyline))
            .color(lineColor)
            .width(15f)
        googleMap?.addPolyline(polylineOptions)

        /*marcadores de usuario y bodega*/
        googleMap?.addMarker(MarkerOptions().position(bodegaLocation).title("Bodega"))
        userMarker = googleMap?.addMarker(MarkerOptions().position(userStartLocation).title("Tu Ubicación").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

        /*ajuste de camara para el mapa*/
        val bounds = LatLngBounds.Builder()
            .include(userStartLocation)
            .include(bodegaLocation)
            .build()
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    /*funcion para verificar permiso de ubicacion, guarda y actualiza la ubicacion del
    * usuario en el mapa cada 5 segundos*/
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    userMarker?.position = currentLatLng
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    /*detiene las actualizaciones de ubicacion cuando no se esta utilizando
    * para ahorra bateria y recursos segun normas de uso android 8.0*/
    override fun onStop() {
        super.onStop()
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

/*ciclo de vida del mapview*/
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
}