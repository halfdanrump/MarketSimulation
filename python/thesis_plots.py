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
figure_save_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/Thesis/Figures/'
table_save_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/Thesis/Tables/'


def plot_issue_21(dataset):
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
	
	
def plot_issue_26(dataset, make_plots = True):
	"""
	PCA and Kmeans for dataset 1
	"""
	from data_analysis import calculate_pca
	from sklearn.cluster import KMeans
	from plotting import make_color_grouped_scatter_plot, make_scatter_plot_for_labelled_data
	
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	transformed_data, pca, n_components  = calculate_pca(fit_data, n_components=3)

	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	filename = figure_save_path + dataset + '_issue_26_fitness_PCA_a_3components.png'
	
	if make_plots: 
		print "Making scatter plot of PCA decompositions of fitness data for dataset %s"%dataset
		make_color_grouped_scatter_plot(data_frame=transformed_data, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)

	n_clusters = 4
	kmeans = KMeans(n_clusters = n_clusters)
	kmeans.fit(transformed_data.values)
	
	colormap = brewer2mpl.get_map('Set2', 'Qualitative', n_clusters, reverse=True)
	filename = figure_save_path + dataset + '_issue_26_fitness_PCA_b_clusters.png'
	
	if make_plots: 
		print "Making scatter plot of K-means clusters of fitness data for dataset %s"%dataset
		make_scatter_plot_for_labelled_data(data_frame=transformed_data, x_name='d1', y_name='d2', labels=kmeans.labels_, filename=filename, colormap = colormap, legend=True)
		
	return kmeans, pca, fit_data
	

def plot_issue_29(dataset, load_clusters_from_file = False):
	from data_analysis import reduce_npoints_kmeans
	from data_analysis import calculate_pca
	from sklearn.cluster import AffinityPropagation
	from plotting import make_color_grouped_scatter_plot
	from plotting import make_scatter_plot_for_labelled_data

	"""
	Use KMeans on fitness data to reduce number of datapoints and then use affinity propagation
	"""
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)

	#trans_full_dataset, pca, components = calculate_pca(fit_data, n_components=3)
	points, labels, km = reduce_npoints_kmeans(dataframe = fit_data, dataset_name = dataset, n_datapoints = 1000, load_from_file = False)
	
	print points
	
	transformed_data, pca, components = calculate_pca(points, n_components=3)
	
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	filename = figure_save_path + dataset + '_issue_29_fitness_reduced_number_of_points.png'
	print "Making scatter plot of fitness data for dataset %s, where the number of points have been reduced by K-Means clustering"%dataset
	make_color_grouped_scatter_plot(data_frame=transformed_data, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)

	ap = AffinityPropagation(damping=0.97)
	ap.fit(transformed_data.values)
	print "Making scatter plot of Affinity Propagation clusters of fitness data for dataset %s"%dataset
	filename = figure_save_path + dataset + '_issue_29_fitness_affinity.png'
	make_scatter_plot_for_labelled_data(data_frame=transformed_data, x_name='d1', y_name='d2', labels=ap.labels_, filename=filename, colormap = colormap, legend=True)

	#dbscan = DBSCAN(min_samples=100)

def plot_issue_32(dataset):
	from data_analysis import calculate_pca
	from plotting import make_color_grouped_scatter_plot, make_scatter_plot_for_labelled_data
	
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	
	
	par_trans, pca, components = calculate_pca(par_data, n_components=3, whiten = True)
	columns = ['d%s'%i for i in range(1, 3+1)]
	
	df = DataFrame(par_trans, columns=columns)
	filename = figure_save_path + dataset + '_issue_32_parameters_PCA.png'
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	print "Making scatter plot of PCA decomposition of parameter data for dataset %s"%dataset
	make_color_grouped_scatter_plot(df, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)
	
	kmeans, fit_pca, fit_data = plot_issue_26(dataset = dataset, make_plots=False)	
	
	filename = figure_save_path + dataset + '_issue_32_parameters_kmeans_labelled.png'
	print "Making scatter plot of K-means clusters of parameter data for dataset %s"%dataset
	make_scatter_plot_for_labelled_data(data_frame=df, x_name='d1', y_name='d2', labels=kmeans.labels_, filename=filename, colormap = colormap, legend=True)


	
	

def plot_issue_34(dataset):
	from matplotlib.pyplot import imshow
	datapath = dataset_paths[dataset]
	par = read_pickle(datapath + 'pars.pandas')
	par = par.drop('gen', 1)
	fit = read_pickle(datapath + 'fits.pandas')
	fit = fit.drop('gen', 1)
	imshow

