# multiAgents.py
# --------------
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


from util import manhattanDistance
from game import Directions
import random, util

from game import Agent

class ReflexAgent(Agent):
    """
      A reflex agent chooses an action at each choice point by examining
      its alternatives via a state evaluation function.

      The code below is provided as a guide.  You are welcome to change
      it in any way you see fit, so long as you don't touch our method
      headers.
    """


    def getAction(self, gameState):
        """
        You do not need to change this method, but you're welcome to.

        getAction chooses among the best options according to the evaluation function.

        Just like in the previous project, getAction takes a GameState and returns
        some Directions.X for some X in the set {North, South, West, East, Stop}
        """
        # Collect legal moves and successor states
        legalMoves = gameState.getLegalActions()

        # Choose one of the best actions
        scores = [self.evaluationFunction(gameState, action) for action in legalMoves]
        bestScore = max(scores)
        bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
        chosenIndex = random.choice(bestIndices) # Pick randomly among the best

        "Add more of your code here if you want to"

        return legalMoves[chosenIndex]

    def evaluationFunction(self, currentGameState, action):
        """
        Design a better evaluation function here.

        The evaluation function takes in the current and proposed successor
        GameStates (pacman.py) and returns a number, where higher numbers are better.

        The code below extracts some useful information from the state, like the
        remaining food (newFood) and Pacman position after moving (newPos).
        newScaredTimes holds the number of moves that each ghost will remain
        scared because of Pacman having eaten a power pellet.

        Print out these variables to see what you're getting, then combine them
        to create a masterful evaluation function.
        """
        # Useful information you can extract from a GameState (pacman.py)
        successorGameState = currentGameState.generatePacmanSuccessor(action)
        newPos = successorGameState.getPacmanPosition()
        newFood = successorGameState.getFood()
        newGhostStates = successorGameState.getGhostStates()
        newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]

        if newFood.count() == 0:
            return 1000000
        foodPosList = newFood.asList()
        foodDistances = []

        for foodPos in foodPosList:
            foodDistances.append(util.manhattanDistance(newPos, foodPos))

        gridSize = newFood.width * newFood.height
        # the maine heuristic is based off how close pacman is to the nearest food
        # as well as the number of food left with respect to the total size of the grid
        h  = -(min(foodDistances) + gridSize * newFood.count())

        ghostPos = []
        ghostDist = []
        for ghostState in newGhostStates:
            if ghostState.scaredTimer == 0:
                ghostPos.append(ghostState.getPosition())

        for ghostPositions in ghostPos:
            ghostDist.append(util.manhattanDistance(newPos, ghostPositions))

        #stay away from ghosts
        if ghostDist and min(ghostDist) <= 1:
            return -1000000
        return h

def scoreEvaluationFunction(currentGameState):
    """
      This default evaluation function just returns the score of the state.
      The score is the same one displayed in the Pacman GUI.

      This evaluation function is meant for use with adversarial search agents
      (not reflex agents).
    """
    return currentGameState.getScore()

class MultiAgentSearchAgent(Agent):
    """
      This class provides some common elements to all of your
      multi-agent searchers.  Any methods defined here will be available
      to the MinimaxPacmanAgent & AlphaBetaPacmanAgent.

      You *do not* need to make any changes here, but you can if you want to
      add functionality to all your adversarial search agents.  Please do not
      remove anything, however.

      Note: this is an abstract class: one that should not be instantiated.  It's
      only partially specified, and designed to be extended.  Agent (game.py)
      is another abstract class.
    """

    def __init__(self, evalFn = 'scoreEvaluationFunction', depth = '2'):
        self.index = 0 # Pacman is always agent index 0
        self.evaluationFunction = util.lookup(evalFn, globals())
        self.depth = int(depth)

class MinimaxAgent(MultiAgentSearchAgent):
    """
      Your minimax agent (question 7)
    """

    def getAction(self, gameState):
        """
          Returns the minimax action from the current gameState using self.depth
          and self.evaluationFunction.

          Here are some method calls that might be useful when implementing minimax.

          gameState.getLegalActions(agentIndex):
            Returns a list of legal actions for an agent
            agentIndex=0 means Pacman, ghosts are >= 1

          gameState.generateSuccessor(agentIndex, action):
            Returns the successor game state after an agent takes an action

          gameState.getNumAgents():
            Returns the total number of agents in the game
        """
        import sys

        def minValue(state, agent, iter):
            agent %= state.getNumAgents()
            possibleActions = state.getLegalActions(agent)
            if len(possibleActions) == 0:
                return self.evaluationFunction(state)
            v = sys.maxint
            for action in possibleActions:
                if (agent+1) % state.getNumAgents() == 0:
                    v = min(v, maxValue(state.generateSuccessor(agent, action), agent + 1, iter - 1))
                else:   # agent is another ghost
                    v = min(v, minValue(state.generateSuccessor(agent, action), agent + 1, iter - 1))
            return v

        def maxValue(state, agent, iter):
            agent %= state.getNumAgents()
            if iter == 0:
                return self.evaluationFunction(state)
            possibleActions = state.getLegalActions(agent)
            if len(possibleActions) == 0:
                return self.evaluationFunction(state)
            v = -sys.maxint
            for action in possibleActions:
                v = max(v, minValue(state.generateSuccessor(agent, action), agent + 1, iter - 1))
            return v

        agent = 0
        iterations = gameState.getNumAgents() * self.depth
        possibleActions = gameState.getLegalActions(agent)
        v = {}
        for action in possibleActions:
            nextState = gameState.generateSuccessor(agent,action)
            v[minValue(nextState, agent + 1, iterations-1)] = action
        return v[max(v)]

