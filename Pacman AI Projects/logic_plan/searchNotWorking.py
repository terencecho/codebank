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
import logic

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

    def getGhostStartStates(self):
        """
        Returns a list containing the start state for each ghost.
        Only used in problems that use ghosts (FoodGhostSearchProblem)
        """
        util.raiseNotDefined()

    def terminalTest(self, state):
        """
          state: Search state

        Returns True if and only if the state is a valid goal state.
        """
        util.raiseNotDefined()
        
    def getGoalState(self):
        """
        Returns goal state for problem. Note only defined for problems that have
        a unique goal state such as PositionSearchProblem
        """
        util.raiseNotDefined()

    def result(self, state, action):
        """
        Given a state and an action, returns resulting state and step cost, which is
        the incremental cost of moving to that successor.
        Returns (next_state, cost)
        """
        util.raiseNotDefined()

    def actions(self, state):
        """
        Given a state, returns available actions.
        Returns a list of actions
        """        
        util.raiseNotDefined()

    def getCostOfActions(self, actions):
        """
         actions: A list of actions to take

        This method returns the total cost of a particular sequence of actions.
        The sequence must be composed of legal moves.
        """
        util.raiseNotDefined()

    def getWidth(self):
        """
        Returns the width of the playable grid (does not include the external wall)
        Possible x positions for agents will be in range [1,width]
        """
        util.raiseNotDefined()

    def getHeight(self):
        """
        Returns the height of the playable grid (does not include the external wall)
        Possible y positions for agents will be in range [1,height]
        """
        util.raiseNotDefined()

    def isWall(self, position):
        """
        Return true if position (x,y) is a wall. Returns false otherwise.
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


def atLeastOne(expressions) :
    """
    Given a list of logic.Expr instances, return a single logic.Expr instance in CNF (conjunctive normal form)
    that represents the logic that at least one of the expressions in the list is true.
    >>> A = logic.PropSymbolExpr('A');
    >>> B = logic.PropSymbolExpr('B');
    >>> symbols = [A, B]
    >>> atleast1 = atLeastOne(symbols)
    >>> model1 = {A:False, B:False}
    >>> print logic.pl_true(atleast1,model1)
    False
    >>> model2 = {A:False, B:True}
    >>> print logic.pl_true(atleast1,model2)
    True
    >>> model3 = {A:True, B:True}
    >>> print logic.pl_true(atleast1,model2)
    True
    """
    expr = expressions[0]
    for expression in expressions:
        expr = expr | expression
    return expr


def atMostOne(expressions) :
    """
    Given a list of logic.Expr instances, return a single logic.Expr instance in CNF (conjunctive normal form)
    that represents the logic that at most one of the expressions in the list is true.
    """
    expr = expressions[0] | ~expressions[0]
    for express1 in expressions:
        for express2 in expressions:
            if express1 == express2:
                continue
            expr = expr & (~express1 | ~express2)
    return expr


def exactlyOne(expressions) :
    """
    Given a list of logic.Expr instances, return a single logic.Expr instance in CNF (conjunctive normal form)
    that represents the logic that exactly one of the expressions in the list is true.
    """
    return atMostOne(expressions) & atLeastOne(expressions)


def extractActionSequence(model, actions):
    """
    Convert a model in to an ordered list of actions.
    model: Propositional logic model stored as a dictionary with keys being
    the symbol strings and values being Boolean: True or False
    Example:
    >>> model = {"North[2]":True, "P[3,4,1]":True, "P[3,3,1]":False, "West[0]":True, "GhostScary":True, "West[3]":False, "South[1]":True, "East[1]":False}
    >>> actions = ['North', 'South', 'East', 'West']
    >>> plan = extractActionSequence(model, actions)
    >>> print plan
    ['West', 'South', 'North']
    """
    i = 0
    actSeq = []

    while (len(actSeq) == i):
        for key in model.keys():
            potentialAction = str(key)
            currTime = "[" + str(i) + "]"
            if model[key] == True and currTime in potentialAction:
                end = potentialAction.index('[')
                move = potentialAction[:end]
                actSeq.append(move)
        i+=1

    return actSeq


def positionLogicPlan(problem):
    """
    Given an instance of a PositionSearchProblem, return a list of actions that lead to the goal.
    Available actions are game.Directions.{NORTH,SOUTH,EAST,WEST}
    Note that STOP is not an available action.
    """
    orgActions = ['North', 'South', 'East', 'West']
    kb = []

    # Get starting constraints.
    initialState, width, height = problem.getStartState(), problem.getWidth(), problem.getHeight()
    goalState = problem.getGoalState()
    kb.append(logic.PropSymbolExpr("P", initialState[0], initialState[1], 0))

    # Can't be in more than one place at a time.
    for x in xrange(1, width + 1):
        for y in xrange(1, height + 1):
            if (x != initialState[0] and y != initialState[1]):
                kb.append(~logic.PropSymbolExpr("P", x, y, 0))

    time_step = 0
    model = False
    exploredStates = {}
    exploredStates[initialState] = time_step
    possibleStates = []
    initialActions = problem.actions(initialState)
    for action in initialActions:
        successor = (problem.result(initialState, action)[0], initialState, action)
        possibleStates.append(successor)
    
    while 1:
        goal_check = list(kb)
        possibleGoal = logic.PropSymbolExpr("P", goalState[0], goalState[1], time_step)
        goal_check.append(possibleGoal)
        model = logic.pycoSAT(goal_check)
        if (not possibleStates) | (model != False):
            break
        curActions = []
        for action in orgActions:
            curActions.append(logic.PropSymbolExpr(action, time_step))
        kb.append(exactlyOne(curActions))

        possible = possibleStates
        possibleStates = []
        nextStates = []
        nextCoords = []

        for state in possible:
            nxt, cur, curAction = state[0], state[1], state[2]
            nextCoords.append(nxt)
            nextStates.append(logic.PropSymbolExpr("P", nxt[0], nxt[1], time_step + 1))
            logicExpression = logic.PropSymbolExpr("P", nxt[0], nxt[1], time_step + 1)
            logicExpression = logicExpression  % (logic.PropSymbolExpr("P", cur[0], cur[1], time_step) & logic.PropSymbolExpr(curAction, time_step))
            kb.append(logic.to_cnf(logicExpression))

            next_successors = []
            next_actions = problem.actions(nxt)
            for next_action in next_actions:
                next_successor = (problem.result(nxt, next_action)[0], nxt, next_action)
                next_successors.append(next_successor)
            for next_successor in next_successors:
                checkExplored = next_successor[0]
                if (next_successor[0] not in exploredStates) or (exploredStates[checkExplored] > time_step + 1):
                    possibleStates.append(next_successor)
                    exploredStates[checkExplored] = time_step + 1

        kb.append(exactlyOne(nextStates))
        impossibleCoords = []
        for x in xrange(1, width + 1):
            for y in xrange(1, height + 1):
                if (x, y) not in nextCoords:
                    impossibleCoords.append(~logic.PropSymbolExpr("P", x, y, time_step + 1))

        impossible = logic.Expr("&", *impossibleCoords)
        kb.append(impossible)
        time_step += 1
    return extractActionSequence(model, orgActions)


def foodLogicPlan(problem):
    """
    Given an instance of a FoodSearchProblem, return a list of actions that help Pacman
    eat all of the food.
    Available actions are game.Directions.{NORTH,SOUTH,EAST,WEST}
    Note that STOP is not an available action.
    """
    orgActions = ['North', 'South', 'East', 'West']
    kb = []

    # Get starting constraints.
    initialState = problem.getStartState()
    grid = initialState[1]
    foodList = grid.asList()
    initPos = initialState[0]
    width, height = problem.getWidth(), problem.getHeight()
    kb.append(logic.PropSymbolExpr("P", initPos[0], initPos[1], 0))

    # Can't be in more than one place at a time.
    for x in range(1, width + 1):
        for y in range(1, height + 1):
            if (x != initPos[0] or y != initPos[1]):
                kb.append(~logic.PropSymbolExpr("P", x, y, 0))
    #kb.append(logic.Expr("&", *initNotPos))

    time_step = 0
    model = False
    possibleStates = []
    initialActions = problem.actions(initialState)
    for action in initialActions:
        successor = (problem.result(initialState, action)[0], initialState, action)
        possibleStates.append(successor)
    while 1:
        print("Time step: " + str(time_step))
        goal_check = list(kb)
        total_food = []
        for pos in foodList:
            food_check = []
            for t in range(0, time_step+1):
                food_check.append(logic.PropSymbolExpr("P", pos[0], pos[1], t))
            goal_check.append(atLeastOne(food_check))
        model = logic.pycoSAT(goal_check)
        
        if (model != False):
            break
        curActions = []
        for action in orgActions:
            curActions.append(logic.PropSymbolExpr(action, time_step))
        kb.append(exactlyOne(curActions))

        possible = possibleStates
        possibleStates = []
        nextStates = []
        nextCoords = []

        tempset = set()

        for state in possible:
            nxt, cur, curAction = state[0], state[1], state[2]
            nxtPos = nxt[0]
            curPos = cur[0]

            if nxtPos in tempset:
                continue

            tempset.add(nxtPos)

            nextCoords.append(nxtPos)
            nextStates.append(logic.PropSymbolExpr("P", nxtPos[0], nxtPos[1], time_step + 1))

            logicExpression = logic.PropSymbolExpr("P", nxtPos[0], nxtPos[1], time_step + 1)
            
            possibleEntry = []
            if not (nxtPos[0]-1 < 1):
                if not problem.isWall((nxtPos[0]-1, nxtPos[1])):
                    possibleEntry.append(logic.PropSymbolExpr("P", nxtPos[0]-1, nxtPos[1], time_step) & logic.PropSymbolExpr("East", time_step))
            if not (nxtPos[0]+1 > width+1):
                if not problem.isWall((nxtPos[0]+1, nxtPos[1])):
                    possibleEntry.append(logic.PropSymbolExpr("P", nxtPos[0]+1, nxtPos[1], time_step) & logic.PropSymbolExpr("West", time_step))
            if not (nxtPos[1]-1 < 1):
                if not problem.isWall((nxtPos[0], nxtPos[1]-1)):
                    possibleEntry.append(logic.PropSymbolExpr("P", nxtPos[0], nxtPos[1]-1, time_step) & logic.PropSymbolExpr("North", time_step))
            if not (nxtPos[1]+1 > height+1):
                if not problem.isWall((nxtPos[0], nxtPos[1]+1)):
                    possibleEntry.append(logic.PropSymbolExpr("P", nxtPos[0], nxtPos[1]+1, time_step) & logic.PropSymbolExpr("South", time_step))
            possibleEntry = logic.Expr("|", *possibleEntry)
            logicExpression = logicExpression % possibleEntry
            kb.append(logic.to_cnf(logicExpression))

            next_actions = problem.actions(nxt)
            for next_action in next_actions:
                next_successor = (problem.result(nxt, next_action)[0], nxt, next_action)
                possibleStates.append(next_successor)
        kb.append(exactlyOne(nextStates))
        allCoords = []
        for x in range(1, width + 1):
            for y in range(1, height + 1):
                allCoords.append(logic.PropSymbolExpr("P", x, y, time_step + 1))
        kb.append(exactlyOne(allCoords))
        time_step += 1
    return extractActionSequence(model, orgActions)


def foodGhostLogicPlan(problem):
    """
    Given an instance of a FoodGhostSearchProblem, return a list of actions that help Pacman
    eat all of the food and avoid patrolling ghosts.
    Ghosts only move east and west. They always start by moving East, unless they start next to
    and eastern wall. 
    Available actions are game.Directions.{NORTH,SOUTH,EAST,WEST}
    Note that STOP is not an available action.
    """
    def ghost_position(start_position, start_direction, time):
        if time < 0:
            return (-1, -1)
        elif time == 0:
            print start_position
            print "dfgdgfhdghdfghdfghdfghdfgbdrgbdrbdrbfbfsgbsdfbsdbf"
            return start_position
        else:
            i = 0
            curr_position = start_position
            curr_direction = start_direction

            while i < time:
                if curr_direction == "East":
                    print ";SDDFAFDA"
                    new_position = (curr_position[0] + 1, curr_position[1])
                    if problem.isWall(new_position):
                        new_position = (curr_position[0] - 1, curr_position[1])
                        curr_direction = "West"
                else:
                    new_position = (curr_position[0] - 1, curr_position[1])
                    if problem.isWall(new_position):
                        new_position = (curr_position[0] + 1, curr_position[1])
                        curr_direction = "East"
                curr_position = new_position
                i += 1
            return curr_position            

    ghostPositions = []
    initial_ghost_positions = problem.getGhostStartStates()
    for ghost in initial_ghost_positions:
        start_pos = ghost.configuration.getPosition()
        if not problem.isWall((start_pos[0] + 1, start_pos[1])):
            start_dir = "East"
        else:
            start_dir = "West"
        ghostPositions.append((start_pos, start_dir))

    orgActions = ['North', 'South', 'East', 'West']
    kb = []

    # Get starting constraints.
    initialState = problem.getStartState()
    grid = initialState[1]
    foodList = grid.asList()
    initPos = initialState[0]
    width, height = problem.getWidth(), problem.getHeight()
    kb.append(logic.PropSymbolExpr("P", initPos[0], initPos[1], 0))

    # Can't be in more than one place at a time.
    for x in range(1, width + 1):
        for y in range(1, height + 1):
            if (x != initPos[0] or y != initPos[1]):
                kb.append(~logic.PropSymbolExpr("P", x, y, 0))

    time_step = 0
    model = False
    possibleStates = []
    initialActions = problem.actions(initialState)
    for action in initialActions:
        successor = (problem.result(initialState, action)[0], initialState, action)
        possibleStates.append(successor)
    while 1:
        # print("Time step: " + str(time_step))
        goal_check = list(kb)
        total_food = []
        for pos in foodList:
            food_check = []
            for t in range(0, time_step+1):
                food_check.append(logic.PropSymbolExpr("P", pos[0], pos[1], t))
            goal_check.append(atLeastOne(food_check))
        model = logic.pycoSAT(goal_check)
        
        if (model != False):
            break
        curActions = []
        for action in orgActions:
            curActions.append(logic.PropSymbolExpr(action, time_step))
        kb.append(exactlyOne(curActions))

        possible = possibleStates
        possibleStates = []
        nextStates = []
        nextCoords = []

        tempset = set()

        ghostLocations = []

        for ghost in ghostPositions:
            if time_step == 0:
                x, y = ghost[0]
                ghostLocations.append((x, y))
            else:
                x, y = ghost_position(ghost[0], ghost[1], time_step + 1)
                prev_x, prev_y = ghost_position(ghost[0], ghost[1], time_step)
                ghostLocations.append((prev_x, prev_y))
                ghostLocations.append((x, y))

        for state in possible:
            nxt, cur, curAction = state[0], state[1], state[2]
            nxtPos = nxt[0]
            curPos = cur[0]

            if nxtPos in ghostLocations:
                continue

            if nxtPos in tempset:
                continue

            tempset.add(nxtPos)

            nextCoords.append(nxtPos)
            nextStates.append(logic.PropSymbolExpr("P", nxtPos[0], nxtPos[1], time_step + 1))

            logicExpression = logic.PropSymbolExpr("P", nxtPos[0], nxtPos[1], time_step + 1)
            
            possibleEntry = []
            if not (nxtPos[0]-1 < 1):
                if not problem.isWall((nxtPos[0]-1, nxtPos[1])):
                    possibleEntry.append(logic.PropSymbolExpr("P", nxtPos[0]-1, nxtPos[1], time_step) & logic.PropSymbolExpr("East", time_step))
            if not (nxtPos[0]+1 > width+1):
                if not problem.isWall((nxtPos[0]+1, nxtPos[1])):
                    possibleEntry.append(logic.PropSymbolExpr("P", nxtPos[0]+1, nxtPos[1], time_step) & logic.PropSymbolExpr("West", time_step))
            if not (nxtPos[1]-1 < 1):
                if not problem.isWall((nxtPos[0], nxtPos[1]-1)):
                    possibleEntry.append(logic.PropSymbolExpr("P", nxtPos[0], nxtPos[1]-1, time_step) & logic.PropSymbolExpr("North", time_step))
            if not (nxtPos[1]+1 > height+1):
                if not problem.isWall((nxtPos[0], nxtPos[1]+1)):
                    possibleEntry.append(logic.PropSymbolExpr("P", nxtPos[0], nxtPos[1]+1, time_step) & logic.PropSymbolExpr("South", time_step))
            possibleEntry = logic.Expr("|", *possibleEntry)
            logicExpression = logicExpression % possibleEntry
            kb.append(logic.to_cnf(logicExpression))

            next_actions = problem.actions(nxt)
            for next_action in next_actions:
                next_successor = (problem.result(nxt, next_action)[0], nxt, next_action)
                possibleStates.append(next_successor)

        kb.append(exactlyOne(nextStates))
        allCoords = []
        for x in range(1, width + 1):
            for y in range(1, height + 1):
                allCoords.append(logic.PropSymbolExpr("P", x, y, time_step + 1))
        kb.append(exactlyOne(allCoords))
        time_step += 1
    return extractActionSequence(model, orgActions)

# Abbreviations
plp = positionLogicPlan
flp = foodLogicPlan
fglp = foodGhostLogicPlan

# Some for the logic module uses pretty deep recursion on long expressions
sys.setrecursionlimit(100000)



