package axle.visualize.gl

import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

import com.jogamp.opengl.util.FPSAnimator

import axle.quanta2.Angle.degree
import axle.quanta2.Distance
import axle.quanta2.Quantity
import javax.swing.JFrame
import javax.swing.SwingUtilities
import spire.math.Number.apply
import spire.implicits.DoubleAlgebra
import spire.implicits.FloatAlgebra
import spire.implicits.moduleOps

class SceneFrame(scene: Scene, width: Int, height: Int, zNear: Quantity[Distance, Float], zFar: Quantity[Distance, Float], fps: Int) {

  def run(): Unit = {

    SwingUtilities.invokeLater(new Runnable() {

      def run(): Unit = {

        val canvas = new AxleGLCanvas(scene, 45f *: degree[Float], zNear, zFar, scene.distanceUnit)
        canvas.setPreferredSize(new Dimension(width, height))

        val animator = new FPSAnimator(canvas, fps, true)

        val frame = new JFrame()
        frame.getContentPane().add(canvas)
        frame.addWindowListener(new WindowAdapter() {
          override def windowClosing(e: WindowEvent): Unit = {
            new Thread() {
              override def run(): Unit = {
                if (animator.isStarted()) animator.stop()
                System.exit(0)
              }
            }.start()
          }
        })
        frame.setTitle(scene.title)
        frame.pack()
        frame.setVisible(true)
        animator.start()
      }
    })

  }
}
