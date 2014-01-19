import utils
from pandas import concat, DataFrame
import IO
from numpy import where
import brewer2mpl
from utils import make_issue_specific_figure_folder

import numpy as np
def issue_83_example_table():
	
	from thesis_plots import table_save_path
	fit, par, gen, ids = IO.load_pickled_generation_dataframe('d3')
	tex_partable = utils.dataframe2latex(par.iloc[range(10),:], 'table:example_dataset_parameters', 'An example data matrix containing the parameters of ten individuals who lived sometime during the execution of the genetic algortihm. In this case, each individual contained paremeters for the number of HFT agents, as well as the latency and thinking time parameters. Hence, the data matrix has a column for each.')
	with open('%sexample_dataset_parameters.tex'%table_save_path, 'w') as f:
			f.write(tex_partable)
	tex_fittable = utils.dataframe2latex(fit.iloc[range(10),:], 'table:example_dataset_fitnesses', 'This table contains the fitness values for each individual in table \\ref{table:example_dataset_parameters}. Note that, in order to increase the reliability of the fitness measure of an individual, the recorded fitness values are the average of the fitnesses obtained by evaluating each individual ten times')		
	with open('%sexample_dataset_fitnesses.tex'%table_save_path, 'w') as f:
			f.write(tex_fittable)

def issue_85_tradeplot_and_tex(folder_with_npz_files, figure_name):
	import os
	from plotting import make_pretty_tradeprice_plot
	npzfiles = os.listdir(folder_with_npz_files)
	npzfiles = [folder_with_npz_files + f for f in npzfiles if f.endswith('.npz')]
	print npzfiles
	for npzfile in npzfiles:
		make_pretty_tradeprice_plot(IO.load_tradeprice_data(npzfile))
	
	utils.export_tradeprice_figure_as_tex(npzfiles, figure_name)

def d9_diagional_points(n_centers = 100):
	from plotting import get_pretty_xy_plot
	def getidx(center, max_dist_to_diagonal = 400, averaging_window_size = 5000): 
		cond1 = (np.abs(fit.time_to_reach_new_fundamental - fit.round_stable) < max_dist_to_diagonal)
		cond2 = (fit.time_to_reach_new_fundamental > center - averaging_window_size)
		cond3 = (fit.time_to_reach_new_fundamental < center + averaging_window_size)
		return cond1 & cond2 & cond3

	folder = make_issue_specific_figure_folder('diagional_points', 'd9')
	fit, par, gen, ids = IO.load_pickled_generation_dataframe('d9')
	centers_to_calculate = np.linspace(10000, 90000, n_centers)
	list_of_dataframes = map(lambda i: par[getidx(i, 1000, 5000)], centers_to_calculate)
	mean_frame = concat(map(lambda x: getattr(x, 'mean')(), list_of_dataframes), axis=1).transpose()
	std_frame = concat(map(lambda x: getattr(x, 'std')(), list_of_dataframes), axis=1).transpose()
	for parameter in mean_frame.columns:
		filename = folder + parameter + '.png'
		#print mean_frame[parameter]
		y1 = mean_frame[parameter] - std_frame[parameter]
		y2 = mean_frame[parameter] + std_frame[parameter]
		ax, fig = get_pretty_xy_plot(x=centers_to_calculate, y=mean_frame[parameter], xlabel='Time to reach new fundamental', ylabel=parameter, filename = filename, save_figure = False)
		ax.fill_between(centers_to_calculate, y2.values, y1.values, color = 'gray', alpha = 0.5)
		fig.savefig(filename)
	return centers_to_calculate, mean_frame, std_frame



