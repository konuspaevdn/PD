#!/bin/bash

#SBATCH --ntasks-per-node=8
#SBATCH --cpus-per-task=1
#SBATCH --partition=RT_study
#SBATCH --job-name=task1
#SBATCH --comment='Testing'
#SBATCH --output=out.txt
#SBATCH --error=error.txt
#SBATCH --open-mode=append
mpirun ./a.out  100000000
