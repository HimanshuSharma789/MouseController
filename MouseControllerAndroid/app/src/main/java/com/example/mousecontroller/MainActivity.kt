package com.example.mousecontroller

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    lateinit var leftClickBtn: Button
    lateinit var rightClickBtn: Button
    lateinit var saveBtn: Button
    lateinit var textView: TextView
    lateinit var touchView: View
    lateinit var ipaddressTv: TextView
    lateinit var sensitivityTv: TextView
    lateinit var sharedPref : SharedPreferences
    private var address = "192.168.1.6:5000"
    private var sensitivity = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        leftClickBtn = findViewById(R.id.leftClickBtn)
        rightClickBtn = findViewById(R.id.rightClickBtn)
        saveBtn = findViewById(R.id.saveBtn)
        textView = findViewById(R.id.textView)
        touchView = findViewById(R.id.touchView)
        ipaddressTv = findViewById(R.id.ipaddressTv)
        sensitivityTv = findViewById(R.id.sensitivityTv)

        sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        address = sharedPref.getString("address", address).toString()
        sensitivity = sharedPref.getInt("sensi", sensitivity)
        sensitivityTv.text = sensitivity.toString()
        ipaddressTv.text = address

        saveBtn.setOnClickListener { saveSettings() }
        leftClickBtn.setOnClickListener { makeRequest(1, null, null) }
        rightClickBtn.setOnClickListener { makeRequest(2, null, null) }

        var mVelocityTracker:VelocityTracker? = null;

        touchView.setOnTouchListener(View.OnTouchListener { v: View, event ->
//            val x = event.x
//            val y = event.y
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
//                    Log.d("TAG", "ACTION_DOWN \nx: $x\ny: $y")
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker?.clear()
                    // If necessary retrieve a new VelocityTracker object to watch the
                    // velocity of a motion.
                    mVelocityTracker = mVelocityTracker ?: VelocityTracker.obtain()
                    // Add a user's movement to the tracker.
                    mVelocityTracker?.addMovement(event)
                }
                MotionEvent.ACTION_MOVE -> {
//                    Log.d("TAG", "ACTION_MOVE \nx: $x\ny: $y")
//                    Log.d("TAG", "ACTION_MOVE \n")
                    mVelocityTracker?.apply {
                        val pointerId: Int = event.getPointerId(event.actionIndex)
                        addMovement(event)
                        // When you want to determine the velocity, call
                        // computeCurrentVelocity(). Then call getXVelocity()
                        // and getYVelocity() to retrieve the velocity for each pointer ID.
                        computeCurrentVelocity(sensitivity*10)
                        // Log velocity of pixels per second
                        // Best practice to use VelocityTrackerCompat where possible.
                        Log.d("TAG", "X velocity: ${getXVelocity(pointerId)}")
                        Log.d("TAG", "Y velocity: ${getYVelocity(pointerId)}")
                        makeRequest(3, getXVelocity(pointerId), getYVelocity(pointerId))
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    Log.d("TAG", "touched up");
//                    Log.d("TAG", "touched up \\nx: $x\\ny: $y");
                    // Return a VelocityTracker object back to be re-used by others.
                    mVelocityTracker?.recycle()
                    mVelocityTracker = null
                }
            }
            v.performClick()
            return@OnTouchListener  true
        })

    }

    private fun saveSettings() {
        val ipadd = ipaddressTv.text.toString()
        var sensi = sensitivityTv.text.toString().toInt()
        if(sensi > 5 || sensi < 1) { sensi = 2 }
        if(ipadd.isNotEmpty()) {
            val shared = sharedPref.edit()
            shared.putString("address", ipadd).apply()
            shared.putInt("sensi", sensi).apply()
            shared.commit()
            address = ipadd
            sensitivity = sensi
        }
    }

    private fun makeRequest(value: Int, xVelocity: Float?, yVelocity: Float?) {
        val url = "http://$address/click"
        val jsonData = JSONObject()
        jsonData.put("value", value)
        if(value == 3) {
            jsonData.put("xVelocity" , xVelocity)
            jsonData.put("yVelocity" , yVelocity)
        }
        val jsonObjectRequest = object: JsonObjectRequest(Method.POST, url, jsonData,
                {response ->
                    textView.text = "Response: %s".format(response.toString())
                }, {
            textView.text = it.toString()
        }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                header["Content-Type"] = "application/json"
                return super.getHeaders()
            }
        }
        jsonObjectRequest.setShouldCache(false)
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
//                TimeUnit.SECONDS.toMillis(0).toInt(), //After the set time elapses the request will timeout
                0,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }


}