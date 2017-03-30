You must use SCPSolver and LBSOLVESolverPack from this directory to
use PersistentMultiAgentQLearning without running into SIGSEGV on
JVM.

Please follow these steps:

- Add SCPSolver.jar and LPSOLVESolverPack.jar to the build path;
- Right click on project main directory -> Build Path -> Configure Build Path;
- Select the Libraries tab;
- Expand the JRE System library option and select Native library location;
- Click on Edit, point to this lib directory and click OK.

These steps were adapted from:
https://examples.javacodegeeks.com/java-basics/java-library-path-what-is-it-and-how-to-use/
 