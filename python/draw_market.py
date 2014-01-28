import networkx as nx
import pickle
import matplotlib.pyplot as plt
import numpy as np
import brewer2mpl

plt.rc('text', usetex=True)
plt.rc('font', family='serif')
plt.rc('font', size=20)

def mkgraph():
	sizes = dict()
	color = dict()

	cmap=brewer2mpl.get_map('Set2', 'qualitative', 3).mpl_colors

	G = nx.Graph()
	G.add_node('Market', node_color = cmap[0], node_size = 40)
	sizes.update({'Market':10000})
	color.update({'Market':'green'})

	market_makers = ['MM%s'%n for n in range(5)]
	chartists = ['C%s'%n for n in range(5)]

	print "~~~Inserting Nodes."


	for a in market_makers:
		G.add_node(a, node_color = cmap[1], node_size = 10)
		G.add_edge(a, 'Market', weight=10000)#np.random.normal(10000, 500))
		sizes.update({a:3000})
		color.update({a:'red'})

	for a in chartists:
		G.add_node(a, node_color = cmap[2], node_size = 10)
		G.add_edge(a, 'Market', weight=10000)#np.random.normal(10000, 500))
		sizes.update({a:3000})
		color.update({a:'blue'})

	plt.figure()
	nx.draw_circular(G, node_color = map(lambda x: color[x], G.nodes()), node_size = map(lambda x: sizes[x], G.nodes()), edge_color='black', width=1, edge_cmap=plt.cm.Blues, with_labels=True, alpha = 0.8)
	#pos = nx.circular_layout(G)
	#nx.draw_networkx_nodes(G, pos, node_color = map(lambda x: color[x], G.nodes()), node_size = map(lambda x: sizes[x], G.nodes()), alpha = 0.8)
	#nx.draw_networkx_labels(G,pos,G.nodes(),font_size=16)
	plt.savefig("graph.png", dpi=500, facecolor='w', edgecolor='w',orientation='portrait', papertype=None, format=None,transparent=False, bbox_inches=None, pad_inches=0.1) 
	return G, sizes

def mkgraph2():
	pass