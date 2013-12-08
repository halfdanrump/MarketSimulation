#import ppl
#import matplotlib.pyplot as plt
#import IO
#import numpy as np
#from numpy import log
import pandas
#from ppl import Ppl
import brewer2mpl
import IO

figure_save_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/Thesis/Figures/'

dataset_paths = {
	'd1':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/datasets/merged_data/d1_sameLatDist_ssmm40_sc100/'
}

def plot_issue_21(dataset = 'd1'):
	"""
	Makes scatter plots of fitness
	"""
	from plotting import make_color_grouped_scatter_plot
	datapath = dataset_paths[dataset]
	fit, gen = IO.load_pickled_generation_dataframe(datapath + 'fits.pandas')
	
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	print "Making scatter plots of fitness data for dataset %s"%dataset
	filename = figure_save_path + dataset + '_issue_21_a.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='overshoot', y_name='time_to_reach_new_fundamental', color_by='stdev', filename=filename, colormap = colormap, y_function='log')

	filename = figure_save_path + dataset + '_issue_21_b.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='overshoot', y_name='stdev', color_by='time_to_reach_new_fundamental', filename=filename, colormap = colormap)

	filename = figure_save_path + dataset + '_issue_21_c.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='time_to_reach_new_fundamental', y_name='round_stable', color_by='stdev', filename=filename, colormap = colormap)
	
	filename = figure_save_path + dataset + '_issue_21_d.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='stdev', y_name='round_stable', color_by='time_to_reach_new_fundamental', filename=filename, colormap = colormap, x_function='log', y_function='log')

	filename = figure_save_path + dataset + '_issue_21_e.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='stdev', y_name='time_to_reach_new_fundamental', color_by='round_stable', filename=filename, colormap = colormap, x_function='log', y_function='log')
	
	
def plot_issue_26(dataset = 'd1', make_plots = True):
	"""
	PCA and Kmeans for dataset 1
	"""
	from data_analysis import calculate_pca
	from sklearn.cluster import KMeans
	from plotting import make_color_grouped_scatter_plot, make_scatter_plot_for_labelled_data
	datapath = dataset_paths[dataset]
	#par = pandas.read_pickle(datapath + 'pars.pandas')
	fit_data, gen = IO.load_pickled_generation_dataframe(datapath + 'fits.pandas')
	t, pca, n_components  = calculate_pca(fit_data, n_components=3)
	

	columns = ['d1', 'd2', 'd3']
	df = pandas.DataFrame(t, columns=columns)

	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	filename = figure_save_path + dataset + '_issue_26_PCA_a_3components.png'
	
	if make_plots: 
		print "Making scatter plot of PCA decompositions of fitness data for dataset %s"%dataset
		make_color_grouped_scatter_plot(data_frame=df, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)

	n_clusters = 4
	kmeans = KMeans(n_clusters = n_clusters)
	kmeans.fit(t)
	
	colormap = brewer2mpl.get_map('Set2', 'Qualitative', n_clusters, reverse=True)
	filename = figure_save_path + dataset + '_issue_26_PCA_b_clusters.png'
	
	if make_plots: 
		print "Making scatter plot of K-means clusters of fitness data for dataset %s"%dataset
		make_scatter_plot_for_labelled_data(data_frame=df, x_name='d1', y_name='d2', labels=kmeans.labels_, filename=filename, colormap = colormap, legend=True)
		
	return kmeans, pca, fit_data
	

def plot_issue_29(load_clusters_from_file, dataset = 'd1'):
	from data_analysis import reduce_npoints_kmeans
	from data_analysis import calculate_pca
	from sklearn.cluster import AffinityPropagation
	from plotting import make_color_grouped_scatter_plot
	from plotting import make_scatter_plot_for_labelled_data
	from pandas import DataFrame
	"""
	Use KMeans to reduce number of datapoints and then use affinity propagation
	"""
	datapath = dataset_paths[dataset]
	fit, gen = IO.load_pickled_generation_dataframe(datapath + 'fits.pandas')

	points = reduce_npoints_kmeans(dataset, fit, n_datapoints=1000, load_from_file=load_clusters_from_file)
	trans, pca, components = calculate_pca(DataFrame(points), n_components=3)
	
	columns = ['d1', 'd2', 'd3']	
	df = pandas.DataFrame(trans, columns=columns)
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	filename = figure_save_path + dataset + '_issue_29_reduced_number_of_points.png'
	print "Making scatter plot of fitness data for dataset %s, where the number of points have been reduced by K-Means clustering"%dataset
	make_color_grouped_scatter_plot(data_frame=df, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)

	ap = AffinityPropagation(damping=0.97)
	ap.fit(trans)
	print "Making scatter plot of Affinity Propagation clusters of fitness data for dataset %s"%dataset
	filename = figure_save_path + dataset + '_issue_29_affinity.png'
	make_scatter_plot_for_labelled_data(data_frame=df, x_name='d1', y_name='d2', labels=ap.labels_, filename=filename, colormap = colormap, legend=True)

	#dbscan = DBSCAN(min_samples=100)

def plot_issue_32(dataset = 'd1'):
	from data_analysis import calculate_pca
	from plotting import make_color_grouped_scatter_plot, make_scatter_plot_for_labelled_data
	datapath = dataset_paths[dataset]
	par_data, gen = IO.load_pickled_generation_dataframe(datapath + 'pars.pandas')
	
	par_trans, pca, components = calculate_pca(par_data, n_components=3, whiten = True)
	
	columns = ['d1', 'd2', 'd3']
	df = pandas.DataFrame(par_trans, columns=columns)
	filename = figure_save_path + dataset + '_issue_32_pars_PCA.png'
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	print "Making scatter plot of PCA decomposition of parameter data for dataset %s"%dataset
	make_color_grouped_scatter_plot(df, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)
	
	kmeans, fit_pca, fit_data = plot_issue_26(make_plots=False)	
	
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

def remake_all_plots():
	#plot_issue_21(dataset='d1')
	#plot_issue_26(dataset='d1')
	#plot_issue_29(dataset='d1', load_clusters_from_file=True)
	plot_issue_32(dataset='d1')

if __name__ == '__main__':
	remake_all_plots()