package axle.data

import axle.nlp._
import axle._
import spire.math._
import spire.algebra._

/**
 *
 * http://www.gutenberg.org/files/18/18.txt
 *
 */

object FederalistPapers {

  val idPattern = """FEDERALIST.? No. (\d+)""".r

  val allCaps = """[A-Z ]+""".r

  case class FederalistPaper(id: Int, author: String, text: String, metadata: String)

  implicit val fpEq = new Eq[FederalistPaper] {
    def eqv(x: FederalistPaper, y: FederalistPaper): Boolean = x equals y
  }

  def parseArticles(filename: String): List[FederalistPaper] = {

    val lines = io.Source.fromFile(filename).getLines.toList

    val starts = lines.zipWithIndex.filter({ case (line, i) => line.startsWith("FEDERALIST") }).map(_._2)

    val last = lines.zipWithIndex.find(_._1.startsWith("End of the Project Gutenberg EBook")).get._2 - 1

    val ranges = starts.zip(starts.drop(1) ++ Vector(last))

    ranges map {
      case (first, last) =>
        val id = idPattern.findFirstMatchIn(lines(first)).map(_.group(1).toInt).getOrElse(0)
        val authorIndex = ((first + 1) to last).find(i => allCaps.unapplySeq(lines(i)).isDefined).getOrElse(0)
        val metadata = ((first + 1) to authorIndex - 1).map(lines).mkString("\n")
        val text = ((authorIndex + 1) to last).map(lines).mkString("\n")
        FederalistPaper(id, lines(authorIndex), text, metadata)
    }
  }

}
