import subprocess

if __name__=='__main__':
    parameters = dict()
    parameters['root_folder'] = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/'
    parameters['jar_path'] = '/Users/halfdan/Dropbox/Waseda/Research/JavaTest/Simulation.jar'
    parameters['experiment_name'] = 'whatever'
    parameters['ssmm_nAgents'] = 30
    parameters['sc_nAgents'] = 100
    
    command = "java -DrootFolder=%(root_folder)s
                    -DexperimentName%(experiment_name)s
                    -Dssmm_nAgents=%(ssmm_nAgents)s
                    -Dsc_nAgents=%(sc_nAgents)s
                    -jar %(jar_path)s" % parameters
    
    print command
    subprocess.call(command.split(' '))


