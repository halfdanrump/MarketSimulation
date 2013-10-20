import settings
from collections import Iterable

def check_simulation_complete(full_simulation_log_path):
    try:
        with open(full_simulation_log_path + 'finished.txt'): 
            print "Found completed data: %s"%full_simulation_log_path
            return True
    except IOError:
        return False


def get_logfolders(parameters = {}, rep = []):
	assert parameters, "Please specify parameters"
	assert rep, "Please specify repetition"
	assert isinstance(rep, Iterable), "rep must be an iterable"
 	return [settings.log_root_folder + str(hash(repr(sorted(parameters.items())))) + '/%s/'%r for r in rep]