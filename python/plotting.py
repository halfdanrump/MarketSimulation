import numpy as np
import matplotlib.pyplot as plt
import matplotlib as mpl
import textwrap
from datetime import datetime
import settings
import gc
from utils import get_fundamental_after_shock
#import IO
import brewer2mpl
from ppl import Ppl
#import string
import pandas
from numpy import log, sqrt
def how_to_make_plot():

    fig = plt.figure()
    fig.subplots_adjust(top=0.9)
    ax1 = fig.add_subplot(211)
    ax1.set_ylabel('volts')
    ax1.set_title('a sine wave')

    t = np.arange(0.0, 1.0, 0.01)
    s = np.sin(2*np.pi*t)
    line, = ax1.plot(t, s, color='blue', lw=2)

    #ax2 = fig.add_axes([0.15, 0.1, 0.7, 0.3])
    ax2 = fig.add_subplot(2,1,2)
    n, bins, patches = ax2.hist(np.random.randn(1000), 50, facecolor='yellow', edgecolor='yellow')
    ax2.set_xlabel('time (s)')

    txt = '''
    Lorem ipsum dolor sit amet, consectetur adipisicing elit,
    sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
    Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris
    nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in
    reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla
    pariatur. Excepteur sint occaecat cupidatat non proident, sunt in
    culpa qui officia deserunt mollit anim id est laborum.'''

    fig.text(.1,.1,txt)
    plt.savefig("test.png")

def get_epoch_time():
    td = datetime.now() - datetime.utcfromtimestamp(0)
    return repr(int(td.total_seconds() * 10**6))

def make_tradeprice_plot(rounds, tradePrice, all_parameters, graph_folder, fitness, generation_number, plot_name = None):
    fig = plt.figure(figsize=(10, 8), dpi=100)
    ax = fig.add_axes([0.1, 0.3, 0.8, 0.6])
    ax.set_xlabel("Round")
    ax.set_ylabel("Traded price")

    caption = "\n".join(textwrap.wrap(repr([(k,all_parameters[k]) for k in settings.parameters_in_genes]), 100))
    caption += '\nFitness: %s'%str(settings.fitness_weights.keys())
    caption += '\nFitness: %s'%str(fitness)
    fig.text(0.1,0.1, caption)
    ax.plot(rounds, tradePrice, lw=2)

    fas = get_fundamental_after_shock()
    ax.hlines([fas - settings.stability_margin, fas + settings.stability_margin], 0, settings.n_simulation_rounds)
    time = get_epoch_time()
    if not plot_name:
        identifier = graph_folder + 'gen%s_'%generation_number + time
        full_plot_name = identifier + '.png'
    else:
        full_plot_name = graph_folder + plot_name + '___%s'%time
        print full_plot_name
    
    print "Saving plot to %s"%full_plot_name
    fig.savefig(full_plot_name)

    pars_in_gene = dict([(k, all_parameters[k]) for k in settings.parameters_in_genes])
    fit = dict(zip(list(fitness.dtype.names), list(fitness.item())))
    if settings.SAVE_DATA_USED_FOR_PLOTTING:     
        data = {'rounds':rounds, 'tradePrice':tradePrice, 'fitness': fit, 'parameters':pars_in_gene}
        np.savez_compressed(identifier, data)

    plt.close()
    gc.collect()

def make_pretty_tradeprice_plot(path_to_npz_file):
    import IO
    rounds, prices, fit, par = IO.load_tradeprice_data_with_parameters(path_to_npz_file)
    cmap = brewer2mpl.get_map('Set1', 'qualitative', 9)
    p = Ppl(cmap, alpha=1)
    fig, ax = plt.subplots(1)    
    p.plot(ax, rounds, prices)

    filename = path_to_npz_file.replace('.npz', '.png')
    fas = get_fundamental_after_shock()
    ax.hlines([fas - settings.stability_margin, fas + settings.stability_margin], 0, settings.n_simulation_rounds)
    ax.set_ylabel('Ticks')
    ax.set_xlabel('Rounds')
    fig.savefig(filename)

def make_pretty_generation_plot(filename, generations, lines_to_plot, x_axis_name, legend_labels):
    cmap = brewer2mpl.get_map('Set1', 'qualitative', 9)
    p = Ppl(cmap, alpha=1)
    fig, ax = plt.subplots(1)
    ax.yaxis.get_major_formatter().set_powerlimits((0, 1))
    ax.set_xlabel('Generation')
    ax.set_ylabel(x_axis_name)
    for series, label in zip(lines_to_plot, legend_labels):
        print len(generations)
        print series.values.shape
        p.plot(ax, generations, series, linewidth=2, label=label)
    p.legend(ax)
    fig.savefig(filename)

