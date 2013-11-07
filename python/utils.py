import numpy as np
from settings import default_parameters as defpar
from settings import data_types


def get_fundamental_after_shock():
	return defpar['fundamental_initial_value'] + defpar['fundamental_shock_size']

def empty_data_matrix(n_rows = 1):
	return np.zeros(shape = n_rows, dtype = data_types.items())