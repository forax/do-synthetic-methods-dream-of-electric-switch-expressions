#!/bin/bash

// evaluate all cells at startup
mkdir -p $HOME/.jupyter/custom
cp travis/custom.js $HOME/.jupyter/custom

# start the notebook with no authentication
jupyter notebook --no-browser --ip 0.0.0.0 --NotebookApp.token=""
