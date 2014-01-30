import numpy as np
import matplotlib.pyplot as plt
import matplotlib as mpl
import textwrap
from pandas import DataFrame
import settings
import gc
from utils import get_fundamental_after_shock, pfn
#import IO
import brewer2mpl
from ppl import Ppl
#import string
import pandas
from numpy import log
from utils import format_as_latex_parameter as fl

errorbar_color=brewer2mpl.get_map('Set1', 'qualitative', 9).mpl_colors[8]

#fitness_latex = DataFrame({
#    'time_to_reach_new_fundamental' : '$f_\\text{t}$'
#})

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



def make_tradeprice_plot(rounds, prices, fit, par, filename):
    fig = plt.figure(figsize=(10, 8), dpi=100)
    ax = fig.add_axes([0.1, 0.3, 0.8, 0.6])
    ax.set_xlabel("Round")
    ax.set_ylabel("Traded price")

    caption = "\n".join(textwrap.wrap(repr([(k,par[k]) for k in settings.parameters_in_genes]), 100))
    caption += '\nFitness: %s'%str(settings.fitness_weights.keys())
    caption += '\nFitness: %s'%str(fit)
    fig.text(0.1,0.1, caption)
    ax.plot(rounds, prices, lw=2)

    fas = get_fundamental_after_shock()
    ax.hlines([fas - settings.stability_margin, fas + settings.stability_margin], 0, settings.n_simulation_rounds)
    
    print "Saving plot to %s"%filename
    fig.savefig(filename)

    plt.close()
    gc.collect()



def make_pretty_tradeprice_plot(rounds, prices, filename, format = 'png', **figargs):
    from settings import default_parameters as dp
    cmap = brewer2mpl.get_map('Set1', 'qualitative', 9)
    p = Ppl(cmap, alpha=1)
    fig = plt.figure(num=None, **figargs)
    ax = fig.add_subplot(1,1,1)
    #fig, ax = plt.subplots(1)
    """
    if kwargs.has_key('dpi'):
        fig.set_dpi(kwargs['dpi'])
    if kwargs.has_key('figwidth'):
        fig.set_figwidth(kwargs['figwidth'])
    if kwargs.has_key('figheight'):
        fig.set_figheight(kwargs['figheight'])
    """
    p.plot(ax, rounds, prices, zorder=1)
    
    #ax.yaxis.get_major_formatter().set_powerlimits((0, 1))
    ### Plotting statbility margins
    fas = get_fundamental_after_shock()
    ax.hlines(y = [fas - settings.stability_margin, fas + settings.stability_margin], xmin=0, xmax=settings.n_simulation_rounds, linestyles = 'dashed')
    ### Plotting fundamental step function
    y = [dp['fundamental_initial_value'], dp['fundamental_initial_value'] + dp['fundamental_shock_size'], ]
    xmin = [0, dp['fundamental_shock_round']]
    xmax = [dp['fundamental_shock_round'], settings.n_simulation_rounds]  
    
    ax.hlines(y, xmin, xmax, linestyles = 'solid', colors='black', linewidth=2, zorder=2)
    ax.vlines(x = dp['fundamental_shock_round'], ymin=dp['fundamental_initial_value'] + dp['fundamental_shock_size'], ymax = dp['fundamental_initial_value'], colors='black', linewidth=2, zorder=3)

    ax.set_ylabel('Traded price (ticks)')
    ax.set_xlabel('Time (rounds)')
    ax.set_xlim([0, max(rounds)])
    ax.set_ylim([min(fas - settings.stability_margin-2, min(prices)), max(fas + settings.stability_margin, max(prices))])
    fig.savefig(filename)
    plt.close()
    gc.collect()

def get_pretty_xy_plot(x, y, xlabel, ylabel, filename, y_errorbar=None, save_figure = True):
    cmap = brewer2mpl.get_map('Set1', 'qualitative', 9)
    p = Ppl(cmap, alpha=1)
    fig, ax = plt.subplots(1)    
    ax.set_xlabel(pfn(xlabel))
    ax.set_ylabel(pfn(ylabel))
    ax.yaxis.get_major_formatter().set_powerlimits((0, 1))
    print "Errorbar: %s"%y_errorbar
    if y_errorbar is not None: ax.errorbar(x, y, yerr=y_errorbar, fmt='o')
    p.plot(ax, x, y, linewidth=2,)
    if save_figure: fig.savefig(filename)
    return ax, fig

