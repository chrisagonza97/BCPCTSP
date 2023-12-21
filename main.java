import java.io.*;
import java.util.*;

//imports for google OR-tools
/*import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;*/

public class main {
    // meta variable
    static String fileName = "Capital_Cities10.txt";

    // CHANGE CITY AND PRIZEGOAL
    static String begin = "";
    static String end = "";
    static double budget = 0; // budget in miles
    static int n = 48;
    private static double remainingBudget;

    // static variables to be tweaked by user
    static final int TRIALS = 300000;
    static final int NUM_AGENTS = 5;
    static final double W = 1000.0; // constant value to update the reward table
    static double alpha = 0.125; // .125 learning rate
    static double gamma = 0.35; // .35 discount factor
    static final double delta = 1; // power for Q value
    static final double beta = 2; // power for distance
    static double q0 = 0.2 ; // coefficient for exploration and exploitation

    // flags for graph (do not touch)
    static final int UNVISITED = 0;
    static final int VISITED = 1;
    static final int LAST_VISIT = 2;

    // pre-initialization parameters (do not touch)
    static LinkedList<CityNode> arrCities;
    static ArrayList<String> nameList;
    static Graph sGraph;
    static double[][] Q;
    static double[][] R;
    static int statesCt;
    static int total_prize = 0;
    static double total_wt = 0;
    static ArrayList<Integer> route;

    // variables for extra credits
    static long randomSeed = 12345; // random seed for inaccessible edge, can change to any long
    static double missingProb = 0.0; // probability of an inaccessible edge

    public static void main(String[] args) throws IOException {
        // variables for time tracking
        long startTime, endTime;
        double totalTime;

        askForUserInputs();

        System.out.println("========== First greedy algorithm ==========");
        initList();
        initGraph();
        startTime = System.nanoTime();
        traverseP();
        endTime = System.nanoTime();
        totalTime = (double) (endTime - startTime) / 1000000;
        System.out.println("Algorithm took " + totalTime + "ms to process.");
        System.out.printf("\nTotal distance: %6.2f miles", total_wt);
        // System.out.printf("\nRemaining distance: %6.2fkm", budget - total_wt);
        System.out.printf("\nCollected Prize: $%d", (total_prize - arrCities.get(0).pop * 2));
        System.out.printf("\nRoute: %s\n", route.toString());
        System.out.printf("Remaining Budget: %.2f miles\n", remainingBudget);

        System.out.println("\n========== Second greedy algorithm ==========");
        initList();
        initGraph();
        startTime = System.nanoTime();
        traverseR();
        endTime = System.nanoTime();
        totalTime = (double) (endTime - startTime) / 1000000;
        System.out.println("Algorithm took " + totalTime + "ms to process.");
        System.out.printf("\nTotal distance: %6.2f miles", total_wt);
        // System.out.printf("\nRemaining distance: %6.2fkm", budget - total_wt);
        System.out.printf("\nCollected Prize: $%d", total_prize - arrCities.get(0).pop * 2);
        System.out.printf("\nRoute: %s\n", route.toString());
        System.out.printf("Remaining Budget: %.2f miles\n", remainingBudget);

        System.out.println("\n========== BC-PC-TSP MARL algorithm ==========");
        initList();
        initGraph();
        initStatics();
        startTime = System.nanoTime();
        printIlpArrays();
        learnQ();
        traverseQ();
        endTime = System.nanoTime();

        totalTime = (double) (endTime - startTime) / 1000000;
        System.out.println("Algorithm took " + totalTime + "ms to process.");
        System.out.printf("\nTotal distance: %6.2f Miles", total_wt);
        // System.out.printf("\nRemaining distance: %6.2fkm", budget - total_wt);
        System.out.printf("\nCollected Prize: $%d", total_prize - arrCities.get(0).pop * 2);
        System.out.printf("\nRoute: %s\n", makeRouteString());
        System.out.printf("Remaining Budget: %.2f Miles\n", remainingBudget);
        /*
         * System.out.println("\n========== Optimal ILP Algorithm ==========");
         * initList(true);
         * initGraph();
         * startTime = System.nanoTime();
         * //traverseIlp();
         * endTime = System.nanoTime();
         * totalTime = (double) (endTime - startTime) / 1000000;
         * System.out.println("Algorithm took " + totalTime + "ms to process.");
         * //System.out.printf("\nTotal distance: %6.2f miles", total_wt);
         * // System.out.printf("\nRemaining distance: %6.2fkm", budget - total_wt);
         * //System.out.printf("\nCollected Prize: $%d", total_prize);
         * //System.out.printf("\nRoute: %s\n", route.toString());
         * //System.out.printf("Remaining Budget: %.2f miles\n", remainingBudget);
         */
    }

