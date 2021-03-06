from deap import base, creator, tools

import settings
#import numpy as np
import random
from fitness import evaluate_simulation_results, get_named_stats
from copy import deepcopy
from collections import OrderedDict
from scoop import futures
from datetime import datetime
from os import makedirs
from IO import store_generation_as_data_matrix
import numpy as np
import shutil
import argparse
import socket
import utils

parser = argparse.ArgumentParser()
parser.add_argument('--skip-scoop', action="store_true", default=False)
parser.add_argument('--dataset-name', '-d')
arguments = parser.parse_args()
assert arguments.dataset_name, "Please specify dataset name using the '-d' option. (e.g. '-d d4')"
print arguments

def evaluate(individual, generation, graph_folder):
	if settings.VERBOSE >= 1: print "Evaluating new individual"
	parameters = scale_genes_to_parameters(individual)
	if settings.VERBOSE >= 2: print parameters
	if verify_simulation_parameters(parameters):
		
		data, saved_simulation_data_ids = evaluate_simulation_results(graph_folder, generation, parameters, settings.reps, autorun=True)
		stats = get_named_stats(data, settings.fitness_weights.keys())
		stats = tuple(OrderedDict(stats['mean']).values())
		print "Gen %s: Finished simulation with parameters: %s"%(generation, scale_genes_to_parameters(individual, False))
	else:
		print "Invalid gene"
		stats = get_invalid_gene_fitness()
		saved_simulation_data_ids = list()
	return (stats, saved_simulation_data_ids)


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

	
	if return_full_parameter_set:
		parameters = settings.get_fixed_parameters()
		parameters.update(scaled_parameters)
	else:
		parameters = scaled_parameters
	return parameters

def create_healthy_individual():
	while True:
		individual = toolbox.individual()
		if verify_simulation_parameters(scale_genes_to_parameters(individual)):
			return individual 

def create_healthy_population():
	population = list()
	for i in range(settings.population_size):
		population.append(create_healthy_individual())
	return population




creator.create("FitnessMulti", base.Fitness, weights = settings.fitness_weights.values())
### Create individual with FitenessMin class
creator.create("Individual", list, fitness = creator.FitnessMulti)

toolbox = base.Toolbox()

toolbox.register("attribute", random.random)
toolbox.register("individual", tools.initRepeat, creator.Individual, toolbox.attribute, n=len(settings.parameters_in_genes))

toolbox.register("population", tools.initRepeat, list, toolbox.individual)

toolbox.register("mate", tools.cxTwoPoints)
toolbox.register("mutate", tools.mutGaussian, mu=0, sigma=0.2, indpb=0.2)
toolbox.register("select", tools.selTournament, tournsize=settings.tournament_size)
toolbox.register("evaluate", evaluate)


def initialize_worker():
	start_time = socket.gethostname() + '_' + datetime.now().strftime("%Y%m%d-%H%M%S")
	data_folder = '../data/gene_data/%s/%s'%(arguments.dataset_name, start_time)
	gene_data_folder = '%s/generations/'%(data_folder)
	graph_folder = '%s/graphs/'%(data_folder)

	makedirs(gene_data_folder)
	makedirs(graph_folder)
	shutil.copyfile('settings.py','%s/settings.py'%(data_folder))
	if not arguments.skip_scoop:
		toolbox.register("map", futures.map)
	return graph_folder, gene_data_folder

if __name__ == "__main__":
	#pool = multiprocessing.Pool(processes=10)
	
	graph_folder, gene_data_folder = initialize_worker()

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
		        before_mutation = deepcopy(mutant)
		        times_mutated = 0
		        keep_mutating = True
		        while keep_mutating:
		        	toolbox.mutate(mutant)
			        if verify_simulation_parameters(scale_genes_to_parameters(mutant)):
			        	break
			        else:
			        	mutant = before_mutation
			        times_mutated += 1
			        if times_mutated == 1000:
			        	print "Created new individual after mutating invalid individual 1000 times..."
			        	mutant = create_healthy_individual()
			        	keep_mutating = False
		        del mutant.fitness.values

		# Evaluate the individuals with an invalid fitness
		invalid_ind = [ind for ind in offspring if not ind.fitness.valid]
		print "Evaluating the fitness of %s individuals"%len(invalid_ind)
		if len(invalid_ind) > 0:
			rep_data = list()
			for ind in range(len(invalid_ind)):
				rep_data.append(np.zeros((len(settings.ga_reps), len(settings.fitness_types.keys()))))
		
			new_data = list()
			for rep in settings.ga_reps:	
				results = toolbox.map(toolbox.evaluate, invalid_ind, np.repeat(g, len(invalid_ind)), np.repeat(graph_folder, len(invalid_ind)))
				
				### Unpacking long list of short tuples into short list of long tuples (sort of like transposing..)
				results = zip(*results)
				fitnesses = list(results[0])
				ids = list(results[1])

				for i, (ind, fit, simdata_ids) in enumerate(zip(invalid_ind, fitnesses, ids)):
				    rep_data[i][rep] = fit

				    scaled_ind = scale_genes_to_parameters(ind, False)

				    new_data.append({'ind': scaled_ind, 'fit': tuple(fit), 'data_ids':tuple(simdata_ids)})

			fitnesses_mean = [np.mean(rep_data[i],0) for i in range(len(rep_data))]
			
			for ind, fit in zip(invalid_ind, fitnesses_mean):
			    ind.fitness.values = fit
		
		# The population is entirely replaced by the offspring
		pop[:] = offspring
		print new_data
		store_generation_as_data_matrix(new_data, g, gene_data_folder)





