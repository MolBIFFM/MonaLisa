import matplotlib.pyplot as plt
import matplotlib.lines as mlines
import matplotlib.colors as mcolors
import os


# Dijkstra data: depth:[probabilities]
dijkstra_prob = {
    4: [0.71969697, 0.189393939],
    6: [0.478503781, 0.187181422, 0.07142449, 0.064662673, 0.024673915],
    8: [0.305578049, 0.152895167, 0.063252047, 0.05281833, 0.037264772,
        0.035532331, 0.026632441, 0.021850707, 0.01569043, 0.012873285],
    10: [0.190438436, 0.113928156, 0.050409798, 0.039356999, 0.029698796,
          0.026476527, 0.022516206, 0.02069983, 0.020646118, 0.017414294]
}
# Dijkstra data: depth:[times]
dijkstra_time = {
    4: [1861.642743, 1387.958533],
    6: [1692.218262, 1596.093429, 1514.423737, 1160.68673, 1079.017039],
    8: [1628.653584, 1716.108737, 1715.156486, 1280.702038, 1529.517971, 
        1206.172964, 1682.261749, 1279.749788, 1496.623234, 1094.111273],
    10: [1601.85106, 1798.199493, 1848.174375, 1362.792795, 1662.53586, 
        1288.26372, 1609.356805, 1256.198886, 1856.651873, 1412.767677]
}

# A* data: depth:[probabilities]
astar_prob = {
    4: [0.189393939,0.71969697],
    6: [0.024673915,
0.064662673,
0.07142449,
0.187181422,
0.478503781
],
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

# A* data: depth:[times]
astar_time = {
    4: [1387.958533,
1861.642743
],
    6: [1079.017039,
1160.68673,
1514.423737,
1596.093429,
1692.218262
],
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

# depth: total probability
total_prob = {
    4: 0.909090909,
    6: 0.826446281,
    8: 0.751314801,
    10: 0.683013455
}

depths = [4, 6, 8, 10]

# depth: initial tokens
tokens = {4: 2, 6: 3, 8: 4, 10: 5}

plt.figure(figsize=(7, 5))

layer_height = 1.2

all_times = []
for d in depths:
    all_times.extend(dijkstra_time[d])
    all_times.extend(astar_time[d])

norm = plt.Normalize(min(all_times), max(all_times))

cmap = mcolors.LinearSegmentedColormap.from_list(
    "blue_grad",
    ["#a9d1f6", "#08306b"]
)

for i, depth in enumerate(depths):
    offset = i * layer_height
    
    times_d = dijkstra_time[depth]
    times_a = astar_time[depth]

    x_d = list(range(1, len(dijkstra_prob[depth]) + 1))
    x_a = list(range(1, len(astar_prob[depth]) + 1))
    
    y_d = [p / total_prob[depth] + offset for p in dijkstra_prob[depth]]
    y_a = [p / total_prob[depth] + offset for p in astar_prob[depth]]
    
    x_d = [x - 0.08 for x in x_d]
    x_a = [x + 0.08 for x in x_a]

    sc = plt.scatter(
        x_d, y_d,
        c=times_d,
        cmap=cmap,
        norm=norm,
        marker='o',
        edgecolors='black',
        linewidths=0.3,
        label="Dijkstra" if i == 0 else ""
    )
    
    sc = plt.scatter(
        x_a, y_a,
        c=times_a,
        cmap=cmap,
        norm=norm,
        marker='x',
        label="A*" if i == 0 else ""
    )
    
    plt.text(9.5, offset + 0.85,
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
    yticks.extend([base, base + 0.5, base + 1.0])
    yticklabels.extend(["0", "0.5", "1"])

plt.yticks(yticks, yticklabels)


plt.xlim(0.5, 10.5)
plt.ylim(0, len(depths) * layer_height-(layer_height-1)/2)
plt.xlabel("Path index")
plt.ylabel("Probability Contribution")


cbar = plt.colorbar(sc) #, pad=0.17
cbar.set_label("Time (sec)")


dijkstra_handle = mlines.Line2D([], [],
                                color='black',
                                marker='o',
                                linestyle='None',
                                markersize=6,
                                label='Stochastic Dijkstra')

astar_handle = mlines.Line2D([], [],
                             color='black',
                             marker='x',
                             linestyle='None',
                             markersize=6,
                             label='Stochastic A*')

plt.legend(handles=[dijkstra_handle, astar_handle], frameon=False, loc='upper left')


plt.grid(True, axis='x', linestyle='--', alpha=0.3)

plt.tight_layout() 


base_dir = os.path.dirname(os.path.abspath(__file__))

png_path = os.path.join(base_dir, "acyclic_path_prob_time.png")
pdf_path = os.path.join(base_dir, "acyclic_path_prob_time.pdf")

plt.savefig(png_path, dpi=300)
plt.savefig(pdf_path, bbox_inches='tight')

print("files saved to:")
print(png_path)
print(pdf_path)

plt.show()