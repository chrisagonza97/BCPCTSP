/**
 * Source code example for "A Practical Introduction to Data
 * Structures and Algorithm Analysis, 3rd Edition (Java)"
 * by Clifford A. Shaffer
 * Copyright 2008-2011 by Clifford A. Shaffer
 */

import java.util.*;

/** Graph: Adjacency matrix */
class Graph {
    public double[][] matrix;               // The edge matrix
    private double[][] shortestMatrix;      // the updated edge matrix using the shortest path algorithm
    public int[][] shortestNext;        // the matrix to construct the path from x to y
    public int numEdge;                     // Number of edges
    public int[] Mark;                      // The mark array
    public String[] nodeName;
    public int[] prize;

    public Graph() {
    }                     // Constructors

    public Graph(int n) {
        Init(n);
    }

    public Graph(Graph G) //hard copy constructor
    {
        int n = G.n();

        this.Mark = new int[n];
        this.matrix = new double[n][n];
        this.shortestMatrix = new double[n][n];
        this.shortestNext = new int[n][n];
        this.prize = new int[n];

        for (int i = 0; i < n; i++) {
            this.Mark[i] = G.Mark[i];
            this.prize[i] = G.prize[i];
            for (int j = 0; j < n; j++) {
                this.matrix[i][j] = G.matrix[i][j];
                this.shortestMatrix[i][j] = G.shortestMatrix[i][j];
                this.shortestNext[i][j] = G.shortestNext[i][j];
            }
        }

        int k = G.numEdge;
        this.numEdge = k;
    }

    public void Init(int n) {
        Mark = new int[n];
        matrix = new double[n][n];
        numEdge = 0;
        nodeName = new String[n];
        prize = new int[n];
        Arrays.fill(nodeName, 0, n, "");
    }

    public int n() {
        return Mark.length;
    } // # of vertices

    public int e() {
        return numEdge;
    }     // # of edges

    /** Set the weight for an edge */
    public void setEdge(int i, int j, double wt) {
        assert wt != 0 : "Cannot set weight to 0";
        if (matrix[i][j] == 0) numEdge++;
        matrix[i][j] = wt;
    }

    /** @return an edge's weight */
    public double weight(int i, int j) {
        return matrix[i][j];
    }

    /** @return the shortest path from i to j */
    public double shortestPath(int i, int j) {
        return shortestMatrix[i][j];
    }

    /** Set/Get the mark value for a vertex */
    public void setMark(int v, int val) {
        this.Mark[v] = val;
    }

    public int getMark(int v) {
        return Mark[v];
    }

    /** Set/Get the name value for a vertex */
    public void setName(int v, String val) {
        this.nodeName[v] = val;
    }

    public String getName(int v) {
        return nodeName[v];
    }

    /** Set/Get the prize for a vertex */
    public void setPrize(int v, int val) {
        this.prize[v] = val;
    }

    public int getPrize(int v) {
        return prize[v];
    }

    /** @return last node of the graph */
    public int getLastNode() {
        return (this.n() - 1);
    }

    public void constructShortestPath() {
        shortestMatrix = new double[n()][n()];
        shortestNext = new int[n()][n()];

        for (int i = 0; i < n(); i++) {
            for (int j = 0; j < n(); j++) {
                shortestMatrix[i][j] = matrix[i][j];
                shortestNext[i][j] = j;
            }
        }

        // This is the famous Floyd Warshall Algorithm from https://www.geeksforgeeks.org/floyd-warshall-algorithm-dp-16/
        for (int k = 0; k < n(); k++) {
            for (int i = 0; i < n(); i++) {
                for (int j = 0; j < n(); j++) {
                    if (shortestMatrix[i][k] + shortestMatrix[k][j] < shortestMatrix[i][j]) {
                        shortestMatrix[i][j] = shortestMatrix[i][k] + shortestMatrix[k][j];
                        shortestNext[i][j] = shortestNext[i][k];
                    }
                }
            }
        }
    }

    public void printExtraPathIfNeeded(int start, int end, ArrayList<Integer> route) {
        if (shortestNext[start][end] != end) {
            // in the case we will detour
            while (start != end) {
                int next = shortestNext[start][end];
                System.out.printf(
                    "\tDetour from %-18s to %-18s with %-5.2f miles\n",
                    getName(start), getName(next), weight(start, next)
                );
                route.add(next);
                start = next;
            }
        } else {
            route.add(end);
        }
    }
}