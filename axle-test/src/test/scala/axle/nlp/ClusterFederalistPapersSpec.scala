package axle.nlp

import org.specs2.mutable.Specification
import axle.nlp.language.English

class ClusterFederalistPapersSpec extends Specification {

  "federal papers" should {
    "cluster with k-means" in {

      import axle.data.FederalistPapers._

      val corpus = Corpus(articles.map(_.text), English)

      val frequentWords = corpus.wordsMoreFrequentThan(100)
      val topBigrams = corpus.topKBigrams(200)
      val numDimensions = frequentWords.size + topBigrams.size

      def featureExtractor(fp: Article): List[Double] = {
        import axle.enrichGenSeq
        import spire.implicits.LongAlgebra

        val tokens = English.tokenize(fp.text.toLowerCase)
        val wordCounts = tokens.tally[Long]
        val bigramCounts = bigrams(tokens).tally[Long]
        val wordFeatures = frequentWords.map(wordCounts(_) + 0.1)
        val bigramFeatures = topBigrams.map(bigramCounts(_) + 0.1)
        wordFeatures ++ bigramFeatures
      }

      import spire.implicits._
      import axle.ml.distance._
      import axle.ml.distance.Euclidean
      import org.jblas.DoubleMatrix
      import axle.jblas.linearAlgebraDoubleMatrix

      implicit val space = {
        import spire.implicits.IntAlgebra
        import spire.implicits.DoubleAlgebra
        import axle.jblas.moduleDoubleMatrix
        implicit val inner = axle.jblas.rowVectorInnerProductSpace[Int, Int, Double](numDimensions)
        Euclidean[DoubleMatrix, Double]
      }

      import axle.ml.KMeans
      import axle.ml.PCAFeatureNormalizer
      import spire.implicits.DoubleAlgebra

      val normalizer = (PCAFeatureNormalizer[DoubleMatrix] _).curried.apply(0.98)

      val classifier = KMeans[Article, List[Article], List[Seq[Double]], DoubleMatrix](
        articles,
        N = numDimensions,
        featureExtractor,
        normalizer,
        K = 4,
        iterations = 100)

      import axle.ml.ConfusionMatrix
      import spire.implicits.IntAlgebra
      import axle.orderStrings

      val confusion = ConfusionMatrix[Article, Int, String, Vector[Article], DoubleMatrix, Vector[(String, Int)], Vector[String]](
        classifier,
        articles.toVector,
        _.author,
        0 to 3)

      import axle.string

      string(corpus) must contain("Top 10 words")
      articles.size must be greaterThan 50
      confusion.counts.rows must be equalTo 5
    }

  }

}