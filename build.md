```
cd wextras
mkdir classes
javac -target 1.1 -source 1.3 -classpath ../superwaba/vmsrc/classes -d classes source/util/Maths.java
# this appears not to be needed but just adding it addes a wextra.ui 
# package which is included by one of the BlockModel classes
javac -target 1.1 -source 1.3 -classpath ../superwaba/vmsrc/classes -d classes source/ui/PreferredSize.java
javac -target 1.1 -source 1.3 -d classes source/wababin/*.java

cd ../wgraph/
make

cd ../ProbeSystem/
make

cd ../BlockModel
make
```
