package solver.variables;

import solver.Solver;
import solver.cstrs.GCF;
import solver.cstrs.GraphConstraintFactory;
import util.objects.graphs.DirectedGraph;
import util.objects.graphs.UndirectedGraph;

import java.util.Arrays;

public class GraphVarFactory {

	//*************************************************************************************
	// GRAPH VARIABLES CREATION
	//*************************************************************************************

	/**
	 * Create an undirected graph variable named NAME
	 * and whose domain is the graph interval [LB,UB]
	 * BEWARE: LB and UB graphs must be backtrackable
	 * (use the solver as an argument in their constructor)!
	 *
	 * @param NAME		Name of the variable
	 * @param LB		Undirected graph representing mandatory nodes and edges
	 * @param UB		Undirected graph representing possible nodes and edges
	 * @param SOLVER	Solver of the variable
	 * @return	An undirected graph variable
	 */
	public static IUndirectedGraphVar undirectedGraph(String NAME, UndirectedGraph LB, UndirectedGraph UB, Solver SOLVER) {
		return new UndirectedGraphVar(NAME, SOLVER, LB, UB);
	}


	/**
	 * Create a directed graph variable named NAME
	 * and whose domain is the graph interval [LB,UB]
	 * BEWARE: LB and UB graphs must be backtrackable
	 * (use the solver as an argument in their constructor)!
	 *
	 * @param NAME		Name of the variable
	 * @param LB		Directed graph representing mandatory nodes and edges
	 * @param UB		Directed graph representing possible nodes and edges
	 * @param SOLVER	Solver of the variable
	 * @return	An undirected graph variable
	 */
	public static IDirectedGraphVar directedGraph(String NAME, DirectedGraph LB, DirectedGraph UB, Solver SOLVER) {
		return new DirectedGraphVar(NAME, SOLVER, LB, UB);
	}

	//*************************************************************************************
	// OTHER
	//*************************************************************************************

	/**
	 * Iterate over the variable of <code>this</code> and build an array that contains the GraphVar only.
	 * It also contains FIXED variables and VIEWS, if any.
	 *
	 * @return array of SetVars of <code>this</code>
	 */
	public static IGraphVar[] retrieveGraphVars(Solver s) {
		int n = s.getNbVars();
		IGraphVar[] bvars = new IGraphVar[n];
		int k = 0;
		for (int i = 0; i < n; i++) {
			if ((s.getVar(i).getTypeAndKind() & Variable.KIND) == Variable.GRAPH) {
				bvars[k++] = (IGraphVar) s.getVar(i);
			}
		}
		return Arrays.copyOf(bvars, k);
	}

	//***********************************************************************************
	// SIMPLE COUNTS
	//***********************************************************************************

	/**
	 * Creates an integer variable representing the number of nodes in g
	 * @param g	a graph variable
	 * @return An integer variable representing the number of nodes in g
	 */
	public static IntVar nb_nodes(IGraphVar g){
		IntVar nb = VF.bounded("nb_nodes",0,g.getNbMaxNodes(),g.getSolver());
		g.getSolver().post(GraphConstraintFactory.nb_nodes(g, nb));
		return nb;
	}

	/**
	 * Creates an integer variable representing the number of nodes in g
	 * @param g	a directed graph variable
	 * @return An integer variable representing the number of nodes in g
	 */
	public static IntVar nb_arcs(IDirectedGraphVar g){
		IntVar nb = VF.bounded("nb_arcs",0,g.getNbMaxNodes()*g.getNbMaxNodes(),g.getSolver());
		g.getSolver().post(GraphConstraintFactory.nb_arcs(g, nb));
		return nb;
	}

	/**
	 * Creates an integer variable representing the number of nodes in g
	 * @param g	an undirected graph variable
	 * @return An integer variable representing the number of nodes in g
	 */
	public static IntVar nb_edges(IUndirectedGraphVar g){
		IntVar nb = VF.bounded("nb_edges",0,g.getNbMaxNodes()*g.getNbMaxNodes(),g.getSolver());
		g.getSolver().post(GraphConstraintFactory.nb_edges(g, nb));
		return nb;
	}

