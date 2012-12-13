package axle.graph

import collection._
import axle._

case class NativeUndirectedGraph[VP, EP](vps: Seq[VP], ef: Seq[NativeUndirectedGraphVertex[VP]] => Seq[(NativeUndirectedGraphVertex[VP], NativeUndirectedGraphVertex[VP], EP)])
  extends UndirectedGraph[VP, EP] {

  type G[VP, EP] = NativeUndirectedGraph[VP, EP]
  type V[VP] = NativeUndirectedGraphVertex[VP]
  type E[VP, EP] = NativeUndirectedGraphEdge[VP, EP]

  // Seq[NativeUndirectedGraphVertex[VP]]
  val _vertices = vps.map(new NativeUndirectedGraphVertex(_))

  // Seq[NativeUndirectedGraphEdge{VP, EP]]
  val _edges = ef(_vertices).map({
    case (vi, vj, ep) => new NativeUndirectedGraphEdge(vi, vj, ep)
  })

  lazy val vertexSet = _vertices.toSet
  lazy val edgeSet = _edges.toSet

  lazy val vertex2edges: Map[NativeUndirectedGraphVertex[VP], Set[NativeUndirectedGraphEdge[VP, EP]]] =
    _edges
      .flatMap(e => {
        val (vi, vj): (NativeUndirectedGraphVertex[VP], NativeUndirectedGraphVertex[VP]) = e.vertices()
        Vector((vi, e), (vj, e))
      }).groupBy(_._1).map(kv => (kv._1, kv._2.map(_._2).toSet))
      .withDefaultValue(Set())

  def storage() = (_vertices, _edges, vertex2edges)

  override def vertices(): Set[NativeUndirectedGraphVertex[VP]] = vertexSet

  override def edges(): Set[NativeUndirectedGraphEdge[VP, EP]] = edgeSet

  def size(): Int = _vertices.size

  def findEdge(vi: NativeUndirectedGraphVertex[VP], vj: NativeUndirectedGraphVertex[VP]): Option[NativeUndirectedGraphEdge[VP, EP]] =
    _edges.find(e => (e.vertices == (vi, vj)) || (e.vertices == (vj, vi))) // Note: no matching on payload

  // TODO findVertex needs an index
  def findVertex(f: NativeUndirectedGraphVertex[VP] => Boolean): Option[NativeUndirectedGraphVertex[VP]] = _vertices.find(f(_))

  def filterEdges(f: ((NativeUndirectedGraphVertex[VP], NativeUndirectedGraphVertex[VP], EP)) => Boolean): NativeUndirectedGraph[VP, EP] = {
    val filter = (es: Seq[(NativeUndirectedGraphVertex[VP], NativeUndirectedGraphVertex[VP], EP)]) => es.filter(f(_))
    NativeUndirectedGraph(vps, filter.compose(ef))
  }

  def unlink(e: NativeUndirectedGraphEdge[VP, EP]): NativeUndirectedGraph[VP, EP] = {
    val filter = (es: Seq[(NativeUndirectedGraphVertex[VP], NativeUndirectedGraphVertex[VP], EP)]) => es.zip(_edges).filter({
      case ((vi, vj, ep), oldEdge) => oldEdge != e
    }).map(_._1)
    NativeUndirectedGraph(vps, filter.compose(ef))
  }

  def unlink(vi: NativeUndirectedGraphVertex[VP], vj: NativeUndirectedGraphVertex[VP]): NativeUndirectedGraph[VP, EP] = findEdge(vi, vj).map(unlink(_)).getOrElse(this)

  def areNeighbors(vi: NativeUndirectedGraphVertex[VP], vj: NativeUndirectedGraphVertex[VP]): Boolean = edges(vi).exists(_.connects(vi, vj))

  def forceClique(among: Set[NativeUndirectedGraphVertex[VP]], payload: (NativeUndirectedGraphVertex[VP], NativeUndirectedGraphVertex[VP]) => EP): NativeUndirectedGraph[VP, EP] = {

    val cliqued: Seq[NativeUndirectedGraphVertex[VP]] => Seq[(NativeUndirectedGraphVertex[VP], NativeUndirectedGraphVertex[VP], EP)] = (newVs: Seq[NativeUndirectedGraphVertex[VP]]) => {

      val old2new = _vertices.zip(newVs).toMap

      val newEdges = among.toIndexedSeq.permutations(2)
        .map({ case vi :: vj :: Nil => (vi, vj) })
        .filter({ case (vi, vj) => !areNeighbors(vi, vj) })
        .map({
          case (vi, vj) => {
            val newVi = old2new(vi)
            val newVj = old2new(vj)
            (newVi, newVj, payload(newVi, newVj))
          }
        })

      ef(newVs) ++ newEdges
    }

    NativeUndirectedGraph(vps, cliqued(_)) // TODO: phrase in terms of mapEdges?
  }

  def isClique(vs: IndexedSeq[NativeUndirectedGraphVertex[VP]]): Boolean =
    vs.permutations(2).∀({ case vi :: vj :: Nil => areNeighbors(vi, vj) })

  def degree(v: NativeUndirectedGraphVertex[VP]): Int = vertex2edges.get(v).map(_.size).getOrElse(0)

  def edges(v: NativeUndirectedGraphVertex[VP]): Set[NativeUndirectedGraphEdge[VP, EP]] = vertex2edges.get(v).getOrElse(Set())

  def neighbors(v: NativeUndirectedGraphVertex[VP]): Set[NativeUndirectedGraphVertex[VP]] =
    vertex2edges.get(v).map(edges => edges.map(edge => edge.other(v)))
    .getOrElse(Set[NativeUndirectedGraphVertex[VP]]())

  def delete(v: NativeUndirectedGraphVertex[VP]): NativeUndirectedGraph[VP, EP] = NativeUndirectedGraph(vps.filter(_ != v), ef)

  // a "leaf" is vertex with only one neighbor
  def firstLeafOtherThan(r: NativeUndirectedGraphVertex[VP]): Option[NativeUndirectedGraphVertex[VP]] = vertices.find({ v => neighbors(v).size == 1 && !v.equals(r) })

  def eliminate(v: NativeUndirectedGraphVertex[VP], payload: (NativeUndirectedGraphVertex[VP], NativeUndirectedGraphVertex[VP]) => EP): NativeUndirectedGraph[VP, EP] = {
    // "decompositions" page 3 (Definition 3, Section 9.3)
    // turn the neighbors of v into a clique
    null // TODO: remove v and all edges it touches, then force clique of all of v's neighbors
  }

  /**
   * dijkstra
   *
   * Modelled after psuedocode on Wikipedia:
   *
   *   http://en.wikipedia.org/wiki/Dijkstra's_algorithm
   */
  //  def dijkstra(source: V, target: V): Map[V, Int] = {
  //
  //    def edgeCost(v1: V, v2: V): Int = 1 // TODO: generalize
  //
  //    val undefined = -1
  //    val dist = mutable.Map[V, Int]()
  //    val previous = mutable.Map[V, V]()
  //    for (v <- vertices) {
  //      dist(v) = Int.MaxValue // Unknown distance function from source to v
  //    }
  //
  //    dist(source) = 0 // Distance from source to source
  //    val Q = mutable.Set[V]() ++ vertices // All nodes in the graph are unoptimized - thus are in Q
  //    var broken = false
  //    while (Q.size > 0 && !broken) {
  //      val u = Q.minBy(dist(_)) // Start node in first case
  //      Q -= u
  //      if (u == target) {
  //        var S = List[V]()
  //        var u = target
  //        while (previous.contains(u)) {
  //          S = u :: S
  //          u = previous(u)
  //        }
  //      }
  //      if (dist(u) == Int.MaxValue) {
  //        broken = true // all remaining vertices are inaccessible from source
  //      } else {
  //        for (v <- neighbors(u)) { // where v has not yet been removed from Q
  //          val alt = dist(u) + edgeCost(u, v)
  //          if (alt < dist(v)) { // Relax (u,v,a)
  //            dist(v) = alt
  //            previous(v) = u
  //            // TODO decrease - key v in Q // Reorder v in the Queue
  //          }
  //        }
  //      }
  //    }
  //    dist
  //  }

}

