///////////////////////////////////////
////////////    README    /////////////
Monk graphs and their optimal solutions
///////////////////////////////////////

/-------------------------------------/
           Naming convention
/-------------------------------------/
Input graphs are named 

xxVV+EE.txt where xx is the graph generation
algorithm used/type of graph, VV is the number
of vertices and EE is the number of edges.

Solutions are named
 
xxVV+EE-P-D.txt

where P is the number of pursuers required
to sweep the graph, and D is the number of
days (iterations).

/--------------------------------------/
             Create graphs
/--------------------------------------/
You can create your own graphs using
Generate.java with the --edge flag.

java Generate --nodes 32 33 --edge > ws32-EE.txt

/--------------------------------------/
             Sweep graphs
/--------------------------------------/
This can be done using the solver coded
by Antoine Amarilli (http://a3nm.net/).

Please look in contamination.cpp for more
information.