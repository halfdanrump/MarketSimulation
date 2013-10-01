#import sys
from subprocess import Popen
import numpy as np
#import pdb
from os import devnull
#from pprint import pprint
import copy
from collections import Iterable
import shutil
from matplotlib import pyplot as plt

jar_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Simulation.jar'
log_root_folder = '/Users/halfdan/Desktop/simulation_data/'
data_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/'
graph_root_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/simulation_graphs/'

graph_types = ['line_chart']
# parameters['experimentName'] = 'whatever'


### store all the parameters in the dict

### Create data file

data_to_calculate = [
                    'buy_catchup_round',
                    'sell_catchup_round',
                    'max_traded_price_after_step',
                    'min_traded_price_after_step',
                    #('largest_100_change', float),
                    #('largest_1000_change', float), ## Calculate with np.diff(n=1000)
                    'traded_price_std_after_sellbuy_reach_new_fundamental',
                    'traded_price_mean_after_sellbuy_reach_new_fundamental',
                    'traded_price_median_after_sellbuy_reach_new_fundamental'
                    ]
                    


#parameters_to_sweep = ['ssmm_nAgents', 'sc_nAgents']


#parameters = {'a':range(2), 'b':range(2)}



def check_parameters(parameter_ranges):
    """ If a parameter is not in the list parameters_to_sweep, the parameter dictionary 
        should only contain a single number for all other variables. If a range has already been
        specified for such a parameter, the minimum value in that range is used.
    """
    for (par,par_range) in parameter_ranges.items(): assert isinstance(par_range, Iterable), "Parameter %s is not defined as a range. Aborting.."%par
    for (par, par_range) in parameter_ranges.items(): assert len(par_range) != 0, "Parameter %s has zero range. Aborting.."%par



def generate_parameter_combinations(parameter_ranges,  remaining_parameters = [], simulation_parameters = dict(), all_combinations = list()):
    if not remaining_parameters: remaining_parameters = parameter_ranges.keys()
    current_parameter = remaining_parameters.pop()
   # print "current_parameter %s, current range %s"%(current_parameter, parameter_ranges[current_parameter])
    for par in parameter_ranges[current_parameter]:
 #       print "current_parameter %s, value %s"%(current_parameter, par)
        simulation_parameters[current_parameter] = par
        if len(remaining_parameters) == 0:
            yield copy.deepcopy(simulation_parameters)
        else:
            for k in generate_parameter_combinations(parameter_ranges=parameter_ranges, remaining_parameters=remaining_parameters, simulation_parameters=simulation_parameters, all_combinations=all_combinations):
                yield k
    remaining_parameters.append(current_parameter)


def get_log_folder_list(log_root_folder, parameters, reps):
    return [log_root_folder + str(hash(repr(sorted(parameters.items())))) + '/%s/'%rep for rep in reps]


def run_simulation_for_all_parameter_combinations(parameter_ranges, reps):
    assert isinstance(reps, Iterable), "Please specify an iterable for reps"
    for parameters in generate_parameter_combinations(parameter_ranges):
        run_simulation_for_single_parameter_combination(parameters, reps)


def run_simulation_for_single_parameter_combination(parameter_combination, reps):
    processes = list()
    log_folders = get_log_folder_list(log_root_folder, parameter_combination, reps)

    for rep in reps:
            ### Remove old simulation data calculated with the same parameters
            
            shutil.rmtree(log_folders[rep], ignore_errors=True)
            
            ### Build parameter string for java program
            par_string = ''
            for (par, val) in parameter_combination.items(): par_string += '-D%s=%s '%(par,val)
            vm_args = "java -d64 -Xms512m -Xmx4g -DlogFolder=%s "%log_folders[rep]
            command = vm_args + par_string + '-jar %s'%jar_path
            print "Running simulation with command: %s\n"%command
            processes.append(Popen(command.split(' '), stdout=open(devnull, 'w')))            
    for p in processes:
        p.wait()


def  make_line_plot(log_root_folder = "", all_parameter_ranges = dict(),  x_axis_parameter = "", x_axis_parameter_range = list(), y_axis_data = ""):
    """

    """
    par_ranges = copy.deepcopy(all_parameter_ranges)

    assert x_axis_parameter, "Please specify x_axis (which simulation parameters to sweep)"
    assert y_axis_data, "Please specify y_axis (calculated data to plot)"
    assert x_axis_parameter in all_parameter_ranges.keys(), "Please specify a parameter which is used in the simulation"
    assert y_axis_data in data_to_calculate, "Please specify a type of data for which calculation has been implemented"
    ### Get data 
    par_ranges[x_axis_parameter] = x_axis_parameter_range

    ### 

