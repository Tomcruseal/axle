package axle.visualize

import Color.yellow
import Color.black

case class DirectedGraphVisualization[DG](
  dg:          DG,
  width:       Int   = 700,
  height:      Int   = 700,
  border:      Int   = 20,
  radius:      Int   = 10,
  arrowLength: Int   = 10,
  color:       Color = yellow,
  borderColor: Color = black,
  fontSize:    Int   = 12)
