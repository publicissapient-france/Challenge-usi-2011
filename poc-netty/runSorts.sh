
RUNS="1 10 100 1000 10000 100000 1000000 10000000 100000000 1000000000"
#RUNS="1 10 100 "

ALGOS="heapSort quickSort selectionSort bubbleSort parallelMerge java merge parallelMergeComp javaComp mergeComp"
#ALGOS="parallelMergeComp javaComp mergeComp"
ALGOS="heapSort quickSort selectionSort bubbleSort"

CLASSPATH=".:/Users/ealliaume/.m2/repository/org/coconut/forkjoin/jsr166y/281207/jsr166y-281207.jar"

cd target/classes

for ALGO in $ALGOS; do
  echo $ALGO  
  
  P=0
  for RUN in $RUNS; do
    R=`java -Xmx3500m -cp $CLASSPATH  SortMain $ALGO $RUN`
    echo " - 10°$P - $R "
    P=`expr $P + 1`
  done
  
  echo
done

cd - &2>/dev/null







