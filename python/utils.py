import numpy as np
from settings import default_parameters as defpar, fitness_types
from IO import load_trade_log_data
from pandas import DataFrame

def get_fundamental_after_shock():
	return defpar['fundamental_initial_value'] + defpar['fundamental_shock_size']

def empty_data_matrix(n_rows = 1):
	return np.zeros(shape = n_rows, dtype = fitness_types.items())





def load_test_trade_data():
	return DataFrame(load_trade_log_data('/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/logs/'))


