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

package org.chocosolver.graphsolver.cstrs.cost.tsp.lagrangian;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.graphsolver.cstrs.cost.GraphLagrangianRelaxation;
import org.chocosolver.graphsolver.cstrs.cost.trees.lagrangian.AbstractTreeFinder;
import org.chocosolver.graphsolver.variables.GraphEventType;
import org.chocosolver.graphsolver.variables.UndirectedGraphVar;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * TSP Lagrangian relaxation
 * Inspired from the work of Held & Karp
 * and Benchimol et. al. (Constraints 2012)
 *
 * @author Jean-Guillaume Fages
 */
public class PropLagrOneTree extends Propagator<Variable> implements GraphLagrangianRelaxation {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected UndirectedGraph g;
	protected IntVar obj;
	protected int n;
	protected final double[][] costs;
	protected final TIntArrayList mandatoryArcsList;
	protected boolean waitFirstSol;
	private UndirectedGraphVar gV;
	private final int[][] originalCosts;
	private final double[] penalities;
	private double totalPenalities;
	private UndirectedGraph mst;
	private double step;
	private final AbstractTreeFinder HKfilter, HK;
	private int nbSprints;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	protected PropLagrOneTree(Variable[] vars, int[][] costMatrix) {
		super(vars, PropagatorPriority.CUBIC, false);
		originalCosts = costMatrix;
		n = originalCosts.length;
		costs = new double[n][n];
		totalPenalities = 0;
		penalities = new double[n];
		mandatoryArcsList = new TIntArrayList();
		nbSprints = 30;
		HK = new PrimOneTreeFinder(n, this);
		HKfilter = new KruskalOneTreeGAC(n, this);
	}

	public PropLagrOneTree(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix) {
		this(new Variable[]{graph, cost}, costMatrix);
		g = graph.getUB();
		gV = graph;
		obj = cost;
	}

	//***********************************************************************************
	// HK Algorithm(s)
	//***********************************************************************************

	public void propagate(int evtmask) throws ContradictionException {
		if (waitFirstSol && getModel().getSolver().getSolutionCount() == 0) {
			return;//the UB does not allow to prune
		}
		// initialisation
		rebuild();
		setCosts();
		int lb;
		do {
			lb = obj.getLB();
			lagrangianRelaxation();
		} while (lb < obj.getLB());
	}

	private void lagrangianRelaxation() throws ContradictionException {
		double hkb;
		double alpha = 2;
		double beta = 0.5;
		double bestHKB;
		bestHKB = 0;
		HKfilter.computeMST(costs, g);
		hkb = HKfilter.getBound() - totalPenalities;
		bestHKB = hkb;
		mst = HKfilter.getMST();
		if (hkb - Math.floor(hkb) < 0.001) {
			hkb = Math.floor(hkb);
		}
		obj.updateLowerBound((int) Math.ceil(hkb), this);
		HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
		for (int iter = 5; iter > 0; iter--) {
			for (int i = nbSprints; i > 0; i--) {
				HK.computeMST(costs, g);
				hkb = HK.getBound() - totalPenalities;
				if (hkb > bestHKB + 1) {
					bestHKB = hkb;
				}
				mst = HK.getMST();
				if (hkb - Math.floor(hkb) < 0.001) {
					hkb = Math.floor(hkb);
				}
				obj.updateLowerBound((int) Math.ceil(hkb), this);
				// HK.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
				//	DO NOT FILTER HERE TO SPEED UP CONVERGENCE (not always true)
				updateStep(hkb, alpha);
				penalitiesHK();
				updateCostMatrix();
			}
			HKfilter.computeMST(costs, g);
			hkb = HKfilter.getBound() - totalPenalities;
			if (hkb > bestHKB + 1) {
				bestHKB = hkb;
			}
			mst = HKfilter.getMST();
			if (hkb - Math.floor(hkb) < 0.001) {
				hkb = Math.floor(hkb);
			}
			obj.updateLowerBound((int) Math.ceil(hkb), this);
			HKfilter.performPruning((double) (obj.getUB()) + totalPenalities + 0.001);
			updateStep(hkb, alpha);
			penalitiesHK();
			updateCostMatrix();
			alpha *= beta;
			beta /= 2;
		}
	}

	//***********************************************************************************
	// DETAILS
	//***********************************************************************************

	protected void rebuild() {
		mandatoryArcsList.clear();
		ISet nei;
		for (int i = 0; i < n; i++) {
			nei = gV.getMandNeighOf(i);
			for (int j : nei) {
				if (i < j) {
					mandatoryArcsList.add(i * n + j);
				}
			}
		}
	}

	private void setCosts() {
		ISet nei;
		for (int i = 0; i < n; i++) {
			nei = g.getNeighOf(i);
			for (int j : nei) {
				if (i < j) {
					costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
					costs[j][i] = costs[i][j];
				}
			}
		}
	}

	private void updateStep(double hkb, double alpha) {
		double nb2viol = 0;
		double target = obj.getUB();
		if (target - hkb < 0) {
			target = hkb + 0.1;
		}
		int deg;
		for (int i = 0; i < n; i++) {
			deg = mst.getNeighOf(i).size();
			nb2viol += (2 - deg) * (2 - deg);
		}
		if (nb2viol == 0) {
			step = 0;
		} else {
			step = alpha * (target - hkb) / nb2viol;
		}
	}

	private void penalitiesHK() {
		if (step == 0) {
			return;
		}
		double sumPenalities = 0;
		int deg;
		for (int i = 0; i < n; i++) {
			deg = mst.getNeighOf(i).size();
			penalities[i] += (deg - 2) * step;
			assert !(penalities[i] > Double.MAX_VALUE / (n - 1) || penalities[i] < -Double.MAX_VALUE / (n - 1)) :
					"Extreme-value lagrangian multipliers. Numerical issue may happen";
			sumPenalities += penalities[i];
		}
		this.totalPenalities = 2 * sumPenalities;
	}

	private void updateCostMatrix() {
		ISet nei;
		for (int i = 0; i < n; i++) {
			nei = g.getNeighOf(i);
			for (int j : nei) {
				if (i < j) {
					costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
					costs[j][i] = costs[i][j];
				}
			}
		}
	}

	//***********************************************************************************
	// INFERENCE
	//***********************************************************************************

	public void remove(int from, int to) throws ContradictionException {
		gV.removeArc(from, to, this);
	}

	public void enforce(int from, int to) throws ContradictionException {
		gV.enforceArc(from, to, this);
	}

	public void contradiction() throws ContradictionException {
		fails();
	}

	//***********************************************************************************
	// PROP METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		if (vIdx == 0) {
			return GraphEventType.REMOVE_ARC.getMask() + GraphEventType.ADD_ARC.getMask();
		} else {
			return IntEventType.boundAndInst();
		}
	}

	@Override
	public ESat isEntailed() {
		return ESat.TRUE;// it is just implied filtering
	}

	public double getMinArcVal() {
		return -(((double) obj.getUB()) + totalPenalities);
	}

	public TIntArrayList getMandatoryArcsList() {
		return mandatoryArcsList;
	}

	public boolean isMandatory(int i, int j) {
		return gV.getMandNeighOf(i).contains(j);
	}

	public void waitFirstSolution(boolean b) {
		waitFirstSol = b;
	}

	public boolean contains(int i, int j) {
		return mst == null || mst.edgeExists(i, j);
	}

	public UndirectedGraph getSupport() {
		return mst;
	}

	public double getReplacementCost(int from, int to) {
		return HKfilter.getRepCost(from, to);
	}

	public double getMarginalCost(int from, int to) {
		return HKfilter.getRepCost(from, to);
	}
}
