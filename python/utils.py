import numpy as np
from settings import default_parameters as defpar, fitness_types
from IO import load_trade_log_data
from pandas import DataFrame
from datetime import datetime
import os

latex_dict = {
				'stdev' : r'$f_\sigma$',
                'overshoot' : r'$f_\text{o}$',
                'time_to_reach_new_fundamental' : r'$f_t$',
                'round_stable' : r'$f_\text{t}$',
                'fundamental_initial_value' : 10000,
			    'fundamental_shock_size' : -10,
			    'fundamental_shock_round' : 10000,
			    'ssmm_nAgents' : r'$N_{m}$',
			    'sc_nAgents' : r'$N_{m}$',

			    'ssmm_latency_mu' : r'$\lambda_{m, \mu}$',
			    'ssmm_latency_s' : r'$\lambda_{m, \sigma}$',
			    'ssmm_think_mu' : r'$\tau_{m, \mu}$',
			    'ssmm_think_s' : r'$\tau_{m, \sigma}$',
			    'sc_latency_mu' : r'$\lambda_{c, \mu}$',
			    'sc_latency_s' : r'$\lambda_{c, \sigma}$',
			    'sc_think_mu' : r'$\tau_{c, \mu}$',
			    'sc_think_s' : r'$\tau_{c, \sigma}$',
			    
			    'ssmm_spread_mu' : 4,
			    'ssmm_spread_s' : 2,
			    'ssmm_ordervol_mu' : 100,
			    'ssmm_ordervol_s' : 5,
			    'ssmm_orderlength_mu' : 500,
			    'ssmm_orderlength_s' : 200,
			    
			    'sc_timehorizon_mu' : 5000,
			    'sc_timehorizon_s' : 2000,
			    'sc_ticksBeforeReacting_mu' : 2,
			    'sc_ticksBeforeReacting_s' : 5,
			    'sc_priceTickSize_mu' : 3,
			    'sc_priceTickSize_s' : 2,
			    'sc_ordervol_mu' : 10,
			    'sc_ordervol_s' : 3,
			    'sc_waitTimeBetweenTrading_mu' : 50,
			    'sc_waitTimeBetweenTrading_s' : 20,

			    'ratio_c2m' : r'$\frac{N_{c}}{N_{m}}$',
			    'ratio_m2c' : r'$\frac{N_{m}}{N_{c}}$'
                }

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
	 return "\\begin{table}\n \centering\n %s \label{%s}\n \caption{%s}\n \end{table}"%(pandas_tex, label, caption)


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
	#print names
	return names

def get_latex_par_names_from_list(list_of_names):
	tex_names = list()
	for name in list_of_names:
		tex_names.append('\\%s'%name.replace('_', ''))
	return tex_names

def export_stats_dict_as_tex(dataset, stats, data_name):
	from thesis_plots import table_save_path
	for stat_name, table in stats.items():
		table.index = ['\%s'%g.replace('_', '') for g in table.index.tolist()]
		tex = table.to_latex(float_format=lambda x: str(round(x,1)))
		
		caption = '%s for parameters and fitness values for each cluster in the %s space for dataset %s.'%(stat_name, data_name, dataset)
		tex = prettify_table(tex, 'issue_65_cluster_in_%s_space_%s'%(data_name,stat_name), caption)
		filename = '%s%s_%s_%s.tex'%(table_save_path, dataset, data_name, stat_name)
		print 'Writing table to %s'%filename
		with open(filename, 'w') as f:
			f.write(tex)

def export_tradeprice_figure_as_tex(filenames, label_root):
	import IO
	partials_dir = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/tex/Partials/'
	sub_tex=list()
	for subfig, data_filename in enumerate(filenames):
		rounds, prices, fit, par = IO.load_tradeprice_data_with_parameters(data_filename)
		par_tex_names = get_latex_par_names_from_list(par.keys())
		pc = '%s'%", ".join(map(lambda p, v: '%s=%s'%(p,v), par_tex_names, par.values()))
		fit_tex_names = get_latex_par_names_from_list(fit.keys())
		fc = '%s'%", ".join(map(lambda p, v: '%s=%s'%(p,v), fit_tex_names, map(lambda x: round(x,3), fit.values())))
		caption = "%s, %s"%(pc, fc)
		label = '%s_%s'%(label_root, subfig)
		figure_filename = data_filename.replace('.npz', '.png')
		sub_tex.append("\subcaptionbox{%s\label{%s}}[0.49\linewidth]{\includegraphics[width=0.5\\textwidth]{%s}}"%(caption, label, figure_filename))
	joined_subs = "\n".join(sub_tex)
	full_tex = "\\begin{figure}%s \end{figure}"%joined_subs
	
	with open('%s%s.tex'%(partials_dir, label_root), 'w') as f:
		f.write(full_tex)

def pfn(name):
	### Pretty format name
	return name.replace('_', ' ').capitalize()

def get_epoch_time():
    td = datetime.now() - datetime.utcfromtimestamp(0)
    return str(int(td.total_seconds() * 10**6))

def make_issue_specific_figure_folder(name, dataset):
	from IO import figure_save_path
	name.replace('/', '')
	folder = '%s%s/%s/'%(figure_save_path, name, dataset)
	if not os.path.exists(folder): os.makedirs(folder)
	return folder

def load_d10d11():
	import IO
	from pandas import concat
	fit10, par10, gen10, ids10 = IO.load_pickled_generation_dataframe('d10')
	fit11, par11, gen11, ids11 = IO.load_pickled_generation_dataframe('d11')
	par10['sc_nAgents'] = 150
	par11['ssmm_nAgents'] = 52
	par = concat([par10, par11])
	fit = concat([fit10, fit11])
	return fit, par

def format_as_latex_parameter(parameter):
	return latex_dict[parameter]