def apply_filters(fit, fnums = [1,2,3,4,5,6,7,8,9]):
	
	def filter1(round_stable_threshold = 15000, time_to_reach_new_fundamental_threshold = 15000):
		cond1 = fit.time_to_reach_new_fundamental < time_to_reach_new_fundamental_threshold
		cond2 = fit.round_stable < round_stable_threshold
		cond3 = np.log(fit.stdev) > 0
		return cond1 & cond2 & cond3
	
	def filter2(max_dist_to_diagonal = 400): 
		cond1 = (np.abs(fit.time_to_reach_new_fundamental - fit.round_stable) < max_dist_to_diagonal)
		cond2 = fit.time_to_reach_new_fundamental > 30000
		return cond1 & cond2

	def filter3(max_dist_to_diagonal = 400): 
		cond1 = (np.abs(fit.time_to_reach_new_fundamental - fit.round_stable) < max_dist_to_diagonal)
		cond2 = fit.time_to_reach_new_fundamental < 30000
		return cond1 & cond2


	def filter4():
		cond1 = fit.round_stable < fit.time_to_reach_new_fundamental
		cond2 = fit.overshoot == 0
		return cond1 & cond2

	def filter5():
		cond1 = fit.round_stable < fit.time_to_reach_new_fundamental
		cond2 = fit.overshoot == 1
		return cond1 & cond2

	def filter6():
		cond1 = fit.round_stable > fit.time_to_reach_new_fundamental
		cond2 = fit.overshoot > 1
		return cond1 & cond2

	def filter7():
		cond1 = fit.time_to_reach_new_fundamental > 10000
		cond2 = fit.time_to_reach_new_fundamental < 25000
		cond3 = fit.round_stable > 20000
		cond4 = fit.round_stable < 40000
		return cond1 & cond2 & cond3 & cond4

	def filter8():
		cond1 = fit.time_to_reach_new_fundamental > 10000
		cond2 = fit.time_to_reach_new_fundamental < 25000
		cond3 = fit.round_stable > 40000
		cond4 = fit.round_stable < 75000
		return cond1 & cond2 & cond3 & cond4

	def filter9(stdev_sensitivity_threshold = 0.2):
		cond1 = np.log(fit.stdev) > -0.5 - stdev_sensitivity_threshold
		cond2 = np.log(fit.stdev) < -0.5 + stdev_sensitivity_threshold
		cond3 = fit.time_to_reach_new_fundamental > 25000
		cond4 = fit.round_stable > 80000
		return cond1 & cond2 & cond3 & cond4


	filters = [eval('filter%s'%i) for i in fnums]
	masks = [f() for f in filters]
	return masks, filters
	

def calculate_jaccard(masks):
		from itertools import combinations
		as_sets = map(lambda x: set(x[0]), map(np.where, masks))
 		jc = np.zeros((len(as_sets), len(as_sets)))
 		jc[jc==0] = None
 		for i,j in combinations(range(len(as_sets)), 2):
 			
 			intersection = len(as_sets[i].intersection(as_sets[j]))
 			size = min(len(as_sets[i]), len(as_sets[j]))
 			#union = len(as_sets[i].union(as_sets[j]))
 			try:
 				jaccard = float(intersection) / float(size)
 			except ZeroDivisionError:
 				jaccard = np.nan
 			jc[j][i] = jaccard
 			#overlaps[j][i] = np.nan
		return jc
	

	
def filter_and_print_tables(dataset):
	from plotting import plot_group_overlap
	from utils import make_issue_specific_figure_folder

	def make_dataframe(l):
		ldf = concat(l, axis=1)
		ldf.columns = columns
		tdf = ldf.transpose()
		tdf.columns = utils.get_latex_par_names([tdf], as_row = True).split(',')
		tdf = tdf.transpose()
		return tdf
	
	fnums = [1,2,3,4,5,6,7,8,9]
	#fit, par, gen, ids = IO.load_pickled_generation_dataframe(dataset)
	fit, par = utils.load_d10d11()
	masks, filters = apply_filters(fit, fnums = fnums)
	jaccard = calculate_jaccard(masks)
	
	columns = map(lambda x: 'F%s'%x, fnums)
	parmeans = make_dataframe(map(lambda f: par[f()].mean(), filters))
	fitmeans = make_dataframe(map(lambda f: fit[f()].mean(), filters))
	counts = make_dataframe(map(lambda f: par[f()].count(), filters))
	
	full_table = concat([parmeans, fitmeans])
	
	counts = counts.iloc[0]
	counts.name = 'Count'
	full_table = full_table.append(counts)
	tex = full_table.to_latex(float_format=lambda x: str(round(x,1)))
	full_tex = utils.prettify_table(tex, 'table:manual_filtering_%s'%dataset, 'XXX').replace('nan','N/A')
	print full_tex
	
	folder = make_issue_specific_figure_folder('manual_filtering', dataset)
	plot_group_overlap(folder + 'group_overlap', jaccard)
	return jaccard
	#stds = concat(map(lambda f: par[f()].std(), filters), axis = 1)
	
	#return means, stds, counts
	#means = map(lambda f: par[eval('filter%s'%f)()].mean(), [1,2,3,4,5])
	#print means
	#print par[filter1()].std()

