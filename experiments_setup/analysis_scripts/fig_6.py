import matplotlib.pyplot as plt
import numpy as np
import os

base_dir = os.path.dirname(os.path.abspath(__file__))

# data
tokens = np.arange(1, 11)

tree_1 = np.array([4, 25, 234, 7174, 116940, np.nan, np.nan, np.nan, np.nan, np.nan], dtype=float)

graph_1 = np.array([4, 11, 25, 51, 94, 159, 251, 375, 536, 739], dtype=float)

tree_2 = np.array([3, 10, 44, 249, 1585, 10118, 61812, np.nan, np.nan, np.nan], dtype=float)

graph_2 = np.array([3, 7, 14, 26, 43, 65, 92, 124, 161, 203], dtype=float)


depth = [2, 4, 6, 9, 11, 13, 15, 17, 19, 21]
depth_tree = [2, 4, 6, 9, 11]

# plot
plt.figure(figsize=(7, 4))
ax = plt.gca()
ax.plot(tokens, tree_1, marker='o', label='tree of cyclic model', color="#308443",linestyle='--')
ax.plot(tokens, graph_1, marker='o', label='graph of cyclic model', color = "#922525",linestyle='--')
ax.plot(tokens, tree_2, marker='o', label='tree of acyclic model', color = "#308443")
ax.plot(tokens, graph_2, marker='o', label='graph of acyclic model', color = "#922525")


plt.yscale('log')
plt.minorticks_off()


ax.set_xticks(tokens)
ax.set_xlabel("Number of Initial Tokens")

# top x-axis
ax_top = ax.twiny()

ax_top.set_xlim(ax.get_xlim())

ax_top.set_xticks(tokens)

# show depth instead of tokens on top axis
ax_top.set_xticklabels(depth)

ax_top.set_xlabel("Exploration Depth")

ax.set_ylabel("Number of Markings")


ax.legend(fontsize=10, frameon=False, loc='upper right',bbox_to_anchor=(1, 1))
plt.grid(True, which="both", linestyle="--", alpha=0.4)

plt.tight_layout()

png_path = os.path.join(base_dir, "states.png")
pdf_path = os.path.join(base_dir, "states.pdf")

plt.savefig(png_path, dpi=300)
plt.savefig(pdf_path, bbox_inches='tight')

print("files saved to:")
print(png_path)
print(pdf_path)

plt.show()