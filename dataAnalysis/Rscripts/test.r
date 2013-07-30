

# This just reads the two arguments passed from the command line
# and assigns them to a vector of characters.
args <- commandArgs(TRUE)

# Here you should add some error exception handling code
# in case the number of passed arguments doesn't match what
# you expect (check what Forester did in his example)

# Parse the arguments (in characters) and evaluate them
vec1 <- eval( parse(text=args[1]) )
vec2 <- eval( parse(text=args[2]) )
mat1 <- eval( parse(text=args[3]) )

print(vec1)   # prints a vector of length 1
print(vec2)   # prints a vector of length 3
print(mat1)   # prints a 2 x 2 matrix
  