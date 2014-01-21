import settings
from collections import Iterable
from shutil import rmtree
import numpy as np
from settings import fitness_types, fitness_weights, parameters_in_genes
from pandas import DataFrame, read_pickle
import re
import os
import shutil
#from shutils import rmtree

dataset_paths = {
    'd1':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/merged_data/d1_sameLatDist_ssmm40_sc100/',
    'd2':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/merged_data/d2/',
    'd3':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/merged_data/d3/',
    'd9':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/merged_data/d9_fixed_nAgents_vary_latpars/',
    'd10':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/merged_data/d10_vary_nssmm_vary_latpars/',
    'd11':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/merged_data/d11_vary_nsc_vary_latpars/',
    'd10d11' : '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/merged_data/d10d11/'
}

figure_save_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/tex/Figures/'
table_save_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/tex/Tables/'
#raw_data_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/datasets/raw_data/'
raw_data_path = '/Users/halfdan/raw_data/'
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
    if all_ids: np.savez_compressed(path + 'gen_%s_dataids'%generation_number, all_ids)


def load_all_generations_as_DataFrame(folder_name, drop_invalid_individuals=True):
    #gen_par = dict()
    from pandas import concat
    def load_files(files):
        all_data = DataFrame()
        for f in files:
            datafile = np.load(folder_name + f)
            df = DataFrame(datafile.items()[0][1])
            datafile.close()
            gen_num = np.repeat(f.split('_')[1], len(df))
            df['gen'] = gen_num
            all_data = all_data.append(df)
        all_data = all_data.reset_index(drop=True)   
        return all_data

    gen_pars_files = [f for f in os.listdir(folder_name) if re.match('gen_\d+_pars\.npz', f)]
    gen_fit_files = [f for f in os.listdir(folder_name) if re.match('gen_\d+_fit\.npz', f)]
    gen_id_files = [f for f in os.listdir(folder_name) if re.match('gen_\d+_dataids\.npz', f)]
    print "Loading data from %s generations..."%len(gen_pars_files)
    all_par = load_files(gen_pars_files)
    all_fit = load_files(gen_fit_files)
    all_ids = load_files(gen_id_files)    
    ### Remove invalid genes
    
    if drop_invalid_individuals:
        i, = np.where(all_fit['overshoot'] >= 10**6)
        invalid_individuals = all_par.iloc[i,:]
        print "Number of discarded individuals: %s"%len(i)
        all_par = all_par.drop(i)
        all_par = all_par.reset_index(drop=True)

        all_fit = all_fit.drop(i)
        all_fit = all_fit.reset_index(drop=True)

        try:
            all_ids = all_ids.drop(i)
            all_ids = all_ids.reset_index(drop=True)
            #invalid_individuals = concat([invalid_individuals, all_ids.iloc[i,:]])
        except Exception:
            pass
        
        return all_par, all_fit, all_ids, invalid_individuals
    else:
        return all_par, all_fit, all_ids, DataFrame()

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

def pickle_generation_data(dataset_name):
    par, fit, ids, invalids = load_all_generations_as_DataFrame(raw_data_path + '%s/generations/'%dataset_name)
    try:
        os.mkdir(dataset_paths[dataset_name])
    except OSError:
        pass
    par.to_pickle(dataset_paths[dataset_name] + 'pars.pandas')
    fit.to_pickle(dataset_paths[dataset_name] + 'fits.pandas')
    ids.to_pickle(dataset_paths[dataset_name] + 'ids.pandas')
    invalids.to_pickle(dataset_paths[dataset_name] + 'invalids.pandas')

    shutil.copy(raw_data_path + '%s/settings.py'%dataset_name, '%s/settings.py'%dataset_paths[dataset_name])

    

def load_pickled_generation_dataframe(dataset_name, return_invalids = False):
    datapath = dataset_paths[dataset_name]
    #par = pandas.read_pickle(datapath + 'pars.pandas')
    fit_data = read_pickle(datapath + 'fits.pandas')
    par_data = read_pickle(datapath + 'pars.pandas')
    gen = fit_data['gen']
    gen = gen.astype(int)
    fit_data = fit_data.drop('gen', 1)
    par_data = par_data.drop('gen', 1)
    fit_data['overshoot'] -= 2
    fit_data['time_to_reach_new_fundamental'] -= 10000
    fit_data['round_stable'] -= 10000
    try:
        ids = read_pickle(datapath + 'ids.pandas')
    except IOError:
        ids = None
        print "No file containing simulation data identifiers was found."

    try:
        invalids = read_pickle(datapath + 'invalids.pandas')
    except IOError:
        invalids = None
        print "No file containing invalid simulations were found"

    ### BAD BAD BAD!!
    if return_invalids:
        return invalids
    else:
        return fit_data, par_data, gen, ids

def merge_and_store_dataset(new_dataset_name, datasets, fill_values):
    assert isinstance(fill_values, list)
    for filldict in fill_values: assert isinstance(filldict, dict)
    assert isinstance(new_dataset_name, str)
    assert isinstance(datasets, list)
    from pandas import concat
    all_data = map(lambda dataset: load_pickled_generation_dataframe(dataset), datasets)
    for i, filldict in enumerate(fill_values):
        for parname, fillvalue in filldict.items():
            all_data[i][1][parname] = fillvalue
        print i, filldict
        print all_data[i][1]
    fit ,par ,gen ,ids = map(lambda x: concat([all_data[i][x] for i in range(len(all_data))], axis=0), range(4))
    fit['gen'] = gen
    par['gen'] = gen
    try:
        try:
            os.makedirs(dataset_paths[new_dataset_name])        
        except OSError:
            pass
        par.to_pickle(dataset_paths[new_dataset_name] + 'pars.pandas')
        fit.to_pickle(dataset_paths[new_dataset_name] + 'fits.pandas')
        ids.to_pickle(dataset_paths[new_dataset_name] + 'ids.pandas')
    except KeyError:
        print 'Aborting: Please insert the new data set name in dataset_paths'


