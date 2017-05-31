from __future__ import division
import os
import glob
import argparse

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Count the number of victories of our agent')
    parser.add_argument('-p', '--path', help='Path to directory to be analysed', required=True)
    parser.add_argument('-n', '--num-reps', help='Number of repetitions', type=int, required=False, default=30)

    args = vars(parser.parse_args())

    num = 0
    count = 0
    num_games = 0
    rep_num = 0

    path = args['path']
    num_reps = args['num_reps']

    for rep_num in range(1, num_reps + 1):
        rep_name = os.path.join(path, '%s%s' % ('rep', str(rep_num).zfill(2)))
        for i, filename in enumerate(glob.glob(os.path.join(rep_name, '*.game'))):
            num_games += 1
            f = open(filename, 'r')
            for line in f:
                if line.startswith('jointRewards'):
                    line = f.next()
                    while True:
                        num = 0
                        line = line.split('[')
                        if not line[1].startswith('-'):
                            num += 1
                        line = f.next()
                        if line.startswith('states:'):
                            break
            count += num

    mean_games = num_games / rep_num
    mean_victories = count / rep_num

    print 'Number of games: ' + str(mean_games)
    print 'Number of victories: ' + str(mean_victories)
    print 'Victory rate: ' + str("{:.0%}".format(mean_victories/mean_games))