package org.pingel.axle.quanta

import org.pingel.axle.graph._
import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP

/**
 * Quantum
 *
 * Used in the sense of the World English Dictionary's 4th definition:
 *
 * 4. something that can be quantified or measured
 *
 * [[http://dictionary.reference.com/browse/quantum]]
 *
 */

trait Quantum extends DirectedGraph {

  outer =>

  type UOM <: UnitOfMeasurement

  implicit def toBD(i: Int) = new BigDecimal(i.toString)

  implicit def toBD(d: Double) = new BigDecimal(d.toString)
  
  implicit def toBD(s: String) = new BigDecimal(s)

  case class Conversion(from: UOM, to: UOM, bd: BigDecimal) extends DirectedGraphEdge {

    def getVertices() = (from, to)
    def getSource() = from
    def getDest() = to
    def getBD() = bd

    override def toString() = from.toString() + " -> " + bd + " -> " + to.toString()
  }

  val one = new BigDecimal("1")

  class UnitOfMeasurement(
    var conversion: Option[Conversion],
    name: Option[String] = None,
    symbol: Option[String] = None,
    link: Option[String] = None)
    extends DirectedGraphVertex {

    self: UOM =>

    def getLabel() = name
    def getSymbol() = symbol
    def getLink() = link

    val quantum: Quantum = outer

    def kilo() = quantity("1000", this, Some("kilo" + name.getOrElse("")), Some("K" + symbol.getOrElse(""))) // 3
    def mega() = quantity("1000", kilo, Some("mega" + name.getOrElse("")), Some("M" + symbol.getOrElse(""))) // 6
    def giga() = quantity("1000", mega, Some("giga" + name.getOrElse("")), Some("G" + symbol.getOrElse(""))) // 9
    def tera() = quantity("1000", giga, Some("kilo" + name.getOrElse("")), Some("T" + symbol.getOrElse(""))) // 12
    def peta() = quantity("1000", tera, Some("peta" + name.getOrElse("")), Some("P" + symbol.getOrElse(""))) // 15
    def exa() = quantity("1000", peta, Some("exa" + name.getOrElse("")), Some("E" + symbol.getOrElse(""))) // 18
    def zetta() = quantity("1000", exa, Some("zetta" + name.getOrElse("")), Some("Z" + symbol.getOrElse(""))) // 21
    def yotta() = quantity("1000", zetta, Some("yotta" + name.getOrElse("")), Some("Y" + symbol.getOrElse(""))) // 24

    def deci() = quantity("0.1", this, Some("deci" + name.getOrElse("")), Some("d" + symbol.getOrElse(""))) // -1
    def centi() = quantity("0.01", this, Some("centi" + name.getOrElse("")), Some("c" + symbol.getOrElse(""))) // -2
    def milli() = quantity("0.001", this, Some("milli" + name.getOrElse("")), Some("m" + symbol.getOrElse(""))) // -3
    def micro() = quantity("0.001", milli, Some("micro" + name.getOrElse("")), Some("μ" + symbol.getOrElse(""))) // -6
    def nano() = quantity("0.001", micro, Some("nano" + name.getOrElse("")), Some("n" + symbol.getOrElse(""))) // -9

    override def hashCode = 42 // TODO (was overflowing stack)

    override def toString() = conversion
      .map(c => c.bd + " " + c.getSource().getSymbol.getOrElse(""))
      .getOrElse(name.getOrElse("") + " (" + symbol.getOrElse("") + "): a measure of " + this.getClass().getSimpleName())

    def *:(bd: BigDecimal) = quantity(bd, this)
    def in_:(bd: BigDecimal) = quantity(bd, this)

    def +(right: UOM): UOM = {
      val (bd, uom) = conversion.map(c => (c.bd, c.getSource)).getOrElse((one, this))
      quantity(bd.add((right in uom).bd), uom)
    }

    def -(right: UOM): UOM = {
      val (bd, uom) = conversion.map(c => (c.bd, c.getSource)).getOrElse((one, this))
      quantity(bd.subtract((right in uom).bd), uom)
    }

    def *(bd: BigDecimal): UOM = conversion
      .map(c => quantity(c.bd.multiply(bd), c.getSource))
      .getOrElse(quantity(bd, this))

    def /(bd: BigDecimal): UOM = conversion
      .map(c => quantity(c.bd.divide(bd, scala.Math.max(c.bd.precision, bd.precision), HALF_UP), c.getSource))
      .getOrElse(quantity(one.divide(bd, bd.precision, HALF_UP), this))

    // TODO: use HList for by, over (?)

    // TODO: name, symbol, link for new units

    def by[QRGT <: Quantum, QRES <: Quantum](right: QRGT#UOM, resultQuantum: QRES): QRES#UOM = conversion match {
      case Some(c) => right.conversion match {
        case Some(rc) => resultQuantum.quantity(c.bd.multiply(rc.bd), resultQuantum.newUnitOfMeasurement(None)) // c.getDest x rc.getDest
        case None => resultQuantum.quantity(c.bd, resultQuantum.newUnitOfMeasurement(None)) // c x right
      }
      case None => right.conversion match {
        case Some(rc) => resultQuantum.quantity(rc.bd, resultQuantum.newUnitOfMeasurement(None)) // thisAsUOM x rc.getDest
        case None => resultQuantum.quantity(one, resultQuantum.newUnitOfMeasurement(None)) // thisAsUOM x right
      }
    }

    def over[QBOT <: Quantum, QRES <: Quantum](bottom: QBOT#UOM, resultQuantum: QRES): QRES#UOM = conversion match {
      // divide(right.magnitude, scala.Math.max(magnitude.precision, right.magnitude.precision), HALF_UP)
      case Some(c) => bottom.conversion match {
        case Some(bc) => resultQuantum.quantity(c.bd.divide(bc.bd, scala.Math.max(c.bd.precision, bc.bd.precision), HALF_UP), resultQuantum.newUnitOfMeasurement(None))
        case None => resultQuantum.quantity(c.bd, resultQuantum.newUnitOfMeasurement(None))
      }
      case None => bottom.conversion match {
        case Some(bc) => resultQuantum.quantity(one.divide(bc.bd, bc.bd.precision, HALF_UP), resultQuantum.newUnitOfMeasurement(None))
        case None => resultQuantum.quantity(one, resultQuantum.newUnitOfMeasurement(None))
      }
    }

    def through[QBOT <: Quantum, QRES <: Quantum](bottom: QBOT#UOM, resultQuantum: QRES): QRES#UOM = over(bottom, resultQuantum)

    def per[QBOT <: Quantum, QRES <: Quantum](bottom: QBOT#UOM, resultQuantum: QRES): QRES#UOM = over(bottom, resultQuantum)

    def in(other: UOM): Conversion = {
      val resultBD = conversionPath(other, this).map(path => {
        path.foldLeft(one)((bd: BigDecimal, conversion: Conversion) => bd.multiply(conversion.bd))
      })
      if (resultBD.isEmpty) {
        throw new Exception("no conversion path from " + this + " to " + other)
      }
      val result = new Conversion(this, other, resultBD.get)
      addEdge(result) // TODO: make this optional
      result
    }

  }

  type V = UOM

  type E = Conversion

  def newUnitOfMeasurement(
    conversion: Option[Conversion] = None,
    name: Option[String] = None,
    symbol: Option[String] = None,
    link: Option[String] = None): UOM

  def unit(name: String, symbol: String, linkOpt: Option[String] = None): UOM =
    newUnitOfMeasurement(None, Some(name), Some(symbol), linkOpt)

  def derive(compoundUnit: UnitOfMeasurement, nameOpt: Option[String] = None, symbolOpt: Option[String] = None, linkOpt: Option[String] = None): UOM = {
    // TODO: Check that the given compound unit is in this quantum's list of derivations
    // TODO: add the compoundUnit to the graph?
    newUnitOfMeasurement(None, nameOpt, symbolOpt, linkOpt)
  }

  def quantity(
    magnitude: BigDecimal,
    unit: UOM,
    qname: Option[String] = None,
    qsymbol: Option[String] = None,
    qlink: Option[String] = None): UOM = {

    val q = newUnitOfMeasurement(None, qname, qsymbol, qlink)
    addVertex(q)
    val conversion1 = newEdge(q, unit, one.divide(magnitude, magnitude.precision, HALF_UP))
    val conversion2 = newEdge(unit, q, magnitude)
    q.conversion = Some(conversion2)
    q
  }

  def newVertex(label: String): UOM = newUnitOfMeasurement(None, Some(label), None, None)

  def newEdge(source: UOM, dest: UOM): Conversion = {
    val result: Conversion = null // TODO
    result
  }

  def newEdge(source: UOM, dest: UOM, magnitude: BigDecimal): Conversion = {
    val edge = new Conversion(source, dest, magnitude)
    addEdge(edge)
    edge
  }

  val wikipediaUrl: String

  //  val derivations: List[Quantum]
  //
  //  def by(right: Quantum, resultQuantum: Quantum): Quantum = QuantumMultiplication(this, right, resultQuantum)
  //
  //  def over(bottom: Quantum, resultQuantum: Quantum): Quantum = QuantumMultiplication(this, bottom, resultQuantum)

  override def toString() = this.getClass().getSimpleName()

  /**
   * Searches the Directed Graph defined by this Quantum for a path of Conversions from source to goal
   * @param source Start node for shortest path search
   * @param goal End node for shortest path search
   */
  def conversionPath(source: UOM, goal: UOM): Option[List[Conversion]] =
    shortestPath(
      source.asInstanceOf[V],
      goal.asInstanceOf[V])

}

//case class QuantumMultiplication[QLEFT <: Quantum, QRIGHT <: Quantum, QRESULT <: Quantum](left: QLEFT, right: QRIGHT, resultQuantum: QRESULT) extends Quantum {
//
//  type UOM = QRESULT#UOM
//
//  val wikipediaUrl = ""
//  val unitsOfMeasurement = Nil // TODO multiplications of the cross-product of left and right
//  val derivations = Nil
//  val examples = Nil
//  
//  def newUnitOfMeasurement(
//    baseUnit: Option[QRESULT#UOM] = None,
//    magnitude: BigDecimal,
//    name: Option[String] = None,
//    symbol: Option[String] = None,
//    link: Option[String] = None): UOM = {
//
//    // TODO: pass on baseUnit
//    resultQuantum.newUnitOfMeasurement(None, magnitude, name, symbol, link)
//  }
//  
//}
//
//case class QuantumDivision[QTOP <: Quantum, QBOTTOM <: Quantum, QRESULT <: Quantum](top: QTOP, bottom: QBOTTOM, resultQuantum: QRESULT) extends Quantum {
//
//  type UOM = QRESULT#UOM
//
//  val wikipediaUrl = ""
//  val unitsOfMeasurement = Nil // TODO divisions of the cross-product of left and right
//  val derivations = Nil
//  val examples = Nil
//  
//  def newUnitOfMeasurement(
//    baseUnit: Option[UOM] = None,
//    magnitude: BigDecimal,
//    name: Option[String] = None,
//    symbol: Option[String] = None,
//    link: Option[String] = None): UOM = {
//
//    // TODO pass on baseUnit
//    resultQuantum.newUnitOfMeasurement(None, magnitude, name, symbol, link)
//  }
//  
//}


//object Quantum {
//
//  case class Scalar(bd: BigDecimal) {
//    def in[Q <: Quantum](uom: Q#UOM) = uom.quantum.quantity(bd, uom.asInstanceOf[uom.quantum.type#UOM]) // TODO remove cast
//  }
//
//  implicit def enrichString(s: String) = Scalar(new BigDecimal(s))
//
//}
