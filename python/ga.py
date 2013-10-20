from deap import base, creator, tools
from settings import fitness_weights
#import numpy as np
import random

IND_SIZE = 22 ### Number of parameters in an individual

parameter_scaling = {
    'ssmm_nAgents' : 50,
    'sc_nAgents' : 500
}


def get_weights():
	return tuple([fitness_weights[k] for k in sorted(fitness_weights.keys())])

def get_random_individual():
	pass

def make_types():
	### Create FitnessMin class with the fitness weights
	creator.create("FitnessMulti", base.Fitness, weights = get_weights())
	### Create individual with FitenessMin class
	creator.create("Individual", list, fitness = creator.FitenessMulti)

	tb = base.Toolbox()
	#tb.register("attr_minLat", random.randint, 0, 500)
	#tb.register("attr_latSpan", random.randint, 0, 1000)
	tb.register("attr_ssmm_nAgents", random.randint, 0, 50)
	tb.register("attr_sc_nAgents", random.randint, 0, 500)


	tb.register('individual', tools.initCycle, creator.Individual, (tb.attr_ssmm_nAgents, tb.attr_sc_nAgents), n=1)
	
	tb.register("population", tools.initRepeat, list, tb.individual)
	tb.population(n=10)


if __name__ == "__main__":
	pass