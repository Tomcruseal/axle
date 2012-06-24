package axle.ast

import scala.util.matching.Regex
import axle.Loggable

class Bar {}

object Foo extends Loggable {

  def match_and_transform(string: String, regex: Regex, transform: (String, Regex.Match) => String): String = {
    var last_end = -1
    var result: String = ""
    for (md <- regex.findAllIn(string).matchData) {
      if (last_end == -1) {
        result += string.substring(0, md.start)
      } else {
        result += string.substring(last_end, md.start)
      }
      result += transform(string, md)
      last_end = md.end
    }
    if (last_end < string.length) {
      result += string.substring(last_end, string.length)
    }
    result
  }

  def main(args: Array[String]): Unit = {

    val s = "bc fidaa abmn fidabb nuaaabi fidjab bkjkj"

    def transform(s: String, m: Regex.Match): String = {
      "$" + s.substring(m.start(1), m.end(1))
    }

    val x = match_and_transform(s, new Regex("fid(\\w+)"), transform)
    info(s)
    info(x)

    /*
     var t = List[String]("a", "b", "c");
     var u = t ::: List[String]("d", "e");
     info("u = " + u);
     
     var z = (x: Int) => x + 1
     
     //var abc = (x: List[String]): Int => { x.length }
     
     var score = new Function1[List[String], Int] {
     def apply(ss: List[String]): Int = ss.length
     }
     */

    //(ss: List[String]): Int => ss.length
    // (for (s <- ss) yield s.length()).foldLeft(0)(_+_);

    /*
     val element: String = "abc"
     val lines = element.split("\n")
     val x = lines.last
     */

    /*
     val y: String = "abc"
     val a: Int = y.indexOf("bc")
     info("a = " + a)
     */

    /*
     var list: List[String] = List[String]()
     list += "A"
     list += "B"
     
     info("list(1) = " + list(1))
     
     var m: Map[String, Int] = Map[String, Int]()
     m += "X" -> 5
     m += "Y" -> 8
     
     info("m = " + m);
     
     for ( i <- m ) {
     info("k = " + i._1 + " m[k] = " + i._2)
     }
     */

    // val indentation_level: Int = 5;
    // val x: String = ( List.range(0, indentation_level) map (i => "| -") ).mkString("");
    // val y: String = ( for ( x <- List.range(0, indentation_level)) yield "   " ).mkString("")
    // var tokens: ListBuffer[String] = ListBuffer[String]();
    // tokens += "efoo"
    // tokens += "ebar"

    // var node2lineno: Map[String, Int] = Map[String, Int]();
    // node2lineno.update("x", 5);

    // var s : String = String.format("foo %s", "abc")
    // info("s = " + s)
  }
}