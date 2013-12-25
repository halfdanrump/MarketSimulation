#import ppl
#import matplotlib.pyplot as plt
#import IO
#import numpy as np
#from numpy import log
from pandas import DataFrame, read_pickle, concat
#from ppl import Ppl
import brewer2mpl
import IO
from data_analysis import dataset_paths
figure_save_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/tex/Figures/'
table_save_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/tex/Tables/'


def issue_21_basic_scatter_plots(dataset):
	"""
	Makes scatter plots of fitness
	"""
	from plotting import make_color_grouped_scatter_plot
	
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	print "Making scatter plots of fitness data for dataset %s"%dataset
	filename = figure_save_path + dataset + '_issue_21_fitness_scatter_a.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='overshoot', y_name='time_to_reach_new_fundamental', color_by='stdev', filename=filename, colormap = colormap, y_function='log')

	filename = figure_save_path + dataset + '_issue_21_fitness_scatter_b.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='overshoot', y_name='stdev', color_by='time_to_reach_new_fundamental', filename=filename, colormap = colormap)

	filename = figure_save_path + dataset + '_issue_21_fitness_scatter_c.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='time_to_reach_new_fundamental', y_name='round_stable', color_by='stdev', filename=filename, colormap = colormap)
	
	filename = figure_save_path + dataset + '_issue_21_fitness_scatter_d.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='stdev', y_name='round_stable', color_by='time_to_reach_new_fundamental', filename=filename, colormap = colormap, x_function='log', y_function='log')

	filename = figure_save_path + dataset + '_issue_21_fitness_scatter_e.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='stdev', y_name='time_to_reach_new_fundamental', color_by='round_stable', filename=filename, colormap = colormap, x_function='log', y_function='log')
	
	
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
	
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)
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
	
		

	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	do_issue(fit_data, 'fitness')
	do_issue(par_data, 'parameter')

	
def issue_88_affinity_after_norm_and_outlier(dataset, load_from_file):
	from sklearn.preprocessing import scale
	from sklearn.cluster import AffinityPropagation
	from data_analysis import reduce_npoints_kmeans, outlier_detection_with_SVM
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)
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
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)


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
		
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	reduce_outlier_cluster_stats(par_data, fit_data, 'parameter', gamma=gamma[0])
	reduce_outlier_cluster_stats(fit_data, par_data, 'fitness', gamma=gamma[1])
	"""
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)	
	par_r, cluster_assignment_o2r, km_r = reduce_npoints_kmeans(par_data, dataset, n_datapoints=1000, load_from_file=load_from_file)
	
	inliers_idx_r, outliers_idx_r = outlier_detection_with_SVM(par_r, kernel='rbf', gamma=gamma, outlier_percentage=0.01)
	
	kmeans = KMeans(n_clusters = n_clusters)
	kmeans.fit(par_r.iloc[inliers_idx_r, :])

	indexes_i, labels_i =  get_group_vector_for_reduced_dataset(inliers_idx_r, cluster_assignment_o2r, cluster_assignment_r2g = kmeans.labels_)
	#inliers_idx_o = get_labels_r2o(cluster_assignment_o2r, inliers_idx_r)
	all_data = concat([par_data, fit_data], axis=1)
	stats = calculate_stats_for_dataframe(all_data.iloc[indexes_i,:], labels_i)
	
	export_stats_dict_as_tex(dataset, stats)
	
	fitness_groups = map(lambda x: fit_data.iloc[indexes_i[where(labels_i==x)]], range(n_clusters))
	fval, pval = f_oneway(*fitness_groups)
	colormap = brewer2mpl.get_map('Set2', 'Qualitative', n_clusters, reverse=True)
	filename = figure_save_path + dataset + '_issue_55_1_%s_clusters_in_PCA_space.png'%data_name
	print "Making scatter plot of K-means clusters of %s data for dataset %s"%(data_name, dataset)
	make_scatter_plot_for_labelled_data(data_frame=transformed_data, x_name='d1', y_name='d2', labels=kmeans.labels_, filename=filename, colormap = colormap, legend=True)
	return stats, pval, kmeans
	"""

	#stats = calculate_stats_for_dataframe(inliers, kmeans.labels_)
	#return stats, inliers_idx, r_label
	
def issue_65_run_sim_for_clusters(dataset, n_clusters, load_from_file = False):
	from settings import get_fixed_parameters
	from fitness import evaluate_simulation_results
	import settings
	import os
	settings.PLOT_SAVE_PROB = 1
	
	fit, par, gen = IO.load_pickled_generation_dataframe(dataset)
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
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)


	reduced_par, labels_all_datapoints, km = reduce_npoints_kmeans(par_data, dataset, n_datapoints=1000, load_from_file=load_from_file)	
	return reduced_par, labels_all_datapoints, km


