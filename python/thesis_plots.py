#import ppl
#import matplotlib.pyplot as plt
#import IO
#import numpy as np
#from numpy import log
from pandas import DataFrame, concat
#from ppl import Ppl
import brewer2mpl
import IO
from IO import figure_save_path, table_save_path
import utils
import sys
from utils import make_issue_specific_figure_folder


def issue_21_basic_scatter_plots(dataset):
	"""
	Makes scatter plots of fitness
	"""
	from plotting import make_color_grouped_scatter_plot
	folder = make_issue_specific_figure_folder('21_scatter_plots', dataset)
	fit_data, par_data, gen, ids = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	
	#colormap = brewer2mpl.get_map('YlOrRd', 'Sequential', 9, reverse=True)
	colormap = brewer2mpl.get_map('Spectral', 'Diverging', 9, reverse=True)
	print "Making scatter plots of fitness data for dataset %s"%dataset
	filename = folder + 'a.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='overshoot', y_name='time_to_reach_new_fundamental', color_by='stdev', filename=filename, colormap = colormap, y_function='log')

	filename = folder + 'b.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='overshoot', y_name='stdev', color_by='time_to_reach_new_fundamental', filename=filename, colormap = colormap)

	filename = folder + 'c.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='time_to_reach_new_fundamental', y_name='round_stable', color_by='stdev', filename=filename, colormap = colormap)
	
	filename = folder + 'd.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='stdev', y_name='round_stable', color_by='time_to_reach_new_fundamental', filename=filename, colormap = colormap, x_function='log', y_function='log')

	filename = folder + 'e.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='stdev', y_name='time_to_reach_new_fundamental', color_by='round_stable', filename=filename, colormap = colormap, x_function='log', y_function='log')
	
	filename = folder + 'f.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='time_to_reach_new_fundamental', y_name='stdev', color_by='round_stable', filename=filename, colormap = colormap)

	filename = folder + 'g.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='time_to_reach_new_fundamental', y_name='stdev', color_by='round_stable', filename=filename, colormap = colormap, x_function='log', y_function='log', color_function='log')

def issue_26_plot_pca_and_cluster(dataset, n_clusters):
	"""
	PCA and Kmeans for dataset 1
	"""
	from data_analysis import calculate_pca
	from sklearn.cluster import KMeans
	from plotting import make_color_grouped_scatter_plot, make_scatter_plot_for_labelled_data
	
	def do_for_dataset(data, data_name):
		transformed_data, pca, components  = calculate_pca(data, n_components=3, normalize = True)
		colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
		filename = figure_save_path + dataset + '_issue_26_1_%s_PCA_3components.png'%data_name
		print "Making scatter plot of PCA decompositions of %s data for dataset %s"%(data_name, dataset)
		make_color_grouped_scatter_plot(data_frame=transformed_data, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)
		
		kmeans = KMeans(n_clusters = n_clusters)
		kmeans.fit(transformed_data.values)
		colormap = brewer2mpl.get_map('Set2', 'Qualitative', n_clusters, reverse=True)
		filename = figure_save_path + dataset + '_issue_26_2_%s_clusters_in_PCA_space.png'%data_name
		print "Making scatter plot of K-means clusters of %s data for dataset %s"%(data_name, dataset)
		make_scatter_plot_for_labelled_data(data_frame=transformed_data, x_name='d1', y_name='d2', labels=kmeans.labels_, filename=filename, colormap = colormap, legend=True)
	
	fit_data, par_data, gen, ids = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	do_for_dataset(fit_data, 'fitness')
	do_for_dataset(par_data, 'parameter')
	
	

def issue_29_reduce_and_affinity(dataset, affinity_damping, load_clusters_from_file = False):
	from data_analysis import reduce_npoints_kmeans, calculate_pca
	from sklearn.cluster import AffinityPropagation
	from sklearn.preprocessing import scale
	from plotting import make_color_grouped_scatter_plot
	from plotting import make_scatter_plot_for_labelled_data

	"""
	Use KMeans on fitness data to reduce number of datapoints and then use affinity propagation
	"""
	def do_issue(data, data_name):
		reduced_points, labels, km = reduce_npoints_kmeans(dataframe = data, dataset_name = dataset, data_name=data_name, n_datapoints = 1000, load_from_file = False)
		transformed_data, pca, components = calculate_pca(reduced_points, n_components=3)
		colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
		filename = figure_save_path + dataset + '_issue_29_1_%s_reduced_number_of_points.png'%data_name
		print "Making scatter plot of %s data for dataset %s, where the number of points have been reduced by K-Means clustering"%(data_name, dataset)
		make_color_grouped_scatter_plot(data_frame=transformed_data, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)

		ap = AffinityPropagation(damping=affinity_damping)
		ap.fit(reduced_points)
		print "Making scatter plot of Affinity Propagation clusters of %s data for dataset %s"%(data_name, dataset)
		filename = figure_save_path + dataset + '_issue_29_2_%s_affinity.png'%data_name
		make_scatter_plot_for_labelled_data(data_frame=transformed_data, x_name='d1', y_name='d2', labels=ap.labels_, filename=filename, colormap = colormap, legend=True)	
	
		

	fit_data, par_data, gen, ids = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	do_issue(fit_data, 'fitness')
	do_issue(par_data, 'parameter')

	
def issue_88_affinity_after_norm_and_outlier(dataset, load_from_file):
	from sklearn.preprocessing import scale
	from sklearn.cluster import AffinityPropagation
	from data_analysis import reduce_npoints_kmeans, outlier_detection_with_SVM
	fit_data, par_data, gen, ids = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	reduced_fitness, labels, km = reduce_npoints_kmeans(dataframe = par_data, dataset_name = dataset, data_name='parameter', n_datapoints = 1000, load_from_file = load_from_file)
	inliers_idx_r, outliers_idx_r = outlier_detection_with_SVM(reduced_fitness, kernel='rbf', gamma=0.1, outlier_percentage=0.01)
	return inliers_idx_r, outliers_idx_r
	ap = AffinityPropagation(damping=0.97)
	ap.t
	#trans_full_dataset, pca, components = calculate_pca(fit_data, n_components=3)
	
	
	

	#dbscan = DBSCAN(min_samples=100)


