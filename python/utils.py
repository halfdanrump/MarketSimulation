from settings import default_parameters as defpar

def get_fundamental_after_shock():
	return defpar['fundamental_initial_value'] + defpar['fundamental_shock_size']