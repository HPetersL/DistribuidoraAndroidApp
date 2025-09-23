package com.example.midistribuidoraapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
import java.text.NumberFormat
import java.util.Locale


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
    private lateinit var layoutResultados: LinearLayout
    private lateinit var tvDistancia: TextView
    private lateinit var tvValorNeto: TextView
    private lateinit var tvIva: TextView
    private lateinit var tvSubtotalProductos: TextView
    private lateinit var tvCostoEnvio: TextView
    private lateinit var tvTotalPagar: TextView
    private lateinit var btnCalcularCosto: Button
    private lateinit var btnPagar: Button
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
        layoutResultados = findViewById(R.id.layout_resultados)
        tvDistancia = findViewById(R.id.tv_distancia)
        tvValorNeto = findViewById(R.id.tv_valor_neto)
        tvIva = findViewById(R.id.tv_iva)
        tvSubtotalProductos = findViewById(R.id.tv_subtotal_productos)
        tvCostoEnvio = findViewById(R.id.tv_costo_envio)
        tvTotalPagar = findViewById(R.id.tv_total_pagar)
        btnCalcularCosto = findViewById(R.id.btn_calcular_costo)
        btnPagar = findViewById(R.id.btn_pagar)
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion)
        recyclerView = findViewById(R.id.rv_productos)

        recyclerView.layoutManager = LinearLayoutManager(this)

        cargarProductosDesdeFirestore()
        guardarUbicacionDelUsuario()

        /*se establecen las acciones de cada boton del layout*/
        btnCalcularCosto.setOnClickListener {
            val subtotal = calcularSubtotal()
            if (subtotal > 0) {
                layoutResultados.visibility = View.GONE
                btnPagar.visibility = View.GONE
                // Se lanza una coroutine para la llamada de red
                lifecycleScope.launch {
                    solicitarUbicacionYCalcularRuta(subtotal)
                }
            } else {
                Toast.makeText(this, "Agrega al menos un producto.", Toast.LENGTH_SHORT).show()
            }
        }

        btnPagar.setOnClickListener {
            Toast.makeText(this, getString(R.string.mensaje_pago_simulado), Toast.LENGTH_LONG).show()
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

    /*da el formato a los valores numericos en la moneda local, en este caso
    * se usa la region CL*/
    private fun formatCurrency(value: Double): String {
        val chileLocale = Locale.Builder().setLanguage("es").setRegion("CL").build()
        val format = NumberFormat.getCurrencyInstance(chileLocale)
        format.maximumFractionDigits = 0
        return format.format(value)
    }

    /*permisos y ubicacion para calcular costos de envio*/
    /*se verifica si hay permiso de ubicacion activo y si no, se solicitan
    * los permisos de ubicacion FINE_LOCATION, si no se conceden se detiene
    * la ejecucion "return"*/
    private fun solicitarUbicacionYCalcularRuta(subtotalProductos: Double) {
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
                                val distanciaEnMetros = result.routes[0].distanceMeters
                                val distanciaEnKm = distanciaEnMetros / 1000.0

                                /*calculamos el costo del envio
                                * descomponemos el subtotal en valor neto e IVA
                                * calculamos el costo total con envio incluido*/
                                val costoEnvio = calcularCostoDespachoLocal(subtotalProductos, distanciaEnKm)
                                val valorNeto = subtotalProductos / (1 + IVA_RATE)
                                val iva = subtotalProductos - valorNeto
                                val totalPagar = subtotalProductos + costoEnvio

                                /*actualizamos la interfaz con los datos obtenidos antes*/
                                actualizarResultadosUI(distanciaEnKm, valorNeto, iva, subtotalProductos, costoEnvio, totalPagar)
                                /*el buen manejo de errores, largo pero atrapa hasta el aire*/
                            } else {
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

    private fun actualizarResultadosUI(distancia: Double, neto: Double, iva: Double, subtotal: Double, envio: Double, total: Double) {
        /*muestra los valores de los textview correspondientes y usa formatCurrency para
        * mostrar los valores en moneda chilena*/
        tvDistancia.text = getString(R.string.label_distancia, distancia)
        tvValorNeto.text = getString(R.string.label_valor_neto, formatCurrency(neto))
        tvIva.text = getString(R.string.label_iva, formatCurrency(iva))
        tvSubtotalProductos.text = getString(R.string.label_subtotal_productos, formatCurrency(subtotal))
        tvCostoEnvio.text = getString(R.string.label_costo_envio, formatCurrency(envio))
        tvTotalPagar.text = getString(R.string.label_total_pagar, formatCurrency(total))

        /*muestra el bloque con los resultados y el boton pagar solo cuando
        * el usuario llega de manera conforme a este paso*/
        layoutResultados.visibility = View.VISIBLE
        btnPagar.visibility = View.VISIBLE
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
                    // No es necesario lanzar una coroutine aquí directamente
                    solicitarUbicacionYCalcularRuta(subtotal)
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