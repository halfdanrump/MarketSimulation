import settings
from collections import Iterable
from shutil import rmtree
import numpy as np
from settings import fitness_types, fitness_weights, parameters_in_genes
from pandas import DataFrame, read_pickle
import re
import os
#from shutils import rmtree

dataset_paths = {
    'd1':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/merged_data/d1_sameLatDist_ssmm40_sc100/',
    'd2':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/merged_data/d2/',
    'd3':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/merged_data/d3/',
    'd9':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/merged_data/d9_fixed_nAgents_vary_latpars/'
}

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
    all_ids = list()
    for gene in generation_data:
        all_genes.append(gene['ind'])
        all_fitnesses.append(gene['fit'])
        if gene.has_key('data_ids'):
            all_ids.append(gene['data_ids'])

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
    if all_ids: np.savez_compressed(path + 'gen_%s_data_ids'%generation_number, all_ids)


def load_all_generations_as_DataFrame(folder_name, drop_invalid_individuals=True, with_data_ids = True):
    #gen_par = dict()
    from pandas import concat
    all_par = DataFrame()
    all_fit = DataFrame()
    gen_pars_files = [f for f in os.listdir(folder_name) if re.match('gen_\d+_pars\.npz', f)]
    gen_fit_files = [f for f in os.listdir(folder_name) if re.match('gen_\d+_fit\.npz', f)]
    print "Loading data for %s generations..."%len(gen_pars_files)
    for p,f in zip(gen_pars_files, gen_fit_files):
        gen_num = p.split('_')[1]
        print gen_num
        dp = np.load(folder_name + p)
        df = np.load(folder_name + f)
        pars = DataFrame(dp.items()[0][1])
        fit = DataFrame(df.items()[0][1])
        gen_num = np.repeat(gen_num, len(fit))
        fit['gen'] = gen_num
        pars['gen'] = gen_num
        all_par = all_par.append(DataFrame(pars))
        all_fit = all_fit.append(DataFrame(fit))
        dp.close()
        df.close()
    
    ### Remove invalid genes
    all_par = all_par.reset_index(drop=True)
    all_fit = all_fit.reset_index(drop=True)
    if drop_invalid_individuals:
        i, = np.where(all_fit['overshoot'] >= 10**6)
        invalid_individuals = concat([all_par.iloc[i,:], all_fit.iloc[i,:]], axis=1)
        print "Number of discarded individuals: %s"%i
        all_par = all_par.drop(i)
        all_fit = all_fit.drop(i)
        all_par = all_par.reset_index(drop=True)
        all_fit = all_fit.reset_index(drop=True)
        return all_par, all_fit, all_data_ids, invalid_individuals
    else:
        return all_par, all_fit, all_data_ids, DataFrame()

def save_tradeprice_data(rounds, tradePrice, fitness, all_parameters, filename):
    pars_in_gene = dict([(k, all_parameters[k]) for k in settings.parameters_in_genes])
    fit = dict(zip(list(fitness.dtype.names), list(fitness.item())))
    if settings.SAVE_DATA_USED_FOR_PLOTTING:     
        data = {'rounds':rounds, 'tradePrice':tradePrice, 'fitness': fit, 'parameters':pars_in_gene}
        np.savez_compressed(filename, data)


def load_tradeprice_data(filename):
    """
    Loads the data (stored as .npz) used to generate the tradePrice plots
    """
    d = np.load(filename).items()[0][1].item()
    rounds = d['rounds']
    prices = d['tradePrice']
    return rounds, prices

def load_tradeprice_data_with_parameters(filename):
    """
    Loads the data (stored as .npz) used to generate the tradePrice plots
    """
    d = np.load(filename).items()[0][1].item()
    rounds = d['rounds']
    prices = d['tradePrice']
    fit = d['fitness']
    par = d['parameters']
    return rounds, prices, fit, par

def pickle_generation_data(dataset_name, with_data_ids = True):
    dataset_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/'
    par, fit, ids, invalids = load_all_generations_as_DataFrame(dataset_path + 'raw_data/%s/generations/'%dataset_name, with_data_ids)
    par.to_pickle(dataset_paths[dataset_name] + 'pars.pandas')
    fit.to_pickle(dataset_paths[dataset_name] + 'fits.pandas')
    if with_data_ids: ids.to_pickle(dataset_paths[dataset_name] + 'ids.pandas')


def load_pickled_generation_dataframe(dataset_name):
    datapath = dataset_paths[dataset_name]
    #par = pandas.read_pickle(datapath + 'pars.pandas')
    fit_data = read_pickle(datapath + 'fits.pandas')
    par_data = read_pickle(datapath + 'pars.pandas')
    gen = fit_data['gen']
    gen = gen.astype(int)
    fit_data = fit_data.drop('gen', 1)
    par_data = par_data.drop('gen', 1)
    return fit_data, par_data, gen