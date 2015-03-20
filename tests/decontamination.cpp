/* decontamination.cpp -- find decontamination sequence of input graph
 * Antoine Amarilli, 2015
 * see http://cstheory.stackexchange.com/q/30592/4795 for problem statement
 *
 * To use, set MAXN in the code to be more than the number of vertices to
 * consider, and compile with g++ -Wall -O3 -std=c++11
 *
 * Provide graph in stdin with each edge represented by the source and target
 * represented as integers, separated by a space, edges separated by newline.
 * EOF terminates. Vertices should be numbered from 0 to n-1.
 *
 * Output is a decontamination sequence with minimal number of nodes to be
 * simultaneously decontaminated, then minimal order, then (normally) minimum in
 * lexicographic order, represented as a bit vector of the node contaminations
 * separated by the nodes to query. */

// max number of vertices, change this
#define MAXN 64

#include <cstdio>
#include <iostream>
#include <assert.h>
#include <queue>
#include <bitset>
#include <algorithm>
#include <unordered_map>

#define max(a, b) ((b)>(a) ? (b) : (a))

using namespace std;

int main() {
  // n_vertices, n_edges
  int n = 0;
  // adjacency lists
  int G[MAXN][MAXN];
  // degrees
  int nG[MAXN];

  for (int i = 0; i < MAXN; i++)
    nG[i] = 0;
  while (true) {
    int f, t, ret;
    ret = scanf("%d %d\n", &f, &t);
    if (ret != 2)
      break;
    // vertices should be numbered from 0 to n-1 with n <= MAXN
    assert(0 <= f && f < MAXN && 0 <= t && t < MAXN);
    n = max(n, max(f, t));
    G[f][nG[f]] = t;
    nG[f]++;
  }
  n++; // so that vertices are from 0 to n-1

  for (int k = 1; k <= n; k++) {
    // decontaminate simultaneously k nodes
    // do a BFS on configurations

    // queue of configurations to consider
    queue<bitset<MAXN> > q;
    // predecessor configuration of each reachable configuration
    unordered_map<bitset<MAXN>, bitset<MAXN> > p;
    // action to reach each reachable configuration from predecessor
    unordered_map<bitset<MAXN>, string > a;

    // start configuration
    bitset<MAXN> s;
    for (int i = 0; i < n; i++)
      s.set(i);
    q.push(s);

    while (!q.empty()) {
      bitset<MAXN> c = q.front();
      if (!c.count())
        break; // target configuration reached
      q.pop();
      // http://rosettacode.org/wiki/Combinations#C.2B.2B
      string bm(k, 1);
      bm.resize(n, 0);
      do {
        bitset<MAXN> nc; // new possible configuration
        for (int i = 0; i < n; i++)
          if (c[i] && !bm[i])
            // i is contaminated and was not decontaminated at this turn
            for (int j = 0; j < nG[i]; j++)
              // all possible contaminations by i
              nc.set(G[i][j]);
        if (!p.count(nc)) {
          // nc was not reachable yet
          p[nc] = c;
          a[nc] = bm;
          q.push(nc);
        }
      } while (prev_permutation(bm.begin(), bm.end()));
    }

    bitset<MAXN> e;
    if (p.count(e)) {
      // target configuration was reached: all nodes decontaminated
      // read back the path to the target configuration
      vector<string> v;
      do {
        v.push_back(e.to_string());
        v.push_back(a[e]);
        e = p[e];
      } while (p.count(e) && p[e] != e);
      v.push_back(e.to_string());
      // path is in reverse order
      reverse(v.begin(), v.end());
      for (unsigned int i = 0; i < v.size(); i++) {
        if (i % 2 == 1) {
          // display decontamination action
          for (int j = 0; j < n; j++) {
            if (v[i][j])
              printf("%d ", j);
          }
        } else {
          // display configuration
          cout << v[i];
        }
        printf("\n");
      }
      break;
    }
    // if target configuration was not reached, increase k
  }

  return 0;
}

