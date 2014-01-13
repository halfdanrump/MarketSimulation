import utils
from pandas import concat
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
		print mean_frame[parameter]
		y1 = mean_frame[parameter] - std_frame[parameter]
		y2 = mean_frame[parameter] + std_frame[parameter]
		ax, fig = get_pretty_xy_plot(x=centers_to_calculate, y=mean_frame[parameter], xlabel='Time to reach new fundamental', ylabel=parameter, filename = filename, save_figure = False)
		ax.fill_between(centers_to_calculate, y2.values, y1.values, color = 'gray', alpha = 0.5)
		fig.savefig(filename)
	return centers_to_calculate, mean_frame, std_frame



def apply_filters(dataset, return_masks = False):
	
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

	def filter9(stdev_sensitivity_threshold = 0.1):
		cond1 = fit.stdev > np.exp(-0.5) - stdev_sensitivity_threshold
		cond2 = fit.stdev < np.exp(-0.5) + stdev_sensitivity_threshold
		cond3 = fit.time_to_reach_new_fundamental > 25000
		return cond1 & cond2 & cond3

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
	
	def make_dataframe(l):
		ldf = concat(l, axis=1)
		ldf.columns = columns
		tdf = ldf.transpose()
		tdf.columns = utils.get_latex_par_names([tdf], as_row = True).split(',')
		tdf = tdf.transpose()
		return tdf

	fit, par, gen, ids = IO.load_pickled_generation_dataframe(dataset)

	fnums = [1,2,3,4,5,6,7,8,9]
	columns = map(lambda x: 'F%s'%x, fnums)
	filters = [eval('filter%s'%i) for i in fnums]
	masks = [f() for f in filters]
	if return_masks: return masks
	jaccard = calculate_jaccard(masks)
	parmeans = make_dataframe(map(lambda f: par[f()].mean(), filters))
	fitmeans = make_dataframe(map(lambda f: fit[f()].mean(), filters))
	counts = make_dataframe(map(lambda f: par[f()].count(), filters))
	return fitmeans, parmeans, counts, jaccard

	
def filter_and_print_tables(dataset):
	fitmeans, parmeans, counts, jaccard = apply_filters(dataset)
	full_table = concat([parmeans, fitmeans])
	
	counts = counts.iloc[0]
	counts.name = 'Count'
	full_table = full_table.append(counts)
	tex = full_table.to_latex(float_format=lambda x: str(round(x,1)))
	print utils.prettify_table(tex, 'table:manual_filtering_%s'%dataset, 'XXX').replace('nan','N/A')
	from plotting import plot_group_overlap
	from utils import make_issue_specific_figure_folder
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








