package com.example.myeventmaker

class Events() {

    var event_title:String?=null
    var event_image:String?=null
    var event_state:String?=null
    var event_time:String?=null
    var event_date:String?=null
    var event_price:String?=null


    constructor(event_title:String,event_image:String,
                event_state:String,event_time:String,event_date:String,event_price:String
    ):this() {
        this.event_title=event_title
        this.event_image=event_image
        this.event_state=event_state
        this.event_time=event_time
        this.event_date =event_date
        this.event_price=event_price


    }
}