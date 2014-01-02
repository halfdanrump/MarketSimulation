#import ppl
#import matplotlib.pyplot as plt
#import IO
#import numpy as np
#from numpy import log
from pandas import DataFrame, concat
#from ppl import Ppl
import brewer2mpl
import IO
figure_save_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/tex/Figures/'
table_save_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/tex/Tables/'
import utils
import os

def make_issue_specific_figure_folder(name, dataset):
	name.replace('/', '')
	folder = '%s%s/%s/'%(figure_save_path, name, dataset)
	if not os.path.exists(folder): os.makedirs(folder)
	return folder

def issue_21_basic_scatter_plots(dataset):
	"""
	Makes scatter plots of fitness
	"""
	from plotting import make_color_grouped_scatter_plot
	folder = make_issue_specific_figure_folder('21_scatter_plots', dataset)
	fit_data, par_data, gen, ids = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
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


def issue_82_parameter_evolution(dataset):
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
		make_pretty_generation_plot(folder + 'latpars_s.png', generations, [group['ssmm_latency_s'].mean(), group['sc_latency_s'].mean()], 'Average latency std', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_latency_s'].std(), group['sc_latency_s'].std()])
		make_pretty_generation_plot(folder + 'latpars_mu.png', generations, [group['ssmm_latency_mu'].mean(), group['sc_latency_mu'].mean()], 'Average latency mean', ['Market makers', 'Chartists'], y_errorbar=[group['ssmm_latency_mu'].std(), group['sc_latency_mu'].std()])
		make_pretty_generation_plot(folder + 'nAgents.png', generations, [group['ssmm_nAgents'].mean()], 'Average number of agents', ['Market makers'], y_errorbar=[group['ssmm_nAgents'].std()])

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
	
	make_pretty_generation_plot(folder + 'time_to_reach_new_fundamental.png', generations, get_stats('time_to_reach_new_fundamental', stats), 'Time to reach fundamental after shock', stats)
	make_pretty_generation_plot(folder + 'stdev.png', generations, get_stats('stdev', stats), 'Standard deviation of trade prices entering stability margin', stats, y_logscale=True)
	make_pretty_generation_plot(folder + 'round_stable.png', generations, get_stats('round_stable', stats), 'Round entering stability margin', stats, y_logscale=True)
	make_pretty_generation_plot(folder + 'overshoot.png', generations, get_stats('overshoot', stats), 'Overshoot', stats)
	eval(dataset)()	
	

def issue_101_plot_pars_vs_fitness(dataset):
	from plotting import make_pretty_multiline_xy_plot, make_pretty_scatter_plot
	folder = make_issue_specific_figure_folder('101_pars_vs_fits', dataset)
	#if not eos.mkdir(folder)
	def mkplot(groupby, plots_to_make):
		g = a.groupby(groupby)
		x = g.groups.keys()
		s = a.sort(groupby)
		for attr, stat in plots_to_make:
			print groupby, attr, stat
			y = getattr(g[attr],stat)()
			filename = '%s%s__vs__%s(%s)'%(folder, groupby, attr, stat)
			make_pretty_multiline_xy_plot(x, groupby, '%s (%s)'%(attr, stat), filename, g[attr].std()/2, y)
			filename = '%s%s__vs__%s_scatter'%(folder, groupby, attr)
			make_pretty_scatter_plot(s[groupby], s[attr], groupby, attr, filename)
	
	def d9():
		groupby = 'ssmm_latency_mu'
		plots_to_make = [('overshoot', 'mean'), ('time_to_reach_new_fundamental', 'mean')]
		mkplot(groupby, plots_to_make)

		groupby = 'sc_latency_mu'
		plots_to_make = [('overshoot', 'min'), ('time_to_reach_new_fundamental', 'std'), ('time_to_reach_new_fundamental', 'min'), ('time_to_reach_new_fundamental', 'mean')]
		mkplot(groupby, plots_to_make)

		groupby = 'ssmm_latency_s'
		plots_to_make = [('overshoot', 'mean'), ('time_to_reach_new_fundamental', 'mean')]
		mkplot(groupby, plots_to_make)

		groupby = 'sc_latency_s'
		plots_to_make = [('overshoot', 'mean'), ('time_to_reach_new_fundamental', 'mean'), ('time_to_reach_new_fundamental', 'min')]
		mkplot(groupby, plots_to_make)

	def d10():
		groupby = 'ssmm_latency_mu'
		plots_to_make = [('overshoot', 'mean'), ('time_to_reach_new_fundamental', 'mean')]
		mkplot(groupby, plots_to_make)

		groupby = 'sc_latency_mu'
		plots_to_make = [('overshoot', 'min'), ('time_to_reach_new_fundamental', 'std'), ('time_to_reach_new_fundamental', 'min'), ('time_to_reach_new_fundamental', 'mean')]
		mkplot(groupby, plots_to_make)

		groupby = 'ssmm_latency_s'
		plots_to_make = [('overshoot', 'mean'), ('time_to_reach_new_fundamental', 'mean')]
		mkplot(groupby, plots_to_make)

		groupby = 'sc_latency_s'
		plots_to_make = [('overshoot', 'mean'), ('time_to_reach_new_fundamental', 'mean'), ('time_to_reach_new_fundamental', 'min')]
		mkplot(groupby, plots_to_make)		

		groupby = 'ssmm_nAgents'
		plots_to_make = [('overshoot', 'mean'), ('time_to_reach_new_fundamental', 'mean'), ('time_to_reach_new_fundamental', 'min')]
		mkplot(groupby, plots_to_make)

	def d11():
		groupby = 'ssmm_latency_mu'
		plots_to_make = [('overshoot', 'mean'), ('time_to_reach_new_fundamental', 'mean')]
		mkplot(groupby, plots_to_make)

		groupby = 'sc_latency_mu'
		plots_to_make = [('overshoot', 'min'), ('time_to_reach_new_fundamental', 'std'), ('time_to_reach_new_fundamental', 'min'), ('time_to_reach_new_fundamental', 'mean')]
		mkplot(groupby, plots_to_make)

		groupby = 'ssmm_latency_s'
		plots_to_make = [('overshoot', 'mean'), ('time_to_reach_new_fundamental', 'mean')]
		mkplot(groupby, plots_to_make)

		groupby = 'sc_latency_s'
		plots_to_make = [('overshoot', 'mean'), ('time_to_reach_new_fundamental', 'mean'), ('time_to_reach_new_fundamental', 'min')]
		mkplot(groupby, plots_to_make)		

		groupby = 'sc_nAgents'
		plots_to_make = [('overshoot', 'mean'), ('time_to_reach_new_fundamental', 'mean'), ('time_to_reach_new_fundamental', 'min')]
		mkplot(groupby, plots_to_make)

	fit, par, gen, ids = IO.load_pickled_generation_dataframe(dataset)
	a = concat([par,fit],axis=1)
	eval(dataset)()

def issue_102_plot_overshoot_and_noreaction():
	### Cannot be finished yet. Needs to be run with "new" data
	pass

def issue_103_manually_removing_large_fitness_points(dataset, overshoot_threshold):
	from plotting import make_color_grouped_scatter_plot
	from numpy import where
	folder = make_issue_specific_figure_folder('103_scatter_manual_outlier', dataset)
	fit, par, gen, ids = IO.load_pickled_generation_dataframe(dataset)
	o = where(fit.overshoot > overshoot_threshold)[0]
	not_o = where(fit.overshoot <= overshoot_threshold)[0]
	
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	
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
	
	stats = concat([par.iloc[not_o,:].mean(), par.iloc[o,:].mean(), par.iloc[not_o,:].std(), par.iloc[o,:].std()], axis=1)
	lt = '$\overshoot > %s$'%overshoot_threshold
	st = '$\overshoot < %s$'%overshoot_threshold
	stats.columns = ['%s (mean)'%st, '%s (mean)'%lt, '%s (std)'%st, '%s (std)'%lt]
	
	tex_index = utils.get_latex_par_names_from_list(stats.index.tolist())
	stats.index = tex_index
	print utils.prettify_table(stats.to_latex(float_format=lambda x: str(round(x,1))), 'LABEL', 'CAPTION')
	return stats

def issue_108(dataset, n_clusters, overshoot_threshold):
	from numpy import where, repeat, log
	from sklearn.cluster import KMeans
	from plotting import make_scatter_plot_for_labelled_data
	from data_analysis import calculate_stats_for_dataframe
	from sklearn.mixture import GMM

	folder = make_issue_specific_figure_folder('108 cluster after removing outliers', dataset)
	fit, par, gen, ids = IO.load_pickled_generation_dataframe(dataset)
	o = where(fit.overshoot > overshoot_threshold)[0]
	not_o = where(fit.overshoot <= overshoot_threshold)[0]
	data_to_plot = fit.iloc[not_o]
	colormap = brewer2mpl.get_map('Paired', 'Qualitative', n_clusters, reverse=True)

	def make_tables(name, clustering_method, cluster_labels):
		fit_inlier_stats = calculate_stats_for_dataframe(fit.iloc[not_o,:], cluster_labels)
		fit_outlier_stats = calculate_stats_for_dataframe(fit.iloc[o,:], repeat(0, len(o)))
		fit_mean_table = concat([fit_inlier_stats['Mean'], fit_outlier_stats['Mean']], axis=1)
		fit_mean_table.index = utils.get_latex_par_names_from_list(fit_mean_table.index)
		par_inlier_stats = calculate_stats_for_dataframe(par.iloc[not_o,:], cluster_labels)
		par_outlier_stats = calculate_stats_for_dataframe(par.iloc[o,:], repeat(0, len(o)))
		par_mean_table = concat([par_inlier_stats['Mean'], par_outlier_stats['Mean']], axis=1)
		par_mean_table.index = utils.get_latex_par_names_from_list(par_mean_table.index)
		tex = par_mean_table.to_latex(float_format=lambda x: str(round(x,1)))
		filename = folder + '%s_%s_%s.tex'%(clustering_method, n_clusters, name)
		tex = utils.prettify_table(fit_mean_table.to_latex(float_format=lambda x: str(round(x,1))), 'table:fit_gmm_'+name, 'gmm_'+name)
		tex += utils.prettify_table(par_mean_table.to_latex(float_format=lambda x: str(round(x,1))), 'table:par_kmeans_'+name, 'kmeans_'+name)

		with open(filename, 'w') as f:
			f.write(tex)

	def cluster_and_label(name, data_to_cluster, xname, yname, xfunc='dummy', yfunc='dummy'):
		km = KMeans(n_clusters=n_clusters)
		labels = km.fit_predict(data_to_cluster)
		filename = folder + 'kmeans_%s_%s.png'%(n_clusters, name)
		make_scatter_plot_for_labelled_data(data_to_plot, x_name=xname, y_name=yname, labels=labels, filename=filename, colormap=colormap, x_function = xfunc, y_function = yfunc, legend = True)
		make_tables(name, 'kmeans', labels)
		gmm = GMM(n_components = n_clusters, covariance_type = 'full')
		gmm.fit(data_to_cluster)
		labels = gmm.predict(data_to_cluster)
		filename = folder + 'gmm_%s_%s.png'%(n_clusters, name)
		make_scatter_plot_for_labelled_data(data_to_plot, x_name=xname, y_name=yname, labels=labels, filename=filename, colormap=colormap, x_function = 'log', y_function = 'log', legend = True)
		make_tables(name, 'gmm', labels)


	data_to_cluster = concat([log(fit['stdev']), log(fit['round_stable']), fit['time_to_reach_new_fundamental']], axis=1).iloc[not_o,:]
	cluster_and_label('logs_logr_t', data_to_cluster, 'stdev', 'round_stable', 'log', 'log')

	data_to_cluster = concat([log(fit['stdev']), log(fit['round_stable'])], axis=1).iloc[not_o,:]
	cluster_and_label('logs_logr', data_to_cluster, 'stdev', 'round_stable', 'log', 'log')

	data_to_cluster = concat([fit['round_stable'], fit['time_to_reach_new_fundamental'], fit['stdev']], axis=1).iloc[not_o,:]
	cluster_and_label('r_t_s', data_to_cluster, 'round_stable', 'time_to_reach_new_fundamental')

	data_to_cluster = concat([fit['round_stable'], fit['time_to_reach_new_fundamental']], axis=1).iloc[not_o,:]
	cluster_and_label('r_t', data_to_cluster, 'round_stable','time_to_reach_new_fundamental')
	#group_members = [where(labels == label)[0] for label in range(km.n_clusters)]
	
	#fit_mean_table.columns = ['C0', 'C1', 'C3', 'Outliers']
	return data_to_cluster

def plots_for_d3():
	issue_21_basic_scatter_plots(dataset='d3')
	issue_101_plot_pars_vs_fitness(dataset='d3')
	issue_103_manually_removing_large_fitness_points(dataset='d3', overshoot_threshold=10)

def plots_for_d9():
	issue_21_basic_scatter_plots(dataset='d9')
	issue_82_parameter_evolution(dataset='d9')
	issue_101_plot_pars_vs_fitness(dataset='d9')
	issue_103_manually_removing_large_fitness_points(dataset='d9', overshoot_threshold=10)
	issue_108(dataset='d9', n_clusters=3, overshoot_threshold=10)
	issue_108(dataset='d9', n_clusters=4, overshoot_threshold=10)
	issue_108(dataset='d9', n_clusters=8, overshoot_threshold=10)
	issue_108(dataset='d9', n_clusters=12, overshoot_threshold=10)

def plots_for_d10():
	issue_21_basic_scatter_plots(dataset='d10')
	issue_82_parameter_evolution(dataset='d10')
	issue_101_plot_pars_vs_fitness(dataset='d10')
	issue_103_manually_removing_large_fitness_points(dataset='d10', overshoot_threshold=10)
	issue_108(dataset='d10', n_clusters=3, overshoot_threshold=10)
	issue_108(dataset='d10', n_clusters=4, overshoot_threshold=10)
	issue_108(dataset='d10', n_clusters=8, overshoot_threshold=10)
	issue_108(dataset='d10', n_clusters=12, overshoot_threshold=10)

def plots_for_d11():
	issue_21_basic_scatter_plots(dataset='d11')
	issue_82_parameter_evolution(dataset='d11')
	issue_101_plot_pars_vs_fitness(dataset='d11')
	issue_103_manually_removing_large_fitness_points(dataset='d11', overshoot_threshold=10)
	issue_108(dataset='d11', n_clusters=3, overshoot_threshold=10)
	issue_108(dataset='d11', n_clusters=4, overshoot_threshold=10)
	issue_108(dataset='d11', n_clusters=8, overshoot_threshold=10)
	issue_108(dataset='d11', n_clusters=12, overshoot_threshold=10)

def remake_make_all_thesis_plots():
	plots_for_d3()
	plots_for_d9()
	plots_for_d10()
	plots_for_d11()
	
	

	


if __name__ == '__main__':
	#plot_issue_('d2', 4, True)
	#table_issue_55(dataset='d2', n_clusters=4, load_from_file=True)
	#plot_issue_29(dataset='d1', load_clusters_from_file=False)
	remake_make_all_thesis_plots()