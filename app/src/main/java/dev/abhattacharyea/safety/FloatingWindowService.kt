package dev.abhattacharyea.safety

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import kotlin.math.abs
import kotlin.math.roundToInt

class FloatingWindowService: Service() {

    lateinit var windowManager: WindowManager
    lateinit var view: View
    private var activity_background = true
    var width: Int = 0
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SERVICE", "start")
        intent?.let {
            activity_background = intent.getBooleanExtra("activity_background", false)
        }
        if(!::view.isInitialized) {
            view = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
            val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

            params.gravity = Gravity.TOP or  Gravity.LEFT
            params.x = 0
            params.y = 100

            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.addView(view, params)

            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)

            val fab = view.findViewById<FloatingActionButton>(R.id.fabHead)

            val layout = view.findViewById<RelativeLayout>(R.id.layout)
            val vto = layout.viewTreeObserver

            vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val w = layout.measuredWidth
                    width = size.x - w

                }
            })

            fab.setOnTouchListener(object :View.OnTouchListener {
                var initialX = 0
                var initialY = 0
                var initialTouchX = 0.0F
                var initialTouchY = 0.0F
                override fun onTouch(v: View?, event: MotionEvent): Boolean {


                    when(event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y

                            initialTouchX = event.rawX
                            initialTouchY = event.rawY

                            return true
                        }

                        MotionEvent.ACTION_UP -> {
                            if(activity_background) {
                                val xdiff = event.rawX - initialTouchX
                                val ydiff = event.rawY - initialTouchY

                                if(abs(xdiff) < 5 && abs(ydiff) < 5) {
                                    startActivity(intentFor<MainActivity>().newTask())
                                    stopSelf()
                                }
                            }

                            val middle = width / 2
                            val nearestWall = if(params.x >= middle) width else 0
                            params.x = nearestWall

                            windowManager.updateViewLayout(view, params)
                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val xdiff = (event.rawX - initialTouchX).roundToInt()
                            val ydiff = (event.rawY - initialTouchY).roundToInt()

                            params.x = initialX + xdiff
                            params.y = initialY + ydiff

                            windowManager.updateViewLayout(view, params)
                            return true
                        }

                        else -> {
                            return false
                        }
                    }
                }
            })
        }


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        setTheme(R.style.AppTheme)

    }

    override fun onDestroy() {
        super.onDestroy()

        if(::view.isInitialized) {
            windowManager.removeViewImmediate(view)
        }
    }
}