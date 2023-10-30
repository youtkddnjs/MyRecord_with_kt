package mhha.sample.myrecord

import android.os.Handler
import android.os.Looper

class Timer(listener: OnTimerListener) {

    private var duration: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            duration += 10L
            handler.postDelayed(this, 50L)
            listener.onTick(duration)
        }

    } //private val runnable: Runnable = object : Runnable

    fun start (){
        handler.postDelayed(runnable,50L)
    }//fun start ()

    fun stop(){
        handler.removeCallbacks(runnable)
        duration = 0
    }//fun stop()

}//class Timer

interface OnTimerListener{
    fun onTick(duration : Long)
}//interface OnTimerListener