def multiline_xy_plot(x, ys, xlabel, ylabel, legend_labels, filename, y_errorbars=None, save_figure = True):
    assert isinstance(ys, list)
    cmap = brewer2mpl.get_map('Set1', 'qualitative', 9)
    p = Ppl(cmap, alpha=1)
    fig, ax = plt.subplots(1)    
    ax.set_xlabel(fl(xlabel))
    ax.set_ylabel(fl(ylabel))
    ax.yaxis.get_major_formatter().set_powerlimits((0, 1))
    for i, y in enumerate(ys):
        #print "Errorbar: %s"%y_errorbar
        #if y_errorbars is not None: ax.errorbar(x, y, yerr=y_errorbar, fmt='o')
        print legend_labels[i]
        p.plot(ax, x, y, linewidth=2, label = legend_labels[i])
    p.legend(ax, loc=0, frameon = False)

    if save_figure: fig.savefig(filename)
    return ax, fig

def make_pretty_scatter_plot(x, y, xlabel, ylabel, filename, ax=None, fig=None):
    cmap = brewer2mpl.get_map('Set1', 'qualitative', 9)
    p = Ppl(cmap, alpha=0.3)
    if (not ax) and (not fig):
        fig, ax = plt.subplots(1)    
    ax.set_xlabel(pfn(xlabel))
    ax.set_ylabel(pfn(ylabel))
    ax.set_xlim([min(x), max(x)])
    ax.set_ylim([min(y), max(y)])
    ax.yaxis.get_major_formatter().set_powerlimits((0, 1))

    p.scatter(ax, x, y, s=5, linewidth=0)

    fig.savefig(filename)


def make_pretty_generation_plot(filename, generations, lines_to_plot, x_axis_name, legend_labels, y_errorbar=None, y_logscale = False, vline_x = [], save_figure = True):
    if y_errorbar:
        assert isinstance(y_errorbar, list), "Please provide a list for error bars"
        assert len(y_errorbar) == len(lines_to_plot), "When plotting error bars you must specify an array of error bars for each line that is plotted"
    

    cmap = brewer2mpl.get_map('Set1', 'qualitative', 9)
    p = Ppl(cmap, alpha=1)
    fig, ax = plt.subplots(1)
    ax.set_xlabel('Generation')
    ax.set_ylabel(x_axis_name)
    if y_logscale: 
        ax.set_yscale('log')
        plt.grid(b=True, which='both', axis='y', color='0.65',linestyle='dashed')
        legend_labels = map(lambda s: s + ' (log)', legend_labels)
    else: ax.yaxis.get_major_formatter().set_powerlimits((0, 1))

    for i, (series, label) in enumerate(zip(lines_to_plot, legend_labels)):
        p.plot(ax, generations, series, linewidth=2, label=label, zorder=2)
    
    if y_errorbar: 
        for i, (series, label) in enumerate(zip(lines_to_plot, legend_labels)):
            ax.errorbar(generations, series, yerr=y_errorbar[i], zorder=1, capsize=2, barsabove=True, ecolor = errorbar_color)
    
    if vline_x:
        for x in vline_x:
            ax.vlines(x = x, ymin = ax.get_ylim()[0], ymax = ax.get_ylim()[1], linewidth = 1, linestyles = 'dashed', alpha = 0.5)

    p.legend(ax, loc=0)
    if save_figure: fig.savefig(filename)
    return fig, ax, filename
    

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
        p.scatter(ax, x, y, label=str(groups.levels[g]), s = 5, linewidth=0)

    if legend: p.legend(ax, loc=0)
    #ax.set_title('prettyplotlib `scatter` example\nshowing default color cycle and scatter params')
    
    bounds = bins
    if colorbar:
        cmap = p.get_colormap().mpl_colormap
        
        norm = mpl.colors.BoundaryNorm(bounds, cmap.N)
        #ax2.set_ylabel(color_by.capitalize().replace('_', ' '), rotation='horizontal')
        #ax2.xaxis.get_major_formatter().set_powerlimits((0, 1))
        #ax2.yaxis.get_major_formatter().set_powerlimits((0, 1))
        ax2 = fig.add_axes([0.9, 0.1 , 0.03, 0.8])

        cbar = mpl.colorbar.ColorbarBase(ax2, cmap=cmap, spacing='proportional', ticks=bounds, norm=norm, alpha=1, orientation='vertical')
        #cbar.ax.set_xticklabels(map(lambda x: '%.3g'%x, bounds))# vertically oriented colorbar
        cbar.ax.set_yticklabels([])# vertically oriented colorbar
        #for j, lab in enumerate(map(lambda lower, upper: '%.3g~%.3g'%(lower, upper), bounds[:-1], bounds[1::])):
        cbar.ax.text(0,1.02, '%.3g'%max(map(eval(color_function), bounds)))
        #for j, lab in enumerate(map(lambda upper: '< %.3g'%upper, bounds[1::])):
        #    cbar.ax.text(.5, (2 * j + 1) / 8.0, lab, ha='center', va='center', rotation='vertical')
        #cbar.ax.set_xticklabels([str(int(t)) for t in bounds])# vertically oriented colorbar
        if color_function == 'log': label = color_by.capitalize().replace('_', ' ') + ' (log)'
        else: label = color_by.capitalize().replace('_', ' ')
        cbar.ax.set_ylabel(label, rotation='vertical')
    fig.savefig(filename)
    return ax, fig

