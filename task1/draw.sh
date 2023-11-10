#!/usr/bin/gnuplot -persist

set terminal png size 1024, 768
set output "plot.png"
set grid xtics ytics
set ylabel "Acceleration"
set xlabel "Number of processes"
set key top left
plot "data" every ::1::8 using 1:2 lw 2 with lines title "N=10^3", \
     "data" every ::9::16 using 1:2 lw 2 with lines title "N=10^6", \
     "data" every ::17::24 using 1:2 lw 2 with lines title "N=10^8"
