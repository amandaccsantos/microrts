import os
import glob
import argparse
import matplotlib.pyplot as plt

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='')
    parser.add_argument('-p', '--path', help='Path to directory to be plotted', required=True)
    parser.add_argument('-e', '--episodes', help='Cumulative reward for X episodes', type=int, required=True)
    parser.add_argument(
        '-n', '--num-reps', help='Number of repetitions (default=30)',
         type=int, required=False, default=30
    )
    parser.add_argument(
        '-r', '--rep-dir', help='Prefix of the directories with repetitions (default=rep)', 
        required=False, default='rep'
    )
    parser.add_argument(
        '-o', '--output', help='Save the plot to this file instead of showing on screen', 
        required=False
    )
    args = vars(parser.parse_args())

    rewards_agent0 = [0.0 for x in range(1000)]
    rewards_agent1 = [0.0 for x in range(1000)]

    window = args['episodes']
    path = args['path']
    repdir = args['rep_dir']
    output = args['output']
    numreps = args['num_reps']

    for rep_num in range(1, numreps + 1):
        # mounts the file name (rep01, rep02...)
        working_dir = os.path.join(path, '%s%s' % (repdir, str(rep_num).zfill(2)))
            
        for i, filename in enumerate(glob.glob(os.path.join(working_dir, '*.game'))):
            f = open(filename, 'r')
            
            while True:
                line = f.readline()
                if line.startswith("jointRewards"):
                    while True:
                        reward = f.readline()
                        if reward.startswith("states"):
                            break
                        replacements = ('[', ']', ',')
                        for r in replacements:
                            reward = reward.replace(r, ' ')
                        reward = reward.split()
                        rewards_agent0[i] += float(reward[1]) / numreps #adds the reward of agent 0 accumulated in game i
                        rewards_agent1[i] += float(reward[2]) / numreps #adds the reward of agent 1 accumulated in game i
                elif not line:
                    break

    points_agent0 = [0 for x in range(1000 / window)]
    points_agent1 = [0 for x in range(1000 / window)]

    #calculate the reward of agent 0 accumulated in x games
    for i in range(len(rewards_agent0)):
        if i % window == 0 or i == 0:
            initial = i
            count = 0
            for j in range(initial, initial + 10):
                if count + i < len(rewards_agent0):
                    interval = i + count - initial
                    count += 1
                    if interval < window and i + count < len(rewards_agent0):
                        points_agent0[i / window] += float(rewards_agent0[i + count])

    #calculate the reward of agent 1 accumulated in x games
    for i in range(len(rewards_agent1)):
        if i % window == 0 or i == 0:
            initial = i
            count = 0
            for j in range(initial, initial + 10):
                if count + i < len(rewards_agent1):
                    interval = i + count - initial
                    count += 1
                    if interval < window and i + count < len(rewards_agent1):
                        points_agent1[i / window] += float(rewards_agent1[i + count])

    line0, = plt.plot(points_agent0, color='b', label='Agent 0')
    line1, = plt.plot(points_agent1, color='r', label='Agent 1')
    plt.legend(handles=[line0, line1])
    plt.xlabel('Cumulative reward for each ' + str(window) + ' episodes')
    
    if output is None:
        plt.show()
    else:
        plt.savefig(output)