package jp.yama07.soundlevelmeter.di

import jp.yama07.soundlevelmeter.viewmodel.DashboardViewModel
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val viewModelModule = module {
  viewModel { DashboardViewModel(get()) }
}