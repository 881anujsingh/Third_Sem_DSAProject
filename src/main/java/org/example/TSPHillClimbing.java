package org.example;

import java.util.Arrays;
import java.util.Random;

public class TSPHillClimbing {

    private static final Random random = new Random();

    // Generate a random initial tour
    private static int[] generateInitialTour(int n) {
        int[] tour = new int[n];
        for (int i = 0; i < n; i++) {
            tour[i] = i;
        }
        // Shuffle the array to get a random tour
        for (int i = 0; i < n; i++) {
            int randomIndex = random.nextInt(n);
            int temp = tour[i];
            tour[i] = tour[randomIndex];
            tour[randomIndex] = temp;
        }
        return tour;
    }

    // Calculate the total distance of a tour
    private static double calculateTourDistance(int[] tour, double[][] distanceMatrix) {
        double totalDistance = 0;
        for (int i = 0; i < tour.length - 1; i++) {
            totalDistance += distanceMatrix[tour[i]][tour[i + 1]];
        }
        totalDistance += distanceMatrix[tour[tour.length - 1]][tour[0]]; // Return to start
        return totalDistance;
    }

    // Generate a neighboring solution by swapping two cities
    private static int[] generateNeighbor(int[] tour) {
        int n = tour.length;
        int[] newTour = Arrays.copyOf(tour, n);
        int i = random.nextInt(n);
        int j = random.nextInt(n);
        // Swap two cities
        int temp = newTour[i];
        newTour[i] = newTour[j];
        newTour[j] = temp;
        return newTour;
    }

    // Hill Climbing algorithm for TSP
    private static int[] hillClimbing(double[][] distanceMatrix) {
        int n = distanceMatrix.length;
        int[] currentTour = generateInitialTour(n);
        double currentDistance = calculateTourDistance(currentTour, distanceMatrix);

        boolean improvement = true;
        while (improvement) {
            improvement = false;
            int[] newTour = generateNeighbor(currentTour);
            double newDistance = calculateTourDistance(newTour, distanceMatrix);
            if (newDistance < currentDistance) {
                currentTour = newTour;
                currentDistance = newDistance;
                improvement = true;
            }
        }
        return currentTour;
    }

    // Example usage
    public static void main(String[] args) {
        // Example 1: 6 cities with specified distances
        double[][] distanceMatrix1 = {
                {0, 12, 18, 25, 16, 20},
                {12, 0, 30, 35, 22, 25},
                {18, 30, 0, 28, 14, 22},
                {25, 35, 28, 0, 30, 18},
                {16, 22, 14, 30, 0, 20},
                {20, 25, 22, 18, 20, 0}
        };
        int[] bestTour1 = hillClimbing(distanceMatrix1);
        System.out.println("Best tour for example 1: " + Arrays.toString(bestTour1));
        System.out.println("Tour distance: " + calculateTourDistance(bestTour1, distanceMatrix1));

        // Example 2: 7 cities with specified distances
        double[][] distanceMatrix2 = {
                {0, 10, 25, 30, 22, 18, 24},
                {10, 0, 20, 35, 24, 28, 16},
                {25, 20, 0, 14, 30, 18, 20},
                {30, 35, 14, 0, 22, 24, 26},
                {22, 24, 30, 22, 0, 20, 28},
                {18, 28, 18, 24, 20, 0, 15},
                {24, 16, 20, 26, 28, 15, 0}
        };
        int[] bestTour2 = hillClimbing(distanceMatrix2);
        System.out.println("Best tour for example 2: " + Arrays.toString(bestTour2));
        System.out.println("Tour distance: " + calculateTourDistance(bestTour2, distanceMatrix2));
    }
}
