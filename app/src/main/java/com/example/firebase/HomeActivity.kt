package com.example.firebase

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*

enum class ProviderType{
    BASIC,
    GOOGLE,
    FACEBOOK
}

class HomeActivity : AppCompatActivity() {

    private  val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val logOutButton = findViewById<Button>(R.id.logOutButton)

        val bundle:Bundle? = intent.extras
        val email:String? = bundle?.getString("email")
        val provider:String? = bundle?.getString("provider")
        setUp(email ?: "", provider ?: "")

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()

        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()

        saveButton.setOnClickListener {

            if (email != null) {
                db.collection("usuarios").document(email).set(
                    hashMapOf("provider" to provider,
                        "address" to textViewDireccion.text.toString(),
                        "phone" to textViewTelefono.text.toString())
                    )
            }

        }

        getButton.setOnClickListener {

            if (email != null) {
                db.collection("usuarios").document(email).get().addOnSuccessListener {

                    textViewDireccion.setText(it.get("address") as String?)
                    textViewTelefono.setText(it.get("phone") as String?)

                }
            }

        }

        deleteButton.setOnClickListener {

            if (email != null) {
                db.collection("usuarios").document(email).delete()
            }

        }

    }

    private fun setUp(email: String, provider: String){

        title = "Inicio"
        textViewCorreo.text = email
        textViewProveedor.text = provider

        logOutButton.setOnClickListener {

            val prefs:SharedPreferences.Editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            if(provider == ProviderType.FACEBOOK.name){

                LoginManager.getInstance().logOut()

            }

            FirebaseAuth.getInstance().signOut()
            onBackPressed()

        }

    }

}