def make_scatter_plot_for_labelled_data(data_frame, x_name, y_name, labels, filename, colormap, x_function = 'dummy', y_function = 'dummy', legend = False, point_size = 5, omit_largest = 0, labels_to_plot = []):
    ### Originally created for issue_28
    if not labels_to_plot: labels_to_plot = set(labels)
    assert omit_largest < max(set(labels)), "omit_largest must be smaller than number of clusters"
    colors = colormap.mpl_colors
    def dummy(a): return a
    p = Ppl(colormap, alpha=1)

    fig, ax = plt.subplots(1)
    #ax.set_autoscale_on(False)
    ax.set_xlim([eval(x_function)(min(data_frame[x_name])), eval(x_function)(max(data_frame[x_name]))])
    ax.set_ylim([eval(y_function)(min(data_frame[y_name])), eval(y_function)(max(data_frame[y_name]))])
    #x_label = x_name.capitalize().replace('_', ' ')
    if x_function == 'log': x_label += ' (log)'
    #y_label = y_name.capitalize().replace('_', ' ')
    if y_function == 'log': y_label += ' (log)'
    ax.set_xlabel(fl(x_name))
    ax.set_ylabel(fl(y_name))
    ax.xaxis.get_major_formatter().set_powerlimits((0, 1))
    ax.yaxis.get_major_formatter().set_powerlimits((0, 1))
    # Show the whole color range
    
    cluster_size = map(lambda l: len(labels[labels == l]), set(labels))
    sizes, groups = zip(*sorted(zip(cluster_size, set(labels)), reverse=True))
    #print sizes, groups
    for order_to_plot, group in enumerate(list(groups)[-(len(groups)-omit_largest):]):
        #print order_to_plot, sizes[order_to_plot], group, cluster_size[group]
        if group in labels_to_plot:
            #print 'Plotting points in group %s'%group
            x = eval(x_function)(data_frame[labels == group][x_name])
            y = eval(y_function)(data_frame[labels == group][y_name])
            p.scatter(ax, x, y, label='C%s: %s'%(group, list(sizes)[order_to_plot]), s=point_size, linewidth=0, zorder=order_to_plot, color=colors[group])
    if legend: 
        legend = p.legend(ax, loc=0, fancybox=True, markerscale=5, frameon=False)
        legend.set_zorder(100)

    #ax.set_title('prettyplotlib `scatter` example\nshowing default color cycle and scatter params')
    

    fig.savefig(filename)

