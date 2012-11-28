package axle.quanta

import java.math.BigDecimal
import axle.graph.JungDirectedGraph._

class Information extends Quantum {

  type Q = InformationQuantity
  type UOM = InformationUnit

  case class InformationUnit(
    _name: Option[String] = None,
    _symbol: Option[String] = None,
    _link: Option[String] = None)
    extends UnitOfMeasurementImpl(_name, _symbol, _link)

  def newUnitOfMeasurement(
    name: Option[String] = None,
    symbol: Option[String] = None,
    link: Option[String] = None): InformationUnit = new InformationUnit(name, symbol, link)

  class InformationQuantity(magnitude: BigDecimal, unit: InformationUnit) extends QuantityImpl(magnitude, unit)

  def newQuantity(magnitude: BigDecimal, unit: InformationUnit): InformationQuantity = new InformationQuantity(magnitude, unit)

  // def zero() = new InformationQuantity(0, bit) with ZeroWithUnit

  def conversionGraph() = _conversionGraph

  lazy val _conversionGraph = JungDirectedGraph[InformationUnit, BigDecimal](
    List(
      unit("bit", "b"),
      unit("nibble", "nibble"),
      unit("byte", "B", Some("http://en.wikipedia.org/wiki/Byte")),
      unit("kilobyte", "KB"),
      unit("megabyte", "MB"),
      unit("gigabyte", "GB"),
      unit("terabyte", "TB"),
      unit("petabyte", "PB")
    ),
    (vs: Seq[JungDirectedGraphVertex[InformationUnit]]) => vs match {
      case bit :: nibble :: byte :: kilobyte :: megabyte :: gigabyte :: terabyte :: petabyte :: Nil => List(
        (bit, nibble, "4"),
        (bit, byte, "8"),
        (byte, kilobyte, "1024"),
        (kilobyte, megabyte, "1024"),
        (megabyte, gigabyte, "1024"),
        (gigabyte, terabyte, "1024"),
        (terabyte, petabyte, "1024")
      )
    }
  )

  val wikipediaUrl = "http://en.wikipedia.org/wiki/Information"

  lazy val bit = byName("bit")
  lazy val nibble = byName("nibble")
  lazy val byte = byName("byte")
  lazy val kilobyte = byName("kilobyte")
  lazy val megabyte = byName("megabyte")
  lazy val gigabyte = byName("gigabyte")
  lazy val terabyte = byName("terabyte")
  lazy val petabyte = byName("petabyte")

  lazy val KB = kilobyte
  lazy val MB = megabyte
  lazy val GB = gigabyte
  lazy val TB = terabyte
  lazy val PB = petabyte

}

object Information extends Information()
