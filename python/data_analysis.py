from sklearn.cluster import KMeans
from sklearn.decomposition import PCA
from IO import load_all_generations_as_DataFrame
import matplotlib.pyplot as plt
from pandas import DataFrame
import cPickle

temp_storage = '/Users/halfdan/temp/'

def reduce_npoints_kmeans(fit, n_datapoints = 1000, load_from_file = False):
	store_file = temp_storage + 'kmeans.pkl'
	
	if load_from_file:
		print 'Loading kmeans from file...'
		with open(store_file, 'rb') as fid:
			kmeans = cPickle.load(fid)
	else:
		print 'Calculating k-means with %s cluster centers...'%n_datapoints
		kmeans = KMeans(n_clusters = n_datapoints, n_jobs=-1, verbose=1)
		kmeans.fit(fit)
		with open(store_file, 'wb') as fid:
			cPickle.dump(kmeans, fid)    


	return kmeans.cluster_centers_

def run_kmeans(gene_folder, n_clusters):
	pars, fitness = load_all_generations_as_DataFrame(gene_folder)
	kmeans = KMeans(n_clusters=n_clusters)
	kmeans.fit(pars)
	means = map(lambda c: fitness[kmeans.labels_ == c].mean()['longest_interval_within_margin'], range(n_clusters))
	stds = map(lambda c: fitness[kmeans.labels_ == c].std()['longest_interval_within_margin'], range(n_clusters))
	return kmeans, means, stds
	
def visualize_with_PCA(pars = DataFrame(), fit = DataFrame(), gene_folder = None):
	if gene_folder:
		print "Loading data from files..."
		pars, fitness = load_all_generations_as_DataFrame(gene_folder)
	elif not (pars and fit):
		assert False, "Please specify either gene_folder or par and fit dataframes."
	
	pca = PCA(n_components=2, copy=True, whiten=True)
	return pca

