import numpy as np
import IO
from simulation_interface import run_simulation
#from settings import data_to_calculate, simulation_parameters
import settings
from collections import Iterable


def calculate_stats(parameters = {}, reps = [0], autorun = False):
     assert parameters, "Please specify a dictionary with par_name:par_value as key:value sets"
     
     data = empty_data_matrix(len(reps))
     
     simulation_reps_to_run = []
     log_folders = IO.get_logfolders(parameters, reps)
     for r in reps:
          try:
               assert IO.check_simulation_complete(log_folders[r]), "Could not calculate data because simulation in folder %s was not finished"%log_folders[r]
          except AssertionError:
               simulation_reps_to_run.append(r)
     
     if simulation_reps_to_run: run_simulation(parameters, simulation_reps_to_run)

     for r in reps:
          data[r] = __calculate_simulation_stats(log_folders[r])
     return data


def __calculate_simulation_stats(logdata_folder = ""):
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


def empty_data_matrix(n_rows = 1):
     return np.zeros(shape = n_rows, dtype = settings.data_to_calculate.items())


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

if __name__ == "__main__":
  calculate_stats(settings.simulation_parameters, reps=range(2))


