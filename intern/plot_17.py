import pandas as pd
import numpy as np
from collections import defaultdict
import matplotlib.pyplot as plt
import argparse
import os
import sys
import re
import seaborn as sns
import math

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

    # total_rows = len(df)
    # # print(f'Total rows: {total_rows}')
    # if total_rows != 98:
    #     return None,None

    #---------------check the death --------------------
    # for index, row in df.iterrows():
    #     if row.iloc[25] != 0:
    #         print(f"Found Non-zero in row {index + 1}, column 26.")  
    #         return
    #     elif row.iloc[26] != 0:
    #         print(f"Found Non-zero in row {index + 1}, column 27.")  
    #         return
        
    # for index, value in df['DeathSigCyt'].items():
    #     if value != 0:  
    #         print(f"Found non-zero value in row {index+1} of 'DeathSigCyt' column in {filename}.")
    #         return  
        
    # for index, value in df['DeathSigVac'].items():
    #     if value != 0:  
    #         print(f"Found non-zero value in row {index+1} of 'DeathSigVac' column in {filename}.")
    #         return  
    # -----------------------------------------------------
    if df.shape[0] < 14:  
        # print(f"File {file_path} does not have enough rows. Terminating.")
        return None, None
    
    if df.iloc[13, 11] == 0 and df.iloc[13, 12] == 0:  
        # print(f"Both values in row 14 are zero in {file_path}. Terminating.")
        return None, None
    


    # SalCyt_map = {2: None, 8: None, 16: None}
    SalCyt_SalVac_map = {2: None, 8: None, 16: None}
    SalVac_map = {2: None, 8: None, 16: None}

    # 2h, 8h, 16h, p.i.
    # row_indices = [13, 49, 97]  # 0-based index
    # keys = [2, 8, 16]

    row_indices = [13, 49, 97, 145, 193, 241, 289, 337]  
    keys =        [2,  8,  16,  24,  32,  40,  48, 56]

    for key, row_index in zip(keys, row_indices):

        if row_index >= df.shape[0]:
            SalCyt_SalVac_map[key] = None
            SalVac_map[key] = None
        else:

            SalCyt = df.iloc[row_index, 11]
            SalVac = df.iloc[row_index, 12]
            # no need to consider death, value of 0 would not be displayed
            # DeathSigCyt = df.iloc[row_index, 25]
            # DeathSigVac = df.iloc[row_index, 26]
            if SalCyt == 0:
                SalVac_map[key] = SalVac  
                # elif DeathSigVac == 1:
                #     SalVac_map[key] = None
                # elif DeathSigCyt ==  1:
                #     SalCyt_SalVac_map[key] == None
            else:
                # if DeathSigVac == 0 and DeathSigCyt == 0:
                SalCyt_SalVac_map[key] = SalCyt + SalVac
                if key == 8:
                    print(f'In {key}h {filename} is there {SalCyt + SalVac}, and SalCyt is {SalCyt}, SalVac is {SalVac}')
                # if key == 56:
                #     print(f'In {key}h {filename} is there {SalCyt + SalVac}, and SalCyt is {SalCyt}, SalVac is {SalVac}')
                # elif DeathSigVac == 1:
                #     SalVac_map[key] == None
                

    return SalCyt_SalVac_map, SalVac_map
        
def sort_by_numbers(name):

    return tuple(map(int, re.findall(r'\d+', name)))


