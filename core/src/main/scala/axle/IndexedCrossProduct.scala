package axle

import collection._

object IndexedCrossProduct {

  def apply[E](lists: Seq[IndexedSeq[E]]): IndexedCrossProduct[E] = new IndexedCrossProduct[E](lists)
}

class IndexedCrossProduct[E](lists: Seq[IndexedSeq[E]]) extends Iterable[Seq[E]] {

  val mults = lists.reverse.map(_.size).scanLeft(1)(_ * _).reverse

  val syze = mults.head

  val modulos = mults.tail

  def indexOf(objects: Seq[E]): Int = {
    val mults = lists.zip(objects).map(lo => lo._1.indexOf(lo._2)).zip(modulos).map(im => im._1 * im._2)
    if (mults.∀(_ >= 0)) {
      mults.sum
    } else {
      -1
    }
  }

  def apply(i: Int): Seq[E] =
    lists.zip(modulos).foldLeft((i, List[E]()))(
      (cr, lm) => (cr._1 % lm._2, lm._1(cr._1 / lm._2) :: cr._2)
    )._2.reverse

  override def size() = syze

  def iterator() = (0.until(size)).iterator.map(this(_))

}
