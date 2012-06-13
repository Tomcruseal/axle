package axle.ml

import javax.swing.JFrame
import java.awt.{ Dimension, BasicStroke, Color, Paint, Stroke, Insets, Graphics, Graphics2D }
import javax.swing.{ JPanel, JFrame }
import java.awt.event.MouseEvent

object KMeans extends KMeans()

trait KMeans {

  import axle.matrix.JblasMatrixFactory._ // TODO: generalize
  type M[T] = JblasMatrix[T]

  def square(x: Double) = x * x

  def √(x: Double) = math.sqrt(x)

  /**
   * cluster[T]
   *
   * @typeparam T  type of the objects being classified
   *
   * @param data
   * @param N
   * @param featureExtractor
   * @param constructor
   *
   */

  def cluster[T](
    data: Seq[T],
    N: Int,
    featureExtractor: T => List[Double],
    constructor: List[Double] => T,
    K: Int,
    iterations: Int): KMeansClassifier[T] = {

    val X = matrix(
      data.length,
      N,
      data.flatMap(featureExtractor(_)).toArray)

    val (scaledX, colMins, colRanges) = Utilities.scaleColumns(X)
    val (μ, c) = clusterLA(scaledX, K, iterations)
    val classifier = KMeansClassifier(N, featureExtractor, constructor, μ, colMins, colRanges, scaledX, c)
    classifier
  }

  def distanceRow(r1: M[Double], r2: M[Double]): Double = {
    // assert(r1.isRowVector && r2.isRowVector && r1.length == r2.length)
    val dRow = r1 - r2
    math.sqrt((0 until r1.columns).map(i => square(dRow(0, i))).reduce(_ + _))
  }

  def centroidIndexClosestTo(μ: M[Double], x: M[Double]): Int = {
    val distances = (0 until μ.columns).map(k => distanceRow(μ.row(k), x))
    val minVI = distances.zipWithIndex.minBy(_._1)
    minVI._2
  }

  // indexes of centroids closest to xi
  def assignments(X: M[Double], μ: M[Double]): M[Int] = {
    val C = zeros[Int](X.rows, 1)
    for (i <- 0 until X.rows) {
      C(i, 0) = centroidIndexClosestTo(μ, X.row(i))
    }
    C
  }

  def centroids(X: M[Double], K: Int, C: M[Int]): M[Double] = {
    val accumulator = zeros[Double](K, X.columns)
    val counts = zeros[Int](K, 1) // Note: Could be a M[Int]
    for (i <- 0 until X.rows) {
      val xi = X.row(i)
      val a = C(i, 0)
      counts(a, 0) += 1
      for (c <- 0 until X.columns) {
        accumulator(a, c) += xi(0, c)
      }
    }

    // accumulator ⨯ counts.inv
    // TODO rephrase this using linear algebra:
    for (r <- 0 until K) {
      val v = counts(r, 0)
      for (c <- 0 until X.columns) {
        if (v == 0) {
          accumulator(r, c) = math.random // TODO verify KMeans algorithm
        } else {
          accumulator(r, c) /= v
        }
      }
    }
    accumulator
  }

  /**
   * assumes that X has already been normalized
   */

  def clusterLA(X: M[Double], K: Int, iterations: Int): (M[Double], M[Int]) = {
    assert(K < X.rows)
    (0 until iterations).foldLeft((
      rand[Double](K, X.columns), // random initial K centroids μ in R^n (aka M)
      zeros[Int](X.rows, 1)) // indexes of centroids closest to xi
    )((μC: (M[Double], M[Int]), i: Int) => {
      val C = assignments(X, μC._1) // K-element column vector
      val μ = centroids(X, K, C) // K x n
      (μ, C)
    })
  }

  class KMeansVisualization[D](classifier: KMeansClassifier[D]) {

    val PAD = 50
    val WIDTH = 600
    val HEIGHT = 600
    val DIAMETER = 10 // of data points

    def draw(): Unit = {
      val frame = new JFrame("KMeans Clustering")
      frame.setBackground(Color.white)
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      frame.setSize(WIDTH, HEIGHT)
      frame.add(new KMeansPanel(classifier))
      //frame.pack()
      //frame.setLocationRelativeTo(null)
      frame.setVisible(true)
    }

    class KMeansPanel(classifier: KMeansClassifier[D]) extends JPanel {

      val colors = List(Color.blue, Color.red, Color.green, Color.orange, Color.pink, Color.yellow)

      // TODO: paintComponent is executed for many kinds of events that will not change the image

      def project(x: Double, y: Double): (Int, Int) = {
        val xp = PAD + (x * (WIDTH - 2 * PAD)).toInt
        val yp = PAD + (y * (HEIGHT - 2 * PAD)).toInt
        (xp, yp)
      }

      override def paintComponent(g: Graphics): Unit = {
        println("KMeansPanel.paintComponent")
        // super.paintComponent(g)
        val size = getSize()
        // val insets = getInsets()
        // val w = size.width - (insets.left + insets.right)
        // val h = size.height - (insets.top + insets.bottom)
        val g2d = g.asInstanceOf[Graphics2D]
        for (i <- 0 until classifier.K()) {
          // TODO: inefficient loop
          g2d.setColor(colors(i % colors.length))
          for (r <- 0 until classifier.scaledX.rows) {
            if (classifier.C(r, 0) == i) {
              // TODO figure out what to do when N > 2
              val (xp, yp) = project(classifier.scaledX(r, 0), classifier.scaledX(r, 1))
              g2d.fillOval(xp, yp, DIAMETER, DIAMETER)
            }
          }
        }
        g2d.setColor(Color.black)
        val p0 = project(0, 0)
        val p1 = project(1, 1)
        g2d.drawRect(p0._1, p0._2, p1._1, p1._2)
      }
    }
  }

  /**
   * KMeansClassifier[D]
   *
   * @typeparam D       type of the objects being classified
   *
   * @param N                number of features
   * @param featureExtractor creates a list of features (Doubles) of length N given a D
   * @param constructor      creates a D from list of arguments of length N
   * @param μ                K x N Matrix[Double], where each row is a centroid
   * @param colMins          1 x N
   * @param colRanges        1 x N
   */

  case class KMeansClassifier[D](
    N: Int,
    featureExtractor: D => List[Double],
    constructor: List[Double] => D,
    μ: M[Double],
    colMins: M[Double],
    colRanges: M[Double],
    scaledX: M[Double],
    C: M[Int]) {

    def K(): Int = μ.rows

    val exemplars: List[D] = (0 until K).map(i => {
      val unscaledCentroid = (μ.row(i) ⨯ diag(colRanges)) + colMins
      constructor(unscaledCentroid.toList)
    }).toList

    def exemplar(i: Int): D = exemplars(i)

    def classify(observation: D): Int = {
      val featureList = featureExtractor(observation)
      val featureRowMatrix = matrix(1, featureList.length, featureList.toArray)
      val scaledX = diag(colRanges).inv ⨯ (featureRowMatrix.subRowVector(colMins).t)
      centroidIndexClosestTo(μ, scaledX)
    }

    def draw(): Unit = new KMeansVisualization(this).draw()

  }

}
