import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
import os
import numpy as np
from matplotlib.legend_handler import HandlerTuple
import matplotlib.lines as mlines
import matplotlib.patches as patches

# depth: [probability]
true_prob = {
  
    8: [0.003646404,
0.00866021,
0.00542033,
0.012873285,
0.035532331,
0.009200298,
0.021850707,
0.05281833,
0.01569043,
0.037264772
],
    10: [5.69E-04,
0.001290617,
7.28E-04,
0.00165079,
9.60E-04,
0.002177386,
0.001081643,
0.004091014,
0.002453877,
0.005232692
]
}

# depth: [time]
true_time = {
   
    8: [986.6874613,
1019.582198,
1061.216536,
1094.111273,
1206.172964,
1246.855051,
1279.749788,
1280.702038,
1496.623234,
1529.517971
],
    10: [950.2878821,
967.9626661,
982.3527165,
1000.0275,
1035.531772,
1053.206556,
1056.881791,
1067.356197,
1074.556575,
1099.421031
]
}

# depth: [probability]
astar_prob = {
    8: [0.003646404,
0.00866021,
0.00542033,
0.012873285,
0.009200298,
0.021850707,
0.01569043,
0.037264772,
0.026632441,
0.063252047
],
    10: [5.69E-04,
7.28E-04,
9.60E-04,
0.001081643,
0.001426683,
0.001246016,
0.001852185,
0.002421607,
0.003143841,
0.003131072
]
}

# depth: [time]
astar_time = {
   
    8: [986.6874613,
1019.582198,
1061.216536,
1094.111273,
1246.855051,
1279.749788,
1496.623234,
1529.517971,
1682.261749,
1715.156486
],
    10: [950.2878821,
982.3527165,
1035.531772,
1056.881791,
1110.060847,
1143.402801,
1217.931876,
1295.699362,
1403.57039,
1492.28849
]
}

# depth: [total probability]
total_prob = {
    8: 0.751314801,
    10: 0.683013455
}

depths = [8, 10]
# depth: initial tokens
tokens = {8: 4, 10: 5}

plt.figure(figsize=(7, 4))

layer_height =  1.2

all_times = []
for d in depths:
    all_times.extend(true_time[d])
    all_times.extend(astar_time[d])

norm = plt.Normalize(min(all_times), max(all_times))

cmap = mcolors.LinearSegmentedColormap.from_list(
    "blue_grad",
    ["#a9d1f6", "#08306b"]
)

layer_max_list = []

for i, depth in enumerate(depths):
    offset = i * layer_height
    
    times_t = true_time[depth]
    times_a = astar_time[depth]

    x_t = list(range(1, len(true_prob[depth]) + 1))
    x_a = list(range(1, len(astar_prob[depth]) + 1))

    layer_max = max(
        max(true_prob[depth]) if len(true_prob[depth]) > 0 else 0,
        max(astar_prob[depth]) if len(astar_prob[depth]) > 0 else 0
    )
    layer_max *= 1.3
    layer_max_list.append(layer_max) 

    y_t = [p / layer_max + offset for p in true_prob[depth]]
    y_a = [p / layer_max + offset for p in astar_prob[depth]]

    overlap_pairs = []  
    for i, (p_a, t_a) in enumerate(zip(astar_prob[depth], astar_time[depth])):
        for j, (p_t, t_t) in enumerate(zip(true_prob[depth], true_time[depth])):
            if np.isclose(p_a, p_t, rtol=1e-6) and np.isclose(t_a, t_t, rtol=1e-6):
                overlap_pairs.append((i, j))
                break
    
    overlap_astar_idx = set(i for i, _ in overlap_pairs)
    overlap_true_idj = set(j for _, j in overlap_pairs)

    x_t = [x - 0.1 for x in x_t]
    x_a = [x + 0.08 for x in x_a]

    for xj, yj, tj, idj in zip(x_t, y_t, times_t, range(len(x_t))):
    
        if idj in overlap_true_idj:
            plt.scatter(
                xj, yj,
                marker='s',
                c=tj,
                cmap=cmap,
                norm=norm,
                label='Found by A*' if (i == 0 and idj == min(overlap_true_idj)) else ""
            )
        else:
            plt.scatter(
                xj, yj,
                marker='s',
                s = 60,             
                c=tj,
                cmap=cmap,
                norm=norm,
                edgecolors='#D55E5E',
                alpha=0.7,
                linewidths=1,
                facecolors='none',
                label='Top-10 (time)' if (i == 0 and idj == 0) else ""
            )

    sc = plt.scatter(
                x_a, y_a,
                c=times_a,
                cmap=cmap,
                norm=norm,
                marker='x',
                label="A*" if (i == 0 and i == 0) else ""
            )
    
    plt.text(9.5, offset + 0.9,
            f"initial tokens\n= {tokens[depth]}",
            va='center',
            ha='center',
            fontsize=9)


for i in range(1, len(depths)):
    plt.axhline(i * layer_height - (layer_height-1)/2, linestyle='--', alpha=0.3)

plt.xticks(range(1, 11))

yticks = []
yticklabels = []

for i in range(len(depths)):
    base = i * layer_height
    lm = layer_max_list[i]
    yticks.extend([base, base + 0.5, base + 1.0])
    yticklabels.extend(["0", f"{lm/2:.2f}",
    f"{lm:.2f}"])

plt.yticks(yticks, yticklabels)

plt.xlim(0.5, 10.5)
plt.ylim(0, len(depths) * layer_height-(layer_height-1)/2)
plt.xlabel("Path index")
plt.ylabel("Probability Contribution")

cbar = plt.colorbar(sc) 
cbar.set_label("Time (sec)")

true_handle = mlines.Line2D([], [],
                                color='black',
                                marker='s',
                                linestyle='None',
                                markersize=6,
                                label='Reference Paths')

square = mlines.Line2D([], [], marker='s', linestyle='None',
                        markerfacecolor='black',
                        markeredgecolor='black',
                        markersize=4,markeredgewidth=4)

cross = mlines.Line2D([], [], marker='x', linestyle='None',
                      color='white', markersize=6, markeredgewidth=1.5)

true_overlap_handle = (square, cross)

rect_handle = patches.Rectangle(
    (0, 0), 1, 1,
    facecolor='none',
    edgecolor='gray',
    linestyle='--',
    linewidth=1.2,
    label='Matched pair'
)

astar_handle = mlines.Line2D([], [],
                             color='black',
                             marker='x',
                             linestyle='None',
                             markersize=6,
                             label='A*')

missed_handle = mlines.Line2D(
    [], [],
    marker='s',
    linestyle='None',
    markersize=7,
    markerfacecolor='none',   
    markeredgecolor='#D55E5E',    
    markeredgewidth=1.5,
    label='Missed by A*'
)

plt.legend(handles=[true_handle, missed_handle, astar_handle],
           labels=[
        "Reference Path",
        "Missed by Stochastic A*",
        "Stochastic A*"
        ], handler_map={tuple: HandlerTuple()},
    frameon=False, loc='upper left')

plt.grid(True, axis='x', linestyle='--', alpha=0.3)

plt.tight_layout() 

base_dir = os.path.dirname(os.path.abspath(__file__))

png_path = os.path.join(base_dir, "acyclic_path_accuracy.png")
pdf_path = os.path.join(base_dir, "acyclic_path_accuracy.pdf")

plt.savefig(png_path, dpi=300)
plt.savefig(pdf_path, bbox_inches='tight')

print("files saved to:")
print(png_path)
print(pdf_path)

plt.show()