def faster_mm_many_chartists():
	from plotting import multiline_xy_plot
	folder = make_issue_specific_figure_folder('par_tendencies', 'all')
	
	def mkplot(filename, line_parameter, intervals_for_lines, range_parameter, fitness_type, legend_caption, xlabel, ylabel):
		ylines = list()
		labels = list()
		x = list(set(p[range_parameter]))

		for lb, ub in zip(intervals_for_lines[:-1], intervals_for_lines[1::]): 
			line = map(lambda l: f[(p[range_parameter] == l) & (lb <= p[line_parameter]) & (p[line_parameter] < lb + ub)][fitness_type].mean(), set(p[range_parameter]))
			ylines.append(line)
			labels.append('%s < %s < %s'%(lb,legend_caption, ub))
		line = map(lambda l: f[(p[range_parameter] == l) & (intervals_for_lines[-1] <= p[line_parameter])][fitness_type].mean(), set(p[range_parameter]))
		ylines.append(line)
		labels.append('%s < %s '%(intervals_for_lines[-1], legend_caption))
		multiline_xy_plot(x, ylines, ylabel = ylabel, xlabel=xlabel, filename = filename, y_errorbars = None, save_figure = True, legend_labels = labels)


	f,p,g,i = IO.load_pickled_generation_dataframe('d11')
	filename = folder + 'd11_overshoot_mm_latency.png'
	mkplot(filename = filename, line_parameter='sc_nAgents', intervals_for_lines = [0, 50, 100], range_parameter='ssmm_latency_mu', fitness_type='overshoot', legend_caption = '# chartists', xlabel = 'Average market maker latency', ylabel = 'Average model overshoot')
	
	filename = folder + 'd11_overshoot_chartist_latency.png'
	mkplot(filename = filename, line_parameter='sc_nAgents', intervals_for_lines = [0, 50, 100], range_parameter='sc_latency_mu', fitness_type='overshoot', legend_caption = '# chartists', xlabel = 'Average chartist latency', ylabel = 'Average model overshoot')

	f,p,g,i = IO.load_pickled_generation_dataframe('d10')
	filename = folder + 'd10_overshoot_mm_latency.png'
	mkplot(filename = filename, line_parameter='ssmm_nAgents', intervals_for_lines = [0, 50, 100], range_parameter='ssmm_latency_mu', fitness_type='overshoot', legend_caption = '# market makers', xlabel = 'Average market maker latency', ylabel = 'Average model overshoot')
	
	filename = folder + 'd10_overshoot_chartist_latency.png'
	mkplot(filename = filename, line_parameter='ssmm_nAgents', intervals_for_lines = [0, 50, 100], range_parameter='sc_latency_mu', fitness_type='overshoot', legend_caption = '# market makers', xlabel = 'Average chartist latency', ylabel = 'Average model overshoot')

	filename = folder + 'd10_overshoot_ssmm_nAgents.png'
	mkplot(filename = filename, line_parameter='ssmm_latency_mu', intervals_for_lines = [0, 20, 40, 60], range_parameter='ssmm_nAgents', fitness_type='overshoot', legend_caption = 'ssmm latency', xlabel = 'Average # market makers', ylabel = 'Average model overshoot')


	par, fit, ids, invalid_inds = IO.load_all_generations_as_DataFrame('/Users/halfdan/raw_data/d10/generations/')
	par = invalid_inds[par.columns]
	fit = invalid_inds[fit.columns]
	filename = folder + 'd10_overshoot_ssmm_nAgents_invalid.png'
	mkplot(filename = filename, line_parameter='ssmm_latency_mu', intervals_for_lines = [0, 20, 40, 60], range_parameter='ssmm_nAgents', fitness_type='overshoot', legend_caption = 'ssmm latency', xlabel = 'Average # market makers', ylabel = 'Average model overshoot')


