package com.example.midistribuidoraapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.Locale

/*se define la clase de dato para cada campo de los productos*/
data class Producto(val id: String, val nombre: String, val precio: Double)

class MainActivity : AppCompatActivity() {
    /*cliente para obtener ubicacion del usuario*/
    private lateinit var fusedLocationClient: FusedLocationProviderClient
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
    /*lista constante de productos disponibles en la app
    * con identificador unico, nombre para mostrar y precio*/
    private val productos = listOf(
        Producto("harina", "Harina", 1500.0),
        Producto("aceite", "Aceite", 2500.0),
        Producto("azucar", "Azúcar", 1200.0),
        Producto("pastas", "Pastas", 900.0),
        Producto("arroz", "Arroz", 1100.0),
        Producto("atun", "Atún", 1300.0),
        Producto("snack", "Snack", 800.0)
    )
    /*aqui se relaciona cada ID de producto con su vista en el layout de la app*/
    private val productViewIds = mapOf(
        "harina" to R.id.item_harina,
        "aceite" to R.id.item_aceite,
        "azucar" to R.id.item_azucar,
        "pastas" to R.id.item_pastas,
        "arroz" to R.id.item_arroz,
        "atun" to R.id.item_atun,
        "snack" to R.id.item_snack
    )
    /*aqui se alamecenan las cantidades seleccionadas para cada producto de la lista*/
    private val cantidades = mutableMapOf<String, Int>()

    /*carga el layout principal y prepara la accion*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)/*layout principal*/

        /*se inicializa el cliente de ubicacion del usuario*/
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

        /*aqui se inicia cada cantidad de productos en 0
        * se configura la vista de los productos disponibles*/
        productos.forEach { producto ->
            cantidades[producto.id] = 0
            val viewId = productViewIds[producto.id]
            if (viewId != null) {
                val productoView = findViewById<View>(viewId)
                setupProductoView(productoView, producto)
            }
        }

        /*se establecen las acciones de cada boton del layout*/
        btnCalcularCosto.setOnClickListener {
            val subtotal = calcularSubtotal()
            if (subtotal > 0) {
                layoutResultados.visibility = View.GONE
                btnPagar.visibility = View.GONE
                solicitarUbicacionYCalcularEnvio(subtotal)
            } else {
                Toast.makeText(this, "Agrega al menos un producto.", Toast.LENGTH_SHORT).show()
            }
        }

        btnPagar.setOnClickListener {
            Toast.makeText(this, getString(R.string.mensaje_pago_simulado), Toast.LENGTH_LONG).show()
        }

        /*se agrego este boton por si el usuario desea cambiar de cuenta de google*/
        btnCerrarSesion.setOnClickListener {
            signOut()
        }
    }

    /*se implementa el cierre de sesion en firebase y se cambia la vista a la pagina de login
    * de la aplicacion para poder volver a acceder con otra cuenta
    * finishi() limpia las actividades de la memoria*/
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /*aqui asociamos el nombre del producto con la cantidad y creamos una variable
    * para cada prodocto */
    private fun setupProductoView(view: View, producto: Producto) {
        val tvNombre: TextView = view.findViewById(R.id.tv_nombre_producto)
        val tvPrecio: TextView = view.findViewById(R.id.tv_precio_producto)
        val tvCantidad: TextView = view.findViewById(R.id.tv_cantidad)
        val btnRestar: Button = view.findViewById(R.id.btn_restar)
        val btnSumar: Button = view.findViewById(R.id.btn_sumar)

        /*se muestra el nombre del producto, se le asigna un formato de moneda
        * y se inicia en valor 0*/
        tvNombre.text = producto.nombre
        tvPrecio.text = formatCurrency(producto.precio)
        tvCantidad.text = "0"

        /*se implementa la accion del boton sumar, aumenta la cantidad de
        * un prodcuto en uno y se guarda en la variable*/
        btnSumar.setOnClickListener {
            var cantidadActual = cantidades[producto.id] ?: 0
            cantidadActual++
            cantidades[producto.id] = cantidadActual
            tvCantidad.text = cantidadActual.toString()
        }

        /*accion del boton resta, disminuye el producto en una unidad y se implementa
        * para >0 para que el valor no pueda ser negativo*/
        btnRestar.setOnClickListener {
            var cantidadActual = cantidades[producto.id] ?: 0
            if (cantidadActual > 0) {
                cantidadActual--
                cantidades[producto.id] = cantidadActual
                tvCantidad.text = cantidadActual.toString()
            }
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
    * los permisos de ubicacion FINE_LOCATION, si no se conceden se cancela
    * el return*/
    private fun solicitarUbicacionYCalcularEnvio(subtotalProductos: Double) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        /*se usa fusedLocationClient en vez de LocationManager porque es mas preciso
        * al usar GPS, wifi y redes moviles para detectar ubicacion*/
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    /*calcula la distancia entre la bodega y el usuario usando la formula de Haversine*/
                    val distancia = LocationUtils.calculateHaversineDistance(location.latitude, location.longitude, BODEGA_LAT, BODEGA_LON)
                    val costoEnvio = ShippingCalculator.calcularCostoDespacho(subtotalProductos, distancia)

                    /*se separa el valor neto y el IVA y se suma el costo del envio
                    *  mostrando el total final a pagar*/
                    val valorNeto = subtotalProductos / (1 + IVA_RATE)
                    val iva = subtotalProductos - valorNeto
                    val totalPagar = subtotalProductos + costoEnvio

                    /*muestra los valores de los textview correspondientes y usa formatCurrency para
                    * mostrar los valores en moneda chilena*/
                    tvDistancia.text = getString(R.string.label_distancia, distancia)
                    tvValorNeto.text = getString(R.string.label_valor_neto, formatCurrency(valorNeto))
                    tvIva.text = getString(R.string.label_iva, formatCurrency(iva))
                    tvSubtotalProductos.text = getString(R.string.label_subtotal_productos, formatCurrency(subtotalProductos))
                    tvCostoEnvio.text = getString(R.string.label_costo_envio, formatCurrency(costoEnvio))
                    tvTotalPagar.text = getString(R.string.label_total_pagar, formatCurrency(totalPagar))

                    /*muestra el bloqye con los resultados y el boton pagar solo cuando
                    * el usuario llega de manera conforme a este paso*/
                    layoutResultados.visibility = View.VISIBLE
                    btnPagar.visibility = View.VISIBLE
                } else {/*manejo de errores en caso de que no se pueda detectar la ubicacion*/
                    Toast.makeText(this, "No se pudo obtener la ubicación. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                }
            }/*captura de excepciones por si las moscas*/
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener ubicación: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /*este bloque verifica si se conceden permismos de ubicacion por parte del usuario
    * de ser asi se vuelven a calcular el envio para continuar con el flujo nomrla*/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                val subtotal = calcularSubtotal()
                if (subtotal > 0) {
                    solicitarUbicacionYCalcularEnvio(subtotal)
                }
            } else {/*manejo de excepcion por no concesion de permiso de ubicacion*/
                Toast.makeText(this, "El permiso de ubicación es necesario para calcular el envío.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*constanste globales utilizadas*/
    companion object {
        /*valor del IVA en chile*/
        private const val IVA_RATE = 0.19
        /*ubicacion de la plaza de armas de Osorno*/
        private const val BODEGA_LAT = -40.5736
        private const val BODEGA_LON = -73.1356
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}