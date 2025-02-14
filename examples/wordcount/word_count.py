#!/usr/bin/python

import os
import sys

class WordCount:
    def __init__(self, folder_path):
        self.words_count = {}
        files = os.listdir(folder_path)
        for file in files:
            file = open(os.path.join(folder_path,file), 'r')
            lines = file.readlines()

            for line in lines:
                words = line.split()
                for word in words:
                    word = word.strip()
                    if word in self.words_count:
                        self.words_count[word]+=1
                    else:
                        self.words_count[word]=1
            file.close()

    
    def print_dictionary(self):
        for item in self.words_count.items():
            print(item[0], " ", item[1])
            

def main():
    n = len(sys.argv)
    if n == 1:
        print("Usage: ", sys.argv[0], " <input folder>")
        exit(1)
    else:
        folder = sys.argv[1]
        preprocessor = WordCount(folder)
        preprocessor.print_dictionary()
    
  
# Using the special variable 
# __name__
if __name__=="__main__":
    main()
