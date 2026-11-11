package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Undirected weighted graph stored as an adjacency list. */
public final class BranchGraph {
    public static final class Edge {
        private final String from;
        private final String to;
        private final int distance;
        public Edge(String from, String to, int distance) {
            this.from = from;
            this.to = to;
            this.distance = distance;
        }
        public String getFrom() { return from; }
        public String getTo() { return to; }
        public int getDistance() { return distance; }
    }

    public static final class Neighbor {
        private final String branchId;
        private final int distance;
        private Neighbor(String branchId, int distance) {
            this.branchId = branchId;
            this.distance = distance;
        }
        public String getBranchId() { return branchId; }
        public int getDistance() { return distance; }
    }

    public static final class RouteResult {
        private final List<String> path;
        private final long distance;
        private RouteResult(List<String> path, long distance) {
            this.path = Collections.unmodifiableList(path);
            this.distance = distance;
        }
        public boolean isReachable() { return !path.isEmpty(); }
        public List<String> getPath() { return path; }
        public long getDistance() { return distance; }
    }

    public static final class MstResult {
        private final boolean spanning;
        private final List<Edge> edges;
        private final long totalDistance;
        private MstResult(boolean spanning, List<Edge> edges, long totalDistance) {
            this.spanning = spanning;
            this.edges = Collections.unmodifiableList(edges);
            this.totalDistance = totalDistance;
        }
        public boolean hasSpanningTree() { return spanning; }
        public List<Edge> getEdges() { return edges; }
        public long getTotalDistance() { return totalDistance; }
    }

    private final List<String> branchIds;
    private final Map<String, List<Neighbor>> adjacency;
    private final List<Edge> edges;

    public BranchGraph(List<String> branchIds, List<Edge> edges) {
        this.branchIds = Collections.unmodifiableList(new ArrayList<String>(branchIds));
        this.edges = Collections.unmodifiableList(new ArrayList<Edge>(edges));
        this.adjacency = new LinkedHashMap<String, List<Neighbor>>();
        for (String id : branchIds) adjacency.put(id, new ArrayList<Neighbor>());
        for (Edge edge : edges) {
            adjacency.get(edge.from).add(new Neighbor(edge.to, edge.distance));
            adjacency.get(edge.to).add(new Neighbor(edge.from, edge.distance));
        }
    }

    public List<String> getBranchIds() { return branchIds; }
    public List<Edge> getEdges() { return edges; }

