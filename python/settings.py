

jar_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Simulation.jar'
log_root_folder = '/Users/halfdan/simulation_data2/'
data_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/data_folder/'
graph_root_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/simulation_graphs/'



simulation_parameters = {
    'nRounds' : 100000,
    'minLat' : 1,
    'maxLat' : 100,
    'minThink' : 1,
    'maxThink' : 1,
    'ssmm_MinSpread' : 2,
    'ssmm_MaxSpread' : 10,
    'ssmm_orderVolMin' : 10,
    'ssmm_orderVolMax' : 100,
    'ssmm_orderVolMin' : 200,
    'ssmm_orderVolMax' : 200,
    'sc_timeHorizonMin' : 1000,
    'sc_timeHorizonMax' : 20000,
    'sc_ticksBeforeReactingMin' : 2,
    'sc_ticksBeforeReactingMax' : 5,
    'sc_priceTickSizeMin' : 1,
    'sc_priceTickSizeMax' : 5,
    'sc_orderVolMin' : 1,
    'sc_orderVolMax' : 10,
    'sc_waitTimeBetweenTradingMin' : 10,
    'sc_waitTimeBetweenTradingMax' : 100,
    'ssmm_nAgents' : 0,
    'sc_nAgents' : 0
}


data_to_calculate = {
                    'buy_catchup_round' : int,
                    'sell_catchup_round' : int,
                    'max_traded_price_after_step' : int,
                    'min_traded_price_after_step' : int,
                    'traded_price_std_after_sellbuy_reach_new_fundamental' : float,
                    'traded_price_mean_after_sellbuy_reach_new_fundamental' : float,
                    'traded_price_median_after_sellbuy_reach_new_fundamental' : int,
                    'tp_stable_round' : int
                    }


fitness_weights = {
                    'buy_catchup_round' : 2,
                    'sell_catchup_round' : 2,
                    'max_traded_price_after_step' : 0,
                    'min_traded_price_after_step' : 0,
                    'traded_price_std_after_sellbuy_reach_new_fundamental' : 1,
                    'traded_price_mean_after_sellbuy_reach_new_fundamental' : 0,
                    'traded_price_median_after_sellbuy_reach_new_fundamental' : 0,
                    'tp_stable_round' : 2
                    }


