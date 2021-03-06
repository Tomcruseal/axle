package axle.ml

import spire.math.min
import spire.algebra.Field
import spire.implicits.additiveSemigroupOps
import spire.implicits.multiplicativeGroupOps
import axle.algebra.Finite
import axle.math._

object RankedClassifierPerformance {

  /**
   * http://en.wikipedia.org/wiki/Information_retrieval#Average_precision
   *
   */

  def averagePrecisionAtK[T, N](actual: Seq[T], predicted: Seq[T], k: Int)(implicit field: Field[N]): N = {

    if (actual.size == 0) {
      field.zero
    } else {

      val cutOff = predicted.take(k)

      val score: N =
        Σ[N, Seq](cutOff
          .zipWithIndex
          .filter({ case (p, i) => actual.contains(p) && (!cutOff.take(i).contains(p)) })
          .zipWithIndex
          .map({ case ((_, i), h) => (field.fromInt(h) + field.one) / (field.fromInt(i) + field.one) }))

      score / min(actual.size, k)
    }
  }

  /**
   *
   * https://www.kaggle.com/wiki/MeanAveragePrecision
   *
   * http://www.kaggle.com/c/FacebookRecruiting/forums/t/2002/alternate-explanation-of-mean-average-precision
   *
   */

  // TODO F[_]: Functor: Aggregatable: Zipper
  def meanAveragePrecisionAtK[T, N: Field](
    actual:    Seq[Seq[T]],
    predicted: Seq[Seq[T]],
    k:         Int         = 10)(implicit finite: Finite[Seq, N]): N =
    mean(
      actual.zip(predicted)
        .map({ case (a: Seq[T], p: Seq[T]) => averagePrecisionAtK[T, N](a, p, k) }))

}