    private static String makeRouteString() {
        String ret = "";
        for (int i = 0; i <route.size();i++){
            ret+=arrCities.get(route.get(i)).name;
            if(i!=route.size()-1){
                ret+="("+route.get(i)+")";
            }else{
                ret+="("+0+")";
            }
            
            if(i!=route.size()-1){
                ret+=", ";
            }
            
        }
        return ret;
    }

    private static void printIlpArrays() {
        // first print dist array in miles
        // note that dist previously used for cplex are in KM
        System.out.println();
        System.out.print("Cost =");
        for (int i = 0; i < 10; i++) {
            if (i == 0) {
                System.out.print("[[");
            } else {
                System.out.print("[");
            }
            for (int j = 0; j < 10; j++) {
                if (i == j) {
                    System.out.print(9999.99);
                } else {
                    double dist = CityNode.getDistance(arrCities.get(i), arrCities.get(j));
                    System.out.printf("%.2f", dist);
                }

                if (j != 9) {
                    System.out.print(", ");
                }

            }
            if (i == 9) {
                System.out.print("]];");
            } else {
                System.out.print("],");
                System.out.println();
            }

        }
        // print prize array
        System.out.println();
        System.out.print("p=");
        for (int i = 0; i < 10; i++) {
            if (i == 0) {
                System.out.print("[0");
            } else {
                System.out.print(arrCities.get(i).pop);
            }
            if (i != 9) {
                System.out.print(", ");
            } else {
                System.out.println("];");
            }

        }
        // print route
        /*String finRoute = " 1 -> 6 \r\n" + //
                " 2 -> 5 \r\n" + //
                " 3 -> 2 \r\n" + //
                " 4 -> 3 \r\n" + //
                " 5 -> 8 \r\n" + //
                " 6 -> 7 \r\n" + //
                " 7 -> 4 \r\n" + //
                " 8 -> 9 \r\n" + //
                " 9 -> 1";
        String finRoutefin = finRoute.replace(" 1 ", arrCities.get(0).name);
        finRoutefin = finRoutefin.replace(" 2 ", arrCities.get(1).name);
        finRoutefin = finRoutefin.replace(" 3 ", arrCities.get(2).name);
        finRoutefin = finRoutefin.replace(" 4 ", arrCities.get(3).name);
        finRoutefin = finRoutefin.replace(" 5 ", arrCities.get(4).name);
        finRoutefin = finRoutefin.replace(" 6 ", arrCities.get(5).name);
        finRoutefin = finRoutefin.replace(" 7 ", arrCities.get(6).name);
        finRoutefin = finRoutefin.replace(" 8 ", arrCities.get(7).name);
        finRoutefin = finRoutefin.replace(" 9 ", arrCities.get(8).name);
        finRoutefin = finRoutefin.replace(" 10 ", arrCities.get(9).name);
        System.out.println(finRoutefin);*/
    }

