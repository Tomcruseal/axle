package axle.visualize

import java.awt.event.MouseEvent
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Paint
import java.awt.Stroke
import org.apache.commons.collections15.Transformer
import axle.graph.JungDirectedGraph._
import edu.uci.ics.jung.algorithms.layout.FRLayout
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position
import edu.uci.ics.jung.visualization.VisualizationViewer
import org.apache.commons.collections15.functors.ChainedTransformer
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller

class JungDirectedGraphVisualization(width: Int = 700, height: Int = 700, border: Int = 50) {

  def component[VP, EP](jdg: JungDirectedGraph[VP, EP]): Component = {

    // type V = jdg.type#V
    // type E = jdg.type#E

    // see
    // http://www.grotto-networking.com/JUNG/
    // http://www.grotto-networking.com/JUNG/JUNG2-Tutorial.pdf

    val layout = new FRLayout(jdg.storage)
    layout.setSize(new Dimension(width, height))
    // val vv = new BasicVisualizationServer[ug.type#V, ug.type#E](layout) // non-interactive
    val vv = new VisualizationViewer(layout) // interactive
    vv.setPreferredSize(new Dimension(width + border, height + border))

    val vertexPaint = new Transformer[JungDirectedGraphVertex[VP], Paint]() {
      def transform(i: JungDirectedGraphVertex[VP]): Paint = Color.GREEN
    }

    val dash = List(10.0f).toArray

    val edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f)

    val edgeStrokeTransformer = new Transformer[JungDirectedGraphEdge[VP, EP], Stroke]() {
      def transform(ep: JungDirectedGraphEdge[VP, EP]) = edgeStroke
    }

    val vertexLabelTransformer = new Transformer[JungDirectedGraphVertex[VP], String]() {
      def transform(v: JungDirectedGraphVertex[VP]) = jdg.vertexToVisualizationHtml(v.payload).toString
    }

    val edgeLabelTransformer = new Transformer[JungDirectedGraphEdge[VP, EP], String]() {
      def transform(ep: JungDirectedGraphEdge[VP, EP]) = ep.toString
    }

    vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint)
    vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer)
    vv.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer)
    vv.getRenderContext().setEdgeLabelTransformer(edgeLabelTransformer)
    vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR)

    // val gm = new DefaultModalGraphMouse()
    // gm.setMode(ModalGraphMouse.Mode.TRANSFORMING)
    val gm = new PluggableGraphMouse()
    gm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON1))
    gm.add(new PickingGraphMousePlugin())
    // gm.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, 1.1f, 0.9f))
    vv.setGraphMouse(gm)
    vv
  }

}