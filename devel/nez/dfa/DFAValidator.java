package nez.dfa;

import java.util.ArrayList;
import java.util.HashMap;

import nez.Grammar;
import nez.dfa.DFAValidator.DefaultVisitor;
import nez.lang.Expression;
import nez.lang.Production;
import nez.util.VisitorMap;

public class DFAValidator extends VisitorMap<DefaultVisitor> {

	private Grammar grammar = null;
	private ArrayList<ArrayList<DirectedEdge>> directedGraph = null;
	private HashMap<String, Integer> nonTerminalToVertexID = null;
	// private ArrayList<String> nonTerminalNames = null;
	// private ArrayList<String> parsingExpression = null;
	private String[] nonTerminalNames = null;
	private String[] parsingExpression = null;
	private int vertexID;
	private int[] in_degree = null;
	private int[] out_degree = null;
	private boolean[] removed = null;

	public DFAValidator() {
		directedGraph = new ArrayList<ArrayList<DirectedEdge>>();
		nonTerminalToVertexID = new HashMap<String, Integer>();
		vertexID = 0;
		init(DFAValidator.class, new DefaultVisitor());
	}

	public boolean isValid(Grammar grammar) {
		this.grammar = grammar;

		initGraph();
		System.out.println("<<debug>--------------------->");
		System.out.println("V = " + vertexID);
		for (int i = 0; i < vertexID; i++) {
			System.out.println(i + "-th : " + nonTerminalNames[i] + " <- " + parsingExpression[i]);
			for (int j = 0; j < directedGraph.get(i).size(); j++) {
				DirectedEdge e = directedGraph.get(i).get(j);
				System.out.println("[" + e.getSrc() + "," + e.getDst() + "] ");
			}
		}
		System.out.println("<---------------------<debug>>");

		{
			boolean update = true;
			while (update) {
				update = false;
				updateDegree();

				ArrayList<DirectedEdge> removed_edges = new ArrayList<DirectedEdge>();
				for (int i = 0; i < directedGraph.size(); i++) {
					if (in_degree[i] == 0) {
						for (int j = 0; j < directedGraph.get(i).size(); j++) {
							removed_edges.add(directedGraph.get(i).get(j));
						}
						removed[i] = true;
						update = true;
						break;
					} else if (out_degree[i] == 0) {
						for (int j = 0; j < directedGraph.size(); j++) {
							for (int k = 0; k < directedGraph.get(j).size(); k++) {
								DirectedEdge e = directedGraph.get(j).get(k);
								if (e.getDst() == i) {
									parsingExpression[e.getSrc()] = expand(e.getSrc(), e.getDst());
									removed_edges.add(e);
								}
							}
						}
						removed[i] = true;
						update = true;
						break;
					}
				}

				if (update) {
					if (removed_edges.isEmpty()) {
						System.out.println("WARNING :: SOMETHING IS WRONG :: removed_edges is empty");
					}
					for (DirectedEdge e : removed_edges) {
						directedGraph.get(e.getSrc()).remove(e);
					}
				}
			}
		}

		return false;
	}

	private String replace() {
		return null;
	}

	// replace target that is in the parsing expression of base with the parsing
	// expression of target
	private String expand(int base, int target) {
		return parsingExpression[base];
	}

	private void updateDegree() {
		for (int i = 0; i < directedGraph.size(); i++) {
			in_degree[i] = out_degree[i] = 0;
		}
		for (int i = 0; i < directedGraph.size(); i++) {
			for (int j = 0; j < directedGraph.get(i).size(); j++) {
				DirectedEdge e = directedGraph.get(i).get(j);
				++in_degree[e.getDst()];
				++out_degree[e.getSrc()];
			}
		}
	}