    /*
     * static void traverseIlp() {
     * // initList(true);
     * // initGraph();
     * // reset();
     * LinkedList<CityNode> tempCit = arrCities;
     * arrCities.get(0).pop = 0;
     * int infinity = java.lang.Integer.MAX_VALUE;
     * int n = 48;
     * Loader.loadNativeLibraries();
     * MPSolver solver = MPSolver.createSolver("GLOP");
     * MPVariable[][] x = new MPVariable[n][n];
     * MPVariable[] u = new MPVariable[n];
     * 
     * // create 2d array of decision variable x
     * // xij value is either 0 or 1
     * for (int i = 0; i < x.length; i++) {
     * for (int j = 0; j < x[i].length; j++) {
     * if (i != j) {
     * x[i][j] = solver.makeIntVar(0, 1, "x_" + i + "_" + j);
     * } else {
     * x[i][j] = solver.makeIntVar(0, 0, "x_" + i + "_" + j);
     * }
     * 
     * }
     * }
     * for (int i = 0; i < n; i++) {
     * u[i] = solver.makeIntVar(0, infinity, "");
     * }
     * // constraint (3)
     * // guarantees that the prize-collecting path starts at nodes and ends at node
     * t.
     * MPConstraint[] three = new MPConstraint[2];
     * makeConstraintThree(three, solver, x);
     * 
     * // constraint (4)
     * // ensures the connectivity of the path and that each node is visited at most
     * // once.
     * MPConstraint[] four = new MPConstraint[n * 2];
     * makeConstraintFour(four, solver, x);
     * 
     * // constraint (5)
     * // guarantees that the total traveling cost on the path does not exceed the
     * // given budget of B.
     * MPConstraint[] five = new MPConstraint[1];
     * makeConstraintFive(five, solver, x);
     * 
     * // constraint (6)
     * // are called Miller–Tucker–Zemlin (MTZ) Subtour Elimination Constraints.
     * They
     * // guarantee that there is one global tour visiting all vertices instead of
     * // multiple subtours each visiting only a subset of the vertices.
     * MPConstraint[] six = new MPConstraint[n * 2 + n * n * 2];
     * makeConstraintSix(six, solver, x, u);
     * 
     * // set Objective,:
     * // maximize sum(i in 2..n) sum(j in 1..n) p[i] * x[i][j];
     * MPObjective objective = solver.objective();
     * for (int i = 1; i < n; i++) {
     * for (int j = 0; j < n; j++) {
     * objective.setCoefficient(x[i][j], arrCities.get(i).pop);
     * }
     * }
     * objective.setMaximization();
     * 
     * // solve
     * final MPSolver.ResultStatus resultStatus = solver.solve();
     * // prize output
     * System.out.println("Objective(prize collected): " + objective.value());
     * // distance output
     * double distance = 0.0;
     * for (int i = 0; i < x.length; i++) {
     * for (int j = 0; j < x[i].length; j++) {
     * if (x[i][j].solutionValue() > 0) {
     * distance += CityNode.getDistance(arrCities.get(i), arrCities.get(j));
     * }
     * }
     * }
     * System.out.println("total distance: " + distance);
     * 
     * remainingBudget = budget - total_wt;
     * }
     */
    /*
     * private static void makeConstraintSix(MPConstraint[] six, MPSolver solver,
     * MPVariable[][] x, MPVariable[] u) {
     * 
     * rule_no_subtour_1:
     * forall(i in 2..n) u[i]<=n;
     * rule_no_subtour_2:
     * forall(i in 2..n) u[i]>=2;
     * 
     * rule_no_subtour_3:
     * forall(i,j in 2..n) u[i]-u[j]+1 <= (n-1) * (1-x[i][j]);
     * u[i]-u[j]+1 <= -47
     * 
     * int infinity = java.lang.Integer.MAX_VALUE;
     * int constraint = 0;
     * MPVariable one = solver.makeIntVar(1, 1, "1");
     * for (int i = 1; i < n; i++) {
     * six[constraint] = solver.makeConstraint(2, n, "");
     * six[constraint].setCoefficient(u[i], 1);
     * constraint++;
     * }
     * 
     * for(int i=1;i<n;i++){
     * six[constraint]= solver.makeConstraint(2,infinity,"");
     * six[constraint].setCoefficient(u[i], 1);
     * constraint++;
     * }
     * 
     * for (int i = 1; i < n; i++) {
     * for (int j = 1; j < n; j++) {
     * if (i == j) {
     * continue;
     * }
     * 
     * six[constraint] = solver.makeConstraint(-infinity, 0, "");
     * // left side
     * six[constraint].setCoefficient(u[i], 1);
     * six[constraint].setCoefficient(u[j], -1);
     * six[constraint].setCoefficient(one, 1);
     * constraint++;
     * // right side
     * six[constraint] = solver.makeConstraint(1, infinity, "");
     * six[constraint].setCoefficient(x[i][j], (n - 1));
     * six[constraint].setCoefficient(one, (n - 1));
     * // six[constraint].setCoefficient(x[i][j],-(n-1));
     * six[constraint].setCoefficient(x[i][j], -(n - 1) * 2);
     * constraint++;
     * }
     * }
     * six[constraint] = solver.makeConstraint(0, 0, "");
     * six[constraint].setCoefficient(u[0], 1);
     * }
     */
    /*
     * private static void makeConstraintFive(MPConstraint[] five, MPSolver solver,
     * MPVariable[][] x) {
     * 
     * constraint_3:
     * sum (i in 1..n) sum(j in 1..n) x[i][j] * Cost[i][j]<=500;
     * 
     * int infinity = java.lang.Integer.MAX_VALUE;
     * five[0] = solver.makeConstraint(-infinity, budget, "");
     * for (int i = 0; i < n; i++) {
     * for (int j = 0; j < n; j++) {
     * five[0].setCoefficient(x[i][j], CityNode.getDistance(arrCities.get(i),
     * arrCities.get(j)));
     * }
     * 
     * }
     * }
     */

    /*
     * private static void makeConstraintFour(MPConstraint[] four, MPSolver solver,
     * MPVariable[][] x) {
     * 
     * constraint_2:
     * forall (k in 1..n){
     * sum(i in 1..n) x[i][k] == sum(j in 1..n) x[k][j] ;
     * sum(i in 1..n) x[i][k] <= 1;
     * }
     * 
     * int infinity = java.lang.Integer.MAX_VALUE;
     * int constraint = 0;
     * for (int i = 0; i < n; i++) {
     * four[constraint] = solver.makeConstraint(0, 0, "");
     * for (int j = 0; j < n; j++) {
     * four[constraint].setCoefficient(x[j][i], 1);
     * }
     * for (int j = 0; j < n; j++) {
     * four[constraint].setCoefficient(x[i][j], -1);
     * }
     * constraint++;
     * four[constraint] = solver.makeConstraint(0, 1, "");
     * for (int j = 0; j < n; j++) {
     * four[constraint].setCoefficient(x[j][i], 1);
     * }
     * constraint++;
     * }
     * }
     */

