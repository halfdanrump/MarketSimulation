import numpy as np
from scipy.optimize import curve_fit

def exponential(x, a, b, c):
		return a*np.exp(-b*x) + c

def linear(x, a,b):
	return a*x + b

def invlinear(x, a,b):
	return 1/(a*x + b)



def mkdata(func, *fargs):
	x = np.linspace(0,4,50)
	y = func(x, *fargs)
	yn = y + 0.2*np.random.normal(size=len(x))
	return x, y, yn
	

def fit(func, x, yn):
	popt, pcov = curve_fit(func, x, yn)
	return popt, pcov

def get_estimate(x, func, *estimated_coeffs):
	return [func(i, *estimated_coeffs) for i in x]

def test():
	func = exponential
	fargs = [0.5, 10, 1]
	x, y, yn = mkdata(func, *fargs)
	popt, pcov = fit(func, x, yn)
	yhat = func(x, *popt)
	return [x, yn, 'r', x, yhat, 'b']