class NativeUndirectedGraphEdge[VP, EP](v1: NativeUndirectedGraphVertex[VP], v2: NativeUndirectedGraphVertex[VP], ep: EP)
  extends UndirectedGraphEdge[VP, EP] {

  type V[VP] = NativeUndirectedGraphVertex[VP]
  
  def vertices(): (NativeUndirectedGraphVertex[VP], NativeUndirectedGraphVertex[VP]) = (v1, v2)

  def payload(): EP = ep

  override def other(u: NativeUndirectedGraphVertex[VP]): NativeUndirectedGraphVertex[VP] = super.other(u).asInstanceOf[NativeUndirectedGraphVertex[VP]]

}

class NativeUndirectedGraphVertex[VP](vp: VP)
  extends UndirectedGraphVertex[VP] {
  def payload(): VP = vp
}

trait NativeUndirectedGraphFactory extends UndirectedGraphFactory {

  type G[VP, EP] = NativeUndirectedGraph[VP, EP]
  type V[VP] = NativeUndirectedGraphVertex[VP]
  // type S = (Seq[NativeUndirectedGraphVertex[VP]], Seq[NativeUndirectedGraphEdge[VP, EP]], Map[NativeUndirectedGraphVertex[VP], Set[NativeUndirectedGraphEdge[VP, EP]]])

  override def apply[VP, EP](vps: Seq[VP], ef: Seq[V[VP]] => Seq[(V[VP], V[VP], EP)]): NativeUndirectedGraph[VP, EP] = new NativeUndirectedGraph(vps, ef)
}

object NativeUndirectedGraph extends NativeUndirectedGraphFactory
