package jp.yama07.soundlevelmeter.di

import jp.yama07.soundlevelmeter.repository.AudioRepository
import org.koin.dsl.module.module

val repositoryModule = module {
  single { AudioRepository() }
}