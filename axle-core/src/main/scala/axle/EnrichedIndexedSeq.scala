package axle

import scala.collection.IndexedSeq

import spire.random.Generator
import spire.implicits.IntAlgebra
import spire.implicits.eqOps

case class EnrichedIndexedSeq[T: Manifest](is: IndexedSeq[T]) {

  def apply(range: Range): IndexedSeq[T] = {
    assert(range.step === 1)
    if (range.isEmpty) {
      List[T]().toIndexedSeq
    } else {
      is.slice(range.start, range.last + 1)
    }
  }

  def swap(i0: Int, i1: Int): IndexedSeq[T] =
    is.zipWithIndex.map({
      case (v, i) =>
        if (i === i0) is(i1) else (if (i === i1) is(i0) else v)
    })

  def random(gen: Generator): T = is(gen.nextInt(is.size))

  def powerset: IndexedPowerSet[T] = IndexedPowerSet(is)

  def ℘(): IndexedPowerSet[T] = IndexedPowerSet(is)

  def permutations(r: Int): PermutationsFast[T] = PermutationsFast(is, r)

  def combinations(r: Int): CombinationsFast[T] = CombinationsFast(is, r)
}
