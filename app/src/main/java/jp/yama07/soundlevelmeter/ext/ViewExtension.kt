package jp.yama07.soundlevelmeter.ext

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(text: CharSequence, duration: Int) {
  Snackbar.make(this, text, duration).show()
}