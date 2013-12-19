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
	
	indexes_o = np.concatenate(merged_labels.values())
	labels_o = list()
	
	for group, indexes in merged_labels.items(): labels_o.append(np.repeat(group, len(indexes)))
	labels_o = np.concatenate(labels_o)
	return indexes_o, labels_o


def prettify_table(pandas_tex, label, caption):
	 return "\\begin{table} \centering %s \label{%s} \caption{%s} \end{table}"%(pandas_tex, label, caption)


def load_test_trade_data(type = 'stable'):
	return DataFrame(load_trade_log_data('/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/data/for_testing/' + type + '/'))

def dataframe2latex(dataframe, label, caption):
	dataframe = dataframe.copy()
	tex_column_names = get_latex_par_names([dataframe], as_row = True)
	
	dataframe.columns = tex_column_names.split(',')
	final_table = dataframe.to_latex()
	return final_table

def get_latex_par_names(tables, as_row):
	assert type(tables) == list, 'Please pass a list of pandas dataframes'
	assert type(as_row) == bool, 'Please pass boolean as second argument'
	from pandas import concat
	df = concat(tables)
	if as_row:
		names = ', '.join(['\\%s'%g.replace('_', '') for g in df.columns])
	else:
		names = ''.join(['\\%s\n'%g.replace('_', '') for g in df.columns])
	print names
	return names

