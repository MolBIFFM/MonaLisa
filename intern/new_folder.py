import os
import itertools
import argparse


parser = argparse.ArgumentParser(description="Generate folders based on provided moi, it, and batch.")


parser.add_argument('--moi', nargs='+', required=True, help='List of MOI values, separated by space.')
parser.add_argument('--it', nargs='+', required=True, help='List of IT values, separated by space.')
parser.add_argument('--batch', nargs='+', required=True, help='List of Batch of Experiments, separated by space.')


args = parser.parse_args()


# moi = ["1", "2", "4","8","16","32","64","128",'256','512','1024']
# # moi = ['','','','','','','','','','']
# it = ["20"]
# NrOfTimes = ["1", "2", "3","4"]


combinations = itertools.product(args.moi, args.it, args.batch)


root_directory = "./generated_folders"


os.makedirs(root_directory, exist_ok=True)


for combination in combinations:
    folder_name = "_".join(combination)  
    folder_path = os.path.join(root_directory, folder_name)
    os.makedirs(folder_path, exist_ok=True)
    print(f"Created folder: {folder_path}")
