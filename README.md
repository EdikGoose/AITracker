# Real implementations of A*, DFC and backtracking algorithms in case of simple game

# PEAS characteristics

---

Harry as an **agent**:

- **Agent type** - Harry Potter
- **Performance measure** - The book is found and Harry makes it to the exit.
- **Environment** - Map of the game
- **Actuators** - Feets to move to neighboring cells and ability to pick up a book or a cloak
- **Sensors** - Harry’s vision of cells in radius depended on mode

Properties of the **Environment:**

- **Partially observable** because Harry sees only cells in radius dependent on mode
- **Single Agent** because Harry is the only agent on the map
- **Stochastic** because we cannot determine will the Harry be captured
- **Sequential** because current Harry’s position depends on his previous step
- **Semi-dynamic** because environment changes only after step Harry’s and doesn’t changes when Harry doesn’t move
- **Discrete** because there states in the environment
- **Known** because ****the designer of the agent have full knowledge of the rules of the
environment

# Prerequisites of I/O

---

### There are 5 input parameters:

- **Input type**
    - By keyboard
    - By random
- **Type of Harry vision**
    - First. It can see only neighbor cells
    - Second. It can see only cells in radius of 2 expect angles.
- **Backtracking parameters:**
    - **Is Backtracking needs to find the shortest path.**
        - Usually, backtracking is needed to find first suitable path, but it can also finds the shortest path. It usually will be too long(exponential asymptotics).
    - **Timeout of Backtracking** (in seconds). It is needed if the backtracking calculates too long and we want to wait not greater than this timeout

### Output.

There will be path by two algorithms.

For better understanding output in console, this is an example from assignment text:

![alt text](pic/board.png)

```java
											C   D   D   D   -   -   -   -   -   
											-   D   I   D   -   -   -   -   -   
											-   D   D   D   -   -   -   -   -   
											-   -   -   -   -   -   -   -   -   
											-   E   D   D   D   D   D   B   -   
											-   -   D   D   D   D   D   -   -   
											-   -   D   D   I   D   D   -   -   
											-   -   D   D   D   D   D   -   -   
											S   -   D   D   D   D   D   -   -
				(S means start point, B means book, C means cloak, E means exit,
							 D means danger cell, I meand inspector's node)
```

This is the shortest path by one algorithm. It divides into parts for better understanding.

The path is the sequence of numbers from 1 to n.

```java
												Path from S to B:
												C   D   D   D   -   -   -   -   -   
												-   D   I   D   -   -   -   -   -   
												-   D   D   D   -   -   -   -   -   
												-   -   5   6   7   8   9   -   -   
												-   E   D   D   D   D   D   B   -   
												3   -   D   D   D   D   D   -   -   
												2   -   D   D   I   D   D   -   -   
												1   -   D   D   D   D   D   -   -   
												S   -   D   D   D   D   D   -   -   
												Path from B to E:
												C   D   D   D   -   -   -   -   -   
												-   D   I   D   -   -   -   -   -   
												-   D   D   D   -   -   -   -   -   
												-   -   5   4   3   2   1   -   -   
												-   E   D   D   D   D   D   B   -   
												-   -   D   D   D   D   D   -   -   
												-   -   D   D   I   D   D   -   -   
												-   -   D   D   D   D   D   -   -   
												S   -   D   D   D   D   D   -   -
```

# Algorithms

---

To finding the shortest path there are 3 scenario of path:

1. Harry firstly pick up book and exits
2. Harry firstly peek cloak, then book and exits
3. Harry firstly peek book, then cloak and exits

After execution of each scenario, the most profitable will be chosen

## There are 2 algorithms for finding needed path:

### Backtracking

Backtracking algorithm search path recursively. On each step of recursive, it:

- If the current cell if the needed cell it returns path(or save it and continue for finding shortest path)
- Else
    - It starts algorithm for all safe and not visited neighbor cells(1,2,3,4) and mark current cell as visited. Neighbor cells:
        
        ```jsx
        3  4  5  
        2  H  6
        1  8  7
        ```
        
    - If there is no safe and not visited neighbor cells, it makes step back to previous cell

**Asymptotically** it is $\mathcal{O}(8^{n})$, because on each step of recursion we creates eight variants.

The algorithm can be too long. For example for this board:

```jsx
									C   -   -   -   D   D   D   -   B   
									-   -   -   -   D   I   D   E   -   
									-   -   -   -   D   D   D   -   -   
									-   -   -   -   D   D   D   D   D   
									-   -   -   -   D   D   D   D   D   
									-   -   -   -   D   D   I   D   D   
									-   -   -   -   D   D   D   D   D   
									-   -   -   -   D   D   D   D   D   
									-   -   S   -   -   -   -   -   -
```

the execution time can be $>1$ hour

### Breadth-first search

BFS finds path by using queue(Inititially, queue contains first cell):

- Take node from queue, mark as visited and for each neighbor:
    - If it is needed cell, returns path
    - Else if it is safe and not visited, add to queue
- Repeat first step

**Asymptotically** it is $\mathcal{O}(8^{n})$, therefore the execution time of any board will be $<3$ ms

# Unsolvable maps

The map is unsolvable if:

- Book/Exit closed with cloak:
    
    ```jsx
    									E   -   -   -   D   D   D   -   C   
    									-   -   -   -   D   I   D   B   -   
    									-   -   -   -   D   D   D   -   -   
    									-   -   -   -   D   D   D   D   D   
    									-   -   -   -   D   D   D   D   D   
    									-   -   -   -   D   D   I   D   D   
    									-   -   -   -   D   D   D   D   D   
    									-   -   -   -   D   D   D   D   D   
    									-   -   S   -   -   -   -   -   -
    ```
    
- Harry is closed without cloak:
    
    ```jsx
    									E   -   -   -   D   D   D   -   S   
    									-   -   -   -   D   I   D   -   -   
    									-   -   -   -   D   D   D   -   -   
    									-   -   -   -   D   D   D   D   D   
    									-   B   -   -   D   D   D   D   D   
    									-   -   -   -   D   D   I   D   D   
    									-   -   -   -   D   D   D   D   D   
    									-   -   -   -   D   D   D   D   D   
    									-   -   C   -   -   -   -   -   -
    ```
    

# Statistics

**Statistics will based on sample of 1000 boards. You can find samples in `samples` folder in project**

Algorithms will be compared by three parameters:

- Time of execution
- Length of the path
- Win rate = $\frac{\text{Number of wins}}{\text{Number of experiments}}$

Mean value will be computed by formula: $\frac{\sum_{i=0}^{n}{X_i}}{n}$

|  | Backtracking(Variant 1) | Backtracking(Variant 2) | BFS(Variant 1) | BFS(Variant 2) |
| --- | --- | --- | --- | --- |
| Mean time of execution(ms) | 15.6 | 0.52 | 0.21 | 0.2 |
| Mean length of the path | 52.4 | 47.23 | 8.8 | 8.6 |
| Win rate(%) | 99.7 | 20 | 99.7 | 83 |
