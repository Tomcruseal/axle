package axle.stats

import axle._
import axle.stats._
import axle.graph._
import axle.pgm._
import spire.math._
import spire.implicits._
import org.specs2.mutable._

class ABE extends Specification {

  import BayesianNetworkModule._
  import FactorModule._

  val bools = Some(Vector(true, false))

  val B = new RandomVariable0[Boolean, Rational]("Burglary", bools, None)
  val E = new RandomVariable0[Boolean, Rational]("Earthquake", bools, None)
  val A = new RandomVariable0[Boolean, Rational]("Alarm", bools, None)
  val J = new RandomVariable0[Boolean, Rational]("John Calls", bools, None)
  val M = new RandomVariable0[Boolean, Rational]("Mary Calls", bools, None)

  val bn = BayesianNetwork[Boolean, Rational](
    "A sounds (due to Burglary or Earthquake) and John or Mary Call",
    List(BayesianNetworkNode[Boolean, Rational](B,
      Factor(Vector(B), Map(
        Vector(B is true) -> Rational(0.001),
        Vector(B is false) -> Rational(0.999)))),
      BayesianNetworkNode(E,
        Factor(Vector(E), Map(
          Vector(E is true) -> Rational(0.002),
          Vector(E is false) -> Rational(0.998)))),
      BayesianNetworkNode(A,
        Factor(Vector(B, E, A), Map(
          Vector(B is false, E is false, A is true) -> Rational(0.001),
          Vector(B is false, E is false, A is false) -> Rational(0.999),
          Vector(B is true, E is false, A is true) -> Rational(0.94),
          Vector(B is true, E is false, A is false) -> Rational(0.06),
          Vector(B is false, E is true, A is true) -> Rational(0.29),
          Vector(B is false, E is true, A is false) -> Rational(0.71),
          Vector(B is true, E is true, A is true) -> Rational(0.95),
          Vector(B is true, E is true, A is false) -> Rational(0.05)))),
      BayesianNetworkNode(J,
        Factor(Vector(A, J), Map(
          Vector(A is true, J is true) -> Rational(0.9),
          Vector(A is true, J is false) -> Rational(0.1),
          Vector(A is false, J is true) -> Rational(0.05),
          Vector(A is false, J is false) -> Rational(0.95)))),
      BayesianNetworkNode(M,
        Factor(Vector(A, M), Map(
          Vector(A is true, M is true) -> Rational(0.7),
          Vector(A is true, M is false) -> Rational(0.3),
          Vector(A is false, M is true) -> Rational(0.01),
          Vector(A is false, M is false) -> Rational(0.99))))),
    (vs: Seq[Vertex[BayesianNetworkNode[Boolean, Rational]]]) => vs match {
      case b :: e :: a :: j :: m :: Nil => List((b, a, ""), (e, a, ""), (a, j, ""), (a, m, ""))
      case _ => Nil
    })

  // val (bn, es): (BayesianNetwork, Seq[BayesianNetwork#E]) =

  "bayesian networks" should {
    "work" in {

      val jpt = bn.jointProbabilityTable

      val sansAll = jpt.Σ(M).Σ(J).Σ(A).Σ(B).Σ(E)

      val abe = (bn.cpt(A) * bn.cpt(B)) * bn.cpt(E)

      val Q: Set[RandomVariable[Boolean, Rational]] = Set(E, B, A)
      val order = List(J, M)

      // val afterVE = bn.variableEliminationPriorMarginalI(Q, order)
      // val afterVE = bn.variableEliminationPriorMarginalII(Q, order, E is true)
      // bn.getRandomVariables.map(rv => println(bn.getMarkovAssumptionsFor(rv)))
      // println("P(B) = " + ans1) // 0.001
      // println("P(A| B, -E) = " + ans2) // 0.94
      // println("eliminating variables other than A, B, and E; and then finding those consistent with E = true")
      // println(afterVE)

      1 must be equalTo 1
    }
  }

}
