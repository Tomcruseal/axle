package axle.quanta

import java.math.BigDecimal

class Area extends Quantum {

  type UOM = AreaUnit

  class AreaUnit(
    conversion: Option[CGE] = None,
    name: Option[String] = None,
    symbol: Option[String] = None,
    link: Option[String] = None)
    extends UnitOfMeasurement(conversion, name, symbol, link)

  def newUnitOfMeasurement(
    conversion: Option[CGE] = None,
    name: Option[String] = None,
    symbol: Option[String] = None,
    link: Option[String] = None): AreaUnit = new AreaUnit(conversion, name, symbol, link)
  
  import Distance.{meter, km}
  
  val wikipediaUrl = "http://en.wikipedia.org/wiki/Area"
    
  // val derivations = List(Distance.by(Distance, this))

  val m2 = derive(meter.by[Distance.type, this.type](meter, this))

  val km2 = derive(km.by[Distance.type, this.type](km, this))
  
}


object Area extends Area()