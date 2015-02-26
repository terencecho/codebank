# search.py
# ---------
# Licensing Information:  You are free to use or extend these projects for 
# educational purposes provided that (1) you do not distribute or publish 
# solutions, (2) you retain this notice, and (3) you provide clear 
# attribution to UC Berkeley, including a link to 
# http://inst.eecs.berkeley.edu/~cs188/pacman/pacman.html
# 
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero 
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and 
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


"""
In search.py, you will implement generic search algorithms which are called by
Pacman agents (in searchAgents.py).
"""

import util
import sys
import copy

class SearchProblem:
    """
    This class outlines the structure of a search problem, but doesn't implement
    any of the methods (in object-oriented terminology: an abstract class).

    You do not need to change anything in this class, ever.
    """

    def getStartState(self):
        """
        Returns the start state for the search problem.
        """
        util.raiseNotDefined()

    def isGoalState(self, state):
        """
          state: Search state

        Returns True if and only if the state is a valid goal state.
        """
        util.raiseNotDefined()

    def getSuccessors(self, state):
        """
          state: Search state

        For a given state, this should return a list of triples, (successor,
        action, stepCost), where 'successor' is a successor to the current
        state, 'action' is the action required to get there, and 'stepCost' is
        the incremental cost of expanding to that successor.
        """
        util.raiseNotDefined()

    def getCostOfActions(self, actions):
        """
         actions: A list of actions to take

        This method returns the total cost of a particular sequence of actions.
        The sequence must be composed of legal moves.
        """
        util.raiseNotDefined()


def tinyMazeSearch(problem):
    """
    Returns a sequence of moves that solves tinyMaze.  For any other maze, the
    sequence of moves will be incorrect, so only use this for tinyMaze.
    """
    from game import Directions
    s = Directions.SOUTH
    w = Directions.WEST
    return  [s, s, w, s, w, w, s, w]

def breadthFirstSearch(problem):
    """
    Search the shallowest nodes in the search tree first.

    You are not required to implement this, but you may find it useful for Q5.
    """
    "*** YOUR CODE HERE ***"
    util.raiseNotDefined()

def nullHeuristic(state, problem=None):
    """
    A heuristic function estimates the cost from the current state to the nearest
    goal in the provided SearchProblem.  This heuristic is trivial.
    """
    return 0

def iterativeDeepeningSearch(problem):
    """
    Perform DFS with increasingly larger depth.

    Begin with a depth of 1 and increment depth by 1 at every step.
    """

    def dfs(problem, maxDepth):
        """
        Performs DFS up to given maxDepth.

        triple in frontier stack (state, depth, actions)
        returns (actions, boolean if reachedGoal, boolean if search is Exhausted)
        """
        explored = util.Counter()
        frontier  = util.Stack()
        startState = problem.getStartState()
        frontier.push((startState, 0, []))
        explored[startState] += 1
        farthestDepth = 0

        while not frontier.isEmpty():
            state, depth, actions = frontier.pop()
            if problem.isGoalState(state):
                return (actions, True, False)
            #at the end of depth, do not go process anymore successors
            if depth >= maxDepth:
                continue
            successors = problem.getSuccessors(state)
            for elem in successors:
                nextState = elem[0];
                if not explored[nextState]:
                    explored[nextState] += 1
                    frontier.push((nextState, depth+1, actions + [elem[1]]))
                    farthestDepth = max(farthestDepth, depth + 1)
        if farthestDepth < maxDepth:
            return([], False, True)
        return ([], False, False)

    depth = 1
    while True:
        actions, reachedGoal, isExhausted = dfs(problem, depth)
        if reachedGoal:
            return actions
        if isExhausted:
            return None
        depth += 1
    return None

def aStarSearch(problem, heuristic=nullHeuristic):
    """
    Search the node that has the lowest combined cost and heuristic first.
    PriorityQueue (frontier) objects hold (state, cost, actions)
    successors holds(state, action, stop cost)
    """
    explored = util.Counter()
    frontier  = util.PriorityQueue()
    startState = problem.getStartState()
    h = heuristic(startState, problem)
    frontier.push((startState, 0, []), h)
    explored[startState] = 1
    # keep track of states that have been visited that need to be updated
    # with a better score
    update = util.Counter()

    while not frontier.isEmpty():
        state, cost, actions = frontier.pop()
        explored[state] += 1
        if problem.isGoalState(state):
            return actions
        successors = problem.getSuccessors(state)
        for elem in successors:
            nextState, nextAction, stepCost = elem
            g = cost + stepCost
            f = g + heuristic(nextState, problem)
            if not explored[nextState]:
                if not nextState in update or update[nextState] > g:
                    update[nextState] = g
                    frontier.push((nextState, g, actions + [nextAction]),f)
    return None






# Abbreviations
bfs = breadthFirstSearch
astar = aStarSearch
ids = iterativeDeepeningSearch
