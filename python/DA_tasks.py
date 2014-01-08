from data_analysis import reduce_npoints_kmeans, outlier_detection_with_SVM
from sklearn.cluster import KMeans
from utils import get_group_vector_for_reduced_dataset

def reduce_outlier_cluster(data, n_clusters=8,dataset = 'd9', data_name='fitness', kernel='rbf', gamma=0.01, load_from_file=True):
		reduced, cluster_assignment_o2r, km_r = reduce_npoints_kmeans(data, dataset, data_name, n_datapoints=1000, load_from_file=load_from_file)	
		inliers_idx_r, outliers_idx_r = outlier_detection_with_SVM(reduced, kernel='rbf', gamma=gamma, outlier_percentage=0.01)
		kmeans = KMeans(n_clusters = n_clusters)
		kmeans.fit(reduced.iloc[inliers_idx_r, :])
		indexes_i, labels_i =  get_group_vector_for_reduced_dataset(inliers_idx_r, cluster_assignment_o2r, cluster_assignment_r2g = kmeans.labels_)
		return indexes_i, labels_i

def pca_and_plot():
	pass

def 