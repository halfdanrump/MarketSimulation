def merge_parameters(dataset, filterpars):
		import importlib
		exp_settings = importlib.import_module('%s_settings'%dataset)
		all_parameters = exp_settings.default_parameters
		all_parameters.update(filterpars)
		return all_parameters.copy()

def helper(kwargs):
	evaluate_simulation_results(**kwargs)

import os
import sys
from IO import load_pickled_generation_dataframe, dataset_paths
#from multiprocessing import Process, Queue
from fitness import evaluate_simulation_results
from multiprocessing import Pool
from utils import make_issue_specific_figure_folder
from other_issues import apply_filters
import numpy as np





if __name__ == "__main__":
	n_times_to_sample = 100
	pool = Pool(24)

	for dataset in ['d9','d10', 'd11']:
		
		fit, par, gen, ids = load_pickled_generation_dataframe(dataset)
		folder = make_issue_specific_figure_folder('tpgraphs_for_filter_simulations', dataset)
		sys.path.append(dataset_paths[dataset])

		masks = apply_filters(dataset, return_masks=True)
		for i, mask in enumerate(masks):
			graph_folder = '%sfilter_%s/'%(folder,i)
			if not os.path.exists(graph_folder): os.makedirs(graph_folder)
			if len(np.where(mask)[0]) > 0:
				sampled_parameters = map(lambda x: par[mask].iloc[x,:], np.random.randint(0,len(par[mask]), n_times_to_sample))
				merged_parameters = map(lambda x: merge_parameters('d9', sampled_parameters[x]), range(n_times_to_sample))
				kwargs = map(lambda x: {'graph_folder':graph_folder, 'generation_number':0, 'reps':range(1), 'autorun':True, 'parameters':x}, merged_parameters)
				pool.map(helper, kwargs)

