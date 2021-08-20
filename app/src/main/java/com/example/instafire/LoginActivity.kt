package com.example.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.instafire.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

private const val TAG= "LoginActivity"
class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth= FirebaseAuth.getInstance()
        if (auth.currentUser!=null){
            goToPostActivity()
        }

        binding.login.setOnClickListener{

            val email= binding.email.text.toString()
            val password=binding.password.text.toString()
            if (email.isBlank() || password.isBlank()){
                Toast.makeText(this, "Please fill the empty fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.login.isEnabled=false


            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                binding.login.isEnabled=true
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    goToPostActivity()
                }
                else{
                    Log.i(TAG,"Login attempt failed", task.exception)
                    Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun goToPostActivity() {
        Log.i(TAG,"Redirecting the user to PostActivity")
        val intent= Intent(this,PostActivity::class.java)
        startActivity(intent)
        finish()
    }
}