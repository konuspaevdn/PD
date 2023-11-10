#include <iomanip>
#include <iostream>
#include <mpi.h>

double func(double x) {
    return 4 / (1 + x * x);
}

double trap_area(double l, double r) {
    return 0.5 * (func(l) + func(r)) * (r - l);
}

int main(int argc, char** argv) {

    MPI_Init(&argc, &argv);

    int n = strtol(argv[1], nullptr, 0);
    int p;
    MPI_Comm_size(MPI_COMM_WORLD, &p);

    int world_rank;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);
    if (0 == world_rank) {
        double startTime1 = MPI_Wtime();
        double area1 = 0;
        for (int i = 0; i < n; ++i) {
            area1 += trap_area((i + .0) / n, (i + 1.0) / n);
        }
        double time1 = MPI_Wtime() - startTime1;

        double startTime2 = MPI_Wtime();
        for (int i = 1; i < p; ++i) {
            int l_border = n / p * i;
            int tag = 0;
            MPI_Send(&l_border, 1, MPI_INTEGER, i, tag, MPI_COMM_WORLD);
        }

        double area2 = 0;
        for (int i = 0; i < n / p; ++i) {
            area2 += trap_area((i + .0) / n, (i + 1.0) / n);
        }
        std::cout << "I_0 = " << area2 << std::endl;

        for (int i = 1; i < p; ++i) {
            double partition;
            MPI_Status status;
            MPI_Recv(&partition, 1, MPI_DOUBLE, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
            std::cout << "I_" << i << " = " << partition << std::endl;
            area2 += partition;
        }
        double time2 = MPI_Wtime() - startTime2;

        std::cout << "Parallel computed integral: " << area2 << std::endl;
        std::cout << "Non-parallel computed integral: " << area1 << std::endl;
        std::cout << "S = " << time1 / time2 << std::endl;
    } else {
        int l_border;
        MPI_Status status;
        MPI_Recv(&l_border, 1, MPI_INTEGER, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
        double partition = 0;
        for (int i = 0; l_border + i < n && i < n / p; ++i) {
            partition += trap_area((l_border + i + .0) / n, (l_border + i + 1.) / n);
        }
        int tag = 0;
        MPI_Send(&partition, 1, MPI_DOUBLE, 0, tag, MPI_COMM_WORLD);
    }
    MPI_Finalize();
    return 0;
}
