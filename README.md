# BaseballElimination

### The baseball elimination problem

 In the baseball elimination problem, there is a division consisting of n teams. 
 At some point during the season, team i has w[i] wins, l[i] losses, r[i] remaining games, and g[i][j] games left to play against team j.
 A team is mathematically eliminated if it cannot possibly finish the season in (or tied for) first place. 
 The goal is to determine exactly which teams are mathematically eliminated. For simplicity, we assume that no games end 
 in a tie (as is the case in Major League Baseball) and that there are no rainouts (i.e., every scheduled game is played).

A maxflow formulation.   We now solve the baseball elimination problem by reducing it to the maxflow problem. 
To check whether team x is eliminated, we consider two cases.

- Trivial elimination. If the maximum number of games team x can win is less than the number of wins of some other team i, then team x is trivially eliminated (as is Montreal in the example above). That is, if w[x] + r[x] < w[i], then team x is mathematically eliminated.

- Nontrivial elimination. Otherwise, we create a flow network and solve a maxflow problem in it. In the network, feasible integral flows correspond to outcomes of the remaining schedule. There are vertices corresponding to teams (other than team x) and to remaining divisional games (not involving team x). Intuitively, each unit of flow in the network corresponds to a remaining game. As it flows through the network from s to t, it passes from a game vertex, say between teams i and j, then through one of the team vertices i or j, classifying this game as being won by that team.

More precisely, the flow network includes the following edges and capacities.

* We connect an artificial source vertex s to each game vertex i-j and set its capacity to g[i][j]. If a flow uses all g[i][j] units of capacity on this edge, then we interpret this as playing all of these games, with the wins distributed between the team vertices i and j.

* We connect each game vertex i-j with the two opposing team vertices to ensure that one of the two teams earns a win. We do not need to restrict the amount of flow on such edges.

* Finally, we connect each team vertex to an artificial sink vertex t. We want to know if there is some way of completing all the games so that team x ends up winning at least as many games as team i. Since team x can win as many as w[x] + r[x] games, we prevent team i from winning more than that many games in total, by including an edge from team vertex i to the sink vertex with capacity w[x] + r[x] - w[i].

