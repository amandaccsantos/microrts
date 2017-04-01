#!/bin/bash

classpath=.:../bin:../lib/jdom.jar:../lib/burlap-3.0.1-jar-with-dependencies.jar:lib/SCPSolver.jar:lib/LPSOLVESolverPack.jar

echo "Launching experiment..."

java -classpath $classpath rl.RLExperiment ../experiments/example_SGQLearningAdapter_Light.xml

echo "Done."
