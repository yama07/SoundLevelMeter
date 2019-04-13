package jp.yama07.soundlevelmeter.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import jp.yama07.soundlevelmeter.R

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)
  }

  override fun onSupportNavigateUp(): Boolean = findNavController(R.navigation.nav_graph).navigateUp()
}
