import matplotlib.pyplot as plt
import sys
import os
import glob

if __name__ == '__main__':
    rewards_agent0 = []
    rewards_agent1 = []

    path = sys.argv[1]
    for filename in glob.glob(os.path.join(path, '*.game')):
        f = open(filename, 'r')

        while True:
            line = f.readline()
            if line.startswith("jointRewards"):
                while True:
                    reward = f.readline()
                    if reward.startswith("states"):
                        break
                    replacements = ('[', ']')
                    for r in replacements:
                        reward = reward.replace(r, ' ')
                    reward = reward.split()
                    rewards_agent0.append(reward[1])
                    rewards_agent1.append(reward[2])
            elif not line:
                break
