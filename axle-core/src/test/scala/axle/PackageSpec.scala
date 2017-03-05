package axle

import org.scalatest._

class PackageSpec extends FunSuite with Matchers {

  test("find gaps in List[Int]") {
    import spire.implicits._
    gaps(List(1, 2, 6, 7, 8, 9, 10, 16)) should be(List((3, 5), (11, 15)))
  }

  test("find runs in List[Int]") {
    import spire.implicits._
    gaps(List(1, 2, 6, 7, 8, 9, 10, 16)) should be(List((1, 2), (6, 10), (16, 16)))
  }

  test("monte carlo pi be at least 2.9") {
    import spire.implicits._
    monteCarloPiEstimate(
      (1 to 1000).toList,
      (n: Int) => n.toDouble) should be > 2.9
  }

  test("Wallis pi > 3d") {
    wallisΠ(100).toDouble should be > 3d
  }

  test("fibonacci iteratively arrive at fib(7) == 21") {
    assertResult(fib(7))(21)
  }

  test("fibonacci recursively") {
    assertResult(recfib(7))(21)
  }

  test("ackermann(2, 2) == ") {
    assertResult(ackermann(2, 2))(7)
  }

  test("mandelbrot at 1.8 1.7") {
    import spire.implicits.DoubleAlgebra
    // import cats.implicits._
    assertResult(inMandelbrotSetAt(4d, 1.8, 1.7, 100).get)(0)
  }

  test("intersperse") {
    assertResult(intersperse(7)((11 to 13).toList))(List(11, 7, 12, 7, 13))
  }
}
