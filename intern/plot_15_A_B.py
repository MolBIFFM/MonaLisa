import pandas as pd
from collections import defaultdict
import matplotlib.pyplot as plt
import argparse
import os
import numpy as np
import sys
import re
  
plt.rcParams.update({
    'font.size': 18,         
    'axes.labelsize': 24,    
    'axes.labelweight':'bold',
    'axes.titlesize': 24,    
    'axes.titleweight': 'bold',
    'lines.linewidth': 3,    
    'lines.markersize': 8,  
    'errorbar.capsize': 5,  
    'legend.fontsize': 16,    
    })

def process_csv_file(file_path, filename):
    try:
        df = pd.read_csv(file_path,  sep=r'\s+', encoding='utf-8')  
        #header=None, delimiter='\s+', 
    except Exception as e:
        print(f"Error reading {filename}: {e}")
        sys.exit(1)  

    if df.shape[0] == 0:
        print(f"File: {filename} is empty.")
        sys.exit(1)

    total_rows = len(df)
    # print(f'Total rows: {total_rows}')    
    if total_rows== 49:
        print(f'{filename} has 49 rows.')
    if total_rows != 50:
        return None,None

    
    #2.5h
    ini= 15
    ini_row = df.iloc[ini]
    
    ini_SalCyt = ini_row.iloc[11] 
    ini_SalVac = ini_row.iloc[12] 

    fold_change_SalCyt = {}
    fold_change_SalVac = {}

    for index, row in df.iloc[ini:].iterrows():
        key = round(row.iloc[1] / 3600, 3)  

         
        Salcyt = row.iloc[11]  
        if ini_SalCyt != 0 and Salcyt != 0:  
            fold_change_12th = round( Salcyt / ini_SalCyt, 3)
            fold_change_SalCyt[key] = fold_change_12th

        
        SalVac = row.iloc[12]  
        if ini_SalVac !=0 and SalVac != 0:
            fold_change_13th = round( SalVac / ini_SalVac, 3) 
            fold_change_SalVac[key] = fold_change_13th

    expected_len = total_rows - ini
    if len(fold_change_SalCyt) == expected_len or len(fold_change_SalVac) == expected_len:
        return fold_change_SalCyt if len(fold_change_SalCyt) == expected_len else None, \
               fold_change_SalVac if len(fold_change_SalVac) == expected_len else None
    return None, None

# print(process_csv_file(file_path='Output_7/50_10_1/1_71.csv',filename='50_10_1'))

def process_folder(folder_path):
    # folder_data = {}
    root_folder_name = os.path.basename(folder_path)

    all_fold_change_SalCyt = []
    all_fold_change_SalVac = []

    for root, dirs, files in os.walk(folder_path):
        folder_name = os.path.basename(root)
        if folder_name == root_folder_name:
            continue
    
        if not files:
            print(f"Skipping empty folder: {root}")
            continue
        
        # print(f"Checking folder: {root}")
        # print(f"Files found: {files}")

        # folder_data[folder_name] = []
    
        for filename in files:
            if filename.endswith('.csv') and 'summary' not in filename:
                # found_files = True
            
                file_path = os.path.join(root, filename)
                
                fold_change_SalCyt, fold_change_SalVac = process_csv_file(file_path, filename)
                
                if fold_change_SalCyt is not None:
                    all_fold_change_SalCyt.append(fold_change_SalCyt)
                
                if fold_change_SalVac is not None:
                    all_fold_change_SalVac.append(fold_change_SalVac)

    return all_fold_change_SalCyt, all_fold_change_SalVac

# all_fold_change_SalCyt, all_fold_change_SalVac = process_folder(folder_path='/home/bo/StochasticSimulation/Output_9')
# print('number of Cyt curve: ',len(all_fold_change_SalCyt),'; ',
#       'number of Vac curve: ',len(all_fold_change_SalVac))

def plot_data(all_fold_change_SalCyt, all_fold_change_SalVac, output_folder):
   
    max_key = max(all_fold_change_SalCyt[0].keys())

    if all_fold_change_SalCyt:
        plt.figure(figsize=(10, 8))
        for fold_change_data in all_fold_change_SalCyt:
            x_vals = list(fold_change_data.keys())
            y_vals = list(fold_change_data.values())
            plt.plot(x_vals, y_vals, color='gray', linewidth = 1)#marker='o', label=f'SalCyt fold change'
        
       
        all_keys = sorted(list(all_fold_change_SalCyt[0].keys()))  
        values_matrix = np.array([[fold_change[key] for fold_change in all_fold_change_SalCyt] for key in all_keys])

        avg_values = np.mean(values_matrix, axis=1)

        plt.plot(all_keys, avg_values, color='black',)  
        
        plt.xlim(1.5, max_key)
        plt.xticks([2, 4, 6, 8])
        plt.ylim(-5,105)
        plt.xlabel('Time p.i.[h]')
        plt.ylabel('Fold change of cytosolic Salmonella')
        # plt.title('Fold Change of SalCyt Over Time')
        # plt.grid(True)
        # plt.legend()
        fig_name = f'Fig_4_15_A_{len(all_fold_change_SalCyt)}.png'
        plt.savefig(os.path.join(output_folder, fig_name))
        print(f"SalCyt fold change plot saved to {output_folder}/{fig_name}")
        plt.close()

    if all_fold_change_SalVac:
        plt.figure(figsize=(10, 8))
        for fold_change_data in all_fold_change_SalVac:
            x_vals = list(fold_change_data.keys())
            y_vals = list(fold_change_data.values())
            plt.plot(x_vals, y_vals, color='gray', linewidth = 1)#marker='o', label=f'SalVac fold change'
        

        all_keys = sorted(list(all_fold_change_SalVac[0].keys()))  
        values_matrix = np.array([[fold_change[key] for fold_change in all_fold_change_SalVac] for key in all_keys])

        avg_values = np.mean(values_matrix, axis=1)

        plt.plot(all_keys, avg_values, color='black',)  
        plt.xlim(1.5, max_key)
        plt.xticks([2, 4, 6, 8])
        plt.ylim(0,6.5)
        plt.xlabel('Time p.i.[h]')
        plt.ylabel('Fold Change of vacuolar Salmonella')
        # plt.title('Fold Change of SalVac Over Time')
        # plt.grid(True)
        # plt.legend()
        fig_name = f'Fig_4_15_B_{len(all_fold_change_SalVac)}.png'
        plt.savefig(os.path.join(output_folder, fig_name))
        print(f"SalCyt fold change plot saved to {output_folder}/{fig_name}")
        plt.close()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Process a folder and generate graphs.")
    parser.add_argument("folder_path", help="Path to the main folder containing subfolders and CSV files.")
    
    args = parser.parse_args()

    all_fold_change_SalCyt, all_fold_change_SalVac = process_folder(args.folder_path)
    
    plot_data(all_fold_change_SalCyt, all_fold_change_SalVac, args.folder_path)