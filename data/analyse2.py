from math import sqrt
from plot import plotGesture
from itertools import combinations

def read_data(filename):
  with open(filename) as f:
    d = f.readlines()
  d = [el.split(",") for el in d]
  return [(float(el[0]), float(el[1])) for el in d]

def convert_to_vec_list(points):
  vecs = []
  for i in range(1, len(points)):
    curr = points[i]
    last = points[i-1]
    vecs.append({
      "start": last,
      "vec": (curr[0]-last[0], curr[1]-last[1])
    })
  return vecs

def calculate_total_length(vecs):
  total = 0
  for vec in vecs:
    total += calculate_vec_length(vec["vec"])
  return total

def calculate_vec_length(vec):
  return sqrt(sum(c**2 for c in vec))

def normalize_vec(vec, length):
  return [el/length for el in vec]

def multiply_vec_with_scalar(vec, scalar):
  return [el*scalar for el in vec]

def add_vec_to_point(point, vec):
  zipped = zip(point, vec)
  return [p+v for p,v in zipped]

def find_point_at_offset(vecs, offset):
  remaining = offset
  for vec in vecs:
    length = calculate_vec_length(vec["vec"])
    if (length < remaining):
      remaining -= length
      continue
    # add remaining to point in dir of vec
    orig_point = vec["start"]
    normalized_dir_vec = normalize_vec(vec["vec"], length)
    dir_vec = multiply_vec_with_scalar(normalized_dir_vec, remaining)
    return add_vec_to_point(orig_point, dir_vec)
  raise Exception("cannot find point for " + str(offset) + ", remaining: " + str(remaining))

def find_piece_points_for_vecs(vecs, piece_count):
  points = []
  length = calculate_total_length(vecs)
  piece_length = length / piece_count
  for i in range(piece_count):
    offset = piece_length * i
    # print(str(i) + ": " + str(offset))
    point = find_point_at_offset(vecs, offset)
    points.append(point)
  return points

def get_points_for_file(filename):
  data = read_data(filename)
  vecs = convert_to_vec_list(data)
  return find_piece_points_for_vecs(vecs, 100)

def totalDifference(coords1, coords2):
  if len(coords1) is not len(coords2):
    raise Exception("no")
  total = 0
  for c1, c2 in zip(coords1, coords2):
    total += abs(c1[0]-c2[0]) + abs(c1[1]-c2[1])
  return total / len(coords1)

def calc_score(coords1, coords2):
  return 1 - totalDifference(coords1, coords2)

gesture_files = [
  "horizontal1.txt",
  "horizontal2.txt",
  "horizontal3.txt",
  "vertical1.txt",
  "vertical2.txt",
  "vertical3.txt",
  "rightCircle1.txt",
  "rightCircle2.txt",
  "rightCircle3.txt",
  "leftCircle1.txt",
  "leftCircle2.txt",
  "leftCircle3.txt",
]

def analyse():
  for g1f, g2f in combinations(gesture_files, 2):
    #print(g1f + " - " + g2f)
    g1 = get_points_for_file(g1f)
    #print("first done")
    g2 = get_points_for_file(g2f)
    score = calc_score(g1, g2)
    print(g1f + " - " + g2f + ": " + str(score))

#x = get_points_for_file("vertical1.txt")
#plotGesture(x)
analyse()