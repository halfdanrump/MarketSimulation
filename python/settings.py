from collections import OrderedDict
from copy import deepcopy


TEST_MODE = False
WITH_SIMULATION_OUTPUT = False
KEEP_SIMULATION_DATA = False
MAKE_TRADEPRICE_PLOT = True
SAVE_DATA_USED_FOR_PLOTTING = True
PLOT_SAVE_PROB = 0.02

ga_reps = range(10)

reps = xrange(1) ### DEPRECATED!!!!!!!!!!!!!!!!!!!!!

jar_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Simulation.jar'
log_root_folder = '/Users/halfdan/simulation_data2/'
data_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/data_folder/'
#graph_root_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/simulation_graphs/'

#########################################################################
### Data analysis settings
#########################################################################
stability_margin = 3

#########################################################################
### GA SETTINGS
#########################################################################
deadborn_gene_fitness = 10000000000
if TEST_MODE:
    population_size = 4
else:
    population_size = 200

n_generations = 1000
mutation_prob = 0.1
crossover_prob = 0.4
tournament_size = 2

if TEST_MODE:
    n_simulation_rounds = 30000
else:
    n_simulation_rounds = 100000

default_parameters = {
    'fundamental_initial_value' : 10000,
    'fundamental_shock_size' : -10,
    'fundamental_shock_round' : 10000,

    'ssmm_nAgents' : 40,
    'sc_nAgents' : 300,

    'ssmm_latency_mu' : 1,
    'ssmm_latency_s' : 0,
    'ssmm_think_mu' : 1,
    'ssmm_think_s' : 0,
    'sc_latency_mu' : 1,
    'sc_latency_s' : 0,
    'sc_think_mu' : 1,
    'sc_think_s' : 0,
    
    'ssmm_spread_mu' : 4,
    'ssmm_spread_s' : 2,
    'ssmm_ordervol_mu' : 50,
    'ssmm_ordervol_s' : 20,
    'ssmm_orderlength_mu' : 500,
    'ssmm_orderlength_s' : 200,
    
    'sc_timehorizon_mu' : 5000,
    'sc_timehorizon_s' : 2000,
    'sc_ticksBeforeReacting_mu' : 2,
    'sc_ticksBeforeReacting_s' : 5,
    'sc_priceTickSize_mu' : 3,
    'sc_priceTickSize_s' : 2,
    'sc_ordervol_mu' : 10,
    'sc_ordervol_s' : 3,
    'sc_waitTimeBetweenTrading_mu' : 50,
    'sc_waitTimeBetweenTrading_s' : 20
}

if TEST_MODE:
    default_parameters['ssmm_nAgents'] = 0
    default_parameters['sc_nAgents'] = 0


parameters_in_genes = [
    'ssmm_nAgents',
    'sc_nAgents' ,

    #'ssmm_latency_mu',
    #'ssmm_latency_s',
    #'ssmm_think_mu',
    #'ssmm_think_s',
    #'sc_latency_mu',
    #'sc_latency_s',
    #'sc_think_mu',
    #'sc_think_s',
    #'sc_timehorizon_mu',
    #'sc_timehorizon_s',
    #'sc_waitTimeBetweenTrading_mu',
    #'sc_waitTimeBetweenTrading_s'
]



parameter_scaling = OrderedDict({
    'ssmm_nAgents' : 50,
    'sc_nAgents' : 300,

    'ssmm_latency_mu' : 10,
    'ssmm_latency_s' : 5,
    'ssmm_think_mu' : 10,
    'ssmm_think_s' : 5,
    'sc_latency_mu' : 10,
    'sc_latency_s' : 5,
    'sc_think_mu' : 10,
    'sc_think_s' : 5,
    
    'ssmm_spread_mu' : 4,
    'ssmm_spread_s' : 2,
    'ssmm_ordervol_mu' : 50,
    'ssmm_ordervol_s' : 20,
    'ssmm_orderlength_mu' : 500,
    'ssmm_orderlength_s' : 200,
    
    'sc_timehorizon_mu' : 5000,
    'sc_timehorizon_s' : 2000,
    'sc_ticksBeforeReacting_mu' : 2,
    'sc_ticksBeforeReacting_s' : 5,
    'sc_priceTickSize_mu' : 3,
    'sc_priceTickSize_s' : 2,
    'sc_ordervol_mu' : 10,
    'sc_ordervol_s' : 3,
    'sc_waitTimeBetweenTrading_mu' : 50,
    'sc_waitTimeBetweenTrading_s' : 20
})

if TEST_MODE:
    parameter_scaling['ssmm_nAgents'] = 0
    parameter_scaling['sc_nAgents'] = 0

parameter_minvals = OrderedDict({
    'ssmm_nAgents' : 0,
    'sc_nAgents' : 0,

    'ssmm_latency_mu' : 1,
    'ssmm_latency_s' : 0,
    'ssmm_think_mu' : 1,
    'ssmm_think_s' : 0,
    'sc_latency_mu' : 1,
    'sc_latency_s' : 0,
    'sc_think_mu' : 1,
    'sc_think_s' : 0,
    
    'ssmm_spread_mu' : 1,
    'ssmm_spread_s' : 0,
    'ssmm_ordervol_mu' : 1,
    'ssmm_ordervol_s' : 0,
    'ssmm_orderlength_mu' : 1,
    'ssmm_orderlength_s' : 0,
    
    'sc_timehorizon_mu' : 1,
    'sc_timehorizon_s' : 0,
    'sc_ticksBeforeReacting_mu' : 1,
    'sc_ticksBeforeReacting_s' : 0,
    'sc_priceTickSize_mu' : 1,
    'sc_priceTickSize_s' : 0,
    'sc_ordervol_mu' : 1,
    'sc_ordervol_s' : 0,
    'sc_waitTimeBetweenTrading_mu' : 1,
    'sc_waitTimeBetweenTrading_s' : 0
})



data_for_failed_simulation = {
                    #'longest_interval_within_margin' : 0,
                    'stdev' : 10**6,
                    'overshoot' : 10**6,
                    'time_to_reach_new_fundamental' : 10**6,
                    'round_stable' : 10**6
                    }


fitness_weights = OrderedDict({
                    #'longest_interval_within_margin' : 1,
                    'stdev' : -1,
                    'overshoot' : -1,
                    'time_to_reach_new_fundamental' : -1,
                    'round_stable' : -1
                    })

fitness_types = {
                    #'longest_interval_within_margin' : int,
                    'stdev' : float,
                    'overshoot' : int,
                    'time_to_reach_new_fundamental' : int,
                    'round_stable' : int
                    }


def get_fixed_parameters():
    return deepcopy(default_parameters)


