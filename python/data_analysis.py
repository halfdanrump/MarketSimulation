from sklearn.cluster import KMeans
from sklearn.decomposition import PCA
from IO import load_all_generations_as_DataFrame
#import matplotlib.pyplot as plt
from pandas import DataFrame
import cPickle

temp_storage = '/Users/halfdan/temp/'

def reduce_npoints_kmeans(dataset, fit, n_datapoints = 1000, load_from_file = False):
	store_file = temp_storage + dataset + '_kmeans.pkl'
	
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
	
def calculate_pca(dataframe, n_components, whiten = False, normalize = True):
	from sklearn.preprocessing import scale
	pca = PCA(n_components=n_components, whiten=whiten)

	if normalize:
		data = scale(dataframe)
	else:
		data = dataframe.values
	transformed_data = pca.fit_transform(data)
	components = DataFrame(pca.components_, columns = dataframe.columns)	
	print components
	return transformed_data, pca, components