def plot_issue_43(dataset, n_clusters, gamma, load_from_file = False):
	from plotting import make_color_grouped_scatter_plot, make_scatter_plot_for_labelled_data
	from data_analysis import reduce_npoints_kmeans, outlier_detection_with_SVM, calculate_pca
	from sklearn.cluster import KMeans	
	from utils import get_group_vector_for_reduced_dataset
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)


	reduced_par, labels_all_datapoints, km = reduce_npoints_kmeans(par_data, dataset, n_datapoints=1000, load_from_file=load_from_file)
	
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
	
	
	
	

def table_issue_55(dataset, n_clusters, gamma, load_from_file = False):
	from data_analysis import reduce_npoints_kmeans, outlier_detection_with_SVM, calculate_stats_for_dataframe
	from sklearn.cluster import KMeans
	from utils import get_group_vector_for_reduced_dataset, prettify_table
	from scipy.stats import f_oneway
	from numpy import where

	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)	
	par_r, cluster_assignment_o2r, km_r = reduce_npoints_kmeans(par_data, dataset, n_datapoints=1000, load_from_file=load_from_file)
	
	

	inliers_idx_r, outliers_idx_r = outlier_detection_with_SVM(par_r, kernel='rbf', gamma=gamma, outlier_percentage=0.01)
	
	kmeans = KMeans(n_clusters = n_clusters)
	kmeans.fit(par_r.iloc[inliers_idx_r, :])

	indexes_o, labels_o =  get_group_vector_for_reduced_dataset(inliers_idx_r, cluster_assignment_o2r, cluster_assignment_r2g = kmeans.labels_)
	#inliers_idx_o = get_labels_r2o(cluster_assignment_o2r, inliers_idx_r)
	all_data = concat([par_data, fit_data], axis=1)
	stats = calculate_stats_for_dataframe(all_data.iloc[indexes_o,:], labels_o)
	
	for stat_name, table in stats.items():
		table.index = ['\%s'%g.replace('_', '') for g in table.index.tolist()]
		tex = table.to_latex(float_format=lambda x: str(round(x,1)))
		tex = prettify_table(tex, 'issue_65_%s'%stat_name, 's')
		with open('%s%s.tex'%(table_save_path, stat_name), 'w') as f:
			f.write(tex)

	fitness_groups = map(lambda x: fit_data.iloc[indexes_o[where(labels_o==x)]], range(n_clusters))
	fval, pval = f_oneway(*fitness_groups)
	

	return stats, pval, kmeans
	

	#stats = calculate_stats_for_dataframe(inliers, kmeans.labels_)
	#return stats, inliers_idx, r_label
	
def issue_65_run_sim_for_clusters(dataset, n_clusters, load_from_file = False):
	from settings import get_fixed_parameters
	from fitness import evaluate_simulation_results
	import settings
	import os
	settings.PLOT_SAVE_PROB = 1
	
	fit, par, gen = IO.load_pickled_generation_dataframe(dataset)
	stats, pvals, kmeans = table_issue_55(dataset, n_clusters, load_from_file)
	graph_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/data_for_figures/issue_65/'
	
	for c, cluster in enumerate(kmeans.cluster_centers_):
		parameters = get_fixed_parameters()
		
		parameters.update(dict(zip(par.columns, map(int, cluster))))
		print parameters
		#plot_name = '%scluster%s'%(graph_folder, c)
		folder = '%scluster_%s/'%(graph_folder,c)
		if not os.path.exists(folder): os.makedirs(folder)
		data = evaluate_simulation_results(folder, 0, parameters, range(4), autorun=True)
		
			





def run_all_issues():
	plot_issue_21(dataset='d1')
	plot_issue_26(dataset='d1')
	plot_issue_29(dataset='d1', load_clusters_from_file=False)
	plot_issue_32(dataset='d1')

	plot_issue_21(dataset='d2')
	plot_issue_26(dataset='d2')
	plot_issue_29(dataset='d2')
	plot_issue_32(dataset='d2')
	plot_issue_43(dataset='d2', n_clusters=4)
	table_issue_55(dataset='d2', n_clusters=4)

	plot_issue_21(dataset='d3')
	plot_issue_26(dataset='d3')
	plot_issue_29(dataset='d3')
	plot_issue_32(dataset='d3')
	plot_issue_43(dataset='d3', n_clusters=4)
	table_issue_55(dataset='d3', n_clusters=4)

if __name__ == '__main__':
	#plot_issue_('d2', 4, True)
	#table_issue_55(dataset='d2', n_clusters=4, load_from_file=True)
	#plot_issue_29(dataset='d1', load_clusters_from_file=False)