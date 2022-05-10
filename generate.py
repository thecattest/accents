from hashlib import md5
from glob import glob
from random import shuffle
from json import dump
import sys


def generate_category(name, lines):
     category = {"id": name.split(".")[0], "taskType": lines[0], "title": lines[1], "tasks": lines[2:]}
     shuffle(category["tasks"])
     category["hash"] = str(md5(''.join(category["tasks"]).encode('utf-8')).hexdigest())
     return category


try:
    version = int(sys.argv[1])
except (IndexError, ValueError):
    version = 11
categories = []
for fn in glob("*.ege"):
    print(fn)
    with open(fn) as f:
        lines = list(map(lambda x: x.strip(), f.readlines()))
        categories.append(generate_category(fn, lines))
dictionary = {"version": version, "categories": sorted(categories, key=lambda c: int(c["id"]))}
print(dictionary)
with open("dictionary3.json", "w") as f:
    dump(dictionary, f)
