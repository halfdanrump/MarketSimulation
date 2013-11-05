from deap import base, creator, tools

import settings
#import numpy as np
import random
import dataAnalysis

from collections import OrderedDict
from scoop import futures
import yaml
from datetime import datetime
from os import makedirs


def evaluate(individual):
	parameters = scale_genes_to_parameters(individual)
	if verify_simulation_parameters(parameters):
		data = dataAnalysis.evaluate_simulation_results(graph_folder, parameters, settings.reps, autorun=True)
		stats = dataAnalysis.get_named_stats(data, settings.fitness_weights.keys())
		stats = tuple(OrderedDict(stats['mean']).values())
		#print "Stats: %s, parameters: %s"%(stats, scale_genes_to_parameters(individual))
	else:
		print "Generated invalid gene: %s"%scale_genes_to_parameters(individual)
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

def scale_genes_to_parameters(individual, return_full_parameter_set = True):
	
	scaled_parameters = dict()
	for i, parameter in enumerate(settings.parameters_in_genes):
		scaling = settings.parameter_scaling[parameter]	
		scaled_parameters[parameter] = int(scaling * individual[i])

	print "scaled_parameters: %s"%scaled_parameters
	if return_full_parameter_set:
		parameters = settings.get_fixed_parameters()
		parameters.update(scaled_parameters)
	else:
		parameters = scaled_parameters
	#for subdict in imap(lambda parameter, scaling, gene: {parameter: int(scaling*gene)}, settings.parameter_scaling.iterkeys(), settings.parameter_scaling.itervalues(), individual):
	#	parameters.update(subdict)
	return parameters

def create_healthy_population():
	population = list()
	for i in range(settings.population_size):
		while True:
			individual = toolbox.individual()
			if verify_simulation_parameters(scale_genes_to_parameters(individual)):
				population.append(individual)
				break
	return population


creator.create("FitnessMulti", base.Fitness, weights = settings.fitness_weights.values())
### Create individual with FitenessMin class
creator.create("Individual", list, fitness = creator.FitnessMulti)

toolbox = base.Toolbox()

toolbox.register("attribute", random.random)
toolbox.register("individual", tools.initRepeat, creator.Individual, toolbox.attribute, n=len(settings.parameters_in_genes))

toolbox.register("population", tools.initRepeat, list, toolbox.individual)

toolbox.register("mate", tools.cxTwoPoints)
toolbox.register("mutate", tools.mutGaussian, mu=0, sigma=0.1, indpb=0.1)
toolbox.register("select", tools.selTournament, tournsize=int(settings.population_size * settings.tournament_selection_percentage))
toolbox.register("evaluate", evaluate)

hall = tools.HallOfFame(1000)
toolbox.register("update_hall_of_fame", hall.update)


start_time = datetime.now().strftime("%Y%m%d-%H%M%S")
gene_data_folder = '../data/gene_data/%s/'%(start_time)
graph_folder = '%sgraphs/'%(gene_data_folder)


if __name__ == "__main__":

	#pool = multiprocessing.Pool(processes=10)
	makedirs(gene_data_folder)
	makedirs(graph_folder)
	
	toolbox.register("map", futures.map)

	pop = create_healthy_population()
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
		print "Evaluating the fitness of %s individuals"%len(invalid_ind)

		fitnesses = toolbox.map(toolbox.evaluate, invalid_ind)
		for ind, fit in zip(invalid_ind, fitnesses):
		    print fit
		    ind.fitness.values = fit

		#for ind in offspring:
		#	print ind.fitness

		# The population is entirely replaced by the offspring
		pop[:] = offspring
		toolbox.update_hall_of_fame(pop)

		individuals = map(lambda k, v: {'fit':k, 'gene':v}, [v.getValues() for v in hall.keys], map(scale_genes_to_parameters, hall.items, [False for i in range(len(hall.items))]))
		data_to_save = {'fixed_parameters':settings.default_parameters, 'individuals':individuals}
		f = open('../data/gene_data/%s/gen%s.yaml'%(start_time, g), 'w')
		yaml.dump(data_to_save, f)
		f.close()
		print "Saved hall of fame after generation %s to %s"%(g, f.name)
		#print zip(settings.parameter_scaling.keys(), parameters)
	#algorithms.eaSimple(toolbox.population(10), toolbox, cxpb=0.5, mutpb=0.2, ngen=500)

