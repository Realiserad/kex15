/* Verifier for the monk problem on small graphs.
Format on input:
#Rows	Data						Example
1		#Nodes						5
1		#Edges E					8
E		An edge						0 1
1		#Pursuers P and				1 6
		length of strategy L
L		P nodes separated by 		1 2 3 3 3 1
		space
		
Nodes should be numbered from 0 to N.
Program will print each state followed
by OK! or NO.
Author: Bastian Fredriksson */
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#define GET_BIT(bit, val) (val&(1<<(64-bit)))>>(64-bit) 

int main() {
	int buf[2]={0};
	int nodes, edges, p, len;
	uint64_t P[64]={0};
	uint64_t s=0, sn=0, bc=0, l;
	
	// Nodes in graph
	fscanf(stdin, "%d ", &nodes);
	assert(nodes<64);
	
	// Number of edges in graph
	fscanf(stdin, "%d", &edges);
	
	// Edges
	for (int i=0; i<edges; i++) {
		fscanf(stdin, "%d %d", &buf[0], &buf[1]);
		assert(buf[0]<nodes);
		assert(buf[1]<nodes);
		P[buf[1]]=P[buf[1]]|(1<<(64-buf[0]));
	}
	
	// Strategy
	fscanf(stdin, "%d %d", &p, &len);
	
	for (int i=0; i<len+1; i++) {
		l=0;
		for (int i=0; i<p; i++) {
			fscanf(stdin, "%d", &buf[0]);
			l|=(1<<(64-buf[0]));
		}
		
		// Print the next state
		for (int bit=0; bit<nodes; bit++) {
			printf("%u ", (unsigned)GET_BIT(bit, s));
		}
		printf("\n");
		
		for (int j=0; j<nodes; j++) {
			if ((P[j]&s)==P[j]) {
				sn|=(1<<(64-j));
				if (++bc==nodes) {
					printf("OK!");
					return 0;
				}
			} else {
				sn&=~(1<<(64-j));
			}
		}
		sn|=l; // radd
		
		s=sn;
		sn=0;
		bc=0;
	}
	printf("NO");
	return 0;
}