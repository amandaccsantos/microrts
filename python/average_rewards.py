import sys
import fileinput

if __name__ == '__main__':
    oi = open(sys.argv[1] + 'output.txt', 'w')
    results_file = open(sys.argv[1] + 'output.txt', 'r+')
    for i in range(1, 4):
        if i < 10:
            output = sys.argv[1] + 'out0' + str(i) + '/output.txt'
        else:
            output = sys.argv[1] + 'out' + str(i) + '/output.txt'
        f = open(output, 'r')

        if i == 1:
            for line in f:
                results_file.write(line)
        else:
            num_line = 0
            results_file.seek(0)
            lines = results_file.readlines()
            for line in f:
                line = line.split()
                value = float(line[1].replace(',', '.'))

                line_result = lines[num_line]
                line_result = line_result.split()
                value_result = float(line_result[1].replace(',', '.'))
                value += value_result
                num_line += 1

                #for line in fileinput.input(sys.argv[1] + 'output.txt', inplace=True):
                    #print "%s %s" % (line[0], str(value))
