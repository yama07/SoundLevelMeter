package jp.yama07.soundlevelmeter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import jp.yama07.soundlevelmeter.repository.AudioRepository
import java.util.*
import kotlin.math.abs
import kotlin.math.log10

class DashboardViewModel(
  private val audioRepository: AudioRepository
) : ViewModel() {

  companion object {
    private const val BASE_VOLUME: Double = 125.0
    private const val VOLUME_FRAME_RATE: Long = 10L
    private const val VOLUME_FRAME_RATE_WINDOW: Int = 8
  }

  private var lastTimestamp: Long = 0L
  private var volumeBuff = ArrayDeque<Double>()
  val volume: LiveData<Double> = MediatorLiveData<Double>().also { mediator ->
    val periodInMillis: Long = 1000 / VOLUME_FRAME_RATE

    mediator.addSource((audioRepository.frames)) { f ->
      val timestamp = System.currentTimeMillis()

      val max = f.map { abs(it.toDouble()) }.max() ?: 0.0
      val volume = 20.0 * log10(max / BASE_VOLUME)

      volumeBuff.push(volume)
      while (volumeBuff.size > VOLUME_FRAME_RATE_WINDOW) {
        volumeBuff.removeLast()
      }

      if ((timestamp - lastTimestamp) >= periodInMillis) {
        val avg = volumeBuff.average()
        mediator.postValue(avg)
        lastTimestamp = timestamp
      }
    }
  }

  private val _frame = MediatorLiveData<Short>().also {
    it.addSource(audioRepository.frames) { f -> f.forEach { v -> it.postValue(v) } }
  }
  val frame: LiveData<Short> = _frame

  fun startRecording() {
    audioRepository.startRecording()
  }

  fun stopRecording() {
    audioRepository.stopRecording()
  }

  override fun onCleared() {
    super.onCleared()
    audioRepository.release()
  }
}