	/**
	 * Creates an integer variable representing the number of loops in g
	 * IntVar = |(i,i) in g|
	 * @param g	a graph variable
	 * @return An integer variable representing the number of loops in g
	 */
	public static IntVar nb_loops(IGraphVar g){
		IntVar nb = VF.bounded("nb_loops",0,g.getNbMaxNodes(),g.getSolver());
		g.getSolver().post(GraphConstraintFactory.nb_loops(g,nb));
		return nb;
	}

	//***********************************************************************************
	// CHANNELING VARIABLES
	//***********************************************************************************

	// Vertices

	/**
	 * Creates a set variable representing the nodes of g
	 * @param g	a graph variable
	 * @return	a set variable representing the nodes of g
	 */
	public static SetVar nodes_set(IGraphVar g){
		int n = g.getNbMaxNodes();
		SetVar nodes = VF.set("nodes",0,n-1,g.getSolver());
		g.getSolver().post(GCF.nodes_channeling(g, nodes));
		return nodes;
	}

	/**
	 * Creates an array of boolean variables representing the nodes of g
	 * @param g	a graph variable
	 * @return	an array of boolean variables representing the nodes of g
	 */
	public static BoolVar[] nodes_bool_array(IGraphVar g){
		BoolVar[] nodes = VF.boolArray("nodes",g.getNbMaxNodes(),g.getSolver());
		g.getSolver().post(GCF.nodes_channeling(g, nodes));
		return nodes;
	}

	/**
	 * Creates a boolean variable representing if the node 'vertex' is in g or not
	 * @param g	a graph variable
	 * @param vertex index of a potential vertex of g
	 * @return a boolean variable representing if the node 'vertex' is in g or not
	 */
	public static BoolVar node_bool(IGraphVar g, int vertex){
		BoolVar node = VF.bool("nodes("+vertex+")",g.getSolver());
		g.getSolver().post(GCF.node_channeling(g, node, vertex));
		return node;
	}

	// Arc


	/**
	 * Creates a boolean variable representing if the arc '(from,to)' is in g or not
	 * @param g	a directed graph variable
	 * @param from index of a potential vertex of g
	 * @param to index of a potential vertex of g
	 * @return a boolean variable representing if the arc '(from,to)' is in g or not
	 */
	public static BoolVar arc_bool(IDirectedGraphVar g, int from, int to){
		BoolVar node = VF.bool("arc("+from+","+to+")",g.getSolver());
		g.getSolver().post(GCF.arc_channeling(g, node, from, to));
		return node;
	}

	// Edge

	/**
	 * Creates a boolean variable representing if the edge '(v1,v2)' is in g or not
	 * @param g	an undirected graph variable
	 * @param v1 index of a potential vertex of g
	 * @param v2 index of a potential vertex of g
	 * @return a boolean variable representing if the edge '(v1,v2)' is in g or not
	 */
	public static BoolVar edge_bool(IUndirectedGraphVar g, int v1, int v2){
		BoolVar node = VF.bool("edge("+v1+","+v2+")",g.getSolver());
		g.getSolver().post(GCF.edge_channeling(g, node, v1, v2));
		return node;
	}

	// Neighbors

	/**
	 * Creates an array of integer variables representing the unique successor of each vertex
	 * This implicitly creates an orientation (even though the graph variable is undirected)
	 * IntVar[i] = j OR IntVar[j] = i <=> (i,j) in g
	 * @param g	an undirected graph variable having an orientation with exactly one successor per vertex
	 *          and for which every vertex is mandatory
	 * @return an array of integer variables representing the unique successor of each vertex
	 */
	public static IntVar[] neigh_int_array(IUndirectedGraphVar g){
		int n = g.getNbMaxNodes();
		IntVar[] successors = VF.enumeratedArray("neighOf",n,0,n-1,g.getSolver());
		g.getSolver().post(GCF.neighbors_channeling(g, successors));
		return successors;
	}