    public Map<String, List<Neighbor>> getAdjacency() {
        Map<String, List<Neighbor>> copy = new LinkedHashMap<String, List<Neighbor>>();
        for (Map.Entry<String, List<Neighbor>> entry : adjacency.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public List<String> breadthFirstSearch(String source) {
        requireBranch(source);
        List<String> order = new ArrayList<String>();
        List<String> queue = new ArrayList<String>();
        Map<String, Boolean> visited = new LinkedHashMap<String, Boolean>();
        visited.put(source, Boolean.TRUE);
        queue.add(source);
        for (int head = 0; head < queue.size(); head++) {
            String current = queue.get(head);
            order.add(current);
            for (Neighbor neighbor : adjacency.get(current)) {
                if (!visited.containsKey(neighbor.branchId)) {
                    visited.put(neighbor.branchId, Boolean.TRUE);
                    queue.add(neighbor.branchId);
                }
            }
        }
        return order;
    }

    /** O(V^2 + E) Dijkstra with deterministic lexicographic ties. */
    public RouteResult shortestRoute(String source, String destination) {
        requireBranch(source);
        requireBranch(destination);
        Map<String, Long> distances = new LinkedHashMap<String, Long>();
        Map<String, String> previous = new LinkedHashMap<String, String>();
        Map<String, Boolean> settled = new LinkedHashMap<String, Boolean>();
        for (String id : branchIds) distances.put(id, Long.MAX_VALUE);
        distances.put(source, 0L);

        for (int step = 0; step < branchIds.size(); step++) {
            String current = null;
            long best = Long.MAX_VALUE;
            for (String id : branchIds) {
                long distance = distances.get(id);
                if (!settled.containsKey(id) && distance < best) {
                    current = id;
                    best = distance;
                } else if (!settled.containsKey(id) && distance == best && distance != Long.MAX_VALUE
                        && current != null && id.compareTo(current) < 0) {
                    current = id;
                }
            }
            if (current == null) break;
            settled.put(current, Boolean.TRUE);
            if (current.equals(destination)) break;
            for (Neighbor neighbor : adjacency.get(current)) {
                if (settled.containsKey(neighbor.branchId)) continue;
                long candidate = best + neighbor.distance;
                long old = distances.get(neighbor.branchId);
                String oldParent = previous.get(neighbor.branchId);
                if (candidate < old || (candidate == old && (oldParent == null || current.compareTo(oldParent) < 0))) {
                    distances.put(neighbor.branchId, candidate);
                    previous.put(neighbor.branchId, current);
                }
            }
        }
        long resultDistance = distances.get(destination);
        if (resultDistance == Long.MAX_VALUE) return new RouteResult(new ArrayList<String>(), Long.MAX_VALUE);
        List<String> path = new ArrayList<String>();
        String current = destination;
        while (current != null) {
            path.add(current);
            if (current.equals(source)) break;
            current = previous.get(current);
        }
        if (path.isEmpty() || !path.get(path.size() - 1).equals(source)) {
            return new RouteResult(new ArrayList<String>(), Long.MAX_VALUE);
        }
        Collections.reverse(path);
        return new RouteResult(path, resultDistance);
    }

    /** Prim's algorithm. An incomplete result is never reported as a spanning tree. */
    public MstResult minimumSpanningTree() {
        if (branchIds.isEmpty()) return new MstResult(false, new ArrayList<Edge>(), 0L);
        String start = branchIds.get(0);
        Map<String, Long> keys = new LinkedHashMap<String, Long>();
        Map<String, String> parent = new LinkedHashMap<String, String>();
        Map<String, Boolean> inTree = new LinkedHashMap<String, Boolean>();
        for (String id : branchIds) keys.put(id, Long.MAX_VALUE);
        keys.put(start, 0L);
        for (int step = 0; step < branchIds.size(); step++) {
            String current = null;
            long bestKey = Long.MAX_VALUE;
            for (String id : branchIds) {
                long key = keys.get(id);
                if (!inTree.containsKey(id) && key < bestKey) {
                    current = id;
                    bestKey = key;
                } else if (!inTree.containsKey(id) && key == bestKey && key != Long.MAX_VALUE
                        && current != null && id.compareTo(current) < 0) {
                    current = id;
                }
            }
            if (current == null) return new MstResult(false, new ArrayList<Edge>(), 0L);
            inTree.put(current, Boolean.TRUE);
            for (Neighbor neighbor : adjacency.get(current)) {
                if (inTree.containsKey(neighbor.branchId)) continue;
                long oldKey = keys.get(neighbor.branchId);
                String oldParent = parent.get(neighbor.branchId);
                if (neighbor.distance < oldKey || (neighbor.distance == oldKey
                        && (oldParent == null || current.compareTo(oldParent) < 0))) {
                    keys.put(neighbor.branchId, (long) neighbor.distance);
                    parent.put(neighbor.branchId, current);
                }
            }
        }
        List<Edge> selected = new ArrayList<Edge>();
        long total = 0L;
        for (String id : branchIds) {
            String predecessor = parent.get(id);
            if (predecessor == null) continue;
            Edge edge = canonicalEdge(predecessor, id, keys.get(id).intValue());
            selected.add(edge);
            total += edge.distance;
        }
        return new MstResult(true, selected, total);
    }

    private Edge canonicalEdge(String a, String b, int distance) {
        return a.compareTo(b) <= 0 ? new Edge(a, b, distance) : new Edge(b, a, distance);
    }

    private void requireBranch(String id) {
        if (!adjacency.containsKey(id)) throw new IllegalArgumentException("unknown branchId: " + id);
    }
}

