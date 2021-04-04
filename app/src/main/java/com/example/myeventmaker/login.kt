package com.example.myeventmaker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class login : AppCompatActivity() {
    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        createaccountid.setOnClickListener {
            var intent = Intent(this,createaccount::class.java)
            startActivity(intent)}

        mAuth = FirebaseAuth.getInstance()
        loginbtn.setOnClickListener {

            var email =emailtxt.text.toString().trim()
            var password =  passwordtxt.text.toString().trim()
            if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                loginUser(email,password)

            }else{
                Toast.makeText(this,"Please fill out the Fields", Toast.LENGTH_LONG)
                    .show()
            }

        }



    }

    private fun loginUser(email:String, password:String){

        mAuth!!.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                    task: Task<AuthResult> ->
                if (task.isSuccessful){
                    var dashboard = Intent(this,DashBoard::class.java)
                    startActivity(dashboard)

                }else{
                    Toast.makeText(this,"login failed", Toast.LENGTH_LONG)
                        .show()

                }
            }

    }
}


