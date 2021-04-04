package com.example.myeventmaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_eventinfo.*

class Eventinfo : AppCompatActivity() {
    var mDatabase: DatabaseReference? =null
    var eventaddress:Any?= null
    var eventcity:Any?= null
    var eventstates:Any?= null
    var eventpin:Any?= null
    var eventdesc:Any?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eventinfo)
        var eventdata = intent.extras
        var eventid = eventdata!!.get("eventkey").toString()
        var eventtitle = eventdata.get("eventtile")
        var eventimage = eventdata.get("eventimage")
        var eventprice = eventdata.get("eventprice")
        var eventdate= eventdata.get("eventdate")
        var eventtime = eventdata.get("eventtime")
        var eventstate=eventdata.get("eventstate")
        var mAuth = FirebaseAuth.getInstance()
        var currentuser = mAuth.currentUser
        var uid = currentuser!!.uid
        var databaseReference = FirebaseDatabase.getInstance().getReference()


        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                eventaddress = dataSnapshot.child("Events").child(eventid).child("event_address").value
                eventcity = dataSnapshot.child("Events").child(eventid).child("event_city").value
                eventstates = dataSnapshot.child("Events").child(eventid).child("event_state").value
                eventpin = dataSnapshot.child("Events").child(eventid).child("event_pin").value
                eventdesc = dataSnapshot.child("Events").child(eventid).child("event_des").value


                adresstextView.setText("$eventaddress")
                citytextView.setText("$eventcity")
                statetext.setText(" $eventstates ")
                pintextView.setText("$eventpin")
                destextView.setText("$eventdesc")




            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }


        })




        Glide.with(this)
            .load(eventimage)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
            .into(eventimageView)


        textView.setText("$eventtitle")
        eventpriceid.setText("₹$eventprice")
        datetextView.setText("Event Date: $eventdate")
        timetextView.setText("Event Time: $eventtime")
        statetextview.setText("State: $eventstate")
    }
}
