package axle.algebra

import spire.algebra._
import spire.implicits._
import org.specs2.mutable._
import org.scalacheck._
import Arbitrary._
import org.typelevel.discipline.specs2.mutable.Discipline
import org.scalacheck.Arbitrary
import axle.algebra.laws._

class FunctorLawsSpec
    extends Specification
    with Discipline {

  implicit def eqF1AB[A: Arbitrary, B: Eq]: Eq[A => B] =
    new Eq[A => B] {
      val arbA = implicitly[Arbitrary[A]]
      // TODO: Is this available in ScalaCheck?
      def eqv(f: A => B, g: A => B): Boolean = {
        (1 to 10) forall { i =>
          val a = arbA.arbitrary.sample.get // TODO when does sample return None?
          f(a) === g(a)
        }
      }
    }

  checkAll("List[Int]", FunctorLaws[List[Int], Int].functorIdentity)
  checkAll("List[String]", FunctorLaws[List[String], String].functorIdentity)
  checkAll("Option[Int]", FunctorLaws[Option[Int], Int].functorIdentity)
  checkAll("List[String]", FunctorLaws[List[String], String].functorIdentity)
  checkAll("Function1[Int, Int]", FunctorLaws[Int => Int, Int].functorIdentity)
  //({ type λ[α] = Int => α })#λ

  checkAll("List[Int]", FunctorLaws[List[Int], Int].functorComposition[Int, Int, List[Int], List[Int]])
  checkAll("List[String]", FunctorLaws[List[String], String].functorComposition[String, String, List[String], List[String]])
  checkAll("Option[Int]", FunctorLaws[Option[Int], Int].functorComposition[Int, Int, Option[Int], Option[Int]])
  checkAll("List[String]", FunctorLaws[List[String], String].functorComposition[String, String, List[String], List[String]])
  checkAll("Function1[Int, Int]", FunctorLaws[Int => Int, Int].functorComposition[Int, Int, Int => Int, Int => Int])

}
