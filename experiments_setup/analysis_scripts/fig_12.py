import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import os

base_dir = os.path.dirname(os.path.abspath(__file__))

# targetType_cycleIntensity: {dijkstra_addedtoQ.csv, astar_addedtoQ.csv}
files = {
    
    'loop_0_67': {
        "A": r"C:\path\to\your\dijkstra_addedtoQ.csv",
        "B": r"C:\path\to\your\astar_addedtoQ.csv"
    },
    'loop_0_067':{
        "A": r"C:\path\to\your\dijkstra_addedtoQ.csv",
        "B": r"C:\path\to\your\astar_addedtoQ.csv"
    },
    'loop_0_0067': {
        "A": r"C:\path\to\your\dijkstra_addedtoQ.csv",
        "B": r"C:\path\to\your\astar_addedtoQ.csv"
    },
    
    'non_loop_0_67': {
        # This dijkstra file may take several hours to write, until at least 80,000 entries.
        "A": r"C:\path\to\your\dijkstra_addedtoQ.csv",
        "B": r"C:\path\to\your\astar_addedtoQ.csv"
    },
    'non_loop_0_067':{
        "A": r"C:\path\to\your\dijkstra_addedtoQ.csv",
        "B": r"C:\path\to\your\astar_addedtoQ.csv"
    },
    'non_loop_0_0067': {
        "A": r"C:\path\to\your\dijkstra_addedtoQ.csv",
        "B": r"C:\path\to\your\astar_addedtoQ.csv"
    },
  
}

state_space_size = {
    'loop_0_67': 11,
    'loop_0_067': 11,
    'loop_0_0067': 11,
    'non_loop_0_67': 11,
    'non_loop_0_067': 11,
    'non_loop_0_0067': 11
}

def compute_coverage(csv_path, tokens):
    if csv_path is None:
        return None
    
    df = pd.read_csv(csv_path)

    marking_cols = df.loc[:, "SalMediumStart":"SalRuffle"]

    unique_markings = marking_cols.drop_duplicates()

    coverage = len(unique_markings) / state_space_size[tokens]
    return coverage


labels = ["Strong", "Mild", "Weak"]
x = np.arange(len(labels))

mapping = {
    "Strong": ("loop_0_67", "non_loop_0_67"),
    "Mild": ("loop_0_067", "non_loop_0_067"),
    "Weak": ("loop_0_0067", "non_loop_0_0067"),
}

tokens_list = list(files.keys())
coverage_A = []
coverage_B = []
state_sizes = []

for t in tokens_list:
    if files[t]["A"] is not None:
        coverage_A.append(compute_coverage(files[t]["A"], t))
    else:
        coverage_A.append(np.nan)
    coverage_B.append(compute_coverage(files[t]["B"], t))
    state_sizes.append(state_space_size[t])

x = np.arange(len(tokens_list))
width = 0.35

fig, axes = plt.subplots(1, 2, figsize=(7, 3), sharey=True)

for idx, mode in enumerate(["cyclic", "non_cyclic"]):
    ax = axes[idx]

    coverage_A = []
    coverage_B = []

    for label in labels:
        loop_key, nonloop_key = mapping[label]

        key = loop_key if mode == "cyclic" else nonloop_key

        coverage_A.append(compute_coverage(files[key]["A"], key))
        coverage_B.append(compute_coverage(files[key]["B"], key))

    x = np.arange(len(labels))

    # ===== bar =====
    bars1 = ax.bar(x - width/2, coverage_A, width, label='Stochastic Dijkstra', color='#1a80bb')
    bars2 = ax.bar(x + width/2, coverage_B, width, label='Stochastic A*', color='#ea801c')

    idx_strong = labels.index("Strong")
    if mode == "non_cyclic":
        val = coverage_A[idx_strong]
        if not np.isnan(val):
            ax.text(idx_strong - width/2, val * 1.045 , '*',
                    ha='center', va='top', fontsize=12, color='red', fontweight='bold')

    # ===== axis =====
    ax.set_xticks(x)
    ax.set_xticklabels(labels)
    ax.set_title(f"{mode.replace('_', '-').capitalize()} target")

    ax.grid(axis='y', linestyle='--', alpha=0.4
)

axes[0].set_ylabel("Coverage")
axes[0].set_ylim(0, 1.05)

handles, labels_legend = axes[0].get_legend_handles_labels()
fig.legend(handles, labels_legend, loc='upper center', ncol=2,  bbox_to_anchor=(0.52, 0.95),
           frameon=False)

plt.tight_layout(rect=[0, 0, 1, 0.9])

png_path = os.path.join(base_dir, "cyclic_coverage.png")
pdf_path = os.path.join(base_dir, "cyclic_coverage.pdf")

plt.savefig(png_path, dpi=300)
plt.savefig(pdf_path, bbox_inches='tight')

print("files saved to:")
print(png_path)
print(pdf_path)

plt.show()