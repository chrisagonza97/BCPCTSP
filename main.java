import java.io.*;
import java.util.*;

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
    static final int TRIALS = 4000;
    static final int NUM_AGENTS = 5;
    static final double W = 100.0; // constant value to update the reward table
    static double alpha = 0.125; // .125 learning rate
    static double gamma = 0.35; // .35 discount factor
    static final double delta = 1; // power for Q value
    static final double beta = 2; // power for distance
    static double q0 = 0.8; // coefficient for exploration and exploitation

    // **************************** greedy epsilon
    static double explor_rate = 1.0;  // default 1.0
    static double max_explor = 1.0;  // default 1.0
    static double min_explore = 0.01;  // default 0.01
    static double decay_rate = 0.01;  // default 0.01
    // *************************************

    // flags for graph (do not touch)
    static final int UNVISITED = 0;
    static final int VISITED = 1;
    static final int LAST_VISIT = 2;

    // pre-initialization parameters (do not touch)
    static LinkedList<CityNode> arrCities;
    static ArrayList<String> nameList;
    static ArrayList<String> allNameList;
    static Graph sGraph;
    static double[][] Q;
    static double[][] R;
    static double[][] C;
    static int statesCt;
    static int total_prize = 0;
    static double total_wt = 0;
    static ArrayList<Integer> route;
    static ArrayList<Integer> routeMax;// keep track of best case
    static int prizeMax = Integer.MIN_VALUE;// keep track of best case
    static int episodeMax = 0;// keep track of episode of best case
    // Create a HashMap with Integer keys and String array values
    static HashMap<Integer, ArrayList<Integer>> listMax = new HashMap<>();

    // index of cities
    static int endCity;
    static int beginCity;
    static int maxCount;
    static int episodeCount;

    // variables for extra credits
    static long randomSeed = 12345; // random seed for inaccessible edge, can change to any long
    static double missingProb = 0.0; // probability of an inaccessible edge

    public static void main(String[] args) throws IOException {
        // variables for time tracking
        long startTime, endTime;
        double totalTime;

//        askForUserInputs();


//        System.out.println("========== First greedy algorithm ==========");
//        initList();
//        initGraph();
//        startTime = System.nanoTime();
//        traverseP();
//        endTime = System.nanoTime();
//        totalTime = (double) (endTime - startTime) / 1000000;
//        System.out.println("Algorithm took " + totalTime + "ms to process.");
//        System.out.printf("\nTotal distance: %6.2f miles", total_wt);
//        // System.out.printf("\nRemaining distance: %6.2fkm", budget - total_wt);
//        System.out.printf("\nCollected Prize: $%d", (total_prize - arrCities.get(0).pop * 2));
//        System.out.printf("\nRoute: %s\n", route.toString());
//        System.out.printf("Remaining Budget: %.2f miles\n", remainingBudget);
//
//        System.out.println("\n========== Second greedy algorithm ==========");
//        initList();
//        initGraph();
//        startTime = System.nanoTime();
//        traverseR();
//        endTime = System.nanoTime();
//        totalTime = (double) (endTime - startTime) / 1000000;
//        System.out.println("Algorithm took " + totalTime + "ms to process.");
//        System.out.printf("\nTotal distance: %6.2f miles", total_wt);
//        // System.out.printf("\nRemaining distance: %6.2fkm", budget - total_wt);
//        System.out.printf("\nCollected Prize: $%d", total_prize - arrCities.get(0).pop * 2);
//        System.out.printf("\nRoute: %s\n", route.toString());
//        System.out.printf("Remaining Budget: %.2f miles\n", remainingBudget);

        System.out.println("\n========== BC-PC-TSP MARL algorithm ==========");
        String[] cityList = {"Albany,NY","Annapolis,MD","Atlanta,GA","Augusta,ME", "Austin,TX","BatonRouge,LA",
                "Bismarck,ND", "Boise,ID", "Boston,MA","CarsonCity,NV"};
//        String[] cityList = {"CarsonCity,NV"};
        budget = 6000;
        System.out.println("Budget: "+budget);
        System.out.println("Episodes: "+TRIALS);
        System.out.println("Agents: "+NUM_AGENTS);

        for(int i = 0;i < cityList.length;i++){
            System.out.println("=================  Start and End City:  " + cityList[i]+ "  ===================\n");
            begin = cityList[i];
            end = cityList[i];
            
            total_prize = 0;
            total_wt = 0;

            prizeMax = Integer.MIN_VALUE;// keep track of best case
            episodeMax = 0;// keep track of episode of best case

            initList();
            initGraph();
            initStatics();
            startTime = System.nanoTime();
//        printIlpArrays();
//        printR("before-qtable");
            learnQ();
//        printR("true-qtable");
            traverseQ();
            endTime = System.nanoTime();


            totalTime = (double) (endTime - startTime) / 1000000;
//        System.out.println("Algorithm took " + totalTime + "ms to process.");

            // old !!!!!!!!!!!!!!!!!!!!!!
//        System.out.printf("\nTotal distance: %6.2f Miles", total_wt);
//        // System.out.printf("\nRemaining distance: %6.2fkm", budget - total_wt);
//        System.out.printf("\nCollected Prize: $%d", total_prize - arrCities.get(0).pop * 2);
//        System.out.printf("\nRoute: %s\n", makeRouteString());
//        System.out.printf("Remaining Budget: %.2f Miles\n", remainingBudget);
            //!!!!!!!!!!!!!!!

            System.out.println();
            System.out.println("Value of P^m: " + (prizeMax - arrCities.get(0).pop));
//        System.out.println("R^m Route: " + arrCities.get(0).name + " " + makeRouteString(true));
            System.out.println("R^m Route: " + makeRouteString(true));
            System.out.println("Episode P^m and R^m: " + episodeMax + "\n\n");


        }



        int[][] twoDArray = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

        // Print the 2D array
//        for (int i = 0; i < Q.length; i++) {
//            for (int j = 0; j < Q[i].length; j++) {
//                System.out.print(Q[i][j] + " ");
//            }
//            System.out.println(); // Move to the next line after printing each row
//        }
    }

    public static void printR(String text){
        // CSV file path
        String csvFilePath = text + "_output.csv";

        // Write the 2D array to the CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            for (int i = 0; i < Q.length; i++) {
                for (int j = 0; j < Q[i].length; j++) {
                    writer.write(String.valueOf(Q[i][j]));

                    // Add a comma if it's not the last element in the row
                    if (j < Q[i].length - 1) {
                        writer.write(",");
                    }
                }
                // Move to the next line after each row
                writer.newLine();
            }

            System.out.println("2D array has been written to " + csvFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String makeRouteString() {
        String ret = "";
        for (int i = 0; i <route.size();i++){
            ret+=arrCities.get(route.get(i)).name;
            if(i!=route.size()-1){
                ret+="("+arrCities.get(route.get(i)).originalIndex+")";
            }else{
                ret+="("+arrCities.get(route.get(i)).originalIndex+")";
            }

            if(i!=route.size()-1){
                ret+=", ";
            }

        }
        return ret;
    }
    private static String routeString(ArrayList<Integer> routeArray) {
        String ret = "";
        System.out.println("reading route" + routeArray.get(0));

        for (int i = 0; i <routeArray.size();i++){
            ret+=arrCities.get(routeArray.get(i)).name;
            ret+="("+routeArray.get(i)+")";


            if(i!=routeArray.size()-1){
                ret+=", ";
            }

        }
        return ret;
    }
    private static String makeRouteString(boolean value) {
        String ret = "";
        for (int i = 0; i <routeMax.size();i++){
            ret+=arrCities.get(routeMax.get(i)).name;
            if(i!=routeMax.size()-1){
                ret+="("+arrCities.get(routeMax.get(i)).originalIndex+")";
            }else{
                ret+="("+arrCities.get(routeMax.get(i)).originalIndex+")";
            }

            if(i!=routeMax.size()-1){
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
    }

    static void askForUserInputs() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the start city: ");
        begin = "CarsonCity,NV";
//        begin = scanner.nextLine();
        System.out.print("Enter the end city: ");
        end = "CarsonCity,NV";
//        end = scanner.nextLine();
        System.out.print("Enter the budget in miles: ");
        budget = 6000;
//        budget = scanner.nextInt();
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
        allNameList = new ArrayList<>();

        try {
            Scanner scan = new Scanner(towns);
            // (2)
            int originIndex=0;
            while (scan.hasNextLine()) {
                String name = scan.next();
                double lat = scan.nextDouble();
                double lon = scan.nextDouble();
                int pop = scan.nextInt();

                arrCities.add(new CityNode(name, lat, lon, pop));
                nameList.add(name.toLowerCase());
                allNameList.add(name.toLowerCase());

                arrCities.get(arrCities.size()-1).originalIndex=originIndex;
                originIndex++;
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
                t1.originalIndex = arrCities.get(sIndex).originalIndex;
                arrCities.remove(sIndex);
                nameList.remove(sIndex);

                arrCities.add(0, t1);
                arrCities.add(t1);
            } else {
                int sIndex = nameList.indexOf(startCity);
                CityNode t1 = new CityNode(arrCities.get(sIndex));
                t1.originalIndex = arrCities.get(sIndex).originalIndex;
                arrCities.remove(sIndex);
                nameList.remove(sIndex);

                int fIndex = nameList.indexOf(endCity);
                CityNode t2 = new CityNode(arrCities.get(fIndex));
                t2.originalIndex = arrCities.get(fIndex).originalIndex;
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
        statesCt = sGraph.n()-1;
        Q = new double[statesCt][statesCt];
        R = new double[statesCt][statesCt];
        C = new double[statesCt][statesCt];

        for (int i = 0; i < statesCt; i++)
            for (int j = 0; j < statesCt; j++) {
                if(i==j){
                    R[i][j] = -999999;
                    Q[i][j] = -9999999;
                    C[i][j] = -9999999;
                } else {
//                    R[i][j] = sGraph.weight(i, j) / sGraph.getPrize(j) * -1;
                    R[i][j] = 0;
//                    Q[i][j] = (sGraph.getPrize(i) + sGraph.getPrize(j)) / sGraph.weight(i, j);
                    C[i][j] = (sGraph.getPrize(i) + sGraph.getPrize(j)) / sGraph.weight(i, j);
//                    Q[i][j] =  sGraph.getPrize(j) / sGraph.weight(i, j);
                Q[i][j] = 0;
                }

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
            //setHypers(i);
            Agent[] aList = new Agent[NUM_AGENTS];
            for (int j = 0; j < NUM_AGENTS; j++) {
                Graph newGraph = new Graph(sGraph);
                Agent a = new Agent(statesCt, budget, newGraph);
                aList[j] = a;
            }


            maxCount = 0;
            while (!allQuotaMet(aList)) {
                for (int j = 0; j < NUM_AGENTS; j++) {
                    Agent aj = aList[j];

                    if (!aj.isDone) {

                        int nextState = getNextStateFromCurState(aj, aj.curState, 1 - q0 * (TRIALS - i) / TRIALS);
//                        System.out.println(nextState);
                        if (nextState == aj.getLastNode()) {
                            aj.isDone = true;
                        }

                        // updating Q-Table
//                        double maxQ = maxQ(aj, nextState);
//                        double maxQ = findFeasibleMaxQ(nextState, aList[j]);

//                        Q[aj.curState][nextState] = (1 - alpha) * Q[aj.curState][nextState] + alpha * gamma * maxQ;


                        // updating current agent
                        aj.indexPath.add(nextState);
                        aj.total_wt += aj.shortestPath(aj.curState, nextState);
                        aj.total_prize += aj.getTotalPrize(nextState);
                        aj.setAgentMark(nextState, VISITED);
                        aj.curState = nextState;
                    }
                }
            }

            int mostFitIndex = findHighestPrize(aList);

            Agent jStar = aList[mostFitIndex];
            ArrayList<Integer> path = jStar.indexPath;
            jStar.resetAgentMarks();

            //new new logic:
            listMax.put(i,path);
            int lastDestination = path.getLast();
            path.add(0,lastDestination);

            //new logic: checking if the currently found route is better than previous episodes
            boolean first = false;
            if(aList[mostFitIndex].total_prize > prizeMax){
                prizeMax = aList[mostFitIndex].total_prize;
                routeMax = path;
                episodeMax = i;
                maxCount++;
                first = true;
            }
            double newlyFound = 1;
            if(first){
                newlyFound = episodeMax;
            }
            for (int v = 0; v < routeMax.size()-1; v++) {
                double maxQ = findMaxQ(routeMax.get(v+1)-1);
                R[(routeMax.get(v))-1][(routeMax.get(v + 1))-1] += ((newlyFound * W) / prizeMax);
                double reward = R[(routeMax.get(v))-1][(routeMax.get(v + 1))-1];

                //Q-Table updated
                double beforeQValue = Q[(routeMax.get(v))-1][(routeMax.get(v + 1))-1];
                Q[(routeMax.get(v))-1][(routeMax.get(v + 1))-1] = (1 - alpha) * Q[routeMax.get(v)-1][routeMax.get(v + 1)-1] + alpha *
                        (reward + gamma * maxQ);
//                System.out.println("Before: " + beforeQValue + "    After: " + Q[(routeMax.get(v))-1][(routeMax.get(v + 1))-1]);
            }

        }

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
    static int findFeasibleMaxQ(Agent aj, int s) {
        ArrayList<Integer> feasible = new ArrayList<>();
        for (int i = 1; i < aj.getLastNode(); i++) {
            if (aj.getMark(i) == UNVISITED
                    && aj.shortestPath(s, i) + aj.shortestPath(i, aj.getLastNode()) < aj.budget - aj.total_wt) {
                feasible.add(i);
            }
        }
//        System.out.println("find" + s);
//        System.out.println(feasible);
        if(feasible.isEmpty()){
            return 0;
        }

        return 1;
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
//                System.out.println("Exploration "+ q0);
                double[] prob = new double[feasible.size()];
                double total = 0;
                for (int i = 0; i < feasible.size(); i++) {
                    int u = feasible.get(i);
                    prob[i] = Math.pow(C[s][u], delta) * aj.getPrize(u) / Math.pow(aj.weight(s, u), beta);
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
//                System.out.println("NOT!!!!!!!!!!!!");
//                System.out.println(" _______________Exploitation");
//                int maxIdx = -1;
//                double curMax = Double.NEGATIVE_INFINITY;
//                for (int i = 0; i < feasible.size(); i++) {
//                    int u = feasible.get(i);
//                    double val = Math.pow(Q[s][u], delta) * aj.getPrize(u) / Math.pow(aj.weight(s, u), beta);
//
//                    if (val > curMax) {
//                        maxIdx = i;
//                        curMax = val;
//                    }
//                }
//                ################## MINE !!!!!!!!!!!!!!!!
                double maxNumber = -99999.9;
                int maxIndex = -1;

                // Find the index with the highest number element in each row
                for (int col = 0; col < Q[s].length; col++) {
                    if (Q[s][col] >= maxNumber && feasible.contains(col)) {
                        maxIndex = col;  // Update the index if a higher value is found
                        maxNumber = Q[s][maxIndex];
                    }
                }
//                System.out.println(maxIndex);
                return maxIndex;
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
//        for(int i = 0; i <possibleActions.length ;i++)
//        System.out.println(possibleActions[1]);
        // the learning rate and eagerness will keep the W value above the lowest reward
        double maxValue = 0;

        for (int i = 0; i < possibleActions.length-1; i++) {

//            System.out.println("nextState: " + nextState + " i: " +i );
            double value = Q[nextState-1][i];

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
        reset(); // makes all static variables reset from previous algorithms...
//        DFSGreed_Q(); //
        qTableTotal();
        remainingBudget = budget - total_wt; // updates remaining budget
    }
    static double findMaxQ(int columnIndex) {
        //find max Q from Q-Table
        double max = 0;
        for (int i = 1; i < Q.length; i++) {
            if (Q[i][columnIndex] > max) {
                max = Q[i][columnIndex];
            }
        }
        return max;
    }
    static double findFeasibleMaxQ(int columnIndex, Agent aj) {
        //find all feasible
        //find max feasible
        //return from Q-table: Q[columnIndex, maxFeasible]
        //remember columnIndex need to be -1
        ArrayList<Integer> feasible = new ArrayList<>();
        for (int i = 1; i < aj.getLastNode(); i++) {
            if (aj.getMark(i) == UNVISITED
                    && aj.shortestPath(columnIndex, i) + aj.shortestPath(i, aj.getLastNode()) < aj.budget - aj.total_wt) {
                feasible.add(i);
            }
        }
        double maxValue = -9999.9;
        int maxIndex = -1;
        for (int i = 0;i < feasible.size();i++){
            if(feasible.get(i) > maxValue){
                maxIndex = i;
                maxValue = sGraph.getPrize(feasible.get(i));
            }
        }
        System.out.println("Prize " +   sGraph.getPrize(feasible.get(maxIndex)-1));
        System.out.println("Name " + sGraph.getName(feasible.get(maxIndex)-1));
        System.out.println("find " + columnIndex);
        System.out.println(feasible);
        if (feasible.isEmpty()) {
            feasible = aj.indexPath;

            System.out.println("feasible: " + feasible);

            return 0;
        }
        System.out.println("feasible: " + feasible);
        return Q[columnIndex - 1][0];
    }
    static void qTableTotal() {
        int beginIndex = 0;
        double qTableBudget = budget;
        double qTableReward = 0;
        ArrayList<Integer> qTableRoute = new ArrayList<>();
        ArrayList<Integer> feasible = new ArrayList<>();
        for(int i = 0;i<10;i++){
            feasible.add(i);
        }

        for (int i = 0; i < allNameList.size(); i++) {
            String name = allNameList.get(i);
            if (name.equals(begin.toLowerCase())) {
                beginIndex = i;
            }
        }
//        System.out.println(begin + " index : "+beginIndex);
//        System.out.println(feasible);
//        System.out.println("------------- Q-Table ---------------");
//        System.out.println(qTableBudget);
        qTableRoute.add(beginIndex);
        int cityIndex = beginIndex;
        feasible.remove(cityIndex);
//        System.out.println(feasible);
        //find max Q
        while(!feasible.isEmpty()){

            int maxIndex = -1;
            double maxNumber = 0.0;
            // Find the index with the highest number element in each row
            for (int col = 0; col < Q[cityIndex].length; col++) {
                double home = sGraph.shortestPath(cityIndex+1, col+1) +
                        sGraph.shortestPath(beginIndex+1, col+1);
                // System.out.println("Home " + home);
                if (Q[cityIndex][col] > maxNumber && feasible.contains(col)
                        && qTableBudget >= home) {
                    maxIndex = col;  // Update the index if a higher value is found
                    maxNumber = Q[cityIndex][maxIndex];
                }
            }
//            System.out.println("done!!! " + feasible + "    " + maxIndex + "    " + qTableBudget);
            if(maxIndex == -1){
                break;
            }
//            System.out.println(maxIndex);

            //subtract from budget
//            System.out.printf(
//                    "Going from %-18s to %-18s was %-5.2fmiles collecting $%-5d with a ratio of $%.7f/miles\n",
//                    sGraph.getName(cityIndex+1), sGraph.getName(maxIndex+1),
//                    sGraph.shortestPath(cityIndex+1, maxIndex+1),
//                    sGraph.getPrize(maxIndex+1),
//                    sGraph.getPrize(maxIndex+1) / sGraph.shortestPath(cityIndex+1, maxIndex+1));


            qTableBudget -= sGraph.shortestPath(cityIndex+1, maxIndex+1);
            qTableReward += sGraph.getPrize(maxIndex+1);
            cityIndex = maxIndex;
            for(int i = 0;i < feasible.size();i++) {
                if(feasible.get(i) == maxIndex){
                    feasible.remove(i);
                    qTableRoute.add(maxIndex);
                    break;
                }
//                double home = sGraph.shortestPath(maxIndex+1, i+1) + sGraph.shortestPath(beginIndex+1, i+1)
//                if()

            }

        }
        //get last city before home
        int cityBeforeHome = qTableRoute.getLast();
        //add home back into route
        qTableRoute.add(qTableRoute.get(0));
        //print out last city before home to home
//        System.out.printf(
//                "Going from %-18s to %-18s was %-5.2fmiles.\n",
//                sGraph.getName(cityBeforeHome+1), sGraph.getName(beginCity),
//                sGraph.shortestPath(cityBeforeHome+1, beginCity));

        qTableBudget -= sGraph.shortestPath(cityBeforeHome+1, beginCity);

//        System.out.println("Total distance: " + (budget-qTableBudget));
        System.out.println("Collected Prize: " + qTableReward);
        System.out.println("Route: " + qTableRoute);
//        System.out.println("Remaining Budget: " + qTableBudget);

    }
    static void DFSGreed_Q() {
        //current state zero???
        int curState = 0;

        //setMark to zero??? and visited which is ONE
        sGraph.setMark(curState, VISITED);
        //Route adding current state which is ZERO?
        route.add(curState);
        //sGraph.getLastNode() is which is 10
        System.out.println(sGraph.getLastNode());
        while (curState != sGraph.getLastNode()) {
            //nexState is getHighestQ(curState) which is
            System.out.println(getHighestQ(curState));
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
        // sGraph.getLastNode() is 10
        for (int i = 1; i < sGraph.getLastNode(); i++) {
            //UVISITED??
//            System.out.println("unvistied:" + UNVISITED);
            if (Q[v][i] > runningHigh && sGraph.getMark(i) == UNVISITED
                    && sGraph.shortestPath(v, i) + sGraph.shortestPath(i, sGraph.getLastNode()) < budget - total_wt) {
                runningHigh = Q[v][i];
                index = i;
            }
        }

        return index;
    }
}