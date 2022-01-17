# Barcode DNA counting system

## Prerequisite

- This system works only on Windows.

## Usage

### Start-up

- Create a shortcut of "barcodeDNA\barcodeDNA.vbs" on the desktop and launch it by double-clicking on it. 
  - Copying the VBS file will not work.

### Main window

![image](https://user-images.githubusercontent.com/2063184/149743104-852c42ef-0c6f-43e5-aabd-fb63f8f349fc.png)

1. Barcode DNA file
    - Click the "Select FASTA file" button on the right to select the file.
    - The extension of the fasta file should be ".fa".
    - If the database for Blast+ calculation does not exist, or if the fasta file is updated, the database will be created automatically when executing "Execute" (14).
2. Read1 FASTQ files
    - Use the "Select FASTQ files" button on the right to make your selection.
3. Select folder
    - When checked, you can select (2) as a whole folder.
    - If not checked, select by file unit (multiple files can be specified). In this case, drag-and-drop selection is also possible.
4. Clear
    - Clears the contents of (2) and (5).
5. Read2 FASTQ files
    - The Read2 file corresponding to "Read1 FASTQ files" (2) is automatically selected.
6. Remove bases
    - first: the number of bases at the beginning of the read that will be removed.
    - last: Number of bases at the end of the read that will be forcibly removed.
    - Minimum read length: The minimum length of the read to be used for the blast+ search after excluding the (consecutive) N sequences at both ends of the read.
7. Max mismatches
    - flank: Sum of the maximum allowed terminal mismatches from the blast+ search results.
    - mid: Sum of non-terminal mismatches allowed in blast+ search results.
    - E-value: e-value to be specified at blast+ execution time.
8. Collect mismatches
    - flank: Sum of the maximum allowed terminal mismatches to be aggregated in the end.
    - mid: Sum of the allowed non-terminal mismatches for final tally.
    - Strand: Select the final read to be aggregated. forward: read-1, reverse: read-2.
    - Collect: Execute the aggregation process and open the result file. The output is saved as a CSV file starting with the string specified in "Output prefix" (10) in "Output folder" (9).
9. Output folder
    - Specify the output directory.
10. Output prefix
    - Specify the output file name for "Collect mismatches" (8) and "Draw figures" (12)
    - Do not including double-byte characters, spaces, and special characters (alphanumeric characters and hyphens "-" are allowed).
11. Number of threads
    - Specify the number of Blast+ threads (-num_threads).
    - Depending on the size of the "Barcode DNA file" (1), it may be faster to set a smaller value.
12. Draw figures
    - Create an HTML file that summarizes the barcode DNA count diagram and open it in a browser.
13. Stop
    - Stop the process started by "Exceute" (14).
14. Execute
    - Count the barcode DNA, run "Collect mismatches" (8), and open the result file in Excel or other program.
15. Messages
    - Show messages.
16. Save
    - Save the message as a text file.
17. Clear
    - Clear the message

### File menu

![image](https://user-images.githubusercontent.com/2063184/149746000-bfe988ec-365c-4a60-a68f-4dbcad73d0c0.png)

1. Import configuration file
2. Export configuration file
3. Close

### Output format

![image](https://user-images.githubusercontent.com/2063184/149746116-9b4310d2-be5b-4f5d-9715-1a9edc0a9d28.png)

![image](https://user-images.githubusercontent.com/2063184/149746428-ffc78e87-eb86-4e02-a6d0-98ec235c0208.png)


- The date is appended to the file name.
-  If a file with the same name exists, a sequential number `_#` is added to the file name.




## Citation

Fumi Minoshima, Haruka Ozaki, Haruki Odaka, Hiroaki Tateno. "Integrated analysis of glycan and RNA in single cells", iScience 24,
102882 (2021)  
[https://doi.org/10.1016/j.isci.2021.102882](https://doi.org/10.1016/j.isci.2021.102882)

