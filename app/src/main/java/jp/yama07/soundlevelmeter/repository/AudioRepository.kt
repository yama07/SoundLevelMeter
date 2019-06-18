package jp.yama07.soundlevelmeter.repository

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import kotlin.properties.Delegates

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
  private var audioBufferSizeInByte: Int by Delegates.notNull()
  private var audioBufferSizeInShort: Int by Delegates.notNull()


  private val _frames = MutableLiveData<ShortArray>()
  val frames: LiveData<ShortArray> = _frames

  init {
    setupAudioRecord()
  }

  private fun setupAudioRecord() {
    audioBufferSizeInByte = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat)
    audioBufferSizeInShort = audioBufferSizeInByte / 2

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
        val audioDataArray = readAudioData(recorder)
        Timber.d("ReadSize: ${audioDataArray.size}")
        _frames.postValue(audioDataArray)
      }

      override fun onMarkerReached(recorder: AudioRecord) {}
    })
  }

  private fun readAudioData(record: AudioRecord = audioRecord) = ShortArray(audioBufferSizeInShort).also {
    record.read(it, 0, audioBufferSizeInShort)
  }

  private fun flushAudioData(record: AudioRecord = audioRecord) {
    readAudioData(record)
  }

  fun startRecording() {
    if (audioRecord.state == AudioRecord.STATE_UNINITIALIZED) {
      audioRecord.release()
      setupAudioRecord()
    }

    Timber.d("Start recording")
    audioRecord.startRecording()
  }

  fun stopRecording() {
    Timber.d("Stop recording")
    audioRecord.stop()
    Timber.d("Flush audio data")
    flushAudioData()
  }

  fun release() {
    audioRecord.stop()
    Timber.d("Release record")
    audioRecord.release()
  }

}