def make_color_grouped_scatter_plot(data_frame, x_name, y_name, color_by, filename, colormap, x_function = 'dummy', y_function = 'dummy', color_function = 'dummy', legend = False, colorbar = True):
    ### Originally created for issue_21
    def dummy(a): return a
    data_frame = data_frame.copy()
    p = Ppl(colormap, alpha=1)

    fig, ax = plt.subplots(1)
    #ax.set_autoscale_on(False)
    ax.set_xlim([eval(x_function)(min(data_frame[x_name])), eval(x_function)(max(data_frame[x_name]))])
    ax.set_ylim([eval(y_function)(min(data_frame[y_name])), eval(y_function)(max(data_frame[y_name]))])
    x_label = x_name.capitalize().replace('_', ' ')
    if x_function == 'log': x_label += ' (log)'
    y_label = y_name.capitalize().replace('_', ' ')
    if y_function == 'log': y_label += ' (log)'
    ax.set_xlabel(x_label)
    ax.set_ylabel(y_label)
    ax.xaxis.get_major_formatter().set_powerlimits((0, 1))
    ax.yaxis.get_major_formatter().set_powerlimits((0, 1))
    # Show the whole color range
    n_intervals = len(colormap.colors)
    if color_function == 'log': bins = np.logspace(np.log10( data_frame[color_by].min()), np.log10(data_frame[color_by].max()), n_intervals + 1, base = 10)
    else: bins = np.linspace(eval(color_function)(data_frame[color_by].min()), eval(color_function)(data_frame[color_by].max()), n_intervals + 1)
        
    data_frame['groups'] = pandas.cut(data_frame[color_by], bins=bins, labels = False)
    groups = pandas.cut(data_frame[color_by], bins=bins)
    bounds = []

    
    for g in range(n_intervals):
        x = eval(x_function)(data_frame[data_frame.groups == g][x_name])
        y = eval(y_function)(data_frame[data_frame.groups == g][y_name])
        p.scatter(ax, x, y, label=str(groups.levels[g]))

    if legend: p.legend(ax)
    #ax.set_title('prettyplotlib `scatter` example\nshowing default color cycle and scatter params')
    
    bounds = bins
    if colorbar:
        cmap = p.get_colormap().mpl_colormap
        
        norm = mpl.colors.BoundaryNorm(bounds, cmap.N)
        ax2 = fig.add_axes([0.2, 0.94 , 0.7, 0.03])
        ax2.set_ylabel(color_by.capitalize().replace('_', ' '))
        cbar = mpl.colorbar.ColorbarBase(ax2, cmap=cmap, spacing='proportional', ticks=bounds, norm=norm, alpha=p.get_alpha(), orientation='horizontal')
        cbar.ax.set_xticklabels([str(int(t)) for t in bounds])# vertically oriented colorbar

    fig.savefig(filename)


def make_scatter_plot_for_labelled_data(data_frame, x_name, y_name, labels, filename, colormap, x_function = 'dummy', y_function = 'dummy', legend = False):
    ### Originally created for issue_28
    def dummy(a): return a
    p = Ppl(colormap, alpha=1)

    fig, ax = plt.subplots(1)
    #ax.set_autoscale_on(False)
    ax.set_xlim([eval(x_function)(min(data_frame[x_name])), eval(x_function)(max(data_frame[x_name]))])
    ax.set_ylim([eval(y_function)(min(data_frame[y_name])), eval(y_function)(max(data_frame[y_name]))])
    x_label = x_name.capitalize().replace('_', ' ')
    if x_function == 'log': x_label += ' (log)'
    y_label = y_name.capitalize().replace('_', ' ')
    if y_function == 'log': y_label += ' (log)'
    ax.set_xlabel(x_label)
    ax.set_ylabel(y_label)
    ax.xaxis.get_major_formatter().set_powerlimits((0, 1))
    ax.yaxis.get_major_formatter().set_powerlimits((0, 1))
    # Show the whole color range
    
    n_labels  = labels.max()

    for g in range(n_labels + 1):
        x = eval(x_function)(data_frame[labels == g][x_name])
        y = eval(y_function)(data_frame[labels == g][y_name])
        p.scatter(ax, x, y, label=str(len(labels[labels == g])))

    if legend: p.legend(ax)
    #ax.set_title('prettyplotlib `scatter` example\nshowing default color cycle and scatter params')
    

    fig.savefig(filename)


"""
def save_line_plot(all_data, prefix, x_axis_name = "", y_axis_name = [""], all_parameters = {}):
    assert x_axis_name in all_data.dtype.names, "x axis parameter not found"
    for y_name in y_axis_name: assert y_name in all_data.dtype.names, "y axis parameter not found"
    assert all_parameters

    fig = plot.figure(figsize=(10, 8), dpi=100)
    ax = fig.add_axes([0.1, 0.3, 0.8, 0.6])
    ax.set_xlabel(x_axis_name)
    ax.set_ylabel(repr(y_axis_name))
    
    for i, y_name in enumerate(y_axis_name):
        plot.plot(all_data[x_axis_name],all_data[y_name], lw=2)
        plot.legend(y_axis_name)
    
    
    caption = "\n".join(textwrap.wrap(repr([(k,all_parameters[k]) for k in sorted(all_parameters)]), 100))
    fig.text(0.1,0.1, caption)
    #plt.text(caption)
    
    graph_filename = "%s_vs_%s.pdf"%(x_axis_name, repr(y_axis_name))
    plot.savefig(settings.graph_root_folder + prefix + graph_filename)
"""
