import java.util.Queue;
import java.util.LinkedList;

public class MinMaxFlow{
    FlowGraph flowGraph = new FlowGraph();
    int [][] residual;
    GraphNode source;
    GraphNode sink;
    final int NO_PREV_PATH = -1;
    int flowIntoSink = 0;
    int totalCost = 0;

    //Constructor
    public MinMaxFlow(String fileName){
        flowGraph.makeGraph(fileName);
        source = flowGraph.G[0];
        sink = flowGraph.G[flowGraph.vertexCt - 1];
        int size = flowGraph.vertexCt;
        residual = new int[size][size];
        for (int i = 0; i < size; i++){
            for (int j = 0; j < size; j++){
                residual[i][j] = 0;
            }
        }
    }

    /**
     * A method to calculate and print the max flow.
     */
    public void printMaxFlow(){
        System.out.println("Flows found for " + flowGraph.graphName);
        findMaxFlow();
        System.out.println(flowGraph.graphName + " Max Flow Space " + flowGraph.maxFlowIntoSink + " assigned " + flowIntoSink);
        updateCost();
        System.out.println("Total cost = " + totalCost);
    }

    /**
     * An internal method to update the flow stored on the residual graph. Prints out the flow where edges are found
     * @param flow The amound of flow you want to push through
     */
    private void updateFlow(int flow){
        GraphNode currNode = sink;
        StringBuilder sb = new StringBuilder();
        sb.append(sink.nodeID);
        int prev = sink.prevNode;
        if (prev == NO_PREV_PATH){
            return;
        }
        while (prev != source.distance){
            sb.insert(0,prev + "->");
            GraphNode prevNode = flowGraph.G[prev];
            residual[currNode.nodeID][prev] = residual[currNode.nodeID][prev] - flow;
            residual[prev][currNode.nodeID] = residual[prev][currNode.nodeID] + flow;
            currNode = prevNode;
            prev = currNode.prevNode;
        }
        sb.insert(0,prev + "->");
        residual[currNode.nodeID][prev] = residual[currNode.nodeID][prev] - flow;
        residual[prev][currNode.nodeID] = residual[prev][currNode.nodeID] + flow;
        flowIntoSink += flow;
        System.out.println("Found Flow " + flow + ": " + sb.toString());

    }

    /**
     * An internal method to update the total cost and print it out
     */
    private void updateCost(){
        for (GraphNode node: flowGraph.G){
            for (EdgeInfo edge: node.succ){
                int from = edge.from;
                int to = edge.to;
                int flowAssigned = residual[from][to];
                if (flowAssigned <= edge.capacity && edge.capacity != 0){
                    totalCost += Math.abs(residual[edge.from][edge.to]) * edge.cost;
                    if (flowAssigned != 0) {
                        System.out.println("Edge " + edge.from + "->" + edge.to + " assigned "
                                + flowAssigned + " of " + edge.capacity + " at cost " + edge.cost);
                    }
                }
            }
        }
    }

    /**
     * A method to find the max flow. Prints out the edges where flow was found.
     */
    public void findMaxFlow(){
        boolean foundShortestPath = true;
        while (foundShortestPath){
            GraphNode currNode = sink;
            foundShortestPath = ShortestPath();
            int prev = sink.prevNode;
            int maxCapacity = GraphNode.INF;
            while (prev >= source.distance){
                GraphNode prevNode = flowGraph.G[prev];
                int currCapacity = prevNode.getCapacity(currNode.nodeID) - residual[prev][currNode.nodeID];
                if (currCapacity < maxCapacity){maxCapacity = currCapacity;}
                currNode = prevNode;
                prev = currNode.prevNode;
            }
            updateFlow(maxCapacity);
        }
    }

    /**
     * Finds the shortest path based on cost in the graph
     * @return True if a path from the source to the sink was found.
     */
    public boolean ShortestPath(){
        Queue<GraphNode> queue = new LinkedList();
        for (GraphNode node : flowGraph.G){
            node.distance = GraphNode.INF;
            node.prevNode = NO_PREV_PATH; //Each node may have previously stored its predecessor
        }
        source.distance = 0;
        queue.add(source);
        while(!queue.isEmpty()){
            GraphNode nodeFrom = queue.remove();
            for (EdgeInfo edge : nodeFrom.succ){
                if (edge.capacity - residual[edge.from][edge.to] <= 0){
                    continue;
                }
                GraphNode nodeTo = flowGraph.G[edge.to];
                if (nodeFrom.distance + edge.cost < nodeTo.distance){
                    nodeTo.distance = nodeFrom.distance + edge.cost;
                    nodeTo.prevNode = nodeFrom.nodeID;
                    queue.add(nodeTo);
                }
            }
        }
        return sink.prevNode >= 0; //Did we find a path to the sink?
    }


    public static void main(String[] args) {
        String[] fileNames = {"group0.txt", "group1.txt", "group4.txt", "group5.txt", "group6.txt",
                "group7.txt" ,"group8.txt", "bellman0.txt"};
        for (String file: fileNames){
            MinMaxFlow minCostMaxFLow = new MinMaxFlow(file);
            minCostMaxFLow.printMaxFlow();
            System.out.println();
        }

    }
}
