import sys
import numpy as np

n = int(sys.argv[1])

matrix = np.empty([n,n])

count = 1
for i in range(n):
    for j in range(n):
        matrix[i,j] = count
        count += 1

np.savetxt('array.txt', matrix, delimiter=', ', newline='; ', fmt='%i')
