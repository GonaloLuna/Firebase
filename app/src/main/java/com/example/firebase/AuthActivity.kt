package com.example.firebase

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100
    private val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración de Firebase completa")
        analytics.logEvent("InitScreen", bundle)

        val signUpbutton = findViewById<Button>(R.id.signUpButton)
        val enterbutton = findViewById<Button>(R.id.logOutButton)
        val googleButton = findViewById<Button>(R.id.GoogleBbutton)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        title = "Autentificación"

        signUpbutton.setOnClickListener {

            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                ).addOnCompleteListener {

                    if (it.isSuccessful) {

                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)

                    } else {

                        showAlert()

                    }

                }

            }

        }

        enterbutton.setOnClickListener {

            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {

                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                ).addOnCompleteListener {

                    if (it.isSuccessful) {

                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)

                    } else {

                        showAlert()

                    }

                }

            }

        }

        googleButton.setOnClickListener{

            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            val googleClient : GoogleSignInClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)

        }

        facebookButton.setOnClickListener {

            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))

            LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult>{

                override fun onSuccess(result: LoginResult?) {

                    result?.let {

                        val token = it.accessToken

                        val credential = FacebookAuthProvider.getCredential(token.token)

                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {

                            if (it.isSuccessful){

                                showHome(it.result?.user?.email?: "", ProviderType.FACEBOOK)

                            }else{

                                showAlert()

                            }

                        }

                    }

                }

                override fun onCancel() {}

                override fun onError(error: FacebookException?) {

                    showAlert()

                }

            })

        }

        session()

    }

    private fun session(){

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email:String? = prefs.getString("email", null)
        val provider:String? = prefs.getString("provider", null)

        if(email != null && provider != null){

            showHome(email, ProviderType.valueOf(provider))

        }

    }

    private fun showAlert(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error de autentificación")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType){

        val homeIntent = Intent(this, HomeActivity::class.java).apply {

            putExtra("email", email)
            putExtra("provider", provider.name)

        }
        startActivity(homeIntent)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        callbackManager.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SIGN_IN){

            val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                val account : GoogleSignInAccount? = task.getResult(ApiException::class.java)

                if (account != null){

                    val credential : AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)

                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {

                        if (it.isSuccessful){

                            showHome(account.email?: "", ProviderType.GOOGLE)

                        }else{

                            showAlert()

                        }

                    }

                }

            }catch (e: ApiException){

                showAlert()

            }

        }

    }

}