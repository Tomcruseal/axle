package org.pingel.axle.quanta

import org.specs2.mutable._

import java.math.BigDecimal

class QuantaSpec extends Specification {

  import org.pingel.axle.Enrichments._

  "Scalar conversion" should {
    "work" in {

      import Mass._
      import Distance._
      
      ("5" *: gram).conversion.get.bd must be equalTo new BigDecimal("5")
      ("1" *: parsec + "4" *: lightyear).conversion.get.bd must be equalTo new BigDecimal("2.228")
      ("4" *: lightyear + "1" *: parsec).conversion.get.bd must be equalTo new BigDecimal("7.26")

    }
  }

  "Quanta conversion" should {

    "work" in {

      import Distance._
      import Mass._

      (kilogram in gram).conversion.get.bd must be equalTo new BigDecimal("1000")
      (megagram in milligram).conversion.get.bd must be equalTo new BigDecimal("1000000000.0")
      (mile in ft).conversion.get.bd must be equalTo new BigDecimal("5280")
     
    }
  }

  "addiiton" should {
    "work" in {
      
      import Mass._
      import Distance._
      
      // Shouldn't compile: gram + mile
      // Shouldn't compile: gram + kilogram + mile + gram
      (earth + sun).conversion.get.bd must be equalTo new BigDecimal("1988916.0936")
      (gram + kilogram).conversion.get.bd must be equalTo new BigDecimal("1001")
    }
  }

  "over" should {
    "work" in {

      import Volume._
      import Flow._

      greatLakes.over(niagaraFalls, Time).conversion.get.bd must be equalTo new BigDecimal("12.36150")
      // TODO convert that to years
    }
  }

}