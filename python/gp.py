import numpy as np
from sklearn.gaussian_process import GaussianProcess
from matplotlib import pyplot as pl
from pandas import concat, DataFrame
np.random.seed(1)


def f(x):
    """The function to predict."""
    return x * np.sin(x)

def get_xy(dataframe, features, target):
    
    assert isinstance(features, list)
    assert isinstance(target, str)
    assert isinstance(dataframe, DataFrame)
    assert target in dataframe.columns
    no_duplicates = dataframe.drop_duplicates(features)
    x = np.matrix(no_duplicates[features])
    y = np.matrix(no_duplicates[target]).transpose()
    return x, y

def myown():
    from sklearn.cross_validation import train_test_split
    
    import IO
    fit, par, gen, ids = IO.load_pickled_generation_dataframe('d10')
    all_data = concat([fit, par], axis=1)
    train, test = map(lambda x: DataFrame(x, columns=all_data.columns), train_test_split(all_data, test_size = 0.95))
    x, y = get_xy(train, features=['time_to_reach_new_fundamental'], target='ssmm_nAgents')
    return x,y
    
def run():
    #----------------------------------------------------------------------
    #  First the noiseless case
    X = np.atleast_2d([1., 3., 5., 6., 7., 8.]).T

    # Observations
    y = f(X).ravel()

    # Mesh the input space for evaluations of the real function, the prediction and
    # its MSE
    x = np.atleast_2d(np.linspace(0, 10, 1000)).T

    # Instanciate a Gaussian Process model
    gp = GaussianProcess(corr='cubic', theta0=1e-2, thetaL=1e-4, thetaU=1e-1,
                         random_start=100)

    # Fit to data using Maximum Likelihood Estimation of the parameters
    gp.fit(X, y)

    # Make the prediction on the meshed x-axis (ask for MSE as well)
    y_pred, MSE = gp.predict(x, eval_MSE=True)
    sigma = np.sqrt(MSE)

    # Plot the function, the prediction and the 95% confidence interval based on
    # the MSE
    fig = pl.figure()
    pl.plot(x, f(x), 'r:', label=u'$f(x) = x\,\sin(x)$')
    pl.plot(X, y, 'r.', markersize=10, label=u'Observations')
    pl.plot(x, y_pred, 'b-', label=u'Prediction')
    pl.fill(np.concatenate([x, x[::-1]]),
            np.concatenate([y_pred - 1.9600 * sigma,
                           (y_pred + 1.9600 * sigma)[::-1]]),
            alpha=.5, fc='b', ec='None', label='95% confidence interval')
    pl.xlabel('$x$')
    pl.ylabel('$f(x)$')
    pl.ylim(-10, 20)
    pl.legend(loc='upper left')

    #----------------------------------------------------------------------
    # now the noisy case
    import IO
    fit,par,gen,ids = IO.load_pickled_generation_dataframe('d10')

    #X = np.linspace(0.1, 9.9, 20)
    #X = np.atleast_2d(X).T
    #print X.shape
    X = fit['time_to_reach_new_fundamental'].iloc[range(100)].reshape((100,1))
    y = par['ssmm_nAgents'].iloc[range(100)].tolist()
    print X
    print y
    # Obsevations and noise
    #y = f(X).ravel()
    
    #dy = 0.5 + 1.0 * np.random.random(y.shape)
    #noise = np.random.normal(0, dy)
    #y += noise

    # Mesh the input space for evaluations of the real function, the prediction and
    # its MSE
    x = np.atleast_2d(np.linspace(0, 10, 1000)).T

    # Instanciate a Gaussian Process model
    gp = GaussianProcess(corr='squared_exponential', theta0=1e-1,
                         thetaL=1e-3, thetaU=1,
                         random_start=100)

    # Fit to data using Maximum Likelihood Estimation of the parameters
    gp.fit(X, y)

    # Make the prediction on the meshed x-axis (ask for MSE as well)
    y_pred, MSE = gp.predict(x, eval_MSE=True)
    sigma = np.sqrt(MSE)

    # Plot the function, the prediction and the 95% confidence interval based on
    # the MSE
    fig = pl.figure()
    pl.plot(x, f(x), 'r:', label=u'$f(x) = x\,\sin(x)$')
    pl.errorbar(X.ravel(), y, dy, fmt='r.', markersize=10, label=u'Observations')
    pl.plot(x, y_pred, 'b-', label=u'Prediction')
    pl.fill(np.concatenate([x, x[::-1]]),
            np.concatenate([y_pred - 1.9600 * sigma,
                           (y_pred + 1.9600 * sigma)[::-1]]),
            alpha=.5, fc='b', ec='None', label='95% confidence interval')
    pl.xlabel('$x$')
    pl.ylabel('$f(x)$')
    pl.ylim(-10, 20)
    pl.legend(loc='upper left')

    pl.show()