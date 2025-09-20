#  Proyecto Distribuidora de Alimentos (App Android)

Este repositorio contiene el código fuente de un prototipo de aplicación móvil nativa para Android, desarrollada como parte de un caso de estudio para el modulo de aplcaciones moviles, el cual se presento como proyecto de las semanas 4, 5 y 6. El propósito de la aplicación es ofrecer una solución tecnológica a una empresa de distribución de alimentos para automatizar y transparentar el proceso de cálculo de costos de despacho a domicilio.

---

##  Evolución del Proyecto (Fase II)

Inicialmente, el proyecto se concibió como un prototipo básico para validar la lógica de negocio del cálculo de envío. En esta segunda fase, la aplicación ha evolucionado hacia una arquitectura más robusta y escalable, implementando mejoras significativas:

- Se reemplazó el cálculo de distancia geodésica (fórmula Haversine) por una integración con la **API de Google Maps Directions**, obteniendo la distancia de conducción real con mayor precisión.
- El catálogo de productos estático fue migrado a una base de datos en la nube (**Cloud Firestore**), permitiendo una gestión dinámica sin necesidad de actualizar la aplicación.
- El sistema de autenticación fue modificado, pasando de un SSO con Google a un sistema tradicional de registro e inicio de sesión con correo electrónico y contraseña.
- Se implementó la persistencia de datos del usuario, como su última ubicación conocida, en **Firebase Realtime Database**.

---

##  Requerimientos Funcionales (RF)

###  Módulo de Autenticación y Usuarios

- **RF-01:** Registro de nuevos usuarios con correo electrónico y contraseña (Fase II).
- **RF-02:** Inicio de sesión para usuarios existentes (Fase II).
- **RF-03:** Validación de credenciales con Firebase Authentication.
- **RF-04:** Redirección a la interfaz principal tras inicio de sesión exitoso.
- **RF-05:** Cierre de sesión y retorno a la vista de login.

###  Módulo de Catálogo y Compra

- **RF-06:** Lista dinámica de productos desde Cloud Firestore (Fase II).
- **RF-07:** Modificación de cantidades por producto.
- **RF-08:** Cálculo y desglose detallado de la compra:
  - Valor Neto
  - IVA
  - Subtotal Productos
  - Distancia
  - Costo de Envío
  - Total a Pagar

###  Módulo de Logística y Despacho

- **RF-09:** Obtención de ubicación GPS actual del usuario.
- **RF-10:** Cálculo de distancia de conducción real desde bodega fija usando Google Maps Directions API (Fase II).
- **RF-11:** Aplicación de reglas de negocio para cálculo del costo de envío.

###  Módulo de Persistencia de Datos

- **RF-12:** Almacenamiento de ubicación GPS y timestamp en Firebase Realtime Database tras inicio de sesión (Fase II).

---

##   Requerimientos No Funcionales (RNF)

###   Plataforma y Compatibilidad

- **RNF-01:** Desarrollo nativo para Android.
- **RNF-02:** Compatibilidad con Android 6.0 (Marshmallow, API 23) o superior.

###  Tecnología

- **RNF-03:** Lenguaje principal: Kotlin.
- **RNF-04:** IDE: Android Studio.
- **RNF-05:** Geolocalización mediante Google Play Services.
- **RNF-06:** Backend: Firebase (Authentication, Realtime Database, Cloud Firestore).
- **RNF-07:** Cálculo de rutas con Google Maps Directions API.

###  Gestión del Proyecto

- **RNF-08:** Código fuente gestionado en repositorio Git.
- **RNF-09:** Documentación principal en archivo `README.md`.
- **RNF-10:** Tareas gestionadas como *Issues* en GitHub, siguiendo el formato de Historias de Usuario.

---

##  Stack Tecnológico

- **Lenguaje:** Kotlin  
- **Entorno:** Android Studio  
- **Backend y Autenticación:** Firebase (Authentication, Cloud Firestore, Realtime Database)  
- **APIs de Google:** Google Maps Directions API, Google Play Services (Location)  
- **Librerías Clave:** Retrofit & Gson para networking  
- **Gestión:** Git y GitHub