def collect_filter_individuals_and_replot(action, n_graphs_to_copy = 10):
	import os, shutil
	from IO import figure_save_path
	from plotting import make_pretty_tradeprice_plot
	
	fit, par, gen, ids = IO.load_pickled_generation_dataframe('d11')
	masks,filters = apply_filters(fit)
	# Give proper column name
	ids.columns = ['id', 'gen']
	raw_data_path = '/Users/halfdan/raw_data/d11/graphs/'
	graph_save_dir = figure_save_path + 'filter_graphs/'

	def replot(mask, subfolder_name, f = 0):
		try:
			in_mask = ids[mask]
			has_tuple = in_mask['id'].map(lambda x: isinstance(x, tuple))
			in_mask = in_mask[has_tuple]
		
			indexes = range(len(in_mask))
			np.random.shuffle(indexes)
			
			if n_graphs_to_copy == 'all':
				n_copy = len(indexes) - 1
			else:
				n_copy = n_graphs_to_copy
			names = map(lambda x: x[0], in_mask.iloc[indexes[0:n_copy]]['id'].values)
			#print 'names'
			#print names
			paths = map(lambda x: '%s%s'%(raw_data_path, x), names)
			graph_paths = map(lambda x: '%s.png'%x, paths)
			data_paths = map(lambda x: '%s.npz'%x, paths)
			#all_data = map(lambda x: np.load(x).items()[0][1].item(), data_paths)
			directory = '%s%s%s/'%(graph_save_dir, subfolder_name, f)
			parameters = DataFrame(columns = par.columns)
			if not os.path.exists(directory): os.makedirs(directory)
			for path, data_path, name in zip(paths, data_paths, names):
				data = np.load(data_path).items()[0][1].item()
				rounds = data['rounds']
				prices = data['tradePrice']
				filename = directory + name + '.png'
				parameters = parameters.append(data['parameters'], ignore_index=True)
				print 'Replotting market with pars: %s'%data['parameters']
				make_pretty_tradeprice_plot(rounds, prices, filename)
			return parameters
			
			for path, graph, name in zip(paths, graph_paths, names):
				print 'copy %s to %s'%(graph, directory + name + '.png')
				shutil.copyfile(graph, directory + name + '.png')
		except IndexError:
			pass

	graph_cond = ids.id != ()
	if action == 'filter':
		for f, m in enumerate(masks):	
			filter_mask = m & graph_cond
			replot(filter_mask, action, f)
			graph_save_dir += 'filter/'
	elif action == 'large_overshoot':
		m = fit.overshoot > 25
		m = m & graph_cond
		graph_save_dir += 'large_overshoot/'
		return replot(m, action)
	elif action == 'no_response':
		m = fit.overshoot == 10**6
		m = m & graph_cond
		graph_save_dir += 'large_overshoot/'
		return replot(m, action)
	else:
		print 'Doing nothing'


