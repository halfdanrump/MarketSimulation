import numpy as np
import IO
from simulation_interface import run_simulation
#from settings import data_to_calculate, simulation_parameters
from settings import stability_margin, data_types, KEEP_SIMULATION_DATA, MAKE_TRADEPRICE_PLOT
from random import randint
from plotting import make_tradeprice_plot
from itertools import groupby
#from collections import Iterable

def get_named_stats(data, attribute_names = list()):
	mean = dict()
	for attr in attribute_names:
		mean.update({attr: np.mean(data[attr])})

	std = dict()
	for attr in attribute_names:
		std.update({attr: np.std(data[attr])})    

	return {'mean':mean, 'std':std}


def evaluate_simulation_results(graph_folder, parameters = {}, reps = [0], autorun = False):
	assert parameters, "Please specify a dictionary with par_name:par_value as key:value sets"

	data = empty_data_matrix(len(reps))

	simulation_reps_to_run = []
	random_path = str(randint(0, 2**64))
	log_folders = IO.get_logfolders(parameters, reps, random_path)


	for r in reps:
		try:
			assert IO.check_simulation_complete(log_folders[r]), "Could not calculate data because simulation in folder %s was not finished"%log_folders[r]
		except AssertionError:
			simulation_reps_to_run.append(r)

	if simulation_reps_to_run: run_simulation(parameters, simulation_reps_to_run, random_path)

	for r in reps:
		data[r] = __evaluate_simulation_results(parameters, log_folders[r], graph_folder)
	return data



def __evaluate_simulation_results(parameters, logdata_folder, graph_folder):
	assert logdata_folder, "Please specify where the logdata is located"

	data = empty_data_matrix(1)

	#ob_round_based = np.genfromtxt(logdata_folder + 'columnLog_roundBased_orderbook(0,0).csv', names=True, dtype=int, delimiter=',', usecols=(1,2))
	stock_round_based = np.genfromtxt(logdata_folder + 'columnLog_roundBased_stock0.csv', names=True, dtype=int, delimiter=',', usecols=(0,1))
	trades = np.genfromtxt(logdata_folder + 'columnLog_transactionBased_stock0.csv', names=True, dtype=int, delimiter=',', usecols=(0,1))

	fundamental_step_round = np.where(np.diff(stock_round_based['fundamental'], n=1) != 0)[0]
	fundamental_after_step = stock_round_based['fundamental'][fundamental_step_round + 1]

	
	### Collect data
	"""
	try:
		data['buy_catchup_round'] = np.min(np.where(ob_round_based['bestStandingBuyPrice'] == fundamental_after_step))
	except ValueError:
		data['buy_catchup_round'] = settings.data_for_failed_simulation['buy_catchup_round']
		
	try:
		data['sell_catchup_round'] = np.min(np.where(ob_round_based['bestStandingSellPrice'] == fundamental_after_step))
	except ValueError:
		data['sell_catchup_round'] = settings.data_for_failed_simulation['sell_catchup_round']
		
	"""
	"""
	if buy_catchup_round and sell_catchup_round:
	 	catchup_round = max(buy_catchup_round, sell_catchup_round)
		try:
			trade_index = np.min(np.where(trades['round'] >= catchup_round))
		 	data['traded_price_std_after_sellbuy_reach_new_fundamental'] = np.std(trades['price'][trade_index::])
		 	data['traded_price_mean_after_sellbuy_reach_new_fundamental'] = np.mean(trades['price'][trade_index::])
		 	data['traded_price_median_after_sellbuy_reach_new_fundamental'] = np.median(trades['price'][trade_index::])
	 	except ValueError:
			data['traded_price_std_after_sellbuy_reach_new_fundamental'] = settings.data_for_failed_simulation['traded_price_std_after_sellbuy_reach_new_fundamental']
		 	data['traded_price_mean_after_sellbuy_reach_new_fundamental'] = settings.data_for_failed_simulation['traded_price_mean_after_sellbuy_reach_new_fundamental']
		 	data['traded_price_median_after_sellbuy_reach_new_fundamental'] = settings.data_for_failed_simulation['traded_price_median_after_sellbuy_reach_new_fundamental']

	try:
		i = np.min(np.where(trades['round'] > fundamental_step_round))
		data['max_traded_price_after_step'] = np.max(trades['price'][i::])
		data['min_traded_price_after_step'] = np.min(trades['price'][i::])
	except ValueError:
		data['max_traded_price_after_step'] = settings.data_for_failed_simulation['max_traded_price_after_step']
		data['min_traded_price_after_step'] = settings.data_for_failed_simulation['min_traded_price_after_step']
	"""
	"""
	try:
		within_margin = np.where(
							(trades['price'] >= fundamental_after_step - settings.stability_margin) & 
							(trades['price'] <= fundamental_after_step + settings.stability_margin))[0]

		diffs = np.diff(stable_idx)
		data['tp_stable_round'] = trades['round'][stable_idx[np.max(np.where(diffs > 1))]]
	except ValueError:
		data['tp_stable_round'] = settings.data_for_failed_simulation['tp_stable_round']
	
	


	within_margin = np.where((trades['price'] >= fundamental_after_step - settings.stability_margin)
							&(trades['price'] <= fundamental_after_step + settings.stability_margin))[0]

	diffs = np.diff(stable_idx)
	"""
	within_margin = get_number_of_rounds_within_stability_margin(trades['price'], trades['rounds'], fundamental_after_step)
	data['n_simulation_rounds_within_stability_margin'] = within_margin['total_number_of_rounds']
	data['n_seperate_intervals_within_stability_margin'] = within_margin['n_intervals']
	if not KEEP_SIMULATION_DATA: IO.delete_simulation_data(logdata_folder)
	if MAKE_TRADEPRICE_PLOT: make_tradeprice_plot(trades['round'], trades['price'], parameters, graph_folder, data)
	return data


