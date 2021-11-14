What is this?
=============

This repository contains algorithms for solving *the monk problem*, a type of pursuit-evasion problem defined in the bachelor's thesis "[The Monk Problem: Verifier, heuristics and graph decompositions for a pursuit-evasion problem with a node-located evader](http://urn.kb.se/resolve?urn=urn:nbn:se:kth:diva-1664425)" that I co-authored together with Edvin Lundberg.

From the abstract:

> This paper concerns a specific pursuit-evasion problem with
a node-located evader which we call the monk problem.
First, we propose a way of verifying a strategy using a new
kind of recursive systems, called EL-systems. We show how
an EL-system representing a graph-instance of the problem
can be represented using matrices, and we give an example
of how this can be used to efficiently implement a verifier.
>
> In the later parts we propose heuristics to construct a
strategy, based on a greedy algorithm. Our main focus is to
minimise the number of pursuers needed, called the search
number. The heuristics rely on properties of minimal stable
components.

Problem Definition
==================

The problem can be stated as follows:

> *Definition 1 (Monk graph)*. A monk graph is a finite graph ``G`` with the following
properties:
> - ``G`` is a directed graph without multiple edges
> - ``G`` has at least one vertex
> - ``G`` consists of exactly one component
> - ``G`` is allowed to contain self-loops (a vertex having an edge to itself)
> - It is possible to follow an edge from every vertex in the graph (no dead ends) if the graph is not a singleton.
>
> We allow a single vertex without edges to fall into the definition of a monk graph. Such a monk graph is called a singleton.
>
> *Definition 2 (The monk problem)*. Given a monk graph ``G``, the monk problem
consists of answering the following two questions:
>
> 1. What is the search number (minimum number of pursuers) required to guarantee capture of all evaders?
> 2. Given ``p`` pursuers, how should they move in order to find all evaders in the shortest amount of time?
