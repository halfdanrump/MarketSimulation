import settings
from collections import Iterable
from shutil import rmtree
import numpy as np
from settings import fitness_types, fitness_weights, parameters_in_genes
from pandas import DataFrame
import re
import os
#from shutils import rmtree

def check_simulation_complete(full_simulation_log_path):
    try:
        with open(full_simulation_log_path + 'finished.txt'): 
            print "Found completed data: %s"%full_simulation_log_path
            return True
    except IOError:
        return False

def delete_simulation_data(log_folder):
	rmtree(log_folder)

def get_logfolders(parameters = {}, rep = [], random_path = ""):
	assert parameters, "Please specify parameters"
	assert rep, "Please specify repetition"
	assert isinstance(rep, Iterable), "rep must be an iterable"
	return [settings.log_root_folder + str(hash(repr(sorted(parameters.items())))) + '/%s/%s/'%(random_path, r) for r in rep]

def load_trade_log_data(simulation_log_path):
    return np.genfromtxt(simulation_log_path + 'columnLog_transactionBased_stock0.csv', names=True, dtype=int, delimiter=',', usecols=(0,1))


def store_generation_as_data_matrix(generation_data, generation_number, path):
    all_genes = list()
    all_fitnesses = list()
    print generation_data
    for gene in generation_data:
        all_genes.append(gene['ind'])
        all_fitnesses.append(gene['fit'])

    par_dtype = [(par, int) for par in parameters_in_genes] 
    fit_dtype = [(stat, float) for stat in fitness_types.keys()]
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
    all_par = DataFrame()
    all_fit = DataFrame()
    gen_pars_files = [f for f in os.listdir(folder_name) if re.match('gen_\d+_pars\.npz', f)]
    gen_fit_files = [f for f in os.listdir(folder_name) if re.match('gen_\d+_fit\.npz', f)]
    print "Loading data for %s generations..."%len(gen_pars_files)
    for p,f in zip(gen_pars_files, gen_fit_files):
        dp = np.load(folder_name + p)
        df = np.load(folder_name + f)
        all_par = all_par.append(DataFrame(dp.items()[0][1]))
        all_fit = all_fit.append(DataFrame(df.items()[0][1]))
        dp.close()
        dp.close()
    
    ### Remove invalid genes
    all_par = all_par.reset_index(drop=True)
    all_fit = all_fit.reset_index(drop=True)
    i, = np.where(all_fit['longest_interval_within_margin'] < 0)
    all_par = all_par.drop(i)
    all_fit = all_fit.drop(i)
    all_par = all_par.reset_index(drop=True)
    all_fit = all_fit.reset_index(drop=True)
    return all_par, all_fit

def load_tradeprice_data(filename):
    """
    Loads the data (stored as .npz) used to generate the tradePrice plots
    """
    d = np.load(filename).items()[0][1].item()
    rounds = d['rounds']
    prices = d['tradePrice']
    return rounds, prices