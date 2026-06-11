import matplotlib.pyplot as plt
import matplotlib.lines as mlines
import matplotlib.colors as mcolors
import os
import numpy as np
from matplotlib.colors import LogNorm

# Dijkstra data: targettype:{probabilities, times, depths}
dijkstra = {
    'cyclic': {
        "prob": [7.38E-04,
7.32E-04,
7.26E-04,
7.20E-04,
7.14E-04,
7.08E-04,
7.02E-04,
7.00E-04,
6.97E-04,
6.95E-04,

                ],
        "time": [792.4412791,
819.9633307,
847.4853822,
875.0074338,
902.5294854,
930.0515369,
957.5735885,
818.7423849,
985.0956401,
846.2644364

                ],
        "depth": [4,
6,
8,
10,
12,
14,
16,
6,
18,
8

                ]
    },
    'non_cyclic': {
        "prob": [5.50E-06,
                5.46E-06,
                5.41E-06,
                5.37E-06,
                5.33E-06,
                5.28E-06,
                5.24E-06,
                5.23E-06,
                5.20E-06,
                5.18E-06
                ],
        "time": [792.4412791,
                819.9633307,
                847.4853822,
                875.0074338,
                902.5294854,
                930.0515369,
                957.5735885,
                818.7423849,
                985.0956401,
                846.2644364
                ],
        "depth": [4,
                6,
                8,
                10,
                12,
                14,
                16,
                6,
                18,
                8
                ]
    }
}

# A* data: targettype:{probabilities, times, depths}
astar = {
    'cyclic': {
        "prob": [1.99E-06,
2.09E-06,
1.08E-07,
1.13E-07,
5.83E-09,
6.12E-09,
3.16E-10,
3.31E-10,
1.71E-11,
1.79E-11
            ],
        "time": [266.8014531,
266.8712018,
268.9578014,
269.0275501,
271.1141498,
271.1838985,
273.2704981,
273.3402468,
275.4268465,
275.4965952
                ],
        "depth": [4,
4,
6,
6,
8,
8,
10,
10,
12,
12
                ]
    },
    'non_cyclic': {
        "prob": [1.56E-08,
8.44E-10,
4.57E-11,
2.47E-12,
1.34E-13,
7.25E-15,
3.92E-16,
2.12E-17,
7.98E-10,
1.15E-18
            ],
        "time": [266.8712018,
269.0275501,
271.1838985,
273.3402468,
275.4965952,
277.6529435,
279.8092919,
281.9656403,
283.4590197,
284.1219886
                ],
        "depth": [4,
6,
8,
10,
12,
14,
16,
18,
8,
20
            ]
    },

}

# targettype: total probability
total_prob = {
    'cyclic': 7.75E-04,
    'non_cyclic': 5.52E-06
}

def extract_arrays(data_dict, tokens):
    data = data_dict[tokens]
    
    probs = data["prob"]
    times = data["time"]
    depths = data["depth"]
    
    assert len(probs) == len(times) == len(depths), "Data length mismatch"
    
    return probs, times, depths

plt.figure(figsize=(7, 4))

color_dijkstra = "tab:blue"
color_astar = "tab:orange"

layer_height = 1.2

all_types = sorted(set(dijkstra.keys()) | set(astar.keys()))

for type in all_types:
    
    times_layer = []
    
    if type in dijkstra:
        times_layer += dijkstra[type]["time"]
    if type in astar:
        times_layer += astar[type]["time"]
    
norm = plt.Normalize(min(times_layer), max(times_layer))

cmap = mcolors.LinearSegmentedColormap.from_list(
    "blue_grad",
    ["#a9d1f6", "#08306b"]
)

for i, type in enumerate(all_types):

    offset = i * layer_height
    
    # Dijkstra
    if type in dijkstra:
        probs_d = dijkstra[type]["prob"]
        times_d = dijkstra[type]["time"]
        depths_d = dijkstra[type]["depth"]
    else:
        probs_d, times_d, depths_d = [], [], []

    # A*
    if type in astar:
        probs_a = astar[type]["prob"]
        times_a = astar[type]["time"]
        depths_a = astar[type]["depth"]

    else:
        probs_a, times_a, depths_a = [], [], []

    # ===== Dijkstra =====
    x_d = list(range(1, len(probs_d) + 1))
    x_d = [x - 0.1 for x in x_d]
    
    y_d = [p / total_prob[type] + offset for p in probs_d]

    sc_d = plt.scatter(
        x_d, y_d,
        c=times_d,
        cmap=cmap,
        norm=norm,
        marker='o',
        label="Dijkstra" if i == 0 else ""
    )

    for x, y, d in zip(x_d, y_d, depths_d):
        plt.annotate(
        str(d),
        (x, y),
        xytext=(0, -8),          
        textcoords='offset points',
        fontsize=7,
        ha='center',            
        va='top'                 
        )
    
    # ===== A* =====
    x_a = list(range(1, len(probs_a) + 1))
    x_a = [x + 0.1 for x in x_a]
    
    y_a = [p / total_prob[type] + offset for p in probs_a]
    
    sc_a =plt.scatter(
        x_a, y_a,
        c=times_a,
        cmap=cmap,
        norm=norm,
        marker='x',
        label="A*" if i == 0 else ""
    )

    for x, y, d in zip(x_a, y_a, depths_a):
        plt.annotate(
        str(d),
        (x, y),
        xytext=(0, 6),          
        textcoords='offset points',
        fontsize=7,
        ha='center',
        va='bottom'
        )
    
    # ===== label for target type =====
    plt.text(
        8.2, offset + 0.5,
        f"{type.replace('_', '-').capitalize()} target",
        va='center',
        ha='left',
        fontsize=9
    )

# separating lines between layers
num_layers = len(astar)
for i in range(1, num_layers):
    plt.axhline(i * layer_height - (layer_height-1)/2, linestyle='--', alpha=0.3)

plt.xticks(range(1, 11))

yticks = []
yticklabels = []

for i in range(num_layers):
    base = i * layer_height
    yticks.extend([base, base + 0.5, base + 1.0])
    yticklabels.extend(["0", "0.5", "1"])

plt.yticks(yticks, yticklabels)

plt.xlim(0.5, 10.5)
plt.ylim(-0.05, num_layers * layer_height)
plt.xlabel("Path index")
plt.ylabel("Probability Ratio")

cbar = plt.colorbar(sc_a)
cbar.set_label("Time")

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

anchor_x = 0.01
anchor_y = 0.85

plt.legend(handles=[dijkstra_handle, astar_handle], frameon=False, 
           loc='upper left',bbox_to_anchor=(anchor_x, anchor_y))

plt.text(
    anchor_x+0.04, anchor_y-0.2,                  
    "Numbers = depth",
    transform=plt.gca().transAxes,
    fontsize=9,
    ha='left'
)

plt.grid(True, axis='x', linestyle='--', alpha=0.3)

plt.tight_layout() 

base_dir = os.path.dirname(os.path.abspath(__file__))

png_path = os.path.join(base_dir, "cyclic_path_prob_time.png")
pdf_path = os.path.join(base_dir, "cyclic_path_prob_time.pdf")

plt.savefig(png_path, dpi=300)
plt.savefig(pdf_path, bbox_inches='tight')

print("files saved to:")
print(png_path)
print(pdf_path)

plt.show()