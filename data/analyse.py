def read_data(filename):
  with open(filename) as f:
    d = f.readlines()
  d = [el.split(",") for el in d]
  return [(float(el[0]), float(el[1])) for el in d]

def getAbsDiffs(d):
  diffs = []
  for i in range(1, len(d)):
    curr = d[i]
    last = d[i-1]
    diffs.append({
      "idx": i,
      "diff": abs(curr[0]-last[0]) + abs(curr[1]-last[1])
    })
  return diffs

def compressToLength(d, length):
  currLength = len(d)
  if currLength < length:
    raise Exception("no")
  if currLength == length:
    return d
  removeCount = currLength - length

  # remove {removeCount} elements that have the smallest abs diff
  diffs = getAbsDiffs(d)
  diffsSorted = sorted(diffs, key=lambda el: el["diff"])
  toRemoveIndices = [el["idx"] for el in diffsSorted[:removeCount]]
  return listWithoutIndices(d, toRemoveIndices)

def listWithoutIndices(data, indices):
  indicesLength = len(indices)
  if (indicesLength == 0): return data
  dataWithoutIndices = []
  indices = sorted(indices)
  nextToRemoveIdx = 0
  for i in range(0, len(data)):
    if i == indices[nextToRemoveIdx]:
      nextToRemoveIdx += 1
      if nextToRemoveIdx == indicesLength:
        if i + 1 < len(data):
          dataWithoutIndices += data[i+1:]
        break
      continue
    dataWithoutIndices.append(data[i])
  return dataWithoutIndices

def totalDifference(coords1, coords2):
  if len(coords1) is not len(coords2):
    raise Exception("no")
  total = 0
  for c1, c2 in zip(coords1, coords2):
    total += abs(c1[0]-c2[0]) + abs(c1[1]-c2[1])
  return total / len(coords1)

def score(coords1, coords2):
  return 1 - totalDifference(coords1, coords2)

def compressByFilename(filename, length):
  d = read_data(filename)
  return compressToLength(d, length)

length = 20

horizontal1 = compressByFilename("horizontal1.txt", length)
horizontal2 = compressByFilename("horizontal2.txt", length)
horizontal3 = compressByFilename("horizontal3.txt", length)
vertical1 = compressByFilename("vertical1.txt", length)
vertical2 = compressByFilename("vertical2.txt", length)
vertical3 = compressByFilename("vertical3.txt", length)
rightCircle1 = compressByFilename("rightCircle1.txt", length)
rightCircle2 = compressByFilename("rightCircle2.txt", length)
rightCircle3 = compressByFilename("rightCircle3.txt", length)
leftCircle1 = compressByFilename("leftCircle1.txt", length)
leftCircle2 = compressByFilename("leftCircle2.txt", length)
leftCircle3 = compressByFilename("leftCircle3.txt", length)
print(score(rightCircle1, rightCircle2))
print(score(rightCircle1, rightCircle3))
print(score(rightCircle1, leftCircle1))
print(score(rightCircle1, leftCircle2))
print(score(rightCircle1, leftCircle3))
#compressed = compressToLength([(1, 2), (1.1, 1.9), (1.4, 2)], 1)
#print(listWithoutIndices([1, 2, 3, 4]))