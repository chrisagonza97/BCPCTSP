import java.awt.font.TransformAttribute;
import java.io.*;
import java.util.*;

//Justin Ruiz

/*
 * TODO: find balance between user tweaked values to more consistently obtain ratio-optimal results
 */

public class Exploration {
    //meta variable
    static String fileName = "Capital_Cities.txt";

    //CHANGE CITY AND PRIZEGOAL
    static String begin = "albany,ny";
    static String end = "jackson,ms";
    static double budget = 10000;

    //static variables to be tweaked by user
    static int TRIALS = 30000;
    static int NUM_AGENTS = 4;
    static double W = 100.0;      //constant value to update the reward table
    static double alpha = 0.05;   //learning rate
    static double gamma = 0.5;    //discount factor
    static double delta = 1;      //power for Q value
    static double beta = 2;       //power for distance
    static double q0 = 0.5;       //coefficient for exploration and exploitation

    //flags for graph (do not touch)
    static final int UNVISITED = 0;
    static final int VISITED = 1;
    static final int LAST_VISIT = 2;

    //pre-initialization parameters (do not touch)
    static LinkedList<CityNode> arrCities;
    static Graph sGraph;
    static double[][] Q;
    static double[][] R;
    static int statesCt;
    static int total_prize = 0;
    static double total_wt = 0;

    public static double oneRun(int trails, int agents, double a, double g, double q) {
        TRIALS = trails;
        NUM_AGENTS = agents;
        alpha = a;
        gamma = g;
        q0 = q;
        //initialization functions
        initList();
        initGraph();
        initStatics();
        //Q-Learning logic
        learnQ();
        //final traversal using completed Q-Table
        traverseQ();
        System.out.printf("Total Prize: $%d\n", total_prize);
        return total_prize;
    }

    public static void main(String[] args) throws IOException
    {
        int[] trails_arr = {25000, 30000, 35000};
        int[] agents_arr = {4, 5, 6};
        double[] alpha_arr = {0.075, 0.1, 0.125};
        double[] gamma_arr = {0.35, 0.4, 0.45};
        double[] q0_arr = {0.7, 0.8, 0.9, 1.0};
//        int[] trails_arr = {30000};
//        int[] agents_arr = {4};
//        double[] alpha_arr = {0.05, 0.1};
//        double[] gamma_arr = {0.5};
//        double[] q0_arr = {0.8, 1.0};

        double maxAvg = 0;
        int maxT = 0;
        int maxAG = 0;
        double maxA = 0;
        double maxG = 0;
        double maxQ = 0;

        for (int t : trails_arr) {
            for (int ag : agents_arr) {
                for (double a : alpha_arr) {
                    for (double g : gamma_arr) {
                        for (double q : q0_arr) {
                            System.out.printf("Trail %d, Agent %d, Alpha %f, Gamma %f, Q0 %f\n", t, ag, a, g, q);
                            double total = 0;
                            for (int i = 0; i < 10; i++) {
                                total += oneRun(t, ag, a, g, q);
                            }
                            System.out.printf("Average prize: %f\n", total / 10);
                            if (total / 10 > maxAvg) {
                                maxAvg = total/10;
                                maxT = t; maxAG = ag; maxA = a; maxG = g; maxQ = q;
                            }
                        }
                    }
                }
            }
        }

        System.out.printf(
            "Max average prize: %f with parameter Trail %d, Agent %d, Alpha %f, Gamma %f, Q0 %f",
            maxAvg, maxT, maxAG, maxA, maxG, maxQ
        );
    }

    static void print2dArray()
    {
        double[][] arr = sGraph.matrix;

        System.out.printf("\t   ");
        for (int i = 0; i < arr[0].length-1; i++)
        {
            System.out.printf("%18s", sGraph.nodeName[i]);
        }
        System.out.println();
        for (int k = 0; k < arr[0].length-1; k++)
        {
            System.out.printf("%-20s", sGraph.nodeName[k]);
            for (int j = 0; j < arr[0].length-1; j++)
            {
                System.out.printf("%-18.2f", arr[k][j]);
            }
            System.out.println("");
        }
    }

