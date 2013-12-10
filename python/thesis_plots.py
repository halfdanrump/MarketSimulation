#import ppl
#import matplotlib.pyplot as plt
#import IO
#import numpy as np
#from numpy import log
import pandas
from pandas import DataFrame
#from ppl import Ppl
import brewer2mpl
import IO
from data_analysis import dataset_paths
figure_save_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/Thesis/Figures/'



def plot_issue_21(dataset):
	"""
	Makes scatter plots of fitness
	"""
	from plotting import make_color_grouped_scatter_plot
	
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	print "Making scatter plots of fitness data for dataset %s"%dataset
	filename = figure_save_path + dataset + '_issue_21_a.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='overshoot', y_name='time_to_reach_new_fundamental', color_by='stdev', filename=filename, colormap = colormap, y_function='log')

	filename = figure_save_path + dataset + '_issue_21_b.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='overshoot', y_name='stdev', color_by='time_to_reach_new_fundamental', filename=filename, colormap = colormap)

	filename = figure_save_path + dataset + '_issue_21_c.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='time_to_reach_new_fundamental', y_name='round_stable', color_by='stdev', filename=filename, colormap = colormap)
	
	filename = figure_save_path + dataset + '_issue_21_d.png'
	make_color_grouped_scatter_plot(data_frame=fit_data, x_name='stdev', y_name='round_stable', color_by='time_to_reach_new_fundamental', filename=filename, colormap = colormap, x_function='log', y_function='log')

	filename = figure_save_path + dataset + '_issue_21_e.png'
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
	filename = figure_save_path + dataset + '_issue_26_PCA_a_3components.png'
	
	if make_plots: 
		print "Making scatter plot of PCA decompositions of fitness data for dataset %s"%dataset
		make_color_grouped_scatter_plot(data_frame=transformed_data, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)

	n_clusters = 4
	kmeans = KMeans(n_clusters = n_clusters)
	kmeans.fit(transformed_data.values)
	
	colormap = brewer2mpl.get_map('Set2', 'Qualitative', n_clusters, reverse=True)
	filename = figure_save_path + dataset + '_issue_26_PCA_b_clusters.png'
	
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
	points, labels = reduce_npoints_kmeans(dataframe = fit_data, dataset_name = dataset, n_datapoints = 1000, load_from_file = False)
	
	print points
	
	transformed_data, pca, components = calculate_pca(points, n_components=3)
	
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	filename = figure_save_path + dataset + '_issue_29_reduced_number_of_points.png'
	print "Making scatter plot of fitness data for dataset %s, where the number of points have been reduced by K-Means clustering"%dataset
	make_color_grouped_scatter_plot(data_frame=transformed_data, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)

	ap = AffinityPropagation(damping=0.97)
	ap.fit(transformed_data.values)
	print "Making scatter plot of Affinity Propagation clusters of fitness data for dataset %s"%dataset
	filename = figure_save_path + dataset + '_issue_29_affinity.png'
	make_scatter_plot_for_labelled_data(data_frame=transformed_data, x_name='d1', y_name='d2', labels=ap.labels_, filename=filename, colormap = colormap, legend=True)

	#dbscan = DBSCAN(min_samples=100)

def plot_issue_32(dataset = 'd1'):
	from data_analysis import calculate_pca
	from plotting import make_color_grouped_scatter_plot, make_scatter_plot_for_labelled_data
	
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)
	
	
	par_trans, pca, components = calculate_pca(par_data, n_components=3, whiten = True)
	columns = ['d%s'%i for i in range(1, 3+1)]
	
	df = pandas.DataFrame(par_trans, columns=columns)
	filename = figure_save_path + dataset + '_issue_32_pars_PCA.png'
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	print "Making scatter plot of PCA decomposition of parameter data for dataset %s"%dataset
	make_color_grouped_scatter_plot(df, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)
	
	kmeans, fit_pca, fit_data = plot_issue_26(dataset = dataset, make_plots=False)	
	
	filename = figure_save_path + dataset + '_issue_32_pars_labelled.png'
	print "Making scatter plot of K-means clusters of parameter data for dataset %s"%dataset
	make_scatter_plot_for_labelled_data(data_frame=df, x_name='d1', y_name='d2', labels=kmeans.labels_, filename=filename, colormap = colormap, legend=True)


	
	

def plot_issue_34(dataset = 'd1'):
	from matplotlib.pyplot import imshow
	datapath = dataset_paths[dataset]
	par = pandas.read_pickle(datapath + 'pars.pandas')
	par = par.drop('gen', 1)
	fit = pandas.read_pickle(datapath + 'fits.pandas')
	fit = fit.drop('gen', 1)
	imshow

def plot_issue_43():
	
	from data_analysis import reduce_npoints_kmeans, calculate_pca, outlier_detection_with_SVM, calculate_cluster_stats, calculate_cluster_stats_for_reduced_dataset
	from plotting import make_color_grouped_scatter_plot, make_scatter_plot_for_labelled_data
	from sklearn.cluster import KMeans, AffinityPropagation
	dataset = 'd2'
	fit_data, par_data, gen = IO.load_pickled_generation_dataframe(dataset_name=dataset)

	reduced_par, labels_all_datapoints = reduce_npoints_kmeans(par_data, 'd2', n_datapoints=1000, load_from_file=False)
	
	inliers, outliers, inliers_idx, outliers_idx = outlier_detection_with_SVM(reduced_par, kernel='rbf', gamma=0.1, outlier_percentage=0.01)
	transformed_data, pca, components = calculate_pca(inliers, n_components = 3, whiten=False, normalize=True)
	
	filename = figure_save_path + dataset + '_issue_43_PCA_after_outlier_detection.png'
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	make_color_grouped_scatter_plot(transformed_data, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)

	
	n_clusters = 4
	kmeans = KMeans(n_clusters = n_clusters)
	kmeans.fit(transformed_data.values)
	#calculate_cluster_stats(dataframe=fit_data, cluster_labels=kmeans.labels_)
	print "Making scatter plot of Affinity Propagation clusters of fitness data for dataset %s"%dataset
	filename = figure_save_path + dataset + '_issue_43_kmeans_after_outlier_and_PCA.png'
	colormap = brewer2mpl.get_map('Set2', 'Qualitative', n_clusters, reverse=True)
	make_scatter_plot_for_labelled_data(data_frame=transformed_data, x_name='d1', y_name='d2', labels=kmeans.labels_, filename=filename, colormap = colormap, legend=True)
	#return fit_data, labels_all_datapoints, kmeans.labels_, inliers, outliers, inliers_idx, outliers_idx
	stats = calculate_cluster_stats_for_reduced_dataset(dataframe=fit_data, inlier_clusters = inliers_idx, labels_reduced=kmeans.labels_, labels_full=labels_all_datapoints)
	print stats


	

def remake_all_plots():
	plot_issue_21(dataset='d1')
	plot_issue_26(dataset='d1')
	plot_issue_29(dataset='d1', load_clusters_from_file=False)
	plot_issue_32(dataset='d1')

	plot_issue_21(dataset='d2')
	plot_issue_26(dataset='d2')
	plot_issue_29(dataset='d2')
	plot_issue_32(dataset='d2')
	plot_issue_43()

if __name__ == '__main__':
	plot_issue_43()
	#plot_issue_29(dataset='d1', load_clusters_from_file=False)