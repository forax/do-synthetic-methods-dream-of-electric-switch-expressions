#!/bin/bash

# provide a header that run all cells
echo "header=travis/run_all_header.txt" >> build.properties

# re-generate all files
java build/build.java

# start the notebook with no authentication
jupyter notebook --no-browser --ip 0.0.0.0 --NotebookApp.token=""