def make_plot(all_data, x_axis_name = "", y_axis_name = "", all_parameters = {}):
    assert x_axis_name in all_data.dtype.names, "x axis parameter not found"
    assert y_axis_name in all_data.dtype.names, "y axis parameter not found"
    assert all_parameters

    plt.plot(all_data[x_axis_name],all_data[y_axis_name])
    graph_filename = graph_root_folder + "%s_vs_%s.pdf"%(x_axis_name, y_axis_name)
    caption = repr([(k,all_parameters[k]) for k in sorted(all_parameters)])
    print caption
    #plt.text(caption)
    plt.savefig(graph_filename)

def get_data_for_single_parameter_sweep(parameter_to_sweep = "", parameter_range = list(), all_parameters = dict(), reps = list()):
    try:
        del all_parameters[parameter_to_sweep]
    except KeyError:
        pass

    for par, r in all_parameters.items():
        assert not isinstance(r, Iterable), "Please specify a single value for all parameters except the parameter to sweep: %s"%par

    #parameters_to_store = [parameter_to_sweep]
    data_type = [(parameter_to_sweep, int)] + [(d, float) for d in data_to_calculate]
    
    all_data = np.zeros(shape=(len(parameter_range)), dtype = data_type)

    for i, parameter in enumerate(parameter_range):
        parameters_to_store = {parameter_to_sweep:parameter}
        all_parameters[parameter_to_sweep] = [parameter]
        
        log_folders = get_log_folder_list(log_root_folder, all_parameters, reps)
        if np.all([check_simulation_complete(log_folder) for log_folder in log_folders]):
            all_data[i] = calculate_data_for_specific_parameter_combination(log_folders, parameters_to_store, reps)        
        else:
            run_simulation_for_single_parameter_combination(all_parameters, reps)
            all_data[i] = calculate_data_for_specific_parameter_combination(log_folders, parameters_to_store, reps)        

    return all_data

"""
def calculate_data_for_all_parameter_combinations(log_root_folder, parameter_ranges, reps):
    check_parameters(parameter_ranges)    
    assert isinstance(reps, Iterable), "Please specify an iterable for reps"
    #parameters_to_store = [(k, int) for k in parameter_ranges.keys()]
    n_data_rows = np.prod([len(p) for p in parameter_ranges])   

    data_type = [(k, int) for k in parameter_ranges.keys()] + [(d, float) for d in data_to_calculate]
    all_data = np.zeros(shape=(n_data_rows), dtype=data_type)
    pdb.set_trace()

    for i, parameters in enumerate(generate_parameter_combinations(parameter_ranges)):
        log_folders = get_log_folder_list(log_root_folder, parameters, reps)
        all_data[i] = calculate_data_for_specific_parameter_combination(log_folders, parameters, reps)
        print all_data
        np.save('data', all_data)
"""

def calculate_data_for_specific_parameter_combination(log_folders = list(), parameters_to_store = {'par_name':'par_value'}, reps = range(0)):

    rep_data = np.zeros(shape=len(reps), dtype = [(d, float) for d in data_to_calculate]) 
    for rep in reps:
        assert check_simulation_complete(log_folders[rep]), "Simulation wasn't finished for specified set of parameters"
        ### Read files        
        ob_round_based = np.genfromtxt(log_folders[rep] + 'columnLog_roundBased_orderbook(0,0).csv', names=True, dtype=int, delimiter=',', usecols=(4,5))
        stock_round_based = np.genfromtxt(log_folders[rep] + 'columnLog_roundBased_stock0.csv', names=True, dtype=int, delimiter=',', usecols=(0,1))
        trades = np.genfromtxt(log_folders[rep] + 'columnLog_transactionBased_stock0.csv', names=True, dtype=int, delimiter=',', usecols=(0,2))

        
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
        
        mean_data = [np.mean(rep_data[d]) for d in rep_data.dtype.names]

        data_type = [(p, int) for p in parameters_to_store] + [(d, float) for d in data_to_calculate]
        md = np.zeros(1, dtype=data_type)
        for (par_name, par_value) in parameters_to_store.items():
            md[par_name] = par_value
        for i, d in enumerate(data_to_calculate):
            md[d] = mean_data[i]
        
        return md
        #print "parameters_to_store %s"%parameters_to_store
        #param_data = [eval(p[0]) for p in parameters_to_store]
        #return tuple(param_data + mean_data)


