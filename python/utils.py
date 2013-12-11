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
	return labels_o


def get_group_vector_for_reduced_dataset(clusters, cluster_assignment_o2r, cluster_assignment_r2g):
	
	merged_labels = dict()
	for k in range(max(cluster_assignment_r2g) + 1): merged_labels[k] = list()
	
	labels_full = DataFrame(cluster_assignment_o2r, columns=['l'])
	group_indices = labels_full.groupby('l').indices
	for idx, cluster in enumerate(clusters):
		member_points = group_indices[cluster]
		merged_labels[cluster_assignment_r2g[idx]].append(np.ravel(member_points))
	for k, v in merged_labels.iteritems(): merged_labels[k] = np.concatenate(v)
	
	labels_o = np.zeros(len(cluster_assignment_o2r))
	indexes_o = np.concatenate(merged_labels.values())
	for group, indexes in merged_labels.items(): 
		labels_o[indexes] = group
	return labels_o, indexes_o



def load_test_trade_data(type = 'stable'):
	return DataFrame(load_trade_log_data('/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/data/for_testing/' + type + '/'))


