import numpy as np
from settings import default_parameters as defpar, fitness_types
from IO import load_trade_log_data
from pandas import DataFrame

def get_fundamental_after_shock():
	return defpar['fundamental_initial_value'] + defpar['fundamental_shock_size']

def empty_data_matrix(n_rows = 1):
	return np.zeros(shape = n_rows, dtype = fitness_types.items())

def get_labels_r2o(cluster_assignment_o2r, labels_r):
	
	labels_o = list()
	for c in labels_r: labels_o.append(np.where(cluster_assignment_o2r == c)[0])
	labels_o = np.concatenate(labels_o)
	labels_o = np.setdiff1d(range(len(cluster_assignment_o2r)), labels_o)
	return labels_o


def load_test_trade_data(type = 'stable'):
	return DataFrame(load_trade_log_data('/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/data/for_testing/' + type + '/'))


