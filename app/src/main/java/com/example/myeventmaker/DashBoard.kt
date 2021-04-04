package com.example.myeventmaker

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.zxing.integration.android.IntentIntegrator
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dash_board.*
import kotlinx.android.synthetic.main.event_row.*
import kotlinx.android.synthetic.main.event_row.view.*
import kotlinx.android.synthetic.main.scanordetaildialogbox.view.*
import java.text.FieldPosition

class DashBoard : AppCompatActivity() {

    lateinit var mRecyView: RecyclerView
    var mAuth = FirebaseAuth.getInstance()
    var eventid:String?= null
    var currentuser = mAuth.currentUser
    var uid = currentuser!!.uid
    lateinit var mDatabase : DatabaseReference
    var queryforshort:Query?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        fabid.setOnClickListener {
            var intent = Intent(this,CreateEvent::class.java)
            startActivity(intent)
        }
        mDatabase = FirebaseDatabase.getInstance().getReference("Admin-Event-view").child(uid).child("Events")
        mRecyView = findViewById(R.id.listView)
        mRecyView.setHasFixedSize(true)
        mRecyView.layoutManager = LinearLayoutManager(this)



        logRecyclerView()


    }

    private fun logRecyclerView(){

        var FirebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Events,EventViewHolder>(


            Events::class.java,R.layout.event_row,EventViewHolder::class.java,mDatabase

        ){
            override fun populateViewHolder(viewHolder: EventViewHolder,model: Events?, position: Int) {
                var eventtile=  viewHolder.mView.event_title.setText(model!!.event_title)
                var eventdate= viewHolder.mView.event_date.setText(model.event_date)
                var eventtime =viewHolder.mView.event_time.setText(model.event_time)
                var eventprice =viewHolder.mView.event_price.setText(model.event_price)
                var eventstate =viewHolder.mView.event_state.setText(model.event_state)
                var eventimage = model!!.event_image

                 // in this we get the id of event
                viewHolder.itemView.setOnClickListener{// it is use for click on card view for some action
                    eventid = getRef(position).key
                    val mDialogView = LayoutInflater.from(this@DashBoard).inflate(R.layout.scanordetaildialogbox
                    ,null)
                    val mBuilder = AlertDialog.Builder(this@DashBoard)
                        .setTitle("What you want to do:")
                        .setView(mDialogView)





                    val mAlertDialog = mBuilder.show()

                    mDialogView.button1.setOnClickListener {
                        val scanner = IntentIntegrator(this@DashBoard)

                        scanner.initiateScan()
                    }
                    mDialogView.button2.setOnClickListener {
                        var intent = Intent(this@DashBoard,Eventinfo::class.java)
                        intent.putExtra("eventkey",eventid)
                        intent.putExtra("eventtile",model.event_title)
                        intent.putExtra("eventimage",eventimage.toString())
                        intent.putExtra("eventprice",model.event_price)
                        intent.putExtra("eventtime",model.event_time)
                        intent.putExtra("eventdate",model.event_date)
                        intent.putExtra("eventstate",model.event_state)
                        startActivity(intent)
                        //Toast.makeText(this@DashBoard,"btn2", Toast.LENGTH_SHORT).show()

                    }


                   // Toast.makeText(this@DashBoard,"$eventid", Toast.LENGTH_SHORT).show() // this show event id on click


                }




                Glide.with(this@DashBoard)
                    .load(model!!.event_image)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(10)))
                    .into(viewHolder.mView.event_image)



            }

//            override fun getItem(position: Int): Events {
//                return super.getItem(itemCount-1-position)
//            }



        }


        mRecyView.adapter = FirebaseRecyclerAdapter
    }

    override fun onActivityResult(requestCode:Int,resultCode: Int, data: Intent?) {
        if (resultCode== Activity.RESULT_OK){
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    var qrcode = result.contents
                   var eventstringid= eventid.toString()
                    var DatabaseReference= FirebaseDatabase.getInstance().getReference("Tickets").child("$eventstringid").orderByKey().equalTo(qrcode)
                    DatabaseReference.addListenerForSingleValueEvent( object :ValueEventListener{

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()){
                                var dataref =FirebaseDatabase.getInstance().getReference()
                                dataref.addValueEventListener(object :ValueEventListener{
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                                        var ticketqtys =dataSnapshot.child("Tickets").child(eventstringid).child(qrcode).child(eventstringid).child("ticket_quantity").value.toString()

                                        val mDialogView = LayoutInflater.from(this@DashBoard).inflate(R.layout.rightticketdialogbox // dialog box for right ticket
                                            ,null)
                                         val qutny = mDialogView.findViewById<TextView>(R.id.ticketqty)
                                        qutny.setText("Number of Tickets: $ticketqtys")
                                        val mBuilder = AlertDialog.Builder(this@DashBoard)
                                            .setView(mDialogView)
                                        val mAlertDialog = mBuilder.show()


                                    }

                                    override fun onCancelled(p0: DatabaseError) {
                                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    }
                                })


                                //Toast.makeText(this@DashBoard, "ticket is avalaible", Toast.LENGTH_LONG).show()



                            }
                            else{
                                //Toast.makeText(this@DashBoard, "ticket is not avalaible", Toast.LENGTH_LONG).show()

                                val mDialogVie = LayoutInflater.from(this@DashBoard).inflate(R.layout.wrongticketdialogbox
                                    ,null) // dialog box for wrong ticket
                                val mBuilde = AlertDialog.Builder(this@DashBoard)
                                    .setView(mDialogVie)
                                val mAlertDialo = mBuilde.show()

                            }

                        }
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }



                    })
                   // Toast.makeText(this@DashBoard,"$eventid", Toast.LENGTH_SHORT).show() //result.contents
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    class EventViewHolder(var mView: View) : RecyclerView.ViewHolder(mView){

    }

}
