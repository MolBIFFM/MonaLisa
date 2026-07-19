import pandas as pd
from collections import defaultdict
import matplotlib.pyplot as plt
import numpy as np
import argparse
import os
from sklearn.linear_model import LinearRegression

def process_csv(input_cell_ruffling, input_sal_per_ruffle, output_file):
    cell_ruffling = pd.read_csv(input_cell_ruffling)
    sal_per_ruffle = pd.read_csv(input_sal_per_ruffle)

    cell_ruffling_ratio = {}
    cell_ruffing_std_dev = {}
    sal_per_ruffle_number = {}
    sal_per_ruffle_std_dev = {}
    
    for col in cell_ruffling.columns:
        key = col.split('_')[0] 
        percentage = cell_ruffling.iloc[3][col] 
        cell_ruffling_ratio[key] = percentage /100
        std_dev = cell_ruffling.iloc[4][col]
        cell_ruffing_std_dev[key] = std_dev / 100

    for col in sal_per_ruffle.columns:
        key = col.split('_')[0]  
        value = sal_per_ruffle.iloc[3][col]  
        sal_per_ruffle_number[key] = value
        std_dev = sal_per_ruffle.iloc[4][col]
        sal_per_ruffle_std_dev[key] = std_dev

    keys = list(cell_ruffling_ratio.keys())
    
    result_df = pd.DataFrame(columns=keys)
    
    result_df.loc[0] = [cell_ruffling_ratio[key] for key in keys]

    result_df.loc[1] = [cell_ruffing_std_dev[key] for key in keys]
    
    result_df.loc[2] = [sal_per_ruffle_number[key] for key in keys]

    result_df.loc[3] = [sal_per_ruffle_std_dev[key] for key in keys]    
    
    result_df.loc[4] = result_df.loc[0] * result_df.loc[2]

    propagated_std_dev = []
    for key in keys:
        a = cell_ruffling_ratio[key]
        b = sal_per_ruffle_number[key]
        sigma_a = cell_ruffing_std_dev[key]
        sigma_b = sal_per_ruffle_std_dev[key]
        
        sigma_c = np.sqrt((b * sigma_a) ** 2 + (a * sigma_b) ** 2)
        propagated_std_dev.append(sigma_c)
    
    result_df.loc[5] = propagated_std_dev
    
    result_df.to_csv(output_file, index=False)
    print(f"Processed file saved as {output_file}")

    numeric_keys = [float(key) for key in keys]
    # print(numeric_keys)
    y_values = result_df.loc[4].values
    yerr = result_df.loc[5]
    
    nb_point_train = 5

    if len(numeric_keys) >= 2:
        coeffs, cov_matrix = np.polyfit(numeric_keys[:nb_point_train], y_values[:nb_point_train], 1, cov=True)  # 线性拟合（多项式次数1）
        poly = np.poly1d(coeffs)
    
        x_extrapolate = np.array(numeric_keys)
        y_extrapolate = poly(x_extrapolate)
        
        p_std_err = np.sqrt(np.diag(cov_matrix))

        x_mean = np.mean(numeric_keys)
        n = len(numeric_keys)
        yerr_extrapolate = np.sqrt(p_std_err[0]**2 * (x_extrapolate - x_mean)**2 + p_std_err[1]**2 + (p_std_err[0]**2 / n))

        # print('predicted_y_values: ',predicted_y_values)
    else:
        print("data is not sufficient, cannot perform linear fitting.")
        return
    
    #Misselwitz
    # y_origin = [0,0,0.02,0.1,0.6,1.5,3.8,6.8]
    # yerr_origin = [0,0,0,0,0.4,0.5,1.2,1.8]

    origin_number= np.array([0.88, 1.08, 1.64, 2, 3.03, 4.84, 8.4, 13.4])
    origin_percentage = np.array([2, 5, 8, 16, 31, 52, 75, 88])/100
    y_origin = origin_number* (origin_percentage )
    # print('y_origin: ',y_origin)
    # propagation of uncertainty

    std_dev_outside=np.array([0, 0, 0.13, 0.2, 0, 0.27, 0.67, 1])
    std_dev_inside=np.array([0, 0, 0.2, 0, 0.43, 0.47, 0.8, 0.93])
  
    variance_A = std_dev_outside ** 2
    variance_B = std_dev_inside ** 2

    total_variance = variance_A + variance_B

    sigma_number = np.sqrt(total_variance)
    sigma_percentage = [0, 0, 0, 0.05, 0.01, 0.04, 0.06, 0.02] #[0, 0, 0, 5, 10, 4, 6, 2]
    yerr_origin = np.sqrt((origin_percentage * sigma_number) ** 2 + (origin_number * sigma_percentage) ** 2)


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

    # Misselwitz
    plt.errorbar(keys, y_origin, yerr = yerr_origin,fmt='-o',color = 'red',label='Experimental', capsize= 5 )

    # Simulation
    plt.errorbar(keys, result_df.loc[4], yerr = yerr, fmt='-o', color='black', label='Simulation', capsize=5)
    
    # Extrapolation
    plt.errorbar(keys, y_extrapolate,yerr = yerr_extrapolate, fmt = '--o', color='gray', label='Simulation, extrapolated from low MOI')  # 预测数据
    
    

    plt.xlabel('m.o.i.')
    plt.ylabel('Number of invaded Salmonella per cell')
    # plt.title("D")
    # plt.title('Line Plot with Error Bars')
    plt.legend()
    plt.grid(False)
    
    plt_output_path = os.path.join(os.path.dirname(output_file), f'Fig_4_13_D_{nb_point_train}.png')
    plt.savefig(plt_output_path,format='png')
    print(f"PNG plot saved to {plt_output_path}")
    # plt.show()


def main():

    parser = argparse.ArgumentParser(description='Process CSV files to calculate data with propagated standard deviations.')
    parser.add_argument('folder', type=str, help='The folder containing the input CSV files.')

    args = parser.parse_args()
    folder = args.folder

    input_cell_ruffling = os.path.join(folder, 'NrRuffle_counted.csv')
    input_sal_per_ruffle = os.path.join(folder, 'SalRuffle_counted.csv')
    output_file = os.path.join(folder, 'Sal_per_cell.csv')

    process_csv(input_cell_ruffling, input_sal_per_ruffle, output_file)

if __name__ == "__main__":
    main()