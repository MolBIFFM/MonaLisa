from lxml import etree
import argparse
import itertools
import os
import re

# MOI,Cremove,Cadd
MOI = [[500,750.0,250.0],#
       [250,875.0,125.0],#
       [125,937.5,62.5],
       [75,962.5,37.5],
       [63,968.5,31.5],
       [50,975.0,25.0],
       [31,984.5,15.5],
       [16,992.0,8.0],
       [8,996.0,4.0],
       [4,998.0,2.0],
       [1024,488,512],
       [512,744,256],
       [256,872,128],
       [128,936,64],
       [64,968,32],
       [32,984,16],
       [2,999,1],
       [1,999.5,0.5]
]

#infection time, Cwash_count
IT = [[5,3.33],
      [9,1.85],
      [10,1.67],
      [15,1.11],
      [20,0.83],
      [25,0.66],
      [30,0.56]
]

def validate_moi(moi_list):
    for i, sublist in enumerate(moi_list):
        # 计算子列表的第二个和第三个数字的和
        sum_values = sublist[1] + sublist[2]
        
        # 检查是否等于1000
        if sum_values != 1000.0:
            # 报错信息
            error_message = f"Error at row {i + 1}: first element is {sublist[0]}"
            raise ValueError(error_message)

def modify_xml(filename, moi, it):
    # 读取文件内容
    with open(filename, 'r', encoding='utf-8') as file:
        lines = file.readlines()

    # 找到 MOI 和 IT 对应的值
    moi_values = next((item for item in MOI if item[0] == moi), None)
    it_values = next((item for item in IT if item[0] == it), None)

    if not moi_values or not it_values:
        print("Invalid moi or it value")
        # raise ValueError("Invalid moi or it value")
        return
    
    # 修改第39行和第40行
    # line_index_39 = 38  # 第39行在索引中是38
    # line_index_40 = 39  # 第40行在索引中是39
    # if 'name="remove"' in lines[line_index_39] and 'name="add"' in lines[line_index_40]:
    #     lines[line_index_39] = lines[line_index_39].replace('firingRate="750.0"', f'firingRate="{moi_values[1]}"')
    #     lines[line_index_40] = lines[line_index_40].replace('firingRate="250.0"', f'firingRate="{moi_values[2]}"')
    # else:
    #     print("Data in File gets wrong for MOI")

    # 修改第69行
    
    # line_index_69 = 68  # 第69行在索引中是68
    # if 'name="wash_count"' in lines[line_index_69]:
    #     lines[line_index_69] = lines[line_index_69].replace('firingRate="3.33"', f'firingRate="{it_values[1]}"')
    # else:
    #     print("Data in File gets wrong for IT")

    for i, line in enumerate(lines):
        if 'name="remove"' in line:
            lines[i] = re.sub(r'firingRate="[\d\.]+"', f'firingRate="{moi_values[1]}"', line)
        if 'name="add"' in line:
            lines[i] = re.sub(r'firingRate="[\d\.]+"', f'firingRate="{moi_values[2]}"', line)
        if 'name="wash_count"' in line:
            lines[i] = re.sub(r'firingRate="[\d\.]+"', f'firingRate="{it_values[1]}"', line)

    # 获取输入文件的目录
    input_directory = os.path.dirname(filename)
    # 新文件名
    # new_filename = filename.replace('MOI', str(moi)).replace('IT', str(it))
    new_filename = os.path.join(input_directory,f"MOI_{moi}_IT_{it}.xml")

    # 写入新的文件
    with open(new_filename, 'w', encoding='utf-8') as file:
        file.writelines(lines)

# 调用函数并传入文件名、moi 和 it
# modify_xml('FR_MOI_IT.xml', 500, 9)

# try:
#     validate_moi(MOI)
    # print("All rows are valid.")
    # modify_xml('FR_MOI_IT.xml')
# except ValueError as e:
#     print(e)
# 调用函数并传入文件名

def main():
    parser = argparse.ArgumentParser(description="Modify an XML file based on MOI and IT")
    parser.add_argument('filename', type=str, help="The XML file to modify")
    # parser.add_argument('moi', type=int, help="The MOI value to use")
    # parser.add_argument('it', type=int, help="The IT value to use")
    parser.add_argument('--moi', nargs='+', required=True, help='List of MOI values, separated by space.')
    parser.add_argument('--it', nargs='+', required=True, help='List of IT values, separated by space.')

    args = parser.parse_args()

    # 转换 MOI 和 IT 为整数
    moi_values = [int(m) for m in args.moi]
    it_values = [int(i) for i in args.it]
    combinations = itertools.product(moi_values, it_values)
    
    # validate_moi(MOI)

     # 遍历每个组合并修改 XML 文件
    for moi, it in combinations:
        print(f"Processing MOI={moi}, IT={it}")
        modify_xml(args.filename, moi, it)

if __name__ == "__main__":
    main()

