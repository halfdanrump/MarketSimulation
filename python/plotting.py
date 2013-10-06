import settings
import numpy as np
from matplotlib.pyplot import plt
import textwrap

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

def save_line_plot(all_data, prefix, x_axis_name = "", y_axis_name = [""], all_parameters = {}):
    assert x_axis_name in all_data.dtype.names, "x axis parameter not found"
    for y_name in y_axis_name: assert y_name in all_data.dtype.names, "y axis parameter not found"
    assert all_parameters

    fig = plt.figure(figsize=(10, 8), dpi=100)
    ax = fig.add_axes([0.1, 0.3, 0.8, 0.6])
    ax.set_xlabel(x_axis_name)
    ax.set_ylabel(repr(y_axis_name))
    
    for i, y_name in enumerate(y_axis_name):
        plt.plot(all_data[x_axis_name],all_data[y_name], lw=2)
        plt.legend(y_axis_name)
    
    
    caption = "\n".join(textwrap.wrap(repr([(k,all_parameters[k]) for k in sorted(all_parameters)]), 100))
    fig.text(0.1,0.1, caption)
    #plt.text(caption)
    
    graph_filename = "%s_vs_%s.pdf"%(x_axis_name, repr(y_axis_name))
    plt.savefig(settings.graph_root_folder + prefix + graph_filename)
