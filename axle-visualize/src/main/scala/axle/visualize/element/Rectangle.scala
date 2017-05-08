package axle.visualize.element

import axle.visualize.Color
import axle.visualize.Point2D
import axle.visualize.ScaledArea2D

case class Rectangle[X, Y](
  scaledArea: ScaledArea2D[X, Y],
  lowerLeft: Point2D[X, Y],
  upperRight: Point2D[X, Y],
  fillColor: Option[Color] = None,
  borderColor: Option[Color] = None,
  id: Option[(String, String)] = None, // optional (id, hoverText)
  link: Option[(java.net.URL, Color)] = None // optional (url, hoverColor)
  )