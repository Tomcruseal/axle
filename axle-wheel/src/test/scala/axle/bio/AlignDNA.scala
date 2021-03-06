package axle.bio

import org.jblas.DoubleMatrix
import org.scalatest._
import cats.implicits._
import axle.algebra.functorIndexedSeq

class AlignDNA extends FunSuite with Matchers {

  test("Needleman-Wunsch DNA alignment") {

    import NeedlemanWunsch.alignmentScore
    import NeedlemanWunsch.optimalAlignment
    import NeedlemanWunschDefaults._

    implicit val laJblasDouble = {
      import spire.implicits.DoubleAlgebra
      axle.jblas.linearAlgebraDoubleMatrix[Double]
    }

    val dna1 = "ATGCGGCC"
    val dna2 = "ATCGCCGG"

    val nwAlignment =
      optimalAlignment[IndexedSeq, Char, DoubleMatrix, Int, Double](
        dna1, dna2, similarity, gap, gapPenalty)

    val score = alignmentScore(
      nwAlignment._1,
      nwAlignment._2,
      gap,
      similarity,
      gapPenalty)

    val space = NeedlemanWunschMetricSpace[IndexedSeq, Char, DoubleMatrix, Int, Double](
      similarity, gapPenalty)

    nwAlignment should be(("ATGCGGCC--".toIndexedSeq, "AT-C-GCCGG".toIndexedSeq))
    score should be(32d)
    space.distance(dna1, dna2) should be(score)
  }

  test("Smith-Waterman") {

    import SmithWatermanDefaults._
    import SmithWaterman.optimalAlignment

    import spire.algebra._

    implicit val laJblasInt = {
      import spire.implicits.IntAlgebra
      axle.jblas.linearAlgebraDoubleMatrix[Int]
    }

    val dna3 = "ACACACTA"
    val dna4 = "AGCACACA"
    val bestAlignment = ("A-CACACTA".toIndexedSeq, "AGCACAC-A".toIndexedSeq)

    val swAlignment = {
      implicit val ringInt: Ring[Int] = spire.implicits.IntAlgebra
      optimalAlignment[IndexedSeq, Char, DoubleMatrix, Int, Int](
        dna3, dna4, w, mismatchPenalty, gap)
    }

    val space = {
      implicit val ringInt: Ring[Int] = spire.implicits.IntAlgebra
      SmithWatermanMetricSpace[IndexedSeq, Char, DoubleMatrix, Int, Int](w, mismatchPenalty)
    }

    swAlignment should be(bestAlignment)
    space.distance(dna3, dna4) should be(12)
  }

}
