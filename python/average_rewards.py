import os
import re
import glob
import argparse
import matplotlib.pyplot as plt


def natural_sort(l):
    """
    Sorts a list of strings in natural order.
    E.g. [file1.txt, file2.txt, file10.txt] instead of
    [file1.txt, file10.txt, file2.txt] (which is lexicographic order)
    Code from Mark Byers @ StackOverflow (https://stackoverflow.com/a/4836734)
    :param l: list
    :return: list
    """

    convert = lambda text: int(text) if text.isdigit() else text.lower()
    alphanum_key = lambda key: [convert(c) for c in re.split('([0-9]+)', key)]
    return sorted(l, key=alphanum_key)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Plot cumulative rewards in a number of episodes, averaged among repetitions'
    )
    parser.add_argument(
        'dir', nargs='+',
        help='List of directories to plot. Each is treated as a repetition'
    )
    parser.add_argument(
        '-w', '--window', help='Cumulative reward for X episodes',
        type=int, required=True
    )
    parser.add_argument(
        '-e', '--episodes', help='Number of episodes tested',
        type=int, required=True
    )
    parser.add_argument(
        '-o', '--output', help='Save the plot to this file instead of showing on screen',
        required=False
    )

    args = vars(parser.parse_args())

    episodes = args['episodes']
    window = args['window']
    directories = args['dir']
    output = args['output']
    num_reps = len(directories)

    rewards_agent0 = [0.0 for x in range(episodes)]
    rewards_agent1 = [0.0 for x in range(episodes)]

    for repetition in directories:
        # retrieves the list of .game files, sorted in natural order
        sorted_files = natural_sort(glob.glob(os.path.join(repetition, '*.game')))

        for i, filename in enumerate(sorted_files):
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
                        rewards_agent0[i] += float(
                            reward[1]) / num_reps  # adds the reward of agent 0 accumulated in game i
                        rewards_agent1[i] += float(
                            reward[2]) / num_reps  # adds the reward of agent 1 accumulated in game i
                elif not line:
                    break

    points_agent0 = [0 for x in range(episodes / window)]
    points_agent1 = [0 for x in range(episodes / window)]

    # calculate the reward of agent 0 accumulated in x games
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

    # calculate the reward of agent 1 accumulated in x games
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
    #line1, = plt.plot(points_agent1, color='r', label='Agent 1')
    #plt.legend(handles=[line0, line1])
    plt.xlabel('Cumulative reward averaged over %d episodes' % window)

    if output is None:
        plt.show()
    else:
        plt.savefig(output)
