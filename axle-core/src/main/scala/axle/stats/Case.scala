package axle.stats

/**
 * TODO
 *
 * Define Case expressions to support full range of case types
 *
 */

case class CaseIs[A](value: A, variable: Variable[A])

object CaseIs {

  import cats.Show

  implicit def showCaseIs[A]: Show[CaseIs[A]] = c => {
    import c._
    val opStr = " = "
    variable.name + opStr + value
  }
}
