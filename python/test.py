import matplotlib.pyplot as plt
import matplotlib.cm as cm
import numpy as np


# just creating random data with a bunch of 2d gaussians

def gauss2d(x, y, a, x0, y0, sx, sy):
    return a * (np.exp(-((x - x0) / sx)**2 / 2.)
                * np.exp(-((y - y0) / sy)**2 / 2.))

def mkplot():
    imsize = 1000
    im = np.zeros((imsize, imsize), dtype=float)

    ng = 50
    x0s = imsize * np.random.random(ng)
    y0s = imsize * np.random.random(ng)
    sxs = 100. * np.random.random(ng)
    sys = sxs #100. * np.random.random(ng)
    amps = 100 + 100 * np.random.random(ng)

    for x0, y0, sx, sy, amp in zip(x0s, y0s, sxs, sys, amps):
        nsig = 5.
        xlo, xhi = int(x0 - nsig * sx), int(x0 + nsig * sx)
        ylo, yhi = int(y0 - nsig * sy), int(y0 + nsig * sy)

        xlo = xlo if xlo >= 0 else 0
        xhi = xhi if xhi <= imsize else imsize
        ylo = ylo if ylo >= 0 else 0
        yhi = yhi if yhi <= imsize else imsize

        nx = xhi - xlo
        ny = yhi - ylo

        imx = np.tile(np.arange(xlo, xhi, 1), ny).reshape((ny, nx))
        imy = np.tile(np.arange(ylo, yhi, 1), nx).reshape((nx, ny)).transpose()

        im[ylo:yhi, xlo:xhi] += gauss2d(imx, imy, amp, x0, y0, sx, sy)


    plt.imshow(im, cmap=cm.gray)

    plt.show()

def test2():
    import numpy as np
    import matplotlib.pyplot as plt
    from sklearn.datasets import fetch_species_distributions
    from sklearn.datasets.species_distributions import construct_grids
    from sklearn.neighbors import KernelDensity

    # if basemap is available, we'll use it.
    # otherwise, we'll improvise later...
    try:
        from mpl_toolkits.basemap import Basemap
        basemap = True
    except ImportError:
        basemap = False

    # Get matrices/arrays of species IDs and locations
    data = fetch_species_distributions()
    species_names = ['Bradypus Variegatus', 'Microryzomys Minutus']

    Xtrain = np.vstack([data['train']['dd lat'],
                        data['train']['dd long']]).T
    ytrain = np.array([d.startswith('micro') for d in data['train']['species']],
                      dtype='int')
    Xtrain *= np.pi / 180.  # Convert lat/long to radians

    # Set up the data grid for the contour plot
    xgrid, ygrid = construct_grids(data)
    return ygrid, xgrid
    X, Y = np.meshgrid(xgrid[::5], ygrid[::5][::-1])
    land_reference = data.coverages[6][::5, ::5]
    land_mask = (land_reference > -9999).ravel()

    xy = np.vstack([Y.ravel(), X.ravel()]).T
    xy = xy[land_mask]
    xy *= np.pi / 180.

    # Plot map of South America with distributions of each species
    fig = plt.figure()
    fig.subplots_adjust(left=0.05, right=0.95, wspace=0.05)

    for i in range(2):
        plt.subplot(1, 2, i + 1)

        # construct a kernel density estimate of the distribution
        print(" - computing KDE in spherical coordinates")
        kde = KernelDensity(bandwidth=0.04, metric='haversine',
                            kernel='gaussian', algorithm='ball_tree')
        print Xtrain[ytrain == i].shape
        kde.fit(Xtrain[ytrain == i])

        # evaluate only on the land: -9999 indicates ocean
        Z = -9999 + np.zeros(land_mask.shape[0])
        Z[land_mask] = np.exp(kde.score_samples(xy))
        Z = Z.reshape(X.shape)

        # plot contours of the density
        levels = np.linspace(0, Z.max(), 25)
        print map(lambda x: x.shape, [X,Y,Z])
        plt.contourf(X, Y, Z, levels=levels, cmap=plt.cm.Reds)

        if basemap:
            print(" - plot coastlines using basemap")
            m = Basemap(projection='cyl', llcrnrlat=Y.min(),
                        urcrnrlat=Y.max(), llcrnrlon=X.min(),
                        urcrnrlon=X.max(), resolution='c')
            m.drawcoastlines()
            m.drawcountries()
        else:
            print(" - plot coastlines from coverage")
            plt.contour(X, Y, land_reference,
                        levels=[-9999], colors="k",
                        linestyles="solid")
            plt.xticks([])
            plt.yticks([])

        plt.title(species_names[i])

    plt.show()