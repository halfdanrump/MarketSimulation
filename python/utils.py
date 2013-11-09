import numpy as np
from settings import default_parameters as defpar
from settings import data_types
from os import listdir
import yaml
import re

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

def load_all_generation_data(folder_name):
	generations = dict()
	gen_files = [f for f in listdir(folder_name) if re.match('gen\d+\.yaml', f)]
	print gen_files
	for f in gen_files:
		generations.update({int(re.findall('\d+', f)[0]): yaml.load(open(folder_name + f))})
	return generations

def pool_genes(generations):
	all_genes = list()
	all_fitnesses = list()
	for generation, new_genes in generations.items():
		for gene in new_genes['genes']:
			all_genes.append(gene['ind'])
			all_fitnesses.append(gene['fit'])
	return all_genes, all_fitnesses
