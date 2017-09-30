package axle.stats

import axle.IndexedCrossProduct
import axle.algebra.LinearAlgebra
import axle.string
import cats.Show
import cats.implicits.catsKernelStdOrderForString
import cats.implicits.catsSyntaxEq
import cats.kernel.Eq
import cats.kernel.Order
import cats.Order.catsKernelOrderingForOrder

import spire.algebra.Field
import spire.algebra.MultiplicativeMonoid
import spire.implicits.RingProduct2
import spire.implicits.convertableOps
import spire.implicits.multiplicativeGroupOps
import spire.implicits.multiplicativeSemigroupOps
import spire.math.ConvertableFrom

/* Technically a "Distribution" is probably a table that sums to 1, which is not
   * always true in a Factor.  They should be siblings rather than parent/child.
   */

object Factor {

  implicit def showFactor[T: Show, N: Show]: Show[Factor[T, N]] =
    new Show[Factor[T, N]] {

      def show(factor: Factor[T, N]): String = {
        import factor._
        varList.map(d => d.name.padTo(d.charWidth, " ").mkString("")).mkString(" ") + "\n" +
          factor.cases.map(kase =>
            kase.map(ci => string(ci.value).padTo(ci.distribution.charWidth, " ").mkString("")).mkString(" ") +
              " " + string(factor(kase))).mkString("\n") // Note: was "%f".format() prior to spire.math
      }

    }

  implicit def factorEq[T: Eq, N]: Eq[Factor[T, N]] =
    new Eq[Factor[T, N]] {
      def eqv(x: Factor[T, N], y: Factor[T, N]): Boolean = x equals y // TODO
    }

  implicit def factorMultMonoid[T: Eq, N: Field: ConvertableFrom: Order]: MultiplicativeMonoid[Factor[T, N]] =
    new MultiplicativeMonoid[Factor[T, N]] {

      val field = Field[N]

      def times(x: Factor[T, N], y: Factor[T, N]): Factor[T, N] = {
        val newVars = (x.variables.toSet union y.variables.toSet).toVector
        Factor(newVars, Factor.cases(newVars).map(kase => (kase, x(kase) * y(kase))).toMap)
      }
      def one: Factor[T, N] = Factor(Vector.empty, Map.empty.withDefaultValue(field.one))
    }

  def cases[T: Eq, N: Field](varSeq: Vector[Variable[T]]): Iterable[Vector[CaseIs[T]]] =
    IndexedCrossProduct(varSeq.map(_.values)) map { kase =>
      varSeq.zip(kase) map {
        case (rv, v) => CaseIs(rv, v)
      } toVector
    }

}

case class Factor[T: Eq, N: Field: Order: ConvertableFrom](
    val varList: Vector[Variable[T]],
    val values: Map[Vector[CaseIs[T]], N]) {

  val field = Field[N]

  lazy val crossProduct = IndexedCrossProduct(varList.map(_.values))

  lazy val elements: IndexedSeq[N] =
    (0 until crossProduct.size) map { i =>
      values.get(caseOf(i)).getOrElse(field.zero)
    } toIndexedSeq

  def variables: Vector[Variable[T]] = varList

  // assume prior and condition are disjoint, and that they are
  // each compatible with this table

  def evaluate(prior: Seq[CaseIs[T]], condition: Seq[CaseIs[T]]): N = {
    val pw = spire.optional.unicode.Σ(cases.map(c => {
      if (isSupersetOf(c, prior)) {
        if (isSupersetOf(c, condition)) {
          (this(c), this(c))
        } else {
          (this(c), field.zero)
        }
      } else {
        (field.zero, field.zero)
      }
    }).toVector)

    pw._1 / pw._2
  }

  def indexOf(cs: Seq[CaseIs[T]]): Int = {
    val rvvs: Seq[(Variable[T], T)] = cs.map(ci => (ci.distribution, ci.value))
    val rvvm = rvvs.toMap
    crossProduct.indexOf(varList.map(rvvm))
  }

  private[this] def caseOf(i: Int): Vector[CaseIs[T]] =
    varList.zip(crossProduct(i)) map { case (variable, value) => CaseIs(variable, value) }

  def cases: Iterable[Seq[CaseIs[T]]] = (0 until elements.length) map { caseOf }

  def apply(c: Seq[CaseIs[T]]): N = elements(indexOf(c))

  // Chapter 6 definition 6
  def maxOut(variable: Variable[T]): Factor[T, N] = {
    val newVars = variables.filterNot(variable === _)
    Factor(newVars,
      Factor.cases(newVars)
        .map(kase => (kase, variable.values.map(value => this(kase)).max))
        .toMap)
  }

  def projectToOnly(remainingVars: Vector[Variable[T]]): Factor[T, N] =
    Factor(remainingVars,
      Factor.cases[T, N](remainingVars).toVector
        .map(kase => (projectToVars(kase, remainingVars.toSet), this(kase)))
        .groupBy(_._1)
        .map({ case (k, v) => (k.toVector, spire.optional.unicode.Σ(v.map(_._2))) })
        .toMap)

  def tally[M](
    a: Variable[T],
    b: Variable[T])(
      implicit la: LinearAlgebra[M, Int, Int, Double]): M =
    la.matrix(
      a.values.size,
      b.values.size,
      (r: Int, c: Int) => spire.optional.unicode.Σ(cases.filter(isSupersetOf(_, Vector(a is a.values(r), b is b.values(c)))).map(this(_)).toVector).toDouble)

  def Σ(varToSumOut: Variable[T]): Factor[T, N] = this.sumOut(varToSumOut)

  // depending on assumptions, this may not be the best way to remove the vars
  def sumOut(gone: Variable[T]): Factor[T, N] = {
    val position = varList.indexOf(gone)
    val newVars = varList.filter(v => !(v === gone))
    Factor(
      newVars,
      Factor.cases(newVars).map(kase => {
        val reals = gone.values.map(gv => {
          val ciGone = List(CaseIs(gv, gone))
          this(kase.slice(0, position) ++ ciGone ++ kase.slice(position, kase.length))
        })
        (kase, spire.optional.unicode.Σ(reals))
      }).toMap)
  }

  def Σ(varsToSumOut: Set[Variable[T]]): Factor[T, N] = sumOut(varsToSumOut)

  def sumOut(varsToSumOut: Set[Variable[T]]): Factor[T, N] =
    varsToSumOut.foldLeft(this)((result, v) => result.sumOut(v))

  // as defined on chapter 6 page 15
  def projectRowsConsistentWith(eOpt: Option[List[CaseIs[T]]]): Factor[T, N] = {
    val e = eOpt.get
    Factor(variables,
      Factor.cases(e.map(_.distribution).toVector).map(kase => (kase, if (isSupersetOf(kase, e)) this(kase) else field.zero)).toMap)
  }

  def mentions(variable: Variable[T]): Boolean =
    variables.exists(v => variable.name === v.name)

  def isSupersetOf(left: Seq[CaseIs[T]], right: Seq[CaseIs[T]]): Boolean = {
    val ll: Seq[(Variable[T], T)] = left.map(ci => (ci.distribution, ci.v))
    val lm = ll.toMap
    right.forall((rightCaseIs: CaseIs[T]) => lm.contains(rightCaseIs.distribution) && (rightCaseIs.v === lm(rightCaseIs.distribution)))
  }

  def projectToVars(cs: Seq[CaseIs[T]], pVars: Set[Variable[T]]): Seq[CaseIs[T]] =
    cs.filter(ci => pVars.contains(ci.distribution))

}
