from __future__ import division
import os
import glob
import argparse

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Count the number of victories of our agent')
    parser.add_argument(
        'dir',
        help='List of directories to analyse, each one is treated as an experiment repetition',
        nargs='+'
    )
    parser.add_argument(
        '-o', '--output', required=False,
        help='Save result to this file instead of showing on screen')
    parser.add_argument(
        '-i', '--initial-epi', required=False, default=0,
        help='First episode to consider'
    )
    parser.add_argument(
        '-f', '--final-epi', help='Last episode to consider', required=False
    )
    parser.add_argument(
        '-v', '--verbose', help='Output additional info?', action='store_true'
    )

    args = vars(parser.parse_args())

    num = 0
    count = 0
    num_games = 0
    rep_num = 0

    directories = args['dir']
    num_reps = len(directories)
    output = args['output']
    initial_epi = args['initial_epi']
    final_epi = args['final_epi']

    for repetition in directories:
        if final_epi is None:
            final_epi = len(glob.glob(os.path.join(repetition, '*.game'))) - 1

        for i, filename in enumerate(glob.glob(os.path.join(repetition, '*.game'))):
            name = filename.split('episode_')[1]
            name = int(name.split('.')[0])
            if initial_epi <= name <= final_epi:
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

    mean_games = num_games / num_reps
    mean_victories = count / num_reps

    if output is not None:
        output_file = open(output, 'w')
        output_file.write('Number of games: ' + str(mean_games) + '\n')
        output_file.write('Number of victories: ' + str(mean_victories) + '\n')
        output_file.write('Victory rate: ' + str("{:.0%}".format(mean_victories / mean_games)) + '\n')

    if args['verbose']:
        print('Number of games: %d' % mean_games)
        print('Mean #victories: %f' % mean_victories)
        print('%mean victories: {:.3%}'.format(mean_victories / mean_games))
        
    else:
        print('%f' % (mean_victories / mean_games))
