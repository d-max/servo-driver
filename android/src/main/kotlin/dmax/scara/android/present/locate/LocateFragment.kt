package dmax.scara.android.present.locate

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import dmax.scara.android.R
import dmax.scara.android.present.common.LocateView
import dmax.scara.android.present.locate.LocateContract.Event
import dmax.scara.android.present.locate.LocateContract.Model
import dmax.scara.android.present.misc.ViewMeasure
import dmax.scara.android.present.misc.component1
import dmax.scara.android.present.misc.component2
import dmax.scara.android.present.misc.observe
import dmax.scara.android.present.misc.toPoint
import dmax.scara.android.present.misc.view
import org.koin.android.viewmodel.ext.android.viewModel

class LocateFragment : Fragment() {

    private val model: Model by viewModel()
    private val locateView: LocateView by view(R.id.locate)
    private val coordinatesView: AppCompatTextView by view(R.id.coordinates)
    private lateinit var measure: ViewMeasure

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_locate, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        measure = ViewMeasure(requireActivity())
        locateView.listener = this::locate

        observe(model.data) { data ->
            (data as? LocateContract.Data.Position)?.let {
                val x = it.x.toFloat()
                val y = it.y.toFloat()
                locateView.point = PointF(x, y)
                coordinatesView.showCoordinates(x, y)
            }
        }
        model.event(Event.OnInit)
    }

    private fun locate(point: PointF) {
        val (x, y) = point.toPoint()
        // invert axises
        val xPixels = y
        val yPixels = x
        val xMm = measure.widthInMm(yPixels)
        val yMm = measure.heightInMm(xPixels)

        coordinatesView.showCoordinates(xMm, yMm)
        model.event(Event.OnLocate(xMm.toInt(), yMm.toInt()))
    }
    
    private fun AppCompatTextView.showCoordinates(xCm: Float, yCm: Float) {
        text = requireContext().getString(
            R.string.coordinates,
            "%.2f".format(xCm),
            "%.2f".format(yCm)
        )
    }
}
