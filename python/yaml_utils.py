import yaml
import re
import os
from settings import parameters_in_genes, fitness_types, fitness_weights
from pandas import DataFrame
import numpy as np

def load_single_generation_data(filename):
	return yaml.load(filename)

def load_all_YAML_generation_data_and_make_data_matrix(folder_name):
	generations = __load_all_generation_data(folder_name)
	g, f = __pool_genes(generations)
	return __make_data_matrix(g, f)

def __load_all_generation_data(folder_name):
	generations = dict()
	gen_files = [f for f in os.listdir(folder_name) if re.match('gen\d+\.yaml', f)]
	for f in gen_files:
		print 'Loading %s'%f
		generations.update({int(re.findall('\d+', f)[0]): yaml.load(open(folder_name + f))})
	return generations

def __pool_genes(generations):
	all_genes = list()
	all_fitnesses = list()
	for generation, new_genes in generations.items():
		for gene in new_genes['genes']:
			all_genes.append(gene['ind'])
			all_fitnesses.append(gene['fit'])
	return all_genes, all_fitnesses

def __make_data_matrix(all_genes, all_fitnesses):
	par_dtype = [(par, int) for par in parameters_in_genes] 
	fit_dtype = [(stat, float) for stat in fitness_types.keys()]
	par_matrix = np.zeros(shape = len(all_genes), dtype = par_dtype)
	fit_matrix = np.zeros(shape = len(all_genes), dtype = fit_dtype)
	for i, (gene, fitness) in enumerate(zip(all_genes, all_fitnesses)):
		for par, val in gene.items():
			par_matrix[par][i] = val
		for k, stat in enumerate(fitness_weights.keys()):
			fit_matrix[stat][i] = fitness[k]
	return DataFrame(par_matrix), DataFrame(fit_matrix)