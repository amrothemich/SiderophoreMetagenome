from subprocess import call
import os
import time

for filename in os.listdir("rawseqs/"):
    if filename.endswith(".tar"):
        filename = filename
        os.system("tar -xf " + "rawseqs/" + filename + -C rawseqs)
        continue
    else:
        continue
     

for filename in os.listdir("rawseqs/"):
    if filename.endswith(".gz"):
        filename = filename
        os.system("gunzip " + "rawseqs/" + filename)
        continue
    else:
        continue