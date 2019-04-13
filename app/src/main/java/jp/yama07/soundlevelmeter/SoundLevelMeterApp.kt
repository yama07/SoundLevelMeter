package jp.yama07.soundlevelmeter

import android.app.Application
import jp.yama07.soundlevelmeter.di.repositoryModule
import jp.yama07.soundlevelmeter.di.viewModelModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber

@Suppress("unused")
class SoundLevelMeterApp : Application() {
  override fun onCreate() {
    super.onCreate()

    Timber.plant(Timber.DebugTree())
    startKoin(this, listOf(repositoryModule, viewModelModule))
  }
}