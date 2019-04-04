/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chocosolver.graphsolver.cstrs.degree;

import org.chocosolver.graphsolver.variables.*;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.Orientation;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Propagator that ensures that a node has at most N successors/predecessors/neighbors
 *
 * @author Jean-Guillaume Fages
 */
public class PropNodeDegreeAtMostCoarse extends Propagator<GraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private GraphVar g;
	private int[] degrees;
	private IncidentSet target;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNodeDegreeAtMostCoarse(DirectedGraphVar graph, Orientation setType, int degree) {
		this(graph, setType, buildArray(degree, graph.getNbMaxNodes()));
	}

	public PropNodeDegreeAtMostCoarse(DirectedGraphVar graph, Orientation setType, int[] degrees) {
		super(new DirectedGraphVar[]{graph}, PropagatorPriority.BINARY, false);
		g = graph;
		this.degrees = degrees;
		switch (setType) {
			case SUCCESSORS:
				target = new IncidentSet.SuccOrNeighSet();
				break;
			case PREDECESSORS:
				target = new IncidentSet.PredOrNeighSet();
				break;
			default:
				throw new UnsupportedOperationException("wrong parameter: use either PREDECESSORS or SUCCESSORS");
		}
	}

	public PropNodeDegreeAtMostCoarse(UndirectedGraphVar graph, int degree) {
		this(graph, buildArray(degree, graph.getNbMaxNodes()));
	}

	public PropNodeDegreeAtMostCoarse(final UndirectedGraphVar graph, int[] degrees) {
		super(new UndirectedGraphVar[]{graph}, PropagatorPriority.BINARY, false);
		target = new IncidentSet.SuccOrNeighSet();
		g = graph;
		this.degrees = degrees;
	}

	private static int[] buildArray(int degree, int n) {
		int[] degrees = new int[n];
		for (int i = 0; i < n; i++) {
			degrees[i] = degree;
		}
		return degrees;
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		ISet act = g.getPotentialNodes();
		for (int node : act) {
			checkAtMost(node);
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return GraphEventType.ADD_ARC.getMask();
	}

	@Override
	public ESat isEntailed() {
		ISet act = g.getMandatoryNodes();
		for (int i : act) {
			if (target.getPotSet(g, i).size() > degrees[i]) {
				return ESat.FALSE;
			}
		}
		if (!g.isInstantiated()) {
			return ESat.UNDEFINED;
		}
		return ESat.TRUE;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	/**
	 * When a node has more than N successors/predecessors/neighbors then it must be removed,
	 * (which results in a failure)
	 * If it has N successors/predecessors/neighbors in the kernel then other incident edges
	 * should be removed
	 */
	private void checkAtMost(int i) throws ContradictionException {
		ISet ker = target.getMandSet(g, i);
		ISet env = target.getPotSet(g, i);
		int size = ker.size();
		if (size > degrees[i]) {
			g.removeNode(i, this);
		} else if (size == degrees[i] && env.size() > size) {
			for (int other : env) {
				if (!ker.contains(other)) {
					target.remove(g, i, other, this);
				}
			}
		}
	}
}