    /*
     * Documentation for initList(boolean flag)
     * (1) attempts to read in file (throws error if file not found)
     * (2) converts text file info into cityNode object and places into array for
     * use in creating graph
     * (3) optional scanner functionality for user inputted begin and end points
     * (4) restructure arrayList to make BEGIN city the first node, and END city the
     * modified for ILP solution
     * last node
     */
    static void initList(boolean flag) {
        // (1)
        File towns = new File("src/" + fileName);
        arrCities = new LinkedList<>();
        nameList = new ArrayList<>();

        try {
            Scanner scan = new Scanner(towns);
            // (2)
            while (scan.hasNextLine()) {
                String name = scan.next();
                double lat = scan.nextDouble();
                double lon = scan.nextDouble();
                int pop = scan.nextInt();

                arrCities.add(new CityNode(name, lat, lon, pop));
                nameList.add(name.toLowerCase());
            }
            scan.close();
        }

        catch (FileNotFoundException p) {
            System.out.println("FILE NOT FOUND.");
            System.exit(0);
        }

        // (3)
        String startCity, endCity;
        startCity = begin.toLowerCase();
        endCity = end.toLowerCase();

        // (4)
        if (nameList.contains(startCity) && nameList.contains(endCity)) {
            if (startCity.equalsIgnoreCase(endCity)) {
                int sIndex = nameList.indexOf(startCity);
                CityNode t1 = new CityNode(arrCities.get(sIndex));
                arrCities.remove(sIndex);
                nameList.remove(sIndex);

                arrCities.add(0, t1);
                // arrCities.add(t1); this line adds an extra node at the end making size==49
            } else {
                int sIndex = nameList.indexOf(startCity);
                CityNode t1 = new CityNode(arrCities.get(sIndex));
                arrCities.remove(sIndex);
                nameList.remove(sIndex);

                int fIndex = nameList.indexOf(endCity);
                CityNode t2 = new CityNode(arrCities.get(fIndex));
                arrCities.remove(fIndex);

                arrCities.add(0, t1);
                arrCities.add(t2);
            }
        }

        else {
            System.out.println("Cannot find city.");
            System.exit(0);
        }
    }

    /*
     * Documentation for initGraph()
     * This function simply initializes the graph with requisite flags,
     * prize and weight values for each node,
     * and a name for each node (debug purposes only)
     */
    static void initGraph(boolean flag) {
        // we can have different keys for difference cases
        Random rand = new Random(randomSeed);
        sGraph = new Graph();
        int size = arrCities.size();
        sGraph.Init(size);

        for (int i = 0; i < size; i++) {
            sGraph.setMark(i, UNVISITED);
            sGraph.setName(i, arrCities.get(i).name);
            sGraph.setPrize(i, arrCities.get(i).pop);
            for (int j = 0; j < i + 1; j++) {
                if (i == j) {
                    sGraph.setEdge(i, j, 0);
                } else {
                    // randomly mark some path as inaccessible
                    if (rand.nextDouble() > missingProb) {
                        sGraph.setEdge(i, j, CityNode.getDistance(arrCities.get(i), arrCities.get(j)));
                        sGraph.setEdge(j, i, CityNode.getDistance(arrCities.get(j), arrCities.get(i)));
                    } else {
                        sGraph.setEdge(i, j, Double.MAX_VALUE);
                        sGraph.setEdge(j, i, Double.MAX_VALUE);
                    }
                }
            }
        }
        sGraph.setMark(sGraph.getLastNode(), LAST_VISIT);
        sGraph.constructShortestPath();
    }

    /*
     * private static void makeConstraintThree(MPConstraint[] three, MPSolver
     * solver, MPVariable[][] x) {
     * 
     * contraint_1a:
     * sum(2..n) x[1][j] ==1;
     * constraint_1b:
     * sum(2..n) x[i][1] ==1;
     * 
     * int constraint = 0;
     * three[constraint] = solver.makeConstraint(1, 1, "");
     * for (int i = 1; i < n; i++) {
     * three[constraint].setCoefficient(x[0][i], 1);
     * }
     * constraint++;
     * three[constraint] = solver.makeConstraint(1, 1, "");
     * for (int i = 1; i < n; i++) {
     * three[constraint].setCoefficient(x[i][0], 1);
     * }
     * constraint++;
     * }
     */

