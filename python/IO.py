import settings
from collections import Iterable
from shutil import rmtree
#from shutils import rmtree

def check_simulation_complete(full_simulation_log_path):
    try:
        with open(full_simulation_log_path + 'finished.txt'): 
            print "Found completed data: %s"%full_simulation_log_path
            return True
    except IOError:
        return False

def delete_simulation_data(log_folder):
	rmtree(log_folder)

def get_logfolders(parameters = {}, rep = [], random_path = ""):
	assert parameters, "Please specify parameters"
	assert rep, "Please specify repetition"
	assert isinstance(rep, Iterable), "rep must be an iterable"
	return [settings.log_root_folder + str(hash(repr(sorted(parameters.items())))) + '/%s/%s/'%(random_path, r) for r in rep]