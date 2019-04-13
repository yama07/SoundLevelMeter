package jp.yama07.soundlevelmeter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import jp.yama07.soundlevelmeter.repository.AudioRepository
import kotlin.math.log10

class DashboardViewModel(
  private val audioRepository: AudioRepository
) : ViewModel() {

  companion object {
    private const val BASE_VOLUME: Double = 12.0
  }

  val volume: LiveData<Double> = Transformations.map(audioRepository.frames) { f ->
    val max = f.max() ?: 0
    return@map 20.0 * log10(max.toDouble() / BASE_VOLUME)
  }

  val prettyVolume: LiveData<String> = Transformations.map(volume) { v -> "%.0f dB".format(v) }

  private val _frame = MediatorLiveData<Short>().also {
    it.addSource(audioRepository.frames) { f -> f.forEach { v -> it.postValue(v) } }
  }
  val frame: LiveData<Short> = _frame

  fun start() {
    audioRepository.startRecording()
  }

  fun stop() {
    audioRepository.stopRecording()
  }

  fun release() {
    audioRepository.release()
  }
}