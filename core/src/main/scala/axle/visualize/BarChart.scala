package axle.visualize

import collection._

case class BarChart[X, Y](
  bars: SortedMap[X, Y],
  width: Int = 700,
  height: Int = 600,
  border: Int = 50,
  barWidthToGapRation: Double = 0.80,
  title: Option[String] = None,
  xAxis: Y,
  xAxisLabel: Option[String] = None,
  yAxisLabel: Option[String] = None)(implicit _yPlottable: Plottable[Y]) {

  val minY = List(xAxis, (bars.values ++ List(yPlottable.zero())).filter(yPlottable.isPlottable(_)).min(yPlottable)).min(yPlottable)
  val maxY = List(xAxis, (bars.values ++ List(yPlottable.zero())).filter(yPlottable.isPlottable(_)).max(yPlottable)).max(yPlottable)

  val yTics = yPlottable.tics(minY, maxY)

  def yPlottable(): Plottable[Y] = _yPlottable

}
