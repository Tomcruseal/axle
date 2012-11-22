package axle.quanta

import java.math.BigDecimal
import axle.graph.JungDirectedGraph._

class Flow extends Quantum {

  type UOM = FlowUnit

  class FlowUnit(
    conversion: Option[JungDirectedGraphEdge[UOM, BigDecimal]] = None,
    name: Option[String] = None,
    symbol: Option[String] = None,
    link: Option[String] = None)
    extends UnitOfMeasurementImpl(conversion, name, symbol, link)

  def newUnitOfMeasurement(
    conversion: Option[JungDirectedGraphEdge[UOM, BigDecimal]] = None,
    name: Option[String] = None,
    symbol: Option[String] = None,
    link: Option[String] = None): FlowUnit = new FlowUnit(conversion, name, symbol, link)

  def zero() = new FlowUnit(None, Some("zero"), Some("0"), None) with ZeroWithUnit

  val wikipediaUrl = "http://en.wikipedia.org/wiki/Volumetric_flow_rate"
    
  // val derivations = List(Volume.over(Time, this))

  import Volume.{m3}
  import Time.{second}

  val m3s = derive(m3.over[Time.type, this.type](second, this), Some("cubic meters per second"), Some("m^3/s"))

  val niagaraFalls = quantity("1834", m3s, Some("Niagara Falls Flow"), None, Some("http://en.wikipedia.org/wiki/Niagara_Falls"))
  
}

object Flow extends Flow()
