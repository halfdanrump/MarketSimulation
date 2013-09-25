from subprocess import Popen
import numpy as np
import pdb
from os import devnull
if __name__=='__main__':
    


    parameters = dict()
    jar_path = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/Simulation.jar'
    log_root_folder = '/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/logs/'
    
    # parameters['experimentName'] = 'whatever'
    parameters['nRounds'] = 30000
    
    parameters['minLat'] = 1
    parameters['maxLat'] = 100
    parameters['minThink'] = 1
    parameters['maxThink'] = 1
            
    
    parameters['ssmm_MinSpread'] = 2
    parameters['ssmm_MaxSpread'] = 10
    
    parameters['sc_timeHorizonMin'] = 1000
    parameters['sc_timeHorizonMax'] = 20000
    parameters['sc_ticksBeforeReactingMin'] = 2
    parameters['sc_ticksBeforeReactingMax'] = 5
    parameters['sc_priceTickSizeMin'] = 1
    parameters['sc_priceTickSizeMax'] = 1
    parameters['sc_waitTimeBetweenTradingMin'] = 10
    parameters['sc_waitTimeBetweenTradingMax'] = 100
    ### store all the parameters in the dict
    
    ### Create data file
    ssmm_nAgents_range = range(0,2)
    sc_nAgents_range = range(0,2)
    
    n_reps = 2

    n_data_rows = len(ssmm_nAgents_range) * len(sc_nAgents_range)

    data_to_calculate = [
                        ('buy_catchup_round', float),
                        ('sell_catchup_round', float),
                        ('max_traded_price',float),
                        ('min_traded_price', float),
                        ('largest_100_change', float),
                        ('largest_1000_change', float), ## Calculate with np.diff(n=1000)
                        ('traded_price_std_after_sellbuy_reach_new_fundamental', float)
                        ]

    parameters_to_store = [('ssmm_nAgents', int), ('sc_nAgents', int)]
    ### Create empty matrix where the left columns are the parameters, and the right columns are the collected data values
    all_data = np.zeros(shape=(n_data_rows), dtype=parameters_to_store + data_to_calculate)
    
    counter = 0

    for ssmm_nAgents in ssmm_nAgents_range:
        for sc_nAgents in sc_nAgents_range:
            parameters['ssmm_nAgents'] = ssmm_nAgents
            parameters['sc_nAgents'] = sc_nAgents
           
            rep_data = np.zeros(shape=(n_reps), dtype = data_to_calculate) 
            processes = list()
            
            log_folders = [log_root_folder + str(hash(repr(sorted(parameters.items())))) + '/%s/'%rep for rep in xrange(n_reps)]
            print log_folders    
            for rep in xrange(n_reps):
                
                par_string = ''
                for (par, val) in parameters.items(): par_string += '-D%s=%s '%(par,val)
                vm_args = "java -d64 -Xms512m -Xmx4g -DlogFolder=%s "%log_folders[rep]
                command = vm_args + par_string + ' -jar %s'%jar_path
                print 'COMAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAND:%s'%command
                ### Run simulation
                processes.append(Popen(command.split(' ')))
            
            for p in processes:    
                p.wait()
        
            print "Reading files"

            for rep in xrange(n_reps):                       
                ### Read files        
                ob_round_based = np.genfromtxt(log_folders[rep] + 'columnLog_roundBased_orderbook(0,0).csv', names=True, dtype=int, delimiter=',', usecols=(4,5))
                stock_round_based = np.genfromtxt(log_folders[rep] + 'columnLog_roundBased_stock0.csv', names=True, dtype=int, delimiter=',', usecols=(0,1))
                trades = np.genfromtxt(log_folders[rep] + 'columnLog_transactionBased_stock0.csv', names=True, dtype=int, delimiter=',', usecols=(0,2))

                fundamental_step_round = np.where(np.diff(stock_round_based['fundamental'], n=1) != 0)[0]
                fundamental_after_step = stock_round_based['fundamental'][fundamental_step_round + 1]
                
                ### Collect data
                try:
                    rep_data['buy_catchup_round'][rep] = np.min(np.where(ob_round_based['bestStandingBuyPrice'] == fundamental_after_step))
                except ValueError:
                    ### Happens when the buy price never catches up to the fundamental
                    rep_data['buy_catchup_round'][rep] = None
                try:
                    rep_data['sell_catchup_round'][rep] = np.min(np.where(ob_round_based['bestStandingSellPrice'] == fundamental_after_step))
                except ValueError:
                    ### Happens when the sell price never catches up to the fundamental
                    rep_data['sell_catchup_round'][rep] = None

                
                i = np.min(np.where(trades['round'] > fundamental_step_round))
                rep_data['max_traded_price'][rep] = np.max(trades['price'][i::])
                rep_data['min_traded_price'][rep] = np.min(trades['price'][i::])
                
                rep_data['traded_price_std_after_sellbuy_reach_new_fundamental'][rep] = np.std(trades['price'][i::])
                rep_data['traded_price_std_after_sellbuy_reach_new_fundamental'][rep] = np.std(trades['price'][i::])
                diff_100 = np.diff(ob_round_based['bestStandingBuyPrice'],  n=100)

                if np.max(diff_100) > np.abs(np.min(diff_100)):
                    rep_data['largest_100_change'][rep] = np.max(diff_100)
                else:
                    rep_data['largest_100_change'][rep] = np.min(diff_100)
                
                """
                diff_1000 = np.diff(ob_round_based['bestStandingBuyPrice'],  n=1000)
                if np.max(diff_1000) > np.abs(np.min(diff_1000)):
                    rep_data['largest_1000_change'][rep] = np.max(diff_1000)
                else:
                    rep_data['largest_1000_change'][rep] = np.min(diff_1000)
                """

                #
                #print np.min(np.where(ob_round_based['bestStandingSellPrice'] == fundamental_after_step))
            print rep_data
            mean_data = [np.mean(rep_data[d]) for d in rep_data.dtype.names]
            param_data = [eval(p[0]) for p in parameters_to_store]
            
            all_data[counter] = tuple(param_data + mean_data)
            print all_data
            #pdb.set_trace()
            counter += 1
            np.save('data', all_data)
                
            #f = open(parameters['log_folder'] + "columnLog_roundBased_orderbook(0,0).csv")
            #orderbookData = array([d.split(',') for d in f.read().split('\n')]) 

            ### Calculate stats
            ### append stats to data file




#Read csv of orderbook file to get best traded prices
#read csv of stock file to get fundamental price


#Things to calculate
#Number of rounds between change of fundamental to best trade prices reach new fundamental
#max and min traded prices before fundamental changes
#max and min traded prices after fundamental change

















