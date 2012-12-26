package axle.visualize

import javax.swing.JPanel
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D

import Plottable._

class BarChartComponent[X, Y](barChart: BarChart[X, Y]) extends JPanel {

  import barChart._

  val clockwise90 = math.Pi / -2.0
  val counterClockwise90 = -1.0 * clockwise90

  val minX = 0.0
  val maxX = 1.0
  val yAxis = minX
  
  val scaledArea = new ScaledArea2D(width = width - 100, height, border, minX, maxX, minY, maxY)(DoublePlottable, yPlottable())

  val normalFont = new Font("Courier New", Font.BOLD, 12)
  val titleFont = new Font("Palatino", Font.BOLD, 20)

  def labels(g2d: Graphics2D, fontMetrics: FontMetrics): Unit = {

    title.map(text => {
      g2d.setFont(titleFont)
      g2d.drawString(text, (width - fontMetrics.stringWidth(text)) / 2, 20)
    })

    g2d.setFont(normalFont)

    xAxisLabel.map(text =>
      g2d.drawString(text, (width - fontMetrics.stringWidth(text)) / 2, height + (fontMetrics.getHeight - border) / 2)
    )

    yAxisLabel.map(text => {
      val tx = 20
      val ty = (height + fontMetrics.stringWidth(text)) / 2
      g2d.translate(tx, ty)
      g2d.rotate(clockwise90)
      g2d.drawString(text, 0, 0)
      g2d.rotate(counterClockwise90)
      g2d.translate(-tx, -ty)
    })

  }

  override def paintComponent(g: Graphics): Unit = {

    val g2d = g.asInstanceOf[Graphics2D]
    val fontMetrics = g2d.getFontMetrics

    g2d.setColor(Color.black)
    labels(g2d, fontMetrics)
    scaledArea.verticalLine(g2d, yAxis)
    scaledArea.horizontalLine(g2d, xAxis)

    scaledArea.drawYTics(g2d, fontMetrics, yTics)

    g2d.setColor(Color.blue)

    val padding = 0.05 // on each side
    val widthPerBar = (1.0 - (2*padding))/bars.size
    val halfWhiteSpace = (widthPerBar * (1.0 - barWidthPercent)) / 2.0
    
    for( ((label, value), i) <- bars.zipWithIndex ) {
      val leftX = padding + halfWhiteSpace + i*widthPerBar
      val rightX = padding - halfWhiteSpace + (i+1)*widthPerBar
      scaledArea.fillRectangle(g2d, Point2D(leftX, minY), Point2D(rightX, value))
    }

  }

}