"""
def get_data_for_single_parameter_sweep(parameter_to_sweep = "", parameter_range = list(), all_parameters = dict(), reps = list()):
		try:
				del all_parameters[parameter_to_sweep]
		except KeyError:
				pass

		for par, r in all_parameters.items():
				assert not isinstance(r, Iterable), "Please specify a single value for all parameters except the parameter to sweep: %s"%par

		#parameters_to_store = [parameter_to_sweep]
		#data_type = [(parameter_to_sweep, int)] + [(d, float) for d in data_to_calculate]
		
		all_data = np.zeros(shape=(len(parameter_range)), dtype = settings.data_type)

		for i, parameter in enumerate(parameter_range):
				parameters_to_store = {parameter_to_sweep:parameter}
				all_parameters[parameter_to_sweep] = parameter
				
				log_folders = settings.get_logfolders(settings.log_root_folder, all_parameters, reps)
				if not np.all([IO.check_simulation_complete(settings.log_folder) for log_folder in log_folders]):
						run_simulation(settings.simulation_parameters, reps)
				
				print "Calculating data for %s"%log_folder
				all_data[i] = calculate_stats(settings.log_folders, parameters_to_store, reps)        
		
		return all_data
"""


def get_number_of_stability_margin_crossings():
	pass

def get_number_of_rounds_within_stability_margin(trade_prices, rounds, fundamental_after_step):
	try:
		#trades_within_margin = rounds[np.where((trade_prices >= fundamental_after_step - stability_margin)
		#						&(trade_prices <= fundamental_after_step + stability_margin))[0]]
		trades_outside_margin = rounds[np.where((trade_prices < fundamental_after_step - stability_margin)
								| (trade_prices > fundamental_after_step + stability_margin))[0]]

		#print list(trades_within_margin)
		#trades_within_margin = list(set(trades_within_margin))
		rounds_outside_margin = set(trades_outside_margin)
		rounds_inside_margin = [i for i in range(max(rounds)) if not i in rounds_outside_margin]
		#	print rounds_inside_margin
		#for i in range(min(trades_within_margin), max(rounds):

		#print min(trades_within_margin)
		#rounds_within_margin = [value for value, group in groupby(trades_within_margin)]
		distance_between_rounds = np.diff(rounds_inside_margin)
		
		stable_periods_lengths = [len([i for i in group]) for value, group in groupby(distance_between_rounds) if value == 1]
		#print stable_periods_lengths
		if len(stable_periods_lengths) == 0:
			n_intervals = 10**6 ### If the simulation never enters the stable region this is a very bad thing.
		else:
			n_intervals = len(stable_periods_lengths)
		return {'total_number_of_rounds':sum(stable_periods_lengths), 'n_intervals':n_intervals}
	except ValueError:
		return {'total_number_of_rounds':0, 'n_intervals':10**6}

def get_first_round_to_enter_stability_margin():
	pass

def get_first_round_to_leave_stability_margin():
	pass

def get_maximum_distance_from_new_fundamental():
	pass

def empty_data_matrix(n_rows = 1):
		 return np.zeros(shape = n_rows, dtype = data_types.items())


#if __name__ == "__main__":
#	evaluate_simulation_results(settings.simulation_parameters, reps=range(2))