def faster_mm_makes_worse_markets(dataset):
	from plotting import multiline_xy_plot
	from utils import make_issue_specific_figure_folder
	def get_mmlat_mask(l, u): 
		return (p.ssmm_latency_mu > l) & (p.ssmm_latency_mu < u)

	def get_ssmmlatencyrange_mean(agent_mask, ssmmlatencyrange = range(1,100), nsc_lower = 0):
		return concat(map(lambda l: f[get_mmlat_mask(l,l+20) & agent_mask].mean(), ssmmlatencyrange), axis=1).transpose()

	def get_sclat_mask(l, u): 
		return (p.sc_latency_mu > l) & (p.sc_latency_mu < u)

	def get_sclatencyrange_mean(agent_mask, sclatencyrange = range(1,100), nsc_lower = 0):
		return concat(map(lambda l: f[get_sclat_mask(l,l+20) & agent_mask].mean(), sclatencyrange), axis=1).transpose()

	def get_nchartist_mask(lower, upper):
		return (p.sc_nAgents >= lower) & (p.sc_nAgents < upper)

	def get_nmm_mask(lower, upper):
		return (p.ssmm_nAgents >= lower) & (p.ssmm_nAgents < upper)
	
	def zip_to_tuples(r): return zip(r[:-1], r[1::])

	ssmmlatencyrange = range(80)
	sclatencyrange = range(100)

	

	if dataset == 'd10d11':
		f, p = utils.load_d10d11()
	else:
		f,p,g, i=IO.load_pickled_generation_dataframe(dataset_name=dataset)

	folder = make_issue_specific_figure_folder('faster_mm_makes_worse_markets', dataset)
	try:
		for fitness in f.columns:
			filename = folder + fitness + '_SC_mmlatency.png'
			xlabel = 'Market maker latency'
			ylabel = fitness
			legend_labels = list()
			ys = list()
			for nsc_lower, nsc_upper in zip_to_tuples(np.linspace(0,500,6)):
				nchartist_mask = get_nchartist_mask(nsc_lower, nsc_upper)	
				means = get_ssmmlatencyrange_mean(nchartist_mask, ssmmlatencyrange, nsc_lower = nsc_lower)
				ys.append(means[fitness])
				legend_labels.append('%s <= # SC < %s'%(nsc_lower, nsc_upper))
			multiline_xy_plot(means.index, ys, xlabel, ylabel, legend_labels, filename, y_errorbars=None, save_figure = True)

			filename = folder + fitness + '_SC_sclatency.png'
			xlabel = 'Chartist latency'
			legend_labels = list()
			ys = list()
			for nsc_lower, nsc_upper in zip_to_tuples(np.linspace(0,500,6)):
				nchartist_mask = get_nchartist_mask(nsc_lower, nsc_upper)	
				means = get_sclatencyrange_mean(nchartist_mask, sclatencyrange, nsc_lower = nsc_lower)
				ys.append(means[fitness])
				legend_labels.append('%s <= # SC < %s'%(nsc_lower, nsc_upper))
			multiline_xy_plot(means.index, ys, xlabel, ylabel, legend_labels, filename, y_errorbars=None, save_figure = True)
	except AttributeError:
		pass

	try:	
		for fitness in f.columns:
			filename = folder + fitness + '_MM_mmlatency.png'
			xlabel = 'Market maker latency'
			ylabel = fitness
			legend_labels = list()
			ys = list()
			for nmm_lower, nmm_upper in zip_to_tuples(range(0,150,25)):
				n_mm_mask = get_nmm_mask(nmm_lower, nmm_upper)	
				means = get_ssmmlatencyrange_mean(n_mm_mask, ssmmlatencyrange, nsc_lower = nsc_lower)
				ys.append(means[fitness])
				legend_labels.append('%s <= # MM < %s'%(nmm_lower, nmm_upper))
			multiline_xy_plot(means.index, ys, xlabel, ylabel, legend_labels, filename, y_errorbars=None, save_figure = True)

			filename = folder + fitness + '_MM_sclatency.png'
			xlabel = 'Chartist latency'
			ylabel = fitness
			legend_labels = list()
			ys = list()
			for nmm_lower, nmm_upper in zip_to_tuples(range(0,150,25)):
				n_mm_mask = get_nmm_mask(nmm_lower, nmm_upper)	
				means = get_sclatencyrange_mean(n_mm_mask, sclatencyrange, nsc_lower = nsc_lower)
				ys.append(means[fitness])
				legend_labels.append('%s <= # MM < %s'%(nmm_lower, nmm_upper))
			multiline_xy_plot(means.index, ys, xlabel, ylabel, legend_labels, filename, y_errorbars=None, save_figure = True)		
	except AttributeError:
		pass



