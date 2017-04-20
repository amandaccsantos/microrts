import sys

if __name__ == '__main__':
    with open(sys.argv[1] + 'output.txt', 'w+') as results_file:
        for i in range(1, 4):
            if i < 10:
                output = sys.argv[1] + 'out0' + str(i) + '/output.txt'
            else:
                output = sys.argv[1] + 'out' + str(i) + '/output.txt'
            with open(output, 'r') as f:
                if i == 1:
                    for line in f:
                        results_file.write(line)
                else:
                    num_line = 0
                    results_file.seek(0)
                    lines = results_file.readlines()
                    lines_in = f.readlines()
                    for line in lines_in:
                        line_split = line.split()
                        value = float(line_split[1].replace(',', '.'))

                        line_result = lines[num_line]
                        line_result = line_result.split()
                        value_result = float(line_result[1].replace(',', '.'))
                        value += value_result

                        lines_in[num_line] = line_split[0] + ' ' + str(value) + '\n'
                        num_line += 1

                    results_file.seek(0)
                    results_file.truncate()
                    for line in lines_in:
                        results_file.write(line)
