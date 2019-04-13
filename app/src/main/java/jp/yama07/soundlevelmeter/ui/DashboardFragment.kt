package jp.yama07.soundlevelmeter.ui

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.snackbar.Snackbar
import jp.yama07.soundlevelmeter.R
import jp.yama07.soundlevelmeter.databinding.DashboardFragmentBinding
import jp.yama07.soundlevelmeter.ext.observeNonNull
import jp.yama07.soundlevelmeter.ext.showSnackbar
import jp.yama07.soundlevelmeter.viewmodel.DashboardViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import timber.log.Timber

@RuntimePermissions
class DashboardFragment : Fragment() {

  private lateinit var binding: DashboardFragmentBinding
  private val vm: DashboardViewModel by viewModel()
  private val chartData = LineData().also { it.setValueTextColor(Color.WHITE) }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = DataBindingUtil.inflate<DashboardFragmentBinding>(
      inflater,
      R.layout.dashboard_fragment,
      container,
      false
    ).also {
      it.lifecycleOwner = this
      it.vm = vm
      it.handler = View.OnClickListener {
        startRecordWithPermissionCheck()
      }
      it.linechart.also { chart ->
        chart.setTouchEnabled(true)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setDrawGridBackground(false)
        chart.setPinchZoom(false)
        chart.setBackgroundColor(Color.argb(0, 0, 0, 0))
        chart.data = chartData
        chart.axisLeft.also { leftAxis ->
          leftAxis.textColor = Color.WHITE
          leftAxis.axisMaximum = 5000f
          leftAxis.axisMinimum = -5000f
          leftAxis.setDrawGridLines(true)
        }
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.xAxis.isEnabled = false
        chart.legend.isEnabled = false
      }
    }
    chartData.addDataSet(LineDataSet(null, "Microphone").also { set ->
      set.mode = LineDataSet.Mode.CUBIC_BEZIER
      set.cubicIntensity = 0.2f
      set.setDrawCircles(false)
      set.axisDependency = YAxis.AxisDependency.LEFT
      set.color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
      set.lineWidth = 2f
      set.setDrawValues(false)
    })

    subscribe()

    return binding.root
  }

  private fun subscribe() {
    vm.frame.observeNonNull(this) {
      addLineChartEntry(it)
    }
    vm.volume.observeNonNull(this) {
      Timber.d("Volume: $it")
    }
  }

  private fun addLineChartEntry(data: Short) {
    val set = chartData.getDataSetByIndex(0)
    set.addEntry(Entry(set.entryCount.toFloat(), data.toFloat()))
    chartData.notifyDataChanged()

    binding.linechart.also { chart ->
      chart.notifyDataSetChanged()
      chart.setVisibleXRangeMaximum(120f)
      chart.setVisibleXRangeMinimum(120f)

      // move to the latest entry
      chart.moveViewToX(chartData.entryCount.toFloat())
    }
  }


  override fun onResume() {
    super.onResume()
    startRecordWithPermissionCheck()
  }

  override fun onPause() {
    super.onPause()
    stopRecordWithPermissionCheck()
  }

  @NeedsPermission(Manifest.permission.RECORD_AUDIO)
  fun startRecord() {
    vm.start()
  }

  @NeedsPermission(Manifest.permission.RECORD_AUDIO)
  fun stopRecord() {
    vm.stop()
  }

  @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
  fun onRecordAudioDenied() {
    binding.root.showSnackbar(
      getString(R.string.cannot_use_record_audio),
      Snackbar.LENGTH_INDEFINITE
    )
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    onRequestPermissionsResult(requestCode, grantResults)
  }
}