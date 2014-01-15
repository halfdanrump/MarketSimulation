def outer_func():

	def a():
		return "Something"

	def b():
		return "Something else"
	print type(a)
	#functions = [x for i in [a, b]]
	print locals()
	print globals()
	return map(lambda x: x(), [a,b])
	

class fcontainer():
	def a(self):
		return "Something"

	def b(self):
		return "Something else"

def test():
	f = fcontainer()
	return map(lambda x: getattr(f, x)(), ['a', 'b'])
	