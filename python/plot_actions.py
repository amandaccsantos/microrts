import matplotlib.pyplot as plt

if __name__ == '__main__':
    agent0 = []
    agent1 = []
    f = open("../action_ep.txt", 'r')
    for line in f:
        if line.startswith("reward0"):
            line = line.strip()
            agent, value = line.split("reward0")
            agent0.append(value)
        elif line.startswith("reward1"):
            line = line.strip()
            agent, value = line.split("reward1")
            agent1.append(value)

    plt.switch_backend('TkAgg')
    plt.plot(list(range(0, 100)), agent0, color='b', label='Agent 1')
    plt.plot(list(range(0, 100)), agent1, color='r', label='Agent 2')
    plt.axis([0, 100, -2, 2])
    plt.legend(loc=2, borderaxespad=0.)

    mng = plt.get_current_fig_manager()
    mng.window.state('zoomed')

    plt.show()
