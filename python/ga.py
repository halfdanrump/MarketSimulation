from deap import base, creator, tools, algorithms
from settings import fitness_weights, reps, parameter_scaling, parameter_minvals, deadborn_gene_fitness
#import numpy as np
import random
import dataAnalysis
from itertools import imap
from collections import OrderedDict

IND_SIZE = len(parameter_scaling) ### Number of parameters in an individual

def evaluate(individual):
	parameters = scale_genes_to_parameters(individual=individual)
	if verify_simulation_parameters(parameters):
		data = dataAnalysis.evaluate_simulation_results(parameters, reps, autorun=True)
		stats = dataAnalysis.get_named_stats(data, fitness_weights.keys())
		stats = tuple(OrderedDict(stats['mean']).values())
	else:
		stats = tuple([v * deadborn_gene_fitness * -1 for v in fitness_weights.itervalues()])
	print stats
	return stats

def verify_simulation_parameters(parameters):
	is_valid = True
	for parameter, minval in parameter_minvals.iteritems():
		if parameters[parameter] < minval:
			is_valid = False
	return is_valid

def scale_genes_to_parameters(individual):
	parameters = dict()
	for subdict in imap(lambda parameter, scaling, gene: {parameter: int(scaling*gene)}, parameter_scaling.iterkeys(), parameter_scaling.itervalues(), individual):
		parameters.update(subdict)
	return parameters

def setup_toolbox():

### Create FitnessMin class with the fitness weights
	creator.create("FitnessMulti", base.Fitness, weights = fitness_weights.values())
	### Create individual with FitenessMin class
	creator.create("Individual", list, fitness = creator.FitnessMulti)

	toolbox = base.Toolbox()

	toolbox.register("attribute", random.random)
	toolbox.register("individual", tools.initRepeat, creator.Individual, toolbox.attribute, n=IND_SIZE)

	toolbox.register("population", tools.initRepeat, list, toolbox.individual)
	toolbox.population(n=1000)

	toolbox.register("mate", tools.cxTwoPoints)
	toolbox.register("mutate", tools.mutGaussian, mu=0, sigma=1, indpb=0.1)
	toolbox.register("select", tools.selTournament, tournsize=3)
	toolbox.register("evaluate", evaluate)
	return toolbox

def run_ga():
	toolbox = setup_toolbox()
	algorithms.eaSimple(toolbox.population(10), toolbox, cxpb=0.5, mutpb=0.2, ngen=500)




