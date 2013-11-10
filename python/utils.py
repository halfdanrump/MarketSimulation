import numpy as np
from settings import default_parameters as defpar, parameters_in_genes, data_types, fitness_weights
from os import listdir
import yaml
import re
from pandas import DataFrame

def get_fundamental_after_shock():
	return defpar['fundamental_initial_value'] + defpar['fundamental_shock_size']

def empty_data_matrix(n_rows = 1):
	return np.zeros(shape = n_rows, dtype = data_types.items())

def load_tradeprice_data(filename):
    """
    Loads the data (stored as .npz) used to generate the tradePrice plots
    """
    d = np.load(filename).items()[0][1].item()
    rounds = d['rounds']
    prices = d['tradePrice']
    return rounds, prices

def load_single_generation_data(filename):
	return yaml.load(filename)

def load_all_YAML_generation_data_and_make_data_matrix(folder_name):
	generations = __load_all_generation_data(folder_name)
	g, f = __pool_genes(generations)
	return __make_data_matrix(g, f)

def __load_all_generation_data(folder_name):
	generations = dict()
	gen_files = [f for f in listdir(folder_name) if re.match('gen\d+\.yaml', f)]
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
	fit_dtype = [(stat, float) for stat in data_types.keys()]
	par_matrix = np.zeros(shape = len(all_genes), dtype = par_dtype)
	fit_matrix = np.zeros(shape = len(all_genes), dtype = fit_dtype)
	for i, (gene, fitness) in enumerate(zip(all_genes, all_fitnesses)):
		for par, val in gene.items():
			par_matrix[par][i] = val
		for k, stat in enumerate(fitness_weights.keys()):
			fit_matrix[stat][i] = fitness[k]
	return DataFrame(par_matrix), DataFrame(fit_matrix)

def store_generation_as_data_matrix(generation_data, generation_number, path):
	all_genes = list()
	all_fitnesses = list()
	print generation_data
	for gene in generation_data:
		all_genes.append(gene['ind'])
		all_fitnesses.append(gene['fit'])

	par_dtype = [(par, int) for par in parameters_in_genes] 
	fit_dtype = [(stat, float) for stat in data_types.keys()]
	par_matrix = np.zeros(shape = len(all_genes), dtype = par_dtype)
	fit_matrix = np.zeros(shape = len(all_genes), dtype = fit_dtype)
	
	for i, (gene, fitness) in enumerate(zip(all_genes, all_fitnesses)):
		for par, val in gene.items():
			par_matrix[par][i] = val
		for k, stat in enumerate(fitness_weights.keys()):
			fit_matrix[stat][i] = fitness[k]
	
	np.savez_compressed(path + 'gen_%s_pars'%generation_number, par_matrix)
	np.savez_compressed(path + 'gen_%s_fit'%generation_number, fit_matrix)

def load_all_generations_as_DataFrame(folder_name):
	#gen_par = dict()
	gen_pars_files = [f for f in listdir(folder_name) if re.match('gen_\d+_pars\.npz', f)]
	gen_fit_files = [f for f in listdir(folder_name) if re.match('gen_\d+_fit\.npz', f)]
	print gen_pars_files, gen_fit_files
	for f in gen_pars_files:
		print 'Loading %s'%f
		d = np.load(folder_name + f)
		print d.items()[0][1]
		#d.items()[0]['arr_0']
		#print d
		#generations.update({int(re.findall('\d+', f)[0]): yaml.load(open(folder_name + f))})
	#return generations	