	/**
	 * Creates an array of set variables representing the neighborhood of every vertex
	 * int j in SetVar[i] <=> (i,j) in g
	 * @param g	an undirected graph variable
	 * @return an array of set variables representing the neighborhood of every vertex
	 */
	public static SetVar[] neigh_set_array(IUndirectedGraphVar g){
		int n = g.getNbMaxNodes();
		SetVar[] neighbors = new SetVar[n];
		for(int i=0;i<n;i++){
			neighbors[i] = VF.set("neighOf("+i+")",0,n-1,g.getSolver());
		}
		g.getSolver().post(GCF.neighbors_channeling(g, neighbors));
		return neighbors;
	}

	/**
	 * Creates a matrix of boolean variables representing the adjacency matrix of g
	 * BoolVar[i][j] = 1 <=> (i,j) in g
	 * @param g	an undirected graph variable
	 * @return a matrix of boolean variables representing the adjacency matrix of g
	 */
	public static BoolVar[][] neigh_bool_matrix(IUndirectedGraphVar g){
		int n = g.getNbMaxNodes();
		BoolVar[][] neighbors = VF.boolMatrix("neighOf",n,n,g.getSolver());
		g.getSolver().post(GCF.neighbors_channeling(g, neighbors));
		return neighbors;
	}

	/**
	 * Creates a set variable representing the neighborhood of 'node' in g
	 * int j in SetVar <=> (node,j) in g
	 * @param g	an undirected graph variable
	 * @return a set variable representing the neighborhood of 'node' in g
	 */
	public static SetVar neighOf_set(IUndirectedGraphVar g, int node){
		int n = g.getNbMaxNodes();
		SetVar neighborsOf = VF.set("neighOf("+node+")",0,n-1,g.getSolver());
		g.getSolver().post(GCF.neighbors_channeling(g, neighborsOf, node));
		return neighborsOf;
	}

	/**
	 * Creates an array of boolean variables representing the neighborhood of 'node' in g
	 * BoolVar[j] = 1 <=> (node,j) in g
	 * @param g	an undirected graph variable
	 * @return an array of boolean variables representing the neighborhood of 'node' in g
	 */
	public static BoolVar[] neighborsChanneling(IUndirectedGraphVar g, int node){
		BoolVar[] neighborsOf = VF.boolArray("neighOf("+node+")",g.getNbMaxNodes(),g.getSolver());
		g.getSolver().post(GCF.neighbors_channeling(g, neighborsOf, node));
		return neighborsOf;
	}

	// Successors

	/**
	 * Creates an array of integer variables representing the unique successor of each vertex
	 * IntVar[i] = j <=> (i,j) in g
	 * @param g	a directed graph variable having exactly one successor per vertex and for which every
	 *          vertex is mandatory
	 * @return an array of integer variables representing the unique successor of each vertex
	 */
	public static IntVar[] succ_int_array(IDirectedGraphVar g){
		int n = g.getNbMaxNodes();
		IntVar[] successors = VF.enumeratedArray("succOf",n,0,n-1,g.getSolver());
		g.getSolver().post(GCF.successors_channeling(g, successors));
		return successors;
	}

	/**
	 * Creates an array of set variables representing the successors of every vertex
	 * int j in SetVar[i] <=> (i,j) in g
	 * @param g	a directed graph variable
	 * @return an array of set variables representing the successors of every vertex
	 */
	public static SetVar[] succ_set_array(IDirectedGraphVar g){
		int n = g.getNbMaxNodes();
		SetVar[] successors = new SetVar[n];
		for(int i=0;i<n;i++){
			successors[i] = VF.set("succOf("+i+")",0,n-1,g.getSolver());
		}
		g.getSolver().post(GCF.successors_channeling(g, successors));
		return successors;
	}

	/**
	 * Creates a matrix of boolean variables representing the adjacency matrix of g
	 * BoolVar[i][j] = 1 <=> (i,j) in g
	 * @param g	a directed graph variable
	 * @return a matrix of boolean variables representing the adjacency matrix of g
	 */
	public static BoolVar[][] succ_bool_matrix(IDirectedGraphVar g){
		int n = g.getNbMaxNodes();
		BoolVar[][] successors = VF.boolMatrix("succOf",n,n,g.getSolver());
		g.getSolver().post(GCF.successors_channeling(g, successors));
		return successors;
	}

