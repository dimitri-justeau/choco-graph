package org.chocosolver.checked;

import org.chocosolver.graphsolver.GraphModel;
import org.chocosolver.graphsolver.variables.UndirectedGraphVar;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by ezulkosk on 5/22/15.
 */
public class NCCTest {

    @Test(groups = "10s")
    public void testConnectedArticulationX() {
        GraphModel m = new GraphModel();
        int n = 6;
        // build m
        UndirectedGraph GLB = new UndirectedGraph(m,n, SetType.BIPARTITESET,false);
        UndirectedGraph GUB = new UndirectedGraph(m,n,SetType.BIPARTITESET,false);
        for(int i=0;i<n;i++)GUB.addNode(i);
        GLB.addNode(0);
        GLB.addNode(4);
        GUB.addEdge(0,1);
        GUB.addEdge(0,3);
        GUB.addEdge(1,2);
        GUB.addEdge(1,3);
        GUB.addEdge(3,4);
        GUB.addEdge(4,5);
        UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);

        m.nbConnectedComponents(graph, m.intVar(1)).post();
        try {
            m.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }
        Assert.assertFalse(GLB.getNodes().contains(1));
        Assert.assertTrue(GLB.getNodes().contains(3));
        while (m.getSolver().solve());
    }

    @Test(groups = "10s")
    public void testConnectedArticulation() {
        GraphModel m = new GraphModel();
        int n = 7;
        // build m
        UndirectedGraph GLB = new UndirectedGraph(m,n, SetType.BIPARTITESET,false);
        UndirectedGraph GUB = new UndirectedGraph(m,n,SetType.BIPARTITESET,false);
        for(int i=0;i<n;i++)GUB.addNode(i);
        GLB.addNode(0);
        GLB.addNode(5);
        GUB.addEdge(0,1);
        GUB.addEdge(0,4);
        GUB.addEdge(1,2);
        GUB.addEdge(1,3);
        GUB.addEdge(2,3);
        GUB.addEdge(1,4);
        GUB.addEdge(4,5);
        GUB.addEdge(4,6);
        GUB.addEdge(5,6);
        UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);

        m.nbConnectedComponents(graph, m.intVar(1)).post();
        try {
            m.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }
        Assert.assertFalse(GLB.getNodes().contains(1));
        Assert.assertTrue(GLB.getNodes().contains(4));
        while (m.getSolver().solve());
    }

    @Test(groups = "10s")
    public void testConnectedArticulation1() {
        GraphModel m = new GraphModel();
        int n = 4;
        // build m
        UndirectedGraph GLB = new UndirectedGraph(m,n, SetType.BIPARTITESET,false);
        UndirectedGraph GUB = new UndirectedGraph(m,n,SetType.BIPARTITESET,false);
        for(int i=0;i<n;i++)GUB.addNode(i);
        GLB.addNode(0);
        GLB.addNode(3);
        GUB.addEdge(0,1);
        GUB.addEdge(1,2);
        GLB.addEdge(0,3);
        GUB.addEdge(0,3);
        UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);

        m.nbConnectedComponents(graph, m.intVar(1)).post();
        try {
            m.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }
        Assert.assertFalse(GLB.getNodes().contains(1));
    }

    @Test(groups = "10s")
    public void testConnectedArticulation2() {
        GraphModel m = new GraphModel();
        int n = 4;
        // build m
        UndirectedGraph GLB = new UndirectedGraph(m,n, SetType.BIPARTITESET,false);
        UndirectedGraph GUB = new UndirectedGraph(m,n,SetType.BIPARTITESET,false);
        for(int i=0;i<n;i++)GUB.addNode(i);
        GLB.addNode(0);
        GLB.addNode(2);
        GLB.addNode(3);
        GUB.addEdge(0,1);
        GUB.addEdge(1,2);
        GLB.addEdge(0,3);
        GUB.addEdge(0,3);
        UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);

        m.nbConnectedComponents(graph, m.intVar(1)).post();

        try {
            m.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }
        Assert.assertTrue(GLB.getNodes().contains(1));
    }

    @Test(groups = "10s")
    public void testChocoConnected() {
        GraphModel m = new GraphModel();
        // build m
        UndirectedGraph GLB = new UndirectedGraph(m,2, SetType.BITSET,false);
        UndirectedGraph GUB = new UndirectedGraph(m,2,SetType.BITSET,false);

        GLB.addNode(0);

        GUB.addNode(0);
        GUB.addNode(1);
        GUB.addEdge(0,1);

        UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);

        assertEquals(m.nbConnectedComponents(graph, m.intVar(1)).isSatisfied(), ESat.UNDEFINED);

        m.nbConnectedComponents(graph, m.intVar(1)).post();

        while (m.getSolver().solve()){
            System.out.println(graph);
        }
    }

	@Test(groups = "10s")
	public void testChocoConnectedEmpty() {
		GraphModel m = new GraphModel();
		// build m
		UndirectedGraph GLB = new UndirectedGraph(m, 2, SetType.BITSET,false);
		UndirectedGraph GUB = new UndirectedGraph(m, 2,SetType.BITSET,false);

		// if one wants a graph with >= 2 nodes he should use the node number constraint
		// connected only focuses on the graph structure to prevent two nodes not to be connected
		// if there is 0 or only 1 node, the constraint is therefore not violated
		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);

		assertEquals(m.nbConnectedComponents(graph, m.intVar(0)).isSatisfied(), ESat.TRUE);
		IntVar nCC = m.intVar(0,1);
		m.nbConnectedComponents(graph, nCC).post();
		Assert.assertTrue(m.getSolver().solve());
		Assert.assertTrue(nCC.getValue() == 0);
	}

	@Test(groups = "10s")
	public void testChocoConnectedSingle() {
		GraphModel m = new GraphModel();
		// build m
		UndirectedGraph GLB = new UndirectedGraph(m, 2, SetType.BITSET,false);
		UndirectedGraph GUB = new UndirectedGraph(m, 2,SetType.BITSET,false);
		GLB.addNode(0);
		GUB.addNode(0);
		GUB.addNode(1);
		GUB.addEdge(0,1);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);

		assertEquals(m.nbConnectedComponents(graph, m.intVar(1)).isSatisfied(), ESat.UNDEFINED);
		m.nbConnectedComponents(graph, m.intVar(1)).post();
		while (m.getSolver().solve());
		Assert.assertTrue(m.getSolver().getSolutionCount() == 2);
	}

	@Test(groups = "10s")
	public void testChocoConnectedNot() {
		GraphModel m = new GraphModel();
		// build m
		UndirectedGraph GLB = new UndirectedGraph(m, 3, SetType.BITSET,false);
		UndirectedGraph GUB = new UndirectedGraph(m, 3,SetType.BITSET,false);
		GLB.addNode(0);
		GUB.addNode(0);
		GUB.addNode(1);
		GUB.addNode(2);
		GUB.addEdge(0,1);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);

		m.nbNodes(graph, m.intVar(3)).post();

		assertEquals(m.nbConnectedComponents(graph, m.intVar(1)).isSatisfied(), ESat.UNDEFINED);
		m.nbConnectedComponents(graph, m.intVar(1)).post();
		while (m.getSolver().solve());
		m.getSolver().printStatistics();
		Assert.assertTrue(m.getSolver().getSolutionCount() == 0);
	}

    @Test(groups = "10s")
    public void testChocoConnectedPA3() throws ContradictionException {
        GraphModel m = new GraphModel();
        UndirectedGraph LB = new UndirectedGraph(m, 3, SetType.BITSET, false);
        UndirectedGraph UB = new UndirectedGraph(m, 3, SetType.BITSET, false);
		LB.addNode(0);
		LB.addNode(2);
		UB.addNode(0);
		UB.addNode(1);
		UB.addNode(2);
		UB.addEdge(0, 1);
		UB.addEdge(1, 2);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);

        m.nbConnectedComponents(g, m.intVar(1)).post();
		Solver s = m.getSolver();

		s.propagate();
		Assert.assertTrue(g.getMandatoryNodes().size() == 3);
		Assert.assertTrue(g.isInstantiated());

		while (m.getSolver().solve());
		Assert.assertTrue(s.getSolutionCount() == 1);
	}

	@Test(groups = "10s")
	public void testChocoConnectedIsthme() throws ContradictionException {
		GraphModel m = new GraphModel();
		UndirectedGraph LB = new UndirectedGraph(m, 3, SetType.BITSET, false);
		UndirectedGraph UB = new UndirectedGraph(m, 3, SetType.BITSET, false);
		UB.addNode(0);
		UB.addNode(1);
		UB.addNode(2);
		UB.addEdge(0, 1);
		UB.addEdge(1, 2);
		UndirectedGraphVar g = m.graphVar("g", LB, UB);
		m.nbNodes(g, m.intVar(3)).post();

		m.nbConnectedComponents(g, m.intVar(1)).post();
		Solver s = m.getSolver();

		s.propagate();
		Assert.assertTrue(g.getMandatoryNodes().size() == 3);
		Assert.assertTrue(g.isInstantiated());

		while (m.getSolver().solve());
		Assert.assertTrue(s.getSolutionCount() == 1);
	}

	@Test
	public void testReif() throws ContradictionException {
		GraphModel m = new GraphModel();
		int nb = 3;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);

		for (int i : new int[] { 0,1,2 })
			GUB.addNode(i);

		add_neighbors(GUB, 0, 1,2);
		add_neighbors(GUB, 1, 2);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);
		BoolVar isConnected = m.nbConnectedComponents(graph, m.intVar(1)).reify();
		m.arithm(isConnected,"+",m.nbNodes(graph),"=",3).post();

		m.getSolver().propagate();
		Assert.assertTrue(!isConnected.isInstantiated());
		while (m.getSolver().solve());
		Assert.assertTrue(m.getSolver().getSolutionCount() == 7);
	}

	@Test
	public void testAP() throws ContradictionException {
		GraphModel m = new GraphModel();
		int nb = 33;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		for (int i : new int[] { 10, 29 })
			GLB.addNode(i);
		for (int i : new int[] { 10, 12, 28, 29, 31, 32 })
			GUB.addNode(i);

		add_neighbors(GUB, 10, 28, 31);
		add_neighbors(GUB, 12, 29, 31, 32);
		add_neighbors(GUB, 28, 10, 31);
		add_neighbors(GUB, 29, 12, 31);
		add_neighbors(GUB, 31, 10, 12, 28, 29);
		add_neighbors(GUB, 32, 12);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);

		System.out.println(graph.graphVizExport());

		m.nbConnectedComponents(graph, m.intVar(1)).post();

		m.getSolver().propagate();

		System.out.println("================== APRES PROPAGATE ================");
		System.out.println(graph.graphVizExport());

		Assert.assertTrue(graph.getMandatoryNodes().contains(31));
		Assert.assertTrue(graph.getMandatoryNodes().size()==3);
	}



	@Test
	public void testAPMini() throws ContradictionException {
		GraphModel m = new GraphModel();
		int nb = 4;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		for (int i : new int[] { 0, 2 })
			GLB.addNode(i);
		for (int i : new int[] { 0, 1, 2, 3 })
			GUB.addNode(i);

		add_neighbors(GUB, 0, 3);
		add_neighbors(GUB, 1, 2, 3);
		add_neighbors(GUB, 2, 3);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);
		m.nbConnectedComponents(graph, m.intVar(1)).post();

		m.getSolver().propagate();
		System.out.println(graph.graphVizExport());
		Assert.assertTrue(graph.getMandatoryNodes().contains(3));
		Assert.assertTrue(graph.getMandatoryNodes().size()==3);
		Assert.assertTrue(m.getSolver().solve());
	}



	@Test
	public void testAPMiniNot() throws ContradictionException {
		GraphModel m = new GraphModel();
		int nb = 4;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		for (int i : new int[] { 2 })
			GLB.addNode(i);
		for (int i : new int[] { 0, 1, 2, 3 })
			GUB.addNode(i);

		add_neighbors(GUB, 0, 3);
		add_neighbors(GUB, 1, 2, 3);
		add_neighbors(GUB, 2, 3);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);
		m.nbConnectedComponents(graph, m.intVar(1)).post();

		m.getSolver().propagate();
		System.out.println(graph.graphVizExport());
		Assert.assertTrue(graph.getMandatoryNodes().size()==1);
		Assert.assertTrue(m.getSolver().solve());
	}

	@Test
	public void testAPMiniIsthma() throws ContradictionException {
		GraphModel m = new GraphModel();
		int nb = 6;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		for (int i : new int[] { 0, 3})
			GLB.addNode(i);
		for (int i : new int[] { 0, 1, 2, 3, 4, 5})
			GUB.addNode(i);

		add_neighbors(GUB, 0, 3);
		add_neighbors(GUB, 1, 2, 3);
		add_neighbors(GUB, 2, 3);
		add_neighbors(GUB, 4, 5);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);
		m.nbConnectedComponents(graph, m.intVar(1)).post();

		m.getSolver().propagate();
		System.out.println(graph.graphVizExport());
		Assert.assertTrue(graph.getMandSuccOrNeighOf(0).contains(3));
		Assert.assertTrue(graph.getMandatoryNodes().size()==2);
		Assert.assertTrue(!graph.getPotentialNodes().contains(4));
		Assert.assertTrue(!graph.getPotentialNodes().contains(5));
		Assert.assertTrue(m.getSolver().solve());
	}

	@Test
	public void testAPMiniIsthma2() throws ContradictionException {
		GraphModel m = new GraphModel();
		int nb = 7;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		for (int i : new int[] { 0, 3, 4, 6})
			GLB.addNode(i);
		for (int i : new int[] { 0, 1, 2, 3, 4, 5, 6})
			GUB.addNode(i);

		add_neighbors(GUB, 0, 3);
		add_neighbors(GUB, 1, 2, 3);
		add_neighbors(GUB, 2, 3);
		add_neighbors(GUB, 4, 5);
		add_neighbors(GUB, 5, 6);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);
		m.nbConnectedComponents(graph, m.intVar(2)).post();

		m.getSolver().propagate();
		System.out.println(graph.graphVizExport());
		Assert.assertTrue(graph.getMandSuccOrNeighOf(0).contains(3));
		Assert.assertTrue(graph.getMandatoryNodes().size()==5);
		Assert.assertTrue(graph.getMandSuccOrNeighOf(5).size()==2);
		Assert.assertTrue(m.getSolver().solve());
	}

	@Test
	public void testAPMiniIsthmaMax() throws ContradictionException {
		GraphModel m = new GraphModel();
		int nb = 7;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		for (int i : new int[] { 0, 3, 4, 6})
			GLB.addNode(i);
		for (int i : new int[] { 0, 1, 2, 3, 4, 5, 6})
			GUB.addNode(i);

		add_neighbors(GUB, 0, 3);
		add_neighbors(GUB, 1, 2, 3);
		add_neighbors(GUB, 2, 3);
		add_neighbors(GUB, 4, 5);
		add_neighbors(GUB, 5, 6);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);
		IntVar nCC = m.intVar(7,10);
		m.nbConnectedComponents(graph, nCC).post();

		m.getSolver().propagate();

		System.out.println(graph.graphVizExport());
		System.out.println(nCC);

		Assert.assertTrue(nCC.getValue()==7);
		Assert.assertTrue(graph.getMandatoryNodes().size()==7);
		for(int i=0;i<nb;i++)
			Assert.assertTrue(graph.getPotNeighOf(i).size()==0);
		Assert.assertTrue(m.getSolver().solve());
	}

	@Test
	public void testAPMiniIsthmaMax2() throws ContradictionException {
		GraphModel m = new GraphModel();
		int nb = 7;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		for (int i : new int[] { 0, 1, 2, 3, 4, 6})
			GLB.addNode(i);
		for (int i : new int[] { 0, 1, 2, 3, 4, 5, 6})
			GUB.addNode(i);

		add_neighbors(GUB, 0, 3);
		add_neighbors(GUB, 1, 2, 3);
		add_neighbors(GUB, 2, 3);
		add_neighbors(GUB, 4, 5);
		add_neighbors(GUB, 5, 6);

		add_neighbors(GLB, 1, 2);
		add_neighbors(GLB, 2, 3);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);
		IntVar nCC = m.intVar(5,10);
		m.nbConnectedComponents(graph, nCC).post();

		m.getSolver().propagate();

		System.out.println(graph.graphVizExport());
		System.out.println(nCC);

		Assert.assertTrue(nCC.getValue()==5);
		Assert.assertTrue(graph.getMandatoryNodes().size()==7);
		Assert.assertTrue(graph.getMandNeighOf(2).size()==2);
		Assert.assertTrue(!graph.getMandNeighOf(1).contains(3));
		Assert.assertTrue(graph.getPotNeighOf(1).contains(3));
		for(int i:new int[]{0,4,5,6})
			Assert.assertTrue(graph.getPotNeighOf(i).size()==0);
		Assert.assertTrue(m.getSolver().solve());
	}

    private static void add_neighbors(UndirectedGraph g, int x, int... list) {
        for (int y : list)
            g.addEdge(x, y);
    }
}
