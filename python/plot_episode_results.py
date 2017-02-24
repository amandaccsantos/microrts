import matplotlib.pyplot as plt
import numpy as np

dict = {}
opening = {}
early = {}
mid = {}
late = {}
end = {}

if __name__ == '__main__':
    number = 0
    game = ''
    agent = ''
    f = open("../output.txt", 'r')
    for line in f:
        if line.startswith("Game: "):
            line = line.strip()
            game, number = line.split(": ")
        elif line.startswith("Value"):
            line = line.strip()
            a, agent = line.split("agent ")
        elif line.startswith("Stage: "):
            line = line.strip()
            line = line.replace(',', '.')
            a, stage, value = line.split(": ")
            dict.update({stage: value})
            line = f.next()
            if stage == "OPENING":
                while True:
                    line = line.strip()
                    line = line.replace(',', '.')
                    behavior, b_value = line.split(": ")
                    opening.update({behavior: float(b_value)})
                    line = f.next()
                    if line.startswith("WorkerRush: "):
                        line = line.strip()
                        line = line.replace(',', '.')
                        behavior, b_value = line.split(": ")
                        opening.update({behavior: float(b_value)})
                        break
            elif stage == "EARLY":
                while True:
                    line = line.strip()
                    line = line.replace(',', '.')
                    behavior, b_value = line.split(": ")
                    early.update({behavior: float(b_value)})
                    line = f.next()
                    if line.startswith("WorkerRush: "):
                        line = line.strip()
                        line = line.replace(',', '.')
                        behavior, b_value = line.split(": ")
                        early.update({behavior: float(b_value)})
                        break
            elif stage == "MID":
                while True:
                    line = line.strip()
                    line = line.replace(',', '.')
                    behavior, b_value = line.split(": ")
                    mid.update({behavior: float(b_value)})
                    line = f.next()
                    if line.startswith("WorkerRush: "):
                        line = line.strip()
                        line = line.replace(',', '.')
                        behavior, b_value = line.split(": ")
                        mid.update({behavior: float(b_value)})
                        break
            elif stage == "LATE":
                while True:
                    line = line.strip()
                    line = line.replace(',', '.')
                    behavior, b_value = line.split(": ")
                    late.update({behavior: float(b_value)})
                    line = f.next()
                    if line.startswith("WorkerRush: "):
                        line = line.strip()
                        line = line.replace(',', '.')
                        behavior, b_value = line.split(": ")
                        late.update({behavior: float(b_value)})
                        break
            elif stage == "END":
                while True:
                    line = line.strip()
                    line = line.replace(',', '.')
                    behavior, b_value = line.split(": ")
                    end.update({behavior: float(b_value)})
                    line = f.next()
                    if line.startswith("WorkerRush: "):
                        line = line.strip()
                        line = line.replace(',', '.')
                        behavior, b_value = line.split(": ")
                        end.update({behavior: float(b_value)})
                        break
    f.close()

    multiple_bars = plt.figure()

    N = 5
    width = 0.1
    x = range(N)
    ind = np.arange(N)

    y = opening.values()
    z = early.values()
    k = mid.values()
    w = late.values()
    d = end.values()

    ax = plt.subplot()

    ax.set_title(game + ' ' + number + '\n' + 'Value functions for agent ' + agent)
    ax.set_xticks(ind + width)
    ax.set_xticklabels(('opening', 'early', 'mid', 'late', 'end'))

    rects1 = ax.bar(ind, y, width=0.2, color='b', align='center')
    rects2 = ax.bar(ind + width, z, width=0.2, color='g', align='center')
    rects3 = ax.bar(ind + width * 2, k, width=0.2, color='r', align='center')
    rects4 = ax.bar(ind + width * 3, w, width=0.18, color='m', align='center')
    rects5 = ax.bar(ind + width * 4, d, width=0.12, color='y', align='center')

    ax.legend((rects1[0], rects2[0], rects3[0], rects4[0], rects5[0]),
              ('LightRush', 'BuildBarracks', 'RangedRush', 'Expand', 'WorkerRush'))

    plt.show()
