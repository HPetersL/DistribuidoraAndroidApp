package com.example.midistribuidoraapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


/*se define la clase de dato para cada campo de los productos*/
data class Producto(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0
)

class MainActivity : AppCompatActivity() {
    /*cliente para obtener ubicacion del usuario*/
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dbRealtime: DatabaseReference
    private lateinit var dbFirestore: FirebaseFirestore

    /*referencias para elementos visuales de activity main*/
    private lateinit var btnCalcularCosto: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var recyclerView: RecyclerView

    private var productos: List<Producto> = listOf()
    /*aqui se alamecenan las cantidades seleccionadas para cada producto de la lista*/
    private val cantidades = mutableMapOf<String, Int>()
    private lateinit var productosAdapter: ProductosAdapter

    /*carga el layout principal y prepara la accion*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)/*layout principal*/

        /*se inicializa el cliente de ubicacion del usuario*/
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        dbRealtime = FirebaseDatabase.getInstance().reference
        dbFirestore = FirebaseFirestore.getInstance()

        /*aqui se relaciona cada vista con su id del layout
        * se usa finViewById como recomendacion de Android*/
        btnCalcularCosto = findViewById(R.id.btn_calcular_costo)
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion)
        recyclerView = findViewById(R.id.rv_productos)

        recyclerView.layoutManager = LinearLayoutManager(this)

        cargarProductosDesdeFirestore()
        guardarUbicacionDelUsuario()

        /*se establecen las acciones de cada boton del layout*/
        btnCalcularCosto.setOnClickListener {
            val subtotal = calcularSubtotal()
            if (subtotal > 0) {
                // Se lanza una coroutine para la llamada de red
                lifecycleScope.launch {
                    solicitarUbicacionYCalcularRuta(subtotal, this@MainActivity)
                }
            } else {
                Toast.makeText(this, "Agrega al menos un producto.", Toast.LENGTH_SHORT).show()
            }
        }

        /*se agrego este boton por si el usuario desea cambiar de cuenta*/
        btnCerrarSesion.setOnClickListener {
            signOut()
        }
    }

    /*se implementa el cierre de sesion en firebase y se cambia la vista a la pagina de login
    * de la aplicacion para poder volver a acceder con otra cuenta
    * finish() limpia las actividades de la memoria*/
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun cargarProductosDesdeFirestore() {
        dbFirestore.collection("productos")
            .get()
            .addOnSuccessListener { result ->
                productos = result.map { document -> document.toObject(Producto::class.java) }
                productos.forEach { producto -> cantidades[producto.id] = 0 }
                productosAdapter = ProductosAdapter(productos, cantidades)
                recyclerView.adapter = productosAdapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar productos: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun guardarUbicacionDelUsuario() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLocation = mapOf(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude,
                            "timestamp" to System.currentTimeMillis()
                        )
                        dbRealtime.child("user_locations").child(userId).setValue(userLocation)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Ubicación guardada.", Toast.LENGTH_SHORT).show()
                            }/*agregamos un manejo de eror por si no se guarda la unbicacion*/
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar ubicación: ${e.message}", Toast.LENGTH_LONG).show()
                                Log.e("RealtimeDB", "Fallo al escribir en la base de datos", e)
                            }
                    }
                }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    /*se calcula el subtotal de productos, esta operacion inicia en 0
    * y calcula los costos multiplicando la cantidad de productos
    * por su valor y el total de productos en "productos"*/
    private fun calcularSubtotal(): Double {
        var total = 0.0
        for (producto in productos) {
            val cantidad = cantidades[producto.id] ?: 0
            total += cantidad * producto.precio
        }
        return total
    }

    /*permisos y ubicacion para calcular costos de envio*/
    /*se verifica si hay permiso de ubicacion activo y si no, se solicitan
    * los permisos de ubicacion FINE_LOCATION, si no se conceden se detiene
    * la ejecucion "return"*/
    // CAMBIO: La función ahora recibe el Context para poder iniciar la nueva actividad
    private fun solicitarUbicacionYCalcularRuta(subtotalProductos: Double, context: Context) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        /*se usa fusedLocationClient en vez de LocationManager porque es mas preciso
       * al usar GPS, wifi y redes moviles para detectar ubicacion*/
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                /*de obtner la unicacion correctamente se sigue con el flujo*/
                if (location != null) {
                    /*se usa esta coorrutina para llamar a la API de manera asicronica
                    * sin alterar el flujo normal de la aplicacion*/
                    lifecycleScope.launch {
                        try {
                            /*cremoas el objeto routerequest con la ubicacion del usuario
                            * y la ubicacion de la bodega*/
                            val request = RouteRequest(
                                origin = Waypoint(Location(latLng = LatLng(location.latitude, location.longitude))),
                                destination = Waypoint(Location(latLng = LatLng(BODEGA_LAT, BODEGA_LON)))
                            )
                            val apiKey = BuildConfig.MAPS_API_KEY

                            /*llamamos a retrofit para realizar el post y devulve la rutas*/
                            val result = RetrofitClient.instance.computeRoutes(
                                apiKey = apiKey,
                                requestBody = request
                            )

                            /*verifica que exista al menos una ruta y devuelve la distancia en km*/
                            if (result.routes.isNotEmpty()) {
                                val route = result.routes[0]
                                val distanciaEnMetros = route.distanceMeters
                                val distanciaEnKm = distanciaEnMetros / 1000.0
                                val encodedPolyline = route.polyline?.encodedPolyline

                                val costoEnvio = calcularCostoDespachoLocal(subtotalProductos, distanciaEnKm)
                                val valorNeto = subtotalProductos / (1 + IVA_RATE)
                                val iva = subtotalProductos - valorNeto
                                val totalPagar = subtotalProductos + costoEnvio

                                /*creacion del intent para nueva interfaz*/
                                val intent = Intent(context, CheckoutActivity::class.java).apply {
                                    putExtra("distancia", distanciaEnKm)
                                    putExtra("neto", valorNeto)
                                    putExtra("iva", iva)
                                    putExtra("subtotal", subtotalProductos)
                                    putExtra("envio", costoEnvio)
                                    putExtra("total", totalPagar)
                                    putExtra("encodedPolyline", encodedPolyline)
                                    putExtra("userLat", location.latitude)
                                    putExtra("userLng", location.longitude)
                                }
                                startActivity(intent)

                            }  else {
                                Toast.makeText(this@MainActivity, "No se encontraron rutas (Routes API).", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Error de red (Routes API): ${e.message}", Toast.LENGTH_LONG).show()
                            Log.e("RoutesAPI", "Fallo en la llamada a la API", e)
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "No se pudo obtener la ubicación. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun calcularCostoDespachoLocal(totalCompra: Double, distanciaEnKm: Double): Double {
        if (totalCompra > 50000 && distanciaEnKm <= 20) {
            return 0.0
        }
        return when {
            totalCompra in 25000.0..49999.0 -> 150.0 * distanciaEnKm
            totalCompra < 25000.0 -> 300.0 * distanciaEnKm
            else -> 150.0 * distanciaEnKm
        }
    }

    /*este bloque verifica si se conceden permismos de ubicacion por parte del usuario
    * de ser asi se vuelven a calcular el envio para continuar con el flujo normal*/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                val subtotal = calcularSubtotal()
                if (subtotal > 0) {
                    solicitarUbicacionYCalcularRuta(subtotal, this)
                }
            } else { /*manejo de excepcion por no concesion de permiso de ubicacion*/
                Toast.makeText(this, "El permiso de ubicación es necesario para calcular el envío.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*constantes globales utilizadas*/
    companion object {
        /*valor del IVA en chile*/
        private const val IVA_RATE = 0.19
        /*ubicacion de la plaza de armas de Osorno*/
        private const val BODEGA_LAT = -40.5736
        private const val BODEGA_LON = -73.1356
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}