class ExpectimaxAgent(MultiAgentSearchAgent):
    """
      Your expectimax agent (question 8)
    """

    def getAction(self, gameState):
        """
          Returns the expectimax action using self.depth and self.evaluationFunction

          All ghosts should be modeled as choosing uniformly at random from their
          legal moves.
        """
        import sys

        def minValue(state, agent, iter):
            agent %= state.getNumAgents()
            possibleActions = state.getLegalActions(agent)
            if len(possibleActions) == 0:
                return self.evaluationFunction(state)
            v = []
            for action in possibleActions:
                if (agent+1) % state.getNumAgents() == 0:
                    v.append(float(maxValue(state.generateSuccessor(agent, action), agent + 1, iter - 1)))
                else:
                    v.append(float(minValue(state.generateSuccessor(agent, action), agent + 1, iter - 1)))
            totalVal = 0
            for value in v:
                totalVal += value
            return totalVal/float(len(v))


        def maxValue(state, agent, iter):
            agent %= state.getNumAgents()
            if iter == 0:
                return self.evaluationFunction(state)
            possibleActions = state.getLegalActions(agent)
            if len(possibleActions) == 0:
                return self.evaluationFunction(state)
            v = -sys.maxint
            for action in possibleActions:
                v = max(v, minValue(state.generateSuccessor(agent, action), agent + 1, iter - 1))
            return v
            
        agent = 0
        iterations = gameState.getNumAgents() * self.depth
        possibleActions = gameState.getLegalActions(agent)
        v = {}
        for action in possibleActions:
            nextState = gameState.generateSuccessor(agent,action)
            v[minValue(nextState, agent + 1, iterations-1)] = action
        return v[max(v)]

def betterEvaluationFunction(currentGameState):
    """
      Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
      evaluation function (question 9).

      DESCRIPTION: - our maing goal is to use the same heuristic as from problem 5
                        -the heuristic gives a better score if pacman is closer to food
                        and if the amount of food left with relationship to the grid size
                        is minimized.
                        -if the ghost are scared (for at least 4 more states), we give preference 
                        to pacman if he is at least 2 spots away from the ghost
                        -however if a ghost is not scared, we give an even higher preference 
                        if pacman is at least 4 spots away (we really don't want him to die)
                        -we also give bonus points to if we make a ghost scared (i.e. eat a power pellet)
                        -finally we automatically want the state if we have a winning state or a state
                        with a score of over 1000
    """
    pos = currentGameState.getPacmanPosition()
    food = currentGameState.getFood()
    ghostStates = currentGameState.getGhostStates()
    scaredTimes = [ghostState.scaredTimer for ghostState in ghostStates]

    if food.count() == 0:
        return 1000000
    if currentGameState.getScore() > 1000:
        return 1000000

    foodPosList = food.asList()
    foodDistances = []

    for foodPos in foodPosList:
        foodDistances.append(util.manhattanDistance(pos, foodPos))

    gridSize = food.width * food.height
    h  = -(min(foodDistances) + gridSize * food.count()) + currentGameState.getScore()

    ghostPos = []
    ghostDist = []
    scaredGhostPos = []
    scaredGhostDist = []
    for ghostState in ghostStates:
        if ghostState.scaredTimer <= 4:
            ghostPos.append(ghostState.getPosition())
        else:
            scaredGhostPos.append(ghostState.getPosition())
            h += 1000

    for ghostPositions in ghostPos:
        ghostDist.append(util.manhattanDistance(pos, ghostPositions))
    for ghostPositions in scaredGhostPos:
        scaredGhostDist.append(util.manhattanDistance(pos, ghostPositions))

    if ghostDist and min(ghostDist) <= 4:
        h -= 1000000

    if scaredGhostDist and min(scaredGhostDist) <= 2:
        h += 1000

    return h

# Abbreviation
better = betterEvaluationFunction

