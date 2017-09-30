package axle.visualize

import scala.collection.immutable.TreeMap
import scala.annotation.implicitNotFound

import axle.algebra.Plottable
import axle.algebra.Zero
import axle.stats.Probability
import cats.kernel.Order
import cats.implicits._
import cats.Order.catsKernelOrderingForOrder

@implicitNotFound("Witness not found for PlotDataView[${X}, ${Y}, ${D}]")
trait PlotDataView[S, X, Y, D] {

  def xsOf(d: D): Traversable[X]

  def valueOf(d: D, x: X): Y

  def xRange(data: Seq[(S, D)], include: Option[X]): (X, X)

  def yRange(data: Seq[(S, D)], include: Option[Y]): (Y, Y)
}

object PlotDataView {

  final def apply[S, X, Y, D](implicit ev: PlotDataView[S, X, Y, D]) = ev

  implicit def treeMapDataView[S, X: Order: Zero: Plottable, Y: Order: Zero: Plottable]: PlotDataView[S, X, Y, TreeMap[X, Y]] =
    new PlotDataView[S, X, Y, TreeMap[X, Y]] {

      def xsOf(d: TreeMap[X, Y]): Traversable[X] = d.keys

      def valueOf(d: TreeMap[X, Y], x: X): Y = d.apply(x)

      def xRange(data: Seq[(S, TreeMap[X, Y])], include: Option[X]): (X, X) = {

        val minXCandidates = include.toList ++ (data flatMap {
          case (label, d: TreeMap[X, Y]) => xsOf(d).headOption
        })
        val minX = if (minXCandidates.size > 0) minXCandidates.min else Zero[X].zero

        val maxXCandidates = include.toList ++ (data flatMap {
          case (label, d: TreeMap[X, Y]) => xsOf(d).lastOption
        })

        val maxX = if (minXCandidates.size > 0) maxXCandidates.max else Zero[X].zero

        (minX, maxX)

      }

      def yRange(data: Seq[(S, TreeMap[X, Y])], include: Option[Y]): (Y, Y) = {

        val minYCandidates = include.toList ++ (data flatMap {
          case (label, d: TreeMap[X, Y]) =>
            val xs = xsOf(d)
            if (xs.size === 0)
              None
            else
              Some(xs map { valueOf(d, _) } min)
        }) filter { Plottable[Y].isPlottable _ }

        val minY = if (minYCandidates.size > 0) minYCandidates.min else Zero[Y].zero

        val maxYCandidates = include.toList ++ (data flatMap {
          case (label, d: TreeMap[X, Y]) => {
            val xs = xsOf(d)
            if (xs.size === 0)
              None
            else
              Some(xs map { valueOf(d, _) } max)
          }
        }) filter { Plottable[Y].isPlottable _ }

        val maxY = if (minYCandidates.size > 0) maxYCandidates.max else Zero[Y].zero

        (minY, maxY)
      }
    }

  implicit def probabilityDataView[S, X: Order: Zero: Plottable, Y: Order: Zero: Plottable, D](
      implicit prob: Probability[D, X, Y]): PlotDataView[S, X, Y, D] =
    new PlotDataView[S, X, Y, D] {

      def xsOf(d: D): Traversable[X] = prob.values(d)

      def valueOf(d: D, x: X): Y = prob(d, x)

      def xRange(data: Seq[(S, D)], include: Option[X]): (X, X) = {

        val minXCandidates = include.toList ++ (data flatMap {
          case (label, d) => xsOf(d).headOption
        })
        val minX = if (minXCandidates.size > 0) minXCandidates.min else Zero[X].zero

        val maxXCandidates = include.toList ++ (data flatMap {
          case (label, d) => xsOf(d).lastOption
        })

        val maxX = if (minXCandidates.size > 0) maxXCandidates.max else Zero[X].zero

        (minX, maxX)
      }

      def yRange(data: Seq[(S, D)], include: Option[Y]): (Y, Y) = {

        val minYCandidates = include.toList ++ (data flatMap {
          case (label, d) =>
            val xs = xsOf(d)
            if (xs.size === 0)
              None
            else
              Some(xs map { valueOf(d, _) } min)
        }) filter { Plottable[Y].isPlottable _ }

        val minY = if (minYCandidates.size > 0) minYCandidates.min else Zero[Y].zero

        val maxYCandidates = include.toList ++ (data flatMap {
          case (label, d) => {
            val xs = xsOf(d)
            if (xs.size === 0)
              None
            else
              Some(xs map { valueOf(d, _) } max)
          }
        }) filter { Plottable[Y].isPlottable _ }

        val maxY = if (minYCandidates.size > 0) maxYCandidates.max else Zero[Y].zero

        (minY, maxY)
      }
    }

}
