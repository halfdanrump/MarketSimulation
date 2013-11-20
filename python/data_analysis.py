from sklearn.cluster import KMeans
from sklearn.decomposition import PCA
from IO import load_all_generations_as_DataFrame
import matplotlib.pyplot as plt
from pandas import DataFrame


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

