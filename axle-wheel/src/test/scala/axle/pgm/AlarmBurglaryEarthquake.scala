package axle.stats

import org.scalatest._
import edu.uci.ics.jung.graph.DirectedSparseGraph
// import cats.implicits._
import spire.math._
import spire.implicits._
import axle.pgm._
import axle.jung.directedGraphJung

class ABE extends FunSuite with Matchers {

  val bools = Vector(true, false)

  val B = UnknownDistribution0[Boolean, Rational](bools, "Burglary")
  val E = UnknownDistribution0[Boolean, Rational](bools, "Earthquake")
  val A = UnknownDistribution0[Boolean, Rational](bools, "Alarm")
  val J = UnknownDistribution0[Boolean, Rational](bools, "John Calls")
  val M = UnknownDistribution0[Boolean, Rational](bools, "Mary Calls")

  val bFactor =
    Factor(Vector(B), Map(
      Vector(B is true) -> Rational(1, 1000),
      Vector(B is false) -> Rational(999, 1000)))

  val eFactor =
    Factor(Vector(E), Map(
      Vector(E is true) -> Rational(1, 500),
      Vector(E is false) -> Rational(499, 500)))

  val aFactor =
    Factor(Vector(B, E, A), Map(
      Vector(B is false, E is false, A is true) -> Rational(1, 1000),
      Vector(B is false, E is false, A is false) -> Rational(999, 1000),
      Vector(B is true, E is false, A is true) -> Rational(940, 1000),
      Vector(B is true, E is false, A is false) -> Rational(60, 1000),
      Vector(B is false, E is true, A is true) -> Rational(290, 1000),
      Vector(B is false, E is true, A is false) -> Rational(710, 1000),
      Vector(B is true, E is true, A is true) -> Rational(950, 1000),
      Vector(B is true, E is true, A is false) -> Rational(50, 1000)))

  val jFactor =
    Factor(Vector(A, J), Map(
      Vector(A is true, J is true) -> Rational(9, 10),
      Vector(A is true, J is false) -> Rational(1, 10),
      Vector(A is false, J is true) -> Rational(5, 100),
      Vector(A is false, J is false) -> Rational(95, 100)))

  val mFactor =
    Factor(Vector(A, M), Map(
      Vector(A is true, M is true) -> Rational(7, 10),
      Vector(A is true, M is false) -> Rational(3, 10),
      Vector(A is false, M is true) -> Rational(1, 100),
      Vector(A is false, M is false) -> Rational(99, 100)))

  // edges: ba, ea, aj, am
  val bn: BayesianNetwork[Boolean, Rational, DirectedSparseGraph[BayesianNetworkNode[Boolean, Rational], Edge]] =
    BayesianNetwork.withGraphK2[Boolean, Rational, DirectedSparseGraph](
      "A sounds (due to Burglary or Earthquake) and John or Mary Call",
      Map(B -> bFactor,
        E -> eFactor,
        A -> aFactor,
        J -> jFactor,
        M -> mFactor))

  test("bayesian networks produces a Joint Probability Table, which is '1' when all variables are removed") {

    val jpt = bn.jointProbabilityTable

    val sansAll: Factor[Boolean, Rational] = jpt.Σ(M).Σ(J).Σ(A).Σ(B).Σ(E)

    val abe = (bn.cpt(A) * bn.cpt(B)) * bn.cpt(E)

    val Q: Set[Distribution[Boolean, Rational]] = Set(E, B, A)
    val order = List(J, M)

    // val afterVE = bn.variableEliminationPriorMarginalI(Q, order)
    // val afterVE = bn.variableEliminationPriorMarginalII(Q, order, E is true)
    // bn.getDistributions.map(rv => println(bn.getMarkovAssumptionsFor(rv)))
    // println("P(B) = " + ans1) // 0.001
    // println("P(A| B, -E) = " + ans2) // 0.94
    // println("eliminating variables other than A, B, and E; and then finding those consistent with E = true")
    // println(afterVE)

    sansAll.values(Vector.empty) should be(Rational(1))
    sansAll.evaluate(Seq.empty, Seq.empty) should be(Rational(1))
  }

  test("bayesian network visualization") {

    import axle.awt._

    val pngGName = "gnGraph.png"
    val graphVis = JungDirectedSparseGraphVisualization[BayesianNetworkNode[Boolean, Rational], Edge](
      bn.graph, 200, 200, 10)
    png(graphVis, pngGName)

    //    import axle.HtmlFrom
    //    implicitly[HtmlFrom[BayesianNetworkNode[Boolean, Rational]]]
    //    implicitly[cats.Show[Edge]]
    //    axle.awt.drawJungDirectedSparseGraphVisualization[BayesianNetworkNode[Boolean, Rational], Edge]
    //    val graphDrawer = implicitly[Draw[JungDirectedSparseGraphVisualization[BayesianNetworkNode[Boolean, Rational], Edge]]]
    //    val visDrawer = axle.awt.drawBayesianNetworkVisualization[Boolean, Rational]
    //    visDrawer.component(vis)

    val pngName = "bn.png"
    val vis = BayesianNetworkVisualization[Boolean, Rational](bn, 200, 200, 10)
    png(vis, pngName)

    new java.io.File(pngGName).exists should be(true)
    new java.io.File(pngName).exists should be(true)

  }
}
