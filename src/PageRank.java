import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * PageRank
 *
 * @author Jonathan Jemson
 * @version 1.0
 */
public class PageRank {

    private static final double EPSILON = 1e-10;
    private List<List<Integer>> inGraph;
    private List<List<Integer>> outGraph;
    private double alpha;
    private List<Double> pagerank;

    public PageRank(String graphRep, double alpha) {
        this.alpha = alpha;
        this.outGraph = PageRank.parseGraph(graphRep);
        this.inGraph = PageRank.invertGraph(outGraph);

        pagerank = new ArrayList<>(inGraph.size());
        for (int i = 0; i < inGraph.size(); i++) {
            pagerank.add(1.0 / inGraph.size());
        }
    }

    public List<Double> computePageRank() {
        List<Double> oldPagerank;
        boolean run = true;
        int numIterations = 0;
        while (run) {
            numIterations++;
            // Save the old vector for checking completion
            oldPagerank = new ArrayList<>(pagerank);

            for (int i = 0; i < pagerank.size(); i++) {
                pagerank.set(i, (1.0d - alpha) / pagerank.size());
                List<Integer> inX = inGraph.get(i);
                for (Integer x : inX) {
                    pagerank.set(i, pagerank.get(i) + alpha * oldPagerank.get(x) / outGraph.get(x).size());
                }
            }

            // Check if we're done
            for (int i = 0; i < pagerank.size(); i++) {
                if (Math.abs(oldPagerank.get(i) - pagerank.get(i)) >= EPSILON) {
                    run = true;
                    break;
                } else {
                    run = false;
                }
            }
        }
        return pagerank;
    }

    public static List<List<Integer>> parseGraph(String s) {
        String[] rows = s.split("\n");

        int currRow = 0;
        int v = Integer.parseInt(rows[rows.length-1].split(":")[0]);

        List<List<Integer>> graph = new ArrayList<>(v);

        List<Integer> adj = null;
        for (int i = 0; i <= v; i++) {
            adj = new LinkedList<>();
            adj.add(i);
            String[] rowItems = rows[currRow].split(":");
            int vi = Integer.parseInt(rowItems[0]);
            String[] vertices = rowItems[1].split(",");
            if (vi == i) {
                adj.addAll(Arrays.stream(vertices)
                                 .map(String::trim)
                                 .map(Integer::parseInt)
                                 .collect(Collectors.toList())
                );
                currRow++;
            }
            graph.add(adj);
        }

        return graph;
    }

    public static List<List<Integer>> invertGraph(List<List<Integer>> graph) {
        List<List<Integer>> graphInverse = new ArrayList<>(graph.size());
        for (int i = 0; i < graph.size(); i++) {
            graphInverse.add(new LinkedList<>());
        }
        for (int i = 0; i < graph.size(); i++) {
            List<Integer> u = graph.get(i);
            for (Integer v1 : u) {
                graphInverse.get(v1).add(i);
            }
        }

        return graphInverse;
    }

    public static void main(String... args) {
        if (args.length < 2) {
            System.out.println("Usage: java PageRank [input file] [alpha]");
            System.exit(1);
        }
        File inputFile = new File(args[0]);
        if (!inputFile.canRead()) {
            System.err.println("Input file cannot be opened for reading.");
            System.exit(1);
        }
        double alpha = Double.parseDouble(args[1]);
        String g = null;
        try {
            Scanner scanner = new Scanner(inputFile).useDelimiter("\\A");
            g = scanner.next();
        } catch (IOException ioe) {
            System.err.println("Error opening input file.");
            ioe.printStackTrace(System.err);
            System.exit(1);
        }
        PageRank pageRank = new PageRank(g, alpha);
        List<Double> rank = pageRank.computePageRank();
        List<Integer> rankings = IntStream.range(0, rank.size()).boxed().collect(Collectors.toList());

        rankings.sort(Comparator.comparing(rank::get).reversed());

        for (Double d : rank) {
            System.out.printf("%.10e\n", d);
        }
        for (Integer i : rankings) {
            System.err.println(i);
        }

    }
}