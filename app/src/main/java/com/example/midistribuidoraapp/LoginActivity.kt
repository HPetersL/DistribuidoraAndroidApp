package com.example.midistribuidoraapp

/*clase para navegacion de android*/
import android.content.Intent
/*clase de obtencion y ciclo de vida de datos*/
import android.os.Bundle
/*clase para crear y manipular botones*/
import android.widget.Button
/*clase para campos de entrada de datos en pantalla*/
import android.widget.EditText
/*clase para mensajes a usuario*/
import android.widget.Toast
/*clase para compatibilidad con versiones antiguas de android*/
import androidx.appcompat.app.AppCompatActivity
/*autenticacion por Firebase y administrador de sesiones*/
import com.google.firebase.auth.FirebaseAuth

/*clase que define la actividad de Login, hereda de AppCompatActivity*/
class LoginActivity : AppCompatActivity() {

    /*instancia de firebase para autenticar*/
    private lateinit var auth: FirebaseAuth

    /*instancias de botones y campos de entrada de texto*/
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    /*metodo de carga del layout inicial*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /*instancia de firebase auth*/
        auth = FirebaseAuth.getInstance()

        /* referencia a la vistas del layout*/
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnRegister = findViewById(R.id.btn_register)

        /*listener para el boton login y registrar*/
        btnLogin.setOnClickListener {
            loginUser()
        }

        btnRegister.setOnClickListener {
            registerUser()
        }
    }

    /*metodo para pasar a pagina de negocio si el usuario ya esta autenticado*/
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            goToMainActivity()
        }
    }

    /*obtencion del texto ingresados en los campos del layout
    * toma el string ingresado en email y contraseña*/
    private fun registerUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        /*manoje de excepciones, verifica que los campos no esten vacios*/
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
            return
        }

        /*metodo para crear la cuenta en firebase con los datos ingresados*/
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    /*usamos la funcion de firebase para enviar correo de confimacion*/
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                /*mensaje de exito*/
                                Toast.makeText(baseContext, "Registro exitoso. Revisa tu correo para verificar tu cuenta.", Toast.LENGTH_LONG).show()
                            } else {
                                /*manejo de excepciones*/
                                Toast.makeText(baseContext, "Registro exitoso, pero falló el envío del correo de verificación.", Toast.LENGTH_LONG).show()
                            }
                        }
                    /*cerramos sesion asi el usuario debe entrar de forma manual con sus nuevas credenciales*/
                    auth.signOut()
                } else {
                    /*manejo de error si falla el registro*/
                    Toast.makeText(baseContext, "Falló el registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    /*metodos para obtener los datos de los campos para login*/
    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        /*manejo de excepciones, verifica que los campos no esten vacios*/
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
            return
        }

        /*metodo para iniciar sesion en firebase con las credenciales creadas*/
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    /*registro exitoso envia al usuario a la pantalla principal*/
                    goToMainActivity()
                } else {
                    Toast.makeText(baseContext, "Falló la autenticación. Revisa tus credenciales.", Toast.LENGTH_LONG).show()
                }
            }
    }

    /*metodo para pasar a pantalla principal y cerrar la interfaz de login
    * para ahorrar memoria, esto impide que el usuario vuelva a la pantalla de login
    * con el boton volver atras*/
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}