import settings

def check_simulation_complete(full_simulation_log_path):
    try:
        with open(full_simulation_log_path + 'finished.txt'): 
            print "Found completed data: %s"%full_simulation_log_path
            return True
    except IOError:
        return False


def get_logfolder(parameters = {}, rep = ""):
	assert parameters, "Please specify parameters"
	assert rep, "Please specify repetition"
 	return settings.log_root_folder + str(hash(repr(sorted(parameters.items())))) + '/%s/'%rep