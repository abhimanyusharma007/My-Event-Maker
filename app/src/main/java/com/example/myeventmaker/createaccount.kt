package com.example.myeventmaker

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_createaccount.*

class createaccount : AppCompatActivity() {
    var mAuth: FirebaseAuth? = null
    var mDatabase: DatabaseReference?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createaccount)



        mAuth= FirebaseAuth.getInstance()
        createbtn.setOnClickListener {
            var firstname = firstname.text.toString().trim()
            var lastname = lastname.text.toString().trim()
            var email = useremail.text.toString().trim()
            var phoneno = userphone.text.toString().trim()
            var password = userpassword.text.toString().trim()

            if (TextUtils.isEmpty(firstname)||TextUtils.isEmpty(lastname)|| TextUtils.isEmpty(email)|| TextUtils.isEmpty(phoneno) || TextUtils.isEmpty(password)){
                Toast.makeText(this,"Please fill out the Fields", Toast.LENGTH_LONG)
                    .show()
            }
            else{

                createAccount(email,password,firstname,lastname,phoneno)
            }
        }
    }

    fun createAccount(email:String,password:String,firstname:String,lastname:String,phoneno:String){

        mAuth!!.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                    task: Task<AuthResult> ->

                if(task.isSuccessful){
                    var currentUser = mAuth!!.currentUser
                    var userId = currentUser!!.uid
                    mDatabase = FirebaseDatabase.getInstance().reference
                        .child("EventAdmins").child(userId)

                    var userObject = HashMap<String,String>()
                    userObject.put("First name",firstname)
                    userObject.put("Last name",lastname)
                    userObject.put("Phone number",phoneno)
                    userObject.put("User email",email)



                    mDatabase!!.setValue(userObject).addOnCompleteListener {
                            task: Task<Void> ->
                        if(task.isSuccessful){

                            Toast.makeText(this,"User Created", Toast.LENGTH_LONG)
                                .show()

                        }else{
                            Toast.makeText(this,"User Not Created", Toast.LENGTH_LONG)
                                .show()

                        }
                    }
                }else{

                }
            }

    }
}


