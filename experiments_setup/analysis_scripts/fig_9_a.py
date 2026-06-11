import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import os
import csv
import matplotlib.ticker as ticker
from collections import Counter
import argparse

# intial tokens: 2, 3, 4, 5
tokens = list(range(2, 6))

dijkstra_expanded = []
astar_expanded = []

# User-defined data directory
# Please set this path to the folder containing experimental results
# Required directory structure:
# base_path/
# ├── dijkstra/
# │   ├── {t}_dijkstra_expanded.csv
# │   ├── ...
# ├── astar/
# │   ├── {t}_astar_expanded.csv
# │   ├── ...
#
# where:
# - t represents the number of initial tokens
base_path = r"C:\path\to\your\data\folder"

# read csv and count rows (excluding header)
def count_rows(file_path):
    with open(file_path, 'r') as f:
        reader = csv.reader(f)
        row_count = sum(1 for row in reader)
    return row_count - 1  # exclude header


for t in tokens:
    dijkstra_file = os.path.join(base_path, "dijkstra", f"{t}_dijkstra_expanded.csv")
    astar_file = os.path.join(base_path, "astar", f"{t}_astar_expanded.csv")

    dijkstra_expanded.append(count_rows(dijkstra_file))
    astar_expanded.append(count_rows(astar_file))
        
fig, ax = plt.subplots(figsize=(4, 3))


ax.plot(tokens, dijkstra_expanded, marker='o', label='Dijkstra', color='#1a80bb')
ax.plot(tokens, astar_expanded, marker='o', label='A*', color='#ea801c')

ax.set_xlabel("Number of Initial Tokens")
ax.set_ylabel("Number of Expanded Markings")

ax.grid(True, which="major", axis='y', linestyle='--')
ax.tick_params(axis='y', which='both', direction='in')
ax.xaxis.set_major_locator(ticker.MultipleLocator(1)) 

ax2 = ax.twinx()
ax2.set_yticks((0,1))
ax2.set_ylim(0,1.05)
ax2.tick_params(axis='y', labelcolor=(0, 0, 0, 0), color=(0, 0, 0, 0))
ax2.set_ylabel("None",color=(0, 0, 0, 0))

plt.tight_layout()

base_dir = os.path.dirname(os.path.abspath(__file__))

png_path = os.path.join(base_dir, "acyclic_expansion.png")
pdf_path = os.path.join(base_dir, "acyclic_expansion.pdf")

plt.savefig(png_path, dpi=300)
plt.savefig(pdf_path, bbox_inches='tight') 

print("file saved to:")
print(png_path)
print(pdf_path)

plt.show()