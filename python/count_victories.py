from __future__ import division
import os
import glob
import argparse


def run(directories, num_reps, output, initial_epi, final_epi, locale, verbose):

    num = 0
    count = 0
    num_games = 0
    rep_num = 0

    for repetition in directories:
        if final_epi is None:
            final_epi = len(glob.glob(os.path.join(repetition, '*.game'))) - 1

        for i, filename in enumerate(glob.glob(os.path.join(repetition, '*.game'))):
            if verbose:
                print('file: %s' % filename)

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
        output_file.write('Number of games: %d\n' % mean_games)
        output_file.write('Mean #victories: %f\n' % mean_victories)
        output_file.write('Victory rate: {:.0%}\n'.format(mean_victories / mean_games))

    if verbose:
        print('Dirs: %s' % directories)
        print('Number of games: %d' % mean_games)
        print('Mean #victories: %f' % mean_victories)
        print('%mean victories: {:.3%}'.format(mean_victories / mean_games))

    else:
        print('{0:n}'.format(mean_victories / mean_games))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Count the number of victories of our agent')
    parser.add_argument(
        'dir',
        help='List of directories to analyse, each one is treated as an experiment repetition',
        nargs='+'
    )
    parser.add_argument(
        '-a', '--aggregate', required=False, action='store_true',
        help='Calculate the mean results from a list of directories, otherwise shows them individually'
    )
    parser.add_argument(
        '-o', '--output', required=False,
        help='Save result to this file instead of showing on screen'
    )
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
    parser.add_argument(
        '-l', '--locale', choices=['pt_BR.utf8', 'en_US.utf-8'], default='pt_BR.utf8',
        help='"pt_BR.utf8" for comma as decimal separator, "en_US.utf-8" for dot'
    )

    args = vars(parser.parse_args())

    # if aggregate is activated, runs once with the list of directories to output the average
    if args['aggregate']:
        run(
            args['dir'], len(args['dir']), args['output'],
            args['initial_epi'], args['final_epi'], args['locale'],
            args['verbose']
        )
    # if aggregate is deactivated, runs once for each dir, outputting to stdout
    else:
        for directory in args['dir']:
            # first argument must be a list, so we pass a single-member one
            run(
                [directory], 1, None, args['initial_epi'],
                args['final_epi'], args['locale'], args['verbose']
            )
