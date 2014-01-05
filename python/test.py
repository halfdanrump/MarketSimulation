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