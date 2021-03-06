package axle.algebra

import scala.annotation.implicitNotFound

/**
 *
 * Vec is the Vector typeclass
 */

@implicitNotFound("Witness not found for Vec[${V}]")
trait Vec[V[_]] {

  def from[N](v: V[N]): N

  def to[N](v: V[N]): N
}

object Vec {

  final def apply[V[_]: Vec]: Vec[V] = implicitly[Vec[V]]

  type TV[N] = (N, N)

  implicit def tuple2Vec: Vec[TV] =
    new Vec[TV] {

      def from[N](v: (N, N)): N = v._1

      def to[N](v: (N, N)): N = v._2
    }

}