	/**
	 * Creates a set variable representing the successors of 'node' in g
	 * int j in SetVar <=> (node,j) in g
	 * @param g	a directed graph variable
	 * @return a set variable representing the successors of 'node' in g
	 */
	public static SetVar succOf_set(IDirectedGraphVar g, int node){
		int n = g.getNbMaxNodes();
		SetVar successorsOf = VF.set("succOf("+node+")",0,n-1,g.getSolver());
		g.getSolver().post(GCF.successors_channeling(g, successorsOf, node));
		return successorsOf;
	}

	/**
	 * Creates an array of boolean variables representing the successors of 'node' in g
	 * BoolVar[j] = 1 <=> (node,j) in g
	 * @param g	a directed graph variable
	 * @return an array of boolean variables representing the successors of 'node' in g
	 */
	public static BoolVar[] successorsChanneling(IDirectedGraphVar g, int node){
		BoolVar[] successorsOf = VF.boolArray("succOf("+node+")",g.getNbMaxNodes(),g.getSolver());
		g.getSolver().post(GCF.successors_channeling(g, successorsOf, node));
		return successorsOf;
	}

	// Predecessors

	/**
	 * Creates a set variable representing the predecessors of 'node' in g
	 * int j in SetVar <=> (j,node) in g
	 * @param g	a directed graph variable
	 * @return a set variable representing the predecessors of 'node' in g
	 */
	public static SetVar predOf_set(IDirectedGraphVar g, int node){
		int n = g.getNbMaxNodes();
		SetVar predecessorsOf = VF.set("predOf("+node+")",0,n-1,g.getSolver());
		g.getSolver().post(GCF.predecessors_channeling(g, predecessorsOf, node));
		return predecessorsOf;
	}

	/**
	 * Creates an array of boolean variables representing the predecessors of 'node' in g
	 * BoolVar[j] = 1 <=> (j,node) in g
	 * @param g	a directed graph variable
	 * @return an array of boolean variables representing the predecessors of 'node' in g
	 */
	public static BoolVar[] predecessorsChanneling(IDirectedGraphVar g, int node){
		BoolVar[] predecessorsOf = VF.boolArray("predOf("+node+")",g.getNbMaxNodes(),g.getSolver());
		g.getSolver().post(GCF.predecessors_channeling(g, predecessorsOf, node));
		return predecessorsOf;

	}

	//***********************************************************************************
	// DEGREE VARIABLES
	//***********************************************************************************

	/**
	 * Creates an array of integer variables representing the degree of each node in g
	 * IntVar[i] = k <=> |(i,j) in g| = k
	 * @param g	an undirected graph variable
	 * @return an array of integer variables representing the degree of each node in g
	 */
	public static IntVar[] degrees(IUndirectedGraphVar g){
		int n = g.getNbMaxNodes();
		IntVar[] degrees = VF.boundedArray("degree",n,0,n,g.getSolver());
		g.getSolver().post(GCF.degrees(g,degrees));
		return degrees;
	}

	/**
	 * Creates an array of integer variables representing the in-degree of each node in g
	 * IntVar[i] = k <=> |(i,j) in g| = k
	 * @param g	a directed graph variable
	 * @return an array of integer variables representing the in-degree of each node in g
	 */
	public static IntVar[] in_degrees(IDirectedGraphVar g){
		int n = g.getNbMaxNodes();
		IntVar[] degrees = VF.boundedArray("in_degree",n,0,n,g.getSolver());
		g.getSolver().post(GCF.in_degrees(g,degrees));
		return degrees;
	}

	/**
	 * Creates an array of integer variables representing the out-degree of each node in g
	 * IntVar[i] = k <=> |(j,i) in g| = k
	 * @param g	a directed graph variable
	 * @return an array of integer variables representing the out-degree of each node in g
	 */
	public static IntVar[] out_degrees(IDirectedGraphVar g){
		int n = g.getNbMaxNodes();
		IntVar[] degrees = VF.boundedArray("out_degree",n,0,n,g.getSolver());
		g.getSolver().post(GCF.out_degrees(g,degrees));
		return degrees;
	}
}
