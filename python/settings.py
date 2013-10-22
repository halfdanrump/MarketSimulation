from collections import OrderedDict

WITH_SIMULATION_OUTPUT = False
KEEP_SIMULATION_DATA = False

reps = xrange(1)

jar_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Simulation.jar'
log_root_folder = '/Users/halfdan/simulation_data/'
data_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/data_folder/'
graph_root_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/simulation_graphs/'

#########################################################################
### Data analysis settings
#########################################################################
stability_margin = 3

#########################################################################
### GA SETTINGS
#########################################################################
deadborn_gene_fitness = 1000000000000000000
population_size = 200
n_generations = 1000
mutation_prob = 0.2
crossover_prob = 0.4

simulation_parameters = {
    'nRounds' : 100000,
    
    'ssmm_nAgents' : 0,
    'sc_nAgents' : 0,

    'hft_latency_mu' : 1,
    'hft_latency_s' : 0,
    'hft_think_mu' : 1,
    'hft_think_s' : 0,
    
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

parameter_scaling = OrderedDict({
    'ssmm_nAgents' : 50,
    'sc_nAgents' : 500,

    'hft_latency_mu' : 100,
    'hft_latency_s' : 50,
    'hft_think_mu' : 100,
    'hft_think_s' : 50,
    
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

parameter_minvals = OrderedDict({
    'ssmm_nAgents' : 0,
    'sc_nAgents' : 0,

    'hft_latency_mu' : 1,
    'hft_latency_s' : 0,
    'hft_think_mu' : 1,
    'hft_think_s' : 0,
    
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


data_to_calculate = {
                    'buy_catchup_round' : int,
                    'sell_catchup_round' : int,
                    #'max_traded_price_after_step' : int,
                    #'min_traded_price_after_step' : int,
                    #'traded_price_std_after_sellbuy_reach_new_fundamental' : float,
                    #'traded_price_mean_after_sellbuy_reach_new_fundamental' : float,
                    #'traded_price_median_after_sellbuy_reach_new_fundamental' : int,
                    'tp_stable_round' : int
                    }

data_for_failed_simulation = {
                    'buy_catchup_round' : 10**6,
                    'sell_catchup_round' : 10**6,
                    'max_traded_price_after_step' : -1,
                    'min_traded_price_after_step' : -1,
                    'traded_price_std_after_sellbuy_reach_new_fundamental' : -1,
                    'traded_price_mean_after_sellbuy_reach_new_fundamental' : -1,
                    'traded_price_median_after_sellbuy_reach_new_fundamental' : -1,
                    'tp_stable_round' : 10*6
                    }


fitness_weights = OrderedDict({
                    'buy_catchup_round' : -1,
                    'sell_catchup_round' : -1,
                    'tp_stable_round' : -1
                    })


