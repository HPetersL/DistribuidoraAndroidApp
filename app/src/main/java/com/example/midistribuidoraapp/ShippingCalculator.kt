package com.example.midistribuidoraapp
/*se define la instancia unica donde se encapsula la logica
* de las reglas de negocio*/
object ShippingCalculator {

    /*se define la funcion con sus parametros y el tipo de dato*/
    fun calcularCostoDespacho(totalCompra: Double, distanciaEnKm: Double): Double {
        /*se aplica la regla 1 de negocio: despacho gratis*/
        if (totalCompra > 50000 && distanciaEnKm <= 20) {
            return 0.0 /*despacho costo cero*/
        }
        return when {
            /*se aplica regla 2: Compra entre $25.000 y $49.999*/
            totalCompra in 25000.0..49999.0 -> {
                150.0 * distanciaEnKm // $150 por km
            }
             /*se aplica regla 3: Compra menor a $25.000*/
            totalCompra < 25000.0 -> {
                300.0 * distanciaEnKm // $300 por km
            }
            /*se aplica una ultima regla adicional en caso de que el envio supere
            *los $50.000 y se mayor a 20km de distancia, lo que aplicara la tarifa
            * mas baja posible*/
            else -> {
                150.0 * distanciaEnKm
            }
        }
    }
}