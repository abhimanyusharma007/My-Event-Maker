package com.example.myeventmaker

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_create_event.*
import kotlinx.android.synthetic.main.activity_login.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.time.Month
import java.util.*
import java.util.UUID.*
import java.util.jar.Manifest
import kotlin.math.log
import android.content.Context as context
import android.util.Log.d as d1


class CreateEvent : AppCompatActivity() {
    var mAuth: FirebaseAuth? = null
    var mDatabase: DatabaseReference? =null
    var mStorageRef:StorageReference?=null
    var galleryid:Int = 1
    var mImageUri: Uri? = null
    var eventimage:ByteArray? =null
    var countevents:Long?=null



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        mStorageRef = FirebaseStorage.getInstance().reference


        selectimg.setOnClickListener {

            var galleryIntent = Intent()  // this function open gallery of the phone
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(galleryIntent,"SELECT_IMAGE"),galleryid)
        }



        eventdate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
             val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug",
                 "Sep","Oct","Nov","Dec")
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener {_,year,monthOfYear, dayOfMonth ->


                eventdate.setText(""+ dayOfMonth +" "+months[monthOfYear] +" "+year)
            }, year, month, day)
            dpd.show()
        }

        eventtime.setOnClickListener {
            val cal =  Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener{_,hour,minute ->
                cal.set(Calendar.HOUR_OF_DAY,hour)
                cal.set(Calendar.MINUTE,minute)

                eventtime.text = SimpleDateFormat("hh:mm a").format(cal.time)
            }
            TimePickerDialog(this,timeSetListener,cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),
                false).show()

        }

        createeventbtn.setOnClickListener {

             val progressDialog = ProgressDialog(this)
              progressDialog.setMessage(" Please Wait")
               progressDialog.setCancelable(false)

            var eventtitle= eventtitle.text.toString().trim()
            var eventprice= eventprice.text.toString().trim()
            var eventdates = eventdate.text.toString().trim()
            var eventtimes = eventtime.text.toString().trim()
            var eventaddress= eventaddress.text.toString().trim()
            var eventstate = eventstate.text.toString().trim()
            var eventcity = eventcity.text.toString().trim()
            var eventpin = pin.text.toString().trim()
            var eventdes = des.text.toString().trim()


            if (TextUtils.isEmpty(eventtitle)|| TextUtils.isEmpty(eventprice)|| TextUtils.isEmpty(eventdates)||
                TextUtils.isEmpty(eventtimes) || TextUtils.isEmpty(eventaddress)|| TextUtils.isEmpty(eventstate)
                ||TextUtils.isEmpty(eventcity)|| TextUtils.isEmpty(eventpin)|| TextUtils.isEmpty(eventdes) ||
                mImageUri == null){

                Toast.makeText(this,"Please fill out the Fields", Toast.LENGTH_LONG)
                    .show()
            }
            else{// create event function below
                  //val timestamp = System.currentTimeMillis().toString()
                 progressDialog.show()
                var database = FirebaseDatabase.getInstance().reference.child("Events")
                database.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()){
                             countevents = dataSnapshot.childrenCount
                        }
                        else{
                             countevents= 0
                        }

                    }

                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })



                var filepath = mStorageRef!!.child("Event_images/"+randomUUID().toString())//this line is for upload image in firebase storage and always send random name of image using randomUUID
                filepath.putBytes(eventimage!!).addOnSuccessListener() { taskSnapshot ->// it for getting link of image
                    filepath.downloadUrl.addOnCompleteListener{taskSnapshot->//download url get the link

                        var url = taskSnapshot.result
                        Log.d("Dricect link","$url")


                        mDatabase = FirebaseDatabase.getInstance().reference
                            .child("Events").push()
                        var eventkey = mDatabase!!.key.toString()// get uid of new create event
                        var currentuid = FirebaseAuth.getInstance().currentUser!!.uid //get uid for-
                        // -current admin who upload the events


                        var userObject = HashMap<String,String>()
                       // userObject.put("timestamp",timestamp)
                        userObject.put("event_image",url.toString())
                        userObject.put("event_title",eventtitle)
                        userObject.put("event_price",eventprice)
                        userObject.put("event_time",eventtimes)
                        userObject.put("event_date",eventdates)
                        userObject.put("event_address",eventaddress)
                        userObject.put("event_state",eventstate)
                        userObject.put("event_city",eventcity)
                        userObject.put("event_pin",eventpin)
                        userObject.put("event_des",eventdes)
                        userObject.put("event_count",countevents.toString())



                        mDatabase!!.setValue(userObject).addOnCompleteListener {
                                task: Task<Void> ->
                            if(task.isSuccessful){
                                FirebaseDatabase.getInstance().reference // it create separate node of admin view
                                    .child("Admin-Event-view").child(currentuid)//after event is successfully create
                                    .child("Events").child(eventkey).setValue(userObject)// in Events node

                                Toast.makeText(this,"Event is Created", Toast.LENGTH_LONG)
                                    .show()

                                 var intent = Intent(this,DashBoard::class.java)
                                  startActivity(intent)
                                 progressDialog.dismiss()


                            }else{
                                Toast.makeText(this,"Event is not Created", Toast.LENGTH_LONG)
                                    .show()

                            }

                        }
                    }
                }






            }




        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int,data: Intent?) { // getting image in mImageUri ..
        mStorageRef = FirebaseStorage.getInstance().reference                        // .. variable
        if (requestCode== galleryid && resultCode== Activity.RESULT_OK) {
            Log.d("my life", "photo is selected")
            mImageUri = data!!.data // selected image in this variable

            CropImage.activity(mImageUri)
                .setAspectRatio(2, 3)
                .start(this)
        }


            if (requestCode===CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                val result = CropImage.getActivityResult(data)

                if (resultCode===Activity.RESULT_OK){
                    val resultUri = result.uri
                    var image = File(resultUri.path)
                    selectimg.setImageURI(resultUri)

                    var imagescompress = Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(65)
                        .compressToBitmap(image)

                    var byteArray = ByteArrayOutputStream()
                    imagescompress.compress(Bitmap.CompressFormat.JPEG,100,byteArray)
                    eventimage= byteArray.toByteArray()


                }
            }

    }


}