def issue_43_outlier_detection(dataset, n_clusters, gamma, load_from_file = False):
	from plotting import make_color_grouped_scatter_plot, make_scatter_plot_for_labelled_data
	from data_analysis import reduce_npoints_kmeans, outlier_detection_with_SVM, calculate_pca
	from sklearn.cluster import KMeans	
	fit_data, par_data, gen, ids = IO.load_pickled_generation_dataframe(dataset_name=dataset)


	reduced_par, labels_all_datapoints, km = reduce_npoints_kmeans(par_data, dataset, 'parameters', n_datapoints=1000, load_from_file=load_from_file)
	
	inliers_idx, outliers_idx = outlier_detection_with_SVM(reduced_par, kernel='rbf', gamma=gamma, outlier_percentage=0.01)
	transformed_data, pca, components = calculate_pca(par_data.iloc[inliers_idx,:], n_components = 3, whiten=False, normalize=True)
	
	kmeans = KMeans(n_clusters = n_clusters)
	kmeans.fit(transformed_data.values)
	
	filename = figure_save_path + dataset + '_issue_43_parameters_PCA_after_outlier_detection.png'
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	make_color_grouped_scatter_plot(transformed_data, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)

	print "Making scatter plot of Affinity Propagation clusters of fitness data for dataset %s"%dataset
	filename = figure_save_path + dataset + '_issue_43_parameters_kmeans_after_outlier_and_PCA.png'
	colormap = brewer2mpl.get_map('Set2', 'Qualitative', n_clusters, reverse=True)

	make_scatter_plot_for_labelled_data(data_frame=transformed_data, x_name='d1', y_name='d2', labels=kmeans.labels_, filename=filename, colormap = colormap, legend=True)
	
	
	
	

def issue_55_calc_cluster_stats(dataset, n_clusters, gamma, load_from_file = False):
	from data_analysis import reduce_npoints_kmeans, outlier_detection_with_SVM, calculate_stats_for_dataframe, calculate_pca
	from sklearn.cluster import KMeans
	from utils import get_group_vector_for_reduced_dataset, export_stats_dict_as_tex
	from plotting import make_scatter_plot_for_labelled_data
	#from scipy.stats import f_oneway
	#from numpy import where
	#from sklearn.preprocessing import scale

	def reduce_outlier_cluster_stats(data, data_target, data_name, gamma):
		reduced, cluster_assignment_o2r, km_r = reduce_npoints_kmeans(data, dataset, data_name, n_datapoints=1000, load_from_file=load_from_file)	
		inliers_idx_r, outliers_idx_r = outlier_detection_with_SVM(reduced, kernel='rbf', gamma=gamma, outlier_percentage=0.01)
		kmeans = KMeans(n_clusters = n_clusters)
		kmeans.fit(reduced.iloc[inliers_idx_r, :])
		indexes_i, labels_i =  get_group_vector_for_reduced_dataset(inliers_idx_r, cluster_assignment_o2r, cluster_assignment_r2g = kmeans.labels_)
		print DataFrame(kmeans.cluster_centers_, columns=data.columns)

		all_data = concat([par_data, fit_data], axis=1)
		stats = calculate_stats_for_dataframe(all_data.iloc[indexes_i,:], labels_i)
		export_stats_dict_as_tex(dataset, stats, data_name)
		#groups = map(lambda x: scale(data_target.iloc[indexes_i[where(labels_i==x)]]), range(n_clusters))
		#fval, pval = f_oneway(*groups)
		#print "P-vals for %s clusters: %s"%(data_name, pval)
		transformed_data, pca, components  = calculate_pca(data.iloc[indexes_i,:], n_components=3, normalize = True)	
		filename = figure_save_path + dataset + 'isse55_1_clusters_in_PCA_%s_space.png'%(data_name)
		colormap = brewer2mpl.get_map('Set2', 'Qualitative', n_clusters, reverse=True)
		print "Making scatter plot of K-means clusters of %s data for dataset %s"%(data_name, dataset)
		make_scatter_plot_for_labelled_data(data_frame=transformed_data, x_name='d1', y_name='d2', labels=labels_i, filename=filename, colormap = colormap, legend=True)
		#fitness_groups = map(lambda x: data.iloc[indexes_i[where(labels_i==x)]], range(n_clusters))
		
	fit_data, par_data, gen, ids = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	reduce_outlier_cluster_stats(par_data, fit_data, 'parameter', gamma=gamma[0])
	reduce_outlier_cluster_stats(fit_data, par_data, 'fitness', gamma=gamma[1])

	
def issue_65_run_sim_for_clusters(dataset, n_clusters, load_from_file = False):
	from settings import get_fixed_parameters
	from fitness import evaluate_simulation_results
	import settings
	import os
	settings.PLOT_SAVE_PROB = 1
	
	fit, par, gen, ids = IO.load_pickled_generation_dataframe(dataset)
	stats, pvals, kmeans = issue_55_calc_cluster_stats(dataset, n_clusters, load_from_file)
	graph_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/data_for_figures/issue_65/'
	
	for c, cluster in enumerate(kmeans.cluster_centers_):
		parameters = get_fixed_parameters()
		
		parameters.update(dict(zip(par.columns, map(int, cluster))))
		print parameters
		#plot_name = '%scluster%s'%(graph_folder, c)
		folder = '%scluster_%s/'%(graph_folder,c)
		if not os.path.exists(folder): os.makedirs(folder)
		evaluate_simulation_results(folder, 0, parameters, range(4), autorun=True)
		
			

