import numpy as np
import IO
from simulation_interface import run_simulation
#from settings import data_to_calculate, simulation_parameters
from settings import stability_margin, KEEP_SIMULATION_DATA, PLOT_SAVE_PROB, data_for_failed_simulation
from random import randint
from itertools import groupby
from utils import get_fundamental_after_shock, empty_data_matrix, get_epoch_time
import plotting
#from collections import Iterable

def get_named_stats(data, attribute_names = list()):
	mean = dict()
	for attr in attribute_names:
		mean.update({attr: np.mean(data[attr])})

	std = dict()
	for attr in attribute_names:
		std.update({attr: np.std(data[attr])})    

	return {'mean':mean, 'std':std}


def evaluate_simulation_results(graph_folder, generation_number, parameters = {}, reps = [0], autorun = False, plot_name = None):
	assert parameters, "Please specify a dictionary with par_name:par_value as key:value sets"

	data = empty_data_matrix(len(reps))
	saved_simulation_data_ids = list()

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
		data[r], data_id = __evaluate_simulation_results(parameters, log_folders[r], graph_folder, generation_number, plot_name)
		if data_id: saved_simulation_data_ids.append(data_id)

	return data, saved_simulation_data_ids



def __evaluate_simulation_results(parameters, logdata_folder, graph_folder, generation_number, plot_name = None):
	assert logdata_folder, "Please specify where the logdata is located"

	data = empty_data_matrix(1)

	#ob_round_based = np.genfromtxt(logdata_folder + 'columnLog_roundBased_orderbook(0,0).csv', names=True, dtype=int, delimiter=',', usecols=(1,2))
	#stock_round_based = np.genfromtxt(logdata_folder + 'columnLog_roundBased_stock0.csv', names=True, dtype=int, delimiter=',', usecols=(0,1))
	trades = IO.load_trade_log_data(logdata_folder)

	

	
	fas = get_fundamental_after_shock()
	within_margin = get_number_of_rounds_within_stability_margin(trades['price'], trades['round'], fas)
	#data['n_simulation_rounds_within_stability_margin'] = within_margin['total_number_of_rounds']
	#data['n_seperate_intervals_within_stability_margin'] = within_margin['n_intervals']
	if 'longest_interval_within_margin' in data_for_failed_simulation.keys():
		data['longest_interval_within_margin'] = within_margin['longest_interval']
	if 'stdev' in data_for_failed_simulation.keys():
		data['stdev'] = get_tp_std_after_entering_margin(trades['price'], trades['round'])
	if 'overshoot' in data_for_failed_simulation.keys():
		data['overshoot'] = calculate_overshoot(fas, trades['price'], trades['round'])
	if 'time_to_reach_new_fundamental' in data_for_failed_simulation.keys():
		data['time_to_reach_new_fundamental'] = get_first_round_to_reach_new_fundamental(trades['price'], trades['round'])
	if 'round_stable' in data_for_failed_simulation.keys():
		data['round_stable'] = calculate_round_stable(trades['price'], trades['round'], fas)

	
	if np.random.random() <= PLOT_SAVE_PROB:
		data_id = 'gen%s_%s_%s'%(generation_number, get_epoch_time(), str(abs(hash(np.random.random()))))
		print graph_folder + data_id + '.npz'
		
		IO.save_tradeprice_data(trades['round'], trades['price'], data, parameters, graph_folder + data_id + '.npz')
		#plotting.make_pretty_tradeprice_plot(trades['round'], trades['price'], data, parameters, graph_folder + data_id + '.png')
		plotting.make_tradeprice_plot(trades['round'], trades['price'], data, parameters, graph_folder + data_id + '.png')
	else:
		data_id = None
		
	if not KEEP_SIMULATION_DATA: IO.delete_simulation_data(logdata_folder)
	return data, data_id


def calculate_round_stable(prices, rounds, fas):
	for p, r in zip(prices[::-1], rounds[::-1]):
		if not (p <= fas + stability_margin and p >= fas - stability_margin):
			return r


def calculate_overshoot(new_fundamental, prices, rounds):
	when_reach_fund = get_first_round_to_reach_new_fundamental(prices, rounds)
	
	idx, = np.where(rounds > when_reach_fund)
	
	try:
		max_price = max(prices[idx])
		min_price = min(prices[idx])
		overshoot = max(abs(new_fundamental - max_price), abs(new_fundamental - min_price))
	except ValueError:
		overshoot = data_for_failed_simulation['overshoot']
	return overshoot

def get_number_of_stability_margin_crossings():
	pass

def get_number_of_rounds_within_stability_margin(trade_prices, trade_rounds, fundamental_after_step):
	try:
		trades_outside_margin = trade_rounds[np.where((trade_prices < fundamental_after_step - stability_margin)
								| (trade_prices > fundamental_after_step + stability_margin))[0]]

		rounds_outside_margin = set(trades_outside_margin)
		rounds_inside_margin = [i for i in range(max(trade_rounds)) if not i in rounds_outside_margin]

		distance_between_rounds = np.diff(rounds_inside_margin)
		
		stable_periods_lengths = [len([i for i in group]) for value, group in groupby(distance_between_rounds) if value == 1]
	
		if len(stable_periods_lengths) == 0:
			n_intervals = 10**6 ### If the simulation never enters the stable region this is a very bad thing.
		else:
			n_intervals = len(stable_periods_lengths)
		return {'total_number_of_rounds':sum(stable_periods_lengths), 'n_intervals':n_intervals, 'longest_interval': max(stable_periods_lengths)}
	except ValueError:
		return {'total_number_of_rounds':0, 'n_intervals':10**6}

def get_first_round_to_reach_new_fundamental(trade_prices, trade_rounds):
	trade_prices = np.array(trade_prices)
	fas = get_fundamental_after_shock()
	try:
		first_round = np.min(trade_rounds[np.where(trade_prices == fas)])
	except ValueError:
		first_round = 10**6
	return first_round

def get_tp_std_after_entering_margin(trade_prices, trade_rounds):
	first_round = get_first_round_to_reach_new_fundamental(trade_prices, trade_rounds)
	try:
		return np.std(trade_prices[trade_rounds > first_round])
	except ValueError:
		return 10**6

def get_first_round_to_leave_stability_margin():
	pass

def get_maximum_distance_from_new_fundamental_after_entering_margin():
	pass



	