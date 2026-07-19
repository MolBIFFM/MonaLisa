import pandas as pd
from collections import defaultdict
import matplotlib.pyplot as plt
import argparse
import os
import numpy as np
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

    SalRuffle = components[9]
    return SalRuffle

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
                    SalRuffle = process_csv_file(file_path, filename)
                    folder_data[folder_name].append(SalRuffle)
                    
                except Exception as e:
                    print(f"Error processing {filename}: {e}")
                    sys.exit(1)

        folder_data[folder_name].sort(reverse=True)

        print(f"{folder_name}: {len(folder_data[folder_name])} CSV files processed.")

    sorted_folder_data = dict(sorted(folder_data.items(), key=lambda item: sort_by_numbers(item[0])))

    # df = pd.DataFrame(sorted_folder_data)

    # output_csv_path = os.path.join(folder_path, 'SalRuffle_summary.csv') #_1ruffle
    # df.to_csv(output_csv_path, index=False)

    # print(f"Data saved to {output_csv_path}")

    return sorted_folder_data

def process_plot_data(folder_data, input_folder):
    # df = pd.read_csv(input_file)
    df = pd.DataFrame(folder_data)

    total_rows = len(df)
    # print(total_rows)

    stats_df = pd.DataFrame(columns=df.columns)

    prefix_groups = defaultdict(list)
    average_means = {}
    std_dev_means = {}

    for col in df.columns:

        df[col] = pd.to_numeric(df[col], errors='coerce')

        non_zero_count = (df[col][0:] != 0).sum()
        
        # print(non_zero_count)

        non_zero_sum = df[col][df[col] != 0].sum()

        if non_zero_count > 0:
            non_zero_mean = round(non_zero_sum / non_zero_count,2)
        else:
            non_zero_mean = 0  

        stats_df.at[0, col] = non_zero_count  
        stats_df.at[1, col] = non_zero_sum
        stats_df.at[2, col] = non_zero_mean
        
        
        prefix = "_".join(col.split('_')[0]) 
        prefix_groups[prefix].append(col)

    for prefix, cols in prefix_groups.items():
        if len(cols) > 1:
            
            # avg_mean = round(stats_df.loc[2, cols].mean(), 2)
            
            total_sum = stats_df.loc[1, cols].sum()  
            total_count = stats_df.loc[0, cols].sum()  
            
            if total_count > 0:
                avg_mean = round(total_sum / total_count, 2)
            else:
                avg_mean = 0  

            # all_non_zero_values = []
            # for col in cols:
            #     all_non_zero_values.extend(df[col][df[col] != 0].tolist())

            # print('all_non_zero_values: ',all_non_zero_values)
            
            # if len(all_non_zero_values) > 1:
            #     std_dev = round(pd.Series(all_non_zero_values).std(), 2)
            # else:
            #     std_dev = 0  
             
            std_dev = round(stats_df.loc[2, cols].std(), 2)

            for col in cols:
                stats_df.at[3, col] = avg_mean
                stats_df.at[4, col] = std_dev
            average_means[prefix] = avg_mean
            std_dev_means[prefix] = std_dev

    result_df = pd.concat([stats_df, df]).reset_index(drop=True)

    output_csv_path = os.path.join(input_folder, 'SalRuffle_counted.csv')
    result_df.to_csv(output_csv_path, index=False)
    print(f"Processed file saved as {output_csv_path}")

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

    prefixes = list(average_means.keys())
    prefixes_str = [item.replace('_', '') for item in prefixes]
    # print(prefixes)
    # print(prefixes_str)

    avg_values = list(average_means.values())
    std_dev_values = [std_dev_means.get(prefix, 0) for prefix in prefixes]
    
    # Bo
    origin_y = [0.88, 1.08, 1.64, 2, 3.03, 4.84, 8.4, 13.4]
    # Bo_inside
    # origin_y =[0.48,0.6,1.04,1.2,1.88,3,5.12,7.72]
    # Jenni
    # origin_y = [1.06, 1.2, 1.7, 1.9, 3.1, 4.8, 8.2, 12.9]
    std_dev_outside=np.array([0, 0, 0.13, 0.2, 0, 0.27, 0.67, 1])
    std_dev_inside=np.array([0, 0, 0.2, 0, 0.43, 0.47, 0.8, 0.93])
   
    variance_A = std_dev_outside ** 2
    variance_B = std_dev_inside ** 2

    total_variance = variance_A + variance_B

    origin_std_dev = np.sqrt(total_variance)
    # print('origin_std_dev: ',origin_std_dev)
    
    plt.errorbar(prefixes_str, origin_y, yerr=origin_std_dev, fmt='-o', color='red', label='Experimental', capsize=5)
    plt.errorbar(prefixes_str, avg_values, yerr=std_dev_values, fmt='-o', color='black', label='Simulation', capsize=5)
    

    plt.xlabel("m.o.i.")
    plt.ylabel("number Salmonella per ruffle")
    # plt.title("C")

    # plt.xticks(ticks=prefixes, labels=prefixes)

    # plt.title("Cell Ruffling vs. MOI")

    plt.grid(False)
    plt.legend()
    

    plt_output_path = os.path.join(input_folder, 'Figure_4_13_C.png')
    plt.savefig(plt_output_path, format='png')
    print(f"PNG plot saved to {plt_output_path}")

    # plt.show()


if __name__ == "__main__":

    parser = argparse.ArgumentParser(description="Process a folder and generate CSV and graphs.")
    parser.add_argument("folder_path", help="Path to the main folder containing subfolders and CSV files.")
    
    args = parser.parse_args()

    folder_data = process_folder(args.folder_path)
    
    process_plot_data(folder_data, args.folder_path)