def issue_36_kernelPCA(dataset, load_from_file):
	import IO
	from data_analysis import reduce_npoints_kmeans
	fit_data, par_data, gen, ids = IO.load_pickled_generation_dataframe(dataset_name=dataset)


	reduced_par, labels_all_datapoints, km = reduce_npoints_kmeans(par_data, dataset, n_datapoints=1000, load_from_file=load_from_file)	
	return reduced_par, labels_all_datapoints, km


def issue_82_parameter_evolution(dataset, vline_x = []):
	def get_stats(name, stats):
		return [getattr(group[name], s)() for s in stats]

	def d3():
		#make_pretty_generation_plot(folder + 'd3_latpars_s.png', generations, [group['ssmm_latency_s'].mean(), group['sc_latency_s'].mean()], 'Average latency std', ['Market makers', 'Chartists'])
		make_pretty_generation_plot(folder + 'nAgents.png', generations, [group['ssmm_nAgents'].mean(), group['sc_nAgents'].mean()], 'Average number of agents', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_nAgents'].std(), group['sc_nAgents'].std()])
		make_pretty_generation_plot(folder + 'thinkpars_s.png', generations, [group['ssmm_think_s'].mean(), group['sc_think_s'].mean()], 'Average if the thinking time standard deviation', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_think_s'].std(), group['sc_think_s'].std()])
		make_pretty_generation_plot(folder + 'thinkpars_mu.png', generations, [group['ssmm_think_mu'].mean(), group['sc_think_mu'].mean()], 'Average of the thinking time distribution mean', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_think_mu'].std(), group['sc_think_mu'].std()])
		make_pretty_generation_plot(folder + 'latpars_mu.png', generations, [group['ssmm_latency_mu'].mean(), group['sc_latency_mu'].mean()], 'Average of the latency distribution mean', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_latency_mu'].std(), group['sc_latency_mu'].std()])
		make_pretty_generation_plot(folder + 'latpars_s.png', generations, [group['ssmm_latency_s'].mean(), group['sc_latency_s'].mean()], 'Average of the latency distribution standard deviation', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_latency_s'].std(), group['sc_latency_s'].std()])
		make_pretty_generation_plot(folder + 'scwaittime_mu.png', generations, [group['sc_waitTimeBetweenTrading_mu'].mean()], 'Average of the chartist waiting time distribution mean', ['Chartists'], y_errorbar=[group['sc_waitTimeBetweenTrading_mu'].std()])
		make_pretty_generation_plot(folder + 'sctimehorizon_mu.png', generations, [group['sc_timehorizon_mu'].mean()], 'Average of the chartist time horizon distribution mean', ['Chartists'], y_errorbar=[group['sc_timehorizon_mu'].std()])
	
	def d9():
		make_pretty_generation_plot(folder + 'latpars_s.png', generations, [group['ssmm_latency_s'].mean(), group['sc_latency_s'].mean()], 'Average latency std', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_latency_s'].std(), group['sc_latency_s'].std()])
		make_pretty_generation_plot(folder + 'latpars_mu.png', generations, [group['ssmm_latency_mu'].mean(), group['sc_latency_mu'].mean()], 'Average latency mean', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_latency_mu'].std(), group['sc_latency_mu'].std()])
		make_pretty_generation_plot(folder + 'thinkpars_s.png', generations, [group['ssmm_think_s'].mean(), group['sc_think_s'].mean()], 'Average think time std', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_think_s'].std(), group['sc_think_s'].std()])
		make_pretty_generation_plot(folder + 'thinkpars_mu.png', generations, [group['ssmm_think_mu'].mean(), group['sc_think_mu'].mean()], 'Average think time mean', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_think_mu'].std(), group['sc_think_mu'].std()])
		make_pretty_generation_plot(folder + 'scwaittime_mu.png', generations, [group['sc_waitTimeBetweenTrading_mu'].mean()], 'Average of the chartist waiting time distribution mean', ['Chartists'], y_errorbar=[group['sc_waitTimeBetweenTrading_mu'].std()])
		make_pretty_generation_plot(folder + 'sctimehorizon_mu.png', generations, [group['sc_timehorizon_mu'].mean()], 'Average of the chartist time horizon distribution mean', ['Chartists'], y_errorbar=[group['sc_timehorizon_mu'].std	()])
	
	def d10():
		make_pretty_generation_plot(folder + 'latpars_s.png', generations, [group['ssmm_latency_s'].mean(), group['sc_latency_s'].mean()], 'Average latency std', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_latency_s'].std(), group['sc_latency_s'].std()], vline_x = vline_x)
		
		fig, ax, filename = make_pretty_generation_plot(folder + 'latpars_mu.png', generations, [group['ssmm_latency_mu'].mean(), group['sc_latency_mu'].mean()], 'Average latency mean', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_latency_mu'].std(), group['sc_latency_mu'].std()], vline_x = vline_x)
		ax.fill_between(x=[30, 50], y1=ax.get_ylim()[0], y2=ax.get_ylim()[1], color='red', alpha=0.1)
		ax.fill_between(x=[0, 17], y1=ax.get_ylim()[0], y2=ax.get_ylim()[1], color='blue', alpha=0.1)
		fig.savefig(filename)
		
		fig, ax, filename = make_pretty_generation_plot(folder + 'nAgents.png', generations, [group['ssmm_nAgents'].mean()], 'Average number of agents', ['Market makers'], y_errorbar=[group['ssmm_nAgents'].std()], vline_x = vline_x)
		ax.fill_between(x=[30, 50], y1=ax.get_ylim()[0], y2=ax.get_ylim()[1], color='red', alpha=0.1)
		ax.fill_between(x=[0, 17], y1=ax.get_ylim()[0], y2=ax.get_ylim()[1], color='blue', alpha=0.1)
		fig.savefig(filename)

		fig, ax, filename = make_pretty_generation_plot(folder + 'time_to_reach_new_fundamental.png', generations, get_stats('time_to_reach_new_fundamental', stats), 'Time to reach fundamental after shock', stats, vline_x = vline_x)
		ax.fill_between(x=[0, 17], y1=ax.get_ylim()[0], y2=ax.get_ylim()[1], color='blue', alpha=0.1)
		fig.savefig(filename)
		
		fig, ax, filename = make_pretty_generation_plot(folder + 'stdev.png', generations, get_stats('stdev', stats), 'Standard deviation of trade prices entering stability margin', stats, y_logscale=True, vline_x=vline_x)
		ax.fill_between(x=[30, 50], y1=ax.get_ylim()[0], y2=ax.get_ylim()[1], color='red', alpha=0.1)
		fig.savefig(filename)

		fig, ax, filename = make_pretty_generation_plot(folder + 'round_stable.png', generations, get_stats('round_stable', stats), 'Round stable', stats, y_logscale=True, vline_x=vline_x)
		ax.fill_between(x=[30, 50], y1=ax.get_ylim()[0], y2=ax.get_ylim()[1], color='red', alpha=0.1)
		fig.savefig(filename)
		
		fig, ax, filename = make_pretty_generation_plot(folder + 'overshoot.png', generations, get_stats('overshoot', stats), 'Overshoot', stats, vline_x=vline_x)
		ax.fill_between(x=[30, 50], y1=ax.get_ylim()[0], y2=ax.get_ylim()[1], color='red', alpha=0.1)
		ax.fill_between(x=[0, 17], y1=ax.get_ylim()[0], y2=ax.get_ylim()[1], color='blue', alpha=0.1)
		fig.savefig(filename)

	def d11():
		make_pretty_generation_plot(folder + 'latpars_s.png', generations, [group['ssmm_latency_s'].mean(), group['sc_latency_s'].mean()], 'Average latency std', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_latency_s'].std(), group['sc_latency_s'].std()])
		make_pretty_generation_plot(folder + 'latpars_mu.png', generations, [group['ssmm_latency_mu'].mean(), group['sc_latency_mu'].mean()], 'Average latency mean', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_latency_mu'].std(), group['sc_latency_mu'].std()])
		make_pretty_generation_plot(folder + 'nAgents.png', generations, [group['sc_nAgents'].mean()], 'Average number of agents', ['Chartists'], y_errorbar=[group['sc_nAgents'].std()])



	from plotting import make_pretty_generation_plot
	folder = make_issue_specific_figure_folder('82_generation_plots', dataset)
	fit,par,gen,ids = IO.load_pickled_generation_dataframe(dataset)
	all_data = concat([fit,par, DataFrame(gen)], axis=1)
	generations = list(set(all_data['gen']))
	group = all_data.groupby('gen')
	stats = ['min', 'mean', 'median']
	
	make_pretty_generation_plot(folder + 'time_to_reach_new_fundamental.png', generations, get_stats('time_to_reach_new_fundamental', stats), 'Time to reach fundamental after shock', stats, vline_x = vline_x)
	make_pretty_generation_plot(folder + 'stdev.png', generations, get_stats('stdev', stats), 'Standard deviation of trade prices entering stability margin', stats, y_logscale=True, vline_x=vline_x)
	make_pretty_generation_plot(folder + 'round_stable.png', generations, get_stats('round_stable', stats), 'Round stable', stats, y_logscale=True, vline_x=vline_x)
	make_pretty_generation_plot(folder + 'overshoot.png', generations, get_stats('overshoot', stats), 'Overshoot', stats, vline_x=vline_x)
	eval(dataset)()	
	

def issue_101_plot_pars_vs_fitness(dataset, overshoot_threshold, preloaded_data = None):
	from plotting import get_pretty_xy_plot, make_pretty_scatter_plot
	from numpy import where
	folder = make_issue_specific_figure_folder('101_pars_vs_fits', dataset)
	stats = ['mean', 'median']

	def get_plots_to_make(fitness_types):
		plots_to_make = list()
		for fitness_type in fitness_types:
			for stat in stats:
				plots_to_make.append((fitness_type, stat))
		return plots_to_make


	def mkplot(all_data, groupby, plots_to_make):
		g = all_data.groupby(groupby)
		#x = g.groups.keys()
		s = all_data.sort(groupby)
		sorted_x, index_order = zip(*sorted(zip(g.groups.keys(), range(len(g.groups.keys())))))
		for attr, stat in plots_to_make:
			print groupby, attr, stat
			y = getattr(g[attr],stat)()
			filename = '%s%s__vs__%s(%s)'%(folder, groupby, attr, stat)
			ax, fig = get_pretty_xy_plot(sorted_x, y, groupby, '%s (%s)'%(attr, stat), filename, g[attr].std()/2, save_figure = False)
			filename = '%s%s__vs__%s(%s)_scatter'%(folder, groupby, attr, stat)
			make_pretty_scatter_plot(s[groupby], s[attr], groupby, '%s (%s)'%(attr, stat), filename, ax=ax, fig=fig)
	
	def run_analysis(groups, data, plots_to_make):
		for groupby in groups:
			mkplot(data, groupby, plots_to_make)

	
	folder = make_issue_specific_figure_folder('101_pars_vs_fits', dataset)
	stats = ['mean', 'median']

	if preloaded_data is None: fit, par, gen, ids = IO.load_pickled_generation_dataframe(dataset)
	else:
		try:
			fit = preloaded_data['fit']
			par = preloaded_data['par']
		except KeyError, e:
			print "Provide dict with keys 'fit' and 'par' containing dataframes for fit and par data"
			print e
			sys.exit(1)
	#o = where(fit.overshoot > overshoot_threshold)[0]
	not_o = where(fit.overshoot <= overshoot_threshold)[0]
	all_data = concat([par.iloc[not_o],fit.iloc[not_o]]	,axis=1)
	plots_to_make = get_plots_to_make(fit.columns)
	run_analysis(par.columns, all_data, plots_to_make)

	#eval(dataset)(all_data, fitness_types = fit.columns, parameters_to_group_by = par.columns)

def issue_102_plot_overshoot_and_noreaction():
	### Cannot be finished yet. Needs to be run with "new" data
	pass

def issue_103_manually_removing_large_fitness_points(dataset, overshoot_threshold):
	from plotting import make_color_grouped_scatter_plot
	from numpy import where
	folder = make_issue_specific_figure_folder('103_scatter_manual_outlier', dataset)
	fit, par, gen, ids = IO.load_pickled_generation_dataframe(dataset)
	#fit['overshoot'] -= 2
	o = where(fit.overshoot > overshoot_threshold)[0]
	not_o = where(fit.overshoot <= overshoot_threshold)[0]
	
	#colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	colormap = brewer2mpl.get_map('Spectral', 'Diverging', 9, reverse=True)

	filename = folder + 'a.png'
	make_color_grouped_scatter_plot(fit.iloc[not_o], 'stdev', 'time_to_reach_new_fundamental', 'round_stable', filename, colormap)
	
	filename = folder + 'b.png'
	make_color_grouped_scatter_plot(fit.iloc[not_o], 'stdev', 'time_to_reach_new_fundamental', 'round_stable', filename, colormap, x_function='log', y_function='log', color_function='log')
	
	filename = folder + 'c.png'
	make_color_grouped_scatter_plot(data_frame=fit.iloc[not_o], x_name='time_to_reach_new_fundamental', y_name='round_stable', color_by='stdev', filename=filename, colormap = colormap)

	filename = folder + 'd.png'
	make_color_grouped_scatter_plot(data_frame=fit.iloc[not_o], x_name='stdev', y_name='round_stable', color_by='time_to_reach_new_fundamental', filename=filename, colormap = colormap, x_function='log', y_function='log')

	filename = folder + 'e.png'
	make_color_grouped_scatter_plot(data_frame=fit.iloc[not_o], x_name='stdev', y_name='overshoot', color_by='time_to_reach_new_fundamental', filename=filename, colormap = colormap)

	filename = folder + 'h.png'
	make_color_grouped_scatter_plot(fit.iloc[not_o], 'stdev', 'time_to_reach_new_fundamental', 'round_stable', filename, colormap, x_function='log')

	filename = folder + 'i.png'
	make_color_grouped_scatter_plot(fit.iloc[not_o], 'stdev', 'time_to_reach_new_fundamental', 'round_stable', filename, colormap)
	
	filename = folder + 'l.png'
	make_color_grouped_scatter_plot(data_frame=fit.iloc[not_o], x_name='stdev', y_name='round_stable', color_by='overshoot', filename=filename, colormap = colormap, x_function='log', y_function='log')

	filename = folder + 'k.png'
	make_color_grouped_scatter_plot(fit.iloc[not_o], 'stdev', 'time_to_reach_new_fundamental', 'overshoot', filename, colormap, x_function='log')

	filename = folder + 'g.png'
	make_color_grouped_scatter_plot(fit.iloc[not_o], 'overshoot', 'time_to_reach_new_fundamental', 'round_stable', filename, colormap)
	
	filename = folder + 'j.png'
	ax, fig = make_color_grouped_scatter_plot(data_frame=fit.iloc[not_o], x_name='time_to_reach_new_fundamental', y_name='round_stable', color_by='overshoot', filename=filename, colormap = colormap)
	ax.plot(range(0,10**5), range(0,10**5), linestyle='dashed', color='black', alpha = 0.5)
	ax.text(40000, 80000, "A", fontsize = 18, alpha = 0.6)
	ax.text(80000, 40000, "B", fontsize = 18, alpha = 0.6)
	fig.savefig(filename)

	#Plot with A B regions
	filename = folder + 'f.png'
	ax, fig = make_color_grouped_scatter_plot(data_frame=fit.iloc[not_o], x_name='time_to_reach_new_fundamental', y_name='round_stable', color_by='stdev', filename=filename, colormap = colormap, color_function='log')
	ax.plot(range(0,10**5), range(0,10**5), linestyle='dashed', color='black', alpha = 0.5)
	ax.text(40000, 80000, "A", fontsize = 18, alpha = 0.6)
	ax.text(80000, 40000, "B", fontsize = 18, alpha = 0.6)
	fig.savefig(filename)
	
	stats = concat([par.iloc[not_o,:].mean(), par.iloc[o,:].mean(), par.iloc[not_o,:].std(), par.iloc[o,:].std()], axis=1)
	lt = '$\overshoot > %s$'%overshoot_threshold
	st = '$\overshoot < %s$'%overshoot_threshold
	stats.columns = ['%s (mean)'%st, '%s (mean)'%lt, '%s (std)'%st, '%s (std)'%lt]
	
	tex_index = utils.get_latex_par_names_from_list(stats.index.tolist())
	stats.index = tex_index
	print utils.prettify_table(stats.to_latex(float_format=lambda x: str(round(x,1))), 'LABEL', 'CAPTION')
	return stats

def issue_108(dataset, n_clusters, overshoot_threshold, load_pickled_labels = False, labels_to_include = []):
	from numpy import where, repeat, log, random
	from sklearn.cluster import KMeans
	from plotting import make_scatter_plot_for_labelled_data
	from data_analysis import calculate_stats_for_dataframe
	from sklearn.mixture import GMM
	from sklearn.decomposition import PCA
	from sklearn.preprocessing import scale
	import cPickle
	if labels_to_include: labels_to_plot = labels_to_include
	else: labels_to_plot = range(n_clusters)

	plots_to_make = [
					{'x_name':'stdev', 'x_function':'log', 'y_name':'round_stable', 'y_function':'log'},
					{'x_name':'time_to_reach_new_fundamental', 'y_name':'round_stable'},
					{'x_name':'stdev', 'x_function':'log', 'y_name':'time_to_reach_new_fundamental'}
					]

	folder = make_issue_specific_figure_folder('108 cluster after removing outliers', dataset)
	fit, par, gen, ids = IO.load_pickled_generation_dataframe(dataset)
	o = where(fit.overshoot > overshoot_threshold)[0]
	not_o = where(fit.overshoot <= overshoot_threshold)[0]
	data_to_plot = fit.iloc[not_o]
	pca = PCA(n_components = 3)
	par_transformed = pca.fit_transform(scale(par.iloc[not_o].astype(float)))
	par_transformed += random.random(par_transformed.shape)*0.2
	par_inliers_PCA = DataFrame(par_transformed, columns = ['PC1', 'PC2', 'PC3'])
	
	"""
	print 'Component 0:'
	print map(lambda c, n: '%s=%.3g'%(n, c), pca.components_[0], utils.get_latex_par_names_from_list(par.columns))
	print 'Component 1:'
	print map(lambda c, n: '%s=%.3g'%(n, c), pca.components_[1], utils.get_latex_par_names_from_list(par.columns))
	print 'Component 1:'
	print map(lambda c, n: '%s=%.3g'%(n, c), pca.components_[2], utils.get_latex_par_names_from_list(par.columns))
	"""
	colormap = brewer2mpl.get_map('Paired', 'Qualitative', n_clusters, reverse=True)

	def print_pca_components(pca, name):
		from plotting import plot_pca_components
		components = DataFrame(pca.components_, columns=utils.get_latex_par_names_from_list(par.columns))
		filename = folder + 'clustering_%s_%s'%(dataset, name)
		plot_pca_components(filename=filename, components=components[::-1])
		components['$\\gamma$'] = pca.explained_variance_ratio_
		
		filename = folder + 'clustering_%s_%s.tex'%(dataset, name)
		tex = utils.prettify_table(components.to_latex(float_format=lambda x: str(round(x,2))), label='table:clustering_%s_%s'%(dataset, name), caption='XXX')
		with open(filename, 'w') as f:
				f.write(tex)
	print_pca_components(pca, 'allclusters')

	def make_tables(clustering_method, name, cluster_labels):
		
		def make_table(stat):
			fit_inlier_stats = calculate_stats_for_dataframe(fit.iloc[not_o,:], cluster_labels)
			fit_outlier_stats = calculate_stats_for_dataframe(fit.iloc[o,:], repeat(0, len(o)))
			fit_mean_table = concat([fit_inlier_stats[stat], fit_outlier_stats[stat]], axis=1)
			fit_mean_table = fit_mean_table.drop('Count', axis=0)
			fit_mean_table.index = utils.get_latex_par_names_from_list(fit_mean_table.index)
			fit_mean_table = fit_mean_table.transpose()
			
			par_inlier_stats = calculate_stats_for_dataframe(par.iloc[not_o,:], cluster_labels)
			par_outlier_stats = calculate_stats_for_dataframe(par.iloc[o,:], repeat(0, len(o)))
			par_mean_table = concat([par_inlier_stats[stat], par_outlier_stats[stat]], axis=1)
			par_mean_table.index = utils.get_latex_par_names_from_list(par_mean_table.index)
			par_mean_table = par_mean_table.transpose()
			
			full_table = concat([fit_mean_table, par_mean_table], axis=1)
			print full_table.columns
			full_table = full_table.sort('\\overshoot')
			tex = full_table.to_latex(float_format=lambda x: str(round(x,1)))
			
			tex = utils.prettify_table(full_table.to_latex(float_format=lambda x: str(round(x,1))), 'table:fit_gmm_'+name, 'gmm_'+name)
			filename = folder + '%s_%s_%s_%s.tex'%(n_clusters,clustering_method, name, stat)
			with open(filename, 'w') as f:
				f.write(tex)
		
		make_table('Mean')
		make_table('Std')
	#def make_pca_plots():


	def make_plots(clustering_method, data_name, labels):
		
		if not labels_to_include:
			for i, plotargs in enumerate(plots_to_make):
				filename = folder + '%s_%s_%s_fit_%s.png'%(n_clusters, clustering_method, data_name, i)
				make_scatter_plot_for_labelled_data(data_to_plot, labels=labels, filename=filename, colormap=colormap, legend = True, **plotargs)
			filename = folder + '%s_%s_%s_par.png'%(n_clusters, clustering_method, data_name)
			make_scatter_plot_for_labelled_data(par_inliers_PCA, x_name='PC1', y_name='PC2', labels=labels, filename=filename, colormap=colormap, legend = True)
			filename = folder + '%s_%s_%s_par_omit.png'%(n_clusters, clustering_method, data_name)
			make_scatter_plot_for_labelled_data(par_inliers_PCA, x_name='PC1', y_name='PC2', labels=labels, filename=filename, colormap=colormap, legend = True, omit_largest=n_clusters-4)
		else:
			ltp = '_'.join(map(str, labels_to_plot))
			filename = folder + '%s_%s_%s_par_%s_pca_1v2.png'%(n_clusters, clustering_method, data_name, ltp)
			data = par_inliers_PCA[['PC1', 'PC2']]
			make_scatter_plot_for_labelled_data(data, x_name='PC1', y_name='PC2', labels=labels, filename=filename, colormap=colormap, legend = True, labels_to_plot = labels_to_plot)
			filename = folder + '%s_%s_%s_par_%s_pca_1v3.png'%(n_clusters, clustering_method, data_name, ltp)
			data = par_inliers_PCA[['PC1', 'PC3']]
			make_scatter_plot_for_labelled_data(data, x_name='PC1', y_name='PC3', labels=labels, filename=filename, colormap=colormap, legend = True, labels_to_plot = labels_to_plot)
			filename = folder + '%s_%s_%s_par_%s_pca_2v3.png'%(n_clusters, clustering_method, data_name, ltp)
			data = par_inliers_PCA[['PC2', 'PC3']]
			make_scatter_plot_for_labelled_data(data, x_name='PC2', y_name='PC3', labels=labels, filename=filename, colormap=colormap, legend = True, labels_to_plot = labels_to_plot)
			

			selective_par = par.iloc[not_o][get_indluded_labels_mask(labels, *labels_to_plot)]
			selective_labels = labels[get_indluded_labels_mask(labels, *labels_to_include)]
			pca_selective = PCA(3)
			selective_par = DataFrame(pca_selective.fit_transform(selective_par), columns = ['PC1', 'PC2', 'PC3'])
			
			print_pca_components(pca_selective, ltp)

			filename = folder + '%s_%s_%s_par_%s_pca_1v2_selective.png'%(n_clusters, clustering_method, data_name, ltp)
			data = selective_par[['PC1', 'PC2']]
			make_scatter_plot_for_labelled_data(data, x_name='PC1', y_name='PC2', labels=selective_labels, filename=filename, colormap=colormap, legend = True, labels_to_plot = labels_to_plot)
			filename = folder + '%s_%s_%s_par_%s_pca_1v3_selective.png'%(n_clusters, clustering_method, data_name, ltp)
			data = selective_par[['PC1', 'PC3']]
			make_scatter_plot_for_labelled_data(data, x_name='PC1', y_name='PC3', labels=selective_labels, filename=filename, colormap=colormap, legend = True, labels_to_plot = labels_to_plot)
			filename = folder + '%s_%s_%s_par_%s_pca_2v3_selective.png'%(n_clusters, clustering_method, data_name, ltp)
			data = selective_par[['PC2', 'PC3']]
			make_scatter_plot_for_labelled_data(data, x_name='PC2', y_name='PC3', labels=selective_labels, filename=filename, colormap=colormap, legend = True, labels_to_plot = labels_to_plot)
			#par_data_for_plotting = par.iloc[not_o].copy() + random.random(par.iloc[not_o].shape) * 0.2
			#filename = folder + '%s_%s_%s_par_latencies.png'%(n_clusters, clustering_method, data_name)
			#columns = ['ssmm_latency_mu', 'sc_latency_mu']
			#make_scatter_plot_for_labelled_data(par_data_for_plotting[columns], 'ssmm_latency_mu', 'sc_latency_mu', labels, filename, colormap, legend = True, labels_to_plot = labels_to_plot)
		

	def get_indluded_labels_mask(labels, *labels_to_include):
		from numpy import repeat
		mask = repeat(False, labels.shape)
		for l in labels_to_include:
			mask = mask | (labels == l)
			print mask
		
		return mask

	def cluster_and_label(name, data_to_cluster):
		
		data_to_cluster = scale(data_to_cluster)
		"""
		km_labels_store_file = folder + '%s_%s_%s_classifier.pickle'%(n_clusters, 'km', name)
		if load_pickled_labels:
			with open(km_labels_store_file, 'rb') as fid:
				km_labels = cPickle.load(fid)
		else:
			km = KMeans(n_clusters=n_clusters)
			km.fit(data_to_cluster)
			km_labels = km.predict(data_to_cluster)
			with open(km_labels_store_file, 'wb') as fid:
				cPickle.dump(km_labels, fid)
		make_plots('km', name, km_labels)
		make_tables('km', name, km_labels)
		"""

		gmm_labels_store_file = folder + '%s_%s_%s_classifier.pickle'%(n_clusters, 'gmm', name)
		if load_pickled_labels:
			with open(gmm_labels_store_file, 'rb') as fid:
				gmm_labels = cPickle.load(fid)
		else:
			gmm = GMM(n_components = n_clusters, covariance_type = 'full')
			gmm.fit(data_to_cluster)
			gmm_labels = gmm.predict(data_to_cluster)
			with open(gmm_labels_store_file, 'wb') as fid:
				cPickle.dump(gmm_labels, fid)
		make_plots('gmm', name, gmm_labels)
		make_tables('gmm', name, gmm_labels)

		
		

		
		

	#data_to_cluster = concat([log(fit['stdev']), log(fit['round_stable']), fit['time_to_reach_new_fundamental']], axis=1).iloc[not_o,:]
	#cluster_and_label('logs_logr_t', data_to_cluster)
	"""
	data_to_cluster = concat([log(fit['stdev']), log(fit['round_stable'])], axis=1).iloc[not_o,:]
	cluster_and_label('logs_logr', data_to_cluster, 'stdev', 'round_stable', 'log', 'log')

	data_to_cluster = concat([fit['round_stable'], log(fit['stdev'])], axis=1).iloc[not_o,:]
	cluster_and_label('r_logs', data_to_cluster, 'time_to_reach_new_fundamental', 'round_stable')

	data_to_cluster = concat([fit['round_stable'], fit['time_to_reach_new_fundamental'], log(fit['stdev'])], axis=1).iloc[not_o,:]
	cluster_and_label('t_r_logs', data_to_cluster, 'time_to_reach_new_fundamental', 'round_stable')

	data_to_cluster = concat([fit['round_stable'], fit['time_to_reach_new_fundamental']], axis=1).iloc[not_o,:]
	cluster_and_label('t_r', data_to_cluster, 'time_to_reach_new_fundamental', 'round_stable')
	"""

	data_to_cluster = fit.iloc[not_o,:]
	cluster_and_label('all', data_to_cluster)
	
	data_to_cluster = concat([log(fit['stdev']), log(fit['round_stable']), fit['time_to_reach_new_fundamental']], axis=1).iloc[not_o,:]
	cluster_and_label('logs_logr_t', data_to_cluster)

	#fit_mean_table.columns = ['C0', 'C1', 'C3', 'Outliers']

def issue_113_make_all_tradeprice_plots():
	from plotting import make_pretty_tradeprice_plot
	data_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/data_for_tradeprice_plots/'
	mapping = {
			'gen12_1386710381616696.npz' : 'low_stdev_but_not_stable',
			'gen56_1388690478773536.npz' : 'low_stdev_and_stable',
			'gen13_1387568360402236.npz' : 'high_stdev_but_stable'

	}
	figure_folder = make_issue_specific_figure_folder('issue_113_tradeprice_plots', 'all')
	for dataname, plotname in mapping.items():
		rounds, prices = IO.load_tradeprice_data(data_folder + dataname)
		make_pretty_tradeprice_plot(rounds, prices, figure_folder + plotname, dpi = 200, figsize=tuple(map(lambda x: x*1, [8,4])), format = 'pdf')



def issue_115_agent_ratio(ratio_threshold):
	from numpy import where
	def run_issue(name, all_fit, all_par):
		all_par['chartist_per_market_maker'] = all_par.sc_nAgents.astype(float) / all_par.ssmm_nAgents
		par_to_plot = DataFrame(all_par['chartist_per_market_maker'])

		over_threshold_idx, = where(par_to_plot['chartist_per_market_maker'] > ratio_threshold)
		print 'Dropping over ratio threshold rows: %s (%s rows)'%(over_threshold_idx, len(over_threshold_idx))
		par_to_plot = par_to_plot.drop(over_threshold_idx)
		all_fit = all_fit.drop(over_threshold_idx)
		issue_101_plot_pars_vs_fitness(name, overshoot_threshold = 10, preloaded_data = {'fit':all_fit, 'par':par_to_plot})
	
	fit10, par10, gen10, ids10 = IO.load_pickled_generation_dataframe('d10')
	fit11, par11, gen11, ids11 = IO.load_pickled_generation_dataframe('d11')
	
	par10['sc_nAgents'] = 150
	par11['ssmm_nAgents'] = 52

	run_issue('d10', fit10, par10)
	run_issue('d11', fit10, par10)

	all_par = concat([par10, par11])
	all_fit = concat([fit10, fit11])
	run_issue('d10_d11',all_fit, all_par)

	
def test(dataset, overshoot_threshold):
	from numpy import where, zeros
	import numpy as np
	from sklearn.neighbors.kde import KernelDensity
	folder = make_issue_specific_figure_folder('108 cluster after removing outliers', dataset)
	fit, par, gen, ids = IO.load_pickled_generation_dataframe(dataset)
	o = where(fit.overshoot > overshoot_threshold)[0]
	#not_o = where(fit.overshoot <= overshoot_threshold)[0]
	par = par.drop(o)
	fit = fit.drop(o)
	g1 = par.groupby('ssmm_nAgents').groups.keys()
	g2 = par.groupby('ssmm_latency_mu').groups.keys()
	#stdev_mean = zeros((len(g1), len(g2)))
	data = DataFrame(columns=['ssmm_nAgents', 'ssmm_latency_mu', 'stdev_mean'])
	for a, ssmm_nAgents in enumerate(g1):
		print ssmm_nAgents
		for l, ssmm_latency_mu in enumerate(g2):
			row = dict()
			try:
				row['stdev_mean'] = fit[(par['ssmm_latency_mu'] == ssmm_latency_mu) & (par['ssmm_nAgents'] == ssmm_nAgents)]['stdev'].mean()
				row['ssmm_nAgents'] = ssmm_nAgents
				row['ssmm_latency_mu'] = ssmm_latency_mu
				#print row
				data = data.append(row, ignore_index = True)
			except TypeError:
				print "ARGHS"

	X, Y = np.meshgrid(g1.groups.keys(), g2.groups.keys())
	xy = np.vstack([Y.ravel(), X.ravel()]).T
	return data



def plots_for_d3():
	issue_21_basic_scatter_plots(dataset='d3')
	issue_101_plot_pars_vs_fitness(dataset='d3')
	issue_103_manually_removing_large_fitness_points(dataset='d3', overshoot_threshold=10)

def plots_for_d9():
	issue_21_basic_scatter_plots(dataset='d9')
	issue_82_parameter_evolution(dataset='d9')
	issue_101_plot_pars_vs_fitness(dataset='d9', overshoot_threshold=10)
	issue_103_manually_removing_large_fitness_points(dataset='d9', overshoot_threshold=10)
	#issue_108(dataset='d9', n_clusters=3, overshoot_threshold=10)
	#issue_108(dataset='d9', n_clusters=4, overshoot_threshold=10)
	#issue_108(dataset='d9', n_clusters=8, overshoot_threshold=10)
	#issue_108(dataset='d9', n_clusters=12, overshoot_threshold=10)

def plots_for_d10():
	issue_21_basic_scatter_plots(dataset='d10')
	issue_82_parameter_evolution(dataset='d10', vline_x = [5, 15,20, 50])
	issue_101_plot_pars_vs_fitness(dataset='d10', overshoot_threshold=10)
	issue_103_manually_removing_large_fitness_points(dataset='d10', overshoot_threshold=10)
	#issue_108(dataset='d10', n_clusters=3, overshoot_threshold=10)
	#issue_108(dataset='d10', n_clusters=4, overshoot_threshold=10)
	#issue_108(dataset='d10', n_clusters=8, overshoot_threshold=10)
	#issue_108(dataset='d10', n_clusters=12, overshoot_threshold=10)

def plots_for_d11():
	issue_21_basic_scatter_plots(dataset='d11')
	issue_82_parameter_evolution(dataset='d11')
	issue_101_plot_pars_vs_fitness(dataset='d11', overshoot_threshold=10)
	issue_103_manually_removing_large_fitness_points(dataset='d11', overshoot_threshold=10)
	#issue_108(dataset='d11', n_clusters=3, overshoot_threshold=10)
	#issue_108(dataset='d11', n_clusters=4, overshoot_threshold=10)
	#issue_108(dataset='d11', n_clusters=8, overshoot_threshold=10)
	#issue_108(dataset='d11', n_clusters=12, overshoot_threshold=10)

def remake_make_all_thesis_plots():
	plots_for_d3()
	plots_for_d9()
	plots_for_d10()
	plots_for_d11()
	issue_113_make_all_tradeprice_plots()
	
	

	


if __name__ == '__main__':
	#plot_issue_('d2', 4, True)
	#table_issue_55(dataset='d2', n_clusters=4, load_from_file=True)
	#plot_issue_29(dataset='d1', load_clusters_from_file=False)
	remake_make_all_thesis_plots()