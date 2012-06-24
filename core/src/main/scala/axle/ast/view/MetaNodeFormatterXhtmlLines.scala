package axle.ast.view

import scala.xml.{ NodeSeq, Text }
import scala.collection._
import axle.ast._

class XhtmlLinesMetaNodeAccumulatorState(xlf: XhtmlLinesMetaNodeFormatter) {

  var currentLine = new mutable.ListBuffer[scala.xml.Node]()

  var lines = Map[Int, NodeSeq]()

  def getLines(): Map[Int, NodeSeq] = {
    if (currentLine != null) {
      lines += xlf.currentLineNo -> currentLine.toList
      currentLine = null // effectively marking this as "finished".
      // need better way of handling subsequent writes if they happen
    }
    lines
  }

  def raw(s: String): Unit = currentLine.append(Text(s))

  def newline(): Unit = {
    val line = currentLine.toList
    lines += xlf.currentLineNo -> line
    xlf.advanceLine()
    currentLine = new mutable.ListBuffer[scala.xml.Node]()
  }

  def space(): Unit = currentLine.append(Text(" "))

  def spaces(): Unit = currentLine.append(<span>&nbsp;&nbsp;&nbsp;</span>) // TODO

  // scala.xml.Utility.escape(word)
  def span(spanclass: String, s: String): Unit = currentLine += <span class={ spanclass }>{ s }</span>

  def absorb(label: String, absorbee: XhtmlLinesMetaNodeAccumulatorState): Unit = {

    for ((lineno, line) <- absorbee.lines) {
      if (currentLine.size > 0) {
        val unfinishedLine: NodeSeq = currentLine.toList
        lines += lineno -> <span>{ unfinishedLine }</span><span class={ label }>{ line }</span>;
        currentLine = new mutable.ListBuffer[scala.xml.Node]();
      } else {
        lines += lineno -> <span class={ label }>{ line }</span>
      }
    }
    if (absorbee.currentLine != null) {
      currentLine = absorbee.currentLine
      // popped.lines += currentLineNo -> popped.currentLine.toList
      // popped.currentLine = null
    }
  }
}

class XhtmlLinesMetaNodeFormatter(language: Language, highlight: Set[MetaNode], conform: Boolean)
  extends MetaNodeFormatter[Map[Int, NodeSeq], mutable.Stack[XhtmlLinesMetaNodeAccumulatorState]](language, highlight, conform) {

  def result(): Map[Int, NodeSeq] = {
    if (tokens.size > 1) {
      throw new Exception(
        "called resultLines on XhtmlLinesFormatter with " +
          "an XhtmlLinesAccumulator that has a stack with more than one entry")
    }
    tokens.top.lines
  }

  var currentLineNo = 1
  def advanceLine(): Unit = currentLineNo += 1

  override var tokens = new mutable.Stack[XhtmlLinesMetaNodeAccumulatorState]()

  tokens.push(new XhtmlLinesMetaNodeAccumulatorState(this))

  def getLines: Map[Int, NodeSeq] = tokens.top.getLines

  override def toString() = "XhtmlLinesAccumulator.toString not implemented"

  // override def append(t: String) { tokens += t }

  // delegate to the top of the stack for all of these

  override def conformTo(node: MetaNode): Unit = {
    if (isConforming()) {
      while (node.getLineNo > lineno) {
        // info("conforming.  formatter.lineno = " + formatter.lineno)
        newline(true, node)
      }
    }
  }

  override def newline(hard: Boolean, node: MetaNode, indent: Boolean = true): Unit = {
    if (node != null) {
      column = 0
      needs_indent = indent
      lineno += 1
      // result.appendAll(<br></br>)
      accNewline()
    }
  }

  override def accRaw(s: String): Unit = tokens.top.raw(s)

  override def accNewline(): Unit = tokens.top.newline()

  override def accSpace(): Unit = tokens.top.space()

  override def accSpaces(): Unit = tokens.top.spaces()

  override def accSpan(spanclass: String, s: String) = tokens.top.span(spanclass, s)

  override def accPushStack(): Unit = tokens.push(new XhtmlLinesMetaNodeAccumulatorState(this))

  // TODO: assert stack.size > 1
  override def accPopAndWrapStack(label: String) = tokens.top.absorb(label, tokens.pop)

}