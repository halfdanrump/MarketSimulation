from sklearn.cluster import KMeans
from sklearn.decomposition import PCA
from IO import load_all_generations_as_DataFrame
import IO
#import matplotlib.pyplot as plt
from pandas import DataFrame
import cPickle
import numpy as np
from numpy import mean, std, median


temp_storage = '/Users/halfdan/temp/'


def issue_41(n_clusters, dataset):
	"""
	Calculate clusters for K-means and calculate fitness stats for each cluster
	"""
	fit_data, par_data, gen, ids= IO.load_pickled_generation_dataframe(dataset)
	par_trans, pca, components = calculate_pca(par_data, n_components=4)

	kmeans = KMeans(n_clusters=n_clusters, n_jobs=-1, verbose=0)
	kmeans.fit(par_trans)
	fit_data['label'] = kmeans.labels_
	group = fit_data.groupby('label')
	print "Count with KMmeans\n", group.count()
	print "mean with KMmeans\n", group.mean()
	print "std with KMmeans\n", group.std()
	print "median with KMmeans\n", group.median()
	
def calculate_cluster_stats(dataframe, cluster_labels):
	assert isinstance(dataframe, DataFrame), "Expected pandas DataFrame, but got %s."%type(dataframe)
	dataframe = dataframe.copy()
	dataframe['label'] = cluster_labels
	group = dataframe.groupby('label')
	print "Count with KMmeans\n", group.count()
	print "mean with KMmeans\n", group.mean()
	print "std with KMmeans\n", group.std()
	print "median with KMmeans\n", group.median()
	return group.count(), group.mean(), group.std(), group.median()

	"""
	stats_to_calculate = ['count', 'mean', 'std', 'median']
	stats = dict()
	for cluster in range(len(merged_labels.values())):
		index = 'c%s'%cluster
		stats[index] = DataFrame(columns=stats_to_calculate, index = dataframe.columns)
		for stat in stats_to_calculate: 
			c = getattr(dataframe.iloc[merged_labels[cluster],:], stat)().copy()
			stats[index][stat] = c

	return concat(stats,axis=1), merged_labels
	"""
	
def calculate_stats_for_dataframe(dataframe, labels):
	from pandas import concat, Series
	from numpy import where
	assert isinstance(dataframe, DataFrame), "Expected pandas DataFrame, but got %s."%type(dataframe)
	assert dataframe.shape[0] == labels.shape[0], 'Please pass labels np.array or similar with the same length as the number of rows in the dataframe'
	stat_functions = ['count', 'mean', 'std', 'median', 'max', 'min']
	stat_names = ['Count', 'Mean', 'Std', 'Median', 'Max', 'Min']

	stats = dict()

	distinct_labels = range(max(labels)+1)
	col_names = map(lambda x: 'C%s'%x, distinct_labels)
	count =  Series(dict(zip(col_names,[len(where(labels == g)[0]) for g in distinct_labels])), name = 'Count')
	for stat, stat_name in zip(stat_functions, stat_names): 
		stats[stat_name] = DataFrame(columns = col_names, index = dataframe.columns)
		for cluster in set(labels):	
			c = getattr(dataframe.iloc[labels == cluster,:], stat)().copy()
			stats[stat_name][col_names[cluster]] = c
		stats[stat_name] = stats[stat_name].append(count)
		#print stats[stat_name]

	return stats
	
	#stats = DataFrame([[eval(s)(merged_labels.values()[i]) for s in stats_to_calculate] for i in clusters], columns=stats_to_calculate, index=['c%s'%i for i in clusters])

def reduce_npoints_kmeans(dataframe, dataset_name, data_name, n_datapoints = 1000, load_from_file = False):
	import inspect
	assert isinstance(dataframe, DataFrame), "Expected pandas DataFrame, but got %s."%type(dataframe)
	issue_number = inspect.stack()[1][3]
	store_file = temp_storage + dataset_name + '_' + issue_number + '_' + data_name+ '_kmeans.pkl'
	
	if load_from_file:
		print 'Loading kmeans from file...'
		with open(store_file, 'rb') as fid:
			kmeans = cPickle.load(fid)
	else:
		print 'Calculating k-means with %s cluster centers...'%n_datapoints
		kmeans = KMeans(n_clusters = n_datapoints, verbose=1)
		kmeans.fit(dataframe.values)
		with open(store_file, 'wb') as fid:
			cPickle.dump(kmeans, fid)
			print 'Pickling KMeans object to file %s'%store_file
	
	clusters = DataFrame(kmeans.cluster_centers_, columns = dataframe.columns)
	labels = kmeans.labels_
	return clusters, labels, kmeans

def run_kmeans(gene_folder, n_clusters):
	pars, fitness = load_all_generations_as_DataFrame(gene_folder)
	kmeans = KMeans(n_clusters=n_clusters)
	kmeans.fit(pars)
	means = map(lambda c: fitness[kmeans.labels_ == c].mean()['longest_interval_within_margin'], range(n_clusters))
	stds = map(lambda c: fitness[kmeans.labels_ == c].std()['longest_interval_within_margin'], range(n_clusters))
	return kmeans, means, stds
	
def calculate_pca(dataframe, n_components, whiten = False, normalize = True, verbose = False):
	assert isinstance(dataframe, DataFrame), "Expected pandas DataFrame, but got %s."%type(dataframe)
	from sklearn.preprocessing import scale
	pca = PCA(n_components=n_components, whiten=whiten)

	if normalize:
		data = scale(dataframe)
	else:
		data = dataframe.values
	transformed_data = pca.fit_transform(data)
	components = DataFrame(pca.components_, columns = dataframe.columns)	
	
	column_names = ['d%s'%i for i in range(1, n_components + 1)]
	transformed_data = DataFrame(transformed_data, columns = column_names)
	if verbose: print components
	return transformed_data, pca, components

def outlier_detection_with_SVM(dataframe, kernel, gamma, outlier_percentage):
	"""
	Note that the SVM parameters are higly sensitive to the dataset, so they have to be manually selected for each dataset
	"""
	assert isinstance(dataframe, DataFrame), "Expected pandas DataFrame, but got %s."%type(dataframe)
	from scipy.stats import scoreatpercentile
	from sklearn import svm
	svm = svm.OneClassSVM(kernel=kernel, gamma=gamma)
	
	points = dataframe.values
	svm.fit(points)
	assignment = svm.decision_function(points)
	score = scoreatpercentile(assignment.ravel(), 1 - outlier_percentage)
	
	inliers_idx, dummy = np.where(assignment <= score)
	outliers_idx, dummy = np.where(assignment > score)
	
	print "%s inliers and %s outliers"%(len(inliers_idx), len(outliers_idx))
	return inliers_idx, outliers_idx

if __name__ == "__main__":
	issue_41(n_clusters=10)