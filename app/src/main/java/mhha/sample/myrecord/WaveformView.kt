package mhha.sample.myrecord

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.time.temporal.TemporalAmount
import kotlin.math.max

class WaveformView @JvmOverloads constructor( // 여러가지 생성자가 자바에서 보이도록
    context : Context,
    attrs : AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val ampList = mutableListOf<Float>()
    private val rectList = mutableListOf<RectF>()
    private val rectWidth =  12f
    private var tick = 0

    //val rectF = RectF(20f, 30f, 20f + 30f, 30f + 60f )
    private val redPaint = Paint().apply {
        color = Color.RED
    }//val redPaint = Paint().apply

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for(rectF in rectList){
            canvas?.drawRect(rectF,redPaint)
        }
    }//override fun onDraw(canvas: Canvas)

    fun addAmplitude(maxAmount: Float){

        val amplitude = (maxAmount / Short.MAX_VALUE) * this.height * 0.8f

        ampList.add(amplitude)
        rectList.clear()

        val rectWidth = 10f
        val maxRect = (this.width / rectWidth).toInt()

        val amps = ampList.takeLast(maxRect)

        for ((i,amp) in amps.withIndex()){
            val rectF = RectF()
            rectF.top = (this.height / 2f) - (amp / 2 ) -3f //중간값
            rectF.bottom = rectF.top + amp + 3f
            rectF.left = i * rectWidth
            rectF.right = rectF.left + rectWidth - 2f

            rectList.add(rectF)
        }

        invalidate() // 초기화 함수
    }

    fun replayAmpliude(duration:Int){
        rectList.clear()

        val maxRect = (this.width / rectWidth)
        val amps = ampList.take(tick).takeLast(maxRect.toInt())

        for ((i,amp) in amps.withIndex()){
            val rectF = RectF()
            rectF.top = (this.height / 2f) - (amp / 2 ) - 3f
            rectF.bottom = rectF.top + amp + 3f
            rectF.left = i * rectWidth
            rectF.right = rectF.left + rectWidth - 2f

            rectList.add(rectF)
        }

        tick++

        invalidate() //초기화 함수
    }//fun replayAmpliude()

    fun clearWave(){
        rectList.clear()
        tick = 0
        invalidate()
    }//fun clearWave()

    fun clearData(){
        ampList.clear()
    }//learData()

} //class WaveformView(