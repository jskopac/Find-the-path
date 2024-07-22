
import java.util.*;

public class ListGraph <T> implements Graph<T>{

    private Map<T, Set<Edge<T>>> graphMap = new HashMap<>();

    @Override
    public void add(T node) {
        graphMap.putIfAbsent(node, new HashSet<>());
    }

    @Override
    public void connect(T fromNode, T toNode, String name, int weight) {
        if (!graphMap.containsKey(fromNode) || !graphMap.containsKey(toNode)){
            throw new NoSuchElementException();
        }
        if (weight < 0) {
            throw new IllegalArgumentException();
        }

        Edge<T> edge = getEdgeBetween(fromNode, toNode);
        if (edge != null){
            throw new IllegalStateException();
        }

        Set<Edge<T>> fromNodeEdges = graphMap.get(fromNode);
        Set<Edge<T>> toNodeEdges = graphMap.get(toNode);

        fromNodeEdges.add(new Edge<>(toNode, name, weight));
        toNodeEdges.add(new Edge<>(fromNode, name, weight));
    }

    @Override
    public void setConnectionWeight(T nodeA, T nodeB, int weight) {
        if (weight < 0){
            throw new IllegalArgumentException();
        }

        if (!graphMap.containsKey(nodeA) || !graphMap.containsKey(nodeB)){
            throw new NoSuchElementException();
        }

        Edge<T> edgeAtoB = getEdgeBetween(nodeA, nodeB);
        Edge<T> edgeBtoA = getEdgeBetween(nodeB, nodeA);
        if (edgeAtoB == null || edgeBtoA == null){
            throw new NoSuchElementException();
        }

        edgeAtoB.setWeight(weight);
        edgeBtoA.setWeight(weight);
    }

    @Override
    public Set<T> getNodes() {
        return new HashSet<>(graphMap.keySet());
    }

    @Override
    public Collection<Edge<T>> getEdgesFrom(T node) {
        if (!graphMap.containsKey(node)){
            throw new NoSuchElementException();
        }

        return graphMap.get(node);
    }

    @Override
    public Edge<T> getEdgeBetween(T nodeA, T nodeB) {
        if (!graphMap.containsKey(nodeA) || !graphMap.containsKey(nodeB)){
            throw new NoSuchElementException();
        }

        for (Edge<T> edgeA : graphMap.get(nodeA)){
            for (Edge<T> edgeB : graphMap.get(nodeB)){
                if(edgeA.getWeight() == edgeB.getWeight() && edgeA.getName().equals(edgeB.getName())){
                    return edgeA;
                }
            }
        }
        return null;
    }

    @Override
    public void disconnect(T nodeA, T nodeB) {
        if (!graphMap.containsKey(nodeA) || !graphMap.containsKey(nodeB)){
            throw new NoSuchElementException();
        }

        Edge<T> edgeAtoB = getEdgeBetween(nodeA, nodeB);
        Edge<T> edgeBtoA = getEdgeBetween(nodeB, nodeA);

        if (edgeAtoB == null || edgeBtoA == null){
            throw new IllegalStateException();
        }

        Set<Edge<T>> nodeAEdges = graphMap.get(nodeA);
        Set<Edge<T>> nodeBEdges = graphMap.get(nodeB);

        nodeAEdges.remove(edgeAtoB);
        nodeBEdges.remove(edgeBtoA);
    }

    @Override
    public void remove(T nodeA) {
        if (!graphMap.containsKey(nodeA)){
            throw new NoSuchElementException();
        }

        for(T nodeB : graphMap.keySet()){
            if (nodeB != nodeA) {
                Edge<T> edgeBtoA = getEdgeBetween(nodeB, nodeA);

                if (edgeBtoA != null) {
                    Set<Edge<T>> nodeBEdges = graphMap.get(nodeB);
                    nodeBEdges.remove(edgeBtoA);
                }
            }
        }
        graphMap.remove(nodeA);
    }

    @Override
    public boolean pathExists(T from, T to) {
        if(!graphMap.containsKey(from) || !graphMap.containsKey(to)){
            return false;
        }

        Set<T> nodesVisited = new HashSet<>();
        return recursiveVisitNodes(from, to, nodesVisited);
    }

    private boolean recursiveVisitNodes(T currentNode, T targetNode, Set<T> nodesVisited){
        nodesVisited.add(currentNode);

        if(currentNode.equals(targetNode)){
            return true;
        }

        for (Edge<T> edge : graphMap.get(currentNode)){
            if (!nodesVisited.contains(edge.getDestination())){
                if(recursiveVisitNodes(edge.getDestination(), targetNode, nodesVisited))
                    return true;
            }
        }

        return false;
    }

    @Override
    public List<Edge<T>> getPath(T from, T to) {
        Map<T, T> path = new HashMap<>();
        recursiveConnect(from, null, path);

        LinkedList<Edge<T>> pathToReturn = new LinkedList<>();
        T currentNode = to;
        while(currentNode != null && !currentNode.equals(from)){
            T nextNode = path.get(currentNode);

            if(nextNode != null) {
                Edge<T> edge = getEdgeBetween(nextNode, currentNode);
                pathToReturn.addFirst(edge);
            }

            currentNode = nextNode;
        }

        if (pathToReturn.isEmpty())
            return null;
        else
            return pathToReturn;
    }

    private void recursiveConnect(T to, T from, Map<T, T> path){
        path.put(to, from);

        for (Edge<T> edge : graphMap.get(to)){
            if(!path.containsKey(edge.getDestination()))
                recursiveConnect(edge.getDestination(), to, path);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Nodes: ");

        for (Map.Entry<T, Set<Edge<T>>> keyValuePair : graphMap.entrySet()){
            stringBuilder.append("\n").append(keyValuePair.getKey()).append(": ").append(keyValuePair.getValue());
        }

        return stringBuilder.toString();
    }
}
