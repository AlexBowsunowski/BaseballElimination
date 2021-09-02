package com.company;

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
public class BaseballElimination {

    private final int count_command;

    private final List<String> teams;

    private final HashMap<String, Integer> teamId;
    private final String[] idTeam;

    private final int[] teamWinsById;
    private final int[] teamLosesById;
    private final int[] teamLeftsById;
    private final int[][] leftGames;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        In in = new In(filename);
        count_command = in.readInt();

        teams = new LinkedList<>();
        teamId = new HashMap<>();
        idTeam = new String[count_command];
        teamWinsById = new int[count_command];
        teamLosesById = new int[count_command];
        teamLeftsById = new int[count_command];
        leftGames = new int[count_command][count_command];

        for (int i = 0; i < count_command; ++i) {
            String nameTeam = in.readString();
            int wins = in.readInt();
            int loses = in.readInt();
            int lefts = in.readInt();

            teams.add(nameTeam);
            idTeam[i] =  nameTeam;
            teamId.put(nameTeam, i);
            teamWinsById[i] = wins;
            teamLosesById[i] = loses;
            teamLeftsById[i] = lefts;

            for (int j = 0; j < count_command; ++j) {
                leftGames[i][j] = in.readInt();
            }

        }

    }

    // number of teams
    public int numberOfTeams() {
        return count_command;
    }

    // all teams
    public Iterable<String> teams() {
        return teams;
    }

    // number of wins for given team
    public int wins(String team) {
        if (!teamId.containsKey(team))
            throw new IllegalArgumentException("Argument is incorrect");

        return teamWinsById[teamId.get(team)];
    }

    // number of losses for given team
    public int losses(String team) {
        if (!teamId.containsKey(team))
            throw new IllegalArgumentException("Argument is incorrect");

        return teamLosesById[teamId.get(team)];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        if (!teamId.containsKey(team))
            throw new IllegalArgumentException("Argument is incorrect");

        return teamLeftsById[teamId.get(team)];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        if (!teamId.containsKey(team1) || !teamId.containsKey(team2))
            throw new IllegalArgumentException("Argument is incorrect");

        return leftGames[teamId.get(team1)][teamId.get(team2)];
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        if (!teamId.containsKey(team)) throw new IllegalArgumentException("Argument is incorrect");

        return trivialEliminated(team) || nontrivialEliminated(team);
    }

    /**
     *  If the maximum number of games team x can win is less
     *  than the number of wins of some other team i,
     *  then team x is trivially eliminated
     * @param team - team name
     * @return true - if the team trivial eliminated, false - otherwise
     */
    private boolean trivialEliminated(String team) {
        int id = teamId.get(team);
        int maxWins = teamWinsById[id] + teamLeftsById[id];
        for (int i = 0; i < numberOfTeams(); ++i) {
            if (i != id && maxWins < teamWinsById[i])
                return true;
        }
        return false;
    }

    /**
     * Use Ford-Fulkerson's algorithm.
     *
     * @param team - team name
     * @return true - if team eliminated, false - if team eliminated;
     */
    private boolean nontrivialEliminated(String team) {
        int maxWins = wins(team) + remaining(team);
        int numMatches = numberOfTeams() * (numberOfTeams() - 1) / 2;
        FlowNetwork net = create(maxWins);
        FordFulkerson fordFulkerson = new FordFulkerson(net, 0, numberOfTeams() + numMatches + 1);
        for (FlowEdge e : net.adj(0)) {
            if (e.flow() != e.capacity())
                return true;
        }
        return false;
    }

    /**
     * Create Flow Network
     *
     * @param maxWins - maximum possible wins for the team
     * @return FlowNetwork
     */
    private FlowNetwork create(int maxWins) {
        int numMatches = numberOfTeams() * (numberOfTeams() - 1) / 2;
        FlowNetwork net = new FlowNetwork(numberOfTeams() + numMatches + 2);

        int vertex = 1;
        int teamVertex = numMatches + 1;
        for (int col = 0; col < leftGames.length; ++col) {
            for (int row = col + 1; row < leftGames[col].length; ++row) {
                net.addEdge(new FlowEdge(0, vertex, leftGames[col][row]));

                net.addEdge(new FlowEdge(vertex, numMatches + 1 + col, Double.POSITIVE_INFINITY));

                net.addEdge(new FlowEdge(vertex, numMatches + 1 + row, Double.POSITIVE_INFINITY));

                vertex++;
            }

            int capacity = maxWins - teamWinsById[teamVertex - numMatches - 1];
            if (capacity < 0) {
                capacity = 0;
            }
            net.addEdge(new FlowEdge(teamVertex, numberOfTeams() + numMatches + 1, capacity));
            teamVertex++;
        }
        return net;
    }

    /**
     * subset R of teams that eliminates given team
     *
     * @param team - team name
     * @return null if not eliminated; List of teams that eliminates given team
     */
    public Iterable<String> certificateOfElimination(String team) {
        if (!teamId.containsKey(team)) throw new IllegalArgumentException("Argument is incorrect");
        if (!isEliminated(team))
            return null;

        ArrayList<String> certificate = new ArrayList<>();
        int maxWins = wins(team) + remaining(team);
        int numMatches = numberOfTeams() * (numberOfTeams() - 1) / 2;
        if (trivialEliminated(team)) {
            for (int i = 0; i < numberOfTeams(); ++i) {
                if (wins(idTeam[i]) > maxWins) {
                    certificate.add(idTeam[i]);
                    return certificate;
                }
            }
        }

        FlowNetwork network = create(maxWins);
        FordFulkerson fordFulkerson = new FordFulkerson(network, 0, numberOfTeams() + numMatches + 1);
        for (int i = 0; i < numberOfTeams(); ++i) {
            if (fordFulkerson.inCut(i + numMatches + 1)) {
                certificate.add(idTeam[i]);
            }
        }
        return certificate;
    }

//    // unit test
//    public static void main(String[] args) {
//
//
//    }
}
