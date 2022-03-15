# MonaLisa

# Download and install
To install MonaLisa simply extract the archive into an arbitrary folder. The archive contains a folder lib with the necessary libraries. MonaLisa requires Java version 7 or higher.

# Start MonaLisa
To start MonaLisa double click on the file MonaLisa.jar. If MonaLisa does not start, type the following console command:
  java -jar MonaLisa.jar

# To work with extended Petri nets, to compute distance matrices, or T-clusters start MonaLisa with sufficient memory (Java virtual machine):
  cd /PATH/TO/MONALISA/
  java -Xmx{MemorySizeInGigabyte}G -jar MonaLisa.jar
or
  java -Xmx{MemorySizeInMegabyte}M -jar MonaLisa.jar

MonaLisa creates an error log (MonaLisaErrorLog.txt) in your home directory.

For a full set of instructions regarding the use of MonaLisa, please visit
https://sourceforge.net/p/monalisa4pn/wiki/Home/