def latency_vs_fitness_with_lines_for_agent_ratio(dataset):
	from plotting import multiline_xy_plot
	from utils import make_issue_specific_figure_folder
	def get_ssmmlat_mask(l, u): 
		return (p.ssmm_latency_mu > l) & (p.ssmm_latency_mu < u)

	def get_sclat_mask(l, u): 
		return (p.sc_latency_mu > l) & (p.sc_latency_mu < u)

	def zip_to_tuples(r): return zip(r[:-1], r[1::])


	def calc_and_plot(ratio_direction):	
		for fitness in f.columns:
			ssmm_ys = list()
			sc_ys = list()
			legend_labels = list()
			for ratio_lower, ratio_upper in zip_to_tuples(ratio_range):
				ratio_mask = (ratio_lower < p.ratio) & (p.ratio < ratio_upper)
				ssmm_lat_range = concat(map(lambda l: f[get_ssmmlat_mask(l,l+20) & ratio_mask].mean(), ssmmlatencyrange), axis=1).transpose()
				ssmm_ys.append(ssmm_lat_range[fitness])
				sc_lat_range = concat(map(lambda l: f[get_sclat_mask(l,l+20) & ratio_mask].mean(), sclatencyrange), axis=1).transpose()
				sc_ys.append(sc_lat_range[fitness])
				legend_labels.append('%s <= %s < %s'%(round(ratio_lower,1), ratio_direction, round(ratio_upper,1)))
			filename = '%s_%s_%s_mmlatency.png'%(folder, ratio_direction, fitness)
			multiline_xy_plot(ssmm_lat_range.index, ssmm_ys, xlabel = 'Market maker latency', ylabel = fitness, legend_labels = legend_labels, filename = filename)
			filename = '%s_%s_%s_sclatency.png'%(folder, ratio_direction, fitness)
			multiline_xy_plot(sc_lat_range.index, sc_ys, xlabel = 'Chartist latency', ylabel = fitness, legend_labels = legend_labels, filename = filename)


	ssmmlatencyrange = range(100)
	sclatencyrange = range(100)
	
	folder = make_issue_specific_figure_folder('latency_vs_fitness_with_lines_for_agent_ratio', dataset)
	
	if dataset == 'd10d11':
		f, p = utils.load_d10d11()
	else:
		f,p,g, i=IO.load_pickled_generation_dataframe(dataset_name=dataset)
		if 'dataset' == 'd10':
			p['sc_nAgents'] = 150
		elif 'dataset' == 'd11':
			p['ssmm_nAgents'] = 52
	
	#nssmm_mask = p.ssmm_nAgents > 50
	#f = f[nssmm_mask]
	#p = p[nssmm_mask]

	p['ratio'] = p['sc_nAgents'].astype(float) / p['ssmm_nAgents']
	ratio_range = np.linspace(0,3,6)
	calc_and_plot('c2m')

	p['ratio'] = p['ssmm_nAgents'].astype(float) / p['sc_nAgents']
	ratio_range = [0,0.01, 0.2,0.35,0.6,1]
	calc_and_plot('m2c')
	
			#concat(map(lambda l: f[get_mmlat_mask(l,l+20) & agent_mask].mean(), ssmmlatencyrange), axis=1).transpose()
			#means = means.append(f[mask].mean()[fitness], ignore_index=True)
	

