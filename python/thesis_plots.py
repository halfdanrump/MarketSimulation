#import ppl
#import matplotlib.pyplot as plt
#import IO
#import numpy as np
#from numpy import log
import pandas
#from ppl import Ppl
import brewer2mpl

figure_save_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/Thesis/Figures/'


def plot_issue_21():
	"""
	Makes scatter plots of fitness for dataset 1
	"""
	from plotting import make_color_grouped_scatter_plot
	datapath = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/datasets/d1_sameLatDist_ssmm40_sc100/'
	fit = pandas.read_pickle(datapath + 'fits.pandas')
	
	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)

	filename = figure_save_path + 'issue_21_a.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='overshoot', y_name='time_to_reach_new_fundamental', color_by='stdev', filename=filename, colormap = colormap, y_function='log')

	filename = figure_save_path + 'issue_21_b.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='overshoot', y_name='stdev', color_by='time_to_reach_new_fundamental', filename=filename, colormap = colormap)

	filename = figure_save_path + 'issue_21_c.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='time_to_reach_new_fundamental', y_name='round_stable', color_by='stdev', filename=filename, colormap = colormap)
	
	filename = figure_save_path + 'issue_21_d.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='stdev', y_name='round_stable', color_by='time_to_reach_new_fundamental', filename=filename, colormap = colormap, x_function='log', y_function='log')

	filename = figure_save_path + 'issue_21_e.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='stdev', y_name='time_to_reach_new_fundamental', color_by='round_stable', filename=filename, colormap = colormap, x_function='log', y_function='log')
	
	
def plot_issue_26():
	"""
	PCA and Kmeans for dataset 1
	"""
	from sklearn.decomposition import PCA
	from sklearn.cluster import KMeans
	from plotting import make_color_grouped_scatter_plot
	datapath = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/datasets/d1_sameLatDist_ssmm40_sc100/'
	#par = pandas.read_pickle(datapath + 'pars.pandas')
	fit = pandas.read_pickle(datapath + 'fits.pandas')
	
	pca = PCA(n_components=3, copy=True, whiten=True)
	fit_data = fit.drop('gen', 1)
	t = pca.fit_transform(fit_data.values)
	columns = ['d1', 'd2', 'd3']
	df = pandas.DataFrame(t, columns=columns)

	colormap = brewer2mpl.get_map('RdBu', 'diverging', 4, reverse=True)
	filename = figure_save_path + 'issue_26_PCA_a_3components.png'
	make_color_grouped_scatter_plot(data_frame=df, x_name='d1', y_name='d2', color_by='d3', filename=filename, colormap=colormap)

	kmeans = KMeans(n_clusters = 3)
	kmeans.fit(t)
	df['labels'] = kmeans.labels_
	print len(df['d1'])
	print len(kmeans.labels_)
	colormap = brewer2mpl.get_map('Set2', 'Qualitative', 3, reverse=True)
	filename = figure_save_path + 'issue_26_PCA_b_clusters.png'
	make_color_grouped_scatter_plot(data_frame=df, x_name='d1', y_name='d2', color_by='labels', filename=filename, colormap = colormap)

def remake_all_plots():
	plot_issue_21()
	plot_issue_26()


if __name__ == '__main__':
	plot_issue_26()