	private void initGraph() {
		if (this.grammar == null) {
			System.out.println("BUILD FAILED : DFAValidator.initGraph : grammar is null");
			return;
		}

		if (in_degree != null) {
			System.out.println("ERROR :: DFAValidator.initGraph : Create a new DFAValidator -|DO NOT USE MORE THAN ONCE|-");
			return;
		}

		for (Production p : grammar.getProductionList()) {
			directedGraph.add(new ArrayList<DirectedEdge>());
			nonTerminalToVertexID.put(p.getLocalName(), vertexID++);
			// nonTerminalNames.add(p.getLocalName());
			// parsingExpression.add(p.getExpression().toString());
		}
		in_degree = new int[vertexID];
		out_degree = new int[vertexID];
		removed = new boolean[vertexID];
		nonTerminalNames = new String[vertexID];
		parsingExpression = new String[vertexID];
		for (int i = 0; i < vertexID; i++) {
			in_degree[i] = out_degree[i] = 0;
			removed[i] = false;
			nonTerminalNames[vertexID] = grammar.getProductionList().get(i).getLocalName();
			parsingExpression[vertexID] = grammar.getProductionList().get(i).getExpression().toString();
		}

		for (Production p : grammar.getProductionList()) {
			visit(p.getExpression(), nonTerminalToVertexID.get(p.getLocalName()));
		}

	}

	// <----- Visitor ----->

	public void visit(Expression e, int nonTerminalVertexID) {
		find(e.getClass().getSimpleName()).accept(e, nonTerminalVertexID);
	}

	public class DefaultVisitor {
		public void accept(Expression e, int nonTerminalNa) {
			System.out.println("ERROR :: INVALID INSTANCE : WHAT IS " + e);
		}
	}

	public class Pempty extends DefaultVisitor {

		@Override
		public void accept(Expression e, int nonTerminalVertexID) {

		}

	}

	public class Pfail extends DefaultVisitor {
		@Override
		public void accept(Expression e, int nonTerminalVertexID) {

		}
	}

	public class Cany extends DefaultVisitor {
		@Override
		public void accept(Expression e, int nonTerminalVertexID) {

		}
	}

	public class Cbyte extends DefaultVisitor {
		@Override
		public void accept(Expression e, int nonTerminalVertexID) {

		}
	}

	public class Cset extends DefaultVisitor {
		@Override
		public void accept(Expression e, int nonTerminalVertexID) {

		}
	}

	public class Poption extends DefaultVisitor {
		@Override
		public void accept(Expression e, int nonTerminalVertexID) {
			visit(e.get(0), nonTerminalVertexID);
		}
	}

	public class Pzero extends DefaultVisitor {
		@Override
		public void accept(Expression e, int nonTerminalVertexID) {
			visit(e.get(0), nonTerminalVertexID);
		}
	}

	public class Pone extends DefaultVisitor {
		@Override
		public void accept(Expression e, int nonTerminalVertexID) {
			visit(e.get(0), nonTerminalVertexID);
		}
	}

	public class Pand extends DefaultVisitor {
		@Override
		public void accept(Expression e, int nonTerminalVertexID) {
			visit(e.get(0), nonTerminalVertexID);
		}
	}

	public class Pnot extends DefaultVisitor {
		@Override
		public void accept(Expression e, int nonTerminalVertexID) {
			visit(e.get(0), nonTerminalVertexID);
		}
	}

	public class Psequence extends DefaultVisitor {
		@Override
		public void accept(Expression e, int nonTerminalVertexID) {
			visit(e.getFirst(), nonTerminalVertexID);
			visit(e.getNext(), nonTerminalVertexID);
		}
	}

	public class Pchoice extends DefaultVisitor {
		@Override
		public void accept(Expression e, int nonTerminalVertexID) {
			for (int i = 0; i < e.size(); i++) {
				visit(e.get(i), nonTerminalVertexID);
			}
		}
	}

	public class NonTerminal extends DefaultVisitor {
		@Override
		public void accept(Expression e, int nonTerminalVertexID) {
			int dstNonTerminalVertexID = nonTerminalToVertexID.get(((nez.lang.expr.NonTerminal) e).getLocalName());
			directedGraph.get(nonTerminalVertexID).add(new DirectedEdge(nonTerminalVertexID, dstNonTerminalVertexID, -1));
		}
	}

}
