package com.example.midistribuidoraapp

/*funcion para comparar valores esperados vs obtenidos*/
import org.junit.Assert.assertEquals
/*funciona para marcar metodos como pruebas TEST*/
import org.junit.Test

/*clase que agrupa las pruebas unitarias*/
class CalculoCostosTest {

    /*creamos el metodo de las reglas de negocio para autocontencion,
    *copiamos esta funcion desde mainactivity y la pegamos aqui para
    *mantener la prueba replicada localmente*/
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

    /*prueba numero 1, envio gratuito*/
    @Test
    fun `CPU-001 - el envío para compras sobre 50k y menos de 20km es gratuito`() {
        /*arrange*/
        val totalCompra = 55000.0
        val distancia = 15.0

        /*act*/
        val costo = calcularCostoDespachoLocal(totalCompra, distancia)

        /*assert*/
        assertEquals("El costo debería ser 0.0", 0.0, costo, 0.0)
    }

    /*prueba numero dos, tarfa estandar*/
    @Test
    fun `CPU-002 - la tarifa estándar de 150 por km se aplica correctamente`() {
        /*arrange*/
        val totalCompra = 30000.0
        val distancia = 10.0

        /*act*/
        val costo = calcularCostoDespachoLocal(totalCompra, distancia)

        /*assert*/
        assertEquals("El costo debería ser 1500.0", 1500.0, costo, 0.0)
    }


    /*prueba numero tres, tarifa alta*/
    @Test
    fun `CPU-003 - la tarifa alta de 300 por km se aplica correctamente`() {
        /*arrange*/
        val totalCompra = 20000.0
        val distancia = 5.0

        /*act*/
        val costo = calcularCostoDespachoLocal(totalCompra, distancia)

        /*assert*/
        assertEquals("El costo debería ser 1500.0", 1500.0, costo, 0.0)
    }

}