    /*
     * Documentation for initList()
     * (1) attempts to read in file (throws error if file not found)
     * (2) converts text file info into cityNode object and places into array for use in creating graph
     * (3) optional scanner functionality for user inputted begin and end points
     * (4) restructure arrayList to make BEGIN city the first node, and END city the last node
     */
    static void initList()
    {
        //(1)
        File towns = new File("src/" + fileName);
        arrCities = new LinkedList<>();
        ArrayList<String> nameList = new ArrayList<>();

        try
        {
            Scanner scan = new Scanner(towns);
            //(2)
            while (scan.hasNextLine())
            {
                String name = scan.next();
                double lat = scan.nextDouble();
                double lon = scan.nextDouble();
                int pop = scan.nextInt();

                arrCities.add(new CityNode(name, lat, lon, pop));
                nameList.add(name.toLowerCase());
            }
            scan.close();
        }

        catch (FileNotFoundException p)
        {System.out.println("FILE NOT FOUND."); System.exit(0);}

        //(3)
        //Scanner userIn = new Scanner(System.in);
        String startCity, endCity;

        //optional scanner for user input
		/*
		System.out.println("Please enter the name of the starting city:");
		startCity = userIn.nextLine().toLowerCase();
		
		System.out.println("Please enter the name of the ending city:");
		endCity = userIn.nextLine().toLowerCase();
		*/

        startCity = begin.toLowerCase();
        endCity = end.toLowerCase();

        //(4)
        if (nameList.contains(startCity) && nameList.contains(endCity))
        {
            if (startCity.equalsIgnoreCase(endCity))
            {
                int sIndex = nameList.indexOf(startCity);
                CityNode t1 = new CityNode(arrCities.get(sIndex));
                arrCities.remove(sIndex);
                nameList.remove(sIndex);

                arrCities.add(0, t1);
                arrCities.add(t1);
            }
            else
            {
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

        else
        {
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
    static void initGraph()
    {
        sGraph = new Graph();
        int size = arrCities.size();
        sGraph.Init(size);

        for(int i = 0; i < size; i++)
        {
            sGraph.setMark(i, UNVISITED);
            sGraph.setName(i, arrCities.get(i).name);
            sGraph.setPrize(i, arrCities.get(i).pop);
            for(int j = 0 ; j < size; j++)
                sGraph.setEdge(i, j, CityNode.getDistance(arrCities.get(i), arrCities.get(j)));
        }
        sGraph.setMark(sGraph.getLastNode(), LAST_VISIT);
    }

    /*
     * Documentation for initStatics()
     * This function simply initializes the 2d arrays used for Q-Learning
     * Reward of edge (i, j), initially -w(i,j)/pv
     */
    static void initStatics()
    {
        statesCt = sGraph.n();
        Q = new double[statesCt][statesCt];
        R = new double[statesCt][statesCt];

        for (int i = 0; i < statesCt; i++)
            for (int j = 0; j < statesCt; j++)
            {
                R[i][j] = sGraph.weight(i, j)/sGraph.getPrize(j) * -1;
                Q[i][j] = (sGraph.getPrize(i) + sGraph.getPrize(j)) / sGraph.weight(i, j);
            }
    }

    static int algoSelect()
    {
        Scanner userIn = new Scanner(System.in);
        System.out.println("Select algorithm:");
        System.out.println("1) Greedy-R");
        System.out.println("2) Greedy-P");
        System.out.println("3) MARL");

        return userIn.nextInt();
    }

    /*
     * Documentation for learnQ()
     * learnQ() is the primary function for the program
     *
     * (1) Random() is called to provide a random number within bounds of the possibleActionsFromState array.
     *
     * (2) Will run as many trials as indicated from the static variables at the top of the program.
     *
     * (3) Initialize the appropriate number of agents and place them into an array of agents (aList)
     *
     * (4) Checks the allQuotaMet function to decide if all agents have reached their prize goal
     * if not, all agents will step through to a node based on the Q-Learning function until they do
     *
     * (5) Once all agents meet their goal, they will go to the last node in the graph
     * once there, they will add those weight and prize values to the agent,
     * calculate the prize/distance ratio for each agent, and set pertinent flags.
     *
     * (6) mostFitIndex() will find the agent with the highest ratio and set them to jStar.
     * Reward and Q- table will then be updated
     */
    static void learnQ()
    {
        for (int i = 0; i < TRIALS; i++)
        {
            Agent[] aList = new Agent[NUM_AGENTS];
            for (int j = 0; j < NUM_AGENTS; j++)
            {
                Graph newGraph = new Graph(sGraph);
                Agent a = new Agent(statesCt, budget, newGraph);
                aList[j] = a;
            }

            while (!allQuotaMet(aList))
            {
                for (int j = 0; j < NUM_AGENTS; j++)
                {
                    Agent aj = aList[j];

                    if (!aj.isDone) {
                        int nextState = getNextStateFromCurState(aj, aj.curState, 1 - q0 * (TRIALS - i)/ TRIALS);
                        if (nextState == aj.getLastNode()) {
                            aj.isDone = true;
                        } else {
                            double maxQ = maxQ(aj, nextState);
                            Q[aj.curState][nextState] = (1-alpha) * Q[aj.curState][nextState] + alpha * gamma * maxQ;
                            aj.indexPath.add(nextState);
                            aj.total_wt += aj.weight(aj.curState, nextState);
                            aj.total_prize += aj.getPrize(nextState);
                            aj.setAgentMark(nextState, VISITED);
                            aj.curState = nextState;
                        }
                    }
                }
            }

            int mostFitIndex = findHighestPrize(aList);

            Agent jStar = aList[mostFitIndex];
            ArrayList<Integer> path = jStar.indexPath;
            jStar.resetAgentMarks();

            for (int v = 0; v < path.size()-1; v++)
            {
                double q = Q[path.get(v)][path.get(v+1)];
                double maxQ = maxQ(jStar, path.get(v+1));
                R[path.get(v)][path.get(v+1)] += (W/jStar.total_prize);
                Q[path.get(v)][path.get(v+1)] = (1-alpha) * q + alpha * (R[path.get(v)][path.get(v+1)] + gamma * maxQ);
            }
        }
    }

    /*
     * Documentation for allQuotaMet()
     * This function checks if every agent has reached their prelim goal (prizeGoal - prize of last node)
     * if every agent in aList does, return true, and break out of while loop. else, return false, while loop continues
     */
    static boolean allQuotaMet(Agent[] aList)
    {
        int trueCounter = 0;

        for (int i = 0; i < aList.length; i++)
            if (aList[i].isDone) trueCounter++;

        if (trueCounter == aList.length)
            return true;
        else
            return false;
    }

    /*
     * Documentation for findBestRatio
     * This function find the agent in the list with the best prizeGoal/distance ratio
     * it then returns its index, and becomes agent jStar
     */
    static int findHighestPrize(Agent[] aList)
    {
        double runningHigh = Double.NEGATIVE_INFINITY;
        int index = 0;
        for (int j = 0; j < aList.length; j++)
        {
            if (aList[j].total_prize > runningHigh)
            {
                runningHigh = aList[j].total_prize;
                index = j;
            }
        }
        return index;
    }

    /*
     * Documentation for reset()
     * resets pertinent variables and graph flags for use in final traversal (see traverse())
     */
    static void reset()
    {
        total_wt = 0;
        total_prize = 0;
        for (int i = 0; i < statesCt; i++)
            sGraph.setMark(i, UNVISITED);
        sGraph.setMark(sGraph.getLastNode(), LAST_VISIT);
    }

    /*
     * Documentation for possibleActionsFromState()
     * This function returns an arraylist of all nodes an agent can currently visit from the current node
     * This does not include its own node, or previously visited nodes
     */
    static int getNextStateFromCurState(Agent aj, int s, double q0)
    {
        ArrayList<Integer> feasible = new ArrayList<>();
        for (int i = 1; i < aj.getLastNode(); i++) {
            if (aj.getMark(i) == UNVISITED && aj.weight(s, i) + aj.weight(i, aj.getLastNode()) < aj.budget - aj.total_wt) {
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
                    prob[i] = Math.pow(Q[s][u], delta) * aj.getPrize(u) / Math.pow(aj.weight(s,u), beta);
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
                    double val = Math.pow(Q[s][u], delta) * aj.getPrize(u) / Math.pow(aj.weight(s,u), beta);
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
     * This function should limit the growth of Q-Values, unsure if working properly but no issues thus far
     */
    static double maxQ(Agent aj, int nextState)
    {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < statesCt; i++)
            if (aj.weight(nextState, i) != 0 && aj.getMark(i) == UNVISITED)
                result.add(i);
        int[] possibleActions = result.stream().mapToInt(i -> i).toArray();

        //the learning rate and eagerness will keep the W value above the lowest reward
        double maxValue = Double.NEGATIVE_INFINITY;
        for (int nextAction : possibleActions)
        {
            double value = Q[nextState][nextAction];

            if (value > maxValue)
                maxValue = value;
        }
        return maxValue;
    }

    /*
     * Documentation for printQ
     * Simply prints the completed Q-Table values
     */
    static void printQ() {
        System.out.println("Q matrix");
        for (int i = 0; i < Q.length; i++) {
            System.out.print("From state " + i + ":  ");
            for (int j = 0; j < Q[i].length; j++) {
                System.out.printf("%-15.2f ", (Q[i][j]));
            }
            System.out.println("\n");
        }
    }

    /*
     * Documentation for traverse() and DFSGreed_P()
     * This function handles the final traversal using the Q-Table as reference
     * DFSGreed_P finds the index of the largest Q-Value in the table for the current node
     * then visits it, and repeats until the prizeGoal is met.
     * Includes printouts for debugging
     */
    private static void traverseQ()
    {
        reset();
        DFSGreed_Q();
    }

    static void DFSGreed_Q()
    {
        int curState = 0;
        sGraph.setMark(curState, VISITED);
        while (curState != sGraph.getLastNode()) {
            int nexState = getHighestQ(curState);
            if (nexState == -1) {
                nexState = sGraph.getLastNode();
            }
            sGraph.setMark(nexState, VISITED);
//            System.out.printf("Going from %-18s to %-18s was %-5.2fkm collecting $%-5d with a ratio of $%.7f/km\n", sGraph.getName(curState), sGraph.getName(nexState), sGraph.weight(curState, nexState), sGraph.getPrize(nexState), sGraph.getPrize(nexState)/sGraph.weight(curState, nexState));
            total_wt += sGraph.weight(curState, nexState);
            total_prize += sGraph.getPrize(nexState);
            curState = nexState;
        }
    }

    private static ArrayList<Integer> getFeasibleSet(int s) {
        ArrayList<Integer> feasible = new ArrayList<>();
        for (int i = 1; i < sGraph.getLastNode(); i++) {
            if (sGraph.getMark(i) == UNVISITED && sGraph.weight(s, i) + sGraph.weight(i, sGraph.getLastNode()) < budget - total_wt) {
                feasible.add(i);
            }
        }
        return feasible;
    }

    private static void traverseR()
    {
        reset();
        int r = 0; // current city
        boolean flag = true;
        ArrayList<Integer> sortedNodes = new ArrayList<>();
        for (int i = 1; i < sGraph.getLastNode(); i++) {
            // ignoring first and last cities since they are beg/end
            // we will sort it later inside the while loop
            sortedNodes.add(i);
        }

        while (budget > total_wt && flag) {
            final int rFinal = r;
            // sort all nodes in descending order of their prizes cost ratio
            sortedNodes.sort(
                (o1, o2) ->
                    Double.compare(
                        sGraph.getPrize(o2) / sGraph.weight(o2, rFinal), sGraph.getPrize(o1) / sGraph.weight(o1, rFinal)
                    )
            );
            int k;
            for (k = 0; k < sortedNodes.size(); k++) {
                int maxPrizeRatioCity = sortedNodes.get(k);
                if (getFeasibleSet(r).contains(maxPrizeRatioCity)) {
                    System.out.printf(
                        "Going from %-18s to %-18s was %-5.2fkm collecting $%-5d with a ratio of $%.7f/km\n",
                        sGraph.getName(r), sGraph.getName(maxPrizeRatioCity),
                        sGraph.weight(r, maxPrizeRatioCity), sGraph.getPrize(maxPrizeRatioCity),
                        sGraph.getPrize(maxPrizeRatioCity)/sGraph.weight(r, maxPrizeRatioCity)
                    );
                    total_wt += sGraph.weight(r, maxPrizeRatioCity);
                    total_prize += sGraph.getPrize(maxPrizeRatioCity);
                    r = maxPrizeRatioCity;
                    sGraph.setMark(maxPrizeRatioCity, VISITED);
                    break;
                }
            }
            if (k == sortedNodes.size()) flag = false;
        }

        total_wt += sGraph.weight(r, sGraph.getLastNode());
        total_prize += sGraph.getPrize(sGraph.getLastNode());
    }

    private static void traverseP()
    {
        reset();
        ArrayList<Integer> sortedNodes = new ArrayList<>();
        for (int i = 1; i < sGraph.getLastNode(); i++) {
            // ignoring first and last cities since they are beg/end
            sortedNodes.add(i);
        }

        // sort all nodes in descending order of their prizes
        sortedNodes.sort(
            Comparator.comparingInt(o -> -sGraph.getPrize(o))
        );
        int k = 0; // iterator for sortedNode
        int r = 0; // current city
        while (total_wt < budget && k < sortedNodes.size()) {
            int maxPrizeCity = sortedNodes.get(k);
            if (getFeasibleSet(r).contains(maxPrizeCity)) {
                System.out.printf(
                    "Going from %-18s to %-18s was %-5.2fkm collecting $%-5d with a ratio of $%.7f/km\n",
                    sGraph.getName(r), sGraph.getName(maxPrizeCity),
                    sGraph.weight(r, maxPrizeCity), sGraph.getPrize(maxPrizeCity),
                    sGraph.getPrize(maxPrizeCity)/sGraph.weight(r, maxPrizeCity)
                );
                total_wt += sGraph.weight(r, maxPrizeCity);
                total_prize += sGraph.getPrize(maxPrizeCity);
                r = maxPrizeCity;
                sGraph.setMark(maxPrizeCity, VISITED);
            }
            k++;
        }
        total_wt += sGraph.weight(r, sGraph.getLastNode());
        total_prize += sGraph.getPrize(sGraph.getLastNode());
    }

    /*
     * Documentation for getHighestQ()
     * This function finds the index of the largest Q-Table value for the current node (v) and returns it.
     * It makes sure to exclude VISITED nodes, and its own node.
     */
    private static int getHighestQ(int v)
    {
        double runningHigh = Double.NEGATIVE_INFINITY;
        int index = -1;
        for (int i = 1; i < sGraph.getLastNode(); i++)
            if (Q[v][i] > runningHigh && sGraph.getMark(i) == UNVISITED && sGraph.weight(v, i) + sGraph.weight(i, sGraph.getLastNode()) < budget - total_wt)
            {
                runningHigh = Q[v][i];
                index = i;
            }

        return index;
    }
}