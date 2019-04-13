package jp.yama07.soundlevelmeter.ext

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, observer: (T) -> Unit) =
  observe(owner, Observer {
    if (it != null) observer.invoke(it)
  })