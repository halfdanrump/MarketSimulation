def issue_83_example_table():
	import IO
	import utils
	fit, par, gen = IO.load_pickled_generation_dataframe('d3')
	tex_partable = utils.dataframe2latex(par.iloc[range(10),:], 'table:example_dataset_parameters', 'An example data matrix containing the parameters of ten individuals who lived sometime during the execution of the genetic algortihm. In this case, each individual contained paremeters for the number of HFT agents, as well as the latency and thinking time parameters. Hence, the data matrix has a column for each.')
	with open('%sexample_dataset_parameters.tex'%table_save_path, 'w') as f:
			f.write(tex_partable)
	tex_fittable = utils.dataframe2latex(fit.iloc[range(10),:], 'table:example_dataset_fitnesses', 'This table contains the fitness values for each individual in table \\ref{table:example_dataset_parameters}. Note that, in order to increase the reliability of the fitness measure of an individual, the recorded fitness values are the average of the fitnesses obtained by evaluating each individual ten times')		
	with open('%sexample_dataset_fitnesses.tex'%table_save_path, 'w') as f:
			f.write(tex_fittable)
