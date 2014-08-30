package axle.quanta2

import org.specs2.mutable._
import spire.implicits.additiveGroupOps
import spire.implicits.additiveSemigroupOps
import spire.implicits.moduleOps
import spire.math.Rational

import Distance._
import Time._

class Quanta2Spec extends Specification {

  "Scalar conversion" should {
    "work" in {

      val d1 = Rational(3, 4) *: meter[Rational]
      val d2 = Rational(7, 2) *: meter[Rational]
      val t1 = Rational(4) *: second[Rational]
      val t2 = Rational(9, 88) *: second[Rational]
      val t3 = Rational(5d) *: second[Rational]
      val t4 = 10 *: second[Rational]

      val d3 = d1 + d2
      val d4 = d2 - d2
      //val d5 = d2 + t2 // shouldn't compile
      val t5 = t2 in minute[Rational]
      val t6 = t1 :* Rational(5, 2)
      val t8 = Rational(5, 3) *: t1
      val t9 = t1 :* 60

      // TODO: show other number types, N

      1 must be equalTo 1
    }
  }

  "Scalar conversion" should {
    "work" in {

      import Mass._
      import Distance._
      import spire.implicits.DoubleAlgebra 

      (5 *: gram[Double]).magnitude must be equalTo 5
      (1 *: parsec[Double] + 4 *: lightyear[Double]).magnitude must be equalTo 7.260
      (4 *: lightyear[Double] + 1 *: parsec[Double]).magnitude must be equalTo 2.226993865030675 // TODO what precision do I want here?
    }
  }

  "Quanta conversion" should {

    "work" in {

      import Distance._
      import Mass._
      import spire.implicits.DoubleAlgebra 

      (kilogram[Double] in gram[Double]).magnitude must be equalTo 1000d // TODO precision
      (megagram[Double] in milligram[Double]).magnitude must be equalTo 1000000000d // TODO precision
      (mile[Double] in ft[Double]).magnitude must be equalTo 5280d // TODO precision

    }

    "use Rational" in {
      import Volume._
      ((Rational(24) *: wineBottle) in nebuchadnezzar).magnitude must be equalTo Rational(6, 5)
    }
  }

  "addition" should {
    "work" in {

      import Mass._
      import Distance._
      import spire.implicits.DoubleAlgebra 

      // Shouldn't compile: gram + mile
      // Shouldn't compile: gram + kilogram + mile + gram
      (meter[Double] + foot[Double]).magnitude must be equalTo 4.2808398950131235 // TODO what precision do I want here?
      (gram[Double] + kilogram[Double]).magnitude must be equalTo 1.001
    }
  }

  "over" should {
    "work" in {

      import Volume._
      import Flow._

      greatLakes.over[Flow, Time, Rational](niagaraFalls).magnitude must be equalTo Rational(1) // TODO convert that to years
    }
  }

}
