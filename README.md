## Description

This is an implementation of a two-pass-linker in Java.

The target machine is word addressable and has a memory of 300 words, each consisting of 4 decimal digits. The ﬁrst (leftmost) digit is the opcode, which is unchanged by the linker. The remaining three digits (called the address ﬁeld) form either 

* An immediate operand, which is unchanged. 
* An absolute address, which is unchanged. 
* A relative address, which is relocated. 
* An external address, which is resolved. 

Relocating relative addresses and resolving external references were discussed in class and are in the notes. The input consists of a series of object modules, each of which contains three parts: deﬁnition list, use list, and program text. 

The linker processes the input twice (that is why it is called two-pass). Pass one determines the base address for each module and the absolute address for each external symbol, storing the later in the symbol table it produces. The ﬁrst module has base address zero; the base address for module I + 1 is equal to the base address of module I plus the length of module I. The absolute address for a symbol S deﬁned in module M is the base address of M plus the relative address of S within M. Pass two uses the base addresses and the symbol table computed in pass one to generate the actual output by relocating relative addresses and resolving external references. 

The deﬁnition list is a count ND (Number of Deﬁnitions) followed by ND pairs (S,R) where S is the symbol being deﬁned and R is the relative address to which the symbol refers. Pass one relocates R forming the absolute address A and stores the pair (S,A) in the symbol table. 

The use list is a count NU (Number of Use lists) followed by the NU “pairs”. The ﬁrst entry in the pair is an external symbol used in the module. The second entry is a list of relative addresses in the module in which the symbol occurs. The list is terminated by a sentinel of -1. For example, a use list of “2 f 3 1 4 -1 xyg 0 -1” signiﬁes that the symbol f is used in instructions 3, 1, and 4, and the symbol xyg is used in instruction 0. 

The program text consists of a count NT (Number of Text entries) followed by NT 5-digit numbers. NT is also the length of the module. The left four digits of each number form the instruction as described above. The last (rightmost) digit speciﬁes the address type: 1 signiﬁes “immediate”, 2 “absolute”, 3 “relative”, and 4 “external”.

## Other requirements: Error detection, arbitrary limits, et al.

Your program must check the input for the errors listed below. All error messages produced must be informative, e.g., “Error: X21 was used but not deﬁned. It has been given the value 111”. 

* If a symbol is multiply deﬁned, print an error message and use the value given in the last deﬁnition. 
* If a symbol is used but not deﬁned, print an error message and use the value 111. 
* If a symbol is deﬁned but not used, print a warning message and continue. 
* If an absolute address exceeds the size of the machine, print an error message and use the largest legal value. 
* If multiple symbols are listed as used in the same instruction, print an error message and ignore all but the last usage given. 
* If an address appearing in a deﬁnition exceeds the size of the module, print an error message and treat the address given as the last word in the module. 

You may need to set “arbitrary limits”, for example you may wish to limit the number of characters in a symbol to (say) 8. Any such limits should be clearly documented in the program and if the input fails to meet your limits, your program must print an error message and continue if possible. Naturally, the limits must be large enough for all the inputs on the web. Under no circumstances should your program reference an array out of bounds, etc. Submit the source code for your lab, together with a README ﬁle (required) describing how to compile and run it. Your program must read an input set from standard input, i.e., directly from the keyboard. It is an error for you to require the input be in a ﬁle. You may develop your lab on any machine you wish, but must insure that it compiles and runs on the NYU system assigned to the course.

There are several sample input sets on the web. The ﬁrst is shown below and the second is an re-formatted version of the ﬁrst. If you use the java Scanner or C’s scanf() inputs 1 and 2 should look the same to your program. Some of the input sets contain errors that you are to detect as described above. We will run your lab on these (and other) input sets. The expected output is also on the web.

```
4 
1 xy 2 
2 z 2 -1 xy 4 -1 
5 10043 56781 20004 80023 70014 
0 
1 z 1 2 3 -1 
6 80013 10004 10004 30004 10023 10102 
0 
1 z 1 -1 
2 50013 40004 
1 z 2 2 xy 
2 -1 z 1 -1 
3 80002 10014 20004
```

The following is output annotated for clarity and class discussion. Your output is not expected to be this fancy.

```
Symbol Table 
xy=2 
z=15

Memory Map
+0 
0: 10042 1004+0 = 1004 
1: 56781 5678 
2: xy: 20004 ->z 2015 
3: 80023 8002+0 = 8002 
4: 70014 ->xy 7002 
+5 
0 80013 8001+5 = 8006 
1 10004 ->z 1015 
2 10004 ->z 1015 
3 30004 ->z 3015 
4 10023 1002+5 = 1007 
5 10102 1010 
+11 
0 50013 5001+11= 5012 
1 40004 ->z 4015 
+13 
0 80002 8000 
1 10014 ->z 1015 
2 z: 20004 ->xy 2002
```
