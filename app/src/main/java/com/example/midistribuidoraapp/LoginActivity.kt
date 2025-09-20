package com.example.midistribuidoraapp

/*clase para navegacion de android*/
import android.content.Intent
/*clase de obtencion y ciclo de vida de datos*/
import android.os.Bundle
/*clase para crear logs*/
import android.util.Log
/*clase para mensajes a usuario*/
import android.widget.Toast
/*clase para compatibilidad con versiones antiguas de android*/
import androidx.appcompat.app.AppCompatActivity
/*API para gestion de credenciales*/
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
/*clase de kotlin para creacion y destruccion de corrutinas*/
import androidx.lifecycle.lifecycleScope
/*clases para boton de google Sign-In y login*/
import com.google.android.gms.common.SignInButton
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
/*autenticacion por Firebase, valida token y administra sesiones*/
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

/*clase que define la actividad de Login, hereda de AppCompatActivity*/
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth/*instancia de firebase para autenticar*/
    private lateinit var credentialManager: CredentialManager/*gestor de credenciales*/
    private lateinit var signInButton: SignInButton/*boton de google para inicio de sesion*/

    /*metodo de carga del layout inicial*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /*inicio de firebase Auth y credential manager*/
        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)
        /*boton de login y funcion sign in*/
        signInButton = findViewById(R.id.sign_in_button)
        signInButton.setOnClickListener {
            lifecycleScope.launch {
                signInWithGoogle()
            }
        }
    }
    /*metodo para pasar a pagina de negocio si el usuario ya esta autenticado*/
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            goToMainActivity()
        }
    }
    /*funcion que inicia el flujo de autenticacion con google*/
    private suspend fun signInWithGoogle() {
            /*configuracion de login con google usando el client_id del archivo
            * json obtenido en Firebase*/
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()
            /*creacion de solicitud de credenciales*/
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        /*bloque try/catch para manejo de excepciones*/
        /*llamada a credential manager para obtener la credencial del usuario*/
        try {
            val result = credentialManager.getCredential(this, request)
            val credential = result.credential
            /*verificacion de la credencial sea del tipo google token y extre
            * el token para autenticar con firebase, si hay error lo registra y muestra*/
            if (credential is GoogleIdTokenCredential) {
                val googleIdToken = credential.idToken
                firebaseAuthWithGoogle(googleIdToken)
            } else {
                Log.e("LoginActivity", "Error: La credencial no es del tipo esperado.")
                Toast.makeText(this, "Error de inicio de sesión.", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("LoginActivity", "Falló el inicio de sesión con Google", e)
            Toast.makeText(this, "Falló el inicio de sesión con Google.", Toast.LENGTH_SHORT).show()
        }
    }

    /*se usa el token extraido de google para crear la credencial de firebase
    * si la autenticacion es exitosa redirige al usuario a la pagina de negocio
    * si falla muestra una mensaje de fallo*/
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Autenticación exitosa.", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(this, "Falló la autenticación con Firebase.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /*funcion para ir directamnete a la vista principal y cerrar esta para manejo de memoria
    * buenas practicas recomendadas para liberar memoria*/
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}