def plot_pca_components(filename, components):
    colormap = brewer2mpl.get_map('Set1', 'qualitative', 9)
    p = Ppl(colormap, alpha=1)
    fig, ax = plt.subplots(1)
    p.pcolormesh(fig, ax, components.values)
    ax.set_xticks([])
    yticks = np.linspace(len(components) - 0.5, 0.5, len(components))
    ax.set_yticks(yticks)
    y_ticklabels = map(lambda x: 'PCA %s'%x, range(len(components)))
    ax.set_yticklabels(y_ticklabels)
    fig.savefig(filename)
    return fig, ax

def plot_image_matrix(filename, jaccard_matrix, x_ticklabels = None, y_ticklabels = None):
    colormap = brewer2mpl.get_map('Set1', 'qualitative', 9)
    p = Ppl(colormap, alpha=1)
    fig, ax = plt.subplots(1)
    masked_jaccard = np.ma.masked_where(np.isnan(jaccard_matrix), jaccard_matrix)
    p.pcolormesh(fig, ax, masked_jaccard)
    print x_ticklabels
    print y_ticklabels
    #ax.imshow(jaccard_matrix, interpolation = 'none')
    #ax.set_xticks([])
    yticks = range(jaccard_matrix.shape[0]+1)
    xticks = range(jaccard_matrix.shape[1]+1)
    ax.set_yticks(yticks)
    ax.set_xticks(xticks)
    #x_ticklabels.reverse()
    ax.set_yticklabels(y_ticklabels)
    ax.set_xticklabels(x_ticklabels)
    ax.set_ylabel(fl('ratioagent'))
    ax.set_xlabel(fl('ratiolatency'))
    fig.savefig(filename)
    return fig, ax

def plot_group_overlap(filename, jaccard_matrix):
    size = jaccard_matrix.shape[0]
    colormap = brewer2mpl.get_map('Set1', 'qualitative', 9)
    p = Ppl(colormap, alpha=1)
    fig, ax = plt.subplots(1)
    masked_jaccard = np.ma.masked_where(np.isnan(jaccard_matrix), jaccard_matrix)
    p.pcolormesh(fig, ax, masked_jaccard)
    #ax.imshow(jaccard_matrix, interpolation = 'none')
    ax.set_xticks([])
    yticks = np.linspace(size - 0.5, 0.5, size)
    ax.set_yticks(yticks)
    y_ticklabels = map(lambda x: 'F%s'%x, range(1,size+1))
    y_ticklabels.reverse()
    ax.set_yticklabels(y_ticklabels)
    ax.set_xticks(yticks)
    ax.set_xticklabels(y_ticklabels)
    fig.savefig(filename)
    return fig, ax

def plot_correlation_matrix(filename, correlation_matrix, labels):
    size = correlation_matrix.shape[0]
    colormap = brewer2mpl.get_map('Set1', 'qualitative', 9)
    p = Ppl(colormap, alpha=1)
    fig, ax = plt.subplots(1)
    #masked_jaccard = np.ma.masked_where(np.isnan(jaccard_matrix), jaccard_matrix)
    p.pcolormesh(fig, ax, correlation_matrix)
    #ax.imshow(jaccard_matrix, interpolation = 'none')
    ax.set_xticks([])
    yticks = np.linspace(size - 0.5, 0.5, size)
    ax.set_yticks(yticks)
    labels.reverse()
    ax.set_yticklabels(labels)
    ax.set_xticks(yticks)
    ax.set_xticklabels(labels)
    fig.savefig(filename)
    return fig, ax


def plot_histogram():
    import numpy as np
    import pylab as P

    # The hist() function now has a lot more options
    #

    #
    # first create a single histogram
    #
    P.figure()
    mu, sigma = 40, 35


    x = abs(np.random.normal(mu, sigma, 1000000))

    # the histogram of the data with histtype='step'
    n, bins, patches = P.hist(x, 100, normed=1, histtype='stepfilled')
    P.setp(patches, 'facecolor', 'g', 'alpha', 0.50)
    P.vlines(np.mean(x), 0, max(n))
    P.vlines(np.median(x), 0, max(n))
    # add a line showing the expected distribution
    y = np.abs(P.normpdf( bins, mu, sigma))
    l = P.plot(bins, y, 'k--', linewidth=1.5)

    P.show()

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
