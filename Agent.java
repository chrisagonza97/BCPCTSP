import java.util.*;

class Agent 
{
	public int total_prize;
	public int statesCt;
	public int curState;
	public double total_wt;
    public double budget;
    public boolean isDone;
	
	public ArrayList<Integer> indexPath;
	private Graph newGraph;
	public int[] actionsFromCurrentState;
	
	public Agent(int statesCt, double budget, Graph newGraph)
	{
		this.statesCt = statesCt;
		this.newGraph = newGraph;
        this.budget = budget;
		total_prize = 0;
		curState = 0;
		total_wt = 0;
		indexPath = new ArrayList<Integer>();
        isDone = false;
	}
	
	public void setAgentMark(int v, int val)
	{
		this.newGraph.Mark[v] = val;
	}
	
	public double weight(int i, int v)
	{
		return this.newGraph.matrix[i][v];
	}

    public double shortestPath(int i, int v)
    {
        return this.newGraph.shortestPath(i, v);
    }
	
	public int getMark(int v)
	{
		return this.newGraph.Mark[v];
	}
	
	public int getLastNode()
	{
		return (this.newGraph.n()-1);
	}
	
	public int getPrize(int v)
	{
		return this.newGraph.prize[v];
	}

    public int getTotalPrize(int end) {
        int start = curState;
        if (newGraph.shortestNext[start][end] != end) {
            // in the case we will detour
            int total = 0;
            while (start != end) {
                int next = newGraph.shortestNext[start][end];
                if (newGraph.getMark(next) == 0) {
                    total += newGraph.prize[next];
                    this.newGraph.Mark[next] = 1;
                }
                start = next;
            }
            return total;
        } else {
            return newGraph.prize[end];
        }
    }
	
	public void resetAgentMarks() {
        for (int i = 0; i < this.newGraph.n(); i++) {
            this.newGraph.Mark[i] = main.UNVISITED;
        }
    }
}