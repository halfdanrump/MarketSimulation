#import sys
from subprocess import Popen
import numpy as np
#import pdb
#from os import devnull
#from pprint import pprint
import copy
from collections import Iterable
import shutil
import simulationSetup
import settings


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
    #par_conf = "_".join([k + str(parameters[k]) for k in sorted(parameters)])
    #return [log_root_folder + par_conf + '/%s/'%rep for rep in reps]
    return [log_root_folder + str(hash(repr(sorted(parameters.items())))) + '/%s/'%rep for rep in reps]


def run_simulation_for_all_parameter_combinations(parameter_ranges, reps):
    assert isinstance(reps, Iterable), "Please specify an iterable for reps"
    for parameters in generate_parameter_combinations(parameter_ranges):
        run_simulation(parameters, reps)



def run_simulation(parameters, reps):
    assert check_parameters(parameters), "Invalid simulation parameters!"

    processes = list()
    log_folders = get_log_folder_list(settings.log_root_folder, parameters, reps)

    for rep in reps:
            ### Remove old simulation data calculated with the same parameters
            shutil.rmtree(log_folders[rep], ignore_errors=True)
            
            ### Build parameter string for java program
            par_string = ''
            for (par, val) in parameters.items(): par_string += '-D%s=%s '%(par,val)
            vm_args = "java -d64 -Xms512m -Xmx4g -DlogFolder=%s "%log_folders[rep]
            command = vm_args + par_string + '-jar %s'%settings.jar_path
            print "Running simulation with command: %s\n"%command
            #processes.append(Popen(command.split(' '), stdout=open(devnull, 'w')))            
            processes.append(Popen(command.split(' ')))            
    for p in processes:
        p.wait()


def check_parameters():
    return False

def get_data_for_single_parameter_sweep(parameter_to_sweep = "", parameter_range = list(), all_parameters = dict(), reps = list()):
    try:
        del all_parameters[parameter_to_sweep]
    except KeyError:
        pass

    for par, r in all_parameters.items():
        assert not isinstance(r, Iterable), "Please specify a single value for all parameters except the parameter to sweep: %s"%par

    #parameters_to_store = [parameter_to_sweep]
    #data_type = [(parameter_to_sweep, int)] + [(d, float) for d in data_to_calculate]
    
    all_data = np.zeros(shape=(len(parameter_range)), dtype = data_type)

    for i, parameter in enumerate(parameter_range):
        parameters_to_store = {parameter_to_sweep:parameter}
        all_parameters[parameter_to_sweep] = parameter
        
        log_folders = get_log_folder_list(log_root_folder, all_parameters, reps)
        if not np.all([check_simulation_complete(log_folder) for log_folder in log_folders]):
            run_simulation_for_single_parameter_combination(all_parameters, reps)
        
        print "Calculating data for %s"%log_folder
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





def run_test_simulation():
    reps = range(5)

    parameters = simulationSetup.parameters
    
    ### Override default values

    for sc_nAgents in range(1):
    #for sc_nAgents in np.linspace(0,1,1):
        parameters['sc_nAgents'] = sc_nAgents

        parameter_to_sweep = 'ssmm_nAgents'
        parameter_range = range(20,21)
        prefix = "sc_nAgents=%s___"%sc_nAgents
        data = get_data_for_single_parameter_sweep(parameter_to_sweep, parameter_range, parameters, reps)
        print data.dtype.names
        #make_plot(data, prefix, parameter_to_sweep, 'buy_catchup_round', parameters)
        make_plot(all_data=data,prefix=prefix, x_axis_name=parameter_to_sweep, y_axis_name=['buy_catchup_round', 'sell_catchup_round'], all_parameters=parameters)
        make_plot(all_data=data,prefix=prefix, x_axis_name=parameter_to_sweep, y_axis_name=['max_traded_price_after_step', 'min_traded_price_after_step'], all_parameters=parameters)
        make_plot(all_data=data,prefix=prefix, x_axis_name=parameter_to_sweep, y_axis_name=['traded_price_mean_after_sellbuy_reach_new_fundamental', 'traded_price_median_after_sellbuy_reach_new_fundamental'], all_parameters=parameters)


def run_simulation():
    reps = range(1)

    parameters = simulationSetup.parameters
    
    ### Override default values

    for sc_nAgents in range(0, 600, 100):
    #for sc_nAgents in np.linspace(0,1,1):
        parameters['sc_nAgents'] = sc_nAgents

        parameter_to_sweep = 'ssmm_nAgents'
        parameter_range = range(40, 50, 5)
        prefix = "sc_nAgents=%s___"%sc_nAgents
        data = get_data_for_single_parameter_sweep(parameter_to_sweep, parameter_range, parameters, reps)
        print data.dtype.names
        #make_plot(data, prefix, parameter_to_sweep, 'buy_catchup_round', parameters)
        make_plot(all_data=data,prefix=prefix, x_axis_name=parameter_to_sweep, y_axis_name=['buy_catchup_round', 'sell_catchup_round'], all_parameters=parameters)
        make_plot(all_data=data,prefix=prefix, x_axis_name=parameter_to_sweep, y_axis_name=['max_traded_price_after_step', 'min_traded_price_after_step'], all_parameters=parameters)
        make_plot(all_data=data,prefix=prefix, x_axis_name=parameter_to_sweep, y_axis_name=['traded_price_mean_after_sellbuy_reach_new_fundamental', 'traded_price_median_after_sellbuy_reach_new_fundamental'], all_parameters=parameters)


if __name__ == "__main__":
    run_test_simulation()











