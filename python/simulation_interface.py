#import sys
from subprocess import Popen
#import pdb
from os import devnull
#from pprint import pprint
import copy
from collections import Iterable
import shutil
import settings
import IO
import numpy as np
#import dataAnalysis


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


"""
def run_simulation_for_all_parameter_combinations(parameter_ranges, reps):
    assert isinstance(reps, Iterable), "Please specify an iterable for reps"
    for parameters in generate_parameter_combinations(parameter_ranges):
        run_simulation(parameters, reps)
"""


def run_simulation(parameters, reps, random_path):
    assert check_parameters(parameters), "Invalid simulation parameters!"

    processes = list()
    log_folders = IO.get_logfolders(parameters, reps, random_path)
    for i, rep in enumerate(reps):
        ### Remove old simulation data calculated with the same parameters
        shutil.rmtree(log_folders[i], ignore_errors=True)
        parameters.update({'nRounds':settings.n_simulation_rounds})
        ### Build parameter string for java program
        par_string = ''
        for (par, val) in parameters.items(): par_string += '-D%s=%s '%(par,val)
        vm_args = "java -d64 -Xms512m -Xmx4g -DlogFolder=%s "%log_folders[i]
        command = vm_args + par_string + '-jar %s'%settings.jar_path
        print "Running simulation with command: %s\n"%command
        if settings.WITH_SIMULATION_OUTPUT:
            processes.append(Popen(command.split(' ')))            
        else:
            processes.append(Popen(command.split(' '), stdout=open(devnull, 'w')))            
    for p in processes:
        p.wait()




def check_parameters(parameters):
    assert isinstance(parameters, Iterable)
    return np.all([isinstance(k, int) for k in parameters.values()])




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

"""
def get_graph_folder(log_root_folder, graph_type = 'line_chart'):
    graph_folder = "%s/%s/"%(graph_root_folder, graph_type)
    print "Graph folder: %s"%graph_folder
    return graph_folder
"""
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