def get_graph_folder(log_root_folder, graph_type = 'line_chart'):
    """
    Graphs for stored in folders according to which type they are
    """
    assert graph_type in graph_types
    graph_folder = "%s/%s/"%(graph_root_folder, graph_type)
    print "Graph folder: %s"%graph_folder
    return graph_folder

"""
def get_finished_simulation_folders(parameter_ranges, log_root_folder, reps):

    ### Runs through parameter ranges and checks which simulations have been completed. Then returns a list of these folders

    completed_folders = list()
    for parameters in generate_parameter_combinations(parameter_ranges):
        folders = get_log_folder_list(log_root_folder, parameters, reps)
        for folder in folders:
            try:
                with open(folder + 'finished.txt'): 
                    completed_folders.append(folder)
            except IOError:
                pass
    return completed_folders
"""


def check_simulation_complete(full_simulation_log_path):
    try:
        with open(full_simulation_log_path + 'finished.txt'): 
            print "Found completed data"
            return True
    except IOError:
        return False



"""
def get_parameter_ranges_for_simulation():
    parameter_ranges = dict()

    parameter_ranges['nRounds'] = [10000]
    parameter_ranges['minLat'] = [1]
    parameter_ranges['maxLat'] = [100]
    parameter_ranges['minThink'] = [1]
    parameter_ranges['maxThink'] = [1]
    parameter_ranges['ssmm_MinSpread'] = [2]
    parameter_ranges['ssmm_MaxSpread'] = [10]

    parameter_ranges['sc_timeHorizonMin'] = [1000]
    parameter_ranges['sc_timeHorizonMax'] = [20000]
    parameter_ranges['sc_ticksBeforeReactingMin'] = [2]
    parameter_ranges['sc_ticksBeforeReactingMax'] = [5]
    parameter_ranges['sc_priceTickSizeMin'] = [1]
    parameter_ranges['sc_priceTickSizeMax'] = [1]
    
    parameter_ranges['sc_waitTimeBetweenTradingMin'] = [10]
    

    parameter_ranges['sc_waitTimeBetweenTradingMax'] = [100]
    
    parameter_ranges['ssmm_nAgents'] = range(2)
    parameter_ranges['sc_nAgents'] = range(2)
    
    check_parameters(parameter_ranges)
    return copy.deepcopy(parameter_ranges)
"""

def get_default_parameters():
    parameter_ranges = dict()

    parameter_ranges['nRounds'] = 100000


    parameter_ranges['minLat'] = 1
    parameter_ranges['maxLat'] = 100
    parameter_ranges['minThink'] = 1
    parameter_ranges['maxThink'] = 1
            

    parameter_ranges['ssmm_MinSpread'] = 2
    parameter_ranges['ssmm_MaxSpread'] = 10

    parameter_ranges['sc_timeHorizonMin'] = 1000
    parameter_ranges['sc_timeHorizonMax'] = 20000
    parameter_ranges['sc_ticksBeforeReactingMin'] = 2
    parameter_ranges['sc_ticksBeforeReactingMax'] = 5
    parameter_ranges['sc_priceTickSizeMin'] = 1
    parameter_ranges['sc_priceTickSizeMax'] = 1
    
    parameter_ranges['sc_waitTimeBetweenTradingMin'] = 10
    

    parameter_ranges['sc_waitTimeBetweenTradingMax'] = 100
    
    parameter_ranges['ssmm_nAgents'] = 0
    parameter_ranges['sc_nAgents'] = 0
    #check_parameters(parameter_ranges)
    return copy.deepcopy(parameter_ranges)

if __name__ == "__main__":
    parameters = get_default_parameters()
    parameter_to_sweep = 'ssmm_nAgents'
    for 
    parameters['sc_nAgents'] = sc_nAgents
    parameter_range = range(0,40,1)
    reps = range(10)
    data = get_data_for_single_parameter_sweep(parameter_to_sweep, parameter_range, parameters, reps)
    make_plot(data, parameter_to_sweep, 'buy_catchup_round', parameters)















