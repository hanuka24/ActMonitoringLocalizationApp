import matplotlib.pyplot as plt
import numpy as np

def main():
    file = open('./data/walking_0.txt', 'r')
    value = getData(file)
    n = list(np.linspace(0, len(value), len(value)))

    plt.plot(n, value)
    plt.title("Activity Signal (Euclidean distance)")
    plt.xlabel("sample number")
    plt.ylabel("Euclidean distance of x, y, z")
    plt.show()

def getData(file):
    value = list()
    for line in file:
        data = line.split(';')
        print data[0]
        value.append(np.sqrt(float(data[1])**2 + float(data[2])**2 + float(data[3])**2))

    return value


if __name__ == '__main__':
    main()
