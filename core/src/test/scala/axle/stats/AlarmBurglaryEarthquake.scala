package axle.stats.examples

import collection._
import axle.stats._
import axle.visualize._
import axle.graph.JungDirectedGraphFactory._
import org.specs2.mutable._
import scalaz._
import Scalaz._

class ABE extends Specification {

  val bools = Some(Vector(true, false))

  val B = new RandomVariable0("Burglary", bools, None)
  val E = new RandomVariable0("Earthquake", bools, None)
  val A = new RandomVariable0("A", bools, None)
  val J = new RandomVariable0("John Calls", bools, None)
  val M = new RandomVariable0("Mary Calls", bools, None)

  val bn = BayesianNetwork("A sounds (due to Burglary or Earthquake) and John or Mary Call")

  val bv = bn += BayesianNetworkNode(B,
    Factor(Vector(B), Some(Map(
      List(B eq true) -> 0.001,
      List(B eq false) -> 0.999
    ))))

  val ev = bn += BayesianNetworkNode(E,
    Factor(Vector(E), Some(Map(
      List(E eq true) -> 0.002,
      List(E eq false) -> 0.998
    ))))

  val av = bn += BayesianNetworkNode(A,
    Factor(Vector(B, E, A), Some(Map(
      List(B eq false, E eq false, A eq true) -> 0.001,
      List(B eq false, E eq false, A eq false) -> 0.999,
      List(B eq true, E eq false, A eq true) -> 0.94,
      List(B eq true, E eq false, A eq false) -> 0.06,
      List(B eq false, E eq true, A eq true) -> 0.29,
      List(B eq false, E eq true, A eq false) -> 0.71,
      List(B eq true, E eq true, A eq true) -> 0.95,
      List(B eq true, E eq true, A eq false) -> 0.05))))

  val jv = bn += BayesianNetworkNode(J,
    Factor(Vector(A, J), Some(Map(
      List(A eq true, J eq true) -> 0.9,
      List(A eq true, J eq false) -> 0.1,
      List(A eq false, J eq true) -> 0.05,
      List(A eq false, J eq false) -> 0.95
    ))))

  val mv = bn += BayesianNetworkNode(M,
    Factor(Vector(A, M), Some(Map(
      List(A eq true, M eq true) -> 0.7,
      List(A eq true, M eq false) -> 0.3,
      List(A eq false, M eq true) -> 0.01,
      List(A eq false, M eq false) -> 0.99
    ))))

  bn += (bv -> av, "")
  bn += (ev -> av, "")
  bn += (av -> jv, "")
  bn += (av -> mv, "")

  val jpt = bn.getJointProbabilityTable()

  val sansAll = jpt.Σ(M).Σ(J).Σ(A).Σ(B).Σ(E)

  val ab = bn.getCPT(A) * bn.getCPT(B)

  val abe = ab * bn.getCPT(E)

  val Q: immutable.Set[RandomVariable[_]] = immutable.Set(E, B, A)
  val order = List(J, M)

  // val afterVE = bn.variableEliminationPriorMarginalI(Q, order)

  val afterVE = bn.variableEliminationPriorMarginalII(Q, order, E eq true)

  "bayesian networks" should {
    "work" in {

      // bn.getRandomVariables.map(rv => println(bn.getMarkovAssumptionsFor(rv)))

      // println("P(B) = " + ans1) // 0.001
      // println("P(A| B, -E) = " + ans2) // 0.94

      println("eliminating variables other than A, B, and E; and then finding those consistent with E = true")
      println(afterVE)

      1 must be equalTo 1
    }
  }

}
