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
   
        folder_name = os.path.basename(root)
        if folder_name == root_folder_name:
            continue
        if not files:
            print(f"Skipping empty folder: {root}")
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

# process_folder(folder_path='Output_10')

def process_data(folder_data, input_folder):
    # df = pd.read_csv(input_file)

    folder_data_df = pd.DataFrame(folder_data)

    all_columns = []
    for folder, data_list in folder_data.items():
        all_columns.extend(data_list)


    total_rows = len(folder_data_df)
    # print(total_rows)

    stats_df = pd.DataFrame(columns=folder_data_df.columns)

    # prefix_groups = defaultdict(list)

    for col in folder_data_df.columns:

        folder_data_df[col] = pd.to_numeric(folder_data_df[col], errors='coerce')
        
        non_zero_count = (folder_data_df[col][0:] != 0).sum()
        # print(non_zero_count)

        non_zero_ratio = round(non_zero_count / total_rows, 4)

        stats_df.at[0, col] = total_rows  
        stats_df.at[1, col] = non_zero_count  
        stats_df.at[2, col] = non_zero_ratio  

        # prefix = "_".join(col.split('_')[0])
        prefix = col.split('_')[0]
        # prefix_groups[prefix].append(col)

    result_df = pd.concat([stats_df, folder_data_df]).reset_index(drop=True)
    
    output_csv_path = os.path.join(input_folder, 'NrRuffle_counted.csv')
    result_df.to_csv(output_csv_path, index=False)
    print(f"Processed data saved to {output_csv_path}")

    return stats_df #prefix_groups, 


def plot_data(stats_df, output_folder):

    prefix_groups = defaultdict(list)

    for col in stats_df.columns:  
        prefix = col.split('_')[0]  
        prefix_groups[prefix].append(col)

    plt.figure(figsize=(12, 8))


    plt.rcParams.update({
    'font.size': 18,        
    'axes.labelsize': 22,    
    'axes.titlesize': 20,    
    'axes.titleweight': 'bold',
    'lines.linewidth': 3,    
    'lines.markersize': 12,  
    'errorbar.capsize': 5,  
    'legend.fontsize': 16,    
    })

    labels = []

    for prefix, columns in reversed(prefix_groups.items()):
        x_values = []  
        y_values = []  

        for col in columns:
            parts = col.split('_')
            # if len(parts) > 1:
            x_value = int(parts[1])  
            x_values.append(x_value)

            non_zero_ratio = stats_df.at[2, col]*100
            y_values.append(non_zero_ratio)

        # x_values, y_values = zip(*sorted(zip(x_values, y_values)))
        plt.plot(x_values, y_values, marker='o',label = f'm.o.i. {prefix}')

        labels.append(f'm.o.i. {prefix}')
    
    plt.xlabel('Infection time (min)')
    plt.ylabel('Infection efficiency')
    
    plt.grid(axis='y',linestyle='--', linewidth=2)
    plt.yticks(range(0, 101, 10), [f'{i}%' for i in range(0, 101, 10)])
    plt.legend(labels=labels, loc='center left', bbox_to_anchor=(1, 0.5), labelspacing=2.5,frameon=False)#title="Prefix Group"
    

    plt.subplots_adjust(right=0.9)  

    plt_output_path = os.path.join(output_folder, 'Fig_4_16.png')

    plt.savefig(plt_output_path,bbox_inches='tight')
    print(f"PNG plot saved to {plt_output_path}")
    # plt.show()
        # else:
        #     print(f"No valid data for prefix group: {prefix}")

# folder = 'Output_11'
# folder_data = process_folder(folder_path=folder)
# stats_df=process_data(folder_data, input_folder=folder)
# stats_df = pd.read_csv('Output_11/NrRuffle_counted.csv')
# plot_data(stats_df,folder)

if __name__ == "__main__":

    parser = argparse.ArgumentParser(description="Process a folder and generate graphs.")
    parser.add_argument("folder_path", help="Path to the main folder containing subfolders and CSV files.")
    
    args = parser.parse_args()

    folder = args.folder_path
    folder_data = process_folder(folder)
    stats_df=process_data(folder_data, folder)
    plot_data(stats_df,folder)
    
