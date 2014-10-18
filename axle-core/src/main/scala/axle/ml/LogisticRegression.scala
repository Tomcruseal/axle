package axle.ml

import scala.math.exp
import scala.math.log

import axle.matrix.MatrixModule

trait LogisticRegressionModule extends MatrixModule with FeatureNormalizerModule {

  // h is essentially P(y=1 | X;θ)
  def h(xi: Matrix[Double], θ: Matrix[Double]): Double = 1 / (1 + exp(-1 * (θ.t ⨯ xi).scalar))

  def cost(xi: Matrix[Double], θ: Matrix[Double], yi: Boolean) =
    -1 * log(if (yi) h(θ, xi) else 1 - h(θ, xi))

  def predictedY(xi: Matrix[Double], θ: Matrix[Double]): Boolean =
    h(xi, θ) >= 0.5

  def Jθ(X: Matrix[Double], θ: Matrix[Double], y: Matrix[Boolean]) =
    (0 until X.rows)
      .foldLeft(0d)((r: Double, i: Int) => r + cost(X.row(i), θ, y(i, 0))) / X.rows

  def dθ(X: Matrix[Double], y: Matrix[Boolean], θ: Matrix[Double]): Matrix[Double] = {
    val yd = y.map(_ match { case true => 1d case false => 0d })
    matrix(θ.rows, 1, (r: Int, c: Int) => {
      (0 until X.rows).map(i => (h(X.row(i), θ) - yd(i, 0)) * X(i, r)).sum
    })
  }

  // objective: minimize (over θ) the value of Jθ

  def gradientDescent(X: Matrix[Double], y: Matrix[Boolean], θ: Matrix[Double], α: Double, iterations: Int) =
    (0 until iterations).foldLeft(θ)((θi: Matrix[Double], i: Int) => θi - (dθ(X, y, θi) * α))

  def regression[D](
    examples: List[D],
    numObservations: Int,
    observationExtractor: D => List[Double],
    objectiveExtractor: D => Boolean,
    α: Double = 0.1,
    numIterations: Int = 100) = {

    val inputX = matrix(examples.length, numObservations, examples.flatMap(observationExtractor).toArray).t
    val y = matrix[Boolean](examples.length, 1, examples.map(objectiveExtractor).toArray)
    val featureNormalizer = new LinearFeatureNormalizer()
    val normalize = featureNormalizer.normalizer(inputX)
    val X = ones[Double](examples.length, 1) +|+ normalize.normalizedData
    val θ0 = ones[Double](X.columns, 1)
    val θ = gradientDescent(X, y, θ0, α, numIterations)
    (θ, normalize)
  }

}
