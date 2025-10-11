# Historias de Usuario

## HU-01: Registro de Nuevo Usuario
**Prioridad:** Alta

**Historia de Usuario:**  
Como un **visitante nuevo**,  
quiero **poder registrar una cuenta usando mi correo electrónico y una contraseña**,  
para poder **acceder a las funcionalidades de la tienda de forma segura**.

**Criterios de Aceptación:**
- Dado que estoy en la pantalla de Login y he ingresado un correo válido no registrado y una contraseña (mínimo 6 caracteres).
- Cuando presiono el botón **"Registrarse"**.
- Entonces se crea una nueva cuenta en **Firebase Authentication**.
- Y se envía un correo de verificación a mi dirección de email.
- Y permanezco en la pantalla de Login para poder iniciar sesión.

---

## HU-02: Inicio de Sesión de Usuario Registrado
**Prioridad:** Alta

**Historia de Usuario:**  
Como un **usuario ya registrado**,  
quiero **poder iniciar sesión con mi correo y contraseña**,  
para poder **acceder a la pantalla principal de la aplicación**.

**Criterios de Aceptación:**
- Dado que estoy en la pantalla de Login y he ingresado las credenciales de un usuario existente.
- Cuando presiono el botón **"Iniciar Sesión"**.
- Entonces el sistema valida mis credenciales contra **Firebase Authentication**.
- Y soy redirigido a la pantalla principal del **Catálogo de Productos**.

---

## HU-03: Visualización Dinámica de Productos
**Prioridad:** Alta

**Historia de Usuario:**  
Como **cliente**,  
quiero **que la lista de productos se cargue desde una base de datos en la nube**,  
para poder **ver siempre el catálogo y los precios más recientes**.

**Criterios de Aceptación:**
- Dado que he iniciado sesión exitosamente.
- Cuando la pantalla principal (**MainActivity**) se carga.
- Entonces la aplicación realiza una consulta a la colección **productos** en **Cloud Firestore**.
- Y el **RecyclerView** se puebla dinámicamente con todos los productos encontrados, mostrando su nombre y precio.

---

## HU-04: Cálculo de Costo con API de Rutas
**Prioridad:** Alta

**Historia de Usuario:**  
Como **cliente**,  
quiero **calcular el costo de envío basado en la distancia de conducción real**,  
para poder **obtener un desglose preciso antes de pagar**.

**Criterios de Aceptación:**
- Dado que he seleccionado productos y estoy en la pantalla del catálogo.
- Cuando presiono el botón **"Calcular Costo"**.
- Entonces la aplicación obtiene mi ubicación GPS actual.
- Y realiza una llamada a la **API de Google Routes** para obtener la distancia en kilómetros y la ruta codificada.
- Y se abre la pantalla de **Checkout (CheckoutActivity)** mostrando el desglose completo del costo.

---

## HU-05: Visualización de Ruta en Mapa de Checkout
**Prioridad:** Media

**Historia de Usuario:**  
Como **cliente**,  
quiero **ver la ruta de entrega en un mapa**,  
para poder **tener una confirmación visual del trayecto de mi despacho**.

**Criterios de Aceptación:**
- Dado que he presionado **"Calcular Costo"** y estoy en la pantalla de **Checkout**.
- Cuando el mapa termina de cargar.
- Entonces se muestra un marcador en mi ubicación y otro en la ubicación de la bodega.
- Y se dibuja en el mapa la **polilínea de la ruta calculada** por la API de Google Routes.

---

## HU-06: Monitoreo de Temperatura en Tiempo Real
**Prioridad:** Alta

**Historia de Usuario:**  
Como un **despachador**,  
quiero **ver la temperatura actual del camión y recibir alertas**,  
para poder **asegurar la cadena de frío de los productos**.

**Criterios de Aceptación:**
- Dado que estoy en la pantalla de **Monitoreo (TemperaturaActivity)**.
- Cuando el sensor de **Wokwi** envía un nuevo dato de temperatura a **Realtime Database**.
- Entonces el valor de temperatura en la pantalla se actualiza en tiempo real.
- Y si la temperatura supera el umbral configurado, se activa una **alarma visual, sonora y de vibración**.

---

## HU-07: Cierre de Sesión de Usuario
**Prioridad:** Media

**Historia de Usuario:**  
Como un **usuario con la sesión iniciada**,  
quiero **tener una opción para cerrar mi sesión**,  
para poder **proteger mi cuenta en el dispositivo**.

**Criterios de Aceptación:**
- Dado que me encuentro en la pantalla principal de productos.
- Cuando presiono el botón **"Cerrar Sesión"**.
- Entonces mi sesión de **Firebase** se cierra.
- Y soy redirigido a la pantalla de Login.
- Y no puedo volver a la pantalla principal usando el botón **"Atrás"** del dispositivo.  
