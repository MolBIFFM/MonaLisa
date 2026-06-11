import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import os

base_dir = os.path.dirname(os.path.abspath(__file__))

# intial: {dijkstra_addedtoQ.csv, astar_addedtoQ.csv}
files = {
    2: {
        "A": r"C:\path\to\your\dijkstra_addedtoQ.csv",
        "B": r"C:\path\to\your\astar_addedtoQ.csv"
    },
    3: {
        "A": r"C:\path\to\your\dijkstra_addedtoQ.csv",
        "B": r"C:\path\to\your\astar_addedtoQ.csv"
    },
    4: {
        "A": r"C:\path\to\your\dijkstra_addedtoQ.csv",
        "B": r"C:\path\to\your\astar_addedtoQ.csv"
    },
    5: {
        "A": r"C:\path\to\your\dijkstra_addedtoQ.csv",
        "B": r"C:\path\to\your\astar_addedtoQ.csv"
    },   
}

# intial tokens: state space size
state_space_size = {
    2: 7,
    3: 14,
    4: 26,
    5: 43,
    6: 65,
    7: 92,
    8: 124,
    9: 161
}

def compute_coverage(csv_path, tokens):
    df = pd.read_csv(csv_path)

    marking_cols = df.loc[:, "SalMediumStart":"SalRuffle"]

    unique_markings = marking_cols.drop_duplicates()

    coverage = len(unique_markings) / state_space_size[tokens]
    return coverage

tokens_list = sorted(files.keys())
coverage_A = []
coverage_B = []
state_sizes = []

for t in tokens_list:
    coverage_A.append(compute_coverage(files[t]["A"], t))
    coverage_B.append(compute_coverage(files[t]["B"], t))
    state_sizes.append(state_space_size[t])

x = np.arange(len(tokens_list))
width = 0.35

fig, ax1 = plt.subplots(figsize=(4,3))

# ===== bar plot (coverage) =====
bars1 = ax1.bar(x - width/2, coverage_A, width, label='Stochastic Dijkstra', color='#1a80bb')
bars2 = ax1.bar(x + width/2, coverage_B, width, label='Stochastic A*', color="#ea801c")

ax1.set_xlabel("Number of Initial Tokens")
ax1.set_ylabel("Coverage")
ax1.set_xticks(x)
ax1.set_xticklabels(tokens_list)
ax1.set_ylim(0, 1.05)
ax1.tick_params(axis='y', which='both', direction='in')

# ===== secondary axis (state space size) =====
ax2 = ax1.twinx()
ax2.plot(x, state_sizes, marker='o', linestyle='--', label='State space size', color="#000000")
ax2.set_ylabel("State space size")
ax2.set_yticks(state_sizes)
ax2.tick_params(axis='y', which='both', direction='out')

lines1, labels1 = ax1.get_legend_handles_labels()
lines2, labels2 = ax2.get_legend_handles_labels()

plt.grid(axis='y', linestyle='--', alpha=0.4)

plt.tight_layout()

png_path = os.path.join(base_dir, "acyclic_coverage.png")
pdf_path = os.path.join(base_dir, "acyclic_coverage.pdf")
legend_png = os.path.join(base_dir, "acyclic_coverage_legend.png")
legend_path = os.path.join(base_dir, "acyclic_coverage_legend.pdf")

plt.savefig(png_path, dpi=300)
plt.savefig(pdf_path, bbox_inches='tight') 


plt.show()

fig_legend = plt.figure(figsize=(2, 0.5))
fig_legend.legend(lines1 + lines2, labels1 + labels2, loc='lower center',
              ncol=3,
            borderaxespad=0,
            frameon=False)
plt.tight_layout()
fig_legend.savefig(legend_png, dpi=300)
fig_legend.savefig(legend_path, bbox_inches='tight') 

plt.show()