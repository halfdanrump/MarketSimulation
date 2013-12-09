from sklearn.cluster import KMeans
from sklearn.decomposition import PCA
from IO import load_all_generations_as_DataFrame
import IO
#import matplotlib.pyplot as plt
from pandas import DataFrame
import cPickle

dataset_paths = {
	'd1':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/datasets/merged_data/d1_sameLatDist_ssmm40_sc100/',
	'd2':'/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/tex/datasets/merged_data/d2/'
}

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
	



def reduce_npoints_kmeans(data, dataset_name = 'd1', n_datapoints = 1000, load_from_file = False):
	store_file = temp_storage + dataset_name + '_kmeans.pkl'
	
	if load_from_file:
		print 'Loading kmeans from file...'
		with open(store_file, 'rb') as fid:
			kmeans = cPickle.load(fid)
	else:
		print 'Calculating k-means with %s cluster centers...'%n_datapoints
		kmeans = KMeans(n_clusters = n_datapoints, n_jobs=-1, verbose=1)
		kmeans.fit(data)
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

if __name__ == "__main__":
	issue_41(n_clusters=10)