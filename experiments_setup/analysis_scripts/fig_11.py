import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import os
import csv

# targetType_cycleIntensity: {dijkstra_expanded.csv, astar_expanded.csv}
files = {

    'loop_0_67': {
        "A": r"C:\path\to\your\dijkstra_expanded.csv",
        "B": r"C:\path\to\your\astar_expanded.csv"
    },
    'loop_0_067':{
        "A": r"C:\path\to\your\dijkstra_expanded.csv",
        "B": r"C:\path\to\your\astar_expanded.csv"
    },
    'loop_0_0067': {
        "A": r"C:\path\to\your\dijkstra_expanded.csv",
        "B": r"C:\path\to\your\astar_expanded.csv"
    },
    'non_loop_0_67': {
        # "A": r"C:\path\to\your\dijkstra_expanded.csv",
        # this data is too large to be read, so we set it to None and handle it in the code
        # we set it to 'over 10^6' in the plot
        # this value could be obtained from reachability dialog interface
        "A": None,
        "B": r"C:\path\to\your\astar_expanded.csv"
    },
    'non_loop_0_067':{
        "A": r"C:\path\to\your\dijkstra_expanded.csv",
        "B": r"C:\path\to\your\astar_expanded.csv"
    },
    'non_loop_0_0067': {
        "A": r"C:\path\to\your\dijkstra_expanded.csv",
        "B": r"C:\path\to\your\astar_expanded.csv"
    },
}

def count_rows(file_path):
    if file_path is None:
        return None
    with open(file_path, 'r') as f:
        reader = csv.reader(f)
        count = sum(1 for _ in reader) - 1
    return max(count, 1)  

labels = ["Strong", "Mild", "Weak"]
x = np.arange(len(labels))
width = 0.35

mapping = {
    "Strong": ("loop_0_67", "non_loop_0_67"),
    "Mild": ("loop_0_067", "non_loop_0_067"),
    "Weak": ("loop_0_0067", "non_loop_0_0067"),
}

dijkstra_loop, astar_loop = [], []
dijkstra_nonloop, astar_nonloop = [], []

for label in labels:
    loop_key, nonloop_key = mapping[label]

    d_l = count_rows(files[loop_key]["A"])
    a_l = count_rows(files[loop_key]["B"])

    d_nl = count_rows(files[nonloop_key]["A"])
    a_nl = count_rows(files[nonloop_key]["B"])

    if d_nl is None:
        d_nl = 1_000_000

    dijkstra_loop.append(d_l)
    astar_loop.append(a_l)

    dijkstra_nonloop.append(d_nl)
    astar_nonloop.append(a_nl)

fig, axes = plt.subplots(1, 2, figsize=(7, 3), sharey=True)

color_dijkstra = '#1a80bb'
color_astar = '#ea801c'

# --- Loop subfigure ---
axes[0].bar(x - width/2, dijkstra_loop, width, label='Stochastic Dijkstra', color=color_dijkstra)
axes[0].bar(x + width/2, astar_loop, width, label='Stochastic A*', color=color_astar)
axes[0].set_title("Cyclic Target")
axes[0].set_xticks(x)
axes[0].set_xticklabels(labels)
axes[0].set_ylabel("Number of Expanded Markings")
axes[0].set_yscale('log')
axes[0].minorticks_off()
axes[0].grid(True, which="major", axis='y', linestyle='--', alpha=0.4)
axes[0].margins(y=0.1)

# --- Non-loop subfigure ---
axes[1].bar(x - width/2, dijkstra_nonloop, width, label='Stochastic Dijkstra', color=color_dijkstra)
axes[1].bar(x + width/2, astar_nonloop, width, label='Stochastic A*', color=color_astar)
axes[1].set_title("Non-cyclic Target")
axes[1].set_xticks(x)
axes[1].set_xticklabels(labels)
axes[1].set_yscale('log')
axes[1].minorticks_off()
axes[1].grid(True, which="major", axis='y', linestyle='--', alpha=0.4)
axes[1].margins(y=0.1)

axes[1].text(x[0] - width/2, dijkstra_nonloop[0]*2, r"$>10^6$",
             ha='center', va='top', color='red', fontsize=10)

handles, labels_legend = axes[0].get_legend_handles_labels()
fig.legend(handles, labels_legend, loc='upper center', ncol=2,frameon=False, bbox_to_anchor=(0.52, 0.95))

plt.tight_layout(rect=[0, 0, 1, 0.9])

base_dir = os.path.dirname(os.path.abspath(__file__))

png_path = os.path.join(base_dir, "cyclic_expansion.png")
pdf_path = os.path.join(base_dir, "cyclic_expansion.pdf")

plt.savefig(png_path, dpi=300)
plt.savefig(pdf_path, bbox_inches='tight')

print("files saved to:")
print(png_path)
print(pdf_path)

plt.show()