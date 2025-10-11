# Pruebas Unitarias

## CPU-001: Verificación de Regla de Negocio - Envío Gratuito
**Fecha:** 11-10-2025  
**Área Funcional:** Módulo de Logística y Despacho  
**Funcionalidad:** Cálculo de Costo de Envío

**Descripción:**  
Este caso de prueba unitario valida que la función `calcularCostoDespachoLocal` retorne **0** cuando el subtotal de la compra es superior a **$50.000** y la distancia es menor o igual a **20 km**.

**Datos de Entrada:**
```text
totalCompra = 55000.0  
distanciaEnKm = 15.0
```  

**Resultado Esperado:**  
La función debe retornar **0.0**.

**Requerimientos de Ambiente:**  
No aplica (Prueba unitaria estándar ejecutada en JVM).

---

## CPU-002: Verificación de Regla de Negocio - Tarifa Estándar
**Fecha:** 11-10-2025  
**Área Funcional:** Módulo de Logística y Despacho  
**Funcionalidad:** Cálculo de Costo de Envío

**Descripción:**  
Valida que la función `calcularCostoDespachoLocal` aplique la tarifa de **$150/km** para compras entre **$25.000** y **$49.999**.

**Datos de Entrada:**
```text
totalCompra = 30000.0  
distanciaEnKm = 10.0
```  

**Resultado Esperado:**  
La función debe retornar **1500.0**.

**Requerimientos de Ambiente:**  
No aplica (Prueba unitaria estándar ejecutada en JVM).

---

## CPU-003: Verificación de Regla de Negocio - Tarifa Alta
**Fecha:** 45941  
**Área Funcional:** Módulo de Logística y Despacho  
**Funcionalidad:** Cálculo de Costo de Envío

**Descripción:**  
Valida que la función `calcularCostoDespachoLocal` aplique la tarifa de **$300/km** para compras inferiores a **$25.000**.

**Datos de Entrada:**
```text
totalCompra = 20000.0  
distanciaEnKm = 5.0
```  

**Resultado Esperado:**  
La función debe retornar **1500.0**.

**Requerimientos de Ambiente:**  
No aplica (Prueba unitaria estándar ejecutada en JVM).  
