from sklearn.cluster import KMeans
from utils import load_all_generations_as_DataFrame


def run_kmeans(gene_folder, n_clusters):
	pars, fitness = load_all_generations_as_DataFrame(gene_folder)
	kmeans = KMeans(n_clusters=n_clusters)
	kmeans.fit(pars)
	means = map(lambda c: fitness[kmeans.labels_ == c].mean()['longest_interval_within_margin'], range(n_clusters))
	stds = map(lambda c: fitness[kmeans.labels_ == c].std()['longest_interval_within_margin'], range(n_clusters))
	print means, stds
	
