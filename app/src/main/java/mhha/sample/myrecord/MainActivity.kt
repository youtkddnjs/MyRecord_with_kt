package mhha.sample.myrecord

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import mhha.sample.myrecord.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity(), OnTimerListener {

    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 200
    }

    // 릴리스 상태 -> 녹음 중 -> 릴리즈 상태
    // 릴리즈 상태 -> 재생 -> 릴리즈 상태
    private enum class State{
        RELEASE, RECORDING, PLAYING
    }

    private lateinit var timer:Timer
    private lateinit var binding : ActivityMainBinding
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var fileName: String =""
    private var state: State = State.RELEASE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rootPath: String = externalCacheDir?.absolutePath.toString()
        fileName = "$rootPath/audioFile.3gp"

        timer = Timer(this)

        binding.recordButton.setOnClickListener {
            when(state){
                State.RELEASE -> {
                    record()
                }
                State.RECORDING -> {
                    onRecord(false)
                }
                State.PLAYING -> {

                }
            }//while (state)
        } //binding.recordButton.setOnClickListener

        binding.playButton.setOnClickListener {
            when(state){
                State.RELEASE -> {
                    onPlay(true)
                }
                State.RECORDING -> {
                }
                State.PLAYING -> {
                }
            }//while (state)
        }
        binding.playButton.isEnabled = false
        binding.playButton.alpha = 0.3f

        binding.stopButton.setOnClickListener {
            when(state){
                State.RELEASE -> {
                }
                State.RECORDING -> {
                }
                State.PLAYING -> {
                    onPlay(false)
                }
            }//while (state)
        }


    } //override fun onCreate(savedInstanceState: Bundle?)


    /**
     * Record
     * 녹은 시작전 퍼미션 확인
     */
    private fun record(){
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                onRecord(true)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.RECORD_AUDIO) -> {
                showRequestPermissionRationaleDialog()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissions(
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                ) //requestPermissions
            }
        } // when
    }

    /**
     * On record
     *  녹음 시작
     */
    private fun onRecord(start:Boolean) = if(start){startRecord()}else{stopRecord()}

    private fun onPlay(start: Boolean) = if(start){startPlaying()}else{stopPlaying()}

    private fun startRecord(){
        Log.i("MyRecordApp","startRecord()")
        state = State.RECORDING

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            recorder = MediaRecorder(this).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(fileName)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                try {
                    prepare()
                }catch (e:IOException){
                    Log.e("MyRecord_err","prepare() failed : $e")
                }
                start()
            }
        }else {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(fileName)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                try {
                    prepare()
                } catch (e: IOException) {
                    Log.e("MyRecord_err", "prepare() failed : $e")
                }
                start()
            }
        }

        binding.waveformView.clearData()
        timer.start()

        binding.recordButton.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_baseline_stop_24
            )
        ) //binding.recordButton.setImageDrawable

        binding.recordButton.imageTintList = ColorStateList.valueOf(Color.GRAY)
        //binding.recordButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.grey)) 색상 변하는 다른 방법


        // 재생버튼
        binding.playButton.isEnabled = false
        binding.playButton.alpha = 0.3f

    }//private fun startRecord()

    private fun stopRecord(){
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        timer.stop()
        state = State.RELEASE

        binding.recordButton.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_baseline_fiber_manual_record_24
            )
        )
        binding.recordButton.imageTintList = ColorStateList.valueOf(Color.RED)
        binding.playButton.isEnabled = true
        binding.playButton.alpha = 1.0f
    }//private fun stopRecord()

    private fun startPlaying(){
        Log.i("MyRecordApp","startPlaying()")
        state = State.PLAYING

        player = MediaPlayer().apply {

            try {
                setDataSource(fileName)
                prepare()
            }catch (e: IOException){
                Log.e("MyRecord_err","prepare() failed : $e")
            }
            start()
        }

        binding.waveformView.clearWave()
        timer.start()
        // 재생일 끝났을 때 리스너
        player?.setOnCompletionListener {
            stopPlaying()
        }

        binding.recordButton.isEnabled = false
        binding.recordButton.alpha = 0.3f
    }//private fun startPlaying()

    private fun stopPlaying(){
        Log.i("MyRecordApp","stopPlaying()")
        state = State.RELEASE

        player?.release()
        player=null

        timer.stop()

        binding.recordButton.isEnabled=true
        binding.recordButton.alpha=1.0f

    }//private fun stopPlaying()

    /**
     * Show request permission rationale dialog
     * 권한 상세 설명
     */
    private fun showRequestPermissionRationaleDialog(){
        AlertDialog.Builder(this)
            .setTitle("권한 요청")
            .setMessage("녹음 권한이 필요함.")
            .setPositiveButton("허용"){ text ,_ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE)
            }//.setPositiveButton("허용")
            .setNegativeButton("취소"){ text, _ -> text.cancel()}
            .show()
    }//private fun showRequestPermissionRationaleDialog()

    /**
     * Show request permission disallow dialog
     * 권한 거절 시 설정 창으로 이동
     */
    private fun showRequestPermissionDisallowDialog(){
        AlertDialog.Builder(this)
            .setTitle("권한 요청")
            .setMessage("녹음 권한이 반드시 필요함.")
            .setPositiveButton("이동"){ _ ,_ ->
                navigateToAppSetting()
            }//.setPositiveButton("이동")
            .setNegativeButton("취소"){ text, _ -> text.cancel()}
            .show()
    } //private fun showRequestPermissionDisallowDialog()

    /**
     * Navigate to app setting
     * 권한 설정 창으로 이동
     */
    private fun navigateToAppSetting(){
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package",packageName,null)
        }
        startActivity(intent)

    }//private fun navigateToAppSetting()

    // 권한 팝업으로 부터 받은 값 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_CODE
                && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if(audioRecordPermissionGranted){
            onRecord(true)
        }else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "권한이 거부 되었습니다.",Toast.LENGTH_SHORT).show()
            }else{
                showRequestPermissionDisallowDialog()
            }
        }//if(audioRecordPermissionGranted)

    } // override fun onRequestPermissionsResult

    override fun onTick(duration: Long) {

        val millisecond = duration % 1000 / 10
        val second = (duration / 1000) % 60
        val minute = (duration / 1000 / 60)

        binding.timerTextView.text = String.format("%02d:%02d.%02d", minute , second, millisecond)

        if(state == State.PLAYING){
            binding.waveformView.replayAmpliude(duration.toInt())
        }else if (state == State.RECORDING){
            binding.waveformView.addAmplitude(recorder?.maxAmplitude?.toFloat() ?: 0f )
        }


    }//override fun onTick(duration: Long)

} //class MainActivity : AppCompatActivity()