package jp.yama07.soundlevelmeter.repository

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

class AudioRepository(
  val samplingRate: Int = DEFAULT_SAMPLING_RATE,
  val channelConfig: Int = DEFAULT_CHANNEL_CONFIG,
  val audioFormat: Int = DEFAULT_AUDIO_FORMAT
) {

  companion object {
    const val DEFAULT_SAMPLING_RATE: Int = 44100
    const val DEFAULT_CHANNEL_CONFIG: Int = AudioFormat.CHANNEL_IN_MONO
    const val DEFAULT_AUDIO_FORMAT: Int = AudioFormat.ENCODING_PCM_16BIT
  }

  private lateinit var audioRecord: AudioRecord

  private val _frames = MutableLiveData<ShortArray>()
  val frames: LiveData<ShortArray> = _frames

  init {
    setupAudioRecord()
  }

  private fun setupAudioRecord() {
    val audioBufferSizeInByte =
      AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat)
    val audioBufferSizeInShort = audioBufferSizeInByte / 2

    audioRecord = AudioRecord(
      MediaRecorder.AudioSource.MIC,
      samplingRate,
      channelConfig,
      audioFormat,
      audioBufferSizeInByte
    )

    audioRecord.positionNotificationPeriod = audioBufferSizeInShort

    audioRecord.setRecordPositionUpdateListener(object :
      AudioRecord.OnRecordPositionUpdateListener {

      override fun onPeriodicNotification(recorder: AudioRecord) {
        val audioDataArray = ShortArray(audioBufferSizeInShort)
        recorder.read(audioDataArray, 0, audioBufferSizeInShort)
        Timber.d("ReadSize: ${audioDataArray.size}")
        _frames.postValue(audioDataArray)
      }

      override fun onMarkerReached(recorder: AudioRecord) {}
    })
  }

  fun startRecording() {
    if (audioRecord.state == AudioRecord.STATE_UNINITIALIZED) {
      audioRecord.release()
      setupAudioRecord()
    }

    if (audioRecord.recordingState == AudioRecord.RECORDSTATE_STOPPED) {
      Timber.d("startRecording")
      audioRecord.startRecording()
    }
  }

  fun stopRecording() {
    if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
      Timber.d("stopRecording")
      audioRecord.stop()
    }
  }

  fun release() {
    stopRecording()
    audioRecord.release()
  }

}