def issue_82_parameter_evolution(dataset):
	def get_stats(name, stats):
		return [getattr(group[name], s)() for s in stats]	
	
	def d9():
		make_pretty_generation_plot(folder + 'd9_latpars_s.png', generations, [group['ssmm_latency_s'].mean(), group['sc_latency_s'].mean()], 'Average latency std', ['Market makers', 'Chartists'])
		make_pretty_generation_plot(folder + 'd9_latpars_mu.png', generations, [group['ssmm_latency_mu'].mean(), group['sc_latency_mu'].mean()], 'Average latency mean', ['Market makers', 'Chartists'])
		make_pretty_generation_plot(folder + 'd9_thinkpars_s.png', generations, [group['ssmm_think_s'].mean(), group['sc_think_s'].mean()], 'Average think time std', ['Market makers', 'Chartists'])
		make_pretty_generation_plot(folder + 'd9_thinkpars_mu.png', generations, [group['ssmm_think_mu'].mean(), group['sc_think_mu'].mean()], 'Average think time mean', ['Market makers', 'Chartists'])

	def d3():
		#make_pretty_generation_plot(folder + 'd3_latpars_s.png', generations, [group['ssmm_latency_s'].mean(), group['sc_latency_s'].mean()], 'Average latency std', ['Market makers', 'Chartists'])
		make_pretty_generation_plot(folder + 'd3_nAgents.png', generations, [group['ssmm_nAgents'].mean(), group['sc_nAgents'].mean()], 'Average nuber of agents', ['Market makers', 'Chartists'])
		make_pretty_generation_plot(folder + 'd9_thinkpars_s.png', generations, [group['ssmm_think_s'].mean(), group['sc_think_s'].mean()], 'Average think time std', ['Market makers', 'Chartists'])
		make_pretty_generation_plot(folder + 'd9_thinkpars_mu.png', generations, [group['ssmm_think_mu'].mean(), group['sc_think_mu'].mean()], 'Average think time mean', ['Market makers', 'Chartists'])
	from plotting import make_pretty_generation_plot
	folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Thesis/data_for_figures/issue_82_generation_plots/'
	fit,par,gen = IO.load_pickled_generation_dataframe(dataset)
	all_data = concat([fit,par, DataFrame(gen)], axis=1)
	generations = list(set(all_data['gen']))
	group = all_data.groupby('gen')
	stats = ['min', 'mean', 'median']
	
	make_pretty_generation_plot(folder + dataset + '_time_to_reach_new_fundamental.png', generations, get_stats('time_to_reach_new_fundamental', stats), 'Time to reach fundamental after shock', stats)
	make_pretty_generation_plot(folder + dataset + '_stdev.png', generations, get_stats('stdev', stats), 'Standard deviation of trade prices entering stability margin', stats)
	make_pretty_generation_plot(folder + dataset + '_round_stable.png', generations, get_stats('round_stable', stats), 'Round entering stability margin', stats)
	make_pretty_generation_plot(folder + dataset + '_overshoot.png', generations, get_stats('overshoot', stats), 'Overshoot', stats)
	eval(dataset)()	
	

def analyze_d1():
	issue_21_basic_scatter_plots(dataset='d1')
	issue_26_plot_pca(dataset='d1', n_clusters=4)
	issue_29_reduce_and_affinity(dataset='d1', affinity_damping=0.97, load_clusters_from_file=True)

def analyse_d2():
	issue_21_basic_scatter_plots(dataset='d2')
	issue_26_plot_pca(dataset='d2', n_clusters=4)
	
	plot_issue_29(dataset='d2')
	plot_issue_32(dataset='d2')
	plot_issue_43(dataset='d2', n_clusters=4)
	table_issue_55(dataset='d2', n_clusters=4)

def analyse_d3():
	plot_issue_21(dataset='d3')
	plot_issue_26(dataset='d3', n_clusters=4)
	plot_issue_29(dataset='d3')
	plot_issue_32(dataset='d3')
	plot_issue_43(dataset='d3', n_clusters=4)
	table_issue_55(dataset='d3', n_clusters=4)


	

	

	

if __name__ == '__main__':
	#plot_issue_('d2', 4, True)
	#table_issue_55(dataset='d2', n_clusters=4, load_from_file=True)
	#plot_issue_29(dataset='d1', load_clusters_from_file=False)
	pass