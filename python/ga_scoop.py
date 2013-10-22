from deap import base, creator, tools

import settings
#import numpy as np
import random
import dataAnalysis
from itertools import imap
from collections import OrderedDict
from scoop import futures
import yaml


def evaluate(individual):
	parameters = scale_genes_to_parameters(individual=individual)
	
	if verify_simulation_parameters(parameters):
		data = dataAnalysis.evaluate_simulation_results(parameters, settings.reps, autorun=True)
		stats = dataAnalysis.get_named_stats(data, settings.fitness_weights.keys())
		stats = tuple(OrderedDict(stats['mean']).values())
	else:
		print "Generated invalid gene"
		stats = get_invalid_gene_fitness()
	return stats



def get_invalid_gene_fitness():
	return tuple([v * settings.deadborn_gene_fitness * -1 for v in settings.fitness_weights.itervalues()])

def verify_simulation_parameters(parameters):
	is_valid = True
	for parameter, minval in settings.parameter_minvals.iteritems():
		if parameters[parameter] < minval:
			is_valid = False
	return is_valid

def scale_genes_to_parameters(individual):
	parameters = dict()
	for subdict in imap(lambda parameter, scaling, gene: {parameter: int(scaling*gene)}, settings.parameter_scaling.iterkeys(), settings.parameter_scaling.itervalues(), individual):
		parameters.update(subdict)
	return parameters



creator.create("FitnessMulti", base.Fitness, weights = settings.fitness_weights.values())
### Create individual with FitenessMin class
creator.create("Individual", list, fitness = creator.FitnessMulti)

toolbox = base.Toolbox()

toolbox.register("attribute", random.random)
toolbox.register("individual", tools.initRepeat, creator.Individual, toolbox.attribute, n=len(settings.parameter_scaling))

toolbox.register("population", tools.initRepeat, list, toolbox.individual)

toolbox.register("mate", tools.cxTwoPoints)
toolbox.register("mutate", tools.mutGaussian, mu=0, sigma=1, indpb=0.1)
toolbox.register("select", tools.selTournament, tournsize=3)
toolbox.register("evaluate", evaluate)

hall = tools.HallOfFame(1000)
toolbox.register("update_hall_of_fame", hall.update)


if __name__ == "__main__":

	#pool = multiprocessing.Pool(processes=10)
	toolbox.register("map", futures.map)

	pop = toolbox.population(settings.population_size)
	for g in range(settings.n_generations):
		print "********************************** GENERATION %s **********************************"%g
		# Select the next generation individuals
		offspring = toolbox.select(pop, len(pop))
		
		# Clone the selected individuals
		offspring = map(toolbox.clone, offspring)

		# Apply crossover on the offspring
		for child1, child2 in zip(offspring[::2], offspring[1::2]):
		    if random.random() < settings.crossover_prob:
		        toolbox.mate(child1, child2)
		        del child1.fitness.values
		        del child2.fitness.values

		# Apply mutation on the offspring
		for mutant in offspring:
		    if random.random() < settings.mutation_prob:
		        toolbox.mutate(mutant)
		        del mutant.fitness.values

		# Evaluate the individuals with an invalid fitness
		invalid_ind = [ind for ind in offspring if not ind.fitness.valid]
		#print invalid_ind

		fitnesses = toolbox.map(toolbox.evaluate, invalid_ind)
		for ind, fit in zip(invalid_ind, fitnesses):
		    print fit
		    ind.fitness.values = fit

		#for ind in offspring:
		#	print ind.fitness

		# The population is entirely replaced by the offspring
		pop[:] = offspring
		toolbox.update_hall_of_fame(pop)

	individuals = list()
	for individual in hall.items:
		individuals.append(scale_genes_to_parameters(individual))

	f = open('genes.yaml', 'w')
	yaml.dump(individuals, f)
	f.close()		
		#print zip(settings.parameter_scaling.keys(), parameters)
	#algorithms.eaSimple(toolbox.population(10), toolbox, cxpb=0.5, mutpb=0.2, ngen=500)