    static void askForUserInputs() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the start city: ");
        begin = scanner.nextLine();
        System.out.print("Enter the end city: ");
        end = scanner.nextLine();
        System.out.print("Enter the budget in miles: ");
        budget = scanner.nextInt();
    }

    /*
     * Documentation for initList()
     * (1) attempts to read in file (throws error if file not found)
     * (2) converts text file info into cityNode object and places into array for
     * use in creating graph
     * (3) optional scanner functionality for user inputted begin and end points
     * (4) restructure arrayList to make BEGIN city the first node, and END city the
     * last node
     */
    static void initList() {
        // (1)
        File towns = new File("src/" + fileName);
        arrCities = new LinkedList<>();
        nameList = new ArrayList<>();

        try {
            Scanner scan = new Scanner(towns);
            // (2)
            while (scan.hasNextLine()) {
                String name = scan.next();
                double lat = scan.nextDouble();
                double lon = scan.nextDouble();
                int pop = scan.nextInt();

                arrCities.add(new CityNode(name, lat, lon, pop));
                nameList.add(name.toLowerCase());
            }
            scan.close();
        }

        catch (FileNotFoundException p) {
            System.out.println("FILE NOT FOUND.");
            System.exit(0);
        }

        // (3)
        String startCity, endCity;
        startCity = begin.toLowerCase();
        endCity = end.toLowerCase();

        // (4)
        if (nameList.contains(startCity) && nameList.contains(endCity)) {
            if (startCity.equalsIgnoreCase(endCity)) {
                int sIndex = nameList.indexOf(startCity);
                CityNode t1 = new CityNode(arrCities.get(sIndex));
                arrCities.remove(sIndex);
                nameList.remove(sIndex);

                arrCities.add(0, t1);
                arrCities.add(t1);
            } else {
                int sIndex = nameList.indexOf(startCity);
                CityNode t1 = new CityNode(arrCities.get(sIndex));
                arrCities.remove(sIndex);
                nameList.remove(sIndex);

                int fIndex = nameList.indexOf(endCity);
                CityNode t2 = new CityNode(arrCities.get(fIndex));
                arrCities.remove(fIndex);

                arrCities.add(0, t1);
                arrCities.add(t2);
            }
        }

        else {
            System.out.println("Cannot find city.");
            System.exit(0);
        }
    }

    /*
     * Documentation for initGraph()
     * This function simply initializes the graph with requisite flags,
     * prize and weight values for each node,
     * and a name for each node (debug purposes only)
     */
    static void initGraph() {
        // we can have different keys for difference cases
        Random rand = new Random(randomSeed);
        sGraph = new Graph();
        int size = arrCities.size();
        sGraph.Init(size);

        for (int i = 0; i < size; i++) {
            sGraph.setMark(i, UNVISITED);
            sGraph.setName(i, arrCities.get(i).name);
            sGraph.setPrize(i, arrCities.get(i).pop);
            for (int j = 0; j < i + 1; j++) {
                if (i == j) {
                    sGraph.setEdge(i, j, 0);
                } else {
                    // randomly mark some path as inaccessible
                    if (rand.nextDouble() > missingProb) {
                        sGraph.setEdge(i, j, CityNode.getDistance(arrCities.get(i), arrCities.get(j)));
                        sGraph.setEdge(j, i, CityNode.getDistance(arrCities.get(j), arrCities.get(i)));
                    } else {
                        sGraph.setEdge(i, j, Double.MAX_VALUE);
                        sGraph.setEdge(j, i, Double.MAX_VALUE);
                    }
                }
            }
        }
        sGraph.setMark(sGraph.getLastNode(), LAST_VISIT);
        sGraph.constructShortestPath();
    }

    /*
     * Documentation for initStatics()
     * This function simply initializes the 2d arrays used for Q-Learning
     * Reward of edge (i, j), initially -w(i,j)/pv
     */
    static void initStatics() {
        statesCt = sGraph.n();
        Q = new double[statesCt][statesCt];
        R = new double[statesCt][statesCt];

        for (int i = 0; i < statesCt; i++)
            for (int j = 0; j < statesCt; j++) {
                R[i][j] = sGraph.weight(i, j) / sGraph.getPrize(j) * -1;
                Q[i][j] = (sGraph.getPrize(i) + sGraph.getPrize(j)) / sGraph.weight(i, j);
            }
    }

    /*
     * Documentation for learnQ()
     * learnQ() is the primary function for the program
     *
     * (1) All the m agents are initially located at the starting node s with zero
     * collected prizes.
     * [line 236-242]
     *
     * (2) Then each independently follows the action rule to move to the next node
     * to collect prizes and
     * collaboratively updates the Q-value.
     * [line 252-262]
     *
     * (3) When an agent can no longer find a feasible unvisited node to move to due
     * to its insufficient budget,
     * it terminates and goes to t.
     * [line 246]
     *
     * (4) Otherwise, it moves to the next node and collects the prize and continues
     * the prize-collecting process.
     * [line 248-265]
     *
     * (5) Then, it finds among the m routes the one with the maximum collected
     * prizes and updates the
     * reward value and Q-value of the edges that belong to this route.
     * [line 269-281]
     */
    static void learnQ() {
        for (int i = 0; i < TRIALS; i++) {
            // for every episode, change learning rate and discount factor and epsilon?
            // setHypers(i);
            Agent[] aList = new Agent[NUM_AGENTS];
            for (int j = 0; j < NUM_AGENTS; j++) {
                Graph newGraph = new Graph(sGraph);
                Agent a = new Agent(statesCt, budget, newGraph);
                aList[j] = a;
            }

            while (!allQuotaMet(aList)) {
                for (int j = 0; j < NUM_AGENTS; j++) {
                    Agent aj = aList[j];

                    if (!aj.isDone) {
                        int nextState = getNextStateFromCurState(aj, aj.curState, 1 - q0 * (TRIALS - i) / TRIALS);
                        if (nextState == aj.getLastNode()) {
                            aj.isDone = true;
                        }
                        double maxQ = maxQ(aj, nextState);
                        Q[aj.curState][nextState] = (1 - alpha) * Q[aj.curState][nextState] + alpha * gamma * maxQ;
                        aj.indexPath.add(nextState);
                        aj.total_wt += aj.shortestPath(aj.curState, nextState);
                        // if(nextState!=0 && nextState!=aj.getLastNode()){
                        aj.total_prize += aj.getTotalPrize(nextState);
                        // }

                        aj.setAgentMark(nextState, VISITED);
                        aj.curState = nextState;
                    }
                }
            }

            int mostFitIndex = findHighestPrize(aList);

            Agent jStar = aList[mostFitIndex];
            ArrayList<Integer> path = jStar.indexPath;
            jStar.resetAgentMarks();

            for (int v = 0; v < path.size() - 1; v++) {
                double q = Q[path.get(v)][path.get(v + 1)];
                double maxQ = maxQ(jStar, path.get(v + 1));
                R[path.get(v)][path.get(v + 1)] += (W / jStar.total_prize);
                Q[path.get(v)][path.get(v + 1)] = (1 - alpha) * q
                        + alpha * (R[path.get(v)][path.get(v + 1)] + gamma * maxQ);
            }
        }
    }

    private static void setHypers(int i) {
        alpha = 1 - (i / TRIALS);
        // gamma = i/TRIALS;
        q0 = 1 - (i / TRIALS);
    }

    /*
     * Documentation for allQuotaMet()
     * This function checks if every agent has no extra budget to travel other than
     * goes to the destination
     * if every agent in aList does, return true, and break out of while loop. else,
     * return false, while loop continues
     */
    static boolean allQuotaMet(Agent[] aList) {
        int trueCounter = 0;

        for (int i = 0; i < aList.length; i++)
            if (aList[i].isDone)
                trueCounter++;

        if (trueCounter == aList.length)
            return true;
        else
            return false;
    }

    /*
     * Documentation for findHighestPrize
     * This function find the agent in the list with the highest prize collected
     * it then returns its index, and becomes agent jStar
     */
    static int findHighestPrize(Agent[] aList) {
        double runningHigh = Double.NEGATIVE_INFINITY;
        int index = 0;
        for (int j = 0; j < aList.length; j++) {
            if (aList[j].total_prize > runningHigh) {
                runningHigh = aList[j].total_prize;
                index = j;
            }
        }
        return index;
    }

    /*
     * Documentation for reset()
     * resets pertinent variables and graph flags for use in final traversal (see
     * traverse())
     */
    static void reset() {
        total_wt = 0;
        total_prize = 0;
        remainingBudget = 0.0;
        route = new ArrayList<>();
        for (int i = 0; i < statesCt; i++)
            sGraph.setMark(i, UNVISITED);
        sGraph.setMark(sGraph.getLastNode(), LAST_VISIT);
    }

    /*
     * Documentation for getNextStateFromCurState()
     * This function returns the next state for a specific agent according to action
     * rule in BC-PC-TSP
     * The q0 passed to this function will change according to different trails,
     * from small to large
     * At the very end, we won't do exploration at all
     */
    static int getNextStateFromCurState(Agent aj, int s, double q0) {
        ArrayList<Integer> feasible = new ArrayList<>();
        for (int i = 1; i < aj.getLastNode(); i++) {
            if (aj.getMark(i) == UNVISITED
                    && aj.shortestPath(s, i) + aj.shortestPath(i, aj.getLastNode()) < aj.budget - aj.total_wt) {
                feasible.add(i);
            }
        }

        if (feasible.size() == 0) {
            return aj.getLastNode();
        } else {
            Random rand = new Random();
            if (rand.nextDouble() > q0) {
                // Exploration
                double[] prob = new double[feasible.size()];
                double total = 0;
                for (int i = 0; i < feasible.size(); i++) {
                    int u = feasible.get(i);
                    prob[i] = Math.pow(Q[s][u], delta) * aj.getPrize(u) / Math.pow(aj.weight(s, u), beta);
                    total += prob[i];
                }
                // uniform the distribution
                for (int i = 0; i < feasible.size(); i++) {
                    prob[i] /= total;
                }

                double target = rand.nextDouble();
                int idx = -1;
                while (target > 0) {
                    idx++;
                    target -= prob[idx];
                }
                return feasible.get(idx);
            } else {
                // Exploitation
                int maxIdx = -1;
                double curMax = Double.NEGATIVE_INFINITY;
                for (int i = 0; i < feasible.size(); i++) {
                    int u = feasible.get(i);
                    double val = Math.pow(Q[s][u], delta) * aj.getPrize(u) / Math.pow(aj.weight(s, u), beta);
                    if (val > curMax) {
                        maxIdx = i;
                        curMax = val;
                    }
                }
                return feasible.get(maxIdx);
            }
        }
    }

    /*
     * Documentation for maxQ
     * This function will find the max Q value among all the unvisited states within
     * the feasible set
     */
    static double maxQ(Agent aj, int nextState) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < statesCt; i++)
            if (aj.shortestPath(nextState, i) != 0 && aj.getMark(i) == UNVISITED)
                result.add(i);
        int[] possibleActions = result.stream().mapToInt(i -> i).toArray();

        // the learning rate and eagerness will keep the W value above the lowest reward
        double maxValue = 0;
        for (int nextAction : possibleActions) {
            double value = Q[nextState][nextAction];

            if (value > maxValue)
                maxValue = value;
        }
        return maxValue;
    }

    /*
     * Documentation for getFeasibleSet
     * Given the current node s wherein the traveling salesman is located and his
     * current available budget B,
     * the set of s’s neighbor nodes that the salesman can travel to while still
     * having enough budgets to go to
     * destination node t is called node s's feasible set.
     * This function will be used across all algorithms
     */
    private static ArrayList<Integer> getFeasibleSet(int s) {
        ArrayList<Integer> feasible = new ArrayList<>();
        for (int i = 1; i < sGraph.getLastNode(); i++) {
            if (sGraph.getMark(i) == UNVISITED &&
                    sGraph.shortestPath(s, i) + sGraph.shortestPath(i, sGraph.getLastNode()) < budget - total_wt) {
                feasible.add(i);
            }
        }
        return feasible;
    }

    /*
     * Documentation for traverse() and DFSGreed_P()
     * This function handles the final traversal using the Q-Table as reference
     * DFSGreed_P finds the index of the largest Q-Value in the table for the
     * current node
     * then visits it, and repeats until the prizeGoal is met.
     * Includes printouts for debugging
     */
    private static void traverseQ() {
        reset();
        DFSGreed_Q();
        remainingBudget = budget - total_wt; // updates remaining budget
    }

    static void DFSGreed_Q() {
        int curState = 0;
        sGraph.setMark(curState, VISITED);
        route.add(curState);
        while (curState != sGraph.getLastNode()) {
            int nexState = getHighestQ(curState);
            if (nexState == -1) {
                nexState = sGraph.getLastNode();
                if (total_wt + sGraph.weight(curState, nexState) > budget) {
                    // early return if we don't have enough budget to go to the end
                    break;
                }
            }
            sGraph.setMark(nexState, VISITED);
            if (nexState != sGraph.getLastNode()) {
                System.out.printf(
                        "Going from %-18s to %-18s was %-5.2fmiles collecting $%-5d with a ratio of $%.7f/miles\n",
                        sGraph.getName(curState), sGraph.getName(nexState),
                        sGraph.shortestPath(curState, nexState),
                        sGraph.getPrize(nexState),
                        sGraph.getPrize(nexState) / sGraph.shortestPath(curState, nexState));
            } else {
                System.out.printf(
                        "Going from %-18s to %-18s was %-5.2fmiles\n",
                        sGraph.getName(curState), sGraph.getName(nexState),
                        sGraph.shortestPath(curState, nexState));
            }
            sGraph.printExtraPathIfNeeded(curState, nexState, route);
            total_wt += sGraph.shortestPath(curState, nexState);
            curState = nexState;
        }
        total_prize = 0;
        for (int city : route) {
            if (sGraph.getMark(city) != 42) {
                sGraph.setMark(city, 42);
                // if( arrCities.get(city).name!=begin){ //change made by chris for final prize
                // calculation for output
                total_prize += sGraph.getPrize(city);
                // }

            }
        }
    }

    private static void traverseR() {
        reset();
        int r = 0; // current city
        boolean flag = true;
        ArrayList<Integer> sortedNodes = new ArrayList<>();
        for (int i = 1; i < sGraph.getLastNode(); i++) {
            // ignoring first and last cities since they are beg/end
            // we will sort it later inside the while loop
            sortedNodes.add(i);
        }

        route.add(r);
        while (budget > total_wt && flag) {
            final int rFinal = r;
            // sort all nodes in descending order of their prizes cost ratio
            sortedNodes.sort(
                    (o1, o2) -> Double.compare(
                            sGraph.getPrize(o2) / sGraph.shortestPath(o2, rFinal),
                            sGraph.getPrize(o1) / sGraph.shortestPath(o1, rFinal)));
            int k;
            for (k = 0; k < sortedNodes.size(); k++) {
                int maxPrizeRatioCity = sortedNodes.get(k);

                if (getFeasibleSet(r).contains(maxPrizeRatioCity)) {
                    double ratio = sGraph.getPrize(maxPrizeRatioCity) / sGraph.shortestPath(r, maxPrizeRatioCity);

                    System.out.printf(
                            "Going from %-18s to %-18s was %-5.2fmiles collecting $%-5d with a ratio of $%.7f/miles\n",
                            sGraph.getName(r), sGraph.getName(maxPrizeRatioCity),
                            sGraph.shortestPath(r, maxPrizeRatioCity), sGraph.getPrize(maxPrizeRatioCity),
                            ratio);
                    sGraph.printExtraPathIfNeeded(r, maxPrizeRatioCity, route);
                    total_wt += sGraph.shortestPath(r, maxPrizeRatioCity);
                    r = maxPrizeRatioCity;
                    sGraph.setMark(maxPrizeRatioCity, VISITED);
                    break;
                }
            }
            if (k == sortedNodes.size())
                flag = false;
        }
        if (budget >= total_wt + sGraph.shortestPath(r, sGraph.getLastNode())) {
            System.out.printf(
                    "Going from %-18s to %-18s was %-5.2f miles\n",
                    sGraph.getName(r), sGraph.getName(sGraph.getLastNode()),
                    sGraph.shortestPath(r, sGraph.getLastNode()));
            sGraph.printExtraPathIfNeeded(r, sGraph.getLastNode(), route);
            total_wt += sGraph.shortestPath(r, sGraph.getLastNode());
        }
        remainingBudget = budget - total_wt; // updates remaining budget
        total_prize = 0;
        for (int city : route) {
            if (sGraph.getMark(city) != 42) {
                sGraph.setMark(city, 42);
                total_prize += sGraph.getPrize(city);
            }
        }
    }

    private static void traverseP() {
        reset();
        ArrayList<Integer> sortedNodes = new ArrayList<>();
        for (int i = 1; i < sGraph.getLastNode(); i++) {
            // ignoring first and last cities since they are beg/end
            sortedNodes.add(i);
        }

        // sort all nodes in descending order of their prizes
        sortedNodes.sort(
                Comparator.comparingInt(o -> -sGraph.getPrize(o)));
        int k = 0; // iterator for sortedNode
        int r = 0; // current city
        route.add(r);
        while (total_wt < budget && k < sortedNodes.size()) {
            int maxPrizeCity = sortedNodes.get(k);
            if (getFeasibleSet(r).contains(maxPrizeCity)) {
                System.out.printf(
                        "Going from %-18s to %-18s was %-5.2fmiles collecting $%-5d with a ratio of $%.7f/miles\n",
                        sGraph.getName(r), sGraph.getName(maxPrizeCity),
                        sGraph.shortestPath(r, maxPrizeCity), sGraph.getPrize(maxPrizeCity),
                        sGraph.getPrize(maxPrizeCity) / sGraph.shortestPath(r, maxPrizeCity));
                sGraph.printExtraPathIfNeeded(r, maxPrizeCity, route);
                total_wt += sGraph.shortestPath(r, maxPrizeCity);
                r = maxPrizeCity;
                sGraph.setMark(maxPrizeCity, VISITED);
            }
            k++;
        }
        if (budget >= total_wt + sGraph.shortestPath(r, sGraph.getLastNode())) {
            System.out.printf(
                    "Going from %-18s to %-18s was %-5.2f miles\n",
                    sGraph.getName(r), sGraph.getName(sGraph.getLastNode()),
                    sGraph.shortestPath(r, sGraph.getLastNode()));
            sGraph.printExtraPathIfNeeded(r, sGraph.getLastNode(), route);
            total_wt += sGraph.shortestPath(r, sGraph.getLastNode());
        }
        remainingBudget = budget - total_wt; // updates remaining budget
        total_prize = 0;
        for (int city : route) {
            if (sGraph.getMark(city) != 42) {
                sGraph.setMark(city, 42);
                total_prize += sGraph.getPrize(city);
            }
        }
    }

    /*
     * Documentation for getHighestQ()
     * This function finds the index of the largest Q-Table value for the current
     * node (v) and returns it.
     * It makes sure to exclude VISITED nodes, and its own node.
     */
    private static int getHighestQ(int v) {
        double runningHigh = Double.NEGATIVE_INFINITY;
        int index = -1;
        for (int i = 1; i < sGraph.getLastNode(); i++)
            if (Q[v][i] > runningHigh && sGraph.getMark(i) == UNVISITED
                    && sGraph.shortestPath(v, i) + sGraph.shortestPath(i, sGraph.getLastNode()) < budget - total_wt) {
                runningHigh = Q[v][i];
                index = i;
            }

        return index;
    }
}