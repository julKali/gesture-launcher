from matplotlib import pyplot

def plotGesture(d):
  x = [x for x,y in d]
  y = [y for x,y in d]
  pyplot.xlim(0, 1)
  pyplot.ylim(0, 1)
  pyplot.scatter(x,y)
  pyplot.show()

def plotFromFile(filename):
  with open(filename) as f:
    d = f.read().split("\n")
  d = [el.split(",") for el in d]
  d = [(float(el[0]), float(el[1])) for el in d]
  plotGesture(d)

if __name__ == "__main__":
  filename = input("Filename: ")

  while filename != "":
    plotFromFile(filename)
    filename = input("Filename: ")