from sklearn.cluster import KMeans
from sklearn.decomposition import PCA
from IO import load_all_generations_as_DataFrame, dataset_paths
import IO
#import matplotlib.pyplot as plt
from pandas import DataFrame, concat
import cPickle
import numpy as np
from numpy import mean, std, median


temp_storage = '/Users/halfdan/temp/'


def issue_41(n_clusters, dataset = 'd1'):
	"""
	Calculate clusters for K-means and calculate fitness stats for each cluster
	"""
	datapath = dataset_paths[dataset]
	par_data, gen = IO.load_pickled_generation_dataframe(datapath + 'pars.pandas')
	fit_data, gen = IO.load_pickled_generation_dataframe(datapath + 'fits.pandas')
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

def calculate_cluster_stats_for_reduced_dataset(dataframe, inlier_clusters, labels_reduced, labels_full):
	assert isinstance(dataframe, DataFrame), "Expected pandas DataFrame, but got %s."%type(dataframe)

	merged_labels = dict()
	for k in range(max(labels_reduced) + 1): merged_labels[k] = list()
	
	labels_full = DataFrame(labels_full, columns=['l'])
	group_indices = labels_full.groupby('l').indices
	for idx, cluster in enumerate(inlier_clusters):
		member_points = group_indices[cluster]
		merged_labels[labels_reduced[idx]].append(np.ravel(member_points))
	for k, v in merged_labels.iteritems(): merged_labels[k] = np.concatenate(v)
	
	return merged_labels
	stats_to_calculate = ['count', 'mean', 'std', 'median']
	stats = dict()
	for cluster in range(len(merged_labels.values())):
		index = 'c%s'%cluster
		stats[index] = DataFrame(columns=stats_to_calculate, index = dataframe.columns)
		for stat in stats_to_calculate: 
			c = getattr(dataframe.iloc[merged_labels[cluster],:], stat)().copy()
			stats[index][stat] = c

	return concat(stats,axis=1), merged_labels

def calculate_stats_for_labelled_dataframe(dataframe, labels):
	from pandas import concat
	assert isinstance(dataframe, DataFrame), "Expected pandas DataFrame, but got %s."%type(dataframe)
	assert dataframe.shape[0] == labels.shape[0], 'Please pass labels np.array or similar with the same length as the number of rows in the dataframe'
	stats_to_calculate = ['count', 'mean', 'std', 'median']
	stats = dict()
	for cluster in set(labels):
		index = 'c%s'%cluster
		stats[index] = DataFrame(columns=stats_to_calculate, index = dataframe.columns)
		for stat in stats_to_calculate: 
			c = getattr(dataframe.iloc[labels == cluster,:], stat)()
			stats[index][stat] = c

	return concat(stats,axis=1)
	
	#stats = DataFrame([[eval(s)(merged_labels.values()[i]) for s in stats_to_calculate] for i in clusters], columns=stats_to_calculate, index=['c%s'%i for i in clusters])

def reduce_npoints_kmeans(dataframe, dataset_name, n_datapoints = 1000, load_from_file = False):
	import inspect
	assert isinstance(dataframe, DataFrame), "Expected pandas DataFrame, but got %s."%type(dataframe)
	issue_number = inspect.stack()[1][3]
	store_file = temp_storage + dataset_name + '_' + issue_number + '_kmeans.pkl'
	
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
	return clusters, labels

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
	inliers = DataFrame(points[inliers_idx,:], columns=dataframe.columns)
	outliers = DataFrame(points[outliers_idx, :], columns=dataframe.columns)
	
	print "%s outlisers and %s inliers"%(len(inliers), len(outliers))
	return inliers, outliers, inliers_idx, outliers_idx

if __name__ == "__main__":
	issue_41(n_clusters=10)