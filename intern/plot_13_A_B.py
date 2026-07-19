import pandas as pd
import numpy as np
from collections import defaultdict
import matplotlib.pyplot as plt
import argparse
import os
import sys
import re

def process_csv_file(file_path, filename):
    try:
        df = pd.read_csv(file_path, encoding='utf-8')  
        #header=None, delimiter='\s+', 
    except Exception as e:
        print(f"Error reading {filename}: {e}")
        sys.exit(1)  

    
    if df.shape[0] == 0:
        print(f"File: {filename} is empty.")
        sys.exit(1)

    
    last_row = df.iloc[-1, 0]  
    components = last_row.split()  

    NrRuffle = components[7]
    return NrRuffle

def sort_by_numbers(name):
    return tuple(map(int, re.findall(r'\d+', name)))

def process_folder(folder_path):
    folder_data = {}
    root_folder_name = os.path.basename(folder_path)

    for root, dirs, files in os.walk(folder_path):
        
        if not files:
            print(f"Skipping empty folder: {root}")
            continue
        
        folder_name = os.path.basename(root)
        if folder_name == root_folder_name:
            continue

        folder_data[folder_name] = []
    
        for filename in files:
            if filename.endswith('.csv') and 'summary' not in filename:
                # found_files = True
            
                file_path = os.path.join(root, filename)
                
                try:
                    NrRuffle = process_csv_file(file_path, filename)
                    
                    folder_data[folder_name].append(NrRuffle)
                    
                except Exception as e:
                    print(f"Error processing {filename}: {e}")
                    sys.exit(1)

        folder_data[folder_name].sort(reverse=True)

        print(f"{folder_name}: {len(folder_data[folder_name])} CSV files processed.")


    sorted_folder_data = dict(sorted(folder_data.items(), key=lambda item: sort_by_numbers(item[0])))

    # df = pd.DataFrame(sorted_folder_data)

    # output_csv_path = os.path.join(folder_path, 'NrRuffle_summary.csv')
    # df.to_csv(output_csv_path, index=False)

    # print(f"Data saved to {output_csv_path}")

    return sorted_folder_data


def process_plot_data(folder_data, input_folder):
    # df = pd.read_csv(input_file)
    folder_data_df = pd.DataFrame(folder_data)

    all_columns = []
    for folder, data_list in folder_data.items():
        all_columns.extend(data_list)

    total_rows = len(folder_data_df)
    # print(total_rows)

    stats_df = pd.DataFrame(columns=folder_data_df.columns)

    prefix_groups = defaultdict(list)
    average_ratios = {}
    std_dev_ratios = {}

    for col in folder_data_df.columns:
        folder_data_df[col] = pd.to_numeric(folder_data_df[col], errors='coerce')
        
        non_zero_count = (folder_data_df[col][0:] != 0).sum()
        # print(non_zero_count)

        non_zero_ratio = round(non_zero_count / total_rows, 4)

        stats_df.at[0, col] = total_rows  
        stats_df.at[1, col] = non_zero_count  
        stats_df.at[2, col] = non_zero_ratio  

        prefix = "_".join(col.split('_')[0])
        prefix_groups[prefix].append(col)

    
    for prefix, cols in prefix_groups.items():
        if len(cols) > 1:
            
            avg_ratio = round(stats_df.loc[2, cols].mean()*100, 4)
            std_dev_ratio = round(stats_df.loc[2, cols].std() * 100, 4)
            
            for col in cols:
                stats_df.at[3, col] = avg_ratio
                stats_df.at[4, col] = std_dev_ratio
            average_ratios[prefix] = avg_ratio
            std_dev_ratios[prefix] = std_dev_ratio

    
    result_df = pd.concat([stats_df, folder_data_df]).reset_index(drop=True)

   
    output_csv_path = os.path.join(input_folder, 'NrRuffle_counted.csv')
    result_df.to_csv(output_csv_path, index=False)
    print(f"Processed data saved to {output_csv_path}")

    
    plt.figure(figsize=(10, 8))

    
    plt.rcParams.update({
    'font.size': 18,         
    'axes.labelsize': 22,    
    'axes.titlesize': 20,    
    'axes.titleweight': 'bold',
    'lines.linewidth': 3,    
    'lines.markersize': 8,  
    'errorbar.capsize': 5,  
    'legend.fontsize': 16,    
    })

    
    prefixes = list(average_ratios.keys())
    prefixes_str = [item.replace('_', '') for item in prefixes]
    # print(prefixes)
    # print(prefixes_str)

    avg_values = list(average_ratios.values())
    std_dev_values = [std_dev_ratios.get(prefix, 0) for prefix in prefixes]

    if len(prefixes_str) == 8:
        origin_y = [2, 5, 8, 16, 31, 52, 75, 88]
        origin_std_dev = np.array([0, 0, 0, 5, 10, 4, 6, 2])/2
        # plt.title("A")
    elif len(prefixes_str) == 11:
        origin_y =[3,4,10,18,32,52,77,84,93,94,91]
        origin_std_dev =np.array([0, 0, 8, 14, 18, 22, 22, 12, 4, 6, 12])/2
        # plt.title("B")
    
    # line_E, =
    plt.errorbar(prefixes_str,origin_y,yerr = origin_std_dev, fmt='-o', color='red', label='Experimental', capsize=5)
    # print('origin_std_dev: ', origin_std_dev)
    # line_S, =
    plt.errorbar(prefixes_str, avg_values, yerr=std_dev_values, fmt='-o', color='black', label='Simulation', capsize=5)
   
    plt.xlabel("m.o.i.")
    plt.ylabel("% cells ruffling")
    
    plt.grid(False)

    plt.legend()
    # plt.legend([line_S, line_E], ['Simulation', 'Experimental'])

    if len(prefixes_str) == 8:
        plt_output_path = os.path.join(input_folder, 'Figure_4_13_A.png')
    elif len(prefixes_str) == 11:
        plt_output_path = os.path.join(input_folder, 'Figure_4_13_B.png')
    plt.savefig(plt_output_path, format='png')
    print(f"PNG plot saved to {plt_output_path}")

    # plt.show()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Process a folder and generate CSV and graphs.")
    parser.add_argument("folder_path", help="Path to the main folder containing subfolders and CSV files.")
    
    args = parser.parse_args()

    folder_data = process_folder(args.folder_path)
    
    process_plot_data(folder_data, args.folder_path)