def agent_ratio_to_latency_ratio(n_ar_bins = 10, n_lr_bins = 10, dataset = 'd10d11'):
	from plotting import plot_image_matrix
	from utils import make_issue_specific_figure_folder
	folder = make_issue_specific_figure_folder('latency_vs_agent_ratios', dataset)
	
	fit, par = utils.load_d10d11()

	#fit, par, gen, ids = IO.load_pickled_generation_dataframe('d10')
	agent_ratio = par.sc_nAgents.astype(float) / par.ssmm_nAgents
	latency_ratio = par['ssmm_latency_mu'].astype(float) / par['sc_latency_mu']

	inf_mask = agent_ratio != np.inf

	agent_ratio_mask = (agent_ratio < 5)
	latency_ratio_mask = (latency_ratio > 0) & (latency_ratio < 5)
	
	#nsc_mask = par.sc_nAgents > 0
	#nssmm_mask = (20 < par.ssmm_nAgents) & (par.ssmm_nAgents < 50)

	#total_mask = inf_mask & agent_ratio_mask & latency_ratio_mask & nsc_mask & nssmm_mask
	total_mask = inf_mask & agent_ratio_mask & latency_ratio_mask
	par = par[total_mask]
	fit = fit[total_mask]
	agent_ratio = agent_ratio[total_mask]
	latency_ratio = latency_ratio[total_mask]

	ar_hn, ar_histbins = np.histogram(agent_ratio, bins = n_ar_bins)
	lr_hn, lr_histbins = np.histogram(latency_ratio, bins = n_lr_bins)
	print ar_histbins
	overshoot_mean = np.zeros((n_ar_bins, n_lr_bins))
	roundstable_mean = np.zeros((n_ar_bins, n_lr_bins))
	stdev_mean = np.zeros((n_ar_bins, n_lr_bins))
	timeto_mean = np.zeros((n_ar_bins, n_lr_bins))
	test = np.zeros((n_ar_bins, n_lr_bins))
	for i, (ar_lower, ar_upper) in enumerate(zip(ar_histbins[:-1], ar_histbins[1::])):
		for j, (lr_lower, lr_upper) in enumerate(zip(lr_histbins[:-1], lr_histbins[1::])):
			index_mask = (ar_lower < agent_ratio) & (agent_ratio < ar_upper) & (lr_lower < latency_ratio) & (latency_ratio < lr_upper)
			means = fit[index_mask].mean()
			overshoot_mean[i][j] = means['overshoot']
			roundstable_mean[i][j] = means['round_stable']
			stdev_mean[i][j] = means['stdev']
			timeto_mean[i][j] = means['time_to_reach_new_fundamental']
			test[i][j] = j
	x_ticklabels = map(lambda x: round(x,1), lr_histbins)
	y_ticklabels = map(lambda x: round(x,1), ar_histbins)
	plot_image_matrix(folder + 'overshoot.png', overshoot_mean, x_ticklabels, y_ticklabels)
	plot_image_matrix(folder + 'stdev.png', stdev_mean, x_ticklabels, y_ticklabels)
	plot_image_matrix(folder + 'time_to_reach_new_fundamental.png', timeto_mean, x_ticklabels, y_ticklabels)
	plot_image_matrix(folder + 'round_stable.png', roundstable_mean, x_ticklabels, y_ticklabels)
	x_ticklabels = range(10)
	y_ticklabels = range(10)
	plot_image_matrix(folder + 'test.png', test, x_ticklabels, y_ticklabels)
	

	return overshoot_mean, stdev_mean, roundstable_mean, timeto_mean

if __name__ == '__main__':
	faster_mm_makes_worse_markets()
	filter_and_print_tables('d10')
	filter_and_print_tables('d11')
	agent_ratio_to_latency_ratio(dataset = 'd10', n_ar_bins = 10, n_lr_bins = 10)
	agent_ratio_to_latency_ratio(dataset = 'd11', n_ar_bins = 10, n_lr_bins = 10)
	agent_ratio_to_latency_ratio(dataset = 'd10d11', n_ar_bins = 10, n_lr_bins = 10)
	collect_filter_individuals_and_replot('filter')
	collect_filter_individuals_and_replot('large_overshoot')


