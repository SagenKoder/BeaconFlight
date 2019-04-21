package app.sagen.beaconflight.node;

public class Main {

    public static void main(String[] args) {
        Graph.Vertex vertex1 = new Graph.Vertex("Start", 0, 0, 0);
        Graph.Vertex vertex2 = new Graph.Vertex("Middle1", 5, 0, 0);
        Graph.Vertex vertex3 = new Graph.Vertex("Middle2", 6, 0, 0);
        Graph.Vertex vertex4 = new Graph.Vertex("Middle3", 7, 10, 0);
        Graph.Vertex vertex5 = new Graph.Vertex("Middle4", 7, 1, 0);
        Graph.Vertex vertex6 = new Graph.Vertex("Middle5", 7, 2, 0);
        Graph.Vertex vertex7 = new Graph.Vertex("Middle6", 7, 3, 0);
        Graph.Vertex vertex8 = new Graph.Vertex("Middle7", 7, 4, 0);
        Graph.Vertex vertex9 = new Graph.Vertex("End", 10, 0, 0);

        Graph graph = new Graph();
        graph.addVertex(vertex1);
        graph.addVertex(vertex2);
        graph.addVertex(vertex3);
        graph.addVertex(vertex4);
        graph.addVertex(vertex5);
        graph.addVertex(vertex6);
        graph.addVertex(vertex7);
        graph.addVertex(vertex8);
        graph.addVertex(vertex9);
        graph.addEdge(vertex1, vertex2);
        graph.addEdge(vertex2, vertex3);
        graph.addEdge(vertex3, vertex4);
        graph.addEdge(vertex3, vertex5);
        graph.addEdge(vertex3, vertex6);
        graph.addEdge(vertex3, vertex7);
        graph.addEdge(vertex3, vertex8);
        graph.addEdge(vertex4, vertex9);
        System.out.println(graph.shortestPath(vertex1, vertex9));
    }

}
