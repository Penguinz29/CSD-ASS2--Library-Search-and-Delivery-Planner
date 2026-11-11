package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BranchGraph {
    /** Represents an edge in the branch graph. */
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

    /** Represents a neighboring branch in the graph. */
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

    /** Represents the result of a route calculation. */
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

    /** Represents the result of a minimum spanning tree search. */
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

    /**
     * Constructs a new branch graph with the given branch IDs and edges.
     *
     * @param branchIds the list of branch IDs
     * @param edges the list of edges
     */
    public BranchGraph(List<String> branchIds, List<Edge> edges) {
        validateBranchIds(branchIds);
        validateEdges(branchIds, edges);

        this.branchIds = Collections.unmodifiableList(new ArrayList<String>(branchIds));
        this.edges = Collections.unmodifiableList(new ArrayList<Edge>(edges));
        this.adjacency = new LinkedHashMap<String, List<Neighbor>>();
        for (String id : branchIds) adjacency.put(id, new ArrayList<Neighbor>());
        for (Edge edge : edges) {
            adjacency.get(edge.from).add(new Neighbor(edge.to, edge.distance));
            adjacency.get(edge.to).add(new Neighbor(edge.from, edge.distance));
        }
    }

    /**
     * Validates the list of branch IDs.
     * @param branchIds the list of branch IDs to validate
     */
    private static void validateBranchIds(List<String> branchIds) {
        if (branchIds == null) {
            throw new IllegalArgumentException("branchIds must not be null");
        }
        Set<String> seen = new HashSet<String>();
        for (String id : branchIds) {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("branchId must not be empty");
            }
            boolean isNew = seen.add(id);
            if (!isNew) {
                throw new IllegalArgumentException("duplicate branchId: " + id);
            }
        }
    }

    /**
     * Validates the list of edges.
     * @param branchIds the list of branch IDs
     * @param edges the list of edges to validate
     */
    private static void validateEdges(List<String> branchIds, List<Edge> edges) {
        if (edges == null) {
            throw new IllegalArgumentException("edges must not be null");
        }
        Set<String> knownIds = new HashSet<String>(branchIds);
        Set<String> seenPairs = new HashSet<String>();
        for (Edge e : edges) {
            if (e == null) {
                throw new IllegalArgumentException("edge must not be null");
            }
            if (!knownIds.contains(e.from) || !knownIds.contains(e.to)) {
                throw new IllegalArgumentException("unknown branchId in edge " + e.from + "-" + e.to);
            }
            if (e.from.equals(e.to)) {
                throw new IllegalArgumentException("self-loop at " + e.from);
            }
            if (e.distance <= 0) {
                throw new IllegalArgumentException("distance must be > 0 in edge " + e.from + "-" + e.to);
            }
            String key = pairKey(e.from, e.to);
            boolean isNewPair = seenPairs.add(key);
            if (!isNewPair) {
                throw new IllegalArgumentException("duplicate edge " + e.from + "-" + e.to);
            }
        }
    }

    /**
     * Returns a unique key for a pair of branch IDs.
     * @param a the first branch ID
     * @param b the second branch ID
     * @return the unique key
     */
    private static String pairKey(String a, String b) {
        if (a.compareTo(b) <= 0) {
            return a + "\u0000" + b;
        }
        return b + "\u0000" + a; //"\u0000"là
    }

    /** Returns the list of branch IDs in the graph. */
    public List<String> getBranchIds() { return branchIds; }

    /** Returns the list of edges in the graph. */
    public List<Edge> getEdges() { return edges; }

    /** Returns the adjacency list representation of the graph. */
    public Map<String, List<Neighbor>> getAdjacency() {
        Map<String, List<Neighbor>> copy = new LinkedHashMap<String, List<Neighbor>>();
        for (Map.Entry<String, List<Neighbor>> entry : adjacency.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }
    
    /** Returns the number of vertices in the graph. */
    public int vertexCount() { return branchIds.size(); }
    /** Returns the number of edges in the graph. */
    public int edgeCount() { return edges.size(); }

    /**
     * Returns the list of neighbors for the given branch ID.
     * @param branchId the branch ID
     * @return the list of neighbors
     */
    public List<Neighbor> neighbours(String branchId) {
        requireBranch(branchId);
        return Collections.unmodifiableList(adjacency.get(branchId));
    }

    /**
     * Returns a string describing the neighbors of each branch in the graph.
     * @return the string describing the neighbors
     */
    public String describeNeighbours() {
        StringBuilder sb = new StringBuilder();
        boolean firstLine = true;
        for (Map.Entry<String, List<Neighbor>> entry : adjacency.entrySet()) {
            if (!firstLine) sb.append('\n');
            firstLine = false;
            sb.append(entry.getKey()).append(": ");
            List<Neighbor> list = entry.getValue();
            if (list.isEmpty()) sb.append("(no neighbours)");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(list.get(i).branchId).append('(').append(list.get(i).distance).append(')');
            }
        }
        return sb.toString();
    }

    /**
     * Returns true if the graph is connected, false otherwise.
     * @return true if the graph is connected, false otherwise
     */
    public boolean isConnected() {
        if (branchIds.isEmpty()) return false;
        return breadthFirstSearch(branchIds.get(0)).size() == branchIds.size();
    }

    /**
     * Performs a breadth-first search starting from the given source branch.
     * @param source the source branch ID
     * @return the list of branches visited in order
     */
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

    /**
     * Returns the shortest route from the source branch to the destination branch.
     * @param source the source branch ID
     * @param destination the destination branch ID
     * @return the route result
     */
    public RouteResult shortestRoute(String source, String destination) {
        return shortestRoute(source, destination, null);
    }

    /**
     * Returns the shortest route from the source branch to the destination branch.
     * @param source the source branch ID
     * @param destination the destination branch ID
     * @param trace the list to store the trace of the algorithm
     * @return the route result
     */
    public RouteResult shortestRoute(String source, String destination, List<String> trace) {
        requireBranch(source);
        requireBranch(destination);
        Map<String, Long> distances = new LinkedHashMap<String, Long>();
        Map<String, String> previous = new LinkedHashMap<String, String>();
        Map<String, Boolean> settled = new LinkedHashMap<String, Boolean>();
        for (String id : branchIds) distances.put(id, Long.MAX_VALUE);
        distances.put(source, 0L);
        note(trace, "init: dist[" + source + "] = 0, every other vertex = INF");

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
            if (current == null) {
                note(trace, "step " + (step + 1) + ": no unsettled vertex with a finite distance is left, stop");
                break;
            }
            settled.put(current, Boolean.TRUE);
            note(trace, "step " + (step + 1) + ": settle " + current + " (dist = " + best + ")");
            if (current.equals(destination)) {
                note(trace, "  destination " + destination + " is settled, stop");
                break;
            }
            for (Neighbor neighbor : adjacency.get(current)) {
                String edgeText = "  " + current + "-" + neighbor.branchId + " (w=" + neighbor.distance + "): ";
                if (settled.containsKey(neighbor.branchId)) {
                    note(trace, edgeText + neighbor.branchId + " already settled, skip");
                    continue;
                }
                long candidate = best + neighbor.distance;
                long old = distances.get(neighbor.branchId);
                String oldParent = previous.get(neighbor.branchId);
                boolean shorter = candidate < old;
                boolean tieWithSmallerParent = candidate == old
                        && (oldParent == null || current.compareTo(oldParent) < 0);
                if (shorter || tieWithSmallerParent) {
                    if (shorter) {
                        note(trace, edgeText + "dist[" + neighbor.branchId + "] " + show(old) + " -> "
                                + candidate + ", parent = " + current);
                    } else {
                        note(trace, edgeText + "tie at " + candidate + ", parent " + oldParent + " -> "
                                + current + " (smaller id)");
                    }
                    distances.put(neighbor.branchId, candidate);
                    previous.put(neighbor.branchId, current);
                } else {
                    note(trace, edgeText + "candidate " + candidate + " does not improve dist["
                            + neighbor.branchId + "] = " + show(old) + ", keep");
                }
            }
        }
        long resultDistance = distances.get(destination);
        if (resultDistance == Long.MAX_VALUE) {
            note(trace, "result: " + destination + " is unreachable from " + source);
            return new RouteResult(new ArrayList<String>(), -1L);
        }
        List<String> path = new ArrayList<String>();
        String current = destination;
        while (current != null) {
            path.add(current);
            if (current.equals(source)) break;
            current = previous.get(current);
        }
        if (path.isEmpty() || !path.get(path.size() - 1).equals(source)) {
            return new RouteResult(new ArrayList<String>(), -1L);
        }
        Collections.reverse(path);
        note(trace, "result: " + join(path, " -> ") + ", total " + resultDistance);
        return new RouteResult(path, resultDistance);
    }

    /**
     * Returns the minimum spanning tree of the graph.
     * @return the minimum spanning tree result
     */
    public MstResult minimumSpanningTree() {
        return minimumSpanningTree(null);
    }

    /**
     * Returns the minimum spanning tree of the graph.
     * @param trace the list to store the trace of the algorithm
     * @return the minimum spanning tree result
     */
    public MstResult minimumSpanningTree(List<String> trace) {
        if (branchIds.isEmpty()) return new MstResult(false, new ArrayList<Edge>(), 0L);
        String start = branchIds.get(0);
        Map<String, Long> keys = new LinkedHashMap<String, Long>();
        Map<String, String> parent = new LinkedHashMap<String, String>();
        Map<String, Boolean> inTree = new LinkedHashMap<String, Boolean>();
        for (String id : branchIds) keys.put(id, Long.MAX_VALUE);
        keys.put(start, 0L);
        note(trace, "init: start at " + start + ", key[" + start + "] = 0, others = INF");
        long runningTotal = 0L;
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
            if (current == null) {
                note(trace, "step " + (step + 1) + ": no remaining branch can be reached from the tree"
                        + " -> graph is disconnected -> NO SPANNING TREE");
                return new MstResult(false, new ArrayList<Edge>(), 0L);
            }
            inTree.put(current, Boolean.TRUE);
            String chosenParent = parent.get(current);
            if (chosenParent == null) {
                note(trace, "step " + (step + 1) + ": add " + current + " (start)");
            } else {
                runningTotal += bestKey;
                note(trace, "step " + (step + 1) + ": add " + current + " via edge " + chosenParent + "-"
                        + current + " (w=" + bestKey + "), total = " + runningTotal);
            }
            for (Neighbor neighbor : adjacency.get(current)) {
                String edgeText = "  " + current + "-" + neighbor.branchId + " (w=" + neighbor.distance + "): ";
                if (inTree.containsKey(neighbor.branchId)) {
                    note(trace, edgeText + neighbor.branchId + " already in tree, skip");
                    continue;
                }
                long oldKey = keys.get(neighbor.branchId);
                String oldParent = parent.get(neighbor.branchId);
                boolean lighter = neighbor.distance < oldKey;
                boolean tieWithSmallerParent = neighbor.distance == oldKey
                        && (oldParent == null || current.compareTo(oldParent) < 0);
                if (lighter || tieWithSmallerParent) {
                    if (lighter) {
                        note(trace, edgeText + "key[" + neighbor.branchId + "] " + show(oldKey) + " -> "
                                + neighbor.distance + ", parent = " + current);
                    } else {
                        note(trace, edgeText + "tie at " + neighbor.distance + ", parent " + oldParent
                                + " -> " + current + " (smaller id)");
                    }
                    keys.put(neighbor.branchId, (long) neighbor.distance);
                    parent.put(neighbor.branchId, current);
                } else {
                    note(trace, edgeText + "does not improve key[" + neighbor.branchId + "] = "
                            + show(oldKey) + ", keep");
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
        note(trace, "result: spanning tree with " + selected.size() + " edges, total " + total);
        return new MstResult(true, selected, total);
    }

    /**
     * Returns the canonical edge between two branches.
     * @param a the first branch ID
     * @param b the second branch ID
     * @param distance the distance between the branches
     * @return the canonical edge
     */
    private Edge canonicalEdge(String a, String b, int distance) {
        if(a.compareTo(b) <= 0) {
            return new Edge(a, b, distance);
        } else {
            return new Edge(b, a, distance);
        }
        
    }

    /**
     * Requires that the given branch exists in the graph.
     * @param id the branch ID
     */
    private void requireBranch(String id) {
        if (!adjacency.containsKey(id)) throw new IllegalArgumentException("unknown branchId: " + id);
    }

    /**
     * Adds a note to the trace list.
     * @param trace the list to store the trace
     * @param line the note to add
     */
    private static void note(List<String> trace, String line) {
        if (trace != null) trace.add(line);
    }

    /**
     * Returns a string representation of the given long value.
     * @param value the long value
     * @return the string representation
     */
    private static String show(long value) {
        if (value == Long.MAX_VALUE) {
            return "INF";
        } else {
            return Long.toString(value);
        }
        
    }

    /**
     * Joins the given parts with the specified separator.
     * @param parts the list of strings to join
     * @param sep the separator
     * @return the joined string
     */
    private static String join(List<String> parts, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) sb.append(sep);
            sb.append(parts.get(i));
        }
        return sb.toString();
    }
}