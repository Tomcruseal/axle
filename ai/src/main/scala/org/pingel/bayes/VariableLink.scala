package org.pingel.bayes

import org.pingel.axle.graph.UndirectedGraphEdge

class VariableLink(v1: RandomVariable, v2: RandomVariable)
extends UndirectedGraphEdge[RandomVariable]
{ 
	def getVertices() = (v1, v2)
}

