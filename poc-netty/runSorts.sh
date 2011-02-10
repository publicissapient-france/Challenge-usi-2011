
RUNS="1 10 100 1000 10000 100000 1000000 10000000 100000000"
ALGOS="java merge parallelMerge"


CLASSPATH=".:/Users/ealliaume/.m2/repository/org/coconut/forkjoin/jsr166y/281207/jsr166y-281207.jar"

cd target/classes

for ALGO in $ALGOS; do
  echo $ALGO  

  for RUN in $RUNS; do
    R=`java -Xmx3000m -cp $CLASSPATH  SortMain $ALGO $RUN`
    echo " - $R "
  done
  
  echo
done

cd - &2>/dev/null