def process_folder(folder_path):

    folder_data = {}
    root_folder_name = os.path.basename(folder_path)
    
    all_salcyt_salvac_map = defaultdict(list)  
    all_salvac_map = defaultdict(list)  


    for root, dirs, files in os.walk(folder_path):

        folder_name = os.path.basename(root)
        if folder_name == root_folder_name:
            continue
        

        if not files:
            print(f"Skipping empty folder: {root}")
            continue

        folder_data[folder_name] = []

        valid_count = 0  
        salvac_2 = 0
        salvac_8 = 0
        salvac_16= 0
        sum_2 = 0
        sum_8 = 0
        sum_16 = 0


        for filename in files:
            if filename.endswith('.csv') and 'summary' not in filename:
                # found_files = True
            
                file_path = os.path.join(root, filename)
                
                try:
                    SalCyt_SalVac_map, SalVac_map = process_csv_file(file_path, filename)
                    # print(f'{filename} => SalCyt_SalVac_map: {SalCyt_SalVac_map}; SalVac: {SalVac_map}')
                    
                    #----------------check the amount----------------
                    if SalCyt_SalVac_map is not None and SalVac_map is not None:
                        for key in SalVac_map:
                            if SalVac_map[key] is not None and SalVac_map[key] != 0:
                                all_salvac_map[key].append(SalVac_map[key])
                        
                        for key in SalCyt_SalVac_map:
                            if SalCyt_SalVac_map[key] is not None and SalCyt_SalVac_map[key] != 0:
                                all_salcyt_salvac_map[key].append(SalCyt_SalVac_map[key])
                        # if 2 in SalVac_map:
                        #     print('2222')
                        # else:
                        #     print("Key 2 not found in SalVac_map")
                        # valid_count += 1  

                        # if SalVac_map.get(2) is not None and SalVac_map.get(2) != 0:
                        #     # print(f'2222 in {filename}')
                        #     all_salvac_map[2].append(SalVac_map.get(2))
                            # salvac_2 += 1
                        # else:
                            # print(f"Key 2 not found in SalVac_map in {filename}")
                            # print(1)
                            
                    #     if SalVac_map.get(8) != None and SalVac_map.get(8) != 0:
                    #         salvac_8 += 1
                    #     if SalVac_map.get(16) != None and SalVac_map.get(16) != 0:
                    #         salvac_16 += 1
                        # if SalCyt_SalVac_map.get(2) != None and SalCyt_SalVac_map.get(2) != 0:
                            # sum_2 += 1
                    #     if SalCyt_SalVac_map.get(8) != None and SalCyt_SalVac_map.get(8) != 0:
                    #         sum_8 += 1
                    #     if SalCyt_SalVac_map.get(16) != None and SalCyt_SalVac_map.get(16) != 0:
                    #         sum_16 += 1
                    # -----------------------------------------------
                
                except KeyError as ke:
                    print(f"KeyError processing {filename}: {ke}")
                except Exception as e:
                    print(f"Error processing {filename}: {e}")
                    sys.exit(1)

        # print(folder_name)
        # print(f"Total valid results: {valid_count}")
        # print(f"nonzero nonnone Salvac_2: {salvac_2}")
        # print(f"nonzero nonnone salvac_8: {salvac_8}")
        # print(f"nonzero nonnone salvac_16: {salvac_16}")
        # print(f"nonzero nonnone sum_2: {sum_2}")
        # print(f"nonzero nonnone sum_8: {sum_8}")
        # print(f"nonzero nonnone sum_16: {sum_16}")

    # print(all_salvac_map.get(2))

    # for key in all_salcyt_salvac_map:
    #     print(f"Key {key} in all_salcyt_salvac_map has {len(all_salcyt_salvac_map[key])} values.")

    # for key in all_salvac_map:
    #     print(f"Key {key} in all_salvac_map has {len(all_salvac_map[key])} values.")

   
    return all_salcyt_salvac_map, all_salvac_map


def plot_data(all_salcyt_salvac_map, all_salvac_map,output_folder):

    combined_data = []

    for key, values in all_salcyt_salvac_map.items():
        for value in values:
            combined_data.append({'Key': key, 'Value': value, 'Source': 'SalCyt_SalVac'})

    for key, values in all_salvac_map.items():
        for value in values:
            combined_data.append({'Key': key, 'Value': value, 'Source': 'SalVac'})

    df = pd.DataFrame(combined_data)

    # -------------------------------
    grouped = df.groupby('Key')

    for key, group in grouped:

        greater_than_100_indices = group[group['Value'] > 100].sort_values(by='Value', ascending=False).index.tolist()
        
        current_value = 120  
        count = 0  

        for idx in greater_than_100_indices:
            df.at[idx, 'Value'] = current_value
            count += 1
            
            if count % 3 == 0:
                current_value -= 2
    #----------------------------------------

    plt.figure(figsize=(10, 8))
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

    sns.swarmplot(data=df, x='Key', y='Value', hue='Source', dodge = False, palette={"SalCyt_SalVac": "green", "SalVac": "maroon"})

    # sns.stripplot(data=df, x='Key', y='Value', hue='Source',jitter=True, alpha=0.6, palette={"SalCyt_SalVac": "green", "SalVac": "maroon"})
    #dodge=True, 

    plt.axhline(100, color='gray', linestyle='--', linewidth=1)  
    # plt.text(0.5, 105, 'Above 100', ha='center', color='gray', fontsize=12)  
    # plt.text(0.5, 50, '0 to 100', ha='center', color='gray', fontsize=12) 


    plt.ylim(0, 130)  

    plt.yticks([0,20,40,60,80,100,120], ['0', '20','40','60','80','100','>100'])

    # y_ticks = plt.yticks()[0]

    # y_labels = ['>100' if tick == 120 else tick for tick in y_ticks]

    # plt.yticks(y_ticks, y_labels)

    # plt.title('Combined SalCyt_SalVac_map and SalVac_map')
    plt.xlabel('Time p.i. [h]')
    plt.ylabel('Number of bacteria/cell')
    # plt.legend(title='Source')  
    plt.legend().set_visible(False)
    # plt.tight_layout()  
    # plt.show()
    fig_name = f'Fig_4_17.png'
    plt.savefig(os.path.join(output_folder, fig_name))
    print(f"Plot saved to {output_folder}/{fig_name}")
    plt.close()

# folder = 'Output_12'
# all_salcyt_salvac_map, all_salvac_map = process_folder(folder)
# plot_combined_data(all_salcyt_salvac_map, all_salvac_map,folder)

if __name__ == "__main__":

    parser = argparse.ArgumentParser(description="Process a folder and generate graphs.")
    parser.add_argument("folder_path", help="Path to the main folder containing subfolders and CSV files.")
    
    args = parser.parse_args()

    all_salcyt_salvac_map, all_salvac_map  = process_folder(args.folder_path)
    
    plot_data(all_salcyt_salvac_map, all_salvac_map , args.folder_path)