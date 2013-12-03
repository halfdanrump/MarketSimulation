import ppl
import matplotlib.pyplot as plt
import IO
import numpy as np
from numpy import log
import pandas

figure_save_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/Thesis/Figures/'


def plot_issue_21():
	"""
	Makes scatter plots for dataset 1
	"""
	from plotting import make_color_grouped_scatter_plot
	datapath = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/datasets/d1_sameLatDist_ssmm40_sc100/'
	par = pandas.read_pickle(datapath + 'pars.pandas')
	fit = pandas.read_pickle(datapath + 'fits.pandas')
	
	filename = figure_save_path + 'issue_21_a.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='overshoot', y_name='time_to_reach_new_fundamental', color_by='stdev', filename=filename, y_function='log', color_function='sqrt')

	filename = figure_save_path + 'issue_21_b.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='overshoot', y_name='stdev', color_by='time_to_reach_new_fundamental', filename=filename)

	filename = figure_save_path + 'issue_21_c.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='time_to_reach_new_fundamental', y_name='round_stable', color_by='stdev', filename=filename)
	
	filename = figure_save_path + 'issue_21_d.png'
	make_color_grouped_scatter_plot(data_frame=fit, x_name='stdev', y_name='round_stable', color_by='time_to_reach_new_fundamental', filename=filename, x_function='log', y_function='log')
	
	# Show the whole color range
	
	#x = np.log(fit['stdev'])
	#y = fit['round_stable']
	#ppl.scatter(ax, x, y)

	#ppl.legend(ax)
	#ax.set_title('prettyplotlib `scatter` example\nshowing default color cycle and scatter params')
	"""
	fig.savefig(figure_save_path + 'issue_21_a.png')

	fig, ax = plt.subplots(1)
	x = np.log(fit['time_to_reach_new_fundamental'])
	y = fit['round_stable']
	ppl.scatter(ax, x, y)
	fig.savefig(figure_save_path + 'issue_21_c.png')


	fig, ax = plt.subplots(1)
	ax.set_xlim([min(fit.overshoot), max(fit.overshoot)])
	ax.set_ylim([min(fit.stdev), max(fit.stdev)])
	n_intervals = 4
	bins = np.linspace(fit['time_to_reach_new_fundamental'].min(), fit['time_to_reach_new_fundamental'].max(), n_intervals + 1)
	fit['stdgroups'] = pandas.cut(fit.time_to_reach_new_fundamental, bins=bins, labels = False)
	groups = pandas.cut(fit.time_to_reach_new_fundamental, bins=bins)
	print groups.levels
	for g in range(n_intervals):
		print g, groups.levels[g]
		x = fit[fit.stdgroups == g]['overshoot']
		y = fit[fit.stdgroups == g]['stdev']
		ppl.scatter(ax, x, y, label=str(groups.levels[g]))

	fig.savefig(figure_save_path + 'issue_21_d.png')



	fig, ax = plt.subplots(1)
	#ax.set_autoscale_on(False)
	ax.set_xlim([min(fit.time_to_reach_new_fundamental), max(fit.time_to_reach_new_fundamental)])
	ax.set_ylim([log(min(fit.round_stable)), log(max(fit.round_stable))])
	# Show the whole color range
	n_intervals = 4
	bins = np.linspace(log(fit['stdev'].min()), log(fit['stdev'].max()), n_intervals + 1)
	fit['stdgroups'] = pandas.cut(fit.stdev, bins=bins, labels = False)
	groups = pandas.cut(fit.stdev.apply(log), bins=bins)
	print groups.levels
	for g in range(n_intervals):
		print g, groups.levels[g]
		x = fit[fit.stdgroups == g]['time_to_reach_new_fundamental']
		y = log(fit[fit.stdgroups == g]['round_stable'])
		ppl.scatter(ax, x, y, label=str(groups.levels[g]))

	ppl.legend(ax)
	#ax.set_title('prettyplotlib `scatter` example\nshowing default color cycle and scatter params')
	fig.savefig(figure_save_path + 'issue_21_b.png')
	"""
if __name__ == '__main__':
	plot_issue_21()