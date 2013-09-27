import sys
from subprocess import Popen
import numpy as np
#import pdb
from os import devnull
from pprint import pprint
import copy
from collections import Iterable


parameters = dict()
jar_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Simulation.jar'
log_root_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/logs/'

# parameters['experimentName'] = 'whatever'
"""
parameters['nRounds'] = [200000]


parameters['minLat'] = [1]
parameters['maxLat'] = [100]
parameters['minThink'] = [1]
parameters['maxThink'] = [1]
        

parameters['ssmm_MinSpread'] = [2]
parameters['ssmm_MaxSpread'] = [10]

parameters['sc_timeHorizonMin'] = [1000]
parameters['sc_timeHorizonMax'] = [20000]
parameters['sc_ticksBeforeReactingMin'] = [2]
parameters['sc_ticksBeforeReactingMax'] = [5]
parameters['sc_priceTickSizeMin'] = [1]
parameters['sc_priceTickSizeMax'] = [1]
parameters['sc_waitTimeBetweenTradingMin'] = [10]
parameters['sc_waitTimeBetweenTradingMax'] = [100]
"""
parameters['ssmm_nAgents'] = xrange(0,5)
parameters['sc_nAgents'] = xrange(5,10)
### store all the parameters in the dict

### Create data file
ssmm_nAgents_range = range(10,500,10)
sc_nAgents_range = range(0, 40)

parameters_to_sweep = ['ssmm_nAgents', 'sc_nAgents']

#parameters = {'a':range(2), 'b':range(2)}

def check_parameters(parameter_ranges):
    """ If a parameter is not in the list parameters_to_sweep, the parameter dictionary 
        should only contain a single number for all other variables. If a range has already been
        specified for such a parameter, the minimum value in that range is used.
    """
    for (par,par_range) in parameter_ranges.items():
        if not isinstance(par_range, Iterable):
            print "Parameter %s is not defined as a range. Aborting.."%par
            sys.exit()

def generate_parameter_combinations(parameter_ranges,  remaining_parameters = [], simulation_parameters = dict(), all_combinations = list()):
    
    if len(remaining_parameters) == 0: remaining_parameters = parameter_ranges.keys()

    current_parameter = remaining_parameters.pop()
    for par in parameter_ranges[current_parameter]:
        simulation_parameters[current_parameter] = par
        if len(remaining_parameters) == 0:
            yield copy.deepcopy(simulation_parameters)
        else:
            for k in generate_parameter_combinations(parameter_ranges=parameter_ranges, remaining_parameters=remaining_parameters, simulation_parameters=simulation_parameters, all_combinations=all_combinations):
                yield k
    remaining_parameters.append(current_parameter)


def get_log_folder_list(log_root_folder, parameters, reps):
    return [log_root_folder + str(hash(repr(sorted(parameters.items())))) + '/%s/'%rep for rep in reps]


def run_simulation(parameter_ranges, reps):
    check_parameters(parameter_ranges)    
    if not isinstance(reps, Iterable): 
        print "Please specify an iterable for reps"
        sys.exit()

    
    for parameters in generate_parameter_combinations(parameter_ranges):
        processes = list()
        log_folders = get_log_folder_list(log_root_folder, parameters, reps)
        for rep in reps:
                par_string = ''
                for (par, val) in parameters.items(): par_string += '-D%s=%s '%(par,val)
                vm_args = "java -d64 -Xms512m -Xmx4g -DlogFolder=%s "%log_folders[rep]
                command = vm_args + par_string + '-jar %s'%jar_path
                processes.append(Popen(command.split(' '), stdout=open(devnull, 'w')))
            
        for p in processes:
            p.wait()

    #recursive_sweep_parameters(parameter_ranges, parameter_ranges.keys())



        ### run simulation with parameter dict, where one parameter is changed
run_simulation(parameter_ranges=parameters)

#recursive_sweep_parameters(parameter_ranges, simulation_parameters=dict(), remaining_parameters=parameters.keys(), all_combinations=all_combinations)

def runSimulation(parameters, jar_path, log_root_folder, reps = xrange(10)):
    if not (jar_path or log_root_folder):
        print "Please specify absolute jar_path and log_root_folder"
    else:
        print "Jar path: %s"%jar_path
        print "Log root folder: %s"%log_root_folder
        print "Reps: %s"%[r for r in reps]

    for sc_nAgents in ssmm_nAgents_range:
        for ssmm_nAgents in sc_nAgents_range:
            print "\nsc_nAgents: %s, ssmm_nAgents: %s\n"%(sc_nAgents, ssmm_nAgents)
            parameters['ssmm_nAgents'] = ssmm_nAgents
            parameters['sc_nAgents'] = sc_nAgents
           
            processes = list()
            
            log_folders = get_log_folder_list(log_root_folder, parameters, reps)
           
            for rep in reps:
                par_string = ''
                for (par, val) in parameters.items(): par_string += '-D%s=%s '%(par,val)
                vm_args = "java -d64 -Xms512m -Xmx4g -DlogFolder=%s "%log_folders[rep]
                command = vm_args + par_string + '-jar %s'%jar_path
                #print command 
           
                processes.append(Popen(command.split(' '), stdout=open(devnull, 'w')))
            
            for p in processes:    
                p.wait()

def get_calculated_parameters():
    pass

def calculateData(log_root_folder, reps):

    n_data_rows = len(ssmm_nAgents_range) * len(sc_nAgents_range)

    data_to_calculate = [
                        ('buy_catchup_round', float),
                        ('sell_catchup_round', float),
                        ('max_traded_price',float),
                        ('min_traded_price', float),
                        #('largest_100_change', float),
                        #('largest_1000_change', float), ## Calculate with np.diff(n=1000)
                        ('traded_price_std_after_sellbuy_reach_new_fundamental', float),
                        ('traded_price_mean_after_sellbuy_reach_new_fundamental', float)
                        ]

    parameters_to_store = [('ssmm_nAgents', int), ('sc_nAgents', int)]

    rep_data = np.zeros(shape=len(reps), dtype = data_to_calculate) 
    all_data = np.zeros(shape=(n_data_rows), dtype=parameters_to_store + data_to_calculate)
    
    counter = 0

    print "Reading files"
    for ssmm_nAgents in ssmm_nAgents_range:
        for sc_nAgents in sc_nAgents_range:
            
            for rep in reps:
                parameters['ssmm_nAgents'] = ssmm_nAgents
                parameters['sc_nAgents'] = sc_nAgents

                log_folders = get_log_folder_list(log_root_folder, parameters, reps)
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
                    rep_data['buy_catchup_round'][rep] = None
                try:
                    sell_catchup_round = np.min(np.where(ob_round_based['bestStandingSellPrice'] == fundamental_after_step))
                    rep_data['sell_catchup_round'][rep] = sell_catchup_round
                except ValueError:
                    ### Happens when the sell price never catches up to the fundamental
                    rep_data['sell_catchup_round'][rep] = None

                if buy_catchup_round and sell_catchup_round:
                    catchup_round = max(buy_catchup_round, sell_catchup_round)
                    try:
                        
                        trade_index = np.min(np.where(trades['round'] >= catchup_round))
                        rep_data['traded_price_std_after_sellbuy_reach_new_fundamental'][rep] = np.std(trades['price'][trade_index::])
                        rep_data['traded_price_mean_after_sellbuy_reach_new_fundamental'][rep] = np.mean(trades['price'][trade_index::])
                        print rep_data
                        
                    except ValueError:
                        rep_data['traded_price_std_after_sellbuy_reach_new_fundamental'][rep] = None
                        rep_data['traded_price_mean_after_sellbuy_reach_new_fundamental'][rep] = None
                
                i = np.min(np.where(trades['round'] > fundamental_step_round))
                rep_data['max_traded_price'][rep] = np.max(trades['price'][i::])
                rep_data['min_traded_price'][rep] = np.min(trades['price'][i::])

            print rep_data
            mean_data = [np.mean(rep_data[d]) for d in rep_data.dtype.names]
            param_data = [eval(p[0]) for p in parameters_to_store]
            
            all_data[counter] = tuple(param_data + mean_data)
            print all_data
            counter += 1
            np.save('data', all_data)



    #Make graph of time to catch up as a function of number of market makers