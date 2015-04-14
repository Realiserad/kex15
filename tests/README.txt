/////////////////////////////////////////////////////
///////////////////    README    ////////////////////
/////////////////////////////////////////////////////

/---------------------------------------------------/
                  Naming convention
/---------------------------------------------------/
Input graphs are named 

xxVV+EE.txt where xx is the graph generation
algorithm used/type of graph, VV is the number
of vertices and EE is the number of edges.

Solutions are named
 
xxVV+EE-P-D.txt

where P is the number of pursuers required
to sweep the graph, and D is the number of
days (iterations).

/---------------------------------------------------/
                   Create graphs
/---------------------------------------------------/
You can create your own graphs using Generate.java

java WattsStrogatz --nodes 32 33 --edge --vcount > ws32-EE.txt

/---------------------------------------------------/
                Decontaminate graphs
/---------------------------------------------------/
This can be done using the solver coded by Antoine Amarilli 
(http://a3nm.net/git/decontamination/). The solver will
only work on small graphs.

You can also use Heuristics.java

cat 2star.txt | java Heuristics
