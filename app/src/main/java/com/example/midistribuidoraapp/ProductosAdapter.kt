package com.example.midistribuidoraapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

/*creamos la clase principal que conecta los datos de la lista
* prodcutos con la interfaz, cantidades guarda cuantos productos se
* han seleccionado por id*/
class ProductosAdapter(
    private val productos: List<Producto>,
    private val cantidades: MutableMap<String, Int>
) : RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder>() {


    /*claseo con las referencias a los textview de los prodctos y botones*/
    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tv_nombre_producto)
        val tvPrecio: TextView = itemView.findViewById(R.id.tv_precio_producto)
        val tvCantidad: TextView = itemView.findViewById(R.id.tv_cantidad)
        val btnRestar: Button = itemView.findViewById(R.id.btn_restar)
        val btnSumar: Button = itemView.findViewById(R.id.btn_sumar)
    }

    /*metodo para crear nuevas instancias de viewholder, una para cada producto detectado*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    /*asocia los datos de los productos con su elemento visual, mostrando los datos id,
    * precio formateado CL y cantidad actual, tambien controla accion de los botones */
    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]

        holder.tvNombre.text = producto.nombre
        holder.tvPrecio.text = formatCurrency(producto.precio)
        holder.tvCantidad.text = (cantidades[producto.id] ?: 0).toString()

        holder.btnSumar.setOnClickListener {
            var cantidadActual = cantidades[producto.id] ?: 0
            cantidadActual++
            cantidades[producto.id] = cantidadActual
            holder.tvCantidad.text = cantidadActual.toString()
        }

        holder.btnRestar.setOnClickListener {
            var cantidadActual = cantidades[producto.id] ?: 0
            if (cantidadActual > 0) {
                cantidadActual--
                cantidades[producto.id] = cantidadActual
                holder.tvCantidad.text = cantidadActual.toString()
            }
        }
    }

    /*devuelve la cantidad de prodcutos en la lista para que
    * recyclerView sepa cuantas instancias de producto mostrar*/
    override fun getItemCount(): Int {
        return productos.size
    }

    /*formato para moneda chilena sin decimales*/
    private fun formatCurrency(value: Double): String {
        val chileLocale = Locale.Builder().setLanguage("es").setRegion("CL").build()
        val format = NumberFormat.getCurrencyInstance(chileLocale)
        format.maximumFractionDigits = 0
        return format.format(value)
    }
}