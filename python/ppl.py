import collections

import matplotlib as mpl
import matplotlib.pyplot as plt
import brewer2mpl
import numpy as np

# Get Set2 from ColorBrewer, a set of colors deemed colorblind-safe and
# pleasant to look at by Drs. Cynthia Brewer and Mark Harrower of Pennsylvania
# State University. These colors look lovely together, and are less
# saturated than those colors in Set1. For more on ColorBrewer, see:
# - Flash-based interactive map:
#     http://colorbrewer2.org/
# - A quick visual reference to every ColorBrewer scale:
#     http://bl.ocks.org/mbostock/5577023

class Ppl:
    def __init__(self, colormap, alpha = 1):
        self.colormap = colormap
        self.set2 = colormap.mpl_colors
        self.alpha = alpha
        # Another ColorBrewer scale. This one has nice "traditional" colors like
        # reds and blues
        self.set1 = brewer2mpl.get_map('Set1', 'qualitative', 9).mpl_colors
        mpl.rcParams['axes.color_cycle'] = self.set2

        # Set some commonly used colors
        self.almost_black = '#262626'
        self.light_grey = np.array([float(248) / float(255)] * 3)

        reds = mpl.cm.Reds
        reds.set_bad('white')
        reds.set_under('white')

        blues_r = mpl.cm.Blues_r
        blues_r.set_bad('white')
        blues_r.set_under('white')

        # Need to 'reverse' red to blue so that blue=cold=small numbers,
        # and red=hot=large numbers with '_r' suffix
        self.blue_red = brewer2mpl.get_map('RdBu', 'Diverging', 11,
                                      reverse=True).mpl_colormap

        # Default "patches" like scatterplots
        mpl.rcParams['patch.linewidth'] = 0.75     # edge width in points

        # Default empty circle with a colored outline
        mpl.rcParams['patch.facecolor'] = 'none'
        mpl.rcParams['patch.edgecolor'] = self.set2[0]

        # Change the default axis colors from black to a slightly lighter black,
        # and a little thinner (0.5 instead of 1)
        mpl.rcParams['axes.edgecolor'] = self.almost_black
        mpl.rcParams['axes.labelcolor'] = self.almost_black
        mpl.rcParams['axes.linewidth'] = 0.5

        # Make the default grid be white so it "removes" lines rather than adds
        mpl.rcParams['grid.color'] = 'white'

        # change the tick colors also to the almost black
        mpl.rcParams['ytick.color'] = self.almost_black
        mpl.rcParams['xtick.color'] = self.almost_black

        # change the text colors also to the almost black
        mpl.rcParams['text.color'] = self.almost_black

        plt.rc('text', usetex=True)
        plt.rc('font', family='serif')
    def set_colormap(self, colormap):
        self.colormap = colormap
        self.set2 = colormap.mpl_colors

    def get_colormap(self):
        return self.colormap

    def get_alpha(self):
        return self.alpha

    def bar(self, ax, left, height, **kwargs):
        """
        Creates a bar plot, with white outlines and a fill color that defaults to
         the first teal-ish green in ColorBrewer's Set2. Optionally accepts
         grid='y' or grid='x' to draw a white grid over the bars,
         to show the scale. Almost like "erasing" some of the plot,
         but it adds more information!

        Can also add an annotation of the height of the barplots directly onto
        the bars with the `annotate` parameter, which can either be True,
        which will annotate the values, or a list of strings, which will annotate
        with the supplied strings.

        @param ax: matplotlib.axes instance
        @param left: Vector of values of where to put the left side of the bar
        @param height: Vector of values of the bar heights
        @param kwargs: Any additional arguments to matplotlib.bar()
        """
        if 'color' not in kwargs:
            kwargs['color'] = self.set2[0]
        if 'edgecolor' not in kwargs:
            kwargs['edgecolor'] = 'white'
        if 'width' in kwargs:
            # Find the middle of the bar
            middle = kwargs['width']/2.0
        else:
            middle = 0.4

        # Label each individual bar, if xticklabels is provided
        xtickabels = kwargs.pop('xticklabels', None)
        # left+0.4 is the center of the bar
        xticks = np.array(left) + middle

        # Whether or not to annotate each bar with the height value
        annotate = kwargs.pop('annotate', False)

        # If no grid specified, don't draw one.
        grid = kwargs.pop('grid', None)

        rectangles = ax.bar(left, height, **kwargs)

        # add whitespace padding on left
        xmin, xmax = ax.get_xlim()
        xmin -= 0.2
        ax.set_xlim(xmin, xmax)

        # If there are negative counts, remove the bottom axes
        # and add a line at y=0
        if any(h < 0 for h in height):
            axes_to_remove = ['top', 'right', 'bottom']
            ax.hlines(y=0, xmin=xmin, xmax=xmax,
                      linewidths=0.75)
        else:
            axes_to_remove = ['top', 'right']

        # Remove excess axes
        self.remove_chartjunk(ax, axes_to_remove, grid=grid)

        # Add the xticklabels if they are there
        if xtickabels is not None:
            ax.set_xticks(xticks)
            ax.set_xticklabels(xtickabels)

        if annotate or isinstance(annotate, collections.Iterable):
            annotate_yrange_factor = 0.025
            ymin, ymax = ax.get_ylim()
            yrange = ymax - ymin

            # Reset ymax and ymin so there's enough room to see the annotation of
            # the top-most
            if ymax > 0:
                ymax += yrange * 0.1
            if ymin < 0:
                ymin -= yrange * 0.1
            ax.set_ylim(ymin, ymax)
            yrange = ymax - ymin

            offset_ = yrange * annotate_yrange_factor
            if isinstance(annotate, collections.Iterable):
                annotations = map(str, annotate)
            else:
                annotations = ['%.3f' % h if type(h) is np.float_ else str(h)
                               for h in height]
            for x, h, annotation in zip(xticks, height, annotations):
                # Adjust the offset to account for negative bars
                offset = offset_ if h >= 0 else -1 * offset_
                verticalalignment = 'bottom' if h >= 0 else 'top'

                # Finally, add the text to the axes
                ax.annotate(annotation, (x, h + offset),
                            verticalalignment=verticalalignment,
                            horizontalalignment='center',
                            color=self.almost_black)
        return rectangles


    def boxplot(self, ax, x, **kwargs):
        """
        Create a box-and-whisker plot showing the mean, 25th percentile, and 75th
        percentile. The difference from matplotlib is only the left axis line is
        shown, and ticklabels labeling each category of data can be added.

        @param ax:
        @param x:
        @param kwargs:
        @return:
        """
        # If no ticklabels are specified, don't draw any
        xticklabels = kwargs.pop('xticklabels', None)

        if 'widths' not in kwargs:
            kwargs['widths'] = 0.15
        bp = ax.boxplot(x, **kwargs)
        if xticklabels:
            ax.xaxis.set_ticklabels(xticklabels)

        self.remove_chartjunk(ax, ['top', 'right', 'bottom'])
        linewidth = 0.75

        plt.setp(bp['boxes'], color=self.set1[1], linewidth=linewidth)
        plt.setp(bp['medians'], color=self.set1[0])
        plt.setp(bp['whiskers'], color=self.set1[1], linestyle='solid',
                 linewidth=linewidth)
        plt.setp(bp['fliers'], color=self.set1[1])
        plt.setp(bp['caps'], color=self.set1[1], linewidth=linewidth)
        ax.spines['left']._linewidth = 0.5
        return bp


    def hist(self, ax, x, **kwargs):
        """
        Plots a histogram of the provided data. Can provide optional argument
        "grid='x'" or "grid='y'" to draw a white grid over the histogram. Almost like "erasing" some of the plot,
         but it adds more information!
        """
        # Reassign the default colors to Set2 by Colorbrewer
        color_cycle = ax._get_lines.color_cycle
        color = kwargs.pop('color', next(color_cycle))
        facecolor = kwargs.pop('facecolor', color)

        # If no grid specified, don't draw one.
        grid = kwargs.pop('grid', None)

        # print 'hist kwargs', kwargs
        patches = ax.hist(x, edgecolor='white', facecolor=facecolor, **kwargs)
        self.remove_chartjunk(ax, ['top', 'right'], grid=grid)
        return patches


    def legend(self, ax, facecolor = None, **kwargs):
        if not facecolor: facecolor = self.light_grey
        legend = ax.legend(scatterpoints=1, **kwargs)
        rect = legend.get_frame()
        rect.set_facecolor(facecolor)
        rect.set_linewidth(0.0)

        # change the label colors in the legend to almost black
        # Change the legend label colors to almost black, too
        texts = legend.texts
        for t in texts:
            t.set_color(self.almost_black)
        return legend


    def plot(self, ax, x, y, **kwargs):
        if 'color' in kwargs:
            color = kwargs['color']
            # Remove the other color argument so matplotlib doesn't complain
            kwargs.pop('color')
        else:
            # if no color is specified, cycle over the ones in this axis
            color_cycle = ax._get_lines.color_cycle
            color = next(color_cycle)
        if 'linewidth' not in kwargs:
            kwargs['linewidth'] = 0.75

        lines = ax.plot(x, y, color=color, **kwargs)
        self.remove_chartjunk(ax, ['top', 'right'])
        return lines


    def scatter(self, ax, x, y, **kwargs):
        """
        This will plot a scatterplot of x and y, iterating over the ColorBrewer
        "Set2" color cycle unless a color is specified. The symbols produced are
        empty circles, with the outline in the color specified by either 'color'
        or 'edgecolor'. If you want to fill the circle, specify 'facecolor'.
        """
        # Force 'color' to indicate the edge color, so the middle of the
        # scatter patches are empty. Can speficy
        if 'edgecolor' not in kwargs:
            kwargs['edgecolor'] = self.almost_black
        if 'color' not in kwargs:
            # Assume that color means the edge color. You can assign the
            color_cycle = ax._get_lines.color_cycle
            kwargs['color'] = next(color_cycle)
        if 'alpha' not in kwargs:
            kwargs['alpha'] = self.alpha
        if 'linewidth' not in kwargs:
            kwargs['linewidth'] = 0.1

        scatterpoints = ax.scatter(x, y, **kwargs)
        self.remove_chartjunk(ax, ['top', 'right'])
        return scatterpoints


    def scatter_column(self, ax, x, **kwargs):
        """
        Creates a boxplot-like 'scatter column' which is like a boxplot, though
        it plots the values of
        """
        pass


    def switch_axis_limits(self, ax, which_axis):
        '''
        Switch the axis limits of either x or y. Or both!
        '''
        for a in which_axis:
            assert a in ('x', 'y')
            ax_limits = ax.axis()
            if a == 'x':
                ax.set_xlim(ax_limits[1], ax_limits[0])
            else:
                ax.set_ylim(ax_limits[3], ax_limits[2])


    def upside_down_hist(self, ax, x, **kwargs):
        self.hist(ax, x, **kwargs)

        # Turn the histogram upside-down by switching the y-axis limits
        self.switch_axis_limits(ax, 'y')
        self.remove_chartjunk(ax, ['bottom', 'right'], grid='y', ticklabels='x')


    def sideways_hist(self, ax, y, **kwargs):
        self.hist(ax, y, orientation='horizontal', **kwargs)

        # Orient the histogram with `0` counts on the right and the max
        # counts on the left by switching the `x` axis limits
        self.switch_axis_limits(ax, 'x')
        self.remove_chartjunk(ax, ['left', 'top'], grid='x', ticklabels='y')


    def pcolormesh(self, fig, ax, x, **kwargs):
        """
        Use for large datasets

        Non-traditional `pcolormesh` kwargs are:
        - xticklabels, which will put x tick labels exactly in the center of the
        heatmap block
        - yticklables, which will put y tick labels exactly aligned in the center
         of the heatmap block
         - xticklabels_rotation, which can be either 'horizontal' or 'vertical'
         depending on how you want the xticklabels rotated. The default is
         'horiztonal' but if you have xticklabels that are longer, you may want
         to do 'vertical' so they don't overlap
         - yticklabels_rotation, which can also be either 'horizontal' or
         'vertical'. The default is 'horizontal' and in most cases,
         that's what you'll want to stick with. But the option is there if you
         want.
        """
        # Deal with arguments in kwargs that should be there, or need to be taken
        #  out
        if 'vmax' not in kwargs:
            kwargs['vmax'] = x.max()
        if 'vmin' not in kwargs:
            kwargs['vmin'] = x.min()

        # If we have both negative and positive values, use a divergent colormap
        if 'cmap' not in kwargs:
            kwargs['cmap'] = self.blue_red
            #if kwargs['vmax'] > 0 and kwargs['vmin'] < 0:
            #    kwargs['cmap'] = self.blue_red
            #elif kwargs['vmax'] <= 0:
            #    kwargs['cmap'] = self.blues_r
            #elif kwargs['vmax'] > 0:
            #    kwargs['cmap'] = self.reds

        if 'xticklabels' in kwargs:
            xticklabels = kwargs['xticklabels']
            kwargs.pop('xticklabels')
        else:
            xticklabels = None
        if 'yticklabels' in kwargs:
            yticklabels = kwargs['yticklabels']
            kwargs.pop('yticklabels')
        else:
            yticklabels = None

        if 'xticklabels_rotation' in kwargs:
            xticklabels_rotation = kwargs['xticklabels_rotation']
            kwargs.pop('xticklabels_rotation')
        else:
            xticklabels_rotation = 'horizontal'
        if 'yticklabels_rotation' in kwargs:
            yticklabels_rotation = kwargs['yticklabels_rotation']
            kwargs.pop('yticklabels_rotation')
        else:
            yticklabels_rotation = 'horizontal'
        p = ax.pcolormesh(x, **kwargs)
        ax.set_ylim(0, x.shape[0])

        # Get rid of ALL axes
        self.remove_chartjunk(ax, ['top', 'right', 'left', 'bottom'])

        if xticklabels:
            xticks = np.arange(0.5, x.shape[1] + 0.5)
            ax.set_xticks(xticks)
            ax.set_xticklabels(xticklabels, rotation=xticklabels_rotation)
        if yticklabels:
            yticks = np.arange(0.5, x.shape[0] + 0.5)
            ax.set_yticks(yticks)
            ax.set_yticklabels(yticklabels, rotation=yticklabels_rotation)
            # Show the scale of the colorbar
        fig.colorbar(p)
        return p


    def remove_chartjunk(self, ax, spines, grid=None, ticklabels=None):
        '''
        Removes "chartjunk", such as extra lines of axes and tick marks.

        If grid="y" or "x", will add a white grid at the "y" or "x" axes,
        respectively

        If ticklabels="y" or "x", or ['x', 'y'] will remove ticklabels from that
        axis
        '''
        all_spines = ['top', 'bottom', 'right', 'left']
        for spine in spines:
            ax.spines[spine].set_visible(False)

        # For the remaining spines, make their line thinner and a slightly
        # off-black dark grey
        for spine in all_spines:
            if spine not in spines:
                ax.spines[spine].set_linewidth(0.5)
                # ax.spines[spine].set_color(almost_black)
                #            ax.spines[spine].set_tick_params(color=almost_black)
                # Check that the axes are not log-scale. If they are, leave the ticks
            # because otherwise people assume a linear scale.
        x_pos = set(['top', 'bottom'])
        y_pos = set(['left', 'right'])
        xy_pos = [x_pos, y_pos]
        xy_ax_names = ['xaxis', 'yaxis']

        for ax_name, pos in zip(xy_ax_names, xy_pos):
            axis = ax.__dict__[ax_name]
            # axis.set_tick_params(color=almost_black)
            if axis.get_scale() == 'log':
                # if this spine is not in the list of spines to remove
                for p in pos.difference(spines):
                    axis.set_ticks_position(p)
                    #                axis.set_tick_params(which='both', p)
            else:
                axis.set_ticks_position('none')

        if grid is not None:
            for g in grid:
                assert g in ('x', 'y')
                ax.grid(axis=grid, color='white', linestyle='-', linewidth=0.5)

        if ticklabels is not None:
            if type(ticklabels) is str:
                assert ticklabels in set(('x', 'y'))
                if ticklabels == 'x':
                    ax.set_xticklabels([])
                if ticklabels == 'y':
                    ax.set_yticklabels([])
            else:
                assert set(ticklabels) | set(('x', 'y')) > 0
                if 'x' in ticklabels:
                    ax.set_xticklabels([])
                elif 'y' in ticklabels:
                    ax.set_yticklabels([])