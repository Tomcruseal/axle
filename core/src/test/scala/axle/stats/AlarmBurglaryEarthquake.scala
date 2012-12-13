package axle.stats

import collection._
import axle.stats._
import axle.graph._
import org.specs2.mutable._

class ABE extends Specification {

  import BayesianNetwork._
  
  val bools = Some(Vector(true, false))

  val B = new RandomVariable0("Burglary", bools, None)
  val E = new RandomVariable0("Earthquake", bools, None)
  val A = new RandomVariable0("Alarm", bools, None)
  val J = new RandomVariable0("John Calls", bools, None)
  val M = new RandomVariable0("Mary Calls", bools, None)

  val bn = BayesianNetwork(
    "A sounds (due to Burglary or Earthquake) and John or Mary Call",
    List(BayesianNetworkNode(B,
      Factor(Vector(B), Map(
        List(B eq true) -> 0.001,
        List(B eq false) -> 0.999
      ))),
      BayesianNetworkNode(E,
        Factor(Vector(E), Map(
          List(E eq true) -> 0.002,
          List(E eq false) -> 0.998
        ))),
      BayesianNetworkNode(A,
        Factor(Vector(B, E, A), Map(
          List(B eq false, E eq false, A eq true) -> 0.001,
          List(B eq false, E eq false, A eq false) -> 0.999,
          List(B eq true, E eq false, A eq true) -> 0.94,
          List(B eq true, E eq false, A eq false) -> 0.06,
          List(B eq false, E eq true, A eq true) -> 0.29,
          List(B eq false, E eq true, A eq false) -> 0.71,
          List(B eq true, E eq true, A eq true) -> 0.95,
          List(B eq true, E eq true, A eq false) -> 0.05))),
      BayesianNetworkNode(J,
        Factor(Vector(A, J), Map(
          List(A eq true, J eq true) -> 0.9,
          List(A eq true, J eq false) -> 0.1,
          List(A eq false, J eq true) -> 0.05,
          List(A eq false, J eq false) -> 0.95
        ))),
      BayesianNetworkNode(M,
        Factor(Vector(A, M), Map(
          List(A eq true, M eq true) -> 0.7,
          List(A eq true, M eq false) -> 0.3,
          List(A eq false, M eq true) -> 0.01,
          List(A eq false, M eq false) -> 0.99
        )))),
    (vs: Seq[JungDirectedGraphVertex[BayesianNetworkNode]]) => vs match {
      case b :: e :: a :: j :: m :: Nil => List((b, a, ""), (e, a, ""), (a, j, ""), (a, m, ""))
    })

  // val (bn, es): (BayesianNetwork, Seq[BayesianNetwork#E]) = 

  "bayesian networks" should {
    "work" in {

      val jpt = bn.jointProbabilityTable()

      val sansAll = jpt.Σ(M).Σ(J).Σ(A).Σ(B).Σ(E)

      val abe = (bn.cpt(A) * bn.cpt(B)) * bn.cpt(E)

      val Q: immutable.Set[RandomVariable[_]] = immutable.Set(E, B, A)
      val order = List(J, M)

      // val afterVE = bn.variableEliminationPriorMarginalI(Q, order)
      // val afterVE = bn.variableEliminationPriorMarginalII(Q, order, E eq true)

      // bn.getRandomVariables.map(rv => println(bn.getMarkovAssumptionsFor(rv)))

      // println("P(B) = " + ans1) // 0.001
      // println("P(A| B, -E) = " + ans2) // 0.94

      // println("eliminating variables other than A, B, and E; and then finding those consistent with E = true")
      // println(afterVE)

      1 must be equalTo 1
    }
  }

}
