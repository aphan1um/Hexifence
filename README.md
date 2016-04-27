# Hexifence
COMP30024 (Project B)

Notes:
- In the end game, you eventually have to start filling up edges where you will give up cells as a result. The agent should choose the ones where they give up the least number of cells and gain the most.
- Chaining (blocking of the chain).
- Consecutive-ness of cells with one edge remaining really matters.
- Early game: avoid unnecessarily giving up cells early.
- Closing off areas of cells means the person who ends up filling it up the most
    without giving up a cell gets everything.
- Chaining could be a feature of the evaluation function.
- Blocking off the chaining by closing off the area where there are potential captures.
- Number of shared edges between open cells with 5 edges filled directly affects who initiates the exchanges first.
- You can afford to sacrifice chained blocks if it means you get bigger chained blocks
    as a result.

TODO:
1. Come up with an evaluation function for board states.
2. Generate training samples (BoardGeneration spits out an evaluation function value).
3. Develop agent. 