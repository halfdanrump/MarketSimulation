import numpy as np
import IO
from simulation_interface import run_simulation
from settings import data_to_calculate



def calculate_simulation_stats_for_reps(parameters = {}, reps = [0], autorun = False):
     assert parameters, "Please specify a dictionary with par_name:par_value as key:value sets"
     
     data = empty_data_matrix(len(reps))
     
     simulation_reps_to_run = []
     for r in reps:
          log_folder = IO.get_logfolder(parameters, r)
          try:
               assert IO.check_simulation_complete(log_folder), "Could not calculate data because simulation in folder %s was not finished"%log_folder
          except AssertionError:
               simulation_reps_to_run.append(r)
     
     run_simulation(parameters)

     for r in reps:
          data[r] = calculate_simulation_stats(log_folder)
     return data


def calculate_simulation_stats(logdata_folder = ""):
     assert logdata_folder, "Please specify where the logdata is located"
     

     data = empty_data_matrix(1)
     ob_round_based = np.genfromtxt(logdata_folder + 'columnLog_roundBased_orderbook(0,0).csv', names=True, dtype=int, delimiter=',', usecols=(1,2))
     stock_round_based = np.genfromtxt(logdata_folder + 'columnLog_roundBased_stock0.csv', names=True, dtype=int, delimiter=',', usecols=(0,1))
     trades = np.genfromtxt(logdata_folder + 'columnLog_transactionBased_stock0.csv', names=True, dtype=int, delimiter=',', usecols=(0,1))

     fundamental_step_round = np.where(np.diff(stock_round_based['fundamental'], n=1) != 0)[0]
     fundamental_after_step = stock_round_based['fundamental'][fundamental_step_round + 1]

     ### Collect data
     try:
       buy_catchup_round = np.min(np.where(ob_round_based['bestStandingBuyPrice'] == fundamental_after_step))
       data['buy_catchup_round'] = buy_catchup_round
     except ValueError:
       ### Happens when the buy price never catches up to the fundamental
       buy_catchup_round = None
     finally:
       data['buy_catchup_round'] = buy_catchup_round

     try:
       sell_catchup_round = np.min(np.where(ob_round_based['bestStandingSellPrice'] == fundamental_after_step))
     except ValueError:
       ### Happens when the sell price never catches up to the fundamental
       sell_catchup_round = None
     finally:
       data['sell_catchup_round'] = sell_catchup_round

     if buy_catchup_round and sell_catchup_round:
       catchup_round = max(buy_catchup_round, sell_catchup_round)
       try:
           trade_index = np.min(np.where(trades['round'] >= catchup_round))
           data['traded_price_std_after_sellbuy_reach_new_fundamental'] = np.std(trades['price'][trade_index::])
           data['traded_price_mean_after_sellbuy_reach_new_fundamental'] = np.mean(trades['price'][trade_index::])
           data['traded_price_median_after_sellbuy_reach_new_fundamental'] = np.median(trades['price'][trade_index::])
       except ValueError:
           data['traded_price_std_after_sellbuy_reach_new_fundamental'] = None
           data['traded_price_mean_after_sellbuy_reach_new_fundamental'] = None
           data['traded_price_median_after_sellbuy_reach_new_fundamental'] = None

     try:
          i = np.min(np.where(trades['round'] > fundamental_step_round))
          data['max_traded_price_after_step'] = np.max(trades['price'][i::])
          data['min_traded_price_after_step'] = np.min(trades['price'][i::])
     except ValueError:
          data['max_traded_price_after_step'] = None
          data['min_traded_price_after_step'] = None

     data['tp_stable_round'] = get_stable_round(trades['price'], fundamental_after_step, threshold = 3)

     return data


def empty_data_matrix(n_rows = 1):
     return np.zeros(shape = n_rows, dtype = data_to_calculate.items())


def get_stable_round(traded_prices, fundamental, threshold = 3):
    try:
        stable_idx = np.where((traded_prices > fundamental - threshold) & (traded_prices < fundamental + threshold))[0]
        print stable_idx
        diffs = np.diff(stable_idx)
        print diffs
        stable_round = np.max(np.where(diffs > 1))
        return stable_round
    except ValueError:
        return None



"""
def calculate_data_for_specific_parameter_combination(log_folders = list(), parameters_to_store = {'par_name':'par_value'}, reps = range(0)):

    rep_data = np.zeros(shape=len(reps), dtype = data_to_calculate.items()) 
    for rep in reps:
        assert IO.check_simulation_complete(log_folders[rep]), "Simulation wasn't finished for specified set of parameters"
        ### Read files        
        

        
        fundamental_step_round = np.where(np.diff(stock_round_based['fundamental'], n=1) != 0)[0]
        fundamental_after_step = stock_round_based['fundamental'][fundamental_step_round + 1]
        
        ### Collect data
        try:
            buy_catchup_round = np.min(np.where(ob_round_based['bestStandingBuyPrice'] == fundamental_after_step))
            rep_data['buy_catchup_round'][rep] = buy_catchup_round
        except ValueError:
            ### Happens when the buy price never catches up to the fundamental
            buy_catchup_round = None
        finally:
            rep_data['buy_catchup_round'][rep] = buy_catchup_round

        try:
            sell_catchup_round = np.min(np.where(ob_round_based['bestStandingSellPrice'] == fundamental_after_step))
        except ValueError:
            ### Happens when the sell price never catches up to the fundamental
            sell_catchup_round = None
        finally:
            rep_data['sell_catchup_round'][rep] = sell_catchup_round

        if buy_catchup_round and sell_catchup_round:
            catchup_round = max(buy_catchup_round, sell_catchup_round)
            
            try:
                trade_index = np.min(np.where(trades['round'] >= catchup_round))
                rep_data['traded_price_std_after_sellbuy_reach_new_fundamental'][rep] = np.std(trades['price'][trade_index::])
                rep_data['traded_price_mean_after_sellbuy_reach_new_fundamental'][rep] = np.mean(trades['price'][trade_index::])
                rep_data['traded_price_median_after_sellbuy_reach_new_fundamental'][rep] = np.median(trades['price'][trade_index::])
            except ValueError:
                rep_data['traded_price_std_after_sellbuy_reach_new_fundamental'][rep] = None
                rep_data['traded_price_mean_after_sellbuy_reach_new_fundamental'][rep] = None
                rep_data['traded_price_median_after_sellbuy_reach_new_fundamental'][rep] = None
        try:
            i = np.min(np.where(trades['round'] > fundamental_step_round))
            rep_data['max_traded_price_after_step'][rep] = np.max(trades['price'][i::])
            rep_data['min_traded_price_after_step'][rep] = np.min(trades['price'][i::])
        except ValueError:
            rep_data['max_traded_price_after_step'][rep] = None
            rep_data['min_traded_price_after_step'][rep] = None
        
        rep_data['tp_stable_round'][rep] = get_stable_round(trades['price'], fundamental_after_step, threshold = 3)

    mean_data = [np.mean(rep_data[d]) for d in rep_data.dtype.names]
    print mean_data
    data_type = [(p, int) for p in parameters_to_store] + [(d, float) for d in data_to_calculate]
    md = np.zeros(1, dtype=data_type)
    for (par_name, par_value) in parameters_to_store.items():
        md[par_name] = par_value
    for i, d in enumerate(data_to_calculate):
        md[d] = mean_data[i]
    
    return md

"""