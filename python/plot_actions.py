import os
import argparse
import matplotlib.pyplot as plt

dict = {}

parser = argparse.ArgumentParser(description='Plot action-values along episodes')
parser.add_argument('path', help='Path to file with action-value data')
parser.add_argument('outdir', help='Directory to generate plots in')

args = vars(parser.parse_args())

outdir = args['outdir']

number = 0
game = ''
agent = ''

ranged_opening = []
worker_opening = []
light_opening = []
barracks_opening = []
expand_opening = []

ranged_early = []
worker_early = []
light_early = []
barracks_early = []
expand_early = []

ranged_mid = []
worker_mid = []
light_mid = []
barracks_mid = []
expand_mid = []

ranged_late = []
worker_late = []
light_late = []
barracks_late = []
expand_late = []

ranged_end = []
worker_end = []
light_end = []
barracks_end = []
expand_end = []

f = open(args['path'], 'r')
#f = open('output.txt', 'r')
for line in f:
    if line.startswith("Game: "):
        line = line.strip()
        game, number = line.split(": ")
    elif line.startswith("Value"):
        line = line.strip()
        a, agent = line.split("agent ")
    elif line.startswith("Stage: ") and agent == '0':
        line = line.strip()
        line = line.replace(',', '.')
        a, stage, value = line.split(": ")
        dict.update({stage: value})
        line = f.next()
        if stage == "OPENING":
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            light_opening.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            barracks_opening.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            ranged_opening.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            expand_opening.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            worker_opening.append((number, b_value))
        elif stage == "EARLY":
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            light_early.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            barracks_early.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            ranged_early.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            expand_early.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            worker_early.append((number, b_value))
        elif stage == "MID":
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            light_mid.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            barracks_mid.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            ranged_mid.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            expand_mid.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            worker_mid.append((number, b_value))
        elif stage == "LATE":
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            light_late.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            barracks_late.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            ranged_late.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            expand_late.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            worker_late.append((number, b_value))
        elif stage == "END":
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            light_end.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            barracks_end.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            ranged_end.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            expand_end.append((number, b_value))
            line = f.next()
            line = line.strip()
            line = line.replace(',', '.')
            behavior, b_value = line.split(": ")
            worker_end.append((number, b_value))
f.close()

plt.figure(1)
plt.plot(*zip(*light_opening), label='LightRush')
plt.plot(*zip(*barracks_opening), label='BuildBarracks')
plt.plot(*zip(*ranged_opening), label='RangedRush')
plt.plot(*zip(*expand_opening), label='Expand')
plt.plot(*zip(*worker_opening), label='WorkerRush')
plt.legend(loc=3, borderaxespad=0.)
plt.savefig(os.path.join(outdir, "opening.png"))

plt.figure(2)
plt.plot(*zip(*light_early), label='LightRush')
plt.plot(*zip(*barracks_early), label='BuildBarracks')
plt.plot(*zip(*ranged_early), label='RangedRush')
plt.plot(*zip(*expand_early), label='Expand')
plt.plot(*zip(*worker_early), label='WorkerRush')
plt.legend(loc=3, borderaxespad=0.)
plt.savefig(os.path.join(outdir, "early.png"))

plt.figure(3)
plt.plot(*zip(*light_mid), label='LightRush')
plt.plot(*zip(*barracks_mid), label='BuildBarracks')
plt.plot(*zip(*ranged_mid), label='RangedRush')
plt.plot(*zip(*expand_mid), label='Expand')
plt.plot(*zip(*worker_mid), label='WorkerRush')
plt.legend(loc=3, borderaxespad=0.)
plt.savefig(os.path.join(outdir, "mid.png"))

plt.figure(4)
plt.plot(*zip(*light_late), label='LightRush')
plt.plot(*zip(*barracks_late), label='BuildBarracks')
plt.plot(*zip(*ranged_late), label='RangedRush')
plt.plot(*zip(*expand_late), label='Expand')
plt.plot(*zip(*worker_late), label='WorkerRush')
plt.legend(loc=3, borderaxespad=0.)
plt.savefig(os.path.join(outdir, "late.png"))

plt.figure(5)
plt.plot(*zip(*light_end), label='LightRush')
plt.plot(*zip(*barracks_end), label='BuildBarracks')
plt.plot(*zip(*ranged_end), label='RangedRush')
plt.plot(*zip(*expand_end), label='Expand')
plt.plot(*zip(*worker_end), label='WorkerRush')
plt.legend(loc=3, borderaxespad=0.)
plt.savefig(os.path.